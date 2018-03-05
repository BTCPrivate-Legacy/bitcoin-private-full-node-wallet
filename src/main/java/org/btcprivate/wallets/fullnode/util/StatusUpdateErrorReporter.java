package org.btcprivate.wallets.fullnode.util;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Reporter for periodic errors. Will later have options to filter errors etc.
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class StatusUpdateErrorReporter
{
    private JFrame parent;
    private long lastReportedErrorTime = 0;

    public StatusUpdateErrorReporter(JFrame parent)
    {
        this.parent = parent;
    }

    public void reportError(Exception e)
    {
        reportError(e, true);
    }

    public void reportError(Exception e, boolean isDueToAutomaticUpdate)
    {
        Log.error("Unexpected error: ", e);

        // TODO: Error logging
        long time = System.currentTimeMillis();

        // TODO: More complex filtering/tracking in the future
        if (isDueToAutomaticUpdate && (time - lastReportedErrorTime) < (45 * 1000))
        {
            return;
        }

        if (isDueToAutomaticUpdate)
        {
            lastReportedErrorTime = time;
        }

        String settingsDirectory = ".BitcoinPrivateDesktopWallet";

        try
        {
            settingsDirectory = OSUtil.getSettingsDirectory();
        } catch (Exception e2)
        {
            Log.error("Secondary error: ", e2);
        }

        JOptionPane.showMessageDialog(
                parent,
                "An unexpected error occurred during the operation of the GUI wallet.\n" +
                        "Details may be found in the log in directory: " + settingsDirectory + "\n" +
                        "\n" +
                        e.getMessage(),
                "Error in wallet operation.", JOptionPane.ERROR_MESSAGE);
    }
}