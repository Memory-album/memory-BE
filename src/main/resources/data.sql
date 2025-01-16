-- 비밀번호:  test1234
INSERT INTO users (id, email, name, profile_img_url, password, email_verified, account_locked, login_attempts, status, created_at, updated_at)
VALUES
-- 시니어 케어 중심 가족
(1, 'father1@test.com', '김아버지', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', '$2b$10$JOjRM/aFFVYmwTPcd2qNNuB13QNoC3Bbnp1vhOLRlauGme3DlkC0u', TRUE, FALSE, 0, 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 'mother1@test.com', '이어머니', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', '$2b$10$JOjRM/aFFVYmwTPcd2qNNuB13QNoC3Bbnp1vhOLRlauGme3DlkC0u', TRUE, FALSE, 0, 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 'grandfather1@test.com', '김할아버지', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', '$2b$10$JOjRM/aFFVYmwTPcd2qNNuB13QNoC3Bbnp1vhOLRlauGme3DlkC0u', TRUE, FALSE, 0, 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(4, 'grandmother1@test.com', '박할머니', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', '$2b$10$JOjRM/aFFVYmwTPcd2qNNuB13QNoC3Bbnp1vhOLRlauGme3DlkC0u', TRUE, FALSE, 0, 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(5, 'son1@test.com', '김아들', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', '$2b$10$JOjRM/aFFVYmwTPcd2qNNuB13QNoC3Bbnp1vhOLRlauGme3DlkC0u', TRUE, FALSE, 0, 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- 자녀 성장 중심 가족
(6, 'father2@test.com', '박아버지', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', '$2b$10$JOjRM/aFFVYmwTPcd2qNNuB13QNoC3Bbnp1vhOLRlauGme3DlkC0u', TRUE, FALSE, 0, 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(7, 'mother2@test.com', '최어머니', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', '$2b$10$JOjRM/aFFVYmwTPcd2qNNuB13QNoC3Bbnp1vhOLRlauGme3DlkC0u', TRUE, FALSE, 0, 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(8, 'daughter2@test.com', '박딸', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', '$2b$10$JOjRM/aFFVYmwTPcd2qNNuB13QNoC3Bbnp1vhOLRlauGme3DlkC0u', TRUE, FALSE, 0, 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO album_groups (id, name, group_description, group_image_url, invite_code, invite_code_expiry_at, is_invite_code_active, created_at, updated_at)
VALUES
-- 시니어 케어 중심 그룹
(1, '김가네 가족', '할아버지, 할머니와 함께하는 우리 가족의 일상', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', 'FAM111', DATEADD('DAY', 7, CURRENT_TIMESTAMP()), TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- 자녀 성장 중심 그룹
(2, '박가네 가족', '아이들의 성장과 발달을 기록하는 공간', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', 'FAM222', DATEADD('DAY', 7, CURRENT_TIMESTAMP()), TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

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
(1, '할아버지 팔순잔치', '할아버지의 건강한 팔순을 기념하며', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', 'SENIOR_CARE', 1, 1, 'GROUP', DATEADD('DAY', -30, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(2, '실버복지관 나들이', '할아버지 할머니와 함께한 복지관 활동', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', 'SENIOR_CARE', 2, 1, 'GROUP', DATEADD('DAY', -60, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(3, '가족 건강검진', '온가족 정기 건강검진 기록', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', 'SENIOR_CARE', 1, 1, 'GROUP', DATEADD('DAY', -90, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
-- 자녀 성장 테마 앨범
(4, '첫 학교생활', '초등학교 입학과 적응기', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', 'CHILD_GROWTH', 6, 2, 'GROUP', DATEADD('DAY', -45, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(5, '방과후 활동', '피아노와 미술 학원 활동 모음', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', 'CHILD_GROWTH', 7, 2, 'GROUP', DATEADD('DAY', -15, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP());

INSERT INTO media (id, album_id, uploaded_by, file_type, file_url, file_size, original_filename, created_at, updated_at)
VALUES
-- 시니어 케어 테마 미디어
(1, 1, 1, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', 1024, 'family_photo.jpg', DATEADD('DAY', -30, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(2, 1, 2, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', 1024, 'cake_photo.jpg', DATEADD('DAY', -30, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(3, 2, 2, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', 1024, 'taichi_photo.jpg', DATEADD('DAY', -60, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(4, 2, 3, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', 1024, 'singing_photo.jpg', DATEADD('DAY', -60, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(5, 3, 1, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', 1024, 'checkup_photo.jpg', DATEADD('DAY', -90, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(6, 3, 4, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', 1024, 'pressure_photo.jpg', DATEADD('DAY', -90, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
-- 자녀 성장 테마 미디어
(7, 4, 6, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', 1024, 'school_photo.jpg', DATEADD('DAY', -45, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(8, 4, 7, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', 1024, 'class_photo.jpg', DATEADD('DAY', -45, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(9, 4, 8, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', 1024, 'lunch_photo.jpg', DATEADD('DAY', -45, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(10, 5, 7, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', 1024, 'piano_photo.jpg', DATEADD('DAY', -15, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(11, 5, 8, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', 1024, 'art_photo.jpg', DATEADD('DAY', -15, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP());

-- 앨범 페이지 데이터 추가
INSERT INTO album_pages (id, album_id, page_number, layout_type, created_at, updated_at)
VALUES
(1, 1, 1, 'GRID_2X2', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 1, 2, 'GRID_3X3', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 2, 1, 'SINGLE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(4, 4, 1, 'GRID_2X2', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 키워드 데이터 추가
INSERT INTO keywords (id, name, category, created_at, updated_at)
VALUES
(1, '웃음', 'EMOTION', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, '가족', 'EVENT', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, '병원', 'PLACE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(4, '학교', 'PLACE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(5, '피아노', 'OBJECT', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 미디어-키워드 매핑 데이터 추가
INSERT INTO media_keywords (id, media_id, keyword_id, confidence_score, created_at, updated_at)
VALUES
(1, 1, 1, 0.95, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 1, 2, 0.90, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 5, 3, 0.85, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(4, 7, 4, 0.92, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(5, 10, 5, 0.88, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 질문 데이터 추가
INSERT INTO questions (id, media_id, content, theme, keywords_used, is_private, created_at, updated_at)
VALUES
(1, 1, '할아버지의 팔순잔치에서 가장 기억에 남는 순간은 무엇인가요?', 'SENIOR_CARE', '가족,웃음', FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 5, '정기 건강검진에서 특별히 신경 쓰신 부분이 있으셨나요?', 'SENIOR_CARE', '병원', FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 7, '첫 등교 날 아침에 어떤 기분이셨나요?', 'CHILD_STORY', '학교', FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(4, 10, '피아노를 처음 배우게 된 계기가 무엇인가요?', 'CHILD_STORY', '피아노', FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 답변 데이터 추가
INSERT INTO answers (id, question_id, created_by, final_story, voice_text, is_private, created_at, updated_at)
VALUES
(1, 1, 1, '할아버지께서 손주들과 함께 케이크 촛불을 끄시던 모습이 가장 기억에 남습니다. 모두가 환하게 웃으며 축하해드렸죠.', '할아버지께서 손주들과 함께...', FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 2, 3, '혈압 체크를 특히 신경 썼습니다. 작년보다 수치가 좋아져서 다행이었어요.', '혈압 체크를 특히...', FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 3, 8, '설렘과 긴장이 함께 있었어요. 새로운 친구들을 만날 생각에 가슴이 두근거렸죠.', '설렘과 긴장이...', FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 좋아요 데이터 추가
INSERT INTO likes (id, user_id, target_type, target_id, created_at, updated_at)
VALUES
(1, 2, 'ALBUM', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 3, 'MEDIA', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 4, 'MEDIA', 2, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(4, 7, 'ALBUM', 4, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(5, 8, 'MEDIA', 10, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 알림 데이터 추가
INSERT INTO notifications (id, user_id, title, content, notification_type, reference_id, is_read, created_at, updated_at)
VALUES
(1, 1, '새로운 스토리가 추가되었습니다', '할아버지의 팔순잔치 앨범에 새로운 스토리가 추가되었습니다.', 'NEW_STORY', 1, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 3, '그룹 초대', '김가네 가족 그룹에 초대되었습니다.', 'GROUP_INVITE', 1, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 6, '앨범 업데이트', '첫 학교생활 앨범이 업데이트되었습니다.', 'ALBUM_UPDATE', 4, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());
=======
-- 비밀번호 전부 test1234
INSERT INTO users (id, email, name, profile_img_url, password, email_verified, account_locked, login_attempts, status, created_at, updated_at)
VALUES
-- 시니어 케어 중심 가족
(1, 'father1@test.com', '김아버지', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', '$2b$10$JOjRM/aFFVYmwTPcd2qNNuB13QNoC3Bbnp1vhOLRlauGme3DlkC0u', TRUE, FALSE, 0, 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 'mother1@test.com', '이어머니', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', '$2b$10$JOjRM/aFFVYmwTPcd2qNNuB13QNoC3Bbnp1vhOLRlauGme3DlkC0u', TRUE, FALSE, 0, 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 'grandfather1@test.com', '김할아버지', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', '$2b$10$JOjRM/aFFVYmwTPcd2qNNuB13QNoC3Bbnp1vhOLRlauGme3DlkC0u', TRUE, FALSE, 0, 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(4, 'grandmother1@test.com', '박할머니', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', '$2b$10$JOjRM/aFFVYmwTPcd2qNNuB13QNoC3Bbnp1vhOLRlauGme3DlkC0u', TRUE, FALSE, 0, 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(5, 'son1@test.com', '김아들', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', '$2b$10$JOjRM/aFFVYmwTPcd2qNNuB13QNoC3Bbnp1vhOLRlauGme3DlkC0u', TRUE, FALSE, 0, 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- 자녀 성장 중심 가족
(6, 'father2@test.com', '박아버지', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', '$2b$10$JOjRM/aFFVYmwTPcd2qNNuB13QNoC3Bbnp1vhOLRlauGme3DlkC0u', TRUE, FALSE, 0, 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(7, 'mother2@test.com', '최어머니', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', '$2b$10$JOjRM/aFFVYmwTPcd2qNNuB13QNoC3Bbnp1vhOLRlauGme3DlkC0u', TRUE, FALSE, 0, 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(8, 'daughter2@test.com', '박딸', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', '$2b$10$JOjRM/aFFVYmwTPcd2qNNuB13QNoC3Bbnp1vhOLRlauGme3DlkC0u', TRUE, FALSE, 0, 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO album_groups (id, name, group_description, group_image_url, invite_code, invite_code_expiry_at, is_invite_code_active, created_at, updated_at)
VALUES
-- 시니어 케어 중심 그룹
(1, '김가네 가족', '할아버지, 할머니와 함께하는 우리 가족의 일상', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', 'FAM111', DATEADD('DAY', 7, CURRENT_TIMESTAMP()), TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- 자녀 성장 중심 그룹
(2, '박가네 가족', '아이들의 성장과 발달을 기록하는 공간', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', 'FAM222', DATEADD('DAY', 7, CURRENT_TIMESTAMP()), TRUE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

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
(1, '할아버지 팔순잔치', '할아버지의 건강한 팔순을 기념하며', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', 'SENIOR_CARE', 1, 1, 'GROUP', DATEADD('DAY', -30, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(2, '실버복지관 나들이', '할아버지 할머니와 함께한 복지관 활동', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', 'SENIOR_CARE', 2, 1, 'GROUP', DATEADD('DAY', -60, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(3, '가족 건강검진', '온가족 정기 건강검진 기록', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', 'SENIOR_CARE', 1, 1, 'GROUP', DATEADD('DAY', -90, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
-- 자녀 성장 테마 앨범
(4, '첫 학교생활', '초등학교 입학과 적응기', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', 'CHILD_GROWTH', 6, 2, 'GROUP', DATEADD('DAY', -45, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(5, '방과후 활동', '피아노와 미술 학원 활동 모음', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', 'CHILD_GROWTH', 7, 2, 'GROUP', DATEADD('DAY', -15, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP());

INSERT INTO media (id, album_id, uploaded_by, file_type, file_url, file_size, original_filename, created_at, updated_at)
VALUES
-- 시니어 케어 테마 미디어
(1, 1, 1, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', 1024, 'family_photo.jpg', DATEADD('DAY', -30, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(2, 1, 2, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', 1024, 'cake_photo.jpg', DATEADD('DAY', -30, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(3, 2, 2, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', 1024, 'taichi_photo.jpg', DATEADD('DAY', -60, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(4, 2, 3, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', 1024, 'singing_photo.jpg', DATEADD('DAY', -60, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(5, 3, 1, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', 1024, 'checkup_photo.jpg', DATEADD('DAY', -90, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(6, 3, 4, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', 1024, 'pressure_photo.jpg', DATEADD('DAY', -90, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
-- 자녀 성장 테마 미디어
(7, 4, 6, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', 1024, 'school_photo.jpg', DATEADD('DAY', -45, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(8, 4, 7, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', 1024, 'class_photo.jpg', DATEADD('DAY', -45, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(9, 4, 8, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', 1024, 'lunch_photo.jpg', DATEADD('DAY', -45, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(10, 5, 7, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/test.png', 1024, 'piano_photo.jpg', DATEADD('DAY', -15, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP()),
(11, 5, 8, 'IMAGE', 'https://min-i-album-storage.s3.ap-northeast-2.amazonaws.com/ping.png', 1024, 'art_photo.jpg', DATEADD('DAY', -15, CURRENT_TIMESTAMP()), CURRENT_TIMESTAMP());

-- 앨범 페이지 데이터 추가
INSERT INTO album_pages (id, album_id, page_number, layout_type, created_at, updated_at)
VALUES
(1, 1, 1, 'GRID_2X2', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 1, 2, 'GRID_3X3', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 2, 1, 'SINGLE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(4, 4, 1, 'GRID_2X2', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 키워드 데이터 추가
INSERT INTO keywords (id, name, category, created_at, updated_at)
VALUES
(1, '웃음', 'EMOTION', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, '가족', 'EVENT', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, '병원', 'PLACE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(4, '학교', 'PLACE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(5, '피아노', 'OBJECT', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 미디어-키워드 매핑 데이터 추가
INSERT INTO media_keywords (id, media_id, keyword_id, confidence_score, created_at, updated_at)
VALUES
(1, 1, 1, 0.95, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 1, 2, 0.90, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 5, 3, 0.85, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(4, 7, 4, 0.92, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(5, 10, 5, 0.88, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 질문 데이터 추가
INSERT INTO questions (id, media_id, content, theme, keywords_used, is_private, created_at, updated_at)
VALUES
(1, 1, '할아버지의 팔순잔치에서 가장 기억에 남는 순간은 무엇인가요?', 'SENIOR_CARE', '가족,웃음', FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 5, '정기 건강검진에서 특별히 신경 쓰신 부분이 있으셨나요?', 'SENIOR_CARE', '병원', FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 7, '첫 등교 날 아침에 어떤 기분이셨나요?', 'CHILD_STORY', '학교', FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(4, 10, '피아노를 처음 배우게 된 계기가 무엇인가요?', 'CHILD_STORY', '피아노', FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 답변 데이터 추가
INSERT INTO answers (id, question_id, created_by, final_story, voice_text, is_private, created_at, updated_at)
VALUES
(1, 1, 1, '할아버지께서 손주들과 함께 케이크 촛불을 끄시던 모습이 가장 기억에 남습니다. 모두가 환하게 웃으며 축하해드렸죠.', '할아버지께서 손주들과 함께...', FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 2, 3, '혈압 체크를 특히 신경 썼습니다. 작년보다 수치가 좋아져서 다행이었어요.', '혈압 체크를 특히...', FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 3, 8, '설렘과 긴장이 함께 있었어요. 새로운 친구들을 만날 생각에 가슴이 두근거렸죠.', '설렘과 긴장이...', FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 좋아요 데이터 추가
INSERT INTO likes (id, user_id, target_type, target_id, created_at, updated_at)
VALUES
(1, 2, 'ALBUM', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 3, 'MEDIA', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 4, 'MEDIA', 2, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(4, 7, 'ALBUM', 4, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(5, 8, 'MEDIA', 10, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 알림 데이터 추가
INSERT INTO notifications (id, user_id, title, content, notification_type, reference_id, is_read, created_at, updated_at)
VALUES
(1, 1, '새로운 스토리가 추가되었습니다', '할아버지의 팔순잔치 앨범에 새로운 스토리가 추가되었습니다.', 'NEW_STORY', 1, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(2, 3, '그룹 초대', '김가네 가족 그룹에 초대되었습니다.', 'GROUP_INVITE', 1, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, 6, '앨범 업데이트', '첫 학교생활 앨범이 업데이트되었습니다.', 'ALBUM_UPDATE', 4, FALSE, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

