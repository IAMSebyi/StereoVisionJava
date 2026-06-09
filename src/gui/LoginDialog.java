package stereovision.gui;

import stereovision.model.UserAccount;
import stereovision.service.AuthService;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;

public final class LoginDialog {
    private LoginDialog() {
    }

    public static UserAccount authenticate(Component parent, AuthService authService) {
        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);

        while (true) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(createRow("Username", usernameField));
            panel.add(createRow("Password", passwordField));

            Object[] options = {"Login", "Register", "Cancel"};
            int option = JOptionPane.showOptionDialog(
                    parent,
                    panel,
                    "Stereo Vision Login",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (option == 2 || option == JOptionPane.CLOSED_OPTION) {
                return null;
            }

            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            try {
                if (option == 0) {
                    return authService.login(username, password);
                }
                UserAccount registeredUser = authService.register(username, password);
                JOptionPane.showMessageDialog(parent, "User created. You are now logged in as " + registeredUser.getUsername() + ".");
                return registeredUser;
            } catch (RuntimeException exception) {
                JOptionPane.showMessageDialog(parent, exception.getMessage(), "Authentication Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static JComponent createRow(String labelText, JComponent inputComponent) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.add(new JLabel(labelText), BorderLayout.WEST);
        row.add(inputComponent, BorderLayout.CENTER);
        return row;
    }
}
