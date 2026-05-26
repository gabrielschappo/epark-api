package com.epark.epark_api.dto;

public record TicketEntradaDTO(
        String placa,
        String modeloVeiculo,
        Long idVaga
) {}