package org.btcprivate.wallets.fullnode.daemon;

import org.btcprivate.wallets.fullnode.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;


/**
 * Executes a command and retruns sthe result.
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class CommandExecutor {
    private String args[];

    public CommandExecutor(String args[])
            throws IOException {
        this.args = args;
    }


    public Process startChildProcess()
            throws IOException {
        return Runtime.getRuntime().exec(args);
    }


    public String execute()
            throws IOException, InterruptedException {
        final StringBuffer result = new StringBuffer();

        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(args);

        final Reader in = new InputStreamReader(new BufferedInputStream(proc.getInputStream()));

        final Reader err = new InputStreamReader(proc.getErrorStream());

        Thread inThread = new Thread(
                () -> {
                    try {
                        int c;
                        while ((c = in.read()) != -1) {
                            result.append((char) c);
                        }
                    } catch (IOException ioe) {
                        Log.error("Error while executing command on daemon. Command attempted: " + args + ". Error: " + ioe.getMessage());
                    }
                }
        );
        inThread.start();

        Thread errThread = new Thread(
                () -> {
                    try {
                        int c;
                        while ((c = err.read()) != -1) {
                            result.append((char) c);
                        }
                    } catch (IOException ioe) {
                        Log.error("Error while executing command on daemon. Command attempted: " + args + ". Error: " + ioe.getMessage());
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

