package org.btcprivate.wallets.fullnode.ui;

import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.btcprivate.wallets.fullnode.ui.WalletPasswordField;
import org.btcprivate.wallets.fullnode.util.Util;


/**
 * Dialog to get the user password - to encrypt a wallet.
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class PasswordEncryptionDialog
        extends PasswordDialog
{
    protected WalletPasswordField passwordConfirmationField = null;

    public PasswordEncryptionDialog(JFrame parent)
    {
        super(parent);

        this.upperLabel.setText(
                "<html>The wallet.dat file will be encrypted with a password. If the operation is successful, " +
                        "btcpd will automatically stop and will need to be restarted. The GUI wallet will also be stopped " +
                        "and will need to be restarted. Please enter the password:</html>");

        JLabel confLabel = new JLabel("Confirmation: ");
        this.freeSlotPanel.add(confLabel);
        this.freeSlotPanel.add(passwordConfirmationField = new WalletPasswordField(30));
        this.passwordLabel.setPreferredSize(confLabel.getPreferredSize());

        JLabel dividerLabel = new JLabel("   ");
        dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 8));
        this.freeSlotPanel2.add(dividerLabel);

        this.setSize(460, 270);
        this.validate();
        this.repaint();
    }


    protected void processOK()
    {
        String password     = this.passwordField.getText();
        String confirmation = this.passwordConfirmationField.getText();

        if (password == null)
        {
            password = "";
        }

        if (confirmation == null)
        {
            confirmation = "";
        }

        if (!password.equals(confirmation))
        {
            JOptionPane.showMessageDialog(
                    this.getParent(),
                    "The password and the confirmation do not match!", "Password Mismatch",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        super.processOK();
    }

}