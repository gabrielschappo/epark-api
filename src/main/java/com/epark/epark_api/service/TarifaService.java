package com.epark.epark_api.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class TarifaService {

    private static final BigDecimal VALOR_HORA = new BigDecimal("10.00");
    private static final int TEMPO_TOLERANCIA_MINUTOS = 15;

    public BigDecimal calcularValor(LocalDateTime entrada, LocalDateTime saida) {
        if (entrada == null || saida == null) {
            throw new IllegalArgumentException("Datas de entrada e saída não podem ser nulas.");
        }

        if (saida.isBefore(entrada)) {
            // Cobre indiretamente o TU02 (Consistência de Data/Hora)
            throw new IllegalArgumentException("A hora de saída não pode ser anterior à entrada.");
        }

        long minutosPermanencia = Duration.between(entrada, saida).toMinutes();

        // TU09 - Regra de Tolerância
        if (minutosPermanencia <= TEMPO_TOLERANCIA_MINUTOS) {
            return BigDecimal.ZERO;
        }

        // TU07 (Hora Cheia) e TU08 (Fração de Hora)
        // Usamos Math.ceil para arredondar sempre para cima. Ex: 61 minutos cobram 2 horas.
        long horasCobradas = (long) Math.ceil(minutosPermanencia / 60.0);

        return VALOR_HORA.multiply(BigDecimal.valueOf(horasCobradas));
    }
}