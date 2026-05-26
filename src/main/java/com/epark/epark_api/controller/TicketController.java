package com.epark.epark_api.controller;


import com.epark.epark_api.domain.Ticket;
import com.epark.epark_api.dto.TicketEntradaDTO;
import com.epark.epark_api.dto.TicketSaidaDTO;
import com.epark.epark_api.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    // Endpoint acionado quando o operador clica em "Registrar Entrada"
    @PostMapping("/entrada")
    public ResponseEntity<?> registrarEntrada(@RequestBody TicketEntradaDTO dto) {
        try {
            // O LocalDateTime.now() captura o exato momento em que o servidor recebe a requisição
            Ticket ticket = ticketService.registrarEntrada(
                    dto.placa(),
                    dto.modeloVeiculo(),
                    dto.idVaga(),
                    LocalDateTime.now()
            );
            return ResponseEntity.ok(ticket);

        } catch (IllegalStateException | IllegalArgumentException e) {
            // Se o estacionamento estiver lotado ou a vaga ocupada, devolvemos o erro 400 (Bad Request)
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint acionado quando o veículo vai pagar e sair
    @PutMapping("/saida")
    public ResponseEntity<?> registrarSaida(@RequestBody TicketSaidaDTO dto) {
        try {
            Ticket ticket = ticketService.registrarSaida(dto.placa(), LocalDateTime.now());
            return ResponseEntity.ok(ticket);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
