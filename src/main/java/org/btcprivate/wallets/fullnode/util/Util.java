package org.btcprivate.wallets.fullnode.util;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import java.io.*;
import java.lang.reflect.Method;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utilities - generally reusable across classes.
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
@SuppressWarnings({"unchecked", "deprecated"})
public class Util
{

    public static ResourceBundle bundle;


    //general daemon params
    public final static String WINDOWS_ENV_FOLDER  = "APPDATA";
    public final static String OSX_WINDOWS_ZCASH_KEY_FOLDER  = "/ZcashParams";
    public final static String OSX_ENV_FOLDER = "Library/Application Support";

    //proving key params
    public final static String VERIFYING_KEY_FILE = "sprout-verifying.key";
    public final static String PROVING_KEY_FILE = "sprout-proving.key";
    public final static String VERIFYING_KEY_FILE_SOURCE = "keys/sprout-verifying.key";
    public static final String PROVING_KEY_PATH_URL = "https://storage.googleapis.com/btcp-sprout-key/sprout-proving.key";
    public static final String PROVING_KEY_SHA256 = "8bc20a7f013b2b58970cddd2e7ea028975c88ae7ceb9259a5344a16bc2c0eef7";


    public static String local(String key) {
        if (bundle == null) {
            try {
                Locale locale = Locale.getDefault();
                bundle = ResourceBundle.getBundle("btcpwalletui",locale);
            }catch(MissingResourceException mre){
                Locale locale = Locale.ENGLISH;
                bundle = ResourceBundle.getBundle("btcpwalletui",locale);
            }
        }
        return bundle.getString(key);
    }
    // Compares two string arrays (two dimensional).
    public static boolean arraysAreDifferent(String ar1[][], String ar2[][])
    {
        if (ar1 == null)
        {
            if (ar2 != null)
            {
                return true;
            }
        } else if (ar2 == null)
        {
            return true;
        }

        if (ar1.length != ar2.length)
        {
            return true;
        }

        for (int i = 0; i < ar1.length; i++)
        {
            if (ar1[i].length != ar2[i].length)
            {
                return true;
            }

            for (int j = 0; j < ar1[i].length; j++)
            {
                String s1 = ar1[i][j];
                String s2 = ar2[i][j];

                if (s1 == null)
                {
                    if (s2 != null)
                    {
                        return true;
                    }
                } else if (s2 == null)
                {
                    return true;
                } else
                {
                    if (!s1.equals(s2))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }


    // Turns a 1.0.7+ error message to a an old JSOn style message
    // info - new style error message
    public static JsonObject getJsonErrorMessage(String info)
            throws IOException
    {
        JsonObject jInfo = new JsonObject();

        // Error message here comes from ZCash 1.0.7+ and is like:
        //zcash-cli getinfo
        //error code: -28
        //error message:
        //Loading block index...
        LineNumberReader lnr = new LineNumberReader(new StringReader(info));
        int errCode =  Integer.parseInt(lnr.readLine().substring(11).trim());
        jInfo.set("code", errCode);
        lnr.readLine();
        jInfo.set("message", lnr.readLine().trim());

        return jInfo;
    }


    /**
     * Escapes a text value to a form suitable to be displayed in HTML content. Important control
     * characters are replaced with entities.
     *
     * @param inputValue th value to escape
     *
     * @return the "safe" value to display.
     */
    public static String escapeHTMLValue(String inputValue)
    {
        StringBuilder outputValue = new StringBuilder();
        for (char c : inputValue.toCharArray())
        {
            if ((c > 127) || (c == '"') || (c == '<') || (c == '>') || (c == '&'))
            {
                outputValue.append("&#");
                outputValue.append((int)c);
                outputValue.append(';');
            } else
            {
                outputValue.append(c);
            }
        }
        return outputValue.toString();
    }


    public static boolean stringIsEmpty(String s)
    {
        return (s == null) || (s.length() <= 0);
    }


    public static String decodeHexMemo(String memoHex)
            throws UnsupportedEncodingException
    {
        // Skip empty memos
        if (memoHex.startsWith("f600000000"))
        {
            return null;
        }

        // should be read with UTF-8
        byte[] bytes = new byte[(memoHex.length() / 2) + 1];
        int count = 0;

        for (int j = 0; j < memoHex.length(); j += 2)
        {
            String str = memoHex.substring(j, j + 2);
            bytes[count++] = (byte)Integer.parseInt(str, 16);
        }

        // Remove zero padding
        // TODO: may cause problems if UNICODE chars have trailing ZEROS
        while (count > 0)
        {
            if (bytes[count - 1] == 0)
            {
                count--;
            } else
            {
                break;
            }
        }

        return new String(bytes, 0, count, "UTF-8");
    }


    public static String encodeHexString(String str)
            throws UnsupportedEncodingException
    {
        return encodeHexArray(str.getBytes("UTF-8"));
    }


    public static String encodeHexArray(byte array[])
    {
        StringBuilder encoded = new StringBuilder();
        for (byte c : array)
        {
            String hexByte = Integer.toHexString((int)c);
            if (hexByte.length() < 2)
            {
                hexByte = "0" + hexByte;
            } else if (hexByte.length() > 2)
            {
                hexByte = hexByte.substring(hexByte.length() - 2, hexByte.length());
            }
            encoded.append(hexByte);
        }

        return encoded.toString();
    }


    /**
     * Maintains a set of old copies for a file.
     * For a file dir/file, the old versions are dir/file.1, dir/file.2 etc. up to 9.
     *
     * @param dir base directory
     *
     * @param file name of the original file
     */
    public static void renameFileForMultiVersionBackup(File dir, String file)
    {
        final int VERSION_COUNT = 9;

        // Delete last one if it exists
        File last = new File(dir, file + "." + VERSION_COUNT);
        if (last.exists())
        {
            last.delete();
        }

        // Iterate and rename
        for (int i = VERSION_COUNT - 1; i >= 1; i--)
        {
            File f = new File(dir, file + "." + i);
            int newIndex = i + 1;
            if (f.exists())
            {
                f.renameTo(new File(dir, file + "." + newIndex));
            }
        }

        // Rename last one
        File orig = new File(dir, file);
        if (orig.exists())
        {
            orig.renameTo(new File(dir, file + ".1"));
        }
    }


    public static JsonObject parseJsonObject(String json)
            throws IOException
    {
        try
        {
            return Json.parse(json).asObject();
        } catch (RuntimeException rte)
        {
            throw new IOException(rte);
        }
    }


    public static JsonObject parseJsonObject(Reader r)
            throws IOException
    {
        try
        {
            return Json.parse(r).asObject();
        } catch (RuntimeException rte)
        {
            throw new IOException(rte);
        }
    }


    public static byte[] calculateSHA256Digest(byte[] input)
            throws IOException
    {
        try
        {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            DigestInputStream dis = new DigestInputStream(new ByteArrayInputStream(input), sha256);
            byte [] temp = new byte[0x1 << 13];
            byte[] digest;
            while(dis.read(temp) >= 0);
            {
                digest = sha256.digest();
            }

            return digest;
        } catch (NoSuchAlgorithmException impossible)
        {
            throw new IOException(impossible);
        }
    }


    public static String convertGroupPhraseToZPrivateKey(String phrase)
            throws IOException
    {
        byte phraseBytes[] = phrase.getBytes("UTF-8");
        byte phraseDigest[] = calculateSHA256Digest(phraseBytes);

        phraseDigest[0] &= (byte)0x0f;

        //System.out.println(encodeHexArray(phraseDigest));

        byte base58Input[] = new byte[phraseDigest.length + 2];
        base58Input[0] = (byte)0xab;
        base58Input[1] = (byte)0x36;
        System.arraycopy(phraseDigest, 0, base58Input, 2, phraseDigest.length);

        // Do a double SHA356 to get a checksum for the data to encode
        byte shaStage1[] = calculateSHA256Digest(base58Input);
        byte checksum[] = calculateSHA256Digest(shaStage1);

        byte base58CheckInput[] = new byte[base58Input.length + 4];
        System.arraycopy(base58Input, 0, base58CheckInput, 0, base58Input.length);
        System.arraycopy(checksum, 0, base58CheckInput, base58Input.length, 4);

        // Call BitcoinJ via reflection - and report error if missing
        try
        {
            Class base58Class = Class.forName("org.bitcoinj.core.Base58");
            Method encode = base58Class.getMethod("encode", byte[].class);
            return (String)encode.invoke(null, base58CheckInput);
        } catch (Exception e)
        {
            throw new IOException(
                    "There was a problem invoking the BitcoinJ library to do Base58 encoding. " +
                            "Make sure the bitcoinj-core-0.14.5.jar is available!", e);
        }
    }


    // zc/zt - mainnet and testnet
    // TODO: We need a much more precise criterion to distinguish T/Z adresses;
    // Testnet: "zz", "zt"
    public static boolean isZAddress(String address)
    {
        return (address != null) &&
                (address.startsWith("zk")) &&
//                (address.startsWith("zk") || address.startsWith("zz") || address.startsWith("zt")) &&
                (address.length() > 40);
    }


    /**
     * Delets a directory and all of its subdirectories.
     *
     * @param dir directory to delete.
     *
     * @throws IOException if not successful
     */
    public static void deleteDirectory(File dir)
            throws IOException
    {
        for (File f : dir.listFiles())
        {
            if (f.isDirectory())
            {
                deleteDirectory(f);
            } else
            {
                if (!f.delete())
                {
                    throw new IOException("Could not delete file: " + f.getAbsolutePath());
                }
            }
        }

        if (!dir.delete())
        {
            throw new IOException("Could not delete directory: " + dir.getAbsolutePath());
        }
    }


    /**
     * Wraps an input string in a block form with the specified width. LF is used to end each line.
     *
     * @param inStr
     * @param width
     *
     * @return input wrapped
     */
    public static String blockWrapString(String inStr, int width)
    {
        StringBuilder block = new StringBuilder();

        int position = 0;
        while (position < inStr.length())
        {
            int endPosition = Math.min(position + width, inStr.length());
            block.append(inStr.substring(position, endPosition));
            block.append("\n");
            position += width;
        }

        return block.toString();
    }


    public static byte[] loadFileInMemory(File f)
            throws IOException
    {
        RandomAccessFile oRAF = null;
        try
        {
            oRAF = new RandomAccessFile(f, "r");

            byte bytes[] = new byte[(int)oRAF.length()];
            oRAF.readFully(bytes);

            return bytes;
        } finally
        {
            if (oRAF != null)
            {
                oRAF.close();
            }
        }
    }
    
    public static final String UTF8_BOM = "\uFEFF";

	public static String removeUTF8BOM(String s)
	{
    	return (s.startsWith(UTF8_BOM) ? s.substring(1) : s);
    }
}
