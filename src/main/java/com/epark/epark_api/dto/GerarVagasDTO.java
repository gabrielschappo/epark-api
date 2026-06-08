package com.epark.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record GerarVagasDTO(
        String setor,

        @NotBlank(message = "O prefixo da vaga é obrigatório")
        String prefixo,

        @Min(value = 0, message = "Quantidade de vagas normais não pode ser negativa")
        int qtdNormal,

        @Min(value = 0, message = "Quantidade de vagas PCD não pode ser negativa")
        int qtdPcd,

        @Min(value = 0, message = "Quantidade de vagas elétricas não pode ser negativa")
        int qtdEletrica
) {}