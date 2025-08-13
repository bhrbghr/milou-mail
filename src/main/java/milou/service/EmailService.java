package milou.service;
import milou.core.GenerateCode;
import milou.DAO.EmailDAO;
import milou.model.Email;
import milou.model.User;
import java.util.List;

public class EmailService {

    private static final EmailDAO emailDAO = new EmailDAO();

    public static Email sendEmail(User sender, String subject, String body, List<User> recipients) {
        if (sender == null) throw new IllegalArgumentException("Sender is no one");
        if (subject == null || subject.isBlank()) throw new IllegalArgumentException("Subject cannot be empty");
        if (body == null || body.isBlank()) throw new IllegalArgumentException("Body cannot be empty");
        if (recipients == null || recipients.isEmpty()) throw new IllegalArgumentException("No recipients");

        Email email = new Email(sender, subject, body);
        emailDAO.saveEmail(email);

        for (User recipient : recipients) {
            emailDAO.addRecipient(email.getId(), recipient.getId());
        }
        return email;
    }

    public static List<Email> showAllEmails(User viewer) {
        if (viewer == null) throw new IllegalArgumentException("Viewer is no one");
        return emailDAO.findAllForRecipient(viewer.getId());
    }

    public static List<Email> showUnreadEmails(User viewer) {
        if (viewer == null) throw new IllegalArgumentException("Viewer is no one");
        return emailDAO.findUnreadForRecipient(viewer.getId());
    }

    public static List<Email> showSentEmails(User viewer) {
        if (viewer == null) throw new IllegalArgumentException("Viewer is no one");
        return emailDAO.findSentBySender(viewer.getId());
    }

    public static void readEmail(User reader, Email email) {
        if (reader == null) throw new IllegalArgumentException("Reader is no one");

        boolean isSender = email.getSender().getId().equals(reader.getId());
        boolean isRecipient = emailDAO.findRecipients(email.getId()).stream()
                .anyMatch(u -> u.getId().equals(reader.getId()));

        if (!isSender && !isRecipient) {
            throw new IllegalArgumentException("You cannot read this email.");
        }

        if (emailDAO.getReadTime(reader.getId(), email.getId()) == null) {
            emailDAO.markAsRead(reader.getId(), email.getId());
        }
    }

    public static Email replyEmail(User sender, String code, String body) {
        Email original = findByCode(code);
        List<User> recipients = emailDAO.findRecipients(original.getId());
        recipients.add(original.getSender());
        recipients.remove(sender);
        return sendEmail(sender, "[Re] " + original.getSubject(), body, recipients);
    }

    public static Email forwardEmail(User sender, String code, List<User> recipients) {
        Email original = findByCode(code);
        return sendEmail(sender, "[Fw] " + original.getSubject(), original.getBody(), recipients);
    }

    public static Email findByCode(String code) {
        if (code == null || code.length() != 6) {
            throw new IllegalArgumentException("Code must be 6 characters");
        }
        return emailDAO.findAll().stream()
                .filter(email -> GenerateCode.convertToCode(email.getId()).equals(code.toLowerCase()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No email with code: " + code));
    }

    public static List<User> findRecipientsOfEmail(Email email) {
        return emailDAO.findRecipients(email.getId());
    }

    public static List<Email> searchEmails(User viewer, String keyword) {
        if (viewer == null) throw new IllegalArgumentException("Viewer is no one");
        if (keyword == null || keyword.isBlank()) return List.of();
        return emailDAO.searchEmails(viewer.getId(), keyword);
    }
    public static void deleteEmailForUser(User user, String code) {
        Email email = findByCode(code);

        boolean isRecipient = emailDAO.findRecipients(email.getId())
                .stream()
                .anyMatch(u -> u.getId().equals(user.getId()));

        if (!isRecipient) {
            throw new IllegalArgumentException("You can only delete emails from your own inbox.");
        }

        emailDAO.deleteEmailForRecipient(email.getId(), user.getId());
    }

}