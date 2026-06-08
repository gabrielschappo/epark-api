package com.epark.epark_api.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErroResposta(String mensagem, List<String> detalhes) {
        ErroResposta(String mensagem) {
            this(mensagem, List.of());
        }
    }

    // Erros de validação do @Valid (campos inválidos nos DTOs)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> handleValidation(MethodArgumentNotValidException ex) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest().body(new ErroResposta("Dados inválidos", detalhes));
    }

    // Regras de negócio violadas (vaga ocupada, placa duplicada, estacionamento lotado, etc.)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErroResposta> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.badRequest().body(new ErroResposta(ex.getMessage()));
    }

    // Recursos não encontrados (vaga, ticket)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResposta> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErroResposta(ex.getMessage()));
    }

    // Violação de constraint única no banco (ex: identificador de vaga duplicado)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> handleDataIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErroResposta("Não foi possível salvar: já existe um registro com estes dados."));
    }
}
