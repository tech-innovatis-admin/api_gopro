package br.com.gopro.api.enums;

public enum UserRoleEnum {
    OWNER,
    SUPERADMIN,
    ADMIN,
    ANALISTA,
    ESTAGIARIO;

    public boolean hasSuperadminPrivileges() {
        return this == OWNER || this == SUPERADMIN;
    }

    public boolean canAccessAdminArea() {
        return hasSuperadminPrivileges() || this == ADMIN;
    }

    public boolean isProtectedAccount() {
        return this == OWNER;
    }
}
