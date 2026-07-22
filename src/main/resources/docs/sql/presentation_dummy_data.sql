-- ============================================================================
-- 상상서가 발표용 더미 데이터
-- ============================================================================
-- 목적
--   1. 발표자 계정은 신규 사용자처럼 개인 도서/독서/소셜 데이터가 0건이다.
--   2. 공개 영역에는 6개 장르(모험, 가족, 우정, 성장, 자연, 판타지)의 책이 있다.
--   3. 메인/랭킹/친구서재/구독/관리자 화면을 시연할 배경 데이터가 있다.
--   4. 900000번대 ID만 사용하므로 기존 개발용 dummy_data.sql과 구분된다.
--
-- 주요 계정 (BCrypt 암호문은 모두 동일하며 평문은 password)
--   demo@sangsang.com   : 발표자 USER, PREMIUM_MONTHLY, 개인 콘텐츠 없음
--   author@sangsang.com : 대표 인기 작가 USER
--   admin@sangsang.com  : 관리자 ADMIN
--   backup@sangsang.com : AI 장애 시 결과를 보여줄 백업 작가 USER
--
-- 데이터 규모
--   회원 12명 / 공개 도서 12권(장르별 2권) / 페이지 48개 / 표지 12개
--   좋아요 50여 개 / 댓글 및 답글 / 팔로우 / 랭킹 / 독서 기록
--   결제 및 AI 사용량 / 알림 / 신고 및 관리자 처리 로그
--
-- 실행 전제
--   - Spring Boot가 한 번 실행되어 JPA 테이블이 생성된 MariaDB에서 실행한다.
--   - UTF-8(utf8mb4)로 저장 및 실행한다.
--   - 외부 표지는 picsum.photos의 고정 seed URL을 사용하므로 발표 전 표시 여부를 확인한다.
--   - 재실행 가능하며, 발표자 계정에 발표 중 생성된 개인 데이터도 함께 초기화한다.
-- ============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------------------------
-- 0. 재실행을 위한 발표 데이터 정리
-- ----------------------------------------------------------------------------
-- 발표 중 demo 계정이 생성한 도서가 900000번대가 아니어도 모두 제거한다.
DELETE FROM `admin_action_log` WHERE `report_id` IN (
    SELECT `id` FROM `report` WHERE `target_type` = 'BOOK' AND `target_id` IN (
        SELECT `id` FROM `book` WHERE `member_id` = 900001
    )
);
DELETE FROM `report` WHERE (`target_type` = 'BOOK' AND `target_id` IN (
    SELECT `id` FROM `book` WHERE `member_id` = 900001
)) OR `member_id` = 900001 OR `processed_by` = 900001;
DELETE FROM `comment` WHERE `book_id` IN (SELECT `id` FROM `book` WHERE `member_id` = 900001)
    OR `member_id` = 900001;
DELETE FROM `book_like` WHERE `book_id` IN (SELECT `id` FROM `book` WHERE `member_id` = 900001)
    OR `member_id` = 900001;
DELETE FROM `bookmark` WHERE `book_id` IN (SELECT `id` FROM `book` WHERE `member_id` = 900001)
    OR `member_id` = 900001;
DELETE FROM `book_review` WHERE `book_id` IN (SELECT `id` FROM `book` WHERE `member_id` = 900001)
    OR `member_id` = 900001;
DELETE FROM `reading_memo` WHERE `book_id` IN (SELECT `id` FROM `book` WHERE `member_id` = 900001)
    OR `member_id` = 900001;
DELETE FROM `reading_plan` WHERE `book_id` IN (SELECT `id` FROM `book` WHERE `member_id` = 900001)
    OR `member_id` = 900001;
DELETE FROM `my_reading` WHERE `book_id` IN (SELECT `id` FROM `book` WHERE `member_id` = 900001)
    OR `member_id` = 900001;
DELETE FROM `weekly_book_ranking` WHERE `book_id` IN (SELECT `id` FROM `book` WHERE `member_id` = 900001);
DELETE FROM `ai_generation_usage` WHERE `book_id` IN (SELECT `id` FROM `book` WHERE `member_id` = 900001)
    OR `member_id` = 900001;
DELETE FROM `book_tag` WHERE `book_id` IN (SELECT `id` FROM `book` WHERE `member_id` = 900001);
DELETE FROM `book_image` WHERE `book_id` IN (SELECT `id` FROM `book` WHERE `member_id` = 900001);
DELETE FROM `book_page` WHERE `book_id` IN (SELECT `id` FROM `book` WHERE `member_id` = 900001);
DELETE FROM `book` WHERE `member_id` = 900001;
DELETE FROM `author_follow` WHERE `follower_id` = 900001 OR `author_id` = 900001;
DELETE FROM `notification` WHERE `member_id` = 900001;

-- 고정 발표 데이터 ID 대역 정리(자식 테이블부터 삭제)
DELETE FROM `admin_action_log` WHERE `id` BETWEEN 900000 AND 999999;
DELETE FROM `weekly_book_ranking` WHERE `id` BETWEEN 900000 AND 999999;
DELETE FROM `book_review` WHERE `id` BETWEEN 900000 AND 999999;
DELETE FROM `my_reading` WHERE `id` BETWEEN 900000 AND 999999;
DELETE FROM `report` WHERE `id` BETWEEN 900000 AND 999999;
DELETE FROM `comment` WHERE `id` BETWEEN 900000 AND 999999;
DELETE FROM `author_follow` WHERE `id` BETWEEN 900000 AND 999999;
DELETE FROM `bookmark` WHERE `id` BETWEEN 900000 AND 999999;
DELETE FROM `book_like` WHERE `id` BETWEEN 900000 AND 999999;
DELETE FROM `reading_plan` WHERE `id` BETWEEN 900000 AND 999999;
DELETE FROM `reading_memo` WHERE `id` BETWEEN 900000 AND 999999;
DELETE FROM `book_tag` WHERE `id` BETWEEN 900000 AND 999999;
DELETE FROM `ai_generation_usage` WHERE `id` BETWEEN 900000 AND 999999;
DELETE FROM `book_image` WHERE `id` BETWEEN 900000 AND 999999;
DELETE FROM `book_page` WHERE `id` BETWEEN 900000 AND 999999;
DELETE FROM `book` WHERE `id` BETWEEN 900000 AND 999999;
DELETE FROM `notification` WHERE `id` BETWEEN 900000 AND 999999;
DELETE FROM `payment` WHERE `id` BETWEEN 900000 AND 999999;
DELETE FROM `guardian_consent` WHERE `id` BETWEEN 900000 AND 999999;
DELETE FROM `member` WHERE `id` BETWEEN 900000 AND 999999;

-- ----------------------------------------------------------------------------
-- 1. 회원: 발표자 1, 관리자 1, 작가 4, 독자 6
-- ----------------------------------------------------------------------------
INSERT INTO `member` (
    `id`, `version`, `email`, `password`, `birth_date`, `nickname`,
    `profile_image_url`, `introduction`, `status`, `role`, `free_trial_used`,
    `viewer_font_size`, `viewer_view_type`, `withdrawn_at`,
    `subscription_plan`, `subscription_start_at`, `subscription_end_at`,
    `subscription_auto_renew`, `daily_text_remaining`, `daily_image_remaining`,
    `last_token_reset_date`, `created_at`, `updated_at`, `auth_provider`, `oauth_provider_id`
) VALUES
(900001, 0, 'demo@sangsang.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '1995-05-05', '동화만드는하늘', NULL, '오늘 처음 나만의 동화를 만들어 봅니다.', 'ACTIVE', 'USER', 1, 'MEDIUM', 'FLIP', NULL, 'PREMIUM_MONTHLY', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 20 DAY), 0, 20, 20, CURDATE(), DATE_SUB(NOW(), INTERVAL 30 DAY), NOW(), 'LOCAL', NULL),
(900002, 0, 'author@sangsang.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '1988-03-12', '달빛작가', 'https://picsum.photos/seed/demo-profile-author/300/300', '아이와 어른이 함께 읽는 따뜻한 이야기를 씁니다.', 'ACTIVE', 'USER', 1, 'LARGE', 'FADE', NULL, 'PREMIUM_YEARLY', DATE_SUB(NOW(), INTERVAL 90 DAY), DATE_ADD(NOW(), INTERVAL 275 DAY), 1, 12, 8, CURDATE(), DATE_SUB(NOW(), INTERVAL 400 DAY), NOW(), 'LOCAL', NULL),
(900003, 0, 'admin@sangsang.com',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '1990-01-01', '상상서가관리자', NULL, '상상서가 운영 관리자입니다.', 'ACTIVE', 'ADMIN', 1, 'MEDIUM', 'FADE', NULL, 'FREE', NULL, NULL, 0, 0, 0, CURDATE(), DATE_SUB(NOW(), INTERVAL 500 DAY), NOW(), 'LOCAL', NULL),
(900004, 0, 'backup@sangsang.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '1992-08-21', '구름책방', 'https://picsum.photos/seed/demo-profile-backup/300/300', '발표용 장르별 완성 도서를 보관합니다.', 'ACTIVE', 'USER', 1, 'MEDIUM', 'FLIP', NULL, 'PREMIUM_MONTHLY', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 25 DAY), 1, 10, 10, CURDATE(), DATE_SUB(NOW(), INTERVAL 300 DAY), NOW(), 'LOCAL', NULL),
(900005, 0, 'forest@sangsang.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '1985-11-03', '숲속이야기꾼', 'https://picsum.photos/seed/demo-profile-forest/300/300', '자연과 생명을 사랑하는 이야기를 만듭니다.', 'ACTIVE', 'USER', 1, 'MEDIUM', 'FADE', NULL, 'PREMIUM_MONTHLY', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_ADD(NOW(), INTERVAL 15 DAY), 1, 6, 4, CURDATE(), DATE_SUB(NOW(), INTERVAL 250 DAY), NOW(), 'LOCAL', NULL),
(900006, 0, 'friend@sangsang.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '1991-07-14', '다정한연필', 'https://picsum.photos/seed/demo-profile-friend/300/300', '친구와 가족의 마음을 담아 씁니다.', 'ACTIVE', 'USER', 0, 'SMALL', 'FLIP', NULL, 'FREE', NULL, NULL, 0, 3, 1, CURDATE(), DATE_SUB(NOW(), INTERVAL 180 DAY), NOW(), 'LOCAL', NULL),
(900007, 0, 'reader1@sangsang.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '1993-02-10', '책읽는민준', NULL, '매일 아이와 한 권씩 읽어요.', 'ACTIVE', 'USER', 0, 'MEDIUM', 'FADE', NULL, 'FREE', NULL, NULL, 0, 5, 2, CURDATE(), DATE_SUB(NOW(), INTERVAL 120 DAY), NOW(), 'LOCAL', NULL),
(900008, 0, 'reader2@sangsang.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '1989-09-18', '별빛서재', NULL, '잠들기 전 그림책을 좋아합니다.', 'ACTIVE', 'USER', 1, 'LARGE', 'FLIP', NULL, 'PREMIUM_MONTHLY', DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_ADD(NOW(), INTERVAL 10 DAY), 1, 8, 5, CURDATE(), DATE_SUB(NOW(), INTERVAL 100 DAY), NOW(), 'LOCAL', NULL),
(900009, 0, 'reader3@sangsang.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '1994-06-25', '구름독자', NULL, NULL, 'ACTIVE', 'USER', 0, 'SMALL', 'FADE', NULL, 'FREE', NULL, NULL, 0, 4, 1, CURDATE(), DATE_SUB(NOW(), INTERVAL 90 DAY), NOW(), 'LOCAL', NULL),
(900010, 0, 'reader4@sangsang.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '1987-12-02', '행복한책장', NULL, NULL, 'ACTIVE', 'USER', 1, 'MEDIUM', 'FLIP', NULL, 'FREE', NULL, NULL, 0, 2, 0, CURDATE(), DATE_SUB(NOW(), INTERVAL 75 DAY), NOW(), 'LOCAL', NULL),
(900011, 0, 'reader5@sangsang.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '1996-04-09', '마음한페이지', NULL, NULL, 'ACTIVE', 'USER', 0, 'MEDIUM', 'FADE', NULL, 'FREE', NULL, NULL, 0, 5, 2, CURDATE(), DATE_SUB(NOW(), INTERVAL 60 DAY), NOW(), 'LOCAL', NULL),
(900012, 0, 'suspended@sangsang.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '1990-10-20', '정지된사용자', NULL, NULL, 'SUSPENDED', 'USER', 0, 'MEDIUM', 'FLIP', NULL, 'FREE', NULL, NULL, 0, 0, 0, CURDATE(), DATE_SUB(NOW(), INTERVAL 200 DAY), NOW(), 'LOCAL', NULL);

-- ----------------------------------------------------------------------------
-- 2. 공개 도서: 발표자(900001)의 소유 도서는 의도적으로 없다.
-- ----------------------------------------------------------------------------
INSERT INTO `book` (
    `id`, `member_id`, `book_type`, `creation_mode`, `author_age_group`, `reader_age_group`,
    `title`, `description`, `category`, `target_lang`, `style_code`, `cover_image_id`,
    `confirmed_settings`, `status`, `page_count`, `view_count`, `like_count`, `comment_count`,
    `week_view_count`, `week_like_count`, `created_at`, `updated_at`
) VALUES
(910001, 900002, 'FAIRY_TALE', 'GUIDED', 'ADULT', 'LOWER_ELEMENTARY', '달빛 고양이와 별의 문', '길을 잃은 달빛 고양이와 별을 찾아 떠나는 따뜻한 모험.', '동화', 'ko', 'WATERCOLOR', NULL, '{"genre":"모험","demo":true}', 'PUBLISHED', 4, 1820, 0, 0, 510, 0, DATE_SUB(NOW(), INTERVAL 25 DAY), NOW()),
(910002, 900004, 'FAIRY_TALE', 'CHOICE', 'ADULT', 'PRESCHOOL', '구름열차와 사라진 별', '구름열차의 마지막 별을 되찾기 위해 친구들이 힘을 합치는 이야기.', '동화', 'ko', 'CARTOON', NULL, '{"genre":"모험","backup":true}', 'PUBLISHED', 4, 920, 0, 0, 230, 0, DATE_SUB(NOW(), INTERVAL 12 DAY), NOW()),
(910003, 900006, 'FAIRY_TALE', 'ANSWER', 'ADULT', 'PRESCHOOL', '엄마에게만 보이는 정원', '서로의 마음을 이해하면서 다시 피어나는 가족의 비밀 정원.', '동화', 'ko', 'WATERCOLOR', NULL, '{"genre":"가족","demo":true}', 'PUBLISHED', 4, 1450, 0, 0, 380, 0, DATE_SUB(NOW(), INTERVAL 30 DAY), NOW()),
(910004, 900002, 'FAIRY_TALE', 'FREE', 'ADULT', 'LOWER_ELEMENTARY', '할아버지의 주머니 시계', '오래된 시계에 담긴 가족의 기억을 따라가는 이야기.', '동화', 'ko', 'PENCIL', NULL, '{"genre":"가족"}', 'PUBLISHED', 4, 680, 0, 0, 145, 0, DATE_SUB(NOW(), INTERVAL 18 DAY), NOW()),
(910005, 900006, 'FAIRY_TALE', 'GUIDED', 'ADULT', 'PRESCHOOL', '구름마을의 새 친구', '서로 다른 모습을 이해하며 진짜 친구가 되어 가는 이야기.', '동화', 'ko', 'CARTOON', NULL, '{"genre":"우정","demo":true}', 'PUBLISHED', 4, 1210, 0, 0, 340, 0, DATE_SUB(NOW(), INTERVAL 22 DAY), NOW()),
(910006, 900004, 'FAIRY_TALE', 'MIXED', 'ADULT', 'LOWER_ELEMENTARY', '무지개 다리의 약속', '다툰 두 친구가 무지개 다리를 함께 고치며 화해하는 이야기.', '동화', 'ko', 'WATERCOLOR', NULL, '{"genre":"우정"}', 'PUBLISHED', 4, 540, 0, 0, 120, 0, DATE_SUB(NOW(), INTERVAL 9 DAY), NOW()),
(910007, 900002, 'FAIRY_TALE', 'ANSWER', 'ADULT', 'LOWER_ELEMENTARY', '겁쟁이 사자의 첫 무대', '두려움을 인정하고 한 걸음 내딛는 어린 사자의 성장 이야기.', '동화', 'ko', 'CARTOON', NULL, '{"genre":"성장","demo":true}', 'PUBLISHED', 4, 1610, 0, 0, 470, 0, DATE_SUB(NOW(), INTERVAL 28 DAY), NOW()),
(910008, 900006, 'FAIRY_TALE', 'GUIDED', 'ADULT', 'PRESCHOOL', '느린 달팽이의 운동회', '남과 비교하지 않고 자신의 속도를 찾아가는 이야기.', '동화', 'ko', 'PENCIL', NULL, '{"genre":"성장"}', 'PUBLISHED', 4, 760, 0, 0, 185, 0, DATE_SUB(NOW(), INTERVAL 14 DAY), NOW()),
(910009, 900005, 'FAIRY_TALE', 'CHOICE', 'ADULT', 'LOWER_ELEMENTARY', '숲을 지키는 작은 다람쥐', '작은 실천들이 모여 숲을 되살리는 자연 이야기.', '동화', 'ko', 'WATERCOLOR', NULL, '{"genre":"자연","demo":true}', 'PUBLISHED', 4, 1340, 0, 0, 405, 0, DATE_SUB(NOW(), INTERVAL 20 DAY), NOW()),
(910010, 900005, 'FAIRY_TALE', 'FREE', 'ADULT', 'PRESCHOOL', '바다를 무서워한 작은 고래', '바다 친구들과 만나 자신의 집을 사랑하게 되는 작은 고래 이야기.', '동화', 'ko', 'CARTOON', NULL, '{"genre":"자연"}', 'PUBLISHED', 4, 830, 0, 0, 210, 0, DATE_SUB(NOW(), INTERVAL 16 DAY), NOW()),
(910011, 900004, 'FAIRY_TALE', 'MIXED', 'ADULT', 'LOWER_ELEMENTARY', '시간을 파는 마법 상점', '시간의 진짜 가치를 배우는 신비한 마법 상점 이야기.', '동화', 'ko', 'WATERCOLOR', NULL, '{"genre":"판타지","demo":true}', 'PUBLISHED', 4, 1740, 0, 0, 495, 0, DATE_SUB(NOW(), INTERVAL 24 DAY), NOW()),
(910012, 900002, 'FAIRY_TALE', 'GUIDED', 'ADULT', 'LOWER_ELEMENTARY', '숲속 시계탑의 비밀', '밤마다 거꾸로 흐르는 시계탑의 비밀을 푸는 판타지 이야기.', '동화', 'ko', 'PENCIL', NULL, '{"genre":"판타지"}', 'PUBLISHED', 4, 1010, 0, 0, 270, 0, DATE_SUB(NOW(), INTERVAL 11 DAY), NOW());

-- ----------------------------------------------------------------------------
-- 3. 페이지: 각 책 4페이지, 한국어/영어 뷰어 전환 시연 가능
-- ----------------------------------------------------------------------------
INSERT INTO `book_page` (`id`, `book_id`, `page_no`, `title`, `content_type`, `content_text_ko`, `content_text_en`, `image_url`, `created_at`, `updated_at`) VALUES
(920001,910001,1,'별빛 숲의 만남','PAGE','하늘이는 별빛 숲에서 길을 잃은 달빛 고양이를 만났어요.','Haneul met a lost moonlight cat in the starlit forest.','https://picsum.photos/seed/demo-910001-1/900/600',DATE_SUB(NOW(),INTERVAL 25 DAY),NOW()),
(920002,910001,2,'사라진 별 지도','PAGE','고양이의 목걸이에는 별의 문으로 가는 지도가 숨어 있었어요.','A map to the Star Gate was hidden in the cat''s necklace.','https://picsum.photos/seed/demo-910001-2/900/600',DATE_SUB(NOW(),INTERVAL 25 DAY),NOW()),
(920003,910001,3,'용기 있는 선택','PAGE','하늘이는 무서웠지만 고양이의 손을 꼭 잡고 어둠 속으로 걸어갔어요.','Haneul was afraid, but walked into the dark holding the cat''s paw.','https://picsum.photos/seed/demo-910001-3/900/600',DATE_SUB(NOW(),INTERVAL 25 DAY),NOW()),
(920004,910001,4,'다시 열린 별의 문','PAGE','두 친구의 용기가 별의 문을 열었고 숲에는 빛이 돌아왔답니다.','Their courage opened the Star Gate and light returned to the forest.','https://picsum.photos/seed/demo-910001-4/900/600',DATE_SUB(NOW(),INTERVAL 25 DAY),NOW()),
(920005,910002,1,'구름역의 아침','PAGE','구름열차가 출발하려는 순간 마지막 별이 사라졌어요.','The last star vanished just as the cloud train was about to leave.','https://picsum.photos/seed/demo-910002-1/900/600',DATE_SUB(NOW(),INTERVAL 12 DAY),NOW()),
(920006,910002,2,'별을 찾는 승객들','PAGE','토끼 기관사와 하늘이는 별이 남긴 반짝이는 가루를 따라갔어요.','The rabbit conductor and Haneul followed sparkling star dust.','https://picsum.photos/seed/demo-910002-2/900/600',DATE_SUB(NOW(),INTERVAL 12 DAY),NOW()),
(920007,910002,3,'바람 터널','PAGE','친구들은 서로를 붙잡으며 거센 바람 터널을 통과했어요.','The friends held onto one another through the windy tunnel.','https://picsum.photos/seed/demo-910002-3/900/600',DATE_SUB(NOW(),INTERVAL 12 DAY),NOW()),
(920008,910002,4,'별빛 출발','PAGE','마지막 별을 되찾은 구름열차는 환하게 빛나며 출발했답니다.','With the last star back, the cloud train departed in bright light.','https://picsum.photos/seed/demo-910002-4/900/600',DATE_SUB(NOW(),INTERVAL 12 DAY),NOW()),
(920009,910003,1,'닫힌 정원문','PAGE','수아는 웃음을 잃은 엄마 뒤로 작은 정원문을 발견했어요.','Sua found a tiny garden gate behind her mother.','https://picsum.photos/seed/demo-910003-1/900/600',DATE_SUB(NOW(),INTERVAL 30 DAY),NOW()),
(920010,910003,2,'마음의 꽃','PAGE','정원에는 가족이 함께 웃었던 기억마다 꽃이 피어 있었어요.','A flower bloomed for every happy family memory.','https://picsum.photos/seed/demo-910003-2/900/600',DATE_SUB(NOW(),INTERVAL 30 DAY),NOW()),
(920011,910003,3,'따뜻한 포옹','PAGE','수아가 엄마를 꼭 안아 주자 시들었던 꽃들이 고개를 들었어요.','The wilted flowers rose when Sua hugged her mother.','https://picsum.photos/seed/demo-910003-3/900/600',DATE_SUB(NOW(),INTERVAL 30 DAY),NOW()),
(920012,910003,4,'모두의 정원','PAGE','그날부터 비밀 정원은 온 가족에게 보이는 행복한 정원이 되었답니다.','The secret garden became a happy garden for the whole family.','https://picsum.photos/seed/demo-910003-4/900/600',DATE_SUB(NOW(),INTERVAL 30 DAY),NOW()),
(920013,910004,1,'멈춘 시계','PAGE','할아버지의 낡은 시계는 가족이 모이면 다시 움직였어요.','Grandfather''s old watch moved again whenever the family gathered.','https://picsum.photos/seed/demo-910004-1/900/600',DATE_SUB(NOW(),INTERVAL 18 DAY),NOW()),
(920014,910004,2,'사진 속 여행','PAGE','시곗바늘을 돌리자 오래된 가족사진 속으로 들어갔어요.','Turning the hands carried them into an old family photograph.','https://picsum.photos/seed/demo-910004-2/900/600',DATE_SUB(NOW(),INTERVAL 18 DAY),NOW()),
(920015,910004,3,'잊지 않은 약속','PAGE','가족은 서로의 이야기를 자주 들어 주기로 약속했어요.','The family promised to listen to one another more often.','https://picsum.photos/seed/demo-910004-3/900/600',DATE_SUB(NOW(),INTERVAL 18 DAY),NOW()),
(920016,910004,4,'다시 가는 시간','PAGE','따뜻한 웃음소리에 주머니 시계도 힘차게 움직였답니다.','The pocket watch ticked strongly to the sound of warm laughter.','https://picsum.photos/seed/demo-910004-4/900/600',DATE_SUB(NOW(),INTERVAL 18 DAY),NOW()),
(920017,910005,1,'낯선 전학생','PAGE','구름마을에 날개 모양이 다른 새 친구 모모가 왔어요.','Momo, whose wings looked different, came to Cloud Village.','https://picsum.photos/seed/demo-910005-1/900/600',DATE_SUB(NOW(),INTERVAL 22 DAY),NOW()),
(920018,910005,2,'혼자인 점심','PAGE','아무도 말을 걸지 않아 모모는 혼자 점심을 먹었어요.','Momo ate lunch alone because no one spoke to him.','https://picsum.photos/seed/demo-910005-2/900/600',DATE_SUB(NOW(),INTERVAL 22 DAY),NOW()),
(920019,910005,3,'먼저 건넨 인사','PAGE','두리가 먼저 다가가 함께 구름 공을 하자고 말했어요.','Duri invited Momo to play cloud ball together.','https://picsum.photos/seed/demo-910005-3/900/600',DATE_SUB(NOW(),INTERVAL 22 DAY),NOW()),
(920020,910005,4,'우리의 날개','PAGE','서로 다른 날개는 더 멋진 팀을 만들어 주었답니다.','Their different wings made them a wonderful team.','https://picsum.photos/seed/demo-910005-4/900/600',DATE_SUB(NOW(),INTERVAL 22 DAY),NOW()),
(920021,910006,1,'무너진 다리','PAGE','다툰 두 친구 앞에서 무지개 다리가 무너졌어요.','The rainbow bridge collapsed before the two quarreling friends.','https://picsum.photos/seed/demo-910006-1/900/600',DATE_SUB(NOW(),INTERVAL 9 DAY),NOW()),
(920022,910006,2,'서로 다른 생각','PAGE','한 친구는 빨간 돌을, 다른 친구는 파란 돌을 놓고 싶었어요.','One wanted red stones and the other wanted blue.','https://picsum.photos/seed/demo-910006-2/900/600',DATE_SUB(NOW(),INTERVAL 9 DAY),NOW()),
(920023,910006,3,'함께 만든 색','PAGE','두 색을 나란히 놓자 더 아름다운 무지개가 되었어요.','Side by side, the two colors made a prettier rainbow.','https://picsum.photos/seed/demo-910006-3/900/600',DATE_SUB(NOW(),INTERVAL 9 DAY),NOW()),
(920024,910006,4,'화해의 약속','PAGE','친구들은 서로의 생각을 먼저 들어 주기로 약속했답니다.','They promised to listen to each other first.','https://picsum.photos/seed/demo-910006-4/900/600',DATE_SUB(NOW(),INTERVAL 9 DAY),NOW()),
(920025,910007,1,'떨리는 첫 연습','PAGE','아기 사자 레오는 무대만 보면 다리가 떨렸어요.','Leo the lion cub trembled whenever he saw the stage.','https://picsum.photos/seed/demo-910007-1/900/600',DATE_SUB(NOW(),INTERVAL 28 DAY),NOW()),
(920026,910007,2,'작은 목소리','PAGE','레오는 빈 의자 하나를 관객이라 생각하고 노래했어요.','Leo sang to one empty chair as if it were his audience.','https://picsum.photos/seed/demo-910007-2/900/600',DATE_SUB(NOW(),INTERVAL 28 DAY),NOW()),
(920027,910007,3,'친구들의 박수','PAGE','친구들의 박수를 듣자 레오의 목소리가 조금씩 커졌어요.','His voice grew louder with his friends'' applause.','https://picsum.photos/seed/demo-910007-3/900/600',DATE_SUB(NOW(),INTERVAL 28 DAY),NOW()),
(920028,910007,4,'첫 번째 무대','PAGE','레오는 떨면서도 끝까지 노래했고 가장 환하게 웃었답니다.','Leo was nervous, but finished the song with a bright smile.','https://picsum.photos/seed/demo-910007-4/900/600',DATE_SUB(NOW(),INTERVAL 28 DAY),NOW()),
(920029,910008,1,'꼴찌 달팽이','PAGE','달팽이 토토는 늘 친구들보다 늦게 도착했어요.','Toto the snail always arrived after his friends.','https://picsum.photos/seed/demo-910008-1/900/600',DATE_SUB(NOW(),INTERVAL 14 DAY),NOW()),
(920030,910008,2,'토토가 본 것','PAGE','천천히 가는 토토는 길가의 다친 개미를 발견했어요.','Moving slowly, Toto noticed an injured ant.','https://picsum.photos/seed/demo-910008-2/900/600',DATE_SUB(NOW(),INTERVAL 14 DAY),NOW()),
(920031,910008,3,'함께 가는 길','PAGE','토토는 개미를 등에 태우고 자신의 속도로 걸었어요.','Toto carried the ant and continued at his own pace.','https://picsum.photos/seed/demo-910008-3/900/600',DATE_SUB(NOW(),INTERVAL 14 DAY),NOW()),
(920032,910008,4,'특별한 완주','PAGE','늦었지만 둘이 함께 들어온 토토에게 가장 큰 박수가 쏟아졌답니다.','Toto finished late, but received the biggest applause.','https://picsum.photos/seed/demo-910008-4/900/600',DATE_SUB(NOW(),INTERVAL 14 DAY),NOW()),
(920033,910009,1,'조용해진 숲','PAGE','어느 날 숲에서 새소리와 바람 소리가 사라졌어요.','One day, birdsong and wind disappeared from the forest.','https://picsum.photos/seed/demo-910009-1/900/600',DATE_SUB(NOW(),INTERVAL 20 DAY),NOW()),
(920034,910009,2,'작은 씨앗','PAGE','다람쥐 도토리는 친구들과 빈 땅에 씨앗을 심었어요.','Dotori and friends planted seeds in the empty ground.','https://picsum.photos/seed/demo-910009-2/900/600',DATE_SUB(NOW(),INTERVAL 20 DAY),NOW()),
(920035,910009,3,'모두의 실천','PAGE','동물들은 물을 아끼고 쓰레기를 하나씩 주웠어요.','The animals saved water and picked up litter.','https://picsum.photos/seed/demo-910009-3/900/600',DATE_SUB(NOW(),INTERVAL 20 DAY),NOW()),
(920036,910009,4,'돌아온 노래','PAGE','새싹이 자라자 숲에는 다시 아름다운 노래가 들렸답니다.','As sprouts grew, beautiful songs returned to the forest.','https://picsum.photos/seed/demo-910009-4/900/600',DATE_SUB(NOW(),INTERVAL 20 DAY),NOW()),
(920037,910010,1,'파도가 무서운 고래','PAGE','작은 고래 파랑이는 큰 파도만 보면 바위 뒤에 숨었어요.','Parang hid behind rocks whenever large waves came.','https://picsum.photos/seed/demo-910010-1/900/600',DATE_SUB(NOW(),INTERVAL 16 DAY),NOW()),
(920038,910010,2,'산호초 친구들','PAGE','산호초 친구들은 바다가 들려주는 노래를 알려 주었어요.','The coral reef friends taught Parang the song of the sea.','https://picsum.photos/seed/demo-910010-2/900/600',DATE_SUB(NOW(),INTERVAL 16 DAY),NOW()),
(920039,910010,3,'파도와 춤을','PAGE','파랑이는 친구들과 천천히 파도 위에서 춤을 추었어요.','Parang slowly danced on the waves with friends.','https://picsum.photos/seed/demo-910010-3/900/600',DATE_SUB(NOW(),INTERVAL 16 DAY),NOW()),
(920040,910010,4,'내가 사랑하는 바다','PAGE','파랑이는 바다가 무서운 곳이 아니라 자신의 따뜻한 집임을 알았답니다.','Parang learned the sea was not scary, but a warm home.','https://picsum.photos/seed/demo-910010-4/900/600',DATE_SUB(NOW(),INTERVAL 16 DAY),NOW()),
(920041,910011,1,'마법 상점의 문','PAGE','유나는 늦은 밤 시간을 파는 이상한 상점을 발견했어요.','Yuna found a strange shop that sold time.','https://picsum.photos/seed/demo-910011-1/900/600',DATE_SUB(NOW(),INTERVAL 24 DAY),NOW()),
(920042,910011,2,'한 시간짜리 병','PAGE','병을 열자 즐거운 한 시간이 눈 깜짝할 사이 지나가 버렸어요.','A happy hour passed in a blink when she opened the bottle.','https://picsum.photos/seed/demo-910011-2/900/600',DATE_SUB(NOW(),INTERVAL 24 DAY),NOW()),
(920043,910011,3,'살 수 없는 순간','PAGE','유나는 가족과 보내는 시간은 돈으로 살 수 없다는 걸 깨달았어요.','Yuna learned family time could not be bought.','https://picsum.photos/seed/demo-910011-3/900/600',DATE_SUB(NOW(),INTERVAL 24 DAY),NOW()),
(920044,910011,4,'오늘이라는 선물','PAGE','유나는 빈 병 대신 가족과 함께할 오늘을 꼭 안았답니다.','Yuna embraced today with her family instead of an empty bottle.','https://picsum.photos/seed/demo-910011-4/900/600',DATE_SUB(NOW(),INTERVAL 24 DAY),NOW()),
(920045,910012,1,'거꾸로 가는 시계','PAGE','밤 열두 시가 되자 숲속 시계탑의 시간이 거꾸로 흘렀어요.','At midnight, time in the forest clock tower ran backward.','https://picsum.photos/seed/demo-910012-1/900/600',DATE_SUB(NOW(),INTERVAL 11 DAY),NOW()),
(920046,910012,2,'멈춰 버린 숲','PAGE','어제에 갇힌 동물들은 같은 하루를 계속 반복했어요.','Animals trapped in yesterday repeated the same day.','https://picsum.photos/seed/demo-910012-2/900/600',DATE_SUB(NOW(),INTERVAL 11 DAY),NOW()),
(920047,910012,3,'내일을 여는 열쇠','PAGE','미르는 새로운 일을 해 볼 용기가 내일을 여는 열쇠임을 알았어요.','Mir learned courage to try something new was the key to tomorrow.','https://picsum.photos/seed/demo-910012-3/900/600',DATE_SUB(NOW(),INTERVAL 11 DAY),NOW()),
(920048,910012,4,'다시 흐르는 시간','PAGE','미르가 첫걸음을 내딛자 시계는 다시 앞으로 움직였답니다.','The clock moved forward when Mir took the first step.','https://picsum.photos/seed/demo-910012-4/900/600',DATE_SUB(NOW(),INTERVAL 11 DAY),NOW());

-- ----------------------------------------------------------------------------
-- 4. 표지 이미지와 대표 표지 연결
-- ----------------------------------------------------------------------------
INSERT INTO `book_image` (`id`, `book_id`, `book_page_id`, `image_type`, `image_order`, `file_url`, `file_name`, `file_extension`, `file_size`, `created_at`, `updated_at`, `deleted_at`) VALUES
(930001,910001,NULL,'COVER',1,'https://picsum.photos/seed/demo-cover-adventure-1/600/800','demo_cover_910001.jpg','jpg',350000,DATE_SUB(NOW(),INTERVAL 25 DAY),NOW(),NULL),
(930002,910002,NULL,'COVER',1,'https://picsum.photos/seed/demo-cover-adventure-2/600/800','demo_cover_910002.jpg','jpg',350000,DATE_SUB(NOW(),INTERVAL 12 DAY),NOW(),NULL),
(930003,910003,NULL,'COVER',1,'https://picsum.photos/seed/demo-cover-family-1/600/800','demo_cover_910003.jpg','jpg',350000,DATE_SUB(NOW(),INTERVAL 30 DAY),NOW(),NULL),
(930004,910004,NULL,'COVER',1,'https://picsum.photos/seed/demo-cover-family-2/600/800','demo_cover_910004.jpg','jpg',350000,DATE_SUB(NOW(),INTERVAL 18 DAY),NOW(),NULL),
(930005,910005,NULL,'COVER',1,'https://picsum.photos/seed/demo-cover-friend-1/600/800','demo_cover_910005.jpg','jpg',350000,DATE_SUB(NOW(),INTERVAL 22 DAY),NOW(),NULL),
(930006,910006,NULL,'COVER',1,'https://picsum.photos/seed/demo-cover-friend-2/600/800','demo_cover_910006.jpg','jpg',350000,DATE_SUB(NOW(),INTERVAL 9 DAY),NOW(),NULL),
(930007,910007,NULL,'COVER',1,'https://picsum.photos/seed/demo-cover-growth-1/600/800','demo_cover_910007.jpg','jpg',350000,DATE_SUB(NOW(),INTERVAL 28 DAY),NOW(),NULL),
(930008,910008,NULL,'COVER',1,'https://picsum.photos/seed/demo-cover-growth-2/600/800','demo_cover_910008.jpg','jpg',350000,DATE_SUB(NOW(),INTERVAL 14 DAY),NOW(),NULL),
(930009,910009,NULL,'COVER',1,'https://picsum.photos/seed/demo-cover-nature-1/600/800','demo_cover_910009.jpg','jpg',350000,DATE_SUB(NOW(),INTERVAL 20 DAY),NOW(),NULL),
(930010,910010,NULL,'COVER',1,'https://picsum.photos/seed/demo-cover-nature-2/600/800','demo_cover_910010.jpg','jpg',350000,DATE_SUB(NOW(),INTERVAL 16 DAY),NOW(),NULL),
(930011,910011,NULL,'COVER',1,'https://picsum.photos/seed/demo-cover-fantasy-1/600/800','demo_cover_910011.jpg','jpg',350000,DATE_SUB(NOW(),INTERVAL 24 DAY),NOW(),NULL),
(930012,910012,NULL,'COVER',1,'https://picsum.photos/seed/demo-cover-fantasy-2/600/800','demo_cover_910012.jpg','jpg',350000,DATE_SUB(NOW(),INTERVAL 11 DAY),NOW(),NULL);

UPDATE `book` SET `cover_image_id` = 930001 WHERE `id` = 910001;
UPDATE `book` SET `cover_image_id` = 930002 WHERE `id` = 910002;
UPDATE `book` SET `cover_image_id` = 930003 WHERE `id` = 910003;
UPDATE `book` SET `cover_image_id` = 930004 WHERE `id` = 910004;
UPDATE `book` SET `cover_image_id` = 930005 WHERE `id` = 910005;
UPDATE `book` SET `cover_image_id` = 930006 WHERE `id` = 910006;
UPDATE `book` SET `cover_image_id` = 930007 WHERE `id` = 910007;
UPDATE `book` SET `cover_image_id` = 930008 WHERE `id` = 910008;
UPDATE `book` SET `cover_image_id` = 930009 WHERE `id` = 910009;
UPDATE `book` SET `cover_image_id` = 930010 WHERE `id` = 910010;
UPDATE `book` SET `cover_image_id` = 930011 WHERE `id` = 910011;
UPDATE `book` SET `cover_image_id` = 930012 WHERE `id` = 910012;

-- 장르 태그 + 탐색용 보조 태그
INSERT INTO `book_tag` (`id`, `book_id`, `tag_name`) VALUES
(940001,910001,'모험'),(940002,910001,'용기'),
(940003,910002,'모험'),(940004,910002,'협동'),
(940005,910003,'가족'),(940006,910003,'사랑'),
(940007,910004,'가족'),(940008,910004,'추억'),
(940009,910005,'우정'),(940010,910005,'다양성'),
(940011,910006,'우정'),(940012,910006,'화해'),
(940013,910007,'성장'),(940014,910007,'자신감'),
(940015,910008,'성장'),(940016,910008,'배려'),
(940017,910009,'자연'),(940018,910009,'환경'),
(940019,910010,'자연'),(940020,910010,'바다'),
(940021,910011,'판타지'),(940022,910011,'시간'),
(940023,910012,'판타지'),(940024,910012,'마법');

-- ----------------------------------------------------------------------------
-- 5. 공개 서비스 활동: demo 계정은 어떤 활동에도 포함하지 않는다.
-- ----------------------------------------------------------------------------
-- 독자 5명이 책 12권 중 규칙에 맞는 책을 좋아하도록 생성(약 48건)
INSERT INTO `book_like` (`id`, `member_id`, `book_id`, `created_at`)
SELECT 950000 + ((m.member_id - 900006) * 20) + (b.book_id - 910000),
       m.member_id, b.book_id,
       DATE_SUB(NOW(), INTERVAL MOD(m.member_id + b.book_id, 7) DAY)
FROM (
    SELECT 900007 AS member_id UNION ALL SELECT 900008 UNION ALL SELECT 900009
    UNION ALL SELECT 900010 UNION ALL SELECT 900011
) m
CROSS JOIN (
    SELECT 910001 AS book_id UNION ALL SELECT 910002 UNION ALL SELECT 910003
    UNION ALL SELECT 910004 UNION ALL SELECT 910005 UNION ALL SELECT 910006
    UNION ALL SELECT 910007 UNION ALL SELECT 910008 UNION ALL SELECT 910009
    UNION ALL SELECT 910010 UNION ALL SELECT 910011 UNION ALL SELECT 910012
) b
WHERE MOD(m.member_id + b.book_id, 5) <> 0;

INSERT INTO `comment` (`id`, `book_id`, `member_id`, `reply_to_id`, `content`, `is_deleted`, `created_at`, `updated_at`) VALUES
(960001,910001,900007,NULL,'아이와 함께 재미있게 읽었어요. 다음 모험도 기대됩니다!',0,DATE_SUB(NOW(),INTERVAL 2 DAY),DATE_SUB(NOW(),INTERVAL 2 DAY)),
(960002,910001,900008,NULL,'하늘이가 용기를 내는 장면이 인상 깊었습니다.',0,DATE_SUB(NOW(),INTERVAL 1 DAY),DATE_SUB(NOW(),INTERVAL 1 DAY)),
(960003,910001,900002,960002,'따뜻하게 읽어 주셔서 감사합니다.',0,DATE_SUB(NOW(),INTERVAL 20 HOUR),DATE_SUB(NOW(),INTERVAL 20 HOUR)),
(960004,910003,900009,NULL,'가족과 서로의 마음을 이야기해 볼 수 있는 책이네요.',0,DATE_SUB(NOW(),INTERVAL 3 DAY),DATE_SUB(NOW(),INTERVAL 3 DAY)),
(960005,910005,900010,NULL,'서로 다른 모습이 장점이 되는 결말이 좋았어요.',0,DATE_SUB(NOW(),INTERVAL 4 DAY),DATE_SUB(NOW(),INTERVAL 4 DAY)),
(960006,910007,900011,NULL,'첫 무대를 마친 레오에게 큰 박수를 보내고 싶어요!',0,DATE_SUB(NOW(),INTERVAL 2 DAY),DATE_SUB(NOW(),INTERVAL 2 DAY)),
(960007,910009,900008,NULL,'환경을 지키는 방법을 아이가 쉽게 이해했어요.',0,DATE_SUB(NOW(),INTERVAL 5 DAY),DATE_SUB(NOW(),INTERVAL 5 DAY)),
(960008,910010,900007,NULL,'삽화와 바다 분위기가 정말 따뜻하네요.',0,DATE_SUB(NOW(),INTERVAL 1 DAY),DATE_SUB(NOW(),INTERVAL 1 DAY)),
(960009,910011,900009,NULL,'시간의 소중함을 자연스럽게 알려 주는 이야기예요.',0,DATE_SUB(NOW(),INTERVAL 6 HOUR),DATE_SUB(NOW(),INTERVAL 6 HOUR)),
(960010,910012,900012,NULL,'관련 없는 홍보 링크를 반복해서 게시합니다.',0,DATE_SUB(NOW(),INTERVAL 8 HOUR),DATE_SUB(NOW(),INTERVAL 8 HOUR)),
(960011,910006,900010,NULL,'삭제된 댓글입니다.',1,DATE_SUB(NOW(),INTERVAL 7 DAY),NOW());

INSERT INTO `author_follow` (`id`, `follower_id`, `author_id`, `created_at`) VALUES
(970001,900007,900002,DATE_SUB(NOW(),INTERVAL 40 DAY)),
(970002,900008,900002,DATE_SUB(NOW(),INTERVAL 35 DAY)),
(970003,900009,900002,DATE_SUB(NOW(),INTERVAL 20 DAY)),
(970004,900010,900002,DATE_SUB(NOW(),INTERVAL 12 DAY)),
(970005,900011,900002,DATE_SUB(NOW(),INTERVAL 5 DAY)),
(970006,900007,900005,DATE_SUB(NOW(),INTERVAL 15 DAY)),
(970007,900008,900005,DATE_SUB(NOW(),INTERVAL 8 DAY)),
(970008,900009,900006,DATE_SUB(NOW(),INTERVAL 10 DAY)),
(970009,900010,900004,DATE_SUB(NOW(),INTERVAL 4 DAY));

-- 다른 독자의 서재 데이터(발표자 서재는 빈 상태 유지)
INSERT INTO `my_reading` (`id`, `member_id`, `book_id`, `reading_status`, `current_page`, `progress`, `recent_read_at`, `completed_at`, `reread_count`, `read_date`, `reading_time`, `created_at`, `updated_at`, `is_wishlist`) VALUES
(971001,900007,910010,'READING',3,75,DATE_SUB(NOW(),INTERVAL 1 DAY),NULL,0,CURDATE(),25,DATE_SUB(NOW(),INTERVAL 10 DAY),NOW(),0),
(971002,900007,910003,'COMPLETED',4,100,DATE_SUB(NOW(),INTERVAL 5 DAY),DATE_SUB(NOW(),INTERVAL 5 DAY),1,DATE_SUB(CURDATE(),INTERVAL 5 DAY),35,DATE_SUB(NOW(),INTERVAL 20 DAY),NOW(),0),
(971003,900008,910011,'READING',2,50,DATE_SUB(NOW(),INTERVAL 2 DAY),NULL,0,DATE_SUB(CURDATE(),INTERVAL 2 DAY),18,DATE_SUB(NOW(),INTERVAL 8 DAY),NOW(),0),
(971004,900009,910001,'COMPLETED',4,100,DATE_SUB(NOW(),INTERVAL 3 DAY),DATE_SUB(NOW(),INTERVAL 3 DAY),0,DATE_SUB(CURDATE(),INTERVAL 3 DAY),30,DATE_SUB(NOW(),INTERVAL 15 DAY),NOW(),0),
(971005,900010,910007,NULL,NULL,0,NULL,NULL,0,NULL,0,DATE_SUB(NOW(),INTERVAL 2 DAY),NOW(),1);

INSERT INTO `bookmark` (`id`, `member_id`, `book_id`, `page_no`, `created_at`) VALUES
(972001,900007,910010,3,DATE_SUB(NOW(),INTERVAL 1 DAY)),
(972002,900008,910011,2,DATE_SUB(NOW(),INTERVAL 2 DAY)),
(972003,900009,910001,3,DATE_SUB(NOW(),INTERVAL 3 DAY));

INSERT INTO `reading_memo` (`id`, `member_id`, `book_id`, `page_no`, `content`, `pos_x`, `pos_y`, `created_at`, `updated_at`) VALUES
(973001,900007,910010,3,'파랑이가 용기를 낸 이유에 관해 이야기하기',20.00,30.00,DATE_SUB(NOW(),INTERVAL 1 DAY),NOW()),
(973002,900008,910011,2,'우리 가족에게 가장 소중한 시간 적어 보기',35.00,45.00,DATE_SUB(NOW(),INTERVAL 2 DAY),NOW());

INSERT INTO `reading_plan` (`id`, `member_id`, `book_id`, `plan_date`, `target_page`, `memo`, `is_completed`, `completed_at`, `created_at`, `updated_at`) VALUES
(974001,900007,910010,CURDATE(),4,'오늘 작은 고래 이야기 완독하기',0,NULL,DATE_SUB(NOW(),INTERVAL 2 DAY),NOW()),
(974002,900008,910011,CURDATE(),3,'잠들기 전에 한 페이지 읽기',0,NULL,DATE_SUB(NOW(),INTERVAL 1 DAY),NOW()),
(974003,900009,910001,DATE_SUB(CURDATE(),INTERVAL 1 DAY),4,'마지막 장까지 읽기',1,DATE_SUB(NOW(),INTERVAL 1 DAY),DATE_SUB(NOW(),INTERVAL 3 DAY),NOW());

INSERT INTO `book_review` (`id`, `member_id`, `book_id`, `content`, `is_draft`, `ai_feedback_content`, `ai_feedback_created_at`, `created_at`, `updated_at`) VALUES
(975001,900007,910003,'가족의 마음은 표현할수록 더 잘 보인다는 것을 느꼈다.',0,'가족의 사랑이라는 핵심 주제를 잘 찾았어요. 기억에 남는 장면을 한 가지 더 적어 보세요.',DATE_SUB(NOW(),INTERVAL 4 DAY),DATE_SUB(NOW(),INTERVAL 5 DAY),NOW()),
(975002,900009,910001,'무서워도 친구를 위해 한 걸음 내딛는 하늘이가 멋졌다.',0,NULL,NULL,DATE_SUB(NOW(),INTERVAL 3 DAY),NOW());

-- ----------------------------------------------------------------------------
-- 6. 주간 랭킹: 최근 날짜를 사용해 발표일이 바뀌어도 조회 가능
-- ----------------------------------------------------------------------------
INSERT INTO `weekly_book_ranking` (`id`, `book_id`, `week_start_date`, `score`) VALUES
(976001,910001,DATE_SUB(CURDATE(),INTERVAL WEEKDAY(CURDATE()) DAY),980),
(976002,910011,DATE_SUB(CURDATE(),INTERVAL WEEKDAY(CURDATE()) DAY),940),
(976003,910007,DATE_SUB(CURDATE(),INTERVAL WEEKDAY(CURDATE()) DAY),890),
(976004,910009,DATE_SUB(CURDATE(),INTERVAL WEEKDAY(CURDATE()) DAY),820),
(976005,910003,DATE_SUB(CURDATE(),INTERVAL WEEKDAY(CURDATE()) DAY),770),
(976006,910005,DATE_SUB(CURDATE(),INTERVAL WEEKDAY(CURDATE()) DAY),710),
(976007,910012,DATE_SUB(CURDATE(),INTERVAL WEEKDAY(CURDATE()) DAY),650),
(976008,910002,DATE_SUB(CURDATE(),INTERVAL WEEKDAY(CURDATE()) DAY),590),
(976009,910010,DATE_SUB(CURDATE(),INTERVAL WEEKDAY(CURDATE()) DAY),540),
(976010,910008,DATE_SUB(CURDATE(),INTERVAL WEEKDAY(CURDATE()) DAY),480);

-- ----------------------------------------------------------------------------
-- 7. 결제/AI 사용량/알림: 발표자에게는 구독 정보만 있고 AI 사용 이력은 0건
-- ----------------------------------------------------------------------------
INSERT INTO `payment` (`id`, `member_id`, `amount`, `status`, `fail_reason`, `pg_transaction_id`, `paid_at`, `plan_type`, `created_at`) VALUES
(977001,900001,9900,'SUCCESS',NULL,'DEMO-PAY-900001',DATE_SUB(NOW(),INTERVAL 10 DAY),'PREMIUM_MONTHLY',DATE_SUB(NOW(),INTERVAL 10 DAY)),
(977002,900002,99000,'SUCCESS',NULL,'DEMO-PAY-900002',DATE_SUB(NOW(),INTERVAL 90 DAY),'PREMIUM_YEARLY',DATE_SUB(NOW(),INTERVAL 90 DAY)),
(977003,900008,9900,'SUCCESS',NULL,'DEMO-PAY-900008',DATE_SUB(NOW(),INTERVAL 20 DAY),'PREMIUM_MONTHLY',DATE_SUB(NOW(),INTERVAL 20 DAY)),
(977004,900006,9900,'FAILED','잔액 부족','DEMO-PAY-FAILED-1',NULL,'PREMIUM_MONTHLY',DATE_SUB(NOW(),INTERVAL 3 DAY));

INSERT INTO `ai_generation_usage` (`id`, `book_id`, `member_id`, `call_type`, `input_token_count`, `output_token_count`, `image_count`, `created_at`) VALUES
(978001,910001,900002,'TEXT',620,1240,NULL,DATE_SUB(NOW(),INTERVAL 6 DAY)),
(978002,910001,900002,'IMAGE',NULL,NULL,4,DATE_SUB(NOW(),INTERVAL 6 DAY)),
(978003,910003,900006,'TEXT',550,1100,NULL,DATE_SUB(NOW(),INTERVAL 5 DAY)),
(978004,910003,900006,'IMAGE',NULL,NULL,4,DATE_SUB(NOW(),INTERVAL 5 DAY)),
(978005,910007,900002,'TEXT',710,1320,NULL,DATE_SUB(NOW(),INTERVAL 4 DAY)),
(978006,910007,900002,'IMAGE',NULL,NULL,4,DATE_SUB(NOW(),INTERVAL 4 DAY)),
(978007,910009,900005,'TEXT',580,1180,NULL,DATE_SUB(NOW(),INTERVAL 3 DAY)),
(978008,910009,900005,'IMAGE',NULL,NULL,4,DATE_SUB(NOW(),INTERVAL 3 DAY)),
(978009,910011,900004,'TEXT',690,1400,NULL,DATE_SUB(NOW(),INTERVAL 2 DAY)),
(978010,910011,900004,'IMAGE',NULL,NULL,4,DATE_SUB(NOW(),INTERVAL 2 DAY));

INSERT INTO `notification` (`id`, `member_id`, `content`, `is_read`, `created_at`) VALUES
(979001,900002,'달빛 고양이와 별의 문에 새로운 댓글이 달렸습니다.',0,DATE_SUB(NOW(),INTERVAL 1 DAY)),
(979002,900002,'책읽는민준님이 작가님을 팔로우했습니다.',0,DATE_SUB(NOW(),INTERVAL 5 DAY)),
(979003,900002,'달빛 고양이와 별의 문이 주간 랭킹 1위에 올랐습니다.',1,DATE_SUB(NOW(),INTERVAL 6 DAY)),
(979004,900005,'숲을 지키는 작은 다람쥐에 새로운 댓글이 달렸습니다.',0,DATE_SUB(NOW(),INTERVAL 5 DAY)),
(979005,900004,'시간을 파는 마법 상점에 새로운 댓글이 달렸습니다.',0,DATE_SUB(NOW(),INTERVAL 6 HOUR));

-- ----------------------------------------------------------------------------
-- 8. 신고 및 관리자 처리 시연
-- ----------------------------------------------------------------------------
INSERT INTO `report` (`id`, `member_id`, `target_type`, `target_id`, `reason`, `reason_detail`, `status`, `processed_by`, `processed_at`, `created_at`) VALUES
(980001,900007,'COMMENT',960010,'SPAM','관련 없는 홍보성 내용을 반복해서 게시했습니다.','PENDING',NULL,NULL,DATE_SUB(NOW(),INTERVAL 7 HOUR)),
(980002,900008,'BOOK',910012,'OTHER','표지 이미지가 내용과 관련 없는지 확인이 필요합니다.','PENDING',NULL,NULL,DATE_SUB(NOW(),INTERVAL 1 DAY)),
(980003,900009,'COMMENT',960011,'ABUSE','불쾌감을 주는 댓글입니다.','RESOLVED',900003,DATE_SUB(NOW(),INTERVAL 2 DAY),DATE_SUB(NOW(),INTERVAL 3 DAY)),
(980004,900010,'BOOK',910006,'OTHER','검토 결과 신고 사유가 확인되지 않았습니다.','REJECTED',900003,DATE_SUB(NOW(),INTERVAL 4 DAY),DATE_SUB(NOW(),INTERVAL 5 DAY));

INSERT INTO `admin_action_log` (`id`, `report_id`, `admin_id`, `action_type`, `action_reason`, `created_at`) VALUES
(981001,980003,900003,'COMMENT_DELETE','운영 정책을 위반한 댓글을 삭제했습니다.',DATE_SUB(NOW(),INTERVAL 2 DAY)),
(981002,980004,900003,'REPORT_REJECT','신고 대상에서 정책 위반 내용을 확인할 수 없습니다.',DATE_SUB(NOW(),INTERVAL 4 DAY));

-- 좋아요/댓글 집계와 주간 좋아요 수를 실제 데이터에 맞춘다.
UPDATE `book` b
SET b.`like_count` = (SELECT COUNT(*) FROM `book_like` bl WHERE bl.`book_id` = b.`id`),
    b.`comment_count` = (SELECT COUNT(*) FROM `comment` c WHERE c.`book_id` = b.`id` AND c.`is_deleted` = 0),
    b.`week_like_count` = (SELECT COUNT(*) FROM `book_like` bl WHERE bl.`book_id` = b.`id` AND bl.`created_at` >= DATE_SUB(NOW(), INTERVAL 7 DAY))
WHERE b.`id` BETWEEN 910001 AND 910012;

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------------------------------------------------------
-- 9. 실행 결과 검증: 아래 결과에서 demo 관련 값은 모두 0이어야 한다.
-- ----------------------------------------------------------------------------
SELECT '발표용 회원 수' AS `check_name`, COUNT(*) AS `result`
FROM `member` WHERE `id` BETWEEN 900001 AND 900012
UNION ALL
SELECT '공개 도서 수', COUNT(*) FROM `book` WHERE `id` BETWEEN 910001 AND 910012
UNION ALL
SELECT '장르 수', COUNT(DISTINCT `tag_name`) FROM `book_tag`
WHERE `id` BETWEEN 940001 AND 940024 AND `tag_name` IN ('모험','가족','우정','성장','자연','판타지')
UNION ALL
SELECT 'demo 소유 도서 수(0 정상)', COUNT(*) FROM `book` WHERE `member_id` = 900001
UNION ALL
SELECT 'demo 독서 기록 수(0 정상)', COUNT(*) FROM `my_reading` WHERE `member_id` = 900001
UNION ALL
SELECT 'demo 좋아요 수(0 정상)', COUNT(*) FROM `book_like` WHERE `member_id` = 900001
UNION ALL
SELECT 'demo 댓글 수(0 정상)', COUNT(*) FROM `comment` WHERE `member_id` = 900001
UNION ALL
SELECT 'demo 팔로우 수(0 정상)', COUNT(*) FROM `author_follow` WHERE `follower_id` = 900001
UNION ALL
SELECT 'demo AI 사용 이력 수(0 정상)', COUNT(*) FROM `ai_generation_usage` WHERE `member_id` = 900001;

-- 고아 데이터가 없으면 result는 모두 0이다.
SELECT '도서-회원 고아 데이터' AS `check_name`, COUNT(*) AS `result`
FROM `book` b LEFT JOIN `member` m ON m.`id` = b.`member_id`
WHERE b.`id` BETWEEN 910001 AND 910012 AND m.`id` IS NULL
UNION ALL
SELECT '페이지-도서 고아 데이터', COUNT(*)
FROM `book_page` p LEFT JOIN `book` b ON b.`id` = p.`book_id`
WHERE p.`id` BETWEEN 920001 AND 920048 AND b.`id` IS NULL
UNION ALL
SELECT '좋아요 집계 불일치', COUNT(*) FROM (
    SELECT b.`id`
    FROM `book` b LEFT JOIN `book_like` bl ON bl.`book_id` = b.`id`
    WHERE b.`id` BETWEEN 910001 AND 910012
    GROUP BY b.`id`, b.`like_count`
    HAVING b.`like_count` <> COUNT(bl.`id`)
) mismatch;
