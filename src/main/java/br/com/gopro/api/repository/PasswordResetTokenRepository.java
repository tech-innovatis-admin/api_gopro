package br.com.gopro.api.repository;

import br.com.gopro.api.model.PasswordResetToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    @Modifying
    @Transactional
    @Query("""
            UPDATE PasswordResetToken token
               SET token.isActive = false,
                   token.updatedBy = :updatedBy
             WHERE token.user.id = :userId
               AND token.isActive = true
               AND token.usedAt IS NULL
            """)
    int invalidateActiveTokensByUserId(@Param("userId") Long userId, @Param("updatedBy") Long updatedBy);

    @Modifying
    @Transactional
    @Query("""
            UPDATE PasswordResetToken token
               SET token.isActive = false,
                   token.updatedBy = :updatedBy
             WHERE token.user.id = :userId
               AND token.isActive = true
               AND token.usedAt IS NULL
               AND token.expiresAt <= :referenceTime
            """)
    int invalidateExpiredActiveTokensByUserId(
            @Param("userId") Long userId,
            @Param("referenceTime") LocalDateTime referenceTime,
            @Param("updatedBy") Long updatedBy
    );
}
