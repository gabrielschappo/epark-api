package com.epark.epark_api;

import com.epark.epark_api.domain.Ticket;
import com.epark.epark_api.domain.Vaga;
import com.epark.epark_api.repository.TicketRepository;
import com.epark.epark_api.repository.VagaRepository;
import com.epark.epark_api.service.TarifaService;
import com.epark.epark_api.service.TicketService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// A anotação abaixo habilita o uso do Mockito no JUnit 5
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private VagaRepository vagaRepository;

    @Mock
    private TarifaService tarifaService;

    // Injeta os Mocks criados acima dentro do TicketService
    @InjectMocks
    private TicketService ticketService;

    @Test
    @DisplayName("TU06 - Deve bloquear a geração de ticket quando estacionamento estiver lotado")
    void deveBloquearEntradaQuandoLotado() {
        // Simulando que não há vagas livres no banco
        when(vagaRepository.countByStatus(Vaga.StatusVaga.LIVRE)).thenReturn(0L);

        // Verifica se a exceção correta é lançada
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            ticketService.registrarEntrada("ABC-1234", "Corolla", 1L, LocalDateTime.now());
        });

        assertEquals("Estacionamento Lotado", exception.getMessage());

        // Garante que o sistema nem tentou salvar nada no banco
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("TU04 - Deve registrar entrada e subtrair vaga disponível (Mudar status para OCUPADA)")
    void deveRegistrarEntradaEAlterarStatusDaVaga() {
        // Simulando que existem vagas
        when(vagaRepository.countByStatus(Vaga.StatusVaga.LIVRE)).thenReturn(10L);

        // Criando uma vaga livre falsa (Mock)
        Vaga vagaLivre = new Vaga();
        vagaLivre.setId(1L);
        vagaLivre.setStatus(Vaga.StatusVaga.LIVRE);
        vagaLivre.setTipo(Vaga.TipoVaga.NORMAL);

        when(vagaRepository.findById(1L)).thenReturn(Optional.of(vagaLivre));

        // Simulando o retorno do save
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Executando o método real
        Ticket ticketGerado = ticketService.registrarEntrada("ABC-1234", "Corolla", 1L, LocalDateTime.now());

        // Verificações
        assertNotNull(ticketGerado);
        assertEquals("ABC-1234", ticketGerado.getPlaca());
        assertEquals(Ticket.StatusTicket.ATIVO, ticketGerado.getStatus());

        // Aqui garantimos o TU04: A vaga mudou para ocupada?
        assertEquals(Vaga.StatusVaga.OCUPADA, vagaLivre.getStatus());

        // Verifica se o método save foi chamado para a vaga e para o ticket
        verify(vagaRepository, times(1)).save(vagaLivre);
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    @DisplayName("TU05 - Deve registrar saída, calcular tarifa e liberar a vaga (Mudar status para LIVRE)")
    void deveRegistrarSaidaCalcularTarifaELiberarVaga() {
        LocalDateTime entrada = LocalDateTime.now().minusHours(2);
        LocalDateTime saida = LocalDateTime.now();

        // Criando uma vaga ocupada falsa
        Vaga vagaOcupada = new Vaga();
        vagaOcupada.setId(1L);
        vagaOcupada.setStatus(Vaga.StatusVaga.OCUPADA);

        // Criando um ticket ativo falso
        Ticket ticketAtivo = new Ticket();
        ticketAtivo.setPlaca("XYZ-9876");
        ticketAtivo.setHoraEntrada(entrada);
        ticketAtivo.setVaga(vagaOcupada);
        ticketAtivo.setStatus(Ticket.StatusTicket.ATIVO);

        // Configurando os Mocks
        when(ticketRepository.findByPlacaAndStatus("XYZ-9876", Ticket.StatusTicket.ATIVO))
                .thenReturn(Optional.of(ticketAtivo));

        when(tarifaService.calcularValor(entrada, saida)).thenReturn(new BigDecimal("20.00"));

        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Executando o método real
        Ticket ticketConcluido = ticketService.registrarSaida("XYZ-9876", saida);

        // Verificações
        assertEquals(Ticket.StatusTicket.CONCLUIDO, ticketConcluido.getStatus());
        assertEquals(new BigDecimal("20.00"), ticketConcluido.getValorPago());
        assertEquals(saida, ticketConcluido.getHoraSaida());

        // Aqui garantimos o TU05: A vaga voltou a ficar livre?
        assertEquals(Vaga.StatusVaga.LIVRE, vagaOcupada.getStatus());

        // Verifica se os salvamentos ocorreram
        verify(vagaRepository, times(1)).save(vagaOcupada);
        verify(ticketRepository, times(1)).save(ticketAtivo);
    }
}