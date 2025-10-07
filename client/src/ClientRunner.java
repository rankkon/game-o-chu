import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import controller.AuthController;
import controller.SocketHandler;

public class ClientRunner {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 12345;
    
    public static void main(String[] args) {
        // Đảm bảo UI chạy trong EDT (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            try {
                // Thiết lập look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                // Khởi tạo socket handler
                SocketHandler socketHandler = new SocketHandler();
                
                // Kết nối tới server
                try {
                    System.out.println("Đang kết nối tới server: " + DEFAULT_HOST + ":" + DEFAULT_PORT);
                    socketHandler.connect(DEFAULT_HOST, DEFAULT_PORT);
                    System.out.println("Đã kết nối thành công!");
                    
                    // Khởi tạo AuthController và hiển thị màn hình đăng nhập
                    AuthController authController = new AuthController(socketHandler);
                    authController.showLogin();
                    
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, 
                        "Không thể kết nối tới server: " + e.getMessage(),
                        "Lỗi Kết Nối", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Lỗi khi khởi động ứng dụng: " + e.getMessage(),
                    "Lỗi Ứng Dụng", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}