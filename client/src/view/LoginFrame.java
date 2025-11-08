package view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagConstraints; // Import lớp Theme
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import controller.AuthController;

public class LoginFrame extends JFrame {
    
    private final AuthController authController;
    
    // Sử dụng CardLayout để chuyển đổi
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainCardPanel;

    // Các trường cho panel Login
    // Thay đổi thành RoundedTextField/PasswordField
    private Theme.RoundedTextField loginUsernameField;
    private Theme.RoundedPasswordField loginPasswordField;

    // Các trường cho panel Register
    // Thay đổi thành RoundedTextField/PasswordField/ComboBox
    private Theme.RoundedTextField regUsernameField;
    private Theme.RoundedPasswordField regPasswordField;
    private Theme.RoundedPasswordField regConfirmPasswordField;
    private Theme.RoundedTextField regFullNameField;
    private Theme.RoundedTextField regYearOfBirthField;
    private Theme.RoundedComboBox<String> regGenderComboBox;
    
    public LoginFrame(AuthController authController) {
        this.authController = authController;
        
        setTitle("Đăng nhập - Game Ô Chữ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(50, 100, 50, 100));
        mainPanel.setBackground(Theme.COLOR_BACKGROUND); 
        
        // Title 
        JLabel titleLabel = new JLabel("GAME Ô CHỮ", JLabel.CENTER);
        titleLabel.setFont(Theme.FONT_TITLE); 
        titleLabel.setForeground(Theme.COLOR_TEXT_DARK);
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Panel chính chứa CardLayout
        mainCardPanel = new Theme.RoundedPanel(cardLayout); // Sử dụng RoundedPanel
        mainCardPanel.setBackground(Theme.COLOR_BACKGROUND); // Màu nền của panel
        mainCardPanel.setBorder(Theme.BORDER_ROUNDED_PANEL); // Bo tròn viền cho panel chính
        
        mainCardPanel.add(createLoginPanel(), "LOGIN");
        JPanel registerFormPanel = createRegisterPanel();
        JScrollPane registerScrollPane = new JScrollPane(registerFormPanel);
        registerScrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        registerScrollPane.getViewport().setBackground(Theme.COLOR_BACKGROUND); 
        registerScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainCardPanel.add(registerScrollPane, "REGISTER");
        
        mainPanel.add(mainCardPanel, BorderLayout.CENTER);
        add(mainPanel);
        
        // Hiển thị panel login trước tiên
        cardLayout.show(mainCardPanel, "LOGIN");
    }
    
    /**
     * Tạo panel Đăng nhập
     */
    private JPanel createLoginPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout()); // Vẫn dùng JPanel thường cho bố cục bên trong
        formPanel.setBackground(Theme.COLOR_BACKGROUND); 
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 20, 15, 20); // Giảm bớt insets
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Tài khoản
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        JLabel userLabel = new JLabel("Tài khoản:");
        userLabel.setFont(Theme.FONT_LABEL); 
        userLabel.setForeground(Theme.COLOR_TEXT_DARK);
        formPanel.add(userLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.5;
        loginUsernameField = new Theme.RoundedTextField(); // Dùng RoundedTextField
        loginUsernameField.setFont(Theme.FONT_INPUT); 
        loginUsernameField.setForeground(Theme.COLOR_TEXT_DARK);
        loginUsernameField.setBackground(Theme.COLOR_WHITE); // Nền trắng cho input
        loginUsernameField.addActionListener(e -> loginPasswordField.requestFocusInWindow());
        formPanel.add(loginUsernameField, gbc);

        // Mật khẩu
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        JLabel passLabel = new JLabel("Mật khẩu:");
        passLabel.setFont(Theme.FONT_LABEL); 
        passLabel.setForeground(Theme.COLOR_TEXT_DARK);
        formPanel.add(passLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.5;
        loginPasswordField = new Theme.RoundedPasswordField(); // Dùng RoundedPasswordField
        loginPasswordField.setFont(Theme.FONT_INPUT); 
        loginPasswordField.setForeground(Theme.COLOR_TEXT_DARK);
        loginPasswordField.setBackground(Theme.COLOR_WHITE); // Nền trắng cho input
        loginPasswordField.addActionListener(e -> handleLogin());
        formPanel.add(loginPasswordField, gbc);

        // Nút đăng nhập và đăng ký
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(25, 20, 15, 20); // Khoảng cách giữa input và nút

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Theme.COLOR_BACKGROUND); 

        JButton loginButton = new JButton("Đăng nhập");
        Theme.styleButtonPrimary(loginButton); 
        loginButton.addActionListener(e -> handleLogin());
        buttonPanel.add(loginButton);
        
        JButton showRegisterButton = new JButton("Đăng ký");
        // Giờ nút Đăng ký cũng là nút chính nhưng màu khác
        Theme.styleButtonPrimary(showRegisterButton); 
        showRegisterButton.setBackground(Theme.COLOR_ACCENT); // Đổi màu nền thành accent
        // Cần cập nhật lại UI sau khi đổi màu background
        showRegisterButton.setUI(new Theme.RoundedButtonUI()); 
        ((Theme.RoundedBorder)showRegisterButton.getBorder()).setColor(Theme.COLOR_ACCENT); // Cập nhật màu viền
        
        // Thêm hiệu ứng hover riêng cho nút đăng ký (nếu muốn màu khác khi hover)
        showRegisterButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                showRegisterButton.setBackground(Theme.COLOR_ACCENT.darker());
                showRegisterButton.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                showRegisterButton.setBackground(Theme.COLOR_ACCENT);
                showRegisterButton.repaint();
            }
        });
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
        formPanel.setBackground(Theme.COLOR_BACKGROUND); 
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 20, 8, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int y = 0;

        // --- Hàng 1: Tài khoản ---
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        JLabel userLabel = new JLabel("Tài khoản:");
        userLabel.setFont(Theme.FONT_LABEL);
        userLabel.setForeground(Theme.COLOR_TEXT_DARK);
        formPanel.add(userLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.5;
        regUsernameField = new Theme.RoundedTextField();
        regUsernameField.setFont(Theme.FONT_INPUT);
        regUsernameField.setForeground(Theme.COLOR_TEXT_DARK);
        regUsernameField.setBackground(Theme.COLOR_WHITE);
        regUsernameField.addActionListener(e -> regPasswordField.requestFocusInWindow());
        formPanel.add(regUsernameField, gbc);
        y++;

        // --- Hàng 2: Mật khẩu ---
        gbc.gridx = 0; gbc.gridy = y;
        JLabel passLabel = new JLabel("Mật khẩu:");
        passLabel.setFont(Theme.FONT_LABEL);
        passLabel.setForeground(Theme.COLOR_TEXT_DARK);
        formPanel.add(passLabel, gbc);

        gbc.gridx = 1;
        regPasswordField = new Theme.RoundedPasswordField(); // Dùng RoundedPasswordField
        regPasswordField.setFont(Theme.FONT_INPUT);
        regPasswordField.setForeground(Theme.COLOR_TEXT_DARK);
        regPasswordField.setBackground(Theme.COLOR_WHITE);
        regPasswordField.addActionListener(e -> regConfirmPasswordField.requestFocusInWindow());
        formPanel.add(regPasswordField, gbc);
        y++;

        // --- Hàng 3: Xác nhận Mật khẩu ---
        gbc.gridx = 0; gbc.gridy = y;
        JLabel confirmPassLabel = new JLabel("Xác nhận MK:");
        confirmPassLabel.setFont(Theme.FONT_LABEL);
        confirmPassLabel.setForeground(Theme.COLOR_TEXT_DARK);
        formPanel.add(confirmPassLabel, gbc);

        gbc.gridx = 1;
        regConfirmPasswordField = new Theme.RoundedPasswordField(); // Dùng RoundedPasswordField
        regConfirmPasswordField.setFont(Theme.FONT_INPUT);
        regConfirmPasswordField.setForeground(Theme.COLOR_TEXT_DARK);
        regConfirmPasswordField.setBackground(Theme.COLOR_WHITE);
        regConfirmPasswordField.addActionListener(e -> regFullNameField.requestFocusInWindow());
        formPanel.add(regConfirmPasswordField, gbc);
        y++;

        // --- Hàng 4: Họ tên ---
        gbc.gridx = 0; gbc.gridy = y;
        JLabel fullNameLabel = new JLabel("Họ và Tên:");
        fullNameLabel.setFont(Theme.FONT_LABEL);
        fullNameLabel.setForeground(Theme.COLOR_TEXT_DARK);
        formPanel.add(fullNameLabel, gbc);

        gbc.gridx = 1;
        regFullNameField = new Theme.RoundedTextField(); // Dùng RoundedTextField
        regFullNameField.setFont(Theme.FONT_INPUT);
        regFullNameField.setForeground(Theme.COLOR_TEXT_DARK);
        regFullNameField.setBackground(Theme.COLOR_WHITE);
        regFullNameField.addActionListener(e -> regYearOfBirthField.requestFocusInWindow());
        formPanel.add(regFullNameField, gbc);
        y++;

        // --- Hàng 5: Giới tính ---
        gbc.gridx = 0; gbc.gridy = y;
        JLabel genderLabel = new JLabel("Giới tính:");
        genderLabel.setFont(Theme.FONT_LABEL);
        genderLabel.setForeground(Theme.COLOR_TEXT_DARK);
        formPanel.add(genderLabel, gbc);

        gbc.gridx = 1;
        String[] genders = {"Nam", "Nữ", "Khác"};
        regGenderComboBox = new Theme.RoundedComboBox<>(genders); // Dùng RoundedComboBox
        regGenderComboBox.setFont(Theme.FONT_INPUT);
        regGenderComboBox.setForeground(Theme.COLOR_TEXT_DARK);
        regGenderComboBox.setBackground(Theme.COLOR_WHITE); 
        formPanel.add(regGenderComboBox, gbc);
        y++; 

        // --- Hàng 6: Năm sinh ---
        gbc.gridx = 0; gbc.gridy = y;
        JLabel yearLabel = new JLabel("Năm sinh:");
        yearLabel.setFont(Theme.FONT_LABEL);
        yearLabel.setForeground(Theme.COLOR_TEXT_DARK);
        formPanel.add(yearLabel, gbc);

        gbc.gridx = 1;
        regYearOfBirthField = new Theme.RoundedTextField(); // Dùng RoundedTextField
        regYearOfBirthField.setFont(Theme.FONT_INPUT);
        regYearOfBirthField.setForeground(Theme.COLOR_TEXT_DARK);
        regYearOfBirthField.setBackground(Theme.COLOR_WHITE);
        regYearOfBirthField.addActionListener(e -> handleRegister());
        formPanel.add(regYearOfBirthField, gbc);
        y++;

        // --- Hàng 7: Nút bấm ---
        gbc.gridx = 0; gbc.gridy = y;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 20, 8, 20);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Theme.COLOR_BACKGROUND);

        JButton registerButton = new JButton("Đăng ký");
        Theme.styleButtonPrimary(registerButton); 
        registerButton.addActionListener(e -> handleRegister());
        buttonPanel.add(registerButton);
        
        JButton showLoginButton = new JButton("Quay lại");
        Theme.styleButtonPrimary(showLoginButton);
        showLoginButton.setBackground(Theme.COLOR_ACCENT);
        showLoginButton.setUI(new Theme.RoundedButtonUI());
        ((Theme.RoundedBorder)showLoginButton.getBorder()).setColor(Theme.COLOR_ACCENT);
        showLoginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                showLoginButton.setBackground(Theme.COLOR_ACCENT.darker());
                showLoginButton.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                showLoginButton.setBackground(Theme.COLOR_ACCENT);
                showLoginButton.repaint();
            }
        });
        showLoginButton.addActionListener(e -> cardLayout.show(mainCardPanel, "LOGIN"));
        buttonPanel.add(showLoginButton);
        
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
        String gender = (String) regGenderComboBox.getSelectedItem();

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
            if (yearOfBirth < 1900 || yearOfBirth > 2024) { 
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            showError("Năm sinh không hợp lệ!");
            return;
        }

        // Gọi AuthController để gửi request đăng ký
        authController.register(username, password, fullName, yearOfBirth, gender);
        
        // Tự động chuyển về màn hình đăng nhập và xóa form
        cardLayout.show(mainCardPanel, "LOGIN");
        loginUsernameField.setText(username); // Điền sẵn tên đăng nhập
        loginPasswordField.setText("");
        
        regUsernameField.setText("");
        regPasswordField.setText("");
        regConfirmPasswordField.setText("");
        regFullNameField.setText("");
        regYearOfBirthField.setText("");
        regGenderComboBox.setSelectedIndex(0);
    }
    
    public void showError(String message) {
        JLabel label = new JLabel(message);
        label.setFont(Theme.FONT_INPUT); 
        JOptionPane.showMessageDialog(this, label, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}