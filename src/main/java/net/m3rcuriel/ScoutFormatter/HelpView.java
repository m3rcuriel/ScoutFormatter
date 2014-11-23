package net.m3rcuriel.ScoutFormatter;

import javax.swing.*;
import java.awt.*;
import java.io.*;

/**
 * Created by Lee on 11/22/2014.
 */
public class HelpView extends JFrame {
    public HelpView() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel helpPane = new JPanel();
        String html = "";

        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File("help.html")));
            String line = reader.readLine();
            while (line != null) {
                html += line;
                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            //todo add alert
        } catch (IOException e) {
            e.printStackTrace();
        }

        JLabel text = new JLabel(html);

        text.setMaximumSize(new Dimension(240, 320));
        helpPane.add(text);

        this.add(helpPane);
        this.pack();
        this.setLocationRelativeTo(this.getParent());
        this.setVisible(true);
    }
}
