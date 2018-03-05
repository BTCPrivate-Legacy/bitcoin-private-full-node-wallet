package org.btcprivate.wallets.fullnode.ui;


import org.btcprivate.wallets.fullnode.daemon.BTCPClientCaller;
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
    public AddressTable(final Object[][] rowData, final Object[] columnNames,
                        final BTCPClientCaller caller)
    {
        super(rowData, columnNames);
        int accelaratorKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        JMenuItem obtainPrivateKey = new JMenuItem("Obtain private key");
        //obtainPrivateKey.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, accelaratorKeyMask));
        popupMenu.add(obtainPrivateKey);

        obtainPrivateKey.addActionListener(new ActionListener()
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
    } // End constructor

}