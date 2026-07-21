package com.kosta.sangsangseoga.domain.friendLibrary.repository;

import com.kosta.sangsangseoga.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 작가(회원) 검색/목록 조회 전용 레포지토리.
 * 팔로워 수 / 작품 수는 member 테이블에 없는 집계값이라 네이티브 쿼리의
 * 상관 서브쿼리로 정렬한다. 작품이 없는 회원도 검색 결과에 포함한다.
 */
public interface AuthorRepository extends JpaRepository<Member, Long> {

    // 팔로워 많은 순
    @Query(value = "SELECT m.* FROM member m " +
            "WHERE (:keyword IS NULL OR m.nickname LIKE CONCAT('%', :keyword, '%')) " +
            "ORDER BY (SELECT COUNT(*) FROM author_follow af WHERE af.author_id = m.id) DESC, m.id DESC",
            countQuery = "SELECT COUNT(*) FROM member m " +
            "WHERE (:keyword IS NULL OR m.nickname LIKE CONCAT('%', :keyword, '%'))",
            nativeQuery = true)
    Page<Member> findAuthorsByFollowers(@Param("keyword") String keyword, Pageable pageable);

    // 작품 많은 순
    @Query(value = "SELECT m.* FROM member m " +
            "WHERE (:keyword IS NULL OR m.nickname LIKE CONCAT('%', :keyword, '%')) " +
            "ORDER BY (SELECT COUNT(*) FROM book b2 WHERE b2.member_id = m.id AND b2.status = 'PUBLISHED') DESC, m.id DESC",
            countQuery = "SELECT COUNT(*) FROM member m " +
            "WHERE (:keyword IS NULL OR m.nickname LIKE CONCAT('%', :keyword, '%'))",
            nativeQuery = true)
    Page<Member> findAuthorsByWorks(@Param("keyword") String keyword, Pageable pageable);
}
