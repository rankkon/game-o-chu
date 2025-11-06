package Client.view.asset;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ResizeImage {
    public static void main(String[] args) {
        try {
            // Đọc ảnh gốc từ file
            BufferedImage originalImage = ImageIO.read(new File("C:\\Users\\phucl\\Downloads\\ranking.png"));

            // Thay đổi kích thước ảnh về 30x30 pixel
            Image resizedImage = originalImage.getScaledInstance(30, 30, Image.SCALE_SMOOTH);

            // Tạo BufferedImage mới để lưu ảnh đã thay đổi kích thước
            BufferedImage outputImage = new BufferedImage(30, 30, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = outputImage.createGraphics();
            g2d.drawImage(resizedImage, 0, 0, null);
            g2d.dispose();

            // Lưu ảnh đã thay đổi kích thước ra file mới
            ImageIO.write(outputImage, "png", new File("C:\\Users\\phucl\\Downloads\\BTL_LTM\\src\\Client\\view\\asset\\icon_ranking.png"));

            System.out.println("Image resized successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
