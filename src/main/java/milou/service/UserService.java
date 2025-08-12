package milou.service;


import milou.DAO.UserDAO;
import milou.model.User;

public class UserService {

    private static final UserDAO userDAO = new UserDAO();

    public static User findByEmail(String email) {
        return userDAO.findByEmail(email);
    }

    public static void registerUser(String name, String email, String password) {
        if (findByEmail(email) != null)
            throw new IllegalArgumentException("Such email already exists");

        if (email == null || email.isEmpty())
            throw new IllegalArgumentException("Email cannot be empty");

        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("Name cannot be empty");

        checkPassword(password);

        User user = new User(name, email, password);
        userDAO.save(user);
    }

    public static User loginUser(String email, String password) {
        checkPassword(password);

        if (email == null || email.isEmpty())
            throw new IllegalArgumentException("Email cannot be empty");

        User user = findByEmail(email);

        if (user == null)
            throw new IllegalArgumentException("No such email.\nPlease sign up first!");

        if (!user.getPassword().equals(password))
            throw new IllegalArgumentException("Wrong password");

        return user;
    }

    private static void checkPassword(String password) {
        if (password == null || password.isEmpty())
            throw new IllegalArgumentException("Password cannot be empty");
        if (password.length() < 8)
            throw new IllegalArgumentException("Short password");
    }
}