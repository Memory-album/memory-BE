package com.min.i.memory_BE.domain.user.repository;

import com.min.i.memory_BE.domain.user.entity.OAuthAccount;
import com.min.i.memory_BE.domain.user.enums.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {

    // 특정 사용자와 제공자(OAuthProvider)를 기반으로 OAuthAccount 조회
    Optional<OAuthAccount> findByUserIdAndProvider(Long userId, OAuthProvider provider);

    // 제공자와 제공자 사용자 ID로 OAuthAccount 조회
    Optional<OAuthAccount> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId);

}
