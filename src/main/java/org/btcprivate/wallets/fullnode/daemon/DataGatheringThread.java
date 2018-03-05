package org.btcprivate.wallets.fullnode.daemon;


import org.btcprivate.wallets.fullnode.util.Log;
import org.btcprivate.wallets.fullnode.util.StatusUpdateErrorReporter;

/**
 * This thread may be used to periodically and asynchronously load data if the load operation
 * takes considerable time. The creator of the thread may obtain the latest gathered data
 * quickly since it is stored in the thread.
 *
 * @param <T> the type of data that is gathered.
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class DataGatheringThread<T>
        extends Thread
{
    /**
     * All implementations must provide an impl. of this interface to
     * gather the actual data.
     *
     * @param <T> the type of data that is gathered.
     */
    public static interface DataGatherer<T>
    {
        T gatherData()
                throws Exception;
    }


    // Last gathered data - stored
    private T lastGatheredData;
    // Gatherer used for the data
    private DataGatherer<T> gatherer;
    // Interval in ms for gathering
    private int interval;
    // Fag to run immediately - no wait
    boolean doAFirstGatehring;
    // Error reporter
    private StatusUpdateErrorReporter errorReporter;
    // Flag allowing the thread to be suspended
    private boolean suspended;

    /**
     * Creates a new thread for data gathering.
     *
     * @param gatherer Gatherer used for the data
     * @param errorReporter Error reporter - may be null
     * @param interval Interval in ms for gathering
     */
    public DataGatheringThread(DataGatherer<T> gatherer, StatusUpdateErrorReporter errorReporter, int interval)
    {
        this(gatherer, errorReporter, interval, false);
    }

    /**
     * Creates a new thread for data gathering.
     *
     * @param gatherer Gatherer used for the data
     * @param errorReporter Error reporter - may be null
     * @param interval Interval in ms for gathering
     */
    public DataGatheringThread(DataGatherer<T> gatherer, StatusUpdateErrorReporter errorReporter,
                               int interval, boolean doAFirstGatehring)
    {
        this.suspended = false;
        this.gatherer = gatherer;
        this.errorReporter = errorReporter;
        this.interval = interval;
        this.doAFirstGatehring = doAFirstGatehring;

        this.lastGatheredData = null;

        // Start the thread to gather
        this.start();
    }


    /**
     * Sets the suspension flag.
     *
     * @param suspended suspension flag.
     */
    public synchronized void setSuspended(boolean suspended)
    {
        this.suspended = suspended;
    }


    /**
     * Returns the current state of the suspension flag.
     *
     * @return the current state of the suspension flag.
     */
    public synchronized boolean isSuspended()
    {
        return this.suspended;
    }


    /**
     * Obtains the last gathered data
     *
     * @return the last gathered data
     */
    public synchronized T getLastData()
    {
        return lastGatheredData;
    }


    /**
     * Runs periodically and gathers the data at intervals;
     */
    @Override
    public void run()
    {
        if (this.doAFirstGatehring && (!this.suspended))
        {
            this.doOneGathering();
        }

        mainLoop:
        while (true)
        {
            synchronized (this)
            {
                long startWait = System.currentTimeMillis();
                long endWait = startWait;
                do
                {
                    try
                    {
                        this.wait(300);
                    } catch (InterruptedException ie)
                    {
                        // One of the rare cases where we do nothing
                        Log.error("Unexpected error: ", ie);
                    }

                    endWait = System.currentTimeMillis();
                } while ((endWait - startWait) <= this.interval);
            }

            if (!this.suspended)
            {
                this.doOneGathering();
            } else
            {
                break mainLoop;
            }
        }

        Log.info("Ending data gathering thread {0} ...", this.getName());
    } // End public void run()


    // Obtains the data in a single run
    private void doOneGathering()
    {
        // The gathering itself is not synchronized
        T localData = null;

        try
        {
            localData = this.gatherer.gatherData();
        } catch (Exception e)
        {
            if (!this.suspended)
            {
                Log.error("Unexpected error: ", e);
                if (this.errorReporter != null)
                {
                    this.errorReporter.reportError(e);
                }
            } else
            {
                Log.info("DataGatheringThread: ignoring " + e.getClass().getName() + " due to suspension!");
            }
        }

        synchronized (this)
        {
            this.lastGatheredData = localData;
        }
    }
}
