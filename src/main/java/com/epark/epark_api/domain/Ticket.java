package com.epark.epark_api.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Data
@Table(name = "tb_tickets")
@NoArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // A anotação @Pattern já ajuda a validar o TU01 (Validação de Formato de Placa)
    @NotBlank(message = "A placa é obrigatória")
    @Pattern(regexp = "^[A-Z]{3}-?[0-9][A-Z0-9][0-9]{2}$", message = "Formato de placa inválido")
    @Column(nullable = false, length = 8)
    private String placa;

    // Ex: "Corolla Cinza"
    @NotBlank(message = "O modelo do veículo é obrigatório")
    @Column(nullable = false, length = 50)
    private String modeloVeiculo;

    // Relacionamento: Vários tickets podem ser emitidos para a mesma vaga ao longo do tempo,
    // mas um ticket específico está ligado a apenas uma vaga.
    @NotNull(message = "A vaga é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaga_id", nullable = false)
    private Vaga vaga;

    @NotNull(message = "A hora de entrada é obrigatória")
    @Column(nullable = false)
    private LocalDateTime horaEntrada;

    private LocalDateTime horaSaida;

    // Usamos BigDecimal para lidar com dinheiro (evita problemas de arredondamento do Double)
    @Column(precision = 10, scale = 2)
    private BigDecimal valorPago;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTicket status = StatusTicket.ATIVO;

    public enum StatusTicket {
        ATIVO,      // Veículo ainda está no estacionamento
        CONCLUIDO   // Veículo já pagou e saiu
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ticket ticket = (Ticket) o;
        return Objects.equals(id, ticket.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
