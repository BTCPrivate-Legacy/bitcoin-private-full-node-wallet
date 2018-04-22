package org.btcprivate.wallets.fullnode.ui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EtchedBorder;

import org.btcprivate.wallets.fullnode.daemon.BTCPClientCaller;
import org.btcprivate.wallets.fullnode.daemon.BTCPClientCaller.*;
import org.btcprivate.wallets.fullnode.daemon.BTCPInstallationObserver;
import org.btcprivate.wallets.fullnode.daemon.DataGatheringThread;
import org.btcprivate.wallets.fullnode.util.BackupTracker;
import org.btcprivate.wallets.fullnode.util.Log;
import org.btcprivate.wallets.fullnode.util.StatusUpdateErrorReporter;
import org.btcprivate.wallets.fullnode.util.Util;


/**
 * Provides the functionality for sending cash
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class SendCashPanel
        extends WalletTabPanel
{
    private BTCPClientCaller         clientCaller;
    private StatusUpdateErrorReporter errorReporter;
    private BTCPInstallationObserver installationObserver;
    private BackupTracker             backupTracker;

    private JComboBox  balanceAddressCombo     = null;
    private JPanel     comboBoxParentPanel     = null;
    private String[][] lastAddressBalanceData  = null;
    private String[]   comboBoxItems           = null;
    private DataGatheringThread<String[][]> addressBalanceGatheringThread = null;

    private JTextField destinationAddressField = null;
    private JTextField destinationAmountField  = null;
    private JTextField destinationMemoField    = null;
    private JTextField transactionFeeField     = null;

    private JButton    sendButton              = null;

    private JPanel       operationStatusPanel        = null;
    private JLabel       operationStatusLabel        = null;
    private JProgressBar operationStatusProhgressBar = null;
    private Timer        operationStatusTimer        = null;
    private String       operationStatusID           = null;
    private int          operationStatusCounter      = 0;


    public SendCashPanel(BTCPClientCaller clientCaller,
                         StatusUpdateErrorReporter errorReporter,
                         BTCPInstallationObserver installationObserver,
                         BackupTracker backupTracker)
            throws IOException, InterruptedException, WalletCallException
    {
        this.timers = new ArrayList<Timer>();
        this.threads = new ArrayList<DataGatheringThread<?>>();

        this.clientCaller = clientCaller;
        this.errorReporter = errorReporter;
        this.installationObserver = installationObserver;
        this.backupTracker = backupTracker;

        // Build content
        this.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        this.setLayout(new BorderLayout());
        JPanel sendCashPanel = new JPanel();
        this.add(sendCashPanel, BorderLayout.NORTH);
        sendCashPanel.setLayout(new BoxLayout(sendCashPanel, BoxLayout.Y_AXIS));
        sendCashPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

        JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tempPanel.add(new JLabel("Send BTCP from:       "));
        tempPanel.add(new JLabel(
                "<html><span style=\"font-size:0.8em;\">" +
                        "* Only addresses with a confirmed balance are shown as sources for sending!" +
                        "</span>  "));
        sendCashPanel.add(tempPanel);

        balanceAddressCombo = new JComboBox<>(new String[] { "" });
        comboBoxParentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        comboBoxParentPanel.add(balanceAddressCombo);
        sendCashPanel.add(comboBoxParentPanel);

        JLabel dividerLabel = new JLabel("   ");
        dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 3));
        sendCashPanel.add(dividerLabel);

        tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tempPanel.add(new JLabel("Destination address:"));
        sendCashPanel.add(tempPanel);

        destinationAddressField = new JTextField(73);
        tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tempPanel.add(destinationAddressField);
        sendCashPanel.add(tempPanel);

        dividerLabel = new JLabel("   ");
        dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 3));
        sendCashPanel.add(dividerLabel);

        tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tempPanel.add(new JLabel("Memo (optional):     "));
        tempPanel.add(new JLabel(
                "<html><span style=\"font-size:0.8em;\">" +
                        "* Memo may be specified only if the destination is a Z (Private) address!" +
                        "</span>  "));
        sendCashPanel.add(tempPanel);

        destinationMemoField = new JTextField(73);
        tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tempPanel.add(destinationMemoField);
        sendCashPanel.add(tempPanel);

        dividerLabel = new JLabel("   ");
        dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 3));
        sendCashPanel.add(dividerLabel);

        // Construct a more complex panel for the amount and transaction fee
        JPanel amountAndFeePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JPanel amountPanel = new JPanel(new BorderLayout());
        amountPanel.add(new JLabel("Amount to send:"), BorderLayout.NORTH);
        tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tempPanel.add(destinationAmountField = new JTextField(13));
        destinationAmountField.setHorizontalAlignment(SwingConstants.RIGHT);
        tempPanel.add(new JLabel(" BTCP    "));
        amountPanel.add(tempPanel, BorderLayout.SOUTH);

        JPanel feePanel = new JPanel(new BorderLayout());
        feePanel.add(new JLabel("Transaction fee:"), BorderLayout.NORTH);
        tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tempPanel.add(transactionFeeField = new JTextField(13));
        transactionFeeField.setText("0.0001"); // Default value
        transactionFeeField.setHorizontalAlignment(SwingConstants.RIGHT);
        tempPanel.add(new JLabel(" BTCP"));
        feePanel.add(tempPanel, BorderLayout.SOUTH);

        amountAndFeePanel.add(amountPanel);
        amountAndFeePanel.add(feePanel);
        sendCashPanel.add(amountAndFeePanel);

        dividerLabel = new JLabel("   ");
        dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 3));
        sendCashPanel.add(dividerLabel);

        tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tempPanel.add(sendButton = new JButton("Send   \u27A4\u27A4\u27A4"));
        sendCashPanel.add(tempPanel);

        dividerLabel = new JLabel("   ");
        dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 5));
        sendCashPanel.add(dividerLabel);

        JPanel warningPanel = new JPanel();
        warningPanel.setLayout(new BorderLayout(7, 3));
        JLabel warningL = new JLabel(
                "<html><span style=\"font-size:0.8em;\">" +
                        " * When sending BTCP from a B (Transparent) address, the remaining unspent balance is sent to another " +
                        "auto-generated B address. When sending from a Z (Private) address, the remaining unspent balance remains with " +
                        "the Z address. In both cases, the original sending address cannot be used for sending again until the " +
                        "transaction is confirmed. The address is temporarily removed from the list! Freshly mined coins may only "+
                        "be sent to a Z (Private) address." +
                        "</span>");
        warningPanel.add(warningL, BorderLayout.NORTH);
        sendCashPanel.add(warningPanel);

        dividerLabel = new JLabel("   ");
        dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 15));
        sendCashPanel.add(dividerLabel);

        // Build the operation status panel
        operationStatusPanel = new JPanel();
        sendCashPanel.add(operationStatusPanel);
        operationStatusPanel.setLayout(new BoxLayout(operationStatusPanel, BoxLayout.Y_AXIS));

        tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tempPanel.add(new JLabel("Last operation status: "));
        tempPanel.add(operationStatusLabel = new JLabel("N/A"));
        operationStatusPanel.add(tempPanel);

        dividerLabel = new JLabel("   ");
        dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 6));
        operationStatusPanel.add(dividerLabel);

        tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tempPanel.add(new JLabel("Progress: "));
        tempPanel.add(operationStatusProhgressBar = new JProgressBar(0, 200));
        operationStatusProhgressBar.setPreferredSize(new Dimension(250, 17));
        operationStatusPanel.add(tempPanel);

        dividerLabel = new JLabel("   ");
        dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 13));
        operationStatusPanel.add(dividerLabel);

        // Wire the buttons
        sendButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    SendCashPanel.this.sendCash();
                } catch (Exception ex)
                {
                    Log.error("Unexpected error: ", ex);

                    String errMessage = "";
                    if (ex instanceof WalletCallException)
                    {
                        errMessage = ((WalletCallException)ex).getMessage().replace(",", ",\n");
                    }

                    JOptionPane.showMessageDialog(
                            SendCashPanel.this.getRootPane().getParent(),
                            "An error occurred when sending BTCP:\n" +
                                    errMessage + "\n\n" +
                                    "Please check that the Bitcoin Private daemon is running and\n" +
                                    "the sending parameters are correct.\n",
                            "Error Sending BTCP", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Update the balances via timer and data gathering thread
        this.addressBalanceGatheringThread = new DataGatheringThread<String[][]>(
                new DataGatheringThread.DataGatherer<String[][]>()
                {
                    public String[][] gatherData()
                            throws Exception
                    {
                        long start = System.currentTimeMillis();
                        String[][] data = SendCashPanel.this.getAddressPositiveBalanceDataFromWallet();
                        long end = System.currentTimeMillis();
                        Log.info("Gathering of address/balance table data done in " + (end - start) + "ms." );

                        return data;
                    }
                },
                this.errorReporter, 10000, true);
        this.threads.add(addressBalanceGatheringThread);

        ActionListener alBalancesUpdater = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    // TODO: if the user has opened the combo box - this closes it (maybe fix)
                    SendCashPanel.this.updateWalletAddressPositiveBalanceComboBox();
                } catch (Exception ex)
                {
                    Log.error("Unexpected error: ", ex);
                    SendCashPanel.this.errorReporter.reportError(ex);
                }
            }
        };
        Timer timerBalancesUpdater = new Timer(15000, alBalancesUpdater);
        timerBalancesUpdater.setInitialDelay(3000);
        timerBalancesUpdater.start();
        this.timers.add(timerBalancesUpdater);

        // Add a popup menu to the destination address field - for convenience
        JMenuItem paste = new JMenuItem("Paste address");
        final JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(paste);
        paste.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    String address = (String)Toolkit.getDefaultToolkit().getSystemClipboard().
                            getData(DataFlavor.stringFlavor);
                    if ((address != null) && (address.trim().length() > 0))
                    {
                        SendCashPanel.this.destinationAddressField.setText(address);
                    }
                } catch (Exception ex)
                {
                    Log.error("Unexpected error", ex);
                    // TODO: clipboard exception handling - do it better
                    // java.awt.datatransfer.UnsupportedFlavorException: Unicode String
                    //SendCashPanel.this.errorReporter.reportError(ex);
                }
            }
        });

        this.destinationAddressField.addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
                if ((!e.isConsumed()) && e.isPopupTrigger())
                {
                    popupMenu.show(e.getComponent(), e.getPoint().x, e.getPoint().y);
                    e.consume();
                };
            }

            public void mouseReleased(MouseEvent e)
            {
                if ((!e.isConsumed()) && e.isPopupTrigger())
                {
                    mousePressed(e);
                }
            }
        });

    }


    private void sendCash()
            throws WalletCallException, IOException, InterruptedException
    {
        if (balanceAddressCombo.getItemCount() <= 0)
        {
            JOptionPane.showMessageDialog(
                    SendCashPanel.this.getRootPane().getParent(),
                    "There are no addresses with a positive balance to send\n" +
                            "BTCP from!",
                    "No Funds Available", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (this.balanceAddressCombo.getSelectedIndex() < 0)
        {
            JOptionPane.showMessageDialog(
                    SendCashPanel.this.getRootPane().getParent(),
                    "Please select a source address with a current positive\n" +
                            "balance to send BTCP from!",
                    "Select Source Address", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final String sourceAddress = this.lastAddressBalanceData[this.balanceAddressCombo.getSelectedIndex()][1];
        final String destinationAddress = Util.removeUTF8BOM(this.destinationAddressField.getText());
        final String memo = Util.removeUTF8BOM(this.destinationMemoField.getText());
        final String amount = Util.removeUTF8BOM(this.destinationAmountField.getText());
        final String fee = Util.removeUTF8BOM(this.transactionFeeField.getText());

        // Verify general correctness.
        // https://github.com/BTCPrivate/trezor-common/blob/08fe85ad07bbbdc25cc83ffae8be7aff89245594/coins.json#L575 
        // B Addresses are 35 chars (b1, bx)
        // Z Addresses are 95 chars (zk)
        // base58check encoded		

        // Prevent accidental sending to non-BTCP addresses (as seems to be supported by daemon)
        if (!installationObserver.isOnTestNet())
        {
            if (!(destinationAddress.startsWith("zk") ||
                    destinationAddress.startsWith("b1") ||
                    destinationAddress.startsWith("bx")))
            {
                Object[] options = { "OK" };

                JOptionPane.showOptionDialog(
                        SendCashPanel.this.getRootPane().getParent(),
                        "The destination address to send BTCP to:\n" +
                                destinationAddress + "\n"+
                                "does not appear to be a valid BTCP address. BTCP addresses start with b1, bx or zk!",
                        "Destination Address Invalid",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.ERROR_MESSAGE,
                        null,
                        options,
                        options[0]);

                return; // Do not send anything!
            }
        }
        
        String errorMessage = null;

		final int B_ADDRESS_PROPER_LENGTH = 35;
		final int Z_ADDRESS_PROPER_LENGTH = 95;
		int sourceAddressProperLength = (sourceAddress.startsWith("zk")) ? Z_ADDRESS_PROPER_LENGTH : B_ADDRESS_PROPER_LENGTH;
		int destinationAddressProperLength = (destinationAddress.startsWith("zk")) ? Z_ADDRESS_PROPER_LENGTH : B_ADDRESS_PROPER_LENGTH;

        if ((sourceAddress == null) || (sourceAddress.trim().length() < sourceAddressProperLength))
        {
            errorMessage = "'From' address is invalid; it is too short or missing.";
        } else if (sourceAddress.trim().length() > sourceAddressProperLength)
        {
            errorMessage = "'From' address is invalid; it is too long.";
        }

        if ((destinationAddress == null) || (destinationAddress.trim().length() < destinationAddressProperLength))
        {
            errorMessage = "Destination address is invalid; it is too short or missing.";
        } else if (destinationAddress.trim().length() > destinationAddressProperLength)
        {
            errorMessage = "Destination address is invalid; it is too long.";
        }

        if ((amount == null) || (amount.trim().length() <= 0))
        {
            errorMessage = "Amount to send is invalid; it is missing.";
        } else
        {
            try
            {
                double d = Double.valueOf(amount);
            } catch (NumberFormatException nfe)
            {
                errorMessage = "Amount to send is invalid; it is not a number.";
            }
        }

        if ((fee == null) || (fee.trim().length() <= 0))
        {
            errorMessage = "Transaction fee is invalid; it is missing.";
        } else
        {
            try
            {
                double d = Double.valueOf(fee);
            } catch (NumberFormatException nfe)
            {
                errorMessage = "Transaction fee is invalid; it is not a number.";
            }
        }


        if (errorMessage != null)
        {
            JOptionPane.showMessageDialog(
                    SendCashPanel.this.getRootPane().getParent(),
                    errorMessage, "Sending parameters are incorrect", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check for encrypted wallet
        final boolean bEncryptedWallet = this.clientCaller.isWalletEncrypted();
        if (bEncryptedWallet)
        {
            PasswordDialog pd = new PasswordDialog((JFrame)(SendCashPanel.this.getRootPane().getParent()));
            pd.setVisible(true);

            if (!pd.isOKPressed())
            {
                return;
            }

            this.clientCaller.unlockWallet(pd.getPassword());
        }

        // Call the wallet send method
        operationStatusID = this.clientCaller.sendCash(sourceAddress, destinationAddress, amount, memo, fee);

        // Make sure the keypool has spare addresses
        if ((this.backupTracker.getNumTransactionsSinceLastBackup() % 5) == 0)
        {
            this.clientCaller.keypoolRefill(100);
        }

        // Disable controls after send
        sendButton.setEnabled(false);
        balanceAddressCombo.setEnabled(false);
        destinationAddressField.setEnabled(false);
        destinationAmountField.setEnabled(false);
        destinationMemoField.setEnabled(false);
        transactionFeeField.setEnabled(false);

        // Start a data gathering thread specific to the operation being executed - this is done is a separate
        // thread since the server responds more slowly during JoinSPlits and this blocks he GUI somewhat.
        final DataGatheringThread<Boolean> opFollowingThread = new DataGatheringThread<Boolean>(
                new DataGatheringThread.DataGatherer<Boolean>()
                {
                    public Boolean gatherData()
                            throws Exception
                    {
                        long start = System.currentTimeMillis();
                        Boolean result = clientCaller.isSendingOperationComplete(operationStatusID);
                        long end = System.currentTimeMillis();
                        Log.info("Checking for operation " + operationStatusID + " status done in " + (end - start) + "ms." );

                        return result;
                    }
                },
                this.errorReporter, 2000, true);

        // Start a timer to update the progress of the operation
        operationStatusCounter = 0;
        operationStatusTimer = new Timer(2000, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    // TODO: Handle errors in case of restarted server while wallet is sending ...
                    Boolean opComplete = opFollowingThread.getLastData();

                    if ((opComplete != null) && opComplete.booleanValue())
                    {
                        // End the special thread used to follow the operation
                        opFollowingThread.setSuspended(true);

                        SendCashPanel.this.reportCompleteOperationToTheUser(
                                amount, sourceAddress, destinationAddress);

                        // Lock the wallet again
                        if (bEncryptedWallet)
                        {
                            SendCashPanel.this.clientCaller.lockWallet();
                        }

                        // Restore controls etc.
                        operationStatusCounter = 0;
                        operationStatusID      = null;
                        operationStatusTimer.stop();
                        operationStatusTimer = null;
                        operationStatusProhgressBar.setValue(0);

                        sendButton.setEnabled(true);
                        balanceAddressCombo.setEnabled(true);
                        destinationAddressField.setEnabled(true);
                        destinationAmountField.setEnabled(true);
                        transactionFeeField.setEnabled(true);
                        destinationMemoField.setEnabled(true);
                    } else
                    {
                        // Update the progress
                        operationStatusLabel.setText(
                                "<html><span style=\"color:orange;font-weight:bold\">IN PROGRESS</span></html>");
                        operationStatusCounter += 2;
                        int progress = 0;
                        if (operationStatusCounter <= 100)
                        {
                            progress = operationStatusCounter;
                        } else
                        {
                            progress = 100 + (((operationStatusCounter - 100) * 6) / 10);
                        }
                        operationStatusProhgressBar.setValue(progress);
                    }

                    SendCashPanel.this.repaint();
                } catch (Exception ex)
                {
                    Log.error("Unexpected error: ", ex);
                    SendCashPanel.this.errorReporter.reportError(ex);
                }
            }
        });
        operationStatusTimer.setInitialDelay(0);
        operationStatusTimer.start();
    }


    public void prepareForSending(String address)
    {
        destinationAddressField.setText(address);
    }


    private void updateWalletAddressPositiveBalanceComboBox()
            throws WalletCallException, IOException, InterruptedException
    {
        String[][] newAddressBalanceData = this.addressBalanceGatheringThread.getLastData();

        // The data may be null if nothing is yet obtained
        if (newAddressBalanceData == null)
        {
            return;
        }

        lastAddressBalanceData = newAddressBalanceData;

        comboBoxItems = new String[lastAddressBalanceData.length];
        for (int i = 0; i < lastAddressBalanceData.length; i++)
        {
            // Do numeric formatting or else we may get 1.1111E-5
            comboBoxItems[i] =
                    new DecimalFormat("########0.00######").format(Double.valueOf(lastAddressBalanceData[i][0]))  +
                            " - " + lastAddressBalanceData[i][1];
        }

        int selectedIndex = balanceAddressCombo.getSelectedIndex();
        boolean isEnabled = balanceAddressCombo.isEnabled();
        this.comboBoxParentPanel.remove(balanceAddressCombo);
        balanceAddressCombo = new JComboBox<>(comboBoxItems);
        comboBoxParentPanel.add(balanceAddressCombo);
        if ((balanceAddressCombo.getItemCount() > 0) &&
                (selectedIndex >= 0) &&
                (balanceAddressCombo.getItemCount() > selectedIndex))
        {
            balanceAddressCombo.setSelectedIndex(selectedIndex);
        }
        balanceAddressCombo.setEnabled(isEnabled);

        this.validate();
        this.repaint();
    }


    private String[][] getAddressPositiveBalanceDataFromWallet()
            throws WalletCallException, IOException, InterruptedException
    {
        // Z Addresses - they are OK
        String[] zAddresses = clientCaller.getWalletZAddresses();

        // T Addresses created inside wallet that may be empty
        String[] tAddresses = this.clientCaller.getWalletAllPublicAddresses();
        Set<String> tStoredAddressSet = new HashSet<>();
        for (String address : tAddresses)
        {
            tStoredAddressSet.add(address);
        }

        // T addresses with unspent outputs (even if not GUI created)...
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

        String[][] tempAddressBalances = new String[zAddresses.length + tAddressesCombined.size()][];

        int count = 0;

        for (String address : tAddressesCombined)
        {
            String balance = this.clientCaller.getBalanceForAddress(address);
            if (Double.valueOf(balance) > 0)
            {
                tempAddressBalances[count++] = new String[]
                        {
                                balance, address
                        };
            }
        }

        for (String address : zAddresses)
        {
            String balance = this.clientCaller.getBalanceForAddress(address);
            if (Double.valueOf(balance) > 0)
            {
                tempAddressBalances[count++] = new String[]
                        {
                                balance, address
                        };
            }
        }

        String[][] addressBalances = new String[count][];
        System.arraycopy(tempAddressBalances, 0, addressBalances, 0, count);

        return addressBalances;
    }


    private void reportCompleteOperationToTheUser(String amount, String sourceAddress, String destinationAddress)
            throws InterruptedException, WalletCallException, IOException, URISyntaxException
    {
        if (clientCaller.isCompletedOperationSuccessful(operationStatusID))
        {
            operationStatusLabel.setText(
                    "<html><span style=\"color:green;font-weight:bold\">SUCCESSFUL</span></html>");
            String TXID = clientCaller.getSuccessfulOperationTXID(operationStatusID);

            Object[] options = { "OK", "Copy transaction ID", "View on the blockchain" };

            int option = JOptionPane.showOptionDialog(
                    SendCashPanel.this.getRootPane().getParent(),
                    "Succesfully sent " + amount + " BTCP from address: \n" +
                            sourceAddress + "\n" +
                            "to address: \n" +
                            destinationAddress + "\n\n" +
                            "Transaction ID: " + TXID,
                    "BTCP sent successfully",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (option == 1)
            {
                // Copy the transaction ID to clipboard
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(new StringSelection(TXID), null);
            } else if (option == 2)
            {
                // Open block explorer
                Log.info("Transaction ID for block explorer is: " + TXID);
                String urlPrefix = "https://explorer.btcprivate.org/tx/";
                if (installationObserver.isOnTestNet())
                {
                    urlPrefix = "https://testnet.btcprivate.org/tx/";
                }
                Desktop.getDesktop().browse(new URL(urlPrefix + TXID).toURI());
            }

            // Call the backup tracker - to remind the user
            this.backupTracker.handleNewTransaction();
        } else
        {
            String errorMessage = clientCaller.getOperationFinalErrorMessage(operationStatusID);
            operationStatusLabel.setText(
                    "<html><span style=\"color:red;font-weight:bold\">ERROR: " + errorMessage + "</span></html>");

            JOptionPane.showMessageDialog(
                    SendCashPanel.this.getRootPane().getParent(),
                    "An error occurred when sending BTCP:\n" +
                            errorMessage + "\n\n" +
                            "Please check that the sending parameters are correct, and try again.\n",
                    "Error Sending BTCP", JOptionPane.ERROR_MESSAGE);

        }
    }
}
