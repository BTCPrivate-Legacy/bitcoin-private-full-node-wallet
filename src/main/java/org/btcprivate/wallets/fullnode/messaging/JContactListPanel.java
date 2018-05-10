package org.btcprivate.wallets.fullnode.messaging;


import org.btcprivate.wallets.fullnode.util.Log;
import org.btcprivate.wallets.fullnode.util.StatusUpdateErrorReporter;
import org.btcprivate.wallets.fullnode.util.Util;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Main panel for messaging
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class JContactListPanel
    extends JPanel {
  private MessagingPanel parent;
  private MessagingStorage messagingStorage;
  private ContactList list;
  private StatusUpdateErrorReporter errorReporter;
  private JFrame parentFrame;

  private JPopupMenu popupMenu;


  private static final String LOCAL_MSG_CONTACT_LIST = Util.local("LOCAL_MSG_CONTACT_LIST");
  private static final String LOCAL_MSG_ADD_CONTACT = Util.local("LOCAL_MSG_ADD_CONTACT");
  private static final String LOCAL_MSG_DEL_CONTACT = Util.local("LOCAL_MSG_DEL_CONTACT");
  private static final String LOCAL_MSG_GROUP = Util.local("LOCAL_MSG_GROUP");
  private static final String LOCAL_MSG_CREATE_GROUP = Util.local("LOCAL_MSG_CREATE_GROUP");
  private static final String LOCAL_MSG_SHOW_DETAILS = Util.local("LOCAL_MSG_SHOW_DETAILS");
  private static final String LOCAL_MENU_DELETE_CONTACT = Util.local("LOCAL_MENU_DELETE_CONTACT");
  private static final String LOCAL_MSG_SEND_CONTACT_DETAILS = Util.local("LOCAL_MSG_SEND_CONTACT_DETAILS");
  private static final String LOCAL_MSG_NO_CONTACT_SELECTED = Util.local("LOCAL_MSG_NO_CONTACT_SELECTED");
  private static final String LOCAL_MSG_NO_CONTACT_SEND_DETAIL = Util.local("LOCAL_MSG_NO_CONTACT_SEND_DETAIL");
  private static final String LOCAL_MSG_SEND_OWN_ID_Q = Util.local("LOCAL_MSG_SEND_OWN_ID_Q");
  private static final String LOCAL_MSG_SEND_ANONYMOUS_CONTACT = Util.local("LOCAL_MSG_SEND_ANONYMOUS_CONTACT");


  public JContactListPanel(MessagingPanel parent,
                           JFrame parentFrame,
                           MessagingStorage messagingStorage,
                           StatusUpdateErrorReporter errorReporter)
      throws IOException {
    super();

    this.parent = parent;
    this.parentFrame = parentFrame;
    this.messagingStorage = messagingStorage;
    this.errorReporter = errorReporter;

    this.setLayout(new BorderLayout(0, 0));

    list = new ContactList();
    list.setIdentities(this.messagingStorage.getContactIdentities(true));
    this.add(new JScrollPane(list), BorderLayout.CENTER);

    JPanel upperPanel = new JPanel(new BorderLayout(0, 0));
    upperPanel.add(new JLabel(
            "<html><span style=\"font-size:1.2em;font-style:bold;\">" + LOCAL_MSG_CONTACT_LIST + "</span></html>"),
        BorderLayout.WEST);
    URL addIconUrl = this.getClass().getClassLoader().getResource("images/add12.png");
    ImageIcon addIcon = new ImageIcon(addIconUrl);
    URL removeIconUrl = this.getClass().getClassLoader().getResource("images/remove12.png");
    ImageIcon removeIcon = new ImageIcon(removeIconUrl);
    JButton addButton = new JButton(addIcon);
    addButton.setToolTipText(LOCAL_MSG_ADD_CONTACT);
    JButton removeButton = new JButton(removeIcon);
    removeButton.setToolTipText(LOCAL_MSG_DEL_CONTACT);
    JButton addGroupButton = new JButton(
        "<html><span style=\"font-size:0.7em;\">" + LOCAL_MSG_GROUP + "</span></html>", addIcon);
    addGroupButton.setToolTipText(LOCAL_MSG_CREATE_GROUP);
    JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
    tempPanel.add(removeButton);
    tempPanel.add(addButton);
    //currently disable group until we have time to investigate after fork
    //tempPanel.add(addGroupButton);
    upperPanel.add(tempPanel, BorderLayout.EAST);

    upperPanel.add(new JLabel(
            "<html><span style=\"font-size:1.6em;font-style:italic;\">&nbsp;</span>"),
        BorderLayout.CENTER);
    upperPanel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
    this.add(upperPanel, BorderLayout.NORTH);

    // Add a listener for adding a contact
    addButton.addActionListener(e -> JContactListPanel.this.parent.importContactIdentity());

    // Add a listener for adding a group
    addGroupButton.addActionListener(e -> JContactListPanel.this.parent.addMessagingGroup());


    // Add a listener for removing a contact
    removeButton.addActionListener(e -> JContactListPanel.this.parent.removeSelectedContact());

    // Take care of updating the messages on selection
    list.addListSelectionListener(e -> {
      try {
        if (e.getValueIsAdjusting()) {
          return; // Change is not final
        }

        MessagingIdentity id = JContactListPanel.this.list.getSelectedValue();

        if (id == null) {
          return; // Nothing selected
        }

        Cursor oldCursor = JContactListPanel.this.parentFrame.getCursor();
        try {
          JContactListPanel.this.parentFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          JContactListPanel.this.parent.displayMessagesForContact(id);
        } finally {
          JContactListPanel.this.parentFrame.setCursor(oldCursor);
        }
      } catch (IOException ioe) {
        Log.error("Unexpected error: ", ioe);
        JContactListPanel.this.errorReporter.reportError(ioe, false);
      }
    });

    // Mouse listener is used to show the popup menu
    list.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if ((!e.isConsumed()) && e.isPopupTrigger()) {
          ContactList list = (ContactList) e.getSource();
          if (list.getSelectedValue() != null) {
            popupMenu.show(e.getComponent(), e.getPoint().x, e.getPoint().y);
            e.consume();
          }
        }
      }

      public void mouseReleased(MouseEvent e) {
        if ((!e.isConsumed()) && e.isPopupTrigger()) {
          mousePressed(e);
        }
      }
    });


    // Actions of the popup menu
    this.popupMenu = new JPopupMenu();
    int accelaratorKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    JMenuItem showDetails = new JMenuItem(LOCAL_MSG_SHOW_DETAILS);
    popupMenu.add(showDetails);
    //showDetails.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, accelaratorKeyMask));
    showDetails.addActionListener(e -> {
      // Show a messaging identity dialog
      if (list.getSelectedValue() != null) {
        IdentityInfoDialog iid = new IdentityInfoDialog(
            JContactListPanel.this.parentFrame, list.getSelectedValue());
        iid.setVisible(true);
      }
    });

    JMenuItem removeContact = new JMenuItem(LOCAL_MENU_DELETE_CONTACT);
    popupMenu.add(removeContact);
    //removeContact.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, accelaratorKeyMask));
    removeContact.addActionListener(e -> JContactListPanel.this.parent.removeSelectedContact());

    JMenuItem sendContactDetails = new JMenuItem(LOCAL_MSG_SEND_CONTACT_DETAILS);
    popupMenu.add(sendContactDetails);
    //sendContactDetails.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, accelaratorKeyMask));
    sendContactDetails.addActionListener(e -> JContactListPanel.this.sendContactDetailsToSelectedContact());
  }


  public void sendContactDetailsToSelectedContact() {
    try {
      MessagingIdentity id = this.list.getSelectedValue();

      if (id == null) {
        JOptionPane.showMessageDialog(
            this.parentFrame,
            LOCAL_MSG_NO_CONTACT_SEND_DETAIL,
            LOCAL_MSG_NO_CONTACT_SELECTED, JOptionPane.ERROR_MESSAGE);
        return;
      }

      if (id.isAnonymous()) {
        int reply = JOptionPane.showConfirmDialog(
            this.parentFrame,
            LOCAL_MSG_SEND_ANONYMOUS_CONTACT,
            LOCAL_MSG_SEND_OWN_ID_Q,
            JOptionPane.YES_NO_OPTION);

        if (reply == JOptionPane.NO_OPTION) {
          return;
        }
      }

      this.parent.sendIdentityMessageTo(id);

    } catch (Exception ioe) {
      Log.error("Unexpected error: ", ioe);
      JContactListPanel.this.errorReporter.reportError(ioe, false);
    }
  }


  public void reloadMessagingIdentities()
      throws IOException {
    list.setIdentities(this.messagingStorage.getContactIdentities(true));
    list.revalidate();
  }


  public int getNumberOfContacts() {
    return list.getModel().getSize();
  }


  // Null if nothing selected
  public MessagingIdentity getSelectedContact() {
    return this.list.getSelectedValue();
  }


  private static class ContactList
      extends JList<MessagingIdentity> {
    ImageIcon contactBlackIcon;
    ImageIcon contactGroupBlackIcon;
    JLabel renderer;

    public ContactList() {
      super();

      this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      URL iconUrl = this.getClass().getClassLoader().getResource("images/contact-black.png");
      contactBlackIcon = new ImageIcon(iconUrl);
      URL groupIconUrl = this.getClass().getClassLoader().getResource("images/contact-group-black.png");
      contactGroupBlackIcon = new ImageIcon(groupIconUrl);

      renderer = new JLabel();
      renderer.setOpaque(true);
    }


    public void setIdentities(List<MessagingIdentity> identities) {
      List<MessagingIdentity> localIdentities = new ArrayList<MessagingIdentity>();
      localIdentities.addAll(identities);

      Collections.sort(
          localIdentities,
          (o1, o2) -> {
            if (o1.isGroup() != o2.isGroup()) {
              return o1.isGroup() ? -1 : +1;
            } else {
              return o1.getDiplayString().toUpperCase().compareTo(
                  o2.getDiplayString().toUpperCase());
            }
          }
      );

      DefaultListModel<MessagingIdentity> newModel = new DefaultListModel<MessagingIdentity>();
      for (MessagingIdentity id : localIdentities) {
        newModel.addElement(id);
      }

      this.setModel(newModel);
    }


    @Override
    public ListCellRenderer<MessagingIdentity> getCellRenderer() {
      return (list, id, index, isSelected, cellHasFocus) -> {
        renderer.setText(id.getDiplayString());
        if (!id.isGroup()) {
          renderer.setIcon(contactBlackIcon);
        } else {
          renderer.setIcon(contactGroupBlackIcon);
        }

        if (isSelected) {
          renderer.setBackground(list.getSelectionBackground());
        } else {
          // TODO: list background issues on Linux - if used directly
          renderer.setBackground(new Color(list.getBackground().getRGB()));
        }

        return renderer;
      };
    }
  } // End private static class ContactList

} // End public class JContactListPanel
