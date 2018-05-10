package org.btcprivate.wallets.fullnode.messaging;

import org.btcprivate.wallets.fullnode.ui.WalletTextField;
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

  protected WalletTextField amountTextField;
  protected WalletTextField transactionFeeTextField;
  protected JCheckBox automaticallyAddUsers;


  private static final String LOCAL_MSG_MESSAGE_OPTS = Util.local("LOCAL_MSG_MESSAGE_OPTS");
  private static final String LOCAL_MSG_MESSAGE_OPTS_SHOW = Util.local("LOCAL_MSG_MESSAGE_OPTS_SHOW");
  private static final String LOCAL_MSG_AUTO_ADD_USER = Util.local("LOCAL_MSG_AUTO_ADD_USER");
  private static final String LOCAL_MSG_AMOUN_PER_MSG = Util.local("LOCAL_MSG_AMOUN_PER_MSG");
  private static final String LOCAL_MSG_TXN_FEE_MSG = Util.local("LOCAL_MSG_TXN_FEE_MSG");
  private static final String LOCAL_MSG_CLOSE = Util.local("LOCAL_MSG_CLOSE");
  private static final String LOCAL_MSG_SAVE_CLOSE = Util.local("LOCAL_MSG_SAVE_CLOSE");
  private static final String LOCAL_MSG_AMOUNT_TO_SEND = Util.local("LOCAL_MSG_AMOUNT_TO_SEND");
  private static final String LOCAL_MSG_TXN_FEE = Util.local("LOCAL_MSG_TXN_FEE");
  private static final String LOCAL_MSG_REQ_FIELD = Util.local("LOCAL_MSG_REQ_FIELD");
  private static final String LOCAL_MSG_REQ = Util.local("LOCAL_MSG_REQ");
  private static final String LOCAL_MSG_POS_NR = Util.local("LOCAL_MSG_POS_NR");
  private static final String LOCAL_MSG_ERROR = Util.local("LOCAL_MSG_ERROR");


  public MessagingOptionsEditDialog(JFrame parentFrame, MessagingStorage storage, StatusUpdateErrorReporter errorReporter)
      throws IOException {
    this.parentFrame = parentFrame;
    this.storage = storage;
    this.errorReporter = errorReporter;

    this.setTitle(LOCAL_MSG_MESSAGE_OPTS);
    this.setModal(true);
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    MessagingOptions options = this.storage.getMessagingOptions();

    this.getContentPane().setLayout(new BorderLayout(0, 0));

    JPanel tempPanel = new JPanel(new BorderLayout(0, 0));
    tempPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    infoLabel = new JLabel(
        "<html><span style=\"font-size:0.93em;\">" + LOCAL_MSG_MESSAGE_OPTS_SHOW + "</span>");
    tempPanel.add(infoLabel, BorderLayout.CENTER);
    this.getContentPane().add(tempPanel, BorderLayout.NORTH);

    JPanel detailsPanel = new JPanel();
    detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));

    addFormField(detailsPanel, LOCAL_MSG_AUTO_ADD_USER,
        automaticallyAddUsers = new JCheckBox());
    addFormField(detailsPanel, LOCAL_MSG_AMOUN_PER_MSG, amountTextField = new WalletTextField(12));
    addFormField(detailsPanel, LOCAL_MSG_TXN_FEE_MSG, transactionFeeTextField = new WalletTextField(12));

    DecimalFormatSymbols decSymbols = new DecimalFormatSymbols(Locale.ROOT);
    automaticallyAddUsers.setSelected(options.isAutomaticallyAddUsersIfNotExplicitlyImported());
    amountTextField.setText(new DecimalFormat("########0.00######", decSymbols).format(options.getAmountToSend()));
    transactionFeeTextField.setText(new DecimalFormat("########0.00######", decSymbols).format(options.getTransactionFee()));

    detailsPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    this.getContentPane().add(detailsPanel, BorderLayout.CENTER);

    // Lower buttons - by default only close is available
    buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 3));
    JButton closeButton = new JButton(LOCAL_MSG_CLOSE);
    buttonPanel.add(closeButton);
    this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

    closeButton.addActionListener(e -> {
      MessagingOptionsEditDialog.this.setVisible(false);
      MessagingOptionsEditDialog.this.dispose();
    });

    JButton saveButton = new JButton(LOCAL_MSG_SAVE_CLOSE);
    buttonPanel.add(saveButton);
    saveButton.addActionListener(e -> {
      try {
        String amountToSend = MessagingOptionsEditDialog.this.amountTextField.getText();
        String transactionFee = MessagingOptionsEditDialog.this.transactionFeeTextField.getText();

        if ((!MessagingOptionsEditDialog.this.verifyNumericField(LOCAL_MSG_AMOUNT_TO_SEND, amountToSend)) ||
            (!MessagingOptionsEditDialog.this.verifyNumericField(LOCAL_MSG_TXN_FEE, transactionFee))) {
          return;
        }

        MessagingOptions options1 = MessagingOptionsEditDialog.this.storage.getMessagingOptions();

        options1.setAmountToSend(Double.parseDouble(amountToSend));
        options1.setTransactionFee(Double.parseDouble(transactionFee));
        options1.setAutomaticallyAddUsersIfNotExplicitlyImported(
            MessagingOptionsEditDialog.this.automaticallyAddUsers.isSelected());

        MessagingOptionsEditDialog.this.storage.updateMessagingOptions(options1);

        MessagingOptionsEditDialog.this.setVisible(false);
        MessagingOptionsEditDialog.this.dispose();
      } catch (Exception ex) {
        Log.error("Unexpected error editing My Identity!", ex);
        MessagingOptionsEditDialog.this.errorReporter.reportError(ex, false);
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
          LOCAL_MSG_REQ_FIELD + name,
          name + LOCAL_MSG_REQ, JOptionPane.ERROR_MESSAGE);
      return false;
    }

    try {
      double dVal = Double.parseDouble(value);

      if (dVal < 0) {
        JOptionPane.showMessageDialog(
            this.parentFrame,
            LOCAL_MSG_POS_NR + name,
            LOCAL_MSG_ERROR, JOptionPane.ERROR_MESSAGE);
        return false;
      }
    } catch (NumberFormatException nfe) {
      JOptionPane.showMessageDialog(
          this.parentFrame,
          LOCAL_MSG_POS_NR + name,
          LOCAL_MSG_ERROR, JOptionPane.ERROR_MESSAGE);
      return false;
    }

    return true;
  }


  private void addFormField(JPanel detailsPanel, String name, JComponent field) {
    JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
    JLabel tempLabel = new JLabel(name, JLabel.RIGHT);
    final int width = new JLabel(LOCAL_MSG_AMOUN_PER_MSG).getPreferredSize().width + 30;
    tempLabel.setPreferredSize(new Dimension(width, tempLabel.getPreferredSize().height));
    tempPanel.add(tempLabel);
    tempPanel.add(field);
    detailsPanel.add(tempPanel);
  }

}