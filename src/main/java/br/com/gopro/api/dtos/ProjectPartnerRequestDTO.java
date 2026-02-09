package br.com.gopro.api.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectPartnerRequestDTO {

    @NotNull(message = "Parceiro e obrigatorio")
    @Positive(message = "ID do parceiro deve ser maior que zero")
    private Long partnerId;
}
