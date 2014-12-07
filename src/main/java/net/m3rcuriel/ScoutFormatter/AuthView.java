package net.m3rcuriel.ScoutFormatter;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by lee on 12/6/14.
 */
public class AuthView extends JDialog implements ActionListener {

    JPanel panel = new JPanel(new MigLayout());
    JButton save = new JButton("Save");
    JButton cancel = new JButton("Cancel");
    JTextField email = new JTextField(20);
    JPasswordField password = new JPasswordField(20);

    private Firebase inputRef;
    private String emailText;
    private String passwordText;
    private boolean success = false;

    public AuthView(JFrame frame, Firebase inputRef) {
        super(frame, "Authentication", ModalityType.DOCUMENT_MODAL);

        this.inputRef = inputRef;

        this.setUndecorated(true);
        save.addActionListener(this);
        cancel.addActionListener(this);

        panel.add(new JLabel("Email:"));
        panel.add(email, "wrap");
        panel.add(new JLabel("Password:"));
        panel.add(password, "wrap");

        Box buttonBox = Box.createHorizontalBox();
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.add(cancel);
        buttonBox.add(Box.createHorizontalStrut(2));
        buttonBox.add(save);
        panel.add(buttonBox, "span 2, growx");
        this.add(panel);

        SwingUtilities.getRootPane(this).setDefaultButton(save);

        this.pack();

        this.setLocationRelativeTo(frame);
        this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == save) {
            if(email.getText().isEmpty() || String.valueOf(password.getPassword()).isEmpty()) {
                JOptionPane.showMessageDialog(this, "You must enter an email and password!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            inputRef.authWithPassword(email.getText(), String.valueOf(password.getPassword()), new Firebase.AuthResultHandler() {
                public void onAuthenticated(AuthData authData) {
                    success = true;
                    dispose();
                    System.out.println("Authentication successful");
                }

                public void onAuthenticationError(FirebaseError firebaseError) {
                    clear();
                    showAuthError();
                }
            });
        }
        if(e.getSource() == cancel) {
            this.dispose();
        }
    }

    public Firebase getFirebase() {
        return inputRef;
    }

    public void clear() {
        email.setText("");
        password.setText("");
    }

    public void showAuthError() {
        JOptionPane.showMessageDialog(this, "Authentication Error!", "Error", JOptionPane.ERROR_MESSAGE);
    }

    public boolean succeeded() {
        return success;
    }
}
