-- 상상서가 전체 스키마 생성 스크립트 (v1 - 2026-07-22 기준, free_trial_started 컬럼 포함).
-- src/main/java/.../domain/*/entity/*.java의 JPA 매핑을 기준으로 작성했다(운영 RDS는
-- ddl-auto: validate라 스키마를 자동 생성하지 않으므로, 새 RDS를 처음 구성하거나 delete_all_tables.sql로
-- 초기화한 뒤 처음부터 다시 만들 때 이 스크립트로 테이블을 만든다).
-- FK가 있는 테이블은 참조 대상 테이블보다 뒤에 오도록 순서를 맞췄다(부모 -> 자식).
--
-- 이미 떠 있는 운영 RDS(구버전 스키마)에는 이 스크립트를 쓰지 말 것 - 전체 재생성용이라 기존 데이터가
-- 다 날아간다. 이미 배포된 운영 DB에 이번 변경(free_trial_started 컬럼)만 반영하려면 같은 폴더의
-- update_v1.sql만 실행한다.
--
-- 주의: Boolean 컬럼은 Hibernate + MariaDB 조합의 기본 매핑인 BIT(1)로 작성했다. 실제 운영 RDS의
-- `DESCRIBE member;` 결과가 TINYINT(1) 등으로 다르게 나온다면 그 타입에 맞게 조정할 것.
-- `book.confirmed_settings`는 ERD(dbdiagram.io)에 JSON으로 표시되어 있어 JSON 타입으로 맞췄다
-- (MariaDB의 JSON은 LONGTEXT + CHECK 제약의 별칭이라 @Lob 매핑과 호환된다).

CREATE TABLE `member` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `version` BIGINT NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `birth_date` DATE NULL,
    `nickname` VARCHAR(255) NULL,
    `profile_image_url` LONGTEXT NULL,
    `introduction` LONGTEXT NULL,
    `status` VARCHAR(255) NOT NULL,
    `role` VARCHAR(255) NOT NULL,
    `free_trial_used` BIT(1) NOT NULL,
    `free_trial_started` BIT(1) NULL,
    `auth_provider` VARCHAR(255) NOT NULL,
    `oauth_provider_id` VARCHAR(255) NULL,
    `viewer_font_size` VARCHAR(255) NULL,
    `viewer_view_type` VARCHAR(255) NULL,
    `withdrawn_at` DATETIME NULL,
    `subscription_plan` VARCHAR(255) NULL,
    `subscription_start_at` DATETIME NULL,
    `subscription_end_at` DATETIME NULL,
    `subscription_auto_renew` BIT(1) NULL,
    `daily_text_remaining` INT NULL,
    `daily_image_remaining` INT NULL,
    `last_token_reset_date` DATE NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_member_email` (`email`),
    UNIQUE KEY `uk_member_nickname` (`nickname`),
    UNIQUE KEY `uk_member_auth_provider` (`auth_provider`, `oauth_provider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `book` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT NOT NULL,
    `book_type` VARCHAR(255) NOT NULL,
    `creation_mode` VARCHAR(255) NULL,
    `author_age_group` VARCHAR(255) NULL,
    `reader_age_group` VARCHAR(255) NULL,
    `title` VARCHAR(255) NOT NULL,
    `description` TEXT NULL,
    `category` VARCHAR(255) NULL,
    `target_lang` VARCHAR(255) NULL,
    `style_code` VARCHAR(255) NULL,
    `cover_image_id` BIGINT NULL,
    `confirmed_settings` JSON NULL,
    `status` VARCHAR(255) NOT NULL,
    `page_count` INT NOT NULL,
    `view_count` INT NOT NULL,
    `like_count` INT NOT NULL,
    `comment_count` INT NOT NULL,
    `week_view_count` INT NOT NULL,
    `week_like_count` INT NOT NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_book_cover_image_id` (`cover_image_id`),
    CONSTRAINT `fk_book_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `guardian_consent` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT NOT NULL,
    `guardian_email` VARCHAR(255) NOT NULL,
    `guardian_id` BIGINT NULL,
    `status` VARCHAR(255) NOT NULL,
    `requested_at` DATETIME NULL,
    `expires_at` DATETIME NULL,
    `approved_at` DATETIME NULL,
    `withdrawn_at` DATETIME NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_guardian_consent_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
    CONSTRAINT `fk_guardian_consent_guardian` FOREIGN KEY (`guardian_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `payment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT NULL,
    `amount` INT NULL,
    `status` VARCHAR(255) NULL,
    `fail_reason` VARCHAR(255) NULL,
    `pg_transaction_id` VARCHAR(255) NULL,
    `paid_at` DATETIME NULL,
    `plan_type` VARCHAR(255) NULL,
    `created_at` DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_payment_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `book_page` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `book_id` BIGINT NOT NULL,
    `page_no` INT NOT NULL,
    `title` VARCHAR(255) NULL,
    `title_en` VARCHAR(255) NULL,
    `content_type` VARCHAR(255) NOT NULL,
    `content_text_ko` TEXT NULL,
    `content_text_en` TEXT NULL,
    `content_font_size_en` INT NULL,
    `image_url` VARCHAR(255) NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_book_page_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `book_image` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `book_id` BIGINT NOT NULL,
    `book_page_id` BIGINT NULL,
    `image_type` VARCHAR(255) NOT NULL,
    `image_order` INT NULL,
    `file_url` LONGTEXT NOT NULL,
    `file_name` VARCHAR(255) NOT NULL,
    `file_extension` VARCHAR(255) NULL,
    `file_size` BIGINT NULL,
    `created_at` DATETIME NULL,
    `updated_at` DATETIME NULL,
    `deleted_at` DATETIME NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_book_image_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `book_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `book_id` BIGINT NOT NULL,
    `tag_name` VARCHAR(50) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_book_tag_book_tag_name` (`book_id`, `tag_name`),
    CONSTRAINT `fk_book_tag_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `weekly_book_ranking` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `book_id` BIGINT NOT NULL,
    `week_start_date` DATE NOT NULL,
    `score` INT NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_weekly_book_ranking_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `ai_generation_usage` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `book_id` BIGINT NULL,
    `member_id` BIGINT NOT NULL,
    `call_type` VARCHAR(255) NOT NULL,
    `input_token_count` INT NULL,
    `output_token_count` INT NULL,
    `image_count` INT NULL,
    `created_at` DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_ai_generation_usage_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`),
    CONSTRAINT `fk_ai_generation_usage_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `book_review` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT NOT NULL,
    `book_id` BIGINT NOT NULL,
    `content` MEDIUMTEXT NULL,
    `is_draft` BIT(1) NOT NULL,
    `ai_feedback_content` TEXT NULL,
    `ai_feedback_created_at` DATETIME NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_book_review_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
    CONSTRAINT `fk_book_review_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `my_reading` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT NOT NULL,
    `book_id` BIGINT NOT NULL,
    `reading_status` VARCHAR(255) NULL,
    `is_wishlist` BIT(1) NOT NULL,
    `current_page` INT NULL,
    `progress` INT NULL,
    `recent_read_at` DATETIME NULL,
    `completed_at` DATETIME NULL,
    `reread_count` INT NOT NULL,
    `read_date` DATE NULL,
    `reading_time` INT NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_my_reading_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
    CONSTRAINT `fk_my_reading_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `reading_memo` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT NOT NULL,
    `book_id` BIGINT NOT NULL,
    `page_no` INT NOT NULL,
    `content` TEXT NOT NULL,
    `pos_x` DECIMAL(19, 2) NULL,
    `pos_y` DECIMAL(19, 2) NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_reading_memo_member_book_page` (`member_id`, `book_id`, `page_no`),
    CONSTRAINT `fk_reading_memo_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
    CONSTRAINT `fk_reading_memo_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `reading_plan` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT NOT NULL,
    `book_id` BIGINT NOT NULL,
    `plan_date` DATE NOT NULL,
    `target_page` INT NULL,
    `memo` VARCHAR(255) NULL,
    `is_completed` BIT(1) NOT NULL,
    `completed_at` DATETIME NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_reading_plan_member_book_date` (`member_id`, `book_id`, `plan_date`),
    CONSTRAINT `fk_reading_plan_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
    CONSTRAINT `fk_reading_plan_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `book_like` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT NOT NULL,
    `book_id` BIGINT NOT NULL,
    `created_at` DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_book_like_member_book` (`member_id`, `book_id`),
    CONSTRAINT `fk_book_like_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
    CONSTRAINT `fk_book_like_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `bookmark` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT NOT NULL,
    `book_id` BIGINT NOT NULL,
    `page_no` INT NOT NULL,
    `created_at` DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_bookmark_member_book` (`member_id`, `book_id`),
    CONSTRAINT `fk_bookmark_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
    CONSTRAINT `fk_bookmark_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `author_follow` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `follower_id` BIGINT NOT NULL,
    `author_id` BIGINT NOT NULL,
    `created_at` DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_author_follow_follower_author` (`follower_id`, `author_id`),
    CONSTRAINT `fk_author_follow_follower` FOREIGN KEY (`follower_id`) REFERENCES `member` (`id`),
    CONSTRAINT `fk_author_follow_author` FOREIGN KEY (`author_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `book_id` BIGINT NOT NULL,
    `member_id` BIGINT NULL,
    `reply_to_id` BIGINT NULL,
    `content` VARCHAR(255) NOT NULL,
    `is_deleted` BIT(1) NOT NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_comment_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`id`),
    CONSTRAINT `fk_comment_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
    CONSTRAINT `fk_comment_reply_to` FOREIGN KEY (`reply_to_id`) REFERENCES `comment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `report` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT NOT NULL,
    `target_type` VARCHAR(255) NOT NULL,
    `target_id` BIGINT NOT NULL,
    `reason` VARCHAR(255) NOT NULL,
    `reason_detail` VARCHAR(255) NULL,
    `status` VARCHAR(255) NOT NULL,
    `processed_by` BIGINT NULL,
    `processed_at` DATETIME NULL,
    `created_at` DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_report_reporter` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
    CONSTRAINT `fk_report_processed_by` FOREIGN KEY (`processed_by`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `admin_action_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `report_id` BIGINT NOT NULL,
    `admin_id` BIGINT NOT NULL,
    `action_type` VARCHAR(255) NOT NULL,
    `action_reason` VARCHAR(255) NULL,
    `created_at` DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_admin_action_log_report` FOREIGN KEY (`report_id`) REFERENCES `report` (`id`),
    CONSTRAINT `fk_admin_action_log_admin` FOREIGN KEY (`admin_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `notification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `member_id` BIGINT NOT NULL,
    `content` VARCHAR(255) NOT NULL,
    `is_read` BIT(1) NOT NULL,
    `created_at` DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_notification_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
