SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------
-- 기존 테이블 삭제 (역순 - FK 체크는 위에서 이미 꺼둠)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `admin_action_log`;
DROP TABLE IF EXISTS `weekly_book_ranking`;
DROP TABLE IF EXISTS `book_review`;
DROP TABLE IF EXISTS `my_reading`;
DROP TABLE IF EXISTS `report`;
DROP TABLE IF EXISTS `comment`;
DROP TABLE IF EXISTS `author_follow`;
DROP TABLE IF EXISTS `bookmark`;
DROP TABLE IF EXISTS `book_like`;
DROP TABLE IF EXISTS `reading_plan`;
DROP TABLE IF EXISTS `book_tag`;
DROP TABLE IF EXISTS `reading_memo`;
DROP TABLE IF EXISTS `book_page`;
DROP TABLE IF EXISTS `book_image`;
DROP TABLE IF EXISTS `book`;
DROP TABLE IF EXISTS `ai_generation_usage`;
DROP TABLE IF EXISTS `payment`;
DROP TABLE IF EXISTS `guardian_consent`;
DROP TABLE IF EXISTS `member`;
 SET FOREIGN_KEY_CHECKS = 1;
