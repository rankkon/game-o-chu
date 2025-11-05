package view;

import java.awt.BorderLayout;
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
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton loginButton;
    private final AuthController authController;
    
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
        
        // Form panel using GridBagLayout để dễ căn chỉnh
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Tài khoản
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0; // label không giãn
        JLabel userLabel = new JLabel("Tài khoản:");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 28));
        formPanel.add(userLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.5; // field giãn toàn bộ cột
        usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 28));
        formPanel.add(usernameField, gbc);

        // Mật khẩu
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0; // label không giãn
        JLabel passLabel = new JLabel("Mật khẩu:");
        passLabel.setFont(new Font("Arial", Font.PLAIN, 28));
        formPanel.add(passLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.5; // field giãn toàn bộ cột
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 28));
        formPanel.add(passwordField, gbc);

        // Nút đăng nhập và đăng ký
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE; // không giãn theo chiều ngang
        gbc.anchor = GridBagConstraints.CENTER; // căn giữa

        JPanel buttonPanel = new JPanel(); // panel chứa 2 nút
        loginButton = new JButton("Đăng nhập");
        loginButton.setFont(new Font("Arial", Font.BOLD, 28)); // giảm font
        loginButton.addActionListener(e -> login());
        buttonPanel.add(loginButton);

        JButton registerButton = new JButton("Đăng ký");
        registerButton.setFont(new Font("Arial", Font.BOLD, 28));
        registerButton.addActionListener(e -> showRegisterDialog());
        buttonPanel.add(registerButton);

        formPanel.add(buttonPanel, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        add(mainPanel);
    }
    
    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            showError("Tài khoản và mật khẩu không được để trống!");
            return;
        }
        
        authController.login(username, password);
    }
    
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void showRegisterDialog() {
        // Tạo các trường cho dialog
        JTextField regUsernameField = new JTextField(20);
        JPasswordField regPasswordField = new JPasswordField(20);
        JPasswordField regConfirmPasswordField = new JPasswordField(20);

        // Tạo panel chứa các trường
        JPanel panel = new JPanel(new java.awt.GridLayout(3, 2, 10, 10));
        panel.add(new JLabel("Tài khoản:"));
        panel.add(regUsernameField);
        panel.add(new JLabel("Mật khẩu:"));
        panel.add(regPasswordField);
        panel.add(new JLabel("Xác nhận mật khẩu:"));
        panel.add(regConfirmPasswordField);

        // Hiển thị dialog
        int result = JOptionPane.showConfirmDialog(this, panel, "Đăng ký tài khoản",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String username = regUsernameField.getText().trim();
            String password = new String(regPasswordField.getPassword());
            String confirmPassword = new String(regConfirmPasswordField.getPassword());

            // Validate
            if (username.isEmpty() || password.isEmpty()) {
                showError("Tài khoản và mật khẩu không được để trống!");
                return;
            }
            if (!password.equals(confirmPassword)) {
                showError("Mật khẩu xác nhận không khớp!");
                return;
            }

            // Gọi AuthController để gửi request đăng ký
            authController.register(username, password);
        }
    }
}
