package org.btcprivate.wallets.fullnode.messaging;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.btcprivate.wallets.fullnode.ui.WalletTextArea;
import org.btcprivate.wallets.fullnode.ui.WalletTextField;

public class IdentityInfoDialog
        extends JDialog
{
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


    public IdentityInfoDialog(JFrame parentFrame, MessagingIdentity identity)
    {
        this.parentFrame = parentFrame;
        this.identity    = identity;

        this.setTitle("Contact Details - " + identity.getDiplayString());
        this.setModal(true);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        this.getContentPane().setLayout(new BorderLayout(0, 0));

        JPanel tempPanel = new JPanel(new BorderLayout(0, 0));
        tempPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        infoLabel = new JLabel(
                "<html><span style=\"font-size:0.97em;\">" +
                        "The information shown below pertains to contact " + identity.getNickname() +
                        "</span>");
        tempPanel.add(infoLabel, BorderLayout.CENTER);
        this.getContentPane().add(tempPanel, BorderLayout.NORTH);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));

        addFormField(detailsPanel, "Nickname:",  nicknameTextField = new WalletTextField(40));
        addFormField(detailsPanel, "First name:", firstnameTextField = new WalletTextField(40));
        addFormField(detailsPanel, "Middle name:", middlenameTextField = new WalletTextField(40));
        addFormField(detailsPanel, "Surname:",    surnameTextField = new WalletTextField(40));

        addFormField(detailsPanel, "Email:",         emailTextField = new WalletTextField(40));
        addFormField(detailsPanel, "Street address:", streetaddressTextField = new WalletTextField(40));
        addFormField(detailsPanel, "Facebook URL:",  facebookTextField = new WalletTextField(40));
        addFormField(detailsPanel, "Twitter URL:",   twitterTextField = new WalletTextField(40));

        addFormField(detailsPanel, "Sender identification B address:", senderidaddressTextField = new WalletTextField(40));
        addFormField(detailsPanel, "Send/receive Z address:", sendreceiveaddressTextArea = new WalletTextArea(2, 40));
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
        JButton closeButon = new JButton("Close");
        buttonPanel.add(closeButon);
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        closeButon.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                IdentityInfoDialog.this.setVisible(false);
                IdentityInfoDialog.this.dispose();
            }
        });

        this.pack();
        this.setLocation(100, 100);
        this.setLocationRelativeTo(parentFrame);
    }



    private void addFormField(JPanel detailsPanel, String name, JComponent field)
    {
        JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        JLabel tempLabel = new JLabel(name, JLabel.RIGHT);
        // TODO: hard sizing of labels may not scale!
        final int width = new JLabel("Sender identification B address:").getPreferredSize().width + 10;
        tempLabel.setPreferredSize(new Dimension(width, tempLabel.getPreferredSize().height));
        tempPanel.add(tempLabel);
        tempPanel.add(field);
        detailsPanel.add(tempPanel);
    }

}
