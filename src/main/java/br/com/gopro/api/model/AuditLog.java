package br.com.gopro.api.model;

import br.com.gopro.api.enums.AuditScopeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "audit_log")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "actor_user_id",
            foreignKey = @ForeignKey(name = "fk_audit_log_actor_user")
    )
    private AppUser actorUser;

    @Column(name = "action", nullable = false, length = 120)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 120)
    private String entityType;

    @Column(name = "entity_id", length = 120)
    private String entityId;

    @Column(name = "before_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String beforeJson;

    @Column(name = "after_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String afterJson;

    @Column(name = "audit_id", length = 36)
    private String auditId;

    @Column(name = "event_at")
    private OffsetDateTime eventAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_auditoria", length = 30)
    private AuditScopeEnum tipoAuditoria;

    @Column(name = "modulo", length = 120)
    private String modulo;

    @Column(name = "feature", length = 160)
    private String feature;

    @Column(name = "entidade_principal", length = 120)
    private String entidadePrincipal;

    @Column(name = "aba", length = 120)
    private String aba;

    @Column(name = "subsecao", length = 120)
    private String subsecao;

    @Column(name = "resumo", length = 500)
    private String resumo;

    @Column(name = "descricao", columnDefinition = "text")
    private String descricao;

    @Column(name = "resultado", length = 20)
    private String resultado;

    @Column(name = "correlacao_id", length = 120)
    private String correlacaoId;

    @Column(name = "alteracoes_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String alteracoesJson;

    @Column(name = "detalhes_tecnicos_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String detalhesTecnicosJson;

    @Column(name = "ip", length = 64)
    private String ip;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
}
