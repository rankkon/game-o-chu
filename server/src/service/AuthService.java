package service;

import dao.UserDAO;
import model.User;

public class AuthService {
    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Xử lý đăng nhập.
     * @param username tên đăng nhập
     * @param password mật khẩu (plain text)
     * @return User nếu đăng nhập thành công, null nếu thất bại
     */
    public User login(String username, String password) {
        return userDAO.login(username, password);
    }
    
    /**
     * Register a new user
     * @param username username
     * @param password plain text password
     * @return true if successful, false otherwise
     */
    public boolean register(String username, String password, String fullName, int yearOfBirth) {
        return userDAO.register(username, password, fullName, yearOfBirth);
    }
}
