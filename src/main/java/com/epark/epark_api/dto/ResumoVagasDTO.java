package com.epark.epark_api.dto;


public record ResumoVagasDTO(
        long totais,
        long ocupadas,
        long livres,
        long especiais
) {}