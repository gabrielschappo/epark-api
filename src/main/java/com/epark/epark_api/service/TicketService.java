package com.epark.epark_api.service;

import com.epark.epark_api.domain.Ticket;
import com.epark.epark_api.domain.Vaga;
import com.epark.epark_api.repository.TicketRepository;
import com.epark.epark_api.repository.VagaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import java.time.LocalDateTime;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final VagaRepository vagaRepository;
    private final TarifaService tarifaService;

    // A injeção de dependências via construtor é a mais recomendada no Spring
    public TicketService(TicketRepository ticketRepository,
                         VagaRepository vagaRepository,
                         TarifaService tarifaService) {
        this.ticketRepository = ticketRepository;
        this.vagaRepository = vagaRepository;
        this.tarifaService = tarifaService;
    }

    @Transactional
    public Ticket registrarEntrada(String placa, String modeloVeiculo, Long idVaga, LocalDateTime horaEntrada) {
        // Impede entrada duplicada para a mesma placa
        if (ticketRepository.findByPlacaAndStatus(placa, Ticket.StatusTicket.ATIVO).isPresent()) {
            throw new IllegalStateException("Já existe um ticket ativo para a placa " + placa + ". Registre a saída antes de uma nova entrada.");
        }

        // TU06 - Bloqueio por Lotação Máxima
        long vagasDisponiveis = vagaRepository.countByStatus(Vaga.StatusVaga.LIVRE);
        if (vagasDisponiveis == 0) {
            throw new IllegalStateException("Estacionamento Lotado");
        }

        // Verifica se a vaga existe e está livre
        Vaga vaga = vagaRepository.findById(idVaga)
                .orElseThrow(() -> new IllegalArgumentException("Vaga não encontrada"));

        if (vaga.getStatus() != Vaga.StatusVaga.LIVRE) {
            throw new IllegalStateException("A vaga selecionada não está livre");
        }

        // TU04 - Decremento de Vagas Disponíveis (ao mudar o status, a contagem de LIVRES cai) [cite: 50]
        vaga.setStatus(Vaga.StatusVaga.OCUPADA);
        vagaRepository.save(vaga);

        // Cria e salva o ticket
        Ticket ticket = new Ticket();
        ticket.setPlaca(placa);
        ticket.setModeloVeiculo(modeloVeiculo);
        ticket.setVaga(vaga);
        ticket.setHoraEntrada(horaEntrada);
        ticket.setStatus(Ticket.StatusTicket.ATIVO);

        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket registrarSaida(String placa, LocalDateTime horaSaida) {
        // Busca o veículo que ainda está no estacionamento
        Ticket ticket = ticketRepository.findByPlacaAndStatus(placa, Ticket.StatusTicket.ATIVO)
                .orElseThrow(() -> new IllegalArgumentException("Nenhum ticket ativo encontrado para esta placa"));

        // Consistência de Data/Hora (Apoia o TU02) [cite: 46]
        if (horaSaida.isBefore(ticket.getHoraEntrada())) {
            throw new IllegalArgumentException("A hora de saída não pode ser anterior à hora de entrada.");
        }

        // Calcula o valor via TarifaService
        var valorAPagar = tarifaService.calcularValor(ticket.getHoraEntrada(), horaSaida);

        // Atualiza o ticket
        ticket.setHoraSaida(horaSaida);
        ticket.setValorPago(valorAPagar);
        ticket.setStatus(Ticket.StatusTicket.CONCLUIDO);

        // TU05 - Incremento de Vagas Disponíveis (ao liberar a vaga, a contagem de LIVRES sobe)
        Vaga vaga = ticket.getVaga();
        vaga.setStatus(Vaga.StatusVaga.LIVRE);
        vagaRepository.save(vaga);

        return ticketRepository.save(ticket);
    }

    public List<Ticket> listarTodos() {
        return ticketRepository.findAll();
    }

    public Ticket buscarTicketAtivoPorVaga(Long vagaId) {
        return ticketRepository.findByVagaIdAndStatus(vagaId, Ticket.StatusTicket.ATIVO)
                .orElseThrow(() -> new IllegalArgumentException("Nenhum ticket ativo encontrado para esta vaga."));
    }
}
