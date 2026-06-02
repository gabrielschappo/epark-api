package com.epark.dto;

public record GerarVagasDTO(
        String setor,
        String prefixo,
        int qtdNormal,
        int qtdPcd,
        int qtdEletrica
) {}