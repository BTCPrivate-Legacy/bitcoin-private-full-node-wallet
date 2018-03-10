package org.btcprivate.wallets.fullnode.ui;


import org.btcprivate.wallets.fullnode.daemon.BTCPClientCaller;
import org.btcprivate.wallets.fullnode.util.Log;
import org.btcprivate.wallets.fullnode.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;


/**
 * Table to be used for addresses - specifically.
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class AddressTable
        extends DataTable {

    private static final String LOCAL_MENU_GET_PK = Util.local("LOCAL_MENU_GET_PK");
    private static final String LOCAL_MENU_PK_INFO_1 = Util.local("LOCAL_MENU_PK_INFO_1");
    private static final String LOCAL_MENU_PK_INFO_2 = Util.local("LOCAL_MENU_PK_INFO_2");
    private static final String LOCAL_MENU_PK_INFO_3 = Util.local("LOCAL_MENU_PK_INFO_3");
    private static final String LOCAL_MSG_ERROR_GET_PK = Util.local("LOCAL_MSG_ERROR_GET_PK");

    public AddressTable(final Object[][] rowData, final Object[] columnNames,
                        final BTCPClientCaller caller) {
        super(rowData, columnNames);
        JMenuItem obtainPrivateKey = new JMenuItem(LOCAL_MENU_GET_PK);
        popupMenu.add(obtainPrivateKey);

        obtainPrivateKey.addActionListener(e -> {
            if ((lastRow >= 0) && (lastColumn >= 0)) {
                try {
                    String address = AddressTable.this.getModel().getValueAt(lastRow, 2).toString();
                    boolean isZAddress = Util.isZAddress(address);

                    // Check for encrypted wallet
                    final boolean bEncryptedWallet = caller.isWalletEncrypted();
                    if (bEncryptedWallet) {
                        PasswordDialog pd = new PasswordDialog((JFrame) (AddressTable.this.getRootPane().getParent()));
                        pd.setVisible(true);

                        if (!pd.isOKPressed()) {
                            return;
                        }

                        caller.unlockWallet(pd.getPassword());
                    }

                    String privateKey = isZAddress ?
                            caller.getZPrivateKey(address) : caller.getTPrivateKey(address);

                    // Lock the wallet again
                    if (bEncryptedWallet) {
                        caller.lockWallet();
                    }

                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(new StringSelection(privateKey), null);

                    JOptionPane.showMessageDialog(
                            AddressTable.this.getRootPane().getParent(),
                            LOCAL_MENU_PK_INFO_1 + "\n" +
                                    address + "\n" +
                                    LOCAL_MENU_PK_INFO_2 + "\n" +
                                    privateKey + "\n\n" +
                                    LOCAL_MENU_PK_INFO_3,
                            "Private Key", JOptionPane.INFORMATION_MESSAGE);


                } catch (Exception ex) {
                    Log.error("Unexpected error: ", ex);
                    JOptionPane.showMessageDialog(
                            AddressTable.this.getRootPane().getParent(),
                            LOCAL_MSG_ERROR_GET_PK + ": \n" +
                                    ex.getMessage() + "\n\n",
                            LOCAL_MSG_ERROR_GET_PK,
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Log perhaps
            }
        });
    } // End constructor
}