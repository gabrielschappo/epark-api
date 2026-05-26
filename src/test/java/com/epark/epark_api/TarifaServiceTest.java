package com.epark.epark_api;


import com.epark.epark_api.service.TarifaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TarifaServiceTest {

    // Instanciamos o serviço diretamente, pois testes unitários não devem subir o contexto do Spring
    private final TarifaService tarifaService = new TarifaService();

    @Test
    @DisplayName("TU09 - Deve isentar o valor se o tempo for menor ou igual à tolerância")
    void deveIsentarTarifaDentroDaTolerancia() {
        LocalDateTime entrada = LocalDateTime.of(2026, 5, 20, 10, 0);
        LocalDateTime saida = LocalDateTime.of(2026, 5, 20, 10, 14); // 14 minutos (dentro dos 15m)

        BigDecimal valor = tarifaService.calcularValor(entrada, saida);

        // Usamos compareTo para BigDecimal, pois 0.00 é diferente de 0 internamente
        assertEquals(0, BigDecimal.ZERO.compareTo(valor));
    }

    @Test
    @DisplayName("TU07 - Deve calcular o valor exato para horas cheias")
    void deveCalcularHoraCheia() {
        LocalDateTime entrada = LocalDateTime.of(2026, 5, 20, 10, 0);
        LocalDateTime saida = LocalDateTime.of(2026, 5, 20, 12, 0); // Exatas 2 horas

        BigDecimal valor = tarifaService.calcularValor(entrada, saida);

        assertEquals(0, new BigDecimal("20.00").compareTo(valor));
    }

    @Test
    @DisplayName("TU08 - Deve cobrar uma nova hora para frações de tempo após a hora cheia")
    void deveCalcularFracaoDeHora() {
        LocalDateTime entrada = LocalDateTime.of(2026, 5, 20, 10, 0);
        LocalDateTime saida = LocalDateTime.of(2026, 5, 20, 11, 15); // 1 hora e 15 minutos

        BigDecimal valor = tarifaService.calcularValor(entrada, saida);

        // Como passou de 1 hora, o sistema deve arredondar para 2 horas cobradas
        assertEquals(0, new BigDecimal("20.00").compareTo(valor));
    }

    @Test
    @DisplayName("TU02 (Parcial) - Deve lançar exceção se a saída for antes da entrada")
    void deveLancarExcecaoSeSaidaAnteriorEntrada() {
        LocalDateTime entrada = LocalDateTime.of(2026, 5, 20, 10, 0);
        LocalDateTime saida = LocalDateTime.of(2026, 5, 20, 9, 0); // Saída no passado

        assertThrows(IllegalArgumentException.class, () -> {
            tarifaService.calcularValor(entrada, saida);
        });
    }
}
