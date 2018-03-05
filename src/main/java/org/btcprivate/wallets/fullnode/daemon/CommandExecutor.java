package org.btcprivate.wallets.fullnode.daemon;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;


/**
 * Executes a command and retruns the result.
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class CommandExecutor
{
    private String args[];

    public CommandExecutor(String args[])
            throws IOException
    {
        this.args = args;
    }


    public Process startChildProcess()
            throws IOException
    {
        return Runtime.getRuntime().exec(args);
    }


    public String execute()
            throws IOException, InterruptedException
    {
        final StringBuffer result = new StringBuffer();

        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(args);

        final Reader in = new InputStreamReader(new BufferedInputStream(proc.getInputStream()));

        final Reader err = new InputStreamReader(proc.getErrorStream());

        Thread inThread = new Thread(
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            int c;
                            while ((c = in.read()) != -1)
                            {
                                result.append((char)c);
                            }
                        } catch (IOException ioe)
                        {
                            // TODO: log or handle the exception
                        }
                    }
                }
        );
        inThread.start();

        Thread errThread =  new Thread(
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            int c;
                            while ((c = err.read()) != -1)
                            {
                                result.append((char)c);
                            }
                        } catch (IOException ioe)
                        {
                            // TODO: log or handle the exception
                        }
                    }
                }
        );
        errThread.start();

        proc.waitFor();
        inThread.join();
        errThread.join();

        return result.toString();
    }
}

