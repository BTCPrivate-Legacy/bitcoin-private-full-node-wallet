package org.btcprivate.wallets.fullnode.ui;


import org.btcprivate.wallets.fullnode.daemon.BTCPClientCaller;
import org.btcprivate.wallets.fullnode.daemon.BTCPClientCaller.WalletCallException;
import org.btcprivate.wallets.fullnode.daemon.DataGatheringThread;
import org.btcprivate.wallets.fullnode.util.Log;
import org.btcprivate.wallets.fullnode.util.OSUtil;
import org.btcprivate.wallets.fullnode.util.OSUtil.OS_TYPE;
import org.btcprivate.wallets.fullnode.util.StatusUpdateErrorReporter;
import org.btcprivate.wallets.fullnode.util.Util;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;



/**
 * Addresses panel - shows T/Z addresses and their balnces.
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class AddressesPanel
        extends WalletTabPanel
{
    private JFrame parentFrame;
    private BTCPClientCaller clientCaller;
    private StatusUpdateErrorReporter errorReporter;

    private JTable addressBalanceTable   = null;
    private JScrollPane addressBalanceTablePane  = null;

    String[][] lastAddressBalanceData = null;

    private DataGatheringThread<String[][]> balanceGatheringThread = null;

    private long lastInteractiveRefresh;

    // Table of validated addresses with their validation result. An invalid or watch-only address should not be shown
    // and should be remembered as invalid here
    private Map<String, Boolean> validationMap = new HashMap<>();

    private static final String LOCAL_MENU_NEW_B_ADDRESS = Util.local("LOCAL_MENU_NEW_B_ADDRESS");
    private static final String LOCAL_MENU_NEW_Z_ADDRESS = Util.local("LOCAL_MENU_NEW_Z_ADDRESS");
    private static final String LOCAL_MENU_REFRESH = Util.local("LOCAL_MENU_REFRESH");
    private static final String LOCAL_MENU_BALANCE = Util.local("LOCAL_MENU_BALANCE");
    private static final String LOCAL_MENU_IS_CONFIRMED = Util.local("LOCAL_MENU_IS_CONFIRMED");
    private static final String LOCAL_MENU_ADDRESS = Util.local("LOCAL_MENU_ADDRESS");
    private static final String LOCAL_MSG_WARN_BLOCK_TIME = Util.local("LOCAL_MSG_WARN_BLOCK_TIME");
    private static final String LOCAL_MSG_ADDRESS_CREATED = Util.local("LOCAL_MSG_ADDRESS_CREATED");
    private static final String LOCAL_MSG_ADDRESS_CREATED_TITLE = Util.local("LOCAL_MSG_ADDRESS_CREATED_TITLE");
    private static final String LOCAL_MSG_YES = Util.local("LOCAL_MSG_YES");
    private static final String LOCAL_MSG_NO = Util.local("LOCAL_MSG_NO");
    private static final String LOCAL_MSG_INVALID_OR_WO_ADDRESS = Util.local("LOCAL_MSG_INVALID_OR_WO_ADDRESS");
    private static final String LOCAL_MSG_INVALID_OR_WO_ADDRESS_DETAIL = Util.local("LOCAL_MSG_INVALID_OR_WO_ADDRESS_DETAIL");
    private static final String LOCAL_MSG_INVALID_OR_WO_ADDRESS_TITLE = Util.local("LOCAL_MSG_INVALID_OR_WO_ADDRESS_TITLE");


    public AddressesPanel(JFrame parentFrame, BTCPClientCaller clientCaller, StatusUpdateErrorReporter errorReporter)
            throws IOException, InterruptedException, WalletCallException
    {
        this.parentFrame = parentFrame;
        this.clientCaller = clientCaller;
        this.errorReporter = errorReporter;

        this.lastInteractiveRefresh = System.currentTimeMillis();

        // Build content
        JPanel addressesPanel = this;
        addressesPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        addressesPanel.setLayout(new BorderLayout(0, 0));

        // Build panel of buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 3));
        buttonPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

        JButton newTAddressButton = new JButton(LOCAL_MENU_NEW_B_ADDRESS);

        buttonPanel.add(newTAddressButton);
        JButton newZAddressButton = new JButton(LOCAL_MENU_NEW_Z_ADDRESS);
        buttonPanel.add(newZAddressButton);
        buttonPanel.add(new JLabel("           "));
        JButton refreshButton = new JButton(LOCAL_MENU_REFRESH);
        buttonPanel.add(refreshButton);

        addressesPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Table of addresses
        lastAddressBalanceData = getAddressBalanceDataFromWallet();
        addressesPanel.add(addressBalanceTablePane = new JScrollPane(
                        addressBalanceTable = this.createAddressBalanceTable(lastAddressBalanceData)),
                BorderLayout.CENTER);

        JPanel warningPanel = new JPanel();
        warningPanel.setLayout(new BorderLayout(3, 3));
        warningPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        JLabel warningL = new JLabel(
                "<html><span style=\"font-size:0.8em;\">" +
                        "* " +
                        LOCAL_MSG_WARN_BLOCK_TIME +
                        "</span>");
        warningPanel.add(warningL, BorderLayout.NORTH);
        addressesPanel.add(warningPanel, BorderLayout.NORTH);

        // Thread and timer to update the address/balance table
        this.balanceGatheringThread = new DataGatheringThread<>(
                () -> {
                    long start = System.currentTimeMillis();
                    String[][] data = AddressesPanel.this.getAddressBalanceDataFromWallet();
                    long end = System.currentTimeMillis();
                    Log.info("Gathering of address/balance table data done in " + (end - start) + "ms." );

                    return data;
                },
                this.errorReporter, 25000);
        this.threads.add(this.balanceGatheringThread);

        ActionListener alBalances = e -> {
            try
            {
                AddressesPanel.this.updateWalletAddressBalanceTableAutomated();
            } catch (Exception ex)
            {
                Log.error("Unexpected error: ", ex);
                AddressesPanel.this.errorReporter.reportError(ex);
            }
        };
        Timer t = new Timer(5000, alBalances);
        t.start();
        this.timers.add(t);

        // Button actions
        refreshButton.addActionListener(e -> {
            Cursor oldCursor = null;
            try
            {
                // TODO: dummy progress bar ... maybe
                oldCursor = AddressesPanel.this.getCursor();
                AddressesPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                AddressesPanel.this.updateWalletAddressBalanceTableInteractive();

                AddressesPanel.this.setCursor(oldCursor);
            } catch (Exception ex)
            {
                if (oldCursor != null)
                {
                    AddressesPanel.this.setCursor(oldCursor);
                }

                Log.error("Unexpected error: ", ex);
                AddressesPanel.this.errorReporter.reportError(ex, false);
            }
        });

        newTAddressButton.addActionListener(e -> createNewAddress(false));

        newZAddressButton.addActionListener(e -> createNewAddress(true));

    }


    // Null if not selected
    public String getSelectedAddress()
    {
        String address = null;

        int selectedRow = this.addressBalanceTable.getSelectedRow();

        if (selectedRow != -1)
        {
            address = this.addressBalanceTable.getModel().getValueAt(selectedRow, 2).toString();
        }

        return address;
    }


    private void createNewAddress(boolean isZAddress)
    {
        try
        {
            // Check for encrypted wallet
            final boolean bEncryptedWallet = this.clientCaller.isWalletEncrypted();
            if (bEncryptedWallet && isZAddress)
            {
                PasswordDialog pd = new PasswordDialog((JFrame)(this.getRootPane().getParent()));
                pd.setVisible(true);

                if (!pd.isOKPressed())
                {
                    return;
                }

                this.clientCaller.unlockWallet(pd.getPassword());
            }

            String address = this.clientCaller.createNewAddress(isZAddress);

            // Lock the wallet again
            if (bEncryptedWallet && isZAddress)
            {
                this.clientCaller.lockWallet();
            }

            JOptionPane.showMessageDialog(
                    this.getRootPane().getParent(),
                    LOCAL_MSG_ADDRESS_CREATED + address,
                    LOCAL_MSG_ADDRESS_CREATED_TITLE, JOptionPane.INFORMATION_MESSAGE);

            this.updateWalletAddressBalanceTableInteractive();
        } catch (Exception e)
        {
            Log.error("Unexpected error: ", e);
            AddressesPanel.this.errorReporter.reportError(e, false);
        }
    }

    // Interactive and non-interactive are mutually exclusive
    private synchronized void updateWalletAddressBalanceTableInteractive()
            throws WalletCallException, IOException, InterruptedException
    {
        this.lastInteractiveRefresh = System.currentTimeMillis();

        String[][] newAddressBalanceData = this.getAddressBalanceDataFromWallet();

        if (Util.arraysAreDifferent(lastAddressBalanceData, newAddressBalanceData))
        {
            Log.info("Updating table of addresses/balances [Interactive]");
            this.remove(addressBalanceTablePane);
            this.add(addressBalanceTablePane = new JScrollPane(
                            addressBalanceTable = this.createAddressBalanceTable(newAddressBalanceData)),
                    BorderLayout.CENTER);
            lastAddressBalanceData = newAddressBalanceData;

            this.validate();
            this.repaint();
        }
    }


    // Interactive and non-interactive are mutually exclusive
    private synchronized void updateWalletAddressBalanceTableAutomated()
            throws WalletCallException, IOException, InterruptedException
    {
        // Make sure it is > 1 min since the last interactive refresh
        if ((System.currentTimeMillis() - lastInteractiveRefresh) < (60 * 1000))
        {
            return;
        }

        String[][] newAddressBalanceData = this.balanceGatheringThread.getLastData();

        if ((newAddressBalanceData != null) &&
                Util.arraysAreDifferent(lastAddressBalanceData, newAddressBalanceData))
        {
            Log.info("Updating table of addresses/balances [Automated]");
            this.remove(addressBalanceTablePane);
            this.add(addressBalanceTablePane = new JScrollPane(
                            addressBalanceTable = this.createAddressBalanceTable(newAddressBalanceData)),
                    BorderLayout.CENTER);
            lastAddressBalanceData = newAddressBalanceData;
            this.validate();
            this.repaint();
        }
    }


    private JTable createAddressBalanceTable(String rowData[][])
            throws WalletCallException, IOException, InterruptedException
    {
        String columnNames[] = {LOCAL_MENU_BALANCE,LOCAL_MENU_IS_CONFIRMED, LOCAL_MENU_ADDRESS};
        JTable table = new AddressTable(rowData, columnNames, this.clientCaller);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.getColumnModel().getColumn(0).setPreferredWidth(160);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(1000);

        return table;
    }


    private String[][] getAddressBalanceDataFromWallet()
            throws WalletCallException, IOException, InterruptedException
    {
        // Z Addresses - they are OK
        String[] zAddresses = clientCaller.getWalletZAddresses();

        // T Addresses listed with the list received by addr comamnd
        String[] tAddresses = this.clientCaller.getWalletAllPublicAddresses();
        Set<String> tStoredAddressSet = new HashSet<>();
        for (String address : tAddresses)
        {
            tStoredAddressSet.add(address);
        }

        // T addresses with unspent outputs - just in case they are different
        String[] tAddressesWithUnspentOuts = this.clientCaller.getWalletPublicAddressesWithUnspentOutputs();
        Set<String> tAddressSetWithUnspentOuts = new HashSet<>();
        for (String address : tAddressesWithUnspentOuts)
        {
            tAddressSetWithUnspentOuts.add(address);
        }

        // Combine all known T addresses
        Set<String> tAddressesCombined = new HashSet<>();
        tAddressesCombined.addAll(tStoredAddressSet);
        tAddressesCombined.addAll(tAddressSetWithUnspentOuts);

        String[][] addressBalances = new String[zAddresses.length + tAddressesCombined.size()][];

        // Format double numbers - else sometimes we get exponential notation 1E-4 ZEN
        DecimalFormat df = new DecimalFormat("########0.00######");

        String confirmed    = "\u2690";
        String notConfirmed = "\u2691";

        // Windows does not support the flag symbol (Windows 7 by default)
        // TODO: isolate OS-specific symbol codes in a separate class
        OS_TYPE os = OSUtil.getOSType();
        if (os == OS_TYPE.WINDOWS)
        {
            confirmed = " \u25B7";
            notConfirmed = " \u25B6";
        }

        int i = 0;

        for (String address : tAddressesCombined)
        {
            String addressToDisplay = address;
            // Make sure the current address is not watch-only or invalid
            if (!this.validationMap.containsKey(address))
            {
                boolean validationResult = this.clientCaller.isWatchOnlyOrInvalidAddress(address);
                this.validationMap.put(address, new Boolean(validationResult));

                if (validationResult)
                {
                    JOptionPane.showMessageDialog(
                            this.parentFrame,
                            LOCAL_MSG_INVALID_OR_WO_ADDRESS + ":\n" +
                                    address + "\n\n" +
                                    LOCAL_MSG_INVALID_OR_WO_ADDRESS_DETAIL,
                            LOCAL_MSG_INVALID_OR_WO_ADDRESS_TITLE,
                            JOptionPane.ERROR_MESSAGE);
                }
            }

            boolean watchOnlyOrInvalid = this.validationMap.get(address).booleanValue();
            if (watchOnlyOrInvalid)
            {
                Log.error("The following address is invalid or a watch-only address: {0}. It will not be displayed!", address);
                addressToDisplay = LOCAL_MSG_INVALID_OR_WO_ADDRESS;
            }
            // End of check for invalid/watch only addresses

            String confirmedBalance = this.clientCaller.getBalanceForAddress(address);
            String unconfirmedBalance = this.clientCaller.getUnconfirmedBalanceForAddress(address);
            boolean isConfirmed =  (confirmedBalance.equals(unconfirmedBalance));
            String balanceToShow = df.format(Double.valueOf(
                    isConfirmed ? confirmedBalance : unconfirmedBalance));

            addressBalances[i++] = new String[]
                    {
                            balanceToShow,
                            isConfirmed ? (LOCAL_MSG_YES + " " + confirmed) : (LOCAL_MSG_NO + " " + notConfirmed),
                            addressToDisplay
                    };
        }

        for (String address : zAddresses)
        {
            String confirmedBalance = this.clientCaller.getBalanceForAddress(address);
            String unconfirmedBalance = this.clientCaller.getUnconfirmedBalanceForAddress(address);
            boolean isConfirmed =  (confirmedBalance.equals(unconfirmedBalance));
            String balanceToShow = df.format(Double.valueOf(
                    isConfirmed ? confirmedBalance : unconfirmedBalance));

            addressBalances[i++] = new String[]
                    {
                            balanceToShow,
                            isConfirmed ? (LOCAL_MSG_YES + " " + confirmed) : (LOCAL_MSG_YES + " " + notConfirmed),
                            address
                    };
        }

        return addressBalances;
    }

}