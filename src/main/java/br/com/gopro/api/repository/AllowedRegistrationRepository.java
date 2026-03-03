package br.com.gopro.api.repository;

import br.com.gopro.api.enums.AllowedRegistrationStatusEnum;
import br.com.gopro.api.model.AllowedRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AllowedRegistrationRepository
        extends JpaRepository<AllowedRegistration, Long>, JpaSpecificationExecutor<AllowedRegistration> {

    Optional<AllowedRegistration> findByEmailIgnoreCase(String email);

    Optional<AllowedRegistration> findByInviteTokenHashAndStatus(String inviteTokenHash, AllowedRegistrationStatusEnum status);
}
