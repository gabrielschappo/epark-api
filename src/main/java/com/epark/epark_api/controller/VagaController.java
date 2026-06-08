package com.epark.epark_api.controller;

import com.epark.epark_api.domain.Vaga;
import com.epark.epark_api.dto.ResumoVagasDTO;
import com.epark.epark_api.service.VagaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.epark.dto.GerarVagasDTO;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/vagas")
@CrossOrigin(origins = "*") // Permite que o seu front-end local consuma a API sem erros de CORS
public class VagaController {

    private final VagaService vagaService;

    public VagaController(VagaService vagaService) {
        this.vagaService = vagaService;
    }

    // Retorna a lista completa para desenhar o grid (A1, A2, etc.)
    @GetMapping
    public ResponseEntity<List<Vaga>> listarTodas() {
        return ResponseEntity.ok(vagaService.listarTodas());
    }

    // Alimenta os 4 cartões do topo (Totais, Ocupadas, Livres, Especiais)
    @GetMapping("/resumo")
    public ResponseEntity<ResumoVagasDTO> obterResumo() {
        return ResponseEntity.ok(vagaService.obterResumoDashboard());
    }

    // Permite buscar os detalhes de uma vaga específica clicando nela
    @GetMapping("/{identificador}")
    public ResponseEntity<Vaga> buscarPorIdentificador(@PathVariable String identificador) {
        return ResponseEntity.ok(vagaService.buscarPorIdentificador(identificador));
    }

    // Permite mudar o status manualmente (Ex: colocar em MANUTENCAO)
    @PutMapping("/{id}/status")
    public ResponseEntity<Vaga> alterarStatus(@PathVariable Long id, @RequestParam Vaga.StatusVaga novoStatus) {
        return ResponseEntity.ok(vagaService.alterarStatusManual(id, novoStatus));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirVaga(@PathVariable Long id) {
        vagaService.excluirVaga(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/lote")
    public ResponseEntity<String> gerarVagasEmLote(@Valid @RequestBody GerarVagasDTO dto) {
        vagaService.gerarVagasEmLote(dto);
        return ResponseEntity.ok("Vagas geradas com sucesso!");
    }
}