package com.epark.epark_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record TicketEntradaDTO(
        @NotBlank(message = "A placa é obrigatória")
        @Pattern(regexp = "^[A-Z]{3}-?[0-9][A-Z0-9][0-9]{2}$", message = "Formato de placa inválido")
        String placa,

        @NotBlank(message = "O modelo do veículo é obrigatório")
        String modeloVeiculo,

        @NotNull(message = "O ID da vaga é obrigatório")
        Long idVaga
) {}