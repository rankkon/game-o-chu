CREATE DATABASE IF NOT EXISTS ochu;
USE ochu;

CREATE TABLE IF NOT EXISTS users (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    Username VARCHAR(50) COLLATE utf8_unicode_ci NOT NULL UNIQUE,
    Password VARCHAR(255) COLLATE utf8_unicode_ci NOT NULL,
    Avatar VARCHAR(255) COLLATE utf8_unicode_ci DEFAULT '',
    Name VARCHAR(100) COLLATE utf8_unicode_ci NOT NULL,
    Gender varchar(10) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'Nam',
    YearOfBirth INT(4) DEFAULT 2004,
    Score DOUBLE NOT NULL DEFAULT 1000,     
    MatchCount INT DEFAULT 0,
    WinCount INT DEFAULT 0,
    DrawCount INT DEFAULT 0,
    LoseCount INT DEFAULT 0,
    CurrentStreak INT DEFAULT 0,
    Rank INT NOT NULL DEFAULT -1,
    Blocked TINYINT(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO users (Username, Password, Avatar, Name, Gender, YearOfBirth, Rank)
VALUES 
('admin', 'admin123', 'admin.png', 'Administrator', 'Nam', 2004, 0),
('viet1', '123456', 'viet.png', 'Cao Duc Viet', 'Nam', 2004, 0),
('trung1', '123456', 'trung.png', 'Nguyen Cong Trung', 'Nam', 2004, 0),
('han1', '123456', 'han.png', 'Dinh Ngoc Han', 'Nam', 2004, 0);
