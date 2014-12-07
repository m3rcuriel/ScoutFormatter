package net.m3rcuriel.ScoutFormatter;

import com.firebase.client.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import org.json.*;

/**
 * Created by Lee Mracek on 11/22/2014.
 */
public class InputView extends JFrame {
    JTextArea increments, selectors, inputs;
    JComboBox<String> matchType;
    String regional = "TES";
    private final ButtonHandler handler = new ButtonHandler();

    public static void main(String[] args) {
        new InputView();
    }

    public InputView() {
        Firebase firebase = new Firebase("https://scouting115.firebaseio.com/");
        firebase.authWithCustomToken("mYraYRVVX70xtLBwdFb0Xd8Cw3MXyHyLN5dRSPdb", new Firebase.AuthResultHandler() {
            public void onAuthenticated(AuthData authData) {

            }

            public void onAuthenticationError(FirebaseError firebaseError) {

            }
        });
        firebase = firebase.child(regional + "/inputs");
        matchType = new JComboBox<String>();
        firebase.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> types = new ArrayList<String>();
                Map<String, Object> children = (Map<String, Object>) dataSnapshot.getValue();
                String[] matchTypes = children.keySet().toArray(new String[(children.size())]);
                System.out.println(matchTypes[0]);
                for(String item : matchTypes)
                    matchType.addItem(item);
            }

            public void onCancelled(FirebaseError firebaseError) {
                firebaseError.toException().printStackTrace();
            }
        });
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Scouting JSON Formatter");

        JPanel input = new JPanel();

        initPanel(input);

        this.add(input);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
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
        JPanel type = new JPanel();
        type.add(new JLabel("Match Section:"));
        type.add(matchType, "growx");
        JButton manage = new JButton("Manage...");
        manage.addActionListener(handler);
        type.add(manage, "gapleft 10");
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

    public void saveJSON() {
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
        firebase.child("TES/inputs/" + matchType.getSelectedItem()).setValue(inputMap, new Firebase.CompletionListener() {
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
                InputView.this.saveJSON();
            }
            if (source.getText().equals("Manage")) {
                //todo make this actually do something
            }
        }
    }
}
