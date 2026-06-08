package com.epark.epark_api.controller;


import com.epark.epark_api.domain.Ticket;
import com.epark.epark_api.dto.TicketEntradaDTO;
import com.epark.epark_api.dto.TicketSaidaDTO;
import com.epark.epark_api.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

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
    public ResponseEntity<Ticket> registrarEntrada(@Valid @RequestBody TicketEntradaDTO dto) {
        Ticket ticket = ticketService.registrarEntrada(
                dto.placa(),
                dto.modeloVeiculo(),
                dto.idVaga(),
                LocalDateTime.now()
        );
        return ResponseEntity.ok(ticket);
    }

    // Endpoint acionado quando o veículo vai pagar e sair
    @PutMapping("/saida")
    public ResponseEntity<Ticket> registrarSaida(@RequestBody TicketSaidaDTO dto) {
        return ResponseEntity.ok(ticketService.registrarSaida(dto.placa(), LocalDateTime.now()));
    }

    @GetMapping
    public ResponseEntity<List<Ticket>> listarTodos() {
        return ResponseEntity.ok(ticketService.listarTodos());
    }
}
