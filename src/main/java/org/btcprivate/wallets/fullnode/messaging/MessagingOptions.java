package org.btcprivate.wallets.fullnode.messaging;


import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.WriterConfig;
import org.btcprivate.wallets.fullnode.util.Util;

import java.io.*;


/**
 * Encapsulates the messaging options that may be set.
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class MessagingOptions
{
    private boolean automaticallyAddUsersIfNotExplicitlyImported;
    private double  amountToSend;
    private double  transactionFee;


    public MessagingOptions()
    {
        // Default values set if not loade etc.
        this.automaticallyAddUsersIfNotExplicitlyImported = true;
        this.amountToSend = this.transactionFee = 0.0001d;
    }


    public MessagingOptions(JsonObject obj)
            throws IOException
    {
        this.copyFromJSONObject(obj);
    }


    public MessagingOptions(File f)
            throws IOException
    {
        Reader r = null;

        try
        {
            r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
            JsonObject obj = Util.parseJsonObject(r);

            this.copyFromJSONObject(obj);
        } finally
        {
            if (r != null)
            {
                r.close();
            }
        }
    }


    public void copyFromJSONObject(JsonObject obj)
            throws IOException
    {
        // Mandatory fields!
        this.automaticallyAddUsersIfNotExplicitlyImported =
                obj.getBoolean("automaticallyaddusersifnotexplicitlyimported", true);
        this.amountToSend   = obj.getDouble("amounttosend",   0.0001d);
        this.transactionFee = obj.getDouble("transactionfee", 0.0001d);
    }


    public JsonObject toJSONObject()
    {
        JsonObject obj = new JsonObject();

        obj.set("automaticallyaddusersifnotexplicitlyimported",
                this.automaticallyAddUsersIfNotExplicitlyImported);
        obj.set("amounttosend",	this.amountToSend);
        obj.set("transactionfee",	this.transactionFee);

        return obj;
    }


    public void writeToFile(File f)
            throws IOException
    {
        Writer w = null;

        try
        {
            w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
            w.write(this.toJSONObject().toString(WriterConfig.PRETTY_PRINT));
        } finally
        {
            if (w != null)
            {
                w.close();
            }
        }
    }


    public boolean isAutomaticallyAddUsersIfNotExplicitlyImported()
    {
        return automaticallyAddUsersIfNotExplicitlyImported;
    }


    public void setAutomaticallyAddUsersIfNotExplicitlyImported(boolean automaticallyAddUsersIfNotExplicitlyImported)
    {
        this.automaticallyAddUsersIfNotExplicitlyImported = automaticallyAddUsersIfNotExplicitlyImported;
    }


    public double getAmountToSend()
    {
        return amountToSend;
    }


    public void setAmountToSend(double amountToSend)
    {
        this.amountToSend = amountToSend;
    }


    public double getTransactionFee()
    {
        return transactionFee;
    }


    public void setTransactionFee(double transactionFee)
    {
        this.transactionFee = transactionFee;
    }

}
