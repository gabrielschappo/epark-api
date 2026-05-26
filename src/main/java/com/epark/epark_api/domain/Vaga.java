package com.epark.epark_api.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
@Table(name = "tb_vagas")
public class Vaga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ex: "A4", "B2"
    @NotBlank(message = "O identificador da vaga é obrigatório")
    @Column(nullable = false, unique = true, length = 10)
    private String identificador;

    // Ex: "Setor A1 (Subsolo)"
    @Column(length = 50)
    private String setor;

    @NotNull(message = "O tipo da vaga é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoVaga tipo;

    @NotNull(message = "O status da vaga é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusVaga status = StatusVaga.LIVRE; // Valor padrão ao criar

    public enum TipoVaga {
        NORMAL,
        PCD,        // Vagas para pessoas com deficiência
        ELETRICA,   // Vagas com carregador
        MOTO
    }

    public enum StatusVaga {
        LIVRE,
        OCUPADA,
        MANUTENCAO
    }

    // equals e hashCode (Importante para comparações corretas de ID no Hibernate)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vaga vaga = (Vaga) o;
        return Objects.equals(id, vaga.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}