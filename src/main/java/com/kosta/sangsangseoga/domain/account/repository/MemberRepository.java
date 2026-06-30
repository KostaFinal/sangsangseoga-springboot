package com.kosta.sangsangseoga.domain.account.repository;

import com.kosta.sangsangseoga.domain.account.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
