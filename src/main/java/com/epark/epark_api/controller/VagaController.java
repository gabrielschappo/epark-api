package com.epark.epark_api.controller;

import com.epark.epark_api.domain.Vaga;
import com.epark.epark_api.dto.ResumoVagasDTO;
import com.epark.epark_api.service.VagaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.epark.dto.GerarVagasDTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

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

    @PostMapping("/lote")
    public ResponseEntity<String> gerarVagasEmLote(@RequestBody GerarVagasDTO dto) {
        try {
            vagaService.gerarVagasEmLote(dto);
            return ResponseEntity.ok("Vagas geradas com sucesso!");
            
        } catch (DataIntegrityViolationException e) {
            // Captura especificamente o erro de tentar salvar dados duplicados (Unique Constraint)
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Não foi possível gerar: Já existem vagas utilizando este prefixo ou numeração.");
            
        } catch (Exception e) {
            // Captura qualquer outro erro inesperado e devolve uma mensagem limpa
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ocorreu um erro interno ao tentar gerar as vagas.");
        }
    }
}