package br.com.gopro.api.service;

public final class AuditActions {

    public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String LOGIN_FAILED = "LOGIN_FAILED";
    public static final String PASSWORD_RESET_REQUESTED = "PASSWORD_RESET_REQUESTED";
    public static final String PASSWORD_RESET_COMPLETED = "PASSWORD_RESET_COMPLETED";
    public static final String INVITE_CREATED = "INVITE_CREATED";
    public static final String INVITE_REISSUED = "INVITE_REISSUED";
    public static final String INVITE_CANCELLED = "INVITE_CANCELLED";
    public static final String INVITE_VALIDATED = "INVITE_VALIDATED";
    public static final String REGISTER_COMPLETED = "REGISTER_COMPLETED";
    public static final String USER_UPDATED = "USER_UPDATED";
    public static final String SUPERADMIN_BOOTSTRAPPED = "SUPERADMIN_BOOTSTRAPPED";

    private AuditActions() {
    }
}
