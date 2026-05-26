package com.epark.epark_api.service;

import com.epark.epark_api.domain.Vaga;
import com.epark.epark_api.dto.ResumoVagasDTO;
import com.epark.epark_api.repository.VagaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VagaService {

    private final VagaRepository vagaRepository;

    public VagaService(VagaRepository vagaRepository) {
        this.vagaRepository = vagaRepository;
    }

    // Retorna todas as vagas para montar o Grid visual (A1, A2, etc)
    public List<Vaga> listarTodas() {
        return vagaRepository.findAll();
    }

    // Retorna os dados de uma vaga específica (para abrir o painel lateral da vaga "A4", por exemplo)
    public Vaga buscarPorIdentificador(String identificador) {
        return vagaRepository.findByIdentificador(identificador)
                .orElseThrow(() -> new IllegalArgumentException("Vaga " + identificador + " não encontrada no sistema."));
    }

    // Calcula os números dos 4 cartões do topo do seu Dashboard
    public ResumoVagasDTO obterResumoDashboard() {
        long totais = vagaRepository.count();
        long livres = vagaRepository.countByStatus(Vaga.StatusVaga.LIVRE);
        long ocupadas = vagaRepository.countByStatus(Vaga.StatusVaga.OCUPADA);

        // Especiais (PCD, Elétrica, etc).
        // Buscamos todas e filtramos para somar os tipos especiais.
        List<Vaga> todasAsVagas = vagaRepository.findAll();
        long especiais = todasAsVagas.stream()
                .filter(v -> v.getTipo() == Vaga.TipoVaga.PCD || v.getTipo() == Vaga.TipoVaga.ELETRICA)
                .count();

        return new ResumoVagasDTO(totais, ocupadas, livres, especiais);
    }

    // Útil para a funcionalidade de colocar uma vaga em "Manutenção"
    @Transactional
    public Vaga alterarStatusManual(Long idVaga, Vaga.StatusVaga novoStatus) {
        Vaga vaga = vagaRepository.findById(idVaga)
                .orElseThrow(() -> new IllegalArgumentException("Vaga não encontrada."));

        vaga.setStatus(novoStatus);
        return vagaRepository.save(vaga);
    }

    // Método para cadastrar as vagas inicialmente (Setup do banco)
    @Transactional
    public Vaga cadastrarVaga(Vaga vaga) {
        if (vagaRepository.findByIdentificador(vaga.getIdentificador()).isPresent()) {
            throw new IllegalArgumentException("Já existe uma vaga com este identificador.");
        }
        return vagaRepository.save(vaga);
    }
}
