package org.btcprivate.wallets.fullnode.daemon;

import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitorInputStream;
import javax.xml.bind.DatatypeConverter;

import org.btcprivate.wallets.fullnode.ui.StartupProgressDialog;
import org.btcprivate.wallets.fullnode.util.OSUtil;
import org.btcprivate.wallets.fullnode.util.OSUtil.*;
import org.btcprivate.wallets.fullnode.util.Util;
import static org.btcprivate.wallets.fullnode.util.Util.*;


/**
 * Fetches the proving key.  Deliberately hardcoded.
 *
 * @author zab
 */
public class ProvingKeyFetcher {

    private static final int PROVING_KEY_SIZE = 910173851;






    private static final String LOCAL_MSG_PROVINGKEY_DOWNLOAD_REQURED = "LOCAL_MSG_PROVINGKEY_DOWNLOAD_REQURED";
    private static final String LOCAL_MSG_DOWNLOADING_PROVING_KEY = "LOCAL_MSG_DOWNLOADING_PROVING_KEY";
    private static final String LOCAL_MSG_VERIFYING_PROVING_KEY = "LOCAL_MSG_VERIFYING_PROVING_KEY";
    private static final String LOCAL_MSG_NO_PROVING_KEY = "LOCAL_MSG_NO_PROVING_KEY";
    private static final String LOCAL_MSG_VERIFYING_DOWNLOADED_PROVING_KEY = "LOCAL_MSG_VERIFYING_DOWNLOADED_PROVING_KEY";
    private static final String LOCAL_MSG_VERIFYING_DOWNLOADED_PROVING_KEY_FAILED = "LOCAL_MSG_VERIFYING_DOWNLOADED_PROVING_KEY_FAILED";


    public void fetchIfMissing(StartupProgressDialog parent) throws IOException {
        try {
            verifyOrFetch(parent);
        } catch (InterruptedIOException iox) {
            JOptionPane.showMessageDialog(parent, LOCAL_MSG_NO_PROVING_KEY);
            System.exit(-3);
        }
    }

    private void verifyOrFetch(StartupProgressDialog parent)
            throws IOException {

        File zCashParams = getZCashParamsFile();

        boolean needsFetch = false;
        if (!zCashParams.exists()) {
            needsFetch = true;
            zCashParams.mkdirs();
        }

        // verifying key is small, always copy it
        File verifyingKeyFile = new File(zCashParams, VERIFYING_KEY_FILE);
        FileOutputStream fos = new FileOutputStream(verifyingKeyFile);
        InputStream is = ProvingKeyFetcher.class.getClassLoader().getResourceAsStream(VERIFYING_KEY_FILE_SOURCE);
        copy(is, fos);
        fos.close();
        is = null;

        File provingKeyFile = new File(zCashParams, PROVING_KEY_FILE);
        provingKeyFile = provingKeyFile.getCanonicalFile();
        if (!provingKeyFile.exists()) {
            needsFetch = true;
        } else if (provingKeyFile.length() != PROVING_KEY_SIZE) {
            needsFetch = true;
        }

        if (!needsFetch) {
            return;
        }

        JOptionPane.showMessageDialog(parent, Util.local(LOCAL_MSG_PROVINGKEY_DOWNLOAD_REQURED));

        parent.setProgressText(LOCAL_MSG_DOWNLOADING_PROVING_KEY);
        provingKeyFile.delete();
        OutputStream os = new BufferedOutputStream(new FileOutputStream(provingKeyFile));
        URL keyURL = new URL(PROVING_KEY_PATH_URL);
        URLConnection urlc = keyURL.openConnection();
        urlc.setRequestProperty("User-Agent", "Wget/1.17.1 (linux-gnu)");

        try {
            is = urlc.getInputStream();
            ProgressMonitorInputStream pmis = new ProgressMonitorInputStream(parent, LOCAL_MSG_DOWNLOADING_PROVING_KEY, is);
            pmis.getProgressMonitor().setMaximum(PROVING_KEY_SIZE);
            pmis.getProgressMonitor().setMillisToPopup(10);

            copy(pmis, os);
            os.close();
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ignore) {
            }
        }
        parent.setProgressText(LOCAL_MSG_VERIFYING_DOWNLOADED_PROVING_KEY);
        if (!checkSHA256(provingKeyFile, parent)) {
            JOptionPane.showMessageDialog(parent, LOCAL_MSG_VERIFYING_DOWNLOADED_PROVING_KEY_FAILED);
            System.exit(-4);
        }
    }

    private File getZCashParamsFile() throws IOException {
        OS_TYPE ost = OSUtil.getOSType();
        File zCashParams = null;

        if (ost == OS_TYPE.WINDOWS) {
            zCashParams = new File(System.getenv(WINDOWS_ENV_FOLDER) + OSX_WINDOWS_ZCASH_KEY_FOLDER);
        } else if (ost == OS_TYPE.MAC_OS) {
            File userHome = new File(System.getProperty("user.home"));
            zCashParams = new File(userHome, OSX_ENV_FOLDER + OSX_WINDOWS_ZCASH_KEY_FOLDER);
        }

        zCashParams = zCashParams.getCanonicalFile();
        return zCashParams;
    }


    private static void copy(InputStream is, OutputStream os) throws IOException {
        byte[] buf = new byte[0x1 << 13];
        int read;
        while ((read = is.read(buf)) > -0) {
            os.write(buf, 0, read);
        }
        os.flush();
    }

    private static boolean checkSHA256(File provingKey, Component parent) throws IOException {
        MessageDigest sha256;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException impossible) {
            throw new IOException(impossible);
        }
        try (InputStream is = new BufferedInputStream(new FileInputStream(provingKey))) {
            ProgressMonitorInputStream pmis = new ProgressMonitorInputStream(parent, LOCAL_MSG_VERIFYING_PROVING_KEY, is);
            pmis.getProgressMonitor().setMaximum(PROVING_KEY_SIZE);
            pmis.getProgressMonitor().setMillisToPopup(10);
            DigestInputStream dis = new DigestInputStream(pmis, sha256);
            byte[] temp = new byte[0x1 << 13];
            while (dis.read(temp) >= 0) ;
            byte[] digest = sha256.digest();
            return PROVING_KEY_SHA256.equalsIgnoreCase(DatatypeConverter.printHexBinary(digest));
        }
    }
}