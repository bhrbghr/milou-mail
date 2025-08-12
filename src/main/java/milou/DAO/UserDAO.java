package milou.DAO;

import milou.core.SessionFactory;
import milou.model.User;

public class UserDAO {

    public User findByEmail(String email) {
        return SessionFactory.get()
                .fromTransaction(session ->
                        session.createNativeQuery("select * from users " +
                                        "where email = :email", User.class)
                                .setParameter("email", email)
                                .getResultStream()
                                .findFirst()
                                .orElse(null));
    }

    public void save(User user) {
        SessionFactory.get()
                .inTransaction(session -> session.persist(user));
    }
}