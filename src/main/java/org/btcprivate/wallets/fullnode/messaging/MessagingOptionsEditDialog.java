package org.btcprivate.wallets.fullnode.messaging;

import org.btcprivate.wallets.fullnode.util.Log;
import org.btcprivate.wallets.fullnode.util.StatusUpdateErrorReporter;
import org.btcprivate.wallets.fullnode.util.Util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;


/**
 * Dialog showing the messaging options and allowing them to be edited.
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class MessagingOptionsEditDialog
        extends JDialog {
    protected JFrame parentFrame;
    protected MessagingStorage storage;
    protected StatusUpdateErrorReporter errorReporter;

    protected JLabel infoLabel;
    protected JPanel buttonPanel;

    protected JTextField amountTextField;
    protected JTextField transactionFeeTextField;
    protected JCheckBox automaticallyAddUsers;

    public MessagingOptionsEditDialog(JFrame parentFrame, MessagingStorage storage, StatusUpdateErrorReporter errorReporter)
            throws IOException {
        this.parentFrame = parentFrame;
        this.storage = storage;
        this.errorReporter = errorReporter;

        this.setTitle("Messaging Options");
        this.setModal(true);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        MessagingOptions options = this.storage.getMessagingOptions();

        this.getContentPane().setLayout(new BorderLayout(0, 0));

        JPanel tempPanel = new JPanel(new BorderLayout(0, 0));
        tempPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        infoLabel = new JLabel(
                "<html><span style=\"font-size:0.93em;\">" +
                        "The following options pertain to messaging:" +
                        "</span>");
        tempPanel.add(infoLabel, BorderLayout.CENTER);
        this.getContentPane().add(tempPanel, BorderLayout.NORTH);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));

        addFormField(detailsPanel, "Automatically add users to contact list? ",
                automaticallyAddUsers = new JCheckBox());
        addFormField(detailsPanel, "Amount of BTCP to send with every message: ", amountTextField = new JTextField(12));
        addFormField(detailsPanel, "Transaction fee: ", transactionFeeTextField = new JTextField(12));

        DecimalFormatSymbols decSymbols = new DecimalFormatSymbols(Locale.ROOT);
        automaticallyAddUsers.setSelected(options.isAutomaticallyAddUsersIfNotExplicitlyImported());
        amountTextField.setText(new DecimalFormat("########0.00######", decSymbols).format(options.getAmountToSend()));
        transactionFeeTextField.setText(new DecimalFormat("########0.00######", decSymbols).format(options.getTransactionFee()));

        detailsPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        this.getContentPane().add(detailsPanel, BorderLayout.CENTER);

        // Lower buttons - by default only close is available
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 3));
        JButton closeButton = new JButton("Close");
        buttonPanel.add(closeButton);
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MessagingOptionsEditDialog.this.setVisible(false);
                MessagingOptionsEditDialog.this.dispose();
            }
        });

        JButton saveButton = new JButton("Save & Close");
        buttonPanel.add(saveButton);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String amountToSend = MessagingOptionsEditDialog.this.amountTextField.getText();
                    String transactionFee = MessagingOptionsEditDialog.this.transactionFeeTextField.getText();

                    if ((!MessagingOptionsEditDialog.this.verifyNumericField("amount to send", amountToSend)) ||
                            (!MessagingOptionsEditDialog.this.verifyNumericField("transaction fee", transactionFee))) {
                        return;
                    }

                    MessagingOptions options = MessagingOptionsEditDialog.this.storage.getMessagingOptions();

                    options.setAmountToSend(Double.parseDouble(amountToSend));
                    options.setTransactionFee(Double.parseDouble(transactionFee));
                    options.setAutomaticallyAddUsersIfNotExplicitlyImported(
                            MessagingOptionsEditDialog.this.automaticallyAddUsers.isSelected());

                    MessagingOptionsEditDialog.this.storage.updateMessagingOptions(options);

                    MessagingOptionsEditDialog.this.setVisible(false);
                    MessagingOptionsEditDialog.this.dispose();
                } catch (Exception ex) {
                    Log.error("Unexpected error editing My Identity!", ex);
                    MessagingOptionsEditDialog.this.errorReporter.reportError(ex, false);
                }
            }
        });

        this.pack();
        this.setLocation(100, 100);
        this.setLocationRelativeTo(parentFrame);
    }


    private boolean verifyNumericField(String name, String value) {
        if (Util.stringIsEmpty(value)) {
            JOptionPane.showMessageDialog(
                    this.parentFrame,
                    "Field \"" + name + "\" is required.",
                    "\"" + name + "\" Required", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            double dVal = Double.parseDouble(value);

            if (dVal < 0) {
                JOptionPane.showMessageDialog(
                        this.parentFrame,
                        "Field \"" + name + "\" must be a positive number!",
                        "\"" + name + "\" Must Be Positive", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(
                    this.parentFrame,
                    "Field \"" + name + "\" must be a number!",
                    "\"" + name + "\" Must Be A Number", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }


    private void addFormField(JPanel detailsPanel, String name, JComponent field) {
        JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        JLabel tempLabel = new JLabel(name, JLabel.RIGHT);
        // TODO: hard sizing of labels may not scale!
        final int width = new JLabel("Amount of BTCP to send with every message: ").getPreferredSize().width + 30;
        tempLabel.setPreferredSize(new Dimension(width, tempLabel.getPreferredSize().height));
        tempPanel.add(tempLabel);
        tempPanel.add(field);
        detailsPanel.add(tempPanel);
    }

}