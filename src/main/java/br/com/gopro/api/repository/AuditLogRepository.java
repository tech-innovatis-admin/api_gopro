package br.com.gopro.api.repository;

import br.com.gopro.api.enums.AuditScopeEnum;
import br.com.gopro.api.model.AuditLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    @Query("""
    select a
    from AuditLog a
    where a.tipoAuditoria = :scope
      and a.action = :action
    order by a.eventAt desc, a.createdAt desc
""")
    List<AuditLog> findRecentByScopeAndAction(
            @Param("scope") AuditScopeEnum scope,
            @Param("action") String action,
            Pageable pageable
    );
}
