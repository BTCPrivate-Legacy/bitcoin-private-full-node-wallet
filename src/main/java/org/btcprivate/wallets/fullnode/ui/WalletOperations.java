package org.btcprivate.wallets.fullnode.ui;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;


import org.btcprivate.wallets.fullnode.daemon.BTCPClientCaller;
import org.btcprivate.wallets.fullnode.daemon.BTCPClientCaller.*;
import org.btcprivate.wallets.fullnode.daemon.BTCPInstallationObserver;
import org.btcprivate.wallets.fullnode.util.BackupTracker;
import org.btcprivate.wallets.fullnode.util.Log;
import org.btcprivate.wallets.fullnode.util.StatusUpdateErrorReporter;
import org.btcprivate.wallets.fullnode.util.Util;


/**
 * Provides miscellaneous operations for the wallet file.
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class WalletOperations
{
    private BTCPWalletUI parent;
    private JTabbedPane tabs;
    private DashboardPanel dashboard;
    private SendCashPanel  sendCash;
    private AddressesPanel addresses;

    private BTCPInstallationObserver installationObserver;
    private BTCPClientCaller clientCaller;
    private StatusUpdateErrorReporter errorReporter;
    private BackupTracker backupTracker;


    public WalletOperations(BTCPWalletUI parent,
                            JTabbedPane tabs,
                            DashboardPanel dashboard,
                            AddressesPanel addresses,
                            SendCashPanel  sendCash,

                            BTCPInstallationObserver installationObserver,
                            BTCPClientCaller clientCaller,
                            StatusUpdateErrorReporter errorReporter,
                            BackupTracker             backupTracker)
            throws IOException, InterruptedException, WalletCallException
    {
        this.parent    = parent;
        this.tabs      = tabs;
        this.dashboard = dashboard;
        this.addresses = addresses;
        this.sendCash  = sendCash;

        this.installationObserver = installationObserver;
        this.clientCaller = clientCaller;
        this.errorReporter = errorReporter;

        this.backupTracker = backupTracker;
    }


    public void encryptWallet()
    {
        try
        {
            if (this.clientCaller.isWalletEncrypted())
            {
                JOptionPane.showMessageDialog(
                        this.parent,
                        "The wallet.dat file being used is already encrypted. " +
                                "This \noperation may be performed only on a wallet that " +
                                "is not\nyet encrypted!",
                        "Wallet Is Already Encrypted",
                        JOptionPane.ERROR_MESSAGE);
                return;

            }

            PasswordEncryptionDialog pd = new PasswordEncryptionDialog(this.parent);
            pd.setVisible(true);

            if (!pd.isOKPressed())
            {
                return;
            }

            Cursor oldCursor = this.parent.getCursor();
            try
            {

                this.parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                this.dashboard.stopThreadsAndTimers();
                this.sendCash.stopThreadsAndTimers();

                this.clientCaller.encryptWallet(pd.getPassword());

                this.parent.setCursor(oldCursor);
            } catch (WalletCallException wce)
            {
                this.parent.setCursor(oldCursor);
                Log.error("Unexpected error: ", wce);

                JOptionPane.showMessageDialog(
                        this.parent,
                        "An unexpected error occurred while encrypting the wallet!\n" +
                                "It is recommended to stop and restart both btcpd and the GUI wallet! \n" +
                                "\n" + wce.getMessage().replace(",", ",\n"),
                        "Error Encrypting Wallet", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(
                    this.parent,
                    "The wallet has been encrypted sucessfully and btcpd has stopped.\n" +
                            "The GUI wallet will be stopped as well. Please restart the program.\n" +
                            "Additionally, the internal wallet keypool has been flushed. You need\n" +
                            "to make a new backup." +
                            "\n",
                    "Wallet Is Now Encrypted", JOptionPane.INFORMATION_MESSAGE);

            this.parent.exitProgram();

        } catch (Exception e)
        {
            this.errorReporter.reportError(e, false);
        }
    }


    public void backupWallet()
    {
        try
        {
            Cursor oldCursor = this.parent.getCursor();

            String path = null;
            try
            {
                this.parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                path = this.clientCaller.backupWallet("");
                this.backupTracker.handleBackup();
                this.parent.setCursor(oldCursor);

            } catch (WalletCallException wce)
            {
                this.parent.setCursor(oldCursor);

                Log.error("Unexpected error: ", wce);

                JOptionPane.showMessageDialog(

                        this.parent,
                        "An unexpected error occurred while backing up the wallet!" +
                                "\n" + wce.getMessage().replace(",", ",\n"),
                        "Error Backing Up Wallet", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(
                    this.parent,
                    "The wallet has been backed up successfully.\nFull path is: " +
                            path,
                    "Successfully Backed Up Wallet", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e)
        {
            this.errorReporter.reportError(e, false);
        }
    }


    public void exportWalletPrivateKeys()
    {
        // TODO: Will need corrections once encryption is reenabled!!!

        try
        {
            Cursor oldCursor = this.parent.getCursor();
            String path=null;
            try
            {
                this.parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                path = this.clientCaller.exportWallet("btcpprivatekeys");
                this.backupTracker.handleBackup();
                this.parent.setCursor(oldCursor);

            } catch (WalletCallException wce)
            {
                this.parent.setCursor(oldCursor);

                Log.error("Unexpected error: ", wce);

                JOptionPane.showMessageDialog(
                        this.parent,
                        "An unexpected error occurred while exporting wallet private keys!" +
                                "\n" + wce.getMessage().replace(",", ",\n"),
                        "Error Exporting Private Keys", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(
                    this.parent,
                    "The wallet private keys have been exported successfully. Full path is: " +
                            path + "\n" +
                            "You need to protect this file from unauthorized access. Anyone who\n" +
                            "has access to the private keys can spend the Bitcoin Private balance!",
                    "Successfully Exported Private Keys", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e)
        {
            this.errorReporter.reportError(e, false);
        }
    }


    public void importWalletPrivateKeys()
    {
        // TODO: Will need corrections once encryption is re-enabled!!!

        int option = JOptionPane.showConfirmDialog(
                this.parent,
                "Importing private keys can be a slow operation. It may take\n" +
                        "several minutes, during which the GUI will be non-responsive.\n" +
                        "The data to import must be in the format used by \n" +
                        "\"Wallet >> Export Private Keys\"\n\n" +
                        "Continue?",
                "Private key import",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.NO_OPTION)
        {
            return;
        }

        try
        {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Import Private Keys from File");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            int result = fileChooser.showOpenDialog(this.parent);

            if (result != JFileChooser.APPROVE_OPTION)
            {
                return;
            }

            File f = fileChooser.getSelectedFile();

            Cursor oldCursor = this.parent.getCursor();
            try
            {
                this.parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                this.clientCaller.importWallet(f.getCanonicalPath());

                this.parent.setCursor(oldCursor);
            } catch (WalletCallException wce)
            {
                this.parent.setCursor(oldCursor);
                Log.error("Unexpected error: ", wce);

                JOptionPane.showMessageDialog(
                        this.parent,
                        "An unexpected error occurred while importing private keys!" +
                                "\n" + wce.getMessage().replace(",", ",\n"),
                        "Error Importing Private Keys", JOptionPane.ERROR_MESSAGE);

                return;
            }

            JOptionPane.showMessageDialog(
                    this.parent,
                    "Wallet private keys have been successfully imported from location:\n" +
                            f.getCanonicalPath() + "\n\n",
                    "Successfully Imported Private Keys", JOptionPane.INFORMATION_MESSAGE);


        } catch (Exception e)
        {
            this.errorReporter.reportError(e, false);
        }
    }


    public void showPrivateKey()
    {
        if (this.tabs.getSelectedIndex() != 1)
        {
            JOptionPane.showMessageDialog(
                    this.parent,
                    "Please select an address in the \"My Addresses\" tab " +
                            "to view its private key.",
                    "Select an Address", JOptionPane.INFORMATION_MESSAGE);
            this.tabs.setSelectedIndex(1);
            return;
        }

        String address = this.addresses.getSelectedAddress();

        if (address == null)
        {
            JOptionPane.showMessageDialog(
                    this.parent,
                    "Please select an address from the table " +
                            "to view its private key.",
                    "Select an Address", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try
        {
            // Check for encrypted wallet
            final boolean bEncryptedWallet = this.clientCaller.isWalletEncrypted();
            if (bEncryptedWallet)
            {
                PasswordDialog pd = new PasswordDialog((JFrame)(this.parent));
                pd.setVisible(true);

                if (!pd.isOKPressed())
                {
                    return;
                }

                this.clientCaller.unlockWallet(pd.getPassword());
            }

            boolean isZAddress = Util.isZAddress(address);

            String privateKey = isZAddress ?
                    this.clientCaller.getZPrivateKey(address) : this.clientCaller.getTPrivateKey(address);

            // Lock the wallet again
            if (bEncryptedWallet)
            {
                this.clientCaller.lockWallet();
            }

            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(privateKey), null);

            JOptionPane.showMessageDialog(
                    this.parent,
                    (isZAddress ? "Z (Private)" : "T (Transparent)") +  " address:\n" +
                            address + "\n" +
                            "has private key:\n" +
                            privateKey + "\n\n" +
                            "The private key has also been copied to the clipboard.",
                    "Private Key Info", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex)
        {
            this.errorReporter.reportError(ex, false);
        }
    }


    public void importSinglePrivateKey()
    {
        try
        {
            SingleKeyImportDialog kd = new SingleKeyImportDialog(this.parent, this.clientCaller,this.sendCash,this.tabs);
            kd.setVisible(true);

        } catch (Exception ex)
        {
            this.errorReporter.reportError(ex, false);
        }
    }
}