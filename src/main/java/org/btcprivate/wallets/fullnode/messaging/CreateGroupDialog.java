package org.btcprivate.wallets.fullnode.messaging;

import org.btcprivate.wallets.fullnode.daemon.BTCPClientCaller;
import org.btcprivate.wallets.fullnode.daemon.BTCPClientCaller.WalletCallException;
import org.btcprivate.wallets.fullnode.util.Log;
import org.btcprivate.wallets.fullnode.util.StatusUpdateErrorReporter;
import org.btcprivate.wallets.fullnode.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class CreateGroupDialog
        extends JDialog
{
    protected MessagingPanel msgPanel;
    protected JFrame parentFrame;
    protected MessagingStorage storage;
    protected StatusUpdateErrorReporter errorReporter;
    protected BTCPClientCaller caller;

    protected boolean isOKPressed = false;
    protected String  key    = null;

    protected JLabel     keyLabel = null;
    protected JTextField keyField = null;

    protected JLabel upperLabel;
    protected JLabel lowerLabel;

    protected JProgressBar progress = null;

    JButton okButton;
    JButton cancelButton;

    protected MessagingIdentity createdGroup = null;

    public CreateGroupDialog(MessagingPanel msgPanel, JFrame parentFrame, MessagingStorage storage, StatusUpdateErrorReporter errorReporter, BTCPClientCaller caller)
            throws IOException
    {
        super(parentFrame);

        this.msgPanel      = msgPanel;
        this.parentFrame   = parentFrame;
        this.storage       = storage;
        this.errorReporter = errorReporter;
        this.caller = caller;

        this.setTitle("Create Messaging Group");
        this.setModal(true);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
        controlsPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel tempPanel = new JPanel(new BorderLayout(0, 0));
        tempPanel.add(this.upperLabel = new JLabel(
                "<html>Please enter a keyphrase that identifies a messaging group. " +
                        "Such a keyphrase is usually a #hashtag<br/>or similar keyword known to the " +
                        " group of people participating:</html>"), BorderLayout.CENTER);
        controlsPanel.add(tempPanel);

        JLabel dividerLabel = new JLabel("   ");
        dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 8));
        controlsPanel.add(dividerLabel);

        tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tempPanel.add(keyField = new JTextField(60));
        controlsPanel.add(tempPanel);

        dividerLabel = new JLabel("   ");
        dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 8));
        controlsPanel.add(dividerLabel);

        tempPanel = new JPanel(new BorderLayout(0, 0));
        tempPanel.add(this.lowerLabel = new JLabel(
                        "<html>The group keyphrase will be converted into a group Z address that " +
                                "all participants share to receive <br/>messages. The addition of a messaging " +
                                "group may take considerable time - please be patient!</html>"),
                BorderLayout.CENTER);
        controlsPanel.add(tempPanel);

        dividerLabel = new JLabel("   ");
        dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 8));
        controlsPanel.add(dividerLabel);

        tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tempPanel.add(progress = new JProgressBar());
        controlsPanel.add(tempPanel);

        this.getContentPane().setLayout(new BorderLayout(0, 0));
        this.getContentPane().add(controlsPanel, BorderLayout.NORTH);

        // Form buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 3));
        okButton = new JButton("Create Group");
        buttonPanel.add(okButton);
        buttonPanel.add(new JLabel("   "));
        cancelButton = new JButton("Cancel");
        buttonPanel.add(cancelButton);
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        okButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                CreateGroupDialog.this.processOK();
            }
        });

        cancelButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                CreateGroupDialog.this.setVisible(false);
                CreateGroupDialog.this.dispose();

                CreateGroupDialog.this.isOKPressed = false;
                CreateGroupDialog.this.key = null;
            }
        });


        this.pack();
        this.setLocation(100, 100);
        this.setLocationRelativeTo(parentFrame);
    }


    protected void processOK()
    {
        final String keyPhrase = Util.removeUTF8BOM(CreateGroupDialog.this.keyField.getText());

        if ((keyPhrase == null) || (keyPhrase.trim().length() <= 0))
        {
            JOptionPane.showMessageDialog(
                    CreateGroupDialog.this.getParent(),
                    "Please enter the group keyphrase.", "Keyphrase Required",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        CreateGroupDialog.this.isOKPressed = true;
        CreateGroupDialog.this.key = keyPhrase;

        // Start import
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.progress.setIndeterminate(true);
        this.progress.setValue(1);

        this.okButton.setEnabled(false);
        this.cancelButton.setEnabled(false);

        CreateGroupDialog.this.keyField.setEditable(false);

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    createGroupForKeyPhrase(keyPhrase);
                } catch (Exception e)
                {
                    Log.error("An error occurred when importing private key for group phrase", e);

                    JOptionPane.showMessageDialog(
                            CreateGroupDialog.this.getRootPane().getParent(),
                            "Error occurred when importing private key for group phrase:\n" +
                                    e.getClass().getName() + ":\n" + e.getMessage() + "\n\n" +
                                    "Please ensure that btcpd is running and the key is in the correct \n" +
                                    "form. Try again later.\n",
                            "Error importing private key/group phrase", JOptionPane.ERROR_MESSAGE);
                } finally
                {
                    CreateGroupDialog.this.setVisible(false);
                    CreateGroupDialog.this.dispose();
                }
            }
        }).start();
    }


    public boolean isOKPressed()
    {
        return this.isOKPressed;
    }


    public String getKey()
    {
        return this.key;
    }


    public MessagingIdentity getCreatedGroup()
    {
        return this.createdGroup;
    }


    private void createGroupForKeyPhrase(String keyPhrase)
            throws IOException, InterruptedException, WalletCallException
    {
        String key = Util.convertGroupPhraseToZPrivateKey(keyPhrase);

        // There is no way (it seems) to find out what Z address was added - we need to
        // analyze which one it is.
        // TODO: This relies that noone is importing keys at the same time!
        Set<String> addressesBeforeAddition = new HashSet<String>();
        for (String address: this.caller.getWalletZAddresses())
        {
            addressesBeforeAddition.add(address);
        }

        CreateGroupDialog.this.caller.importPrivateKey(key);

        Set<String> addressesAfterAddition = new HashSet<String>();
        for (String address: this.caller.getWalletZAddresses())
        {
            addressesAfterAddition.add(address);
        }

        addressesAfterAddition.removeAll(addressesBeforeAddition);

        String ZAddress = (addressesAfterAddition.size() > 0) ?
                addressesAfterAddition.iterator().next() :
                this.findZAddressForImportKey(key);
        MessagingIdentity existingIdentity = this.findExistingGroupBySendReceiveAddress(ZAddress);

        if (existingIdentity == null)
        {
            Log.info("Newly created messaging group \"{0}\" address is: {1}", keyPhrase, ZAddress);
            // Add a group personality etc.
            MessagingIdentity newID = new MessagingIdentity();
            newID.setGroup(true);
            newID.setNickname(keyPhrase);
            newID.setSendreceiveaddress(ZAddress);
            newID.setSenderidaddress("");
            newID.setFirstname("");
            newID.setMiddlename("");
            newID.setSurname("");
            newID.setEmail("");
            newID.setStreetaddress("");
            newID.setFacebook("");
            newID.setTwitter("");

            this.storage.addContactIdentity(newID);

            CreateGroupDialog.this.createdGroup = newID;

            JOptionPane.showMessageDialog(
                    CreateGroupDialog.this,
                    "The messaging group with keyphrase:\n" +
                            keyPhrase + "\n" +
                            "has been created successfully. All messages sent by individual users to the " +
                            "group will be sent to Z address:\n"
                            + ZAddress + "\n\n" +
                            "IMPORTANT: Do NOT send any BTCP to this address except in cases of messaging transactions. Any\n" +
                            "funds sent to this address may be spent by any user who has access to the group keyphrase!",
                    "Group Successfully Created",
                    JOptionPane.INFORMATION_MESSAGE);
        } else
        {
            CreateGroupDialog.this.createdGroup = existingIdentity;
            // TODO: Group was already added it seems - see if it can be made more reliable
            JOptionPane.showMessageDialog(
                    CreateGroupDialog.this,
                    "A messaging group for keyphrase:\n" +
                            keyPhrase + "\n" +
                            "already exists",
                    "Group Already Exists",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    CreateGroupDialog.this.msgPanel.getContactList().reloadMessagingIdentities();
                } catch (Exception e)
                {
                    Log.error("Unexpected error in reloading contacts after gathering messages: ", e);
                    CreateGroupDialog.this.errorReporter.reportError(e);
                }
            }
        });
    }


    /**
     * Finds a group identity for a send/receive address.
     *
     * @param address
     *
     * @return identity for the address or null
     */
    private MessagingIdentity findExistingGroupBySendReceiveAddress(String address)
            throws IOException
    {
        MessagingIdentity identity = null;

        for (MessagingIdentity id : this.storage.getContactIdentities(false))
        {
            if (id.isGroup())
            {
                if (id.getSendreceiveaddress().equals(address))
                {
                    identity = id;
                    break;
                }
            }
        }

        return identity;
    }


    /**
     * Checks the wallet's private keys to find what address corresponds to a key.
     *
     * @param key to search for
     *
     * @return address for the key or null;
     */
    private String findZAddressForImportKey(String key)
            throws InterruptedException, WalletCallException, IOException
    {
        String address = null;

        for (String zAddr : this.caller.getWalletZAddresses())
        {
            String privKey = this.caller.getZPrivateKey(zAddr);
            if (privKey.equals(key))
            {
                address = zAddr;
                break;
            }
        }

        return address;
    }
}
