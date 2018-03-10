package org.btcprivate.wallets.fullnode.ui;

import org.btcprivate.wallets.fullnode.daemon.BTCPClientCaller;
import org.btcprivate.wallets.fullnode.daemon.BTCPInstallationObserver;
import org.btcprivate.wallets.fullnode.util.Log;
import org.btcprivate.wallets.fullnode.util.Util;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Table to be used for transactions - specifically.
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class TransactionTable extends DataTable {

    private static final String BLOCK_EXPLORER_URL = "https://explorer.btcprivate.org/tx/";
    private static final String BLOCK_EXPLORER_TEST_URL = "https://testnet.btcprivate.org/tx/";

    private static final String LOCAL_MSG_SHOW_DETAILS = Util.local("LOCAL_MSG_SHOW_DETAILS");
    private static final String LOCAL_MSG_VIEW_ON_EXPLORER = Util.local("LOCAL_MSG_VIEW_ON_EXPLORER");
    private static final String LOCAL_MSG_SHOW_MEMO = Util.local("LOCAL_MSG_SHOW_MEMO");
    private static final String LOCAL_MSG_NO_MEMO = Util.local("LOCAL_MSG_NO_MEMO");
    private static final String LOCAL_MSG_NO_MEMO_TITLE = Util.local("LOCAL_MSG_NO_MEMO_TITLE");
    private static final String LOCAL_MSG_MEMO_DETAIL_1 = Util.local("LOCAL_MSG_MEMO_DETAIL_1");
    private static final String LOCAL_MSG_MEMO_DETAIL_2 = Util.local("LOCAL_MSG_MEMO_DETAIL_2");
    private static final String LOCAL_MSG_NO_MEMO_DETAIL = Util.local("LOCAL_MSG_NO_MEMO_DETAIL");
    private static final String LOCAL_MSG_MEMO = Util.local("LOCAL_MSG_MEMO");
    private static final String LOCAL_MSG_TXN_DETAILS = Util.local("LOCAL_MSG_TXN_DETAILS");
    private static final String LOCAL_MSG_TXN_DETAILS_1 = Util.local("LOCAL_MSG_TXN_DETAILS_1");
    private static final String LOCAL_MSG_TXN_NAME = Util.local("LOCAL_MSG_TXN_NAME");
    private static final String LOCAL_MSG_TXN_VALUE = Util.local("LOCAL_MSG_TXN_VALUE");
    private static final String LOCAL_MSG_TXN_CLOSE = Util.local("LOCAL_MSG_TXN_CLOSE");

    public TransactionTable(final Object[][] rowData, final Object[] columnNames, final JFrame parent,
                            final BTCPClientCaller caller, final BTCPInstallationObserver installationObserver) {
        super(rowData, columnNames);

        JMenuItem showDetails = new JMenuItem(LOCAL_MSG_SHOW_DETAILS);
        popupMenu.add(showDetails);

        showDetails.addActionListener(e -> {
            if ((lastRow >= 0) && (lastColumn >= 0)) {
                try {
                    String txID = TransactionTable.this.getModel().getValueAt(lastRow, 6).toString();
                    txID = txID.replaceAll("\"", ""); // In case it has quotes

                    Log.info("Transaction ID for detail dialog is: " + txID);
                    Map<String, String> details = caller.getRawTransactionDetails(txID);
                    String rawTrans = caller.getRawTransaction(txID);

                    DetailsDialog dd = new DetailsDialog(parent, details);
                    dd.setVisible(true);
                } catch (Exception ex) {
                    Log.error("Unexpected error: ", ex);
                }
            } else {
                // Log perhaps
            }
        });

        JMenuItem showInExplorer = new JMenuItem(LOCAL_MSG_VIEW_ON_EXPLORER);
        popupMenu.add(showInExplorer);

        showInExplorer.addActionListener(e -> {
            if ((lastRow >= 0) && (lastColumn >= 0)) {
                try {
                    String txID = TransactionTable.this.getModel().getValueAt(lastRow, 6).toString();
                    txID = txID.replaceAll("\"", ""); // In case it has quotes

                    Log.info("Transaction ID for block explorer is: " + txID);
                    String urlPrefix = BLOCK_EXPLORER_URL;
                    if (installationObserver.isOnTestNet()) {
                        urlPrefix = BLOCK_EXPLORER_TEST_URL;
                    }
                    Desktop.getDesktop().browse(new URL(urlPrefix + txID).toURI());
                } catch (Exception ex) {
                    Log.error("Unexpected error: ", ex);
                }
            } else {
                // Log perhaps
            }
        });

        JMenuItem showMemoField = new JMenuItem(LOCAL_MSG_SHOW_MEMO);
        popupMenu.add(showMemoField);

        showMemoField.addActionListener(e -> {
            if ((lastRow >= 0) && (lastColumn >= 0)) {
                Cursor oldCursor = parent.getCursor();
                try {
                    String txID = TransactionTable.this.getModel().getValueAt(lastRow, 6).toString();
                    txID = txID.replaceAll("\"", ""); // In case it has quotes

                    String acc = TransactionTable.this.getModel().getValueAt(lastRow, 5).toString();
                    acc = acc.replaceAll("\"", ""); // In case it has quotes

                    boolean isZAddress = Util.isZAddress(acc);
                    if (!isZAddress) {
                        JOptionPane.showMessageDialog(parent,
                                LOCAL_MSG_NO_MEMO,
                                LOCAL_MSG_NO_MEMO_TITLE, JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    Log.info("Transaction ID for Memo field is: " + txID);
                    Log.info("Account for Memo field is: " + acc);
                    parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    // TODO: some day support outgoing Z transactions
                    String MemoField = caller.getMemoField(acc, txID);
                    parent.setCursor(oldCursor);
                    Log.info("Memo field is: " + MemoField);

                    if (MemoField != null) {
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clipboard.setContents(new StringSelection(MemoField), null);

                        MemoField = Util.blockWrapString(MemoField, 80);
                        JOptionPane.showMessageDialog(parent,
                                LOCAL_MSG_MEMO_DETAIL_1 + ": \n" + MemoField + "\n\n"
                                        + LOCAL_MSG_MEMO_DETAIL_2,
                                LOCAL_MSG_MEMO, JOptionPane.PLAIN_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(parent,
                                LOCAL_MSG_NO_MEMO_DETAIL,
                                LOCAL_MSG_NO_MEMO_TITLE, JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    parent.setCursor(oldCursor);
                    Log.error("", ex);
                }
            }
        });

    } // End constructor

    private static class DetailsDialog extends JDialog {
        public DetailsDialog(JFrame parent, Map<String, String> details) throws UnsupportedEncodingException {
            this.setTitle(LOCAL_MSG_TXN_DETAILS);
            this.setSize(600, 310);
            this.setLocation(100, 100);
            this.setLocationRelativeTo(parent);
            this.setModal(true);
            this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            this.getContentPane().setLayout(new BorderLayout(0, 0));

            JPanel tempPanel = new JPanel(new BorderLayout(0, 0));
            tempPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            JLabel infoLabel = new JLabel("<html><span style=\"font-size:0.97em;\">"
                    + LOCAL_MSG_TXN_DETAILS_1 + "</span>");
            infoLabel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
            tempPanel.add(infoLabel, BorderLayout.CENTER);
            this.getContentPane().add(tempPanel, BorderLayout.NORTH);

            String[] columns = new String[]{LOCAL_MSG_TXN_NAME, LOCAL_MSG_TXN_VALUE};
            String[][] data = new String[details.size()][2];
            int i = 0;
            int maxPreferredWidth = 400;
            for (Entry<String, String> ent : details.entrySet()) {
                if (maxPreferredWidth < (ent.getValue().length() * 6)) {
                    maxPreferredWidth = ent.getValue().length() * 6;
                }

                data[i][0] = ent.getKey();
                data[i][1] = ent.getValue();
                i++;
            }

            Arrays.sort(data, new Comparator<String[]>() {
                public int compare(String[] o1, String[] o2) {
                    return o1[0].compareTo(o2[0]);
                }

                public boolean equals(Object obj) {
                    return false;
                }
            });

            DataTable table = new DataTable(data, columns);
            table.getColumnModel().getColumn(0).setPreferredWidth(200);
            table.getColumnModel().getColumn(1).setPreferredWidth(maxPreferredWidth);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            JScrollPane tablePane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

            this.getContentPane().add(tablePane, BorderLayout.CENTER);

            // Lower close button
            JPanel closePanel = new JPanel();
            closePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 3));
            JButton closeButton = new JButton(LOCAL_MSG_TXN_CLOSE);
            closePanel.add(closeButton);
            this.getContentPane().add(closePanel, BorderLayout.SOUTH);

            closeButton.addActionListener(e -> {
                DetailsDialog.this.setVisible(false);
                DetailsDialog.this.dispose();
            });
        }
    }
}