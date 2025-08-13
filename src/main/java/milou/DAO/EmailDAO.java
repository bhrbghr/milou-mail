package milou.DAO;
import milou.core.SessionFactory;
import milou.model.Email;
import milou.model.User;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class EmailDAO {

    public void saveEmail(Email email) {
        SessionFactory.get().inTransaction(session -> session.persist(email));
    }

    public void addRecipient(long emailId, long recipientId) {
        SessionFactory.get().inTransaction(session ->
                session.createNativeMutationQuery("""
                    insert into email_recipients(email_id, recipient_id)
                    values (:email_id, :recipient_id)
                """)
                        .setParameter("email_id", emailId)
                        .setParameter("recipient_id", recipientId)
                        .executeUpdate()
        );
    }

    public List<Email> findAllForRecipient(long recipientId) {
        return SessionFactory.get().fromTransaction(session ->
                session.createNativeQuery("""
                    select e.id, e.sender_id, e.subject, e.body, e.send_time
                    from emails e
                    join email_recipients er on er.email_id = e.id
                    where recipient_id = :recipient_id
                """, Email.class)
                        .setParameter("recipient_id", recipientId)
                        .getResultList()
        );
    }

    public List<Email> findUnreadForRecipient(long recipientId) {
        return SessionFactory.get().fromTransaction(session ->
                session.createNativeQuery("""
                    select e.id, e.sender_id, e.subject, e.body, e.send_time
                    from emails e
                    join email_recipients er on er.email_id = e.id
                    where recipient_id = :recipient_id and er.read_time is null
                """, Email.class)
                        .setParameter("recipient_id", recipientId)
                        .getResultList()
        );
    }

    public List<Email> findSentBySender(long senderId) {
        return SessionFactory.get().fromTransaction(session ->
                session.createNativeQuery("""
                    select * from emails e
                    where sender_id = :sender_id
                """, Email.class)
                        .setParameter("sender_id", senderId)
                        .getResultList()
        );
    }

    public Timestamp getReadTime(long readerId, long emailId) {
        List<Timestamp> times = SessionFactory.get().fromTransaction(session ->
                session.createNativeQuery("""
                    select read_time
                    from email_recipients
                    where recipient_id = :reader_id and email_id = :email_id
                """, Timestamp.class)
                        .setParameter("reader_id", readerId)
                        .setParameter("email_id", emailId)
                        .getResultList()
        );
        return times.isEmpty() ? null : times.get(0);
    }

    public void markAsRead(long readerId, long emailId) {
        SessionFactory.get().inTransaction(session ->
                session.createNativeMutationQuery("""
                    update email_recipients
                    set read_time = :now
                    where recipient_id = :reader_id and email_id = :email_id
                """)
                        .setParameter("now", Timestamp.valueOf(LocalDateTime.now()))
                        .setParameter("reader_id", readerId)
                        .setParameter("email_id", emailId)
                        .executeUpdate()
        );
    }

    public List<User> findRecipients(long emailId) {
        return SessionFactory.get().fromTransaction(session ->
                session.createNativeQuery("""
                    select u.id, u.name, u.email, u.password, u.signUp_time
                    from users u
                    join email_recipients er on er.recipient_id = u.id
                    where er.email_id = :email_id
                """, User.class)
                        .setParameter("email_id", emailId)
                        .getResultList()
        );
    }

    public List<Email> findAll() {
        return SessionFactory.get().fromTransaction(session ->
                session.createQuery("FROM Email", Email.class).getResultList()
        );
    }

    public List<Email> searchEmails(long viewerId, String keyword) {
        String likeKeyword = "%" + keyword.toLowerCase() + "%";
        return SessionFactory.get().fromTransaction(session ->
                session.createNativeQuery("""
                    select e.id, e.sender_id, e.subject, e.body, e.send_time
                    from emails e
                    join email_recipients er on er.email_id = e.id
                    where er.recipient_id = :recipient_id
                      and (lower(e.subject) like :kw or lower(e.body) like :kw)
                """, Email.class)
                        .setParameter("recipient_id", viewerId)
                        .setParameter("kw", likeKeyword)
                        .getResultList()
        );
    }
    public void deleteEmailForRecipient(long emailId, long recipientId) {
        SessionFactory.get().inTransaction(session ->
                session.createNativeMutationQuery("""
            delete from email_recipients
            where email_id = :email_id and recipient_id = :recipient_id
        """)
                        .setParameter("email_id", emailId)
                        .setParameter("recipient_id", recipientId)
                        .executeUpdate()
        );
    }


}