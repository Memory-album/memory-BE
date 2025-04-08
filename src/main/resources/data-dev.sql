-- 사용자 데이터
INSERT INTO users (id, email, password, name, created_at, updated_at) 
VALUES (1, 'test@example.com', '$2a$10$rDkPvvAFV6GgJzKYQ5wNYOc.0.0.0.0.0.0.0.0.0.0.0', '테스트 사용자', NOW(), NOW());

-- 질문 데이터
INSERT INTO questions (id, content, created_at, updated_at) 
VALUES 
(1, '가장 기억에 남는 여행은 언제인가요?', NOW(), NOW()),
(2, '어린 시절의 가장 행복했던 순간은 무엇인가요?', NOW(), NOW()),
(3, '가장 감동받은 책이나 영화는 무엇인가요?', NOW(), NOW()),
(4, '가족과 함께한 특별한 순간이 있다면 무엇인가요?', NOW(), NOW()),
(5, '지금까지의 인생에서 가장 큰 도전은 무엇이었나요?', NOW(), NOW());

-- 답변 데이터
INSERT INTO answers (id, question_id, user_id, content, is_private, created_at, updated_at)
VALUES 
(1, 1, 1, '제주도 여행이 가장 기억에 남습니다. 바다와 함께한 시간이 특별했어요.', false, NOW(), NOW()),
(2, 2, 1, '할머니 댁에서 보낸 여름 방학이 가장 행복했던 순간이에요.', false, NOW(), NOW()),
(3, 3, 1, '영화 "인터스텔라"가 가장 감동적이었습니다.', false, NOW(), NOW()); 