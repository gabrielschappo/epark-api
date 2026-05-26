package com.epark.epark_api;

import com.epark.epark_api.domain.Vaga;
import com.epark.epark_api.dto.ResumoVagasDTO;
import com.epark.epark_api.repository.VagaRepository;
import com.epark.epark_api.service.VagaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VagaServiceTest {

    @Mock
    private VagaRepository vagaRepository;

    @InjectMocks
    private VagaService vagaService;

    @Test
    @DisplayName("Deve retornar os totais corretos para o Dashboard de Vagas")
    void deveRetornarResumoDashboard() {
        // Criando dados falsos para simular as vagas do banco de dados
        Vaga v1 = new Vaga(); v1.setTipo(Vaga.TipoVaga.NORMAL); v1.setStatus(Vaga.StatusVaga.OCUPADA);
        Vaga v2 = new Vaga(); v2.setTipo(Vaga.TipoVaga.PCD); v2.setStatus(Vaga.StatusVaga.LIVRE);
        Vaga v3 = new Vaga(); v3.setTipo(Vaga.TipoVaga.ELETRICA); v3.setStatus(Vaga.StatusVaga.LIVRE);
        Vaga v4 = new Vaga(); v4.setTipo(Vaga.TipoVaga.NORMAL); v4.setStatus(Vaga.StatusVaga.MANUTENCAO);

        // Simulando o comportamento do banco
        when(vagaRepository.count()).thenReturn(4L);
        when(vagaRepository.countByStatus(Vaga.StatusVaga.LIVRE)).thenReturn(2L);
        when(vagaRepository.countByStatus(Vaga.StatusVaga.OCUPADA)).thenReturn(1L);
        when(vagaRepository.findAll()).thenReturn(Arrays.asList(v1, v2, v3, v4));

        // Executando o método real
        ResumoVagasDTO resumo = vagaService.obterResumoDashboard();

        // Validando se os cartões do dashboard receberão a matemática correta
        assertEquals(4L, resumo.totais());
        assertEquals(2L, resumo.livres());
        assertEquals(1L, resumo.ocupadas());
        // PCD e Elétrica devem somar 2 vagas especiais
        assertEquals(2L, resumo.especiais());
    }

    @Test
    @DisplayName("Deve alterar o status de uma vaga para Manutenção com sucesso")
    void deveAlterarStatusManualComSucesso() {
        Vaga vaga = new Vaga();
        vaga.setId(1L);
        vaga.setIdentificador("A10");
        vaga.setStatus(Vaga.StatusVaga.LIVRE);

        // Retorna a vaga criada acima quando o método findById for chamado
        when(vagaRepository.findById(1L)).thenReturn(Optional.of(vaga));
        // Simula o retorno do save
        when(vagaRepository.save(any(Vaga.class))).thenAnswer(i -> i.getArgument(0));

        Vaga vagaAtualizada = vagaService.alterarStatusManual(1L, Vaga.StatusVaga.MANUTENCAO);

        assertEquals(Vaga.StatusVaga.MANUTENCAO, vagaAtualizada.getStatus());
        verify(vagaRepository, times(1)).save(vaga);
    }

    @Test
    @DisplayName("Deve lançar exceção ao cadastrar vaga com identificador já existente")
    void deveLancarExcecaoAoCadastrarVagaDuplicada() {
        Vaga vaga = new Vaga();
        vaga.setIdentificador("A1");

        // Simula que o banco já encontrou uma vaga com o identificador "A1"
        when(vagaRepository.findByIdentificador("A1")).thenReturn(Optional.of(new Vaga()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            vagaService.cadastrarVaga(vaga);
        });

        assertEquals("Já existe uma vaga com este identificador.", exception.getMessage());
        // Garante que o sistema travou a operação antes de tentar salvar
        verify(vagaRepository, never()).save(any(Vaga.class));
    }

    @Test
    @DisplayName("Deve lançar exceção se tentar alterar status de vaga inexistente")
    void deveLancarExcecaoAoAlterarStatusDeVagaQueNaoExiste() {
        when(vagaRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            vagaService.alterarStatusManual(99L, Vaga.StatusVaga.MANUTENCAO);
        });

        assertEquals("Vaga não encontrada.", exception.getMessage());
    }
}