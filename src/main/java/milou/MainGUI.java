package milou;
import milou.model.Email;
import milou.model.User;
import milou.service.EmailService;
import milou.service.UserService;
import milou.core.GenerateCode;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainGUI extends JFrame {

    private User loggedInUser;

    public MainGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        showWelcomePage();
        setVisible(true);
    }

    private void showWelcomePage() {
        getContentPane().removeAll();
        setTitle("Milou Mail - Welcome");

        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");
        JButton quitBtn = new JButton("Quit");

        loginBtn.addActionListener(e -> loginUI());
        registerBtn.addActionListener(e -> registerUI());
        quitBtn.addActionListener(e -> System.exit(0));

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        panel.add(loginBtn);
        panel.add(registerBtn);
        panel.add(quitBtn);

        add(panel, BorderLayout.CENTER);
        revalidate();
        repaint();
        setVisible(true);
    }

    private void showMainPage() {
        getContentPane().removeAll();
        setTitle("Milou Mail - " + loggedInUser.getName());
        setSize(600, 400);

        try {
            List<Email> unreadEmails = EmailService.showUnreadEmails(loggedInUser);
            if (!unreadEmails.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append("You have ").append(unreadEmails.size()).append(" unread emails:\n");
                for (Email email : unreadEmails) {
                    sb.append("+ ").append(email.getSender().getEmail())
                            .append(" - ").append(email.getSubject())
                            .append(" (").append(GenerateCode.convertToCode(email.getId())).append(")\n");
                }
                JOptionPane.showMessageDialog(this, sb.toString(),
                        "Unread Emails", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading unread emails: " + e.getMessage());
        }

        JButton sendBtn = new JButton("Send Email");
        JButton inboxBtn = new JButton("View Inbox");
        JButton replyBtn = new JButton("Reply");
        JButton forwardBtn = new JButton("Forward");
        JButton searchBtn = new JButton("Search Emails");
        JButton readCodeBtn = new JButton("Read by Code");
        JButton deleteBtn = new JButton("Delete from Inbox");

        JButton logoutBtn = new JButton("Logout");

        sendBtn.addActionListener(e -> sendEmailUI(null, null, null, null));
        inboxBtn.addActionListener(e -> viewEmailsUI(loggedInUser));
        replyBtn.addActionListener(e -> replyEmailUI());
        forwardBtn.addActionListener(e -> forwardEmailUI());
        searchBtn.addActionListener(e -> searchEmailsUI());
        readCodeBtn.addActionListener(e -> readByCodeUI());
        deleteBtn.addActionListener(e -> deleteEmailUI());
        logoutBtn.addActionListener(e -> {
            loggedInUser = null;
            showWelcomePage();
            setSize(400, 300);
        });

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.add(sendBtn);
        panel.add(inboxBtn);
        panel.add(replyBtn);
        panel.add(forwardBtn);
        panel.add(searchBtn);
        panel.add(readCodeBtn);
        panel.add(deleteBtn);
        panel.add(logoutBtn);

        add(panel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void loginUI() {
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        Object[] fields = {
                "Email:", emailField,
                "Password:", passwordField
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Login", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                loggedInUser = UserService.loginUser(
                        completeEmail(emailField.getText()),
                        new String(passwordField.getPassword())
                );
                JOptionPane.showMessageDialog(
                        this,
                        "Welcome back, " + loggedInUser.getName() + "!",
                        "Login Successful",
                        JOptionPane.INFORMATION_MESSAGE
                );
                showMainPage();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void registerUI() {
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        Object[] fields = {
                "Name:", nameField,
                "Email:", emailField,
                "Password:", passwordField
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Register", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                UserService.registerUser(nameField.getText(), completeEmail(emailField.getText()), new String(passwordField.getPassword()));
                JOptionPane.showMessageDialog(this, "Your new account is created. \nGo ahead and login!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void sendEmailUI(List<User> preRecipients, String preSubject, String preBody, String originalCode) {
        JTextField recipientsField = new JTextField();
        if (preRecipients != null) {
            recipientsField.setText(String.join(", ", preRecipients.stream().map(User::getEmail).toList()));
        }

        JTextField subjectField = new JTextField(preSubject != null ? preSubject : "");
        JTextArea bodyArea = new JTextArea(preBody != null ? preBody : "", 5, 20);

        Object[] fields = {
                "Recipients (comma-separated):", recipientsField,
                "Subject:", subjectField,
                "Body:", new JScrollPane(bodyArea)
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Send Email", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String[] recipientsEmail = recipientsField.getText().split(",\\s*");
            ArrayList<String> wrongEmails = new ArrayList<>();
            ArrayList<User> existEmails = new ArrayList<>();

            for (String recipient : recipientsEmail) {
                User found = UserService.findByEmail(completeEmail(recipient.trim()));
                if (found == null) wrongEmails.add(recipient);
                else existEmails.add(found);
            }

            try {
                Email sentEmail;
                if (originalCode != null && !originalCode.isEmpty()) {
                    if (preBody != null && !preBody.isEmpty() && bodyArea.getText().equals(preBody)) {
                        sentEmail = EmailService.forwardEmail(loggedInUser, originalCode, existEmails);
                    } else {
                        sentEmail = EmailService.replyEmail(loggedInUser, originalCode, bodyArea.getText());
                    }
                } else {
                    sentEmail = EmailService.sendEmail(loggedInUser, subjectField.getText(), bodyArea.getText(), existEmails);
                }
                String msg;
                if (originalCode != null && !originalCode.isEmpty()) {
                    if (preBody != null && !preBody.isEmpty() && bodyArea.getText().equals(preBody)) {
                        msg = "Successfully forwarded your email.\nCode: " + GenerateCode.convertToCode(sentEmail.getId());
                    } else {
                        msg = "Successfully sent your reply to email " + originalCode + ".\nCode: " + GenerateCode.convertToCode(sentEmail.getId());
                    }
                } else {
                    msg = "Successfully sent your email.\nCode: " + GenerateCode.convertToCode(sentEmail.getId());
                }

                JOptionPane.showMessageDialog(this, msg);

                if (!wrongEmails.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Not sent to: " + wrongEmails);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void showEmailsUI(String title, List<Email> emails) {
        String[] columnNames = {"From", "Subject", "Code"};
        String[][] data = new String[emails.size()][3];

        for (int i = 0; i < emails.size(); i++) {
            Email email = emails.get(i);
            data[i][0] = email.getSender().getEmail();
            data[i][1] = email.getSubject();
            data[i][2] = GenerateCode.convertToCode(email.getId());
        }

        JTable table = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(table);

        JOptionPane.showMessageDialog(this, scrollPane, title + " (" + emails.size() + ")", JOptionPane.INFORMATION_MESSAGE);
    }

    private void viewEmailsUI(User user) {
        String[] options = {"All Emails", "Unread Emails", "Sent Emails"};
        int choice = JOptionPane.showOptionDialog(this,
                "Select emails to view:",
                "View Emails",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == JOptionPane.CLOSED_OPTION) return;

        List<Email> emails;
        try {
            switch (choice) {
                case 0 -> emails = EmailService.showAllEmails(user);
                case 1 -> emails = EmailService.showUnreadEmails(user);
                case 2 -> emails = EmailService.showSentEmails(user);
                default -> emails = List.of();
            }
            showEmailsUI(options[choice], emails);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchEmailsUI() {
        String keyword = JOptionPane.showInputDialog(this, "Enter keyword:");
        if (keyword != null && !keyword.isEmpty()) {
            try {
                List<Email> results = EmailService.searchEmails(loggedInUser, keyword);
                if (results.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No matching emails found.");
                } else {
                    showEmailsUI("Search Results", results);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void readByCodeUI() {
        String code = JOptionPane.showInputDialog(this, "Enter email code:");
        if (code != null && !code.isEmpty()) {
            try {
                Email email = EmailService.findByCode(code);

                if (!EmailService.canAccessEmail(loggedInUser, email)) {
                    JOptionPane.showMessageDialog(this, "You don't have access to this email.");
                    return;
                }

                EmailService.readEmail(loggedInUser, email);

                JTextArea contentArea = new JTextArea("From: " + email.getSender().getEmail() +
                        "\nSubject: " + email.getSubject() +
                        "\nDate: " + email.getSendTime() +
                        "\n\n" + email.getBody());
                contentArea.setEditable(false);

                JOptionPane.showMessageDialog(this, new JScrollPane(contentArea), "Email Content", JOptionPane.INFORMATION_MESSAGE);

            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this, "No email found for this code.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }



    private void replyEmailUI() {
        String code = JOptionPane.showInputDialog(this, "Enter email code to reply:");
        if (code != null && !code.isEmpty()) {
            try {
                Email email = EmailService.findByCode(code);

                if (!EmailService.canAccessEmail(loggedInUser, email)) {
                    JOptionPane.showMessageDialog(this, "You don't have access to this email.");
                    return;
                }

                sendEmailUI(List.of(email.getSender()), "Re: " + email.getSubject(), "", code);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Email not found for code: " + code);
            }
        }
    }



    private void forwardEmailUI() {
        String code = JOptionPane.showInputDialog(this, "Enter email code to forward:");
        if (code != null && !code.isEmpty()) {
            try {
                Email email = EmailService.findByCode(code);

                if (!EmailService.canAccessEmail(loggedInUser, email)) {
                    JOptionPane.showMessageDialog(this, "You don't have access to this email.");
                    return;
                }

                sendEmailUI(null, "Fwd: " + email.getSubject(), email.getBody(), code);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Email not found for code: " + code);
            }
        }
    }


    private void deleteEmailUI() {
        String code = JOptionPane.showInputDialog(this, "Enter email code to delete from inbox:");
        if (code != null && !code.isEmpty()) {
            try {
                EmailService.deleteEmailForUser(loggedInUser, code);
                JOptionPane.showMessageDialog(this, "Email removed from your inbox.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }


    private String completeEmail(String email) {
        if (!email.contains("@"))
            email += "@milou.com";
        return email;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainGUI::new);
    }
}