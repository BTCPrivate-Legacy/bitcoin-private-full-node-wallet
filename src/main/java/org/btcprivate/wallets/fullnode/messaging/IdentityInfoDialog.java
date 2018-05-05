package org.btcprivate.wallets.fullnode.messaging;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.btcprivate.wallets.fullnode.ui.WalletTextArea;
import org.btcprivate.wallets.fullnode.ui.WalletTextField;
import org.btcprivate.wallets.fullnode.util.Util;

public class IdentityInfoDialog
    extends JDialog {
  protected JFrame parentFrame;
  protected MessagingIdentity identity;

  protected JLabel infoLabel;

  protected JPanel buttonPanel;

  protected WalletTextField nicknameTextField;
  protected WalletTextArea sendreceiveaddressTextArea;
  protected WalletTextField senderidaddressTextField;
  protected WalletTextField firstnameTextField;
  protected WalletTextField middlenameTextField;
  protected WalletTextField surnameTextField;
  protected WalletTextField emailTextField;
  protected WalletTextField streetaddressTextField;
  protected WalletTextField facebookTextField;
  protected WalletTextField twitterTextField;


  private static final String LOCAL_MSG_CONTACT_DETAILS = Util.local("LOCAL_MSG_CONTACT_DETAILS");
  private static final String LOCAL_MSG_TXN_CLOSE = Util.local("LOCAL_MSG_TXN_CLOSE");
  private static final String LOCAL_MSG_CONTACT_INFO = Util.local("LOCAL_MSG_CONTACT_INFO");
  private static final String LOCAL_MSG_NICKNAME = Util.local("LOCAL_MSG_NICKNAME");
  private static final String LOCAL_MSG_FIRST_NAME = Util.local("LOCAL_MSG_FIRST_NAME");
  private static final String LOCAL_MSG_MIDDLE_NAME = Util.local("LOCAL_MSG_MIDDLE_NAME");
  private static final String LOCAL_MSG_SURNAME = Util.local("LOCAL_MSG_SURNAME");
  private static final String LOCAL_MSG_EMAIL = Util.local("LOCAL_MSG_EMAIL");
  private static final String LOCAL_MSG_STREET_ADDRESS = Util.local("LOCAL_MSG_STREET_ADDRESS");
  private static final String LOCAL_MSG_FB = Util.local("LOCAL_MSG_FB");
  private static final String LOCAL_MSG_TW = Util.local("LOCAL_MSG_TW");
  private static final String LOCAL_MSG_SENDER_ID_B_ADDR = Util.local("LOCAL_MSG_SENDER_ID_B_ADDR");
  private static final String LOCAL_MSG_SENDER_RECEIVE_Z_ADDR = Util.local("LOCAL_MSG_SENDER_RECEIVE_Z_ADDR");


  public IdentityInfoDialog(JFrame parentFrame, MessagingIdentity identity) {
    this.parentFrame = parentFrame;
    this.identity = identity;

    this.setTitle(LOCAL_MSG_CONTACT_DETAILS + identity.getDiplayString());
    this.setModal(true);
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    this.getContentPane().setLayout(new BorderLayout(0, 0));

    JPanel tempPanel = new JPanel(new BorderLayout(0, 0));
    tempPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    infoLabel = new JLabel(
        "<html><span style=\"font-size:0.97em;\">" + LOCAL_MSG_CONTACT_INFO + identity.getNickname() + "</span>");
    tempPanel.add(infoLabel, BorderLayout.CENTER);
    this.getContentPane().add(tempPanel, BorderLayout.NORTH);

    JPanel detailsPanel = new JPanel();
    detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));

    addFormField(detailsPanel, LOCAL_MSG_NICKNAME, nicknameTextField = new WalletTextField(40));
    addFormField(detailsPanel, LOCAL_MSG_FIRST_NAME, firstnameTextField = new WalletTextField(40));
    addFormField(detailsPanel, LOCAL_MSG_MIDDLE_NAME, middlenameTextField = new WalletTextField(40));
    addFormField(detailsPanel, LOCAL_MSG_SURNAME, surnameTextField = new WalletTextField(40));

    addFormField(detailsPanel, LOCAL_MSG_EMAIL, emailTextField = new WalletTextField(40));
    addFormField(detailsPanel, LOCAL_MSG_STREET_ADDRESS, streetaddressTextField = new WalletTextField(40));
    addFormField(detailsPanel, LOCAL_MSG_FB, facebookTextField = new WalletTextField(40));
    addFormField(detailsPanel, LOCAL_MSG_TW, twitterTextField = new WalletTextField(40));

    addFormField(detailsPanel, LOCAL_MSG_SENDER_ID_B_ADDR, senderidaddressTextField = new WalletTextField(40));
    addFormField(detailsPanel, LOCAL_MSG_SENDER_RECEIVE_Z_ADDR, sendreceiveaddressTextArea = new WalletTextArea(2, 40));
    sendreceiveaddressTextArea.setLineWrap(true);


    nicknameTextField.setText(this.identity.getNickname());
    firstnameTextField.setText(this.identity.getFirstname());
    middlenameTextField.setText(this.identity.getMiddlename());
    surnameTextField.setText(this.identity.getSurname());
    emailTextField.setText(this.identity.getEmail());
    streetaddressTextField.setText(this.identity.getStreetaddress());
    facebookTextField.setText(this.identity.getFacebook());
    twitterTextField.setText(this.identity.getTwitter());
    senderidaddressTextField.setText(this.identity.getSenderidaddress());
    sendreceiveaddressTextArea.setText(this.identity.getSendreceiveaddress());

    nicknameTextField.setEditable(false);
    firstnameTextField.setEditable(false);
    middlenameTextField.setEditable(false);
    surnameTextField.setEditable(false);
    emailTextField.setEditable(false);
    streetaddressTextField.setEditable(false);
    facebookTextField.setEditable(false);
    twitterTextField.setEditable(false);
    senderidaddressTextField.setEditable(false);
    sendreceiveaddressTextArea.setEditable(false);

    detailsPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    this.getContentPane().add(detailsPanel, BorderLayout.CENTER);

    // Lower buttons - by default only close is available
    buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 3));
    JButton closeButon = new JButton(LOCAL_MSG_TXN_CLOSE);
    buttonPanel.add(closeButon);
    this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

    closeButon.addActionListener(e -> {
      IdentityInfoDialog.this.setVisible(false);
      IdentityInfoDialog.this.dispose();
    });

    this.pack();
    this.setLocation(100, 100);
    this.setLocationRelativeTo(parentFrame);
  }


  private void addFormField(JPanel detailsPanel, String name, JComponent field) {
    JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
    JLabel tempLabel = new JLabel(name, JLabel.RIGHT);
    // TODO: hard sizing of labels may not scale!
    final int width = new JLabel(LOCAL_MSG_SENDER_ID_B_ADDR).getPreferredSize().width + 10;
    tempLabel.setPreferredSize(new Dimension(width, tempLabel.getPreferredSize().height));
    tempPanel.add(tempLabel);
    tempPanel.add(field);
    detailsPanel.add(tempPanel);
  }

}
