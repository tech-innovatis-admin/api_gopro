package br.com.gopro.api.repository;

import br.com.gopro.api.enums.UserRoleEnum;
import br.com.gopro.api.enums.UserStatusEnum;
import br.com.gopro.api.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long>, JpaSpecificationExecutor<AppUser> {

    @Query("""
            SELECT u
            FROM AppUser u
            WHERE lower(u.email) = lower(:login)
               OR (u.username IS NOT NULL AND lower(u.username) = lower(:login))
            """)
    Optional<AppUser> findByLogin(@Param("login") String login);

    Optional<AppUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByRoleAndStatusAndIsActive(UserRoleEnum role, UserStatusEnum status, Boolean isActive);
}
