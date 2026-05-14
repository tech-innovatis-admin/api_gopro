package br.com.gopro.api.enums;

public enum ContractingStatusEnum {
    EM_CADASTRO,
    EM_CONTRATACAO,
    CONTRATADA,
    EM_EXECUCAO,
    CONCLUIDA,
    CANCELADA;

    public boolean allowsFinancialLink() {
        return this == CONTRATADA || this == EM_EXECUCAO;
    }
}
