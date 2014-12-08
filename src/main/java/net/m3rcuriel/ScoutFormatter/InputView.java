package net.m3rcuriel.ScoutFormatter;

import com.firebase.client.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Lee Mracek on 11/22/2014.
 */
public class InputView extends JFrame implements ItemListener{
    JTextArea increments, selectors, inputs;
    JComboBox<String> matchType = new JComboBox<String>();
    JComboBox<String> regionals = new JComboBox<String>();
    JButton typeManage = new JButton("Manage...");
    JButton regionalManage = new JButton("Manage...");
    Firebase firebase = new Firebase("https://scouting115.firebaseio.com/");

    private final ButtonHandler handler = new ButtonHandler();

    public static void main(String[] args) {
        new InputView();
    }

    public InputView() {
        firebase.authWithCustomToken("mYraYRVVX70xtLBwdFb0Xd8Cw3MXyHyLN5dRSPdb", new Firebase.AuthResultHandler() {
            public void onAuthenticated(AuthData authData) {

            }

            public void onAuthenticationError(FirebaseError firebaseError) {

            }
        });
        refreshView(firebase);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Scouting JSON Formatter");

        regionals.addItemListener(this);

        JPanel input = new JPanel();

        initPanel(input);

        this.add(input);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public void refreshView (final Firebase firebase) {
        Firebase regionalRef = firebase;
        final int index = regionals.getSelectedIndex() == -1 ? 0 : regionals.getSelectedIndex();
        regionals.removeAllItems();
        regionalRef.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> children = (Map<String, Object>) dataSnapshot.getValue();
                String[] regionalList = children.keySet().toArray(new String[(children.size())]);

                for (String item : regionalList)
                    regionals.addItem(item);
                refreshTypes(firebase);
                regionals.setSelectedIndex(index);
            }

            public void onCancelled(FirebaseError firebaseError) {
                firebaseError.toException().printStackTrace();
            }
        });
    }

    public void refreshTypes (Firebase firebase) {
        Firebase inputs = firebase.child(regionals.getSelectedItem() + "/inputs");
        inputs.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    Map<String, Object> children = (Map<String, Object>) dataSnapshot.getValue();
                    String[] matchTypes = children.keySet().toArray(new String[(children.size())]);
                    matchType.removeAllItems();
                    for (String item : matchTypes)
                        matchType.addItem(item);
                } catch (NullPointerException e) {
                    matchType.removeAllItems();
                } catch (ClassCastException e) {
                    matchType.removeAllItems();
                }
            }

            public void onCancelled(FirebaseError firebaseError) {
                firebaseError.toException().printStackTrace();
            }
        });
    }

    public JScrollPane initTextArea(JTextArea jt) {
        jt.setTabSize(4);
        JScrollPane scroll = new JScrollPane(jt);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    public void initPanel(JPanel input) {
        input.setLayout(new MigLayout("wrap 1"));

        increments = new JTextArea(5, 50);
        JScrollPane scrollIncrements = initTextArea(increments);
        selectors = new JTextArea(5, 50);
        JScrollPane scrollSelectors = initTextArea(selectors);
        inputs = new JTextArea(5, 50);
        JScrollPane scrollInputs = initTextArea(inputs);
        JPanel regional = new JPanel();
        regional.add(new JLabel("Regional:"));
        regional.add(regionals, "growx");
        regionalManage.addActionListener(handler);
        regional.add(regionalManage, "gapleft 10");
        JPanel type = new JPanel();
        type.add(new JLabel("Match Section:"));
        type.add(matchType, "growx");
        typeManage.addActionListener(handler);
        type.add(typeManage, "gapleft 10");
        input.add(regional, "center");
        input.add(type, "center");
        input.add(new JLabel("Increments: "));
        input.add(scrollIncrements, "gapleft 10");
        input.add(new JLabel("Selectors: "));
        input.add(scrollSelectors, "gapleft 10");
        input.add(new JLabel("Inputs: "));
        input.add(scrollInputs, "gapleft 10");

        Box buttonBox = Box.createHorizontalBox();

        initButtonBox(buttonBox);

        input.add(buttonBox, "gaptop 5, growx, gapleft 10, gapright 10");
    }

    public void initButtonBox(Box buttonBox) {
        JButton help = new JButton("Help");
        JButton cancel = new JButton("Cancel");
        JButton save = new JButton("Save");

        help.addActionListener(handler);
        cancel.addActionListener(handler);
        save.addActionListener(handler);
        buttonBox.add(help);
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.add(cancel);
        buttonBox.add(Box.createHorizontalStrut(30));
        buttonBox.add(save);
    }

    public void save() {
        final CountDownLatch done = new CountDownLatch(1);
        Firebase firebase = new Firebase("https://scouting115.firebaseio.com/");
        final AuthView authenticate = new AuthView(this, firebase);
        if (!authenticate.succeeded()) {
            System.out.println("canceled");
            return;
        }
        firebase = authenticate.getFirebase();
        Map<String, Object> inputMap = new HashMap<String, Object>();
        inputMap.put("inputs", inputs.getText().split("\n"));
        inputMap.put("increments", increments.getText().split("\n"));
        String[] selectorText = selectors.getText().split("\n");
        List<List<Object>> selectorTags = new ArrayList<List<Object>>();
        for (String text : selectorText) {
            selectorTags.add((List) Arrays.asList(text.split("\t")));
        }
        inputMap.put("selectors", selectorTags.toArray());
        firebase.child(regionals.getSelectedItem() + "/inputs/" + matchType.getSelectedItem()).setValue(inputMap, new Firebase.CompletionListener() {
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                System.out.println("done");
                done.countDown();
            }
        });
        try {
            done.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    private void manageFirebase(JButton event) {
        if(event == typeManage)
            new ManageView(firebase.child(regionals.getSelectedItem() + "/inputs"), this);
        if(event == regionalManage)
            new ManageView(firebase, this);
    }

    public void onComplete() {
        refreshView(firebase);
    }

    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED && e.getSource() == regionals) {
            refreshTypes(firebase);
        }
    }

    public class ButtonHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JButton source = (JButton) e.getSource();
            if (source.getText().equals("Cancel")) {
                System.exit(0);
            }
            if (source.getText().equals("Help")) {
                new HelpView();
            }
            if (source.getText().equals("Save")) {
                InputView.this.save();
            }
            if (source.getText().equals("Manage...")) {
                manageFirebase(source);
            }
        }
    }
}
