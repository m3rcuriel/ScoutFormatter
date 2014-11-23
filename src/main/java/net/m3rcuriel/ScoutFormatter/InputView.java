package net.m3rcuriel.ScoutFormatter;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import org.json.*;

/**
 * Created by Lee Mracek on 11/22/2014.
 */
public class InputView extends JFrame {
    JTextArea increments, selectors, inputs;
    JTextField matchType = new JTextField("Autonomous", 30);
    public static void main (String[] args) {
        new InputView();
    }

    public InputView() {
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
        type.add(matchType);
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

    public void initButtonBox (Box buttonBox) {
        ButtonHandler handler = new ButtonHandler();
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

    public void saveJSON () {
        File json = new File(matchType.getText() + "_collection.json");
        JSONObject objects = new JSONObject();

        objects.put("increments", increments.getText().split("\n"));

        JSONArray selectorTags = new JSONArray();
        String[] selectorText = selectors.getText().split("\n");
        for (String text : selectorText) {
            selectorTags.put(text.split("\t"));
        }
        objects.put("selectors", selectorTags);
        objects.put("inputs", inputs.getText().split("\n"));

        try {
            PrintWriter printWriter = new PrintWriter(json);
            objects.write(printWriter);
            printWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }

    public class ButtonHandler implements ActionListener {
        @Override
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
        }
    }
}
