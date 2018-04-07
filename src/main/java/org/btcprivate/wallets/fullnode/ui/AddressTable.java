package org.btcprivate.wallets.fullnode.ui;


import org.btcprivate.wallets.fullnode.daemon.BTCPClientCaller;
import org.btcprivate.wallets.fullnode.daemon.BTCPClientCaller.ShieldCoinbaseResponse;
import org.btcprivate.wallets.fullnode.util.Log;
import org.btcprivate.wallets.fullnode.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Table to be used for addresses - specifically.
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class AddressTable
        extends DataTable
{

	protected JPopupMenu zAddressPopupMenu;
	protected BTCPClientCaller caller;

    public AddressTable(final Object[][] rowData, final Object[] columnNames,
                        final BTCPClientCaller caller)
    {
        super(rowData, columnNames);
        
        this.caller = caller;
        
        // instantiate and do basic setup for popup menu for z-addresses
        zAddressPopupMenu = new JPopupMenu();
        zAddressPopupMenu.add(instantiateCopyMenuItem());
        zAddressPopupMenu.add(instantiateExportToCSVMenuItem());
        
        int accelaratorKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        popupMenu.add(instantiateObtainPrivateKeyMenuItem());
        zAddressPopupMenu.add(instantiateObtainPrivateKeyMenuItem());
        
        zAddressPopupMenu.add(instantiateShieldAllCoinbaseMenuItem());


    } // End constructor
    
    protected JMenuItem instantiateObtainPrivateKeyMenuItem() {
        JMenuItem menuItem = new JMenuItem("Obtain private key");
        //obtainPrivateKey.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, accelaratorKeyMask));
        
        menuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if ((lastRow >= 0) && (lastColumn >= 0))
                {
                    try
                    {
                        String address = AddressTable.this.getModel().getValueAt(lastRow, 2).toString();
                        boolean isZAddress = Util.isZAddress(address);
						
                        // Check for encrypted wallet
                        final boolean bEncryptedWallet = caller.isWalletEncrypted();
                        if (bEncryptedWallet)
                        {
                            PasswordDialog pd = new PasswordDialog((JFrame)(AddressTable.this.getRootPane().getParent()));
                            pd.setVisible(true);

                            if (!pd.isOKPressed())
                            {
                                return;
                            }

                            caller.unlockWallet(pd.getPassword());
                        }

                        String privateKey = isZAddress ?
                                caller.getZPrivateKey(address) : caller.getTPrivateKey(address);

                        // Lock the wallet again
                        if (bEncryptedWallet)
                        {
                            caller.lockWallet();
                        }

                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clipboard.setContents(new StringSelection(privateKey), null);

                        JOptionPane.showMessageDialog(
                                AddressTable.this.getRootPane().getParent(),
                                (isZAddress ? "Z (Private)" : "T (Transparent)") +  " address:\n" +
                                        address + "\n" +
                                        "has private key:\n" +
                                        privateKey + "\n\n" +
                                        "The private key has also been copied to the clipboard.",
                                "Private key information", JOptionPane.INFORMATION_MESSAGE);


                    } catch (Exception ex)
                    {
                        Log.error("Unexpected error: ", ex);
                        JOptionPane.showMessageDialog(
                                AddressTable.this.getRootPane().getParent(),
                                "Error in obtaining private key:" + "\n" +
                                        ex.getMessage() + "\n\n",
                                "Error in obtaining private key!",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else
                {
                    // Log perhaps
                }
            }
        });
		return menuItem;
    }
    
    protected JMenuItem instantiateShieldAllCoinbaseMenuItem() {
        JMenuItem shieldAllCoinbaseFundsMenuItem = new JMenuItem("Shield all coinbases to this z-address");
        //obtainPrivateKey.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, accelaratorKeyMask));
        
        shieldAllCoinbaseFundsMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if ((lastRow >= 0) && (lastColumn >= 0))
                {
                    try
                    {
                        String address = AddressTable.this.getModel().getValueAt(lastRow, 2).toString();
                        boolean isZAddress = Util.isZAddress(address);

						if (!isZAddress) {
							// TODO: this should never happen- gracefully error out
							return;
						}
                        // Check for encrypted wallet
                        final boolean bEncryptedWallet = caller.isWalletEncrypted();
                        if (bEncryptedWallet)
                        {
                            PasswordDialog pd = new PasswordDialog((JFrame)(AddressTable.this.getRootPane().getParent()));
                            pd.setVisible(true);

                            if (!pd.isOKPressed())
                            {
                                return;
                            }

                            caller.unlockWallet(pd.getPassword());
                        }

						ShieldCoinbaseResponse shieldCoinbaseResponse = caller.shieldCoinbase("*", address);

                        // Lock the wallet again
                        if (bEncryptedWallet)
                        {
                            caller.lockWallet();
                        }

                        JOptionPane.showMessageDialog(
                                AddressTable.this.getRootPane().getParent(),
                                "Coinbase funds in the amount of " + shieldCoinbaseResponse.shieldedValue + " BTCP from " + shieldCoinbaseResponse.shieldedUTXOs + " UTXO" + (shieldCoinbaseResponse.shieldedUTXOs == 1 ? "" : "s") + " were shielded to the following z-address:\n" + address + "\nPlease Refresh to update balances.\n",
                                "Shield All Coinbases", JOptionPane.INFORMATION_MESSAGE);

						// TODO: trigger refresh of window

                    } catch (Exception ex)
                    {
                        Log.error("Unexpected error: ", ex);
                        JOptionPane.showMessageDialog(
                                AddressTable.this.getRootPane().getParent(),
                                "Error:" + "\n" +
                                        ex.getMessage() + "\n\n",
                                "Error in shielding all coinbases!",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else
                {
                    // Log perhaps
                }
            }
        });
		return shieldAllCoinbaseFundsMenuItem;
    }
    @Override
	protected JPopupMenu getPopupMenu(int row, int column) 
	{
	    String address = AddressTable.this.getModel().getValueAt(row, 2).toString();
        boolean isZAddress = Util.isZAddress(address);
		if (isZAddress) 
		{
			return zAddressPopupMenu;
		}
		return popupMenu;
	}
	
}