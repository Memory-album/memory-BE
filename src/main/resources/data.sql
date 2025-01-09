INSERT INTO users (id, email, name, profile_img_url, password, email_verified, account_locked, login_attempts, status, created_at, updated_at)
VALUES
-- 시니어 케어 중심 가족
(1, 'father1@test.com', '김아버지', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', TRUE, FALSE, 0, 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 'mother1@test.com', '이어머니', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', TRUE, FALSE, 0, 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 'grandfather1@test.com', '김할아버지', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', TRUE, FALSE, 0, 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(4, 'grandmother1@test.com', '박할머니', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', TRUE, FALSE, 0, 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(5, 'son1@test.com', '김아들', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', TRUE, FALSE, 0, 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- 자녀 성장 중심 가족
(6, 'father2@test.com', '박아버지', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', TRUE, FALSE, 0, 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(7, 'mother2@test.com', '최어머니', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', TRUE, FALSE, 0, 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(8, 'daughter2@test.com', '박딸', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', TRUE, FALSE, 0, 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO album_groups (id, name, group_description, group_image_url, invite_code, invite_code_expiry_at, is_invite_code_active, created_at, updated_at)
VALUES
-- 시니어 케어 중심 그룹
(1, '김가네 가족', '할아버지, 할머니와 함께하는 우리 가족의 일상', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', 'FAM111', DATE_ADD(CURRENT_TIMESTAMP(), INTERVAL 7 DAY), TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- 자녀 성장 중심 그룹
(2, '박가네 가족', '아이들의 성장과 발달을 기록하는 공간', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', 'FAM222', DATE_ADD(CURRENT_TIMESTAMP(), INTERVAL 7 DAY), TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO user_groups (id, user_id, group_id, role, group_nickname, group_profile_img_url, notification_enabled, last_visit_at, sort_order, created_at, updated_at)
VALUES
-- 시니어 케어 중심 그룹 멤버
(1, 1, 1, 'OWNER', '큰아들', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', TRUE, CURRENT_TIMESTAMP(), 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 2, 1, 'MEMBER', '큰며느리', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', TRUE, CURRENT_TIMESTAMP(), 2, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 3, 1, 'SENIOR', '아버님', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', TRUE, CURRENT_TIMESTAMP(), 3, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(4, 4, 1, 'SENIOR', '어머님', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', TRUE, CURRENT_TIMESTAMP(), 4, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(5, 5, 1, 'MEMBER', '손주', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', TRUE, CURRENT_TIMESTAMP(), 5, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- 자녀 성장 중심 그룹 멤버
(6, 6, 2, 'OWNER', '아빠', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', TRUE, CURRENT_TIMESTAMP(), 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(7, 7, 2, 'MEMBER', '엄마', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', TRUE, CURRENT_TIMESTAMP(), 2, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(8, 8, 2, 'MEMBER', '첫째딸', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', TRUE, CURRENT_TIMESTAMP(), 3, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO albums (id, title, description, thumbnail_url, theme, user_id, group_id, visibility, created_at, updated_at)
VALUES
-- 시니어 케어 테마 앨범
(1, '할아버지 팔순잔치', '할아버지의 건강한 팔순을 기념하며', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', 'SENIOR_CARE', 1, 1, 'GROUP', DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 30 DAY), CURRENT_TIMESTAMP()),
(2, '실버복지관 나들이', '할아버지 할머니와 함께한 복지관 활동', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', 'SENIOR_CARE', 2, 1, 'GROUP', DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 60 DAY), CURRENT_TIMESTAMP()),
(3, '가족 건강검진', '온가족 정기 건강검진 기록', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', 'SENIOR_CARE', 1, 1, 'GROUP', DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 90 DAY), CURRENT_TIMESTAMP()),
-- 자녀 성장 테마 앨범
(4, '첫 학교생활', '초등학교 입학과 적응기', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', 'CHILD_GROWTH', 6, 2, 'GROUP', DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 45 DAY), CURRENT_TIMESTAMP()),
(5, '방과후 활동', '피아노와 미술 학원 활동 모음', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', 'CHILD_GROWTH', 7, 2, 'GROUP', DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 15 DAY), CURRENT_TIMESTAMP());

INSERT INTO media (id, album_id, uploaded_by, file_type, file_url, file_size, original_filename, created_at, updated_at)
VALUES
-- 시니어 케어 테마 미디어
(1, 1, 1, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', 1024, 'family_photo.jpg', DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 30 DAY), CURRENT_TIMESTAMP()),
(2, 1, 2, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', 1024, 'cake_photo.jpg', DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 30 DAY), CURRENT_TIMESTAMP()),
(3, 2, 2, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', 1024, 'taichi_photo.jpg', DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 60 DAY), CURRENT_TIMESTAMP()),
(4, 2, 3, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', 1024, 'singing_photo.jpg', DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 60 DAY), CURRENT_TIMESTAMP()),
(5, 3, 1, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', 1024, 'checkup_photo.jpg', DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 90 DAY), CURRENT_TIMESTAMP()),
(6, 3, 4, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', 1024, 'pressure_photo.jpg', DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 90 DAY), CURRENT_TIMESTAMP()),
-- 자녀 성장 테마 미디어
(7, 4, 6, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', 1024, 'school_photo.jpg', DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 45 DAY), CURRENT_TIMESTAMP()),
(8, 4, 7, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', 1024, 'class_photo.jpg', DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 45 DAY), CURRENT_TIMESTAMP()),
(9, 4, 8, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', 1024, 'lunch_photo.jpg', DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 45 DAY), CURRENT_TIMESTAMP()),
(10, 5, 7, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', 1024, 'piano_photo.jpg', DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 15 DAY), CURRENT_TIMESTAMP()),
(11, 5, 8, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', 1024, 'art_photo.jpg', DATE_SUB(CURRENT_TIMESTAMP(), INTERVAL 15 DAY), CURRENT_TIMESTAMP());