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
    words_json JSON NOT NULL,                                        -- Danh sách 5 từ (VD: ["apple","orange","mango","guava","pear"])
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
    category_code VARCHAR(50) COLLATE utf8mb4_unicode_ci NOT NULL,  -- Mã không dấu (VD: HOAQUA)
    category_name VARCHAR(100) COLLATE utf8mb4_unicode_ci NOT NULL,         -- Tên có dấu (VD: Hoa quả)
    description VARCHAR(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- DỮ LIỆU CHỦ ĐỀ
INSERT INTO category (category_code, category_name, description)
VALUES
('HOAQUA', 'Fruits', 'Common fruits'),
('DONGVAT', 'Animals', 'Names of animals'),
('NGHENGHIEP', 'Occupations', 'Common job titles');

-- ===============================
-- BẢNG TỪ ĐIỂN (DICTIONARY)
-- ===============================
CREATE TABLE IF NOT EXISTS dictionary (
    word_id INT AUTO_INCREMENT PRIMARY KEY,
    word_code VARCHAR(100) COLLATE utf8mb4_unicode_ci NOT NULL,  -- dạng không dấu viết hoa (VD: APPLE)
    word_text VARCHAR(100) COLLATE utf8mb4_unicode_ci NOT NULL,         -- dạng có dấu (VD: apple)
    category_id INT NOT NULL,
    FOREIGN KEY (category_id) REFERENCES category(category_id)
      ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

-- =====================================
-- FRUITS (category_id = 1)
-- =====================================
INSERT INTO dictionary (word_code, word_text, category_id) VALUES
('APPLE', 'apple', 1),
('ORANGE', 'orange', 1),
('BANANA', 'banana', 1),
('MANGO', 'mango', 1),
('PEAR', 'pear', 1),
('PLUM', 'plum', 1),
('JACKFRUIT', 'jackfruit', 1),
('COCONUT', 'coconut', 1),
('WATERMELON', 'watermelon', 1),
('MELON', 'melon', 1),
('CANTALOUPE', 'cantaloupe', 1),
('MANGOSTEEN', 'mangosteen', 1),
('POMELO', 'pomelo', 1),
('GRAPE', 'grape', 1),
('PEACH', 'peach', 1),
('STARFRUIT', 'starfruit', 1),
('LEMON', 'lemon', 1),
('PASSIONFRUIT', 'passionfruit', 1),
('DURIAN', 'durian', 1),
('LYCHEE', 'lychee', 1),
('LONGAN', 'longan', 1),
('STRAWBERRY', 'strawberry', 1),
('BLUEBERRY', 'blueberry', 1),
('KIWI', 'kiwi', 1),
('DRAGONFRUIT', 'dragonfruit', 1),
('FIG', 'fig', 1),
('CACAO', 'cacao', 1),
('AVOCADO', 'avocado', 1),
('CHERRY', 'cherry', 1),
('RASPBERRY', 'raspberry', 1),
('GUAVA', 'guava', 1),
('LIME', 'lime', 1),
('APRICOT', 'apricot', 1),
('PAPAYA', 'papaya', 1),
('PINEAPPLE', 'pineapple', 1);

-- =====================================
-- OCCUPATIONS (category_id = 3)
-- =====================================
INSERT INTO dictionary (word_code, word_text, category_id) VALUES
('DOCTOR', 'doctor', 3),
('NURSE', 'nurse', 3),
('TEACHER', 'teacher', 3),
('STUDENT', 'student', 3),
('PUPIL', 'pupil', 3),
('ENGINEER', 'engineer', 3),
('PROGRAMMER', 'programmer', 3),
('REPORTER', 'reporter', 3),
('JOURNALIST', 'journalist', 3),
('ANNOUNCER', 'announcer', 3),
('ACTOR', 'actor', 3),
('DIRECTOR', 'director', 3),
('SINGER', 'singer', 3),
('MUSICIAN', 'musician', 3),
('ARTIST', 'artist', 3),
('ARCHITECT', 'architect', 3),
('PILOT', 'pilot', 3),
('ATTENDANT', 'attendant', 3),
('DRIVER', 'driver', 3),
('POLICE', 'police', 3),
('SOLDIER', 'soldier', 3),
('FIREFIGHTER', 'firefighter', 3),
('GUARD', 'guard', 3),
('LAWYER', 'lawyer', 3),
('ACCOUNTANT', 'accountant', 3),
('RECEPTIONIST', 'receptionist', 3),
('SECRETARY', 'secretary', 3),
('TAILOR', 'tailor', 3),
('ELECTRICIAN', 'electrician', 3),
('MECHANIC', 'mechanic', 3),
('BARBER', 'barber', 3),
('FARMER', 'farmer', 3),
('MINER', 'miner', 3),
('TECHNICIAN', 'technician', 3),
('ATHLETE', 'athlete', 3),
('COACH', 'coach', 3),
('GAMER', 'gamer', 3),
('STREAMER', 'streamer', 3),
('YOUTUBER', 'youtuber', 3),
('TIKTOKER', 'tiktoker', 3),
('CHEF', 'chef', 3),
('JUDGE', 'judge', 3),
('BAKER', 'baker', 3),
('DENTIST', 'dentist', 3),
('VET', 'vet', 3),
('ACTRESS', 'actress', 3),
('WAITER', 'waiter', 3),
('SCIENTIST', 'scientist', 3);

-- =====================================
-- ANIMALS (category_id = 2)
-- =====================================
INSERT INTO dictionary (word_code, word_text, category_id) VALUES
('DOG', 'dog', 2),
('CAT', 'cat', 2),
('BIRD', 'bird', 2),
('CHICKEN', 'chicken', 2),
('DUCK', 'duck', 2),
('GOOSE', 'goose', 2),
('BUFFALO', 'buffalo', 2),
('COW', 'cow', 2),
('GOAT', 'goat', 2),
('SHEEP', 'sheep', 2),
('PIG', 'pig', 2),
('HORSE', 'horse', 2),
('TIGER', 'tiger', 2),
('LION', 'lion', 2),
('LEOPARD', 'leopard', 2),
('FOX', 'fox', 2),
('BEAR', 'bear', 2),
('PANDA', 'panda', 2),
('MONKEY', 'monkey', 2),
('ELEPHANT', 'elephant', 2),
('RHINO', 'rhino', 2),
('HIPPO', 'hippo', 2),
('GIRAFFE', 'giraffe', 2),
('DEER', 'deer', 2),
('WOLF', 'wolf', 2),
('SNAKE', 'snake', 2),
('COBRA', 'cobra', 2),
('SHARK', 'shark', 2),
('WHALE', 'whale', 2),
('DOLPHIN', 'dolphin', 2),
('SEAHORSE', 'seahorse', 2),
('CATFISH', 'catfish', 2),
('EEL', 'eel', 2),
('SALMON', 'salmon', 2),
('TUNA', 'tuna', 2),
('CROCODILE', 'crocodile', 2),
('SHRIMP', 'shrimp', 2),
('CRAB', 'crab', 2),
('SNAIL', 'snail', 2),
('CLAM', 'clam', 2),
('JELLYFISH', 'jellyfish', 2),
('OCTOPUS', 'octopus', 2),
('SQUID', 'squid', 2),
('BEE', 'bee', 2),
('BUTTERFLY', 'butterfly', 2),
('MOSQUITO', 'mosquito', 2),
('ANT', 'ant', 2),
('ROACH', 'roach', 2),
('SPIDER', 'spider', 2),
('BEETLE', 'beetle', 2),
('MANTIS', 'mantis', 2),
('FROG', 'frog', 2),
('TOAD', 'toad', 2),
('TURTLE', 'turtle', 2),
('OSTRICH', 'ostrich', 2),
('PEACOCK', 'peacock', 2),
('SPARROW', 'sparrow', 2),
('DOVE', 'dove', 2),
('PENGUIN', 'penguin', 2),
('FLAMINGO', 'flamingo', 2),
('FALCON', 'falcon', 2),
('SEAGULL', 'seagull', 2),
('EAGLE', 'eagle', 2),
('HAWK', 'hawk', 2),
('CRICKET', 'cricket', 2),
('FISH', 'fish', 2),
('MOUSE', 'mouse', 2),
('RAT', 'rat', 2),
('BAT', 'bat', 2),
('OWL', 'owl', 2),
('SWAN', 'swan', 2),
('WORM', 'worm', 2),
('FLY', 'fly', 2),
('ALLIGATOR', 'alligator', 2),
('KANGAROO', 'kangaroo', 2),
('ZEBRA', 'zebra', 2),
('RABBIT', 'rabbit', 2);