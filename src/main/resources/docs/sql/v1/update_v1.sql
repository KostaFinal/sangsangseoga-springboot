-- 운영 RDS(sssg-mariadb, ap-northeast-2) 스키마 업데이트 - v1.
-- create_all_tables.sql로 전체를 새로 만드는 대신, 이미 떠 있는 운영 DB에 "이번에 새로 생긴 차이"만
-- 반영할 때 쓴다. 데이터는 그대로 두고 컬럼만 추가/변경한다.
--
-- 실행 방법(EC2에 SSH로 접속해서):
--   mysql -h sssg-mariadb.c3ecoskkcdmv.ap-northeast-2.rds.amazonaws.com -u <PROD_DB_USERNAME> -p sangsangseoga < update_v1.sql
-- 또는 EC2에 파일을 올려둔 뒤 그 안에서 위 명령을 그대로 실행해도 된다.
--
-- 적용 전 확인: free_trial_used 컬럼과 정확히 같은 타입으로 맞추기 위해 먼저 아래로 실제 타입을 확인한다.
--   SHOW COLUMNS FROM member LIKE 'free_trial_used';
-- 결과가 bit(1)이 아니라 tinyint(1) 등으로 나오면, 아래 ALTER의 BIT(1)을 그 타입으로 바꿔서 실행할 것.

-- ------------------------------------------------------------
-- member.free_trial_started 추가
-- FREE 상태로 무료체험 AI 생성을 시작했는지 표식(nullable). 이미 PREMIUM으로 전환된 뒤 책을 완성해도
-- 무료체험 슬롯이 "안 쓴 것"으로 남지 않도록 book.publish()가 이 값을 참고해 소진 처리한다.
-- (MemberOptimisticRetrySupport / UsageService.assertCanGenerate / BookServiceImpl.publish 참고)
-- ------------------------------------------------------------
ALTER TABLE `member` ADD COLUMN `free_trial_started` BIT(1) NULL;

-- ------------------------------------------------------------
-- 롤백이 필요할 때만 아래 주석을 풀어서 실행한다(이번 배포를 되돌리는 경우).
-- 되돌릴 때는 Member.freeTrialStarted / MemberRepository.markFreeTrialStartedIfNeeded /
-- UsageService, BookServiceImpl의 관련 코드도 같이 되돌려야 ddl-auto: validate가 다시 통과한다.
-- ------------------------------------------------------------
-- ALTER TABLE `member` DROP COLUMN `free_trial_started`;
