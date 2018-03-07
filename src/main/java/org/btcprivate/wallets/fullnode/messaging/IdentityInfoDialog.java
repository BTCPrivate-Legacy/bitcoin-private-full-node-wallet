package org.btcprivate.wallets.fullnode.messaging;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class IdentityInfoDialog
        extends JDialog
{
    protected JFrame parentFrame;
    protected MessagingIdentity identity;

    protected JLabel infoLabel;

    protected JPanel buttonPanel;

    protected JTextField nicknameTextField;
    protected JTextArea sendreceiveaddressTextField;
    protected JTextField senderidaddressTextField;
    protected JTextField firstnameTextField;
    protected JTextField middlenameTextField;
    protected JTextField surnameTextField;
    protected JTextField emailTextField;
    protected JTextField streetaddressTextField;
    protected JTextField facebookTextField;
    protected JTextField twitterTextField;


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

        addFormField(detailsPanel, "Nickname:",  nicknameTextField = new JTextField(40));
        addFormField(detailsPanel, "First name:", firstnameTextField = new JTextField(40));
        addFormField(detailsPanel, "Middle name:", middlenameTextField = new JTextField(40));
        addFormField(detailsPanel, "Surname:",    surnameTextField = new JTextField(40));

        addFormField(detailsPanel, "Email:",         emailTextField = new JTextField(40));
        addFormField(detailsPanel, "Street address:", streetaddressTextField = new JTextField(40));
        addFormField(detailsPanel, "Facebook URL:",  facebookTextField = new JTextField(40));
        addFormField(detailsPanel, "Twitter URL:",   twitterTextField = new JTextField(40));

        addFormField(detailsPanel, "Sender identification T address:", senderidaddressTextField = new JTextField(40));
        addFormField(detailsPanel, "Send/receive Z address:", sendreceiveaddressTextField = new JTextArea(2, 40));
        sendreceiveaddressTextField.setLineWrap(true);


        nicknameTextField.setText(this.identity.getNickname());
        firstnameTextField.setText(this.identity.getFirstname());
        middlenameTextField.setText(this.identity.getMiddlename());
        surnameTextField.setText(this.identity.getSurname());
        emailTextField.setText(this.identity.getEmail());
        streetaddressTextField.setText(this.identity.getStreetaddress());
        facebookTextField.setText(this.identity.getFacebook());
        twitterTextField.setText(this.identity.getTwitter());
        senderidaddressTextField.setText(this.identity.getSenderidaddress());
        sendreceiveaddressTextField.setText(this.identity.getSendreceiveaddress());

        nicknameTextField.setEditable(false);
        firstnameTextField.setEditable(false);
        middlenameTextField.setEditable(false);
        surnameTextField.setEditable(false);
        emailTextField.setEditable(false);
        streetaddressTextField.setEditable(false);
        facebookTextField.setEditable(false);
        twitterTextField.setEditable(false);
        senderidaddressTextField.setEditable(false);
        sendreceiveaddressTextField.setEditable(false);

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
        final int width = new JLabel("Sender identification T address:").getPreferredSize().width + 10;
        tempLabel.setPreferredSize(new Dimension(width, tempLabel.getPreferredSize().height));
        tempPanel.add(tempLabel);
        tempPanel.add(field);
        detailsPanel.add(tempPanel);
    }

}
