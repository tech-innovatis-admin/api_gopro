package br.com.gopro.api.enums;

public enum AuditScopeEnum {
    SYSTEM("system"),
    CONTRACTS("contracts"),
    USERS("users"),
    PEOPLE_COMPANIES("people_companies");

    private final String prefix;

    AuditScopeEnum(String prefix) {
        this.prefix = prefix;
    }

    public String prefix() {
        return prefix;
    }
}
