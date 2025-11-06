-- ===============================
--  TẠO DATABASE
-- ===============================
CREATE DATABASE IF NOT EXISTS ochu
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ochu;

-- ===============================
--  BẢNG NGƯỜI DÙNG (users)
-- ===============================
CREATE TABLE IF NOT EXISTS users (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    Username VARCHAR(50) COLLATE utf8mb4_unicode_ci NOT NULL UNIQUE,
    Password VARCHAR(255) COLLATE utf8mb4_unicode_ci NOT NULL,
    Avatar VARCHAR(255) COLLATE utf8mb4_unicode_ci DEFAULT '',
    Name VARCHAR(100) COLLATE utf8mb4_unicode_ci NOT NULL,
    Gender VARCHAR(10) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Nam',
    YearOfBirth INT(4) DEFAULT 2004,
    Score DOUBLE NOT NULL DEFAULT 1000,     
    MatchCount INT DEFAULT 0,
    WinCount INT DEFAULT 0,
    LoseCount INT DEFAULT 0,
    Blocked TINYINT(1) DEFAULT 0
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- ===============================
--  DỮ LIỆU NGƯỜI DÙNG MẪU
-- ===============================
INSERT INTO users (Username, Password, Avatar, Name, Gender, YearOfBirth)
VALUES 
('admin', 'admin123', 'admin.png', 'Administrator', 'Nam', 2004),
('viet1', '123456', 'icons8_alien_96px.png', 'Cao Đức Việt', 'Nam', 2004),
('trung1', '123456', 'icons8_alien_96px.png', 'Nguyễn Công Trung', 'Nam', 2004),
('han1', '123456', 'icons8_alien_96px.png', 'Đinh Ngọc Hân', 'Nam', 2004);

-- ===============================
--  BẢNG TRẬN ĐẤU (game_match)
-- ===============================
CREATE TABLE IF NOT EXISTS game_match (
    match_id INT AUTO_INCREMENT PRIMARY KEY,
    player1_id INT NOT NULL,
    player2_id INT NOT NULL,
    category_name VARCHAR(100) COLLATE utf8mb4_unicode_ci NOT NULL,   -- Chủ đề (VD: Hoa quả, Nghề nghiệp)
    words_json JSON NOT NULL,                                        -- Danh sách 5 từ (VD: ["táo","cam","xoài","ổi","bơ"])
    winner_id INT DEFAULT NULL,
    loser_id INT DEFAULT NULL,
    player1_score INT DEFAULT 0,
    player2_score INT DEFAULT 0,
    winner_time_remaining INT DEFAULT NULL,                          -- Thời gian còn lại nếu thắng nhanh
    start_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    end_time DATETIME DEFAULT NULL,
    chat_log MEDIUMTEXT COLLATE utf8mb4_unicode_ci,                  -- Lưu nội dung chat hoặc log
    FOREIGN KEY (player1_id) REFERENCES users(ID),
    FOREIGN KEY (player2_id) REFERENCES users(ID),
    FOREIGN KEY (winner_id) REFERENCES users(ID),
    FOREIGN KEY (loser_id) REFERENCES users(ID)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- ===============================
-- BẢNG CHỦ ĐỀ (CATEGORY)
-- ===============================
CREATE TABLE IF NOT EXISTS category (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_code VARCHAR(50) COLLATE utf8mb4_unicode_ci NOT NULL UNIQUE,  -- Mã không dấu (VD: HOAQUA)
    category_name VARCHAR(100) COLLATE utf8mb4_unicode_ci NOT NULL,         -- Tên có dấu (VD: Hoa quả)
    description VARCHAR(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- DỮ LIỆU CHỦ ĐỀ
INSERT INTO category (category_code, category_name, description)
VALUES
('HOAQUA', 'Hoa quả', 'Các loại trái cây thông dụng'),
('DONGVAT', 'Động vật', 'Tên các loài động vật'),
('NGHENGHIEP', 'Nghề nghiệp', 'Tên các nghề nghiệp phổ biến');

-- ===============================
-- BẢNG TỪ ĐIỂN (DICTIONARY)
-- ===============================
CREATE TABLE IF NOT EXISTS dictionary (
    word_id INT AUTO_INCREMENT PRIMARY KEY,
    word_code VARCHAR(100) COLLATE utf8mb4_unicode_ci NOT NULL UNIQUE,  -- dạng không dấu viết hoa (VD: TAO)
    word_text VARCHAR(100) COLLATE utf8mb4_unicode_ci NOT NULL,         -- dạng có dấu (VD: táo)
    category_id INT NOT NULL,
    FOREIGN KEY (category_id) REFERENCES category(category_id)
      ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- =====================================
-- HOA QUẢ (category_id = 1)
-- =====================================
INSERT INTO dictionary (word_code, word_text, category_id) VALUES
('TAO', 'táo', 1),
('CAM', 'cam', 1),
('CHUOI', 'chuối', 1),
('XOAI', 'xoài', 1),
('LE', 'lê', 1),
('MAN', 'mận', 1),
('MIT', 'mít', 1),
('DUA', 'dừa', 1),
('DUAHAU', 'dưa hấu', 1),
('DUALE', 'dưa lê', 1),
('DUAGANG', 'dưa gang', 1),
('NA', 'na', 1),
('MANGCUT', 'măng cụt', 1),
('BUOI', 'bưởi', 1),
('NHO', 'nho', 1),
('DAO', 'đào', 1),
('KHE', 'khế', 1),
('QUYT', 'quýt', 1),
('CHANH', 'chanh', 1),
('CHANHLEO', 'chanh leo', 1),
('LUU', 'lựu', 1),
('HONG', 'hồng', 1),
('SAURIENG', 'sầu riêng', 1),
('VAI', 'vải', 1),
('NHAN', 'nhãn', 1),
('MANGCAU', 'mãng cầu', 1),
('MANGCAUXIEM', 'mãng cầu xiêm', 1),
('DAUTAY', 'dâu tây', 1),
('VIETQUAT', 'việt quất', 1),
('KIWI', 'kiwi', 1),
('THANHLONG', 'thanh long', 1),
('SUNG', 'sung', 1),
('CACAO', 'ca cao', 1),
('ME', 'me', 1),
('BO', 'bơ', 1),
('CHOMCHOM', 'chôm chôm', 1),
('CHERRY', 'cherry', 1),
('MAMXOI', 'mâm xôi', 1),
('PHUCBONTU', 'phúc bồn tử', 1),
('OI', 'ổi', 1);

-- =====================================
-- NGHỀ NGHIỆP (category_id = 3)
-- =====================================
INSERT INTO dictionary (word_code, word_text, category_id) VALUES
('BACSI', 'bác sĩ', 3),
('YTA', 'y tá', 3),
('DUOCSI', 'dược sĩ', 3),
('GIAOVIEN', 'giáo viên', 3),
('SINHVIEN', 'sinh viên', 3),
('HOCSINH', 'học sinh', 3),
('KYSU', 'kỹ sư', 3),
('LAPTRINHVIEN', 'lập trình viên', 3),
('PHONGVIEN', 'phóng viên', 3),
('NHABAO', 'nhà báo', 3),
('PHATTHANHVIEN', 'phát thanh viên', 3),
('DIENVIEN', 'diễn viên', 3),
('DAODIEN', 'đạo diễn', 3),
('CASI', 'ca sĩ', 3),
('NHACSI', 'nhạc sĩ', 3),
('HOASI', 'hoạ sĩ', 3),
('KIENTRUCSU', 'kiến trúc sư', 3),
('PHICONG', 'phi công', 3),
('TIEPVIEN', 'tiếp viên', 3),
('TAIXE', 'tài xế', 3),
('GIAOHANG', 'giao hàng', 3),
('CANHSAT', 'cảnh sát', 3),
('BODOI', 'bộ đội', 3),
('LINHCUUHOA', 'lính cứu hoả', 3),
('BAOVE', 'bảo vệ', 3),
('LUATSU', 'luật sư', 3),
('KETOAN', 'kế toán', 3),
('GIAMDOC', 'giám đốc', 3),
('LETAN', 'lễ tân', 3),
('THUKY', 'thư ký', 3),
('THOMAY', 'thợ may', 3),
('THODIEN', 'thợ điện', 3),
('THOHO', 'thợ hồ', 3),
('THOSUAXE', 'thợ sửa xe', 3),
('THOCATTOC', 'thợ cắt tóc', 3),
('NONGDAN', 'nông dân', 3),
('THOMO', 'thợ mỏ', 3),
('KYTHUATVIEN', 'kỹ thuật viên', 3),
('BANHANG', 'nhân viên bán hàng', 3),
('VANDONGVIEN', 'vận động viên', 3),
('HUANLUYENVIEN', 'huấn luyện viên', 3),
('GAMETHU', 'game thủ', 3),
('STREAMER', 'streamer', 3),
('YOUTUBER', 'youtuber', 3),
('TIKTOKER', 'tiktoker', 3);

-- =====================================
-- ĐỘNG VẬT (category_id = 2)
-- =====================================
INSERT INTO dictionary (word_code, word_text, category_id) VALUES
('CHO', 'chó', 2),
('MEO', 'mèo', 2),
('CHIM', 'chim', 2),
('GA', 'gà', 2),
('VIT', 'vịt', 2),
('NGAN', 'ngan', 2),
('NGONG', 'ngỗng', 2),
('TRAU', 'trâu', 2),
('BO', 'bò', 2),
('DE', 'dê', 2),
('CUU', 'cừu', 2),
('LON', 'lợn', 2),
('NGUA', 'ngựa', 2),
('HO', 'hổ', 2),
('SUTU', 'sư tử', 2),
('BAO', 'báo', 2),
('CAO', 'cáo', 2),
('GAU', 'gấu', 2),
('GAUTRUC', 'gấu trúc', 2),
('KHI', 'khỉ', 2),
('VOI', 'voi', 2),
('TEGIAC', 'tê giác', 2),
('HAMA', 'hà mã', 2),
('HUOUCAOCO', 'hươu cao cổ', 2),
('HUOU', 'hươu', 2),
('NAI', 'nai', 2),
('CHOSOI', 'chó sói', 2),
('MEORUNG', 'mèo rừng', 2),
('RAN', 'rắn', 2),
('RANHOMANG', 'rắn hổ mang', 2),
('CAMAP', 'cá mập', 2),
('CAVOI', 'cá voi', 2),
('CAHEO', 'cá heo', 2),
('CANGUA', 'cá ngựa', 2),
('CATRE', 'cá trê', 2),
('CALOC', 'cá lóc', 2),
('CACHINH', 'cá chình', 2),
('CAHOI', 'cá hồi', 2),
('CANGU', 'cá ngừ', 2),
('CASAU', 'cá sấu', 2),
('TOM', 'tôm', 2),
('CUA', 'cua', 2),
('OC', 'ốc', 2),
('OCSEN', 'ốc sên', 2),
('NGAO', 'ngao', 2),
('SO', 'sò', 2),
('NGHEU', 'nghêu', 2),
('SUA', 'sứa', 2),
('BACHTUOC', 'bạch tuộc', 2),
('MUC', 'mực', 2),
('ONG', 'ong', 2),
('BUOM', 'bướm', 2),
('MUOI', 'muỗi', 2),
('KIEN', 'kiến', 2),
('GIAN', 'gián', 2),
('VESAU', 've sầu', 2),
('NHEN', 'nhện', 2),
('BOCANHCUNG', 'bọ cánh cứng', 2),
('BOHUNG', 'bọ hung', 2),
('BONGUA', 'bọ ngựa', 2),
('ECH', 'ếch', 2),
('NHAI', 'nhái', 2),
('COC', 'cóc', 2),
('BABA', 'ba ba', 2),
('RUA', 'rùa', 2),
('RUABIEN', 'rùa biển', 2),
('DADIEU', 'đà điểu', 2),
('CONG', 'công', 2),
('CHIMSE', 'chim sẻ', 2),
('CHIMBOCAU', 'chim bồ câu', 2),
('CHIMCANHCUT', 'chim cánh cụt', 2),
('CHIMHONGHAC', 'chim hồng hạc', 2),
('CHIMUNG', 'chim ưng', 2),
('CHIMHAIHAU', 'chim hải âu', 2),
('CHIMDAIBANG', 'đại bàng', 2),
('CHIMDIEUHAU', 'diều hâu', 2),
('CHAUCHAU', 'châu chấu', 2),
('DEMEN', 'dế mèn', 2);
