package view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import controller.AuthController;

public class LoginFrame extends JFrame {
    
    private final AuthController authController;
    
    // Sử dụng CardLayout để chuyển đổi
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainCardPanel;

    // Các trường cho panel Login
    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;

    // Các trường cho panel Register
    private JTextField regUsernameField;
    private JPasswordField regPasswordField;
    private JPasswordField regConfirmPasswordField;
    private JTextField regFullNameField;
    private JTextField regYearOfBirthField; 
    
    public LoginFrame(AuthController authController) {
        this.authController = authController;
        
        setTitle("Đăng nhập - Game Ô Chữ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(50, 100, 50, 100));
        
        // Title 
        JLabel titleLabel = new JLabel("GAME Ô CHỮ", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Panel chính chứa CardLayout
        mainCardPanel = new JPanel(cardLayout);
        mainCardPanel.add(createLoginPanel(), "LOGIN");
        mainCardPanel.add(createRegisterPanel(), "REGISTER");
        
        mainPanel.add(mainCardPanel, BorderLayout.CENTER);
        add(mainPanel);
        
        // Hiển thị panel login trước tiên
        cardLayout.show(mainCardPanel, "LOGIN");
    }
    
    /**
     * Tạo panel Đăng nhập
     */
    private JPanel createLoginPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Tài khoản
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        JLabel userLabel = new JLabel("Tài khoản:");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 28));
        formPanel.add(userLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.5;
        loginUsernameField = new JTextField();
        loginUsernameField.setFont(new Font("Arial", Font.PLAIN, 28));
        formPanel.add(loginUsernameField, gbc);

        // Mật khẩu
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        JLabel passLabel = new JLabel("Mật khẩu:");
        passLabel.setFont(new Font("Arial", Font.PLAIN, 28));
        formPanel.add(passLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.5;
        loginPasswordField = new JPasswordField();
        loginPasswordField.setFont(new Font("Arial", Font.PLAIN, 28));
        formPanel.add(loginPasswordField, gbc);

        // Nút đăng nhập
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel();

        JButton loginButton = new JButton("Đăng nhập");
        loginButton.setFont(new Font("Arial", Font.BOLD, 28));
        loginButton.addActionListener(e -> handleLogin());
        buttonPanel.add(loginButton);
        
        // Label để chuyển sang Đăng ký
        JButton showRegisterButton = new JButton("Đăng ký");
        showRegisterButton.setFont(new Font("Arial", Font.BOLD, 28));
        showRegisterButton.addActionListener(e -> cardLayout.show(mainCardPanel, "REGISTER"));
        buttonPanel.add(showRegisterButton);
        
        formPanel.add(buttonPanel, gbc);
        
        return formPanel;
    }

    /**
     * Tạo panel Đăng ký
     */
    private JPanel createRegisterPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20); // Giảm insets
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int y = 0;

        // Tài khoản
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0;
        JLabel userLabel = new JLabel("Tài khoản:");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        formPanel.add(userLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.5;
        regUsernameField = new JTextField();
        regUsernameField.setFont(new Font("Arial", Font.PLAIN, 24));
        formPanel.add(regUsernameField, gbc);
        y++;

        // Mật khẩu
        gbc.gridx = 0;
        gbc.gridy = y;
        JLabel passLabel = new JLabel("Mật khẩu:");
        passLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        formPanel.add(passLabel, gbc);

        gbc.gridx = 1;
        regPasswordField = new JPasswordField();
        regPasswordField.setFont(new Font("Arial", Font.PLAIN, 24));
        formPanel.add(regPasswordField, gbc);
        y++;

        // Xác nhận Mật khẩu
        gbc.gridx = 0;
        gbc.gridy = y;
        JLabel confirmPassLabel = new JLabel("Xác nhận MK:");
        confirmPassLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        formPanel.add(confirmPassLabel, gbc);

        gbc.gridx = 1;
        regConfirmPasswordField = new JPasswordField();
        regConfirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 24));
        formPanel.add(regConfirmPasswordField, gbc);
        y++;

        // Họ tên (Mới)
        gbc.gridx = 0;
        gbc.gridy = y;
        JLabel fullNameLabel = new JLabel("Họ và Tên:");
        fullNameLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        formPanel.add(fullNameLabel, gbc);

        gbc.gridx = 1;
        regFullNameField = new JTextField();
        regFullNameField.setFont(new Font("Arial", Font.PLAIN, 24));
        formPanel.add(regFullNameField, gbc);
        y++;

        // Năm sinh (Mới)
        gbc.gridx = 0;
        gbc.gridy = y;
        JLabel yearLabel = new JLabel("Năm sinh:");
        yearLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        formPanel.add(yearLabel, gbc);

        gbc.gridx = 1;
        regYearOfBirthField = new JTextField();
        regYearOfBirthField.setFont(new Font("Arial", Font.PLAIN, 24));
        formPanel.add(regYearOfBirthField, gbc);
        y++;

        // Nút Đăng ký
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel();

        JButton registerButton = new JButton("Hoàn tất Đăng ký");
        registerButton.setFont(new Font("Arial", Font.BOLD, 28));
        registerButton.addActionListener(e -> handleRegister());
        buttonPanel.add(registerButton);
        
        // Label để quay lại Đăng nhập
        JButton showLoginButton = new JButton("Quay lại");
        showLoginButton.setFont(new Font("Arial", Font.BOLD, 28));
        showLoginButton.addActionListener(e -> cardLayout.show(mainCardPanel, "LOGIN"));
        buttonPanel.add(showLoginButton); // Thêm nút Quay lại
        
        formPanel.add(buttonPanel, gbc);
        
        return formPanel;
    }
    
    /**
     * Xử lý logic khi nhấn nút Đăng nhập
     */
    private void handleLogin() {
        String username = loginUsernameField.getText().trim();
        String password = new String(loginPasswordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            showError("Tài khoản và mật khẩu không được để trống!");
            return;
        }
        
        authController.login(username, password);
    }

    /**
     * Xử lý logic khi nhấn nút Đăng ký
     */
    private void handleRegister() {
        // Lấy dữ liệu từ các trường đăng ký
        String username = regUsernameField.getText().trim();
        String password = new String(regPasswordField.getPassword());
        String confirmPassword = new String(regConfirmPasswordField.getPassword());
        String fullName = regFullNameField.getText().trim();
        String yearStr = regYearOfBirthField.getText().trim();

        // Validate
        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || yearStr.isEmpty()) {
            showError("Vui lòng nhập đầy đủ thông tin!");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError("Mật khẩu xác nhận không khớp!");
            return;
        }
        
        int yearOfBirth;
        try {
            yearOfBirth = Integer.parseInt(yearStr);
            if (yearOfBirth < 1900 || yearOfBirth > 2024) { // Giới hạn hợp lý
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            showError("Năm sinh không hợp lệ!");
            return;
        }

        // Gọi AuthController để gửi request đăng ký với các trường mới
        authController.register(username, password, fullName, yearOfBirth);
    }
    
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    // XÓA PHƯƠNG THỨC showRegisterDialog() CŨ
}