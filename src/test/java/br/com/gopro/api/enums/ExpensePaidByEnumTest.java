package br.com.gopro.api.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExpensePaidByEnumTest {

    @Test
    void fromValue_shouldAcceptCurrentAndLegacyValues() {
        assertThat(ExpensePaidByEnum.fromValue("INNOVATIS")).isEqualTo(ExpensePaidByEnum.INNOVATIS);
        assertThat(ExpensePaidByEnum.fromValue("EXECUCAO")).isEqualTo(ExpensePaidByEnum.EXECUCAO);
        assertThat(ExpensePaidByEnum.fromValue("EXECUÇÃO")).isEqualTo(ExpensePaidByEnum.EXECUCAO);
        assertThat(ExpensePaidByEnum.fromValue("EMPRESA")).isEqualTo(ExpensePaidByEnum.INNOVATIS);
        assertThat(ExpensePaidByEnum.fromValue("PARCEIRO")).isEqualTo(ExpensePaidByEnum.EXECUCAO);
    }
}
