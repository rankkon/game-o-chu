import controller.ServerMain;

/**
 * Lớp chính để khởi chạy Server Game Ô Chữ
 */
public class ServerRunner {
    public static void main(String[] args) {
        System.out.println("Khởi động Server Game Ô Chữ...");
        
        // Khởi tạo và chạy server
        ServerMain server = new ServerMain();
        server.start();
        
        // Đăng ký shutdown hook để đảm bảo server tắt đúng cách khi chương trình kết thúc
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Đang đóng server...");
            server.shutdown();
        }));
        
        System.out.println("Server đã sẵn sàng phục vụ kết nối từ client.");
    }
}
