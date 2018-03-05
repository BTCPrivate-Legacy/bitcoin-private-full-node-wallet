package org.btcprivate.wallets.fullnode.util;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Log
{
    private static PrintStream fileOut;

    private static Set<String> oneTimeMessages = new HashSet<String>();

    static
    {
        try
        {
            // Initialize log to a file
            String settingsDir = OSUtil.getSettingsDirectory();
            Date today = new Date();
            String logFile = settingsDir + File.separator +
                    "BitcoinPrivateGUIWallet_" +
                    (int)(today.getYear() + 1900) + "_" +
                    (int)(today.getMonth() + 1) + "_" +
                    "debug.log";
            fileOut = new PrintStream(new FileOutputStream(logFile, true));
        }
        catch (IOException ioe)
        {
            fileOut = null;
            System.out.println("Error in initializing file logging!!!");
            ioe.printStackTrace();
        }
    }

    public static void debug(String message, Object ... args)
    {
        printMessage("DEBUG", message, null, args);
    }


    public static void trace(String message, Object ... args)
    {
        printMessage("TRACE", message, null, args);
    }


    public static void info(String message, Object ... args)
    {
        printMessage("INFO", message, null, args);
    }


    public static void warning(String message, Object ... args)
    {
        warning(message, null, args);
    }


    public static void warning(String message, Throwable t, Object ... args)
    {
        printMessage("WARNING", message, t, args);
    }


    public static void warningOneTime(String message, Object ... args)
    {
        printMessage(true, "WARNING", message, null, args);
    }


    public static void error(String message, Object ... args)
    {
        error(message, null, args);
    }


    public static void error(String message, Throwable t, Object ... args)
    {
        printMessage("ERROR", message, t, args);
    }


    private static void printMessage(String messageClass, String message,
                                     Throwable t, Object ... args)
    {
        printMessage(false, messageClass, message, t, args);
    }


    private static void printMessage(boolean oneTimeOnly, String messageClass, String message,
                                     Throwable t, Object ... args)
    {
        // TODO: Too much garbage collection
        for (int i = 0; i < args.length; i++)
        {
            if (args[i] != null)
            {
                message = message.replace("{" + i  + "}", args[i].toString());
            }
        }
        message += " ";

        if (oneTimeOnly) // One time messages logged only once!
        {
            if (oneTimeMessages.contains(message))
            {
                return;
            } else
            {
                oneTimeMessages.add(message);
            }
        }

        String prefix =
                "[" + Thread.currentThread().getName() + "] " +
                        "[" + (new Date()).toString() + "] ";

        messageClass = "[" + messageClass + "] ";

        String throwable = "";
        if (t != null)
        {
            CharArrayWriter car = new CharArrayWriter(500);
            PrintWriter pr = new PrintWriter(car);
            pr.println();  // One line extra before the exception.
            t.printStackTrace(pr);
            pr.close();
            throwable = new String(car.toCharArray());
        }

        System.out.println(prefix + messageClass + message + throwable);

        if (fileOut != null)
        {
            fileOut.println(prefix + messageClass + message + throwable);
            fileOut.flush();
        }
    }
}
