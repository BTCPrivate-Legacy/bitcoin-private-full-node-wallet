package org.btcprivate.wallets.fullnode.ui;

import org.btcprivate.wallets.fullnode.daemon.BTCPClientCaller.WalletCallException;
import org.btcprivate.wallets.fullnode.daemon.DataGatheringThread;
import org.btcprivate.wallets.fullnode.util.Log;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Base for all panels contained as wallet TABS.
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class WalletTabPanel
        extends JPanel
{
    // Lists of threads and timers that may be stopped if necessary
    protected List<Timer> timers                   = null;
    protected List<DataGatheringThread<?>> threads = null;

    public WalletTabPanel()
            throws IOException, InterruptedException, WalletCallException
    {
        super();

        this.timers = new ArrayList<>();
        this.threads = new ArrayList<>();
    }


    public void stopThreadsAndTimers()
    {
        for (Timer t : this.timers)
        {
            t.stop();
        }

        for (DataGatheringThread<?> t : this.threads)
        {
            t.setSuspended(true);
        }
    }


    // Interval is in milliseconds
    // Returns true if all threads have ended, else false
    public boolean waitForEndOfThreads(long interval)
    {
        synchronized (this)
        {
            long startWait = System.currentTimeMillis();
            long endWait = startWait;
            do
            {
                boolean allEnded = true;
                for (DataGatheringThread<?> t : this.threads)
                {
                    if (t.isAlive())
                    {
                        allEnded = false;
                    }
                }

                if (allEnded)
                {
                    return true; // End here
                }

                try
                {
                    this.wait(100);
                } catch (InterruptedException ie)
                {
                    // One of the rare cases where we do nothing
                    Log.error("Unexpected error: ", ie);
                }

                endWait = System.currentTimeMillis();
            } while ((endWait - startWait) <= interval);
        }

        return false;
    }

} // End class