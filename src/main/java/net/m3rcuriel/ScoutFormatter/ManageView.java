package net.m3rcuriel.ScoutFormatter;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.client.core.SyncTree;
import com.firebase.client.core.view.*;
import com.firebase.client.core.view.Event;
import com.sun.org.apache.xpath.internal.operations.Mod;
import net.miginfocom.swing.MigLayout;
import oracle.jrockit.jfr.JFR;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

/**
 * Created by lee on 12/7/14.
 */
public class ManageView extends JFrame implements ActionListener{
    List<String> children = new ArrayList<String>();
    JButton add = new JButton();
    JButton remove = new JButton();
    JButton save = new JButton("Save");
    JButton cancel = new JButton("Cancel");
    final InputView iv;
    Firebase firebase;
    JList<String> list;
    DefaultListModel<String> listModel = new DefaultListModel<String>();
    Map<String, Object> childMap;

    public ManageView (Firebase firebase, final InputView iv) {
        super("Manage Firebase");
        childMap = new HashMap<String, Object>();
        children.clear();
        this.firebase = firebase;
        this.iv = iv;
        this.firebase.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    childMap = (Map<String, Object>) dataSnapshot.getValue();
                    for (String item : childMap.keySet().toArray(new String[(childMap.size())]))
                        ManageView.this.children.add(item);
                } catch (NullPointerException e) {
                    children.clear();
                } catch (ClassCastException e) {
                    children.clear();
                }
                initDisplay(iv);
                SwingUtilities.getRootPane(ManageView.this).setDefaultButton(save);
            }

            public void onCancelled(FirebaseError firebaseError) {
                firebaseError.toException().printStackTrace();
            }
        });
    }

    public void initDisplay(JFrame frame) {
        list = new JList<String>(listModel);

        for(String child : children)
            listModel.addElement(child);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(-1);

        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(250, 80));

        JPanel panel = new JPanel(new MigLayout("wrap 2"));

        add.setMargin(new Insets(0, 0, 0, 0));
        add.setText("+");
        add.setMaximumSize(new Dimension(15, 15));
        add.addActionListener(this);
        remove.setMargin(new Insets(0, 0, 0, 0));
        remove.setText("-");
        remove.setMaximumSize(new Dimension(15, 15));
        remove.addActionListener(this);

        save.addActionListener(this);
        cancel.addActionListener(this);

        Box modifierBox = Box.createVerticalBox();
        modifierBox.add(add);
        modifierBox.add(Box.createVerticalStrut(5));
        modifierBox.add(remove);
        modifierBox.setMaximumSize(new Dimension(15, 80));

        Box bottomBox = Box.createHorizontalBox();
        bottomBox.add(Box.createHorizontalGlue());
        bottomBox.add(cancel);
        bottomBox.add(Box.createVerticalStrut(10));
        bottomBox.add(save);

        panel.add(listScroller);
        panel.add(modifierBox, "growy, wrap 15");
        panel.add(bottomBox, "growx, span 2");
        this.add(panel);
        this.pack();
        this.setLocationRelativeTo(frame);
        this.setVisible(true);
    }

    private void saveFirebase() {
        final AuthView authenticate = new AuthView(this.getContentPane(), firebase);
        if (!authenticate.succeeded()) {
            System.out.println("canceled");
            return;
        }
        firebase = authenticate.getFirebase();

        firebase.setValue(childMap.isEmpty() ? "" : childMap, new Firebase.CompletionListener() {
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                iv.onComplete();
                ManageView.this.dispose();
            }
        });
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == add) {
            String addValue = JOptionPane.showInputDialog(this, "", "Add new", JOptionPane.PLAIN_MESSAGE);
            if (addValue != null && !addValue.isEmpty()) {
                listModel.add(listModel.size(), addValue);
                try {
                    childMap.put(addValue, "");
                } catch (NullPointerException er) {
                    childMap = new HashMap<String, Object>();
                    childMap.put(addValue, "");
                }
                list.setSelectedIndex(listModel.size() - 1);
            }
        }
        if (e.getSource() == remove) {
            childMap.remove(list.getSelectedValue());
            listModel.remove(list.getSelectedIndex());
            list.setSelectedIndex(0);
        }
        if (e.getSource() == cancel) {
            this.dispose();
        }
        if (e.getSource() == save) {
            saveFirebase();
        }
    }
}
//todo add rename