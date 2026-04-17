package br.com.gopro.api.repository;

import java.math.BigDecimal;

public interface ProjectTotalRow {
    Long getId();
    String getName();
    String getCode();
    String getProjectStatus();
    BigDecimal getTotalReceived();
    BigDecimal getTotalExpenses();
    BigDecimal getTotalReserved();
    BigDecimal getSaldoReal();
    BigDecimal getSaldo();
}
