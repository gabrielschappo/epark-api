package com.epark.epark_api.repository;

import com.epark.epark_api.domain.Vaga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VagaRepository extends JpaRepository<Vaga, Long> {

    // Busca uma vaga exata pelo identificador (Ex: "A4")
    Optional<Vaga> findByIdentificador(String identificador);

    // Retorna uma lista de vagas dependendo do status (útil para o Dashboard)
    List<Vaga> findByStatus(Vaga.StatusVaga status);

    // Conta quantas vagas existem em um determinado status (Ex: Quantas estão LIVRES?)
    long countByStatus(Vaga.StatusVaga status);
}
