package org.btcprivate.wallets.fullnode.util;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;


/**
 * Tracks important user actions and reminds the user to back up the wallet depending on
 * the content of the current user activity.
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class BackupTracker
{
    private static final String TRANSACTIONS_COUNTER_FILE      = "transactionsCountSinceBackup.txt";
    private static final int    NUM_TRANSACTIONS_WIHOUT_BACKUP = 50;

    private JFrame parentFrame;


    public BackupTracker(JFrame parentFrame)
    {
        this.parentFrame = parentFrame;
    }


    /**
     * Called when the wallet balance is updated.
     */
    public synchronized void handleWalletBalanceUpdate(double balance)
            throws IOException
    {
        if ((balance > 0) && (!transactionsCounterFileExists()))
        {
            this.promptToDoABackup();
        }

        if (!transactionsCounterFileExists())
        {
            this.writeNumTransactionsSinceLastBackup(0);
        }
    }


    /**
     * Called upon sending funds.
     */
    public synchronized void handleNewTransaction()
            throws IOException
    {
        if (!transactionsCounterFileExists())
        {
            this.writeNumTransactionsSinceLastBackup(0);
        } else
        {
            int numTransactionsSinceLastBackup = this.getNumTransactionsSinceLastBackup();
            numTransactionsSinceLastBackup++;
            if (numTransactionsSinceLastBackup > NUM_TRANSACTIONS_WIHOUT_BACKUP)
            {
                this.promptToDoABackup();
            }
            this.writeNumTransactionsSinceLastBackup(numTransactionsSinceLastBackup);
        }
    }


    /**
     * Called when a new backup is made
     */
    public synchronized void handleBackup()
            throws IOException
    {
        this.writeNumTransactionsSinceLastBackup(0);
    }


    private void promptToDoABackup()
    {
        JOptionPane.showMessageDialog(
                this.parentFrame,
                "It appears that you have not backed up your wallet recently. It is recommended to\n" +
                        "back up the wallet after every 50 outgoing transactions, and after creating a new\n" +
                        "Z address. The wallet needs to be backed up to another safe location that can survive any\n" +
                        "data loss on the PC where the wallet is currenly located. Not backing up the wallet\n" +
                        "may result in loss of funds in case of data loss on the current PC. To back up the\n" +
                        "wallet, use menu option: Wallet >> Backup\n\n",
                "Back Up Your Wallet!", JOptionPane.INFORMATION_MESSAGE);
    }


    private boolean transactionsCounterFileExists()
            throws IOException
    {
        String dir = OSUtil.getSettingsDirectory();
        File counter = new File(dir + File.separator + TRANSACTIONS_COUNTER_FILE);
        return counter.exists();
    }


    private void writeNumTransactionsSinceLastBackup(int numTransactions)
            throws IOException
    {
        String dir = OSUtil.getSettingsDirectory();
        File counter = new File(dir + File.separator + TRANSACTIONS_COUNTER_FILE);

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(counter);
            fos.write(String.valueOf(numTransactions).getBytes("ISO-8859-1"));
        } finally
        {
            if (fos != null)
            {
                fos.close();
            }
        }

    }


    public int getNumTransactionsSinceLastBackup()
            throws IOException
    {
        int countNum = 0;
        String dir = OSUtil.getSettingsDirectory();
        File counter = new File(dir + File.separator + TRANSACTIONS_COUNTER_FILE);

        if (counter.exists())
        {
            byte[] bytes = Util.loadFileInMemory(counter);
            String countAsString = new String(bytes, "ISO-8859-1");

            try
            {
                countNum = Integer.parseInt(countAsString.trim());
            } catch (NumberFormatException nfe)
            {
                // No error but only a logged message
                Log.error("Transaction counter file {0} contains invalid numeric data: {1}",
                        TRANSACTIONS_COUNTER_FILE, countAsString);
            }
        }

        return countNum;
    }
}
