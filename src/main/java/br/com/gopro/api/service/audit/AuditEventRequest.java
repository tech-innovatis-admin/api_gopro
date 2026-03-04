package br.com.gopro.api.service.audit;

import br.com.gopro.api.enums.AuditResultEnum;
import br.com.gopro.api.enums.AuditScopeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class AuditEventRequest {
    private final Long actorUserId;
    private final AuditScopeEnum tipoAuditoria;
    private final String modulo;
    private final String feature;
    private final String entidadePrincipal;
    private final String entidadeId;
    private final String aba;
    private final String subsecao;
    private final String acao;
    private final String resumo;
    private final String descricao;
    private final AuditResultEnum resultado;
    private final String correlacaoId;
    private final Object antes;
    private final Object depois;
    private final List<AuditFieldChange> alteracoes;
    private final Object detalhesTecnicos;
}

