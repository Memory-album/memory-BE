-- 테스트 사용자 추가 (비밀번호: test1234!)
INSERT INTO users (
    email, password, name, email_verified, status,
    created_at, updated_at, login_attempts, account_locked
) VALUES (
    'test@example.com',
    '$2a$10$yUhxGjxZHDD7hW2tY6v7k.UG/K.vSTnVYE4LYRqBCupY4Qq4TGFIG',  -- 'test1234!'
    '테스트유저',
    true,
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0,
    false
);

-- 테스트 그룹 추가
INSERT INTO album_groups (
    name, group_description, invite_code,
    invite_code_expiry_at, is_invite_code_active, group_image_url,
    created_at, updated_at
) VALUES
(
    '가족 그룹',
    '우리 가족 추억 공유방',
    'ABC123',
    DATEADD('DAY', 7, CURRENT_TIMESTAMP),
    true,
    'https://min-i-album-storage.s3.amazonaws.com/groups/1/image/default.png',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    '친구들 모임',
    '친구들과 함께하는 공간',
    'XYZ789',
    DATEADD('DAY', 7, CURRENT_TIMESTAMP),
    true,
    'https://min-i-album-storage.s3.amazonaws.com/groups/2/image/default.png',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 사용자-그룹 관계 추가
INSERT INTO user_groups (
    user_id, group_id, role, group_nickname,
    group_profile_img_url, notification_enabled,
    sort_order, last_visit_at,
    created_at, updated_at
) VALUES
(
    1, 1, 'OWNER', '아빠',
    'https://min-i-album-storage.s3.amazonaws.com/users/test/profile/default.png',
    true,
    1,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    1, 2, 'MEMBER', '테스트유저',
    'https://min-i-album-storage.s3.amazonaws.com/users/test/profile/default.png',
    true,
    2,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);