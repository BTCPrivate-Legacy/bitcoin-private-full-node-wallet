package org.btcprivate.wallets.fullnode.ui;

import org.btcprivate.wallets.fullnode.daemon.BTCPClientCaller;
import org.btcprivate.wallets.fullnode.daemon.BTCPClientCaller.WalletCallException;
import org.btcprivate.wallets.fullnode.ui.WalletTextField;
import org.btcprivate.wallets.fullnode.util.Log;
import org.btcprivate.wallets.fullnode.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;


/**
 * Dialog to enter a single private key to import
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class SingleKeyImportDialog
        extends JDialog {
    protected boolean isOKPressed = false;
    protected String key = null;

    protected JLabel     keyLabel = null;
    protected WalletTextField keyField = null;

    protected JLabel upperLabel;
    protected JLabel lowerLabel;

    protected JProgressBar progress = null;

    protected BTCPClientCaller caller;

    JButton okButton;
    JButton cancelButton;

    private static final String LOCAL_MSG_WARNING = Util.local("LOCAL_MSG_WARNING");
    private static final String LOCAL_MSG_IMPORT_PK = Util.local("LOCAL_MSG_IMPORT_PK");
    private static final String LOCAL_MSG_IMPORT_PK_DETAIL = Util.local("LOCAL_MSG_IMPORT_PK_DETAIL");
    private static final String LOCAL_MSG_IMPORT_PK_WARNING = Util.local("LOCAL_MSG_IMPORT_PK_WARNING");
    private static final String LOCAL_MSG_IMPORT = Util.local("LOCAL_MSG_IMPORT");
    private static final String LOCAL_MSG_CANCEL = Util.local("LOCAL_MSG_CANCEL");
    private static final String LOCAL_MSG_ENTER_KEY = Util.local("LOCAL_MSG_ENTER_KEY");
    private static final String LOCAL_MSG_NO_KEY_ENTERED = Util.local("LOCAL_MSG_NO_KEY_ENTERED");
    private static final String LOCAL_MSG_KEY_TO_ADDRESS = Util.local("LOCAL_MSG_KEY_TO_ADDRESS");
    private static final String LOCAL_MSG_IMPORT_SUCCESS = Util.local("LOCAL_MSG_IMPORT_SUCCESS");
    private static final String LOCAL_MSG_IMPORT_SUCCESS_NO_BALANCE = Util.local("LOCAL_MSG_IMPORT_SUCCESS_NO_BALANCE");
    private static final String LOCAL_MSG_IMPORT_SUCCESS_DETAIL_1 = Util.local("LOCAL_MSG_IMPORT_SUCCESS_DETAIL_1");
    private static final String LOCAL_MSG_IMPORT_SUCCESS_DETAIL_2 = Util.local("LOCAL_MSG_IMPORT_SUCCESS_DETAIL_2");
    private static final String LOCAL_MSG_IMPORT_SUCCESS_DETAIL_TITLE = Util.local("LOCAL_MSG_IMPORT_SUCCESS_DETAIL_TITLE");
    private static final String LOCAL_MSG_IMPORT_SUCCESS_DETAIL_NO_SWEEP = Util.local("LOCAL_MSG_IMPORT_SUCCESS_DETAIL_NO_SWEEP");
    private static final String LOCAL_MSG_SWEEP_SUCCESS = Util.local("LOCAL_MSG_SWEEP_SUCCESS");
    private static final String LOCAL_MSG_SWEEP_FROM = Util.local("LOCAL_MSG_SWEEP_FROM");
    private static final String LOCAL_MSG_SWEEP_TO = Util.local("LOCAL_MSG_SWEEP_TO");
    private static final String LOCAL_MSG_SWEEP_ERROR = Util.local("LOCAL_MSG_SWEEP_ERROR");
    private static final String LOCAL_MSG_SWEEP_ERROR_DETAIL_1 = Util.local("LOCAL_MSG_SWEEP_ERROR_DETAIL_1");
    private static final String LOCAL_MSG_SWEEP_ERROR_DETAIL_2 = Util.local("LOCAL_MSG_SWEEP_ERROR_DETAIL_2");


    public SingleKeyImportDialog(JFrame parent, BTCPClientCaller caller) {
        super(parent);
        this.caller = caller;

        this.setTitle(LOCAL_MSG_IMPORT_PK);
        this.setLocation(parent.getLocation().x + 50, parent.getLocation().y + 50);
        this.setModal(true);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
        controlsPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel tempPanel = new JPanel(new BorderLayout(0, 0));
        tempPanel.add(this.upperLabel = new JLabel(
                "<html>" + LOCAL_MSG_IMPORT_PK_DETAIL +
                        "</html>"), BorderLayout.CENTER);
        controlsPanel.add(tempPanel);

        JLabel dividerLabel = new JLabel("   ");
        dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 8));
        controlsPanel.add(dividerLabel);

        tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tempPanel.add(keyLabel = new JLabel("Key: "));
        tempPanel.add(keyField = new WalletTextField(60));
        controlsPanel.add(tempPanel);

        dividerLabel = new JLabel("   ");
        dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 8));
        controlsPanel.add(dividerLabel);

        tempPanel = new JPanel(new BorderLayout(0, 0));
        tempPanel.add(this.lowerLabel = new JLabel(
                        "<html><span style=\"font-weight:bold\">" +
                                LOCAL_MSG_WARNING + ":</span>" + LOCAL_MSG_IMPORT_PK_WARNING + "</html>"),
                BorderLayout.CENTER);
        controlsPanel.add(tempPanel);

        dividerLabel = new JLabel("   ");
        dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 8));
        controlsPanel.add(dividerLabel);

        tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tempPanel.add(progress = new JProgressBar());
        controlsPanel.add(tempPanel);

        this.getContentPane().setLayout(new BorderLayout(0, 0));
        this.getContentPane().add(controlsPanel, BorderLayout.NORTH);

        // Form buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 3));
        okButton = new JButton(LOCAL_MSG_IMPORT);
        buttonPanel.add(okButton);
        buttonPanel.add(new JLabel("   "));
        cancelButton = new JButton(LOCAL_MSG_CANCEL);
        buttonPanel.add(cancelButton);
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        okButton.addActionListener(e -> SingleKeyImportDialog.this.processOK());

        cancelButton.addActionListener(e -> {
            SingleKeyImportDialog.this.setVisible(false);
            SingleKeyImportDialog.this.dispose();

            SingleKeyImportDialog.this.isOKPressed = false;
            SingleKeyImportDialog.this.key = null;
        });

        this.setSize(740, 210);
        this.validate();
        this.repaint();

        this.pack();
    }


    protected void processOK() {
        final String key = SingleKeyImportDialog.this.keyField.getText();

        if ((key == null) || (key.trim().length() <= 0)) {
            JOptionPane.showMessageDialog(
                    SingleKeyImportDialog.this.getParent(),
                    LOCAL_MSG_ENTER_KEY, LOCAL_MSG_NO_KEY_ENTERED,
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        SingleKeyImportDialog.this.isOKPressed = true;
        SingleKeyImportDialog.this.key = key;

        // Start import
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.progress.setIndeterminate(true);
        this.progress.setValue(1);

        this.okButton.setEnabled(false);
        this.cancelButton.setEnabled(false);

        SingleKeyImportDialog.this.keyField.setEditable(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String address = SingleKeyImportDialog.this.caller.importPrivateKey(key);
                    String addition = "";

                    if (!Util.stringIsEmpty(address)) {
                        addition = " " + LOCAL_MSG_KEY_TO_ADDRESS + ":\n" + address;
                    } else {

                        address = getAddressForPrivateKey(key);
                        //still NULL if no balance, since looping over balances to get PKs and check a matching PK.
                        if (Util.stringIsEmpty(address)) {
                            //show insufficient balance warning. let them know that they can still manually sweep later on should the blockchain not be synced 100% yet.
                            JOptionPane.showMessageDialog(
                                    SingleKeyImportDialog.this.getRootPane().getParent(),
                                    LOCAL_MSG_IMPORT_SUCCESS,
                                    LOCAL_MSG_IMPORT_SUCCESS_NO_BALANCE, JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    }

                    int doSweep = JOptionPane.showConfirmDialog(
                            SingleKeyImportDialog.this,
                            LOCAL_MSG_IMPORT_SUCCESS_DETAIL_1 + ":\n" +
                                    key + "\n"
                                    + addition
                                    + "\n\n"
                                    + LOCAL_MSG_IMPORT_SUCCESS_DETAIL_2,
                            LOCAL_MSG_IMPORT_SUCCESS_DETAIL_TITLE,
                            JOptionPane.YES_NO_OPTION);

                    if (doSweep == JOptionPane.YES_OPTION) {
                        float txnFee = 0.0001f;
                        String sweepZ = SingleKeyImportDialog.this.caller.createNewAddress(true);
                        String stringBalance = SingleKeyImportDialog.this.caller.getBalanceForAddress(address);
                        //full amount minus default txn fee
                        float balance = Float.parseFloat(stringBalance);
                        if (balance == 0 || balance <= txnFee) {
                            //show insufficient balance warning. let them know that they can still manually sweep later on should the blockchain not be synced 100% yet.
                            JOptionPane.showMessageDialog(
                                    SingleKeyImportDialog.this.getRootPane().getParent(),
                                    LOCAL_MSG_IMPORT_SUCCESS_DETAIL_NO_SWEEP,
                                    LOCAL_MSG_IMPORT_SUCCESS_NO_BALANCE, JOptionPane.ERROR_MESSAGE);
                        } else {
                            float amount = balance - txnFee;

                            SingleKeyImportDialog.this.caller.sendCash(address, sweepZ, String.valueOf(amount), "", String.valueOf(txnFee));
                            JOptionPane.showMessageDialog(
                                    SingleKeyImportDialog.this.getRootPane().getParent(),
                                    LOCAL_MSG_SWEEP_SUCCESS + ": " + String.valueOf(amount) + "BTCP" + LOCAL_MSG_SWEEP_FROM + address + "\n"
                                            + " " + LOCAL_MSG_SWEEP_TO + " " + sweepZ,
                                    LOCAL_MSG_SWEEP_SUCCESS, JOptionPane.INFORMATION_MESSAGE);
                        }
                    }


                } catch (Exception e) {
                    Log.error("An error occurred when importing private key", e);

                    JOptionPane.showMessageDialog(
                            SingleKeyImportDialog.this.getRootPane().getParent(),
                            LOCAL_MSG_SWEEP_ERROR_DETAIL_1 + ":\n" +
                                    e.getClass().getName() + ":\n" + e.getMessage() + "\n\n" +
                                    LOCAL_MSG_SWEEP_ERROR_DETAIL_2,
                            LOCAL_MSG_SWEEP_ERROR, JOptionPane.ERROR_MESSAGE);
                } finally {
                    SingleKeyImportDialog.this.setVisible(false);
                    SingleKeyImportDialog.this.dispose();
                }
            }

            private String getAddressForPrivateKey(String privKey) {
                BTCPClientCaller caller = SingleKeyImportDialog.this.caller;
                String address = null;
                try {
                    //if found, return
                    if (address != null) return address;
                    //else continue looking in other addresses
                    for (String a : caller.getWalletZAddresses()) {
                        if (caller.getZPrivateKey(a).equals(privKey)) {
                            address = a;
                            break;
                        }
                    }
                    //if found, return
                    if (address != null) return address;
                    for (String a : caller.getWalletPublicAddressesWithUnspentOutputs()) {
                        if (caller.getTPrivateKey(a).equals(privKey)) {
                            address = a;
                            break;
                        }
                    }

                } catch (WalletCallException | IOException | InterruptedException e) {
                    Log.error("Error retrieving address for private key. Error: " + e.getMessage());
                }
                return address;
            }
        }).start();
    }
}
