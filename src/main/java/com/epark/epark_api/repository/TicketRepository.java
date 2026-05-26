package com.epark.epark_api.repository;

import com.epark.epark_api.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Busca o ticket que ainda não foi pago de um carro específico pela placa
    Optional<Ticket> findByPlacaAndStatus(String placa, Ticket.StatusTicket status);

    // Lista todos os tickets de um determinado status (Ex: Todos os CONCLUIDOS para o relatório financeiro)
    List<Ticket> findByStatus(Ticket.StatusTicket status);
}
