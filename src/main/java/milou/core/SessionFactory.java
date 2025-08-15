package milou.core;
import org.hibernate.cfg.Configuration;

public class SessionFactory {
    private static org.hibernate.SessionFactory sessionFactory = null;
    public static org.hibernate.SessionFactory get() {
        if (sessionFactory == null) {
            sessionFactory = new Configuration()
                    .configure("hibernate.cfg.xml")
                    .buildSessionFactory();

        }
        return sessionFactory;
    }
    public static void close() {
        if (sessionFactory == null) {
            return;
        }
        sessionFactory.close();
    }
}