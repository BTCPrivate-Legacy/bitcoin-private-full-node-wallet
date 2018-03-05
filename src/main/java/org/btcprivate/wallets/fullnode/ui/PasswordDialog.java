package org.btcprivate.wallets.fullnode.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Dialog to get the user password for encrypted wallets - for unlock.
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class PasswordDialog
        extends JDialog
{
    protected boolean isOKPressed = false;
    protected String  password    = null;

    protected JLabel     passwordLabel = null;
    protected JTextField passwordField = null;

    protected JLabel upperLabel;
    protected JLabel lowerLabel;

    protected JPanel freeSlotPanel;
    protected JPanel freeSlotPanel2;

    public PasswordDialog(JFrame parent)
    {
        super(parent);

        this.setTitle("Password");
        this.setLocation(parent.getLocation().x + 50, parent.getLocation().y + 50);
        this.setModal(true);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
        controlsPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel tempPanel = new JPanel(new BorderLayout(0, 0));
        tempPanel.add(this.upperLabel = new JLabel("<html>The wallet is encrypted and protected with a password. " +
                "Please enter the password to unlock it temporarily for operation.</html>"), BorderLayout.CENTER);
        controlsPanel.add(tempPanel);

        JLabel dividerLabel = new JLabel("   ");
        dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 8));
        controlsPanel.add(dividerLabel);

        tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tempPanel.add(passwordLabel = new JLabel("Password: "));
        tempPanel.add(passwordField = new JPasswordField(30));
        controlsPanel.add(tempPanel);

        dividerLabel = new JLabel("   ");
        dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 8));
        controlsPanel.add(dividerLabel);

        this.freeSlotPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        controlsPanel.add(this.freeSlotPanel);

        this.freeSlotPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        controlsPanel.add(this.freeSlotPanel2);

        tempPanel = new JPanel(new BorderLayout(0, 0));
        tempPanel.add(this.lowerLabel = new JLabel("<html><span style=\"font-weight:bold\">" +
                "WARNING: Never enter your password on a public/shared " +
                "computer, or one that you suspect has been infected with malware! " +
                "</span></html>"), BorderLayout.CENTER);
        controlsPanel.add(tempPanel);

        this.getContentPane().setLayout(new BorderLayout(0, 0));
        this.getContentPane().add(controlsPanel, BorderLayout.NORTH);

        // Form buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 3));
        JButton okButton = new JButton("OK");
        buttonPanel.add(okButton);
        buttonPanel.add(new JLabel("   "));
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(cancelButton);
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        okButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                PasswordDialog.this.processOK();
            }
        });

        cancelButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                PasswordDialog.this.setVisible(false);
                PasswordDialog.this.dispose();

                PasswordDialog.this.isOKPressed = false;
                PasswordDialog.this.password = null;
            }
        });

        this.setSize(450, 190);
        this.validate();
        this.repaint();
    }


    protected void processOK()
    {
        String pass = PasswordDialog.this.passwordField.getText();

        if ((pass == null) || (pass.trim().length() <= 0))
        {
            JOptionPane.showMessageDialog(
                    PasswordDialog.this.getParent(),

                    "Please enter the password.", "Password Required",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        PasswordDialog.this.setVisible(false);
        PasswordDialog.this.dispose();

        PasswordDialog.this.isOKPressed = true;
        PasswordDialog.this.password = pass;
    }


    public boolean isOKPressed()
    {
        return this.isOKPressed;
    }


    public String getPassword()
    {
        return this.password;
    }
}
