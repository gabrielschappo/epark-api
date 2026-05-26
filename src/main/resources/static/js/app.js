// ==========================================
// CONFIGURAÇÕES GERAIS
// ==========================================
var baseUrl = '/api/vagas';
var offcanvasVaga = null; // Variável para controlar o painel lateral do Bootstrap


// ==========================================
// 1. CARREGAMENTO INICIAL DO DASHBOARD
// ==========================================

// Busca os números e atualiza os 4 cartões do topo
function carregarResumoDashboard() {
    var xhr = new XMLHttpRequest();
    xhr.open('GET', baseUrl + '/resumo', true);
    
    xhr.onload = function() {
        if (xhr.status === 200) {
            var resumo = JSON.parse(xhr.responseText);
            document.getElementById('txt-vagas-totais').innerText = resumo.totais;
            document.getElementById('txt-vagas-ocupadas').innerText = resumo.ocupadas;
            document.getElementById('txt-vagas-livres').innerText = resumo.livres;
            document.getElementById('txt-vagas-especiais').innerText = resumo.especiais;
        } else {
            console.error('Erro ao buscar o resumo das vagas.');
        }
    };
    xhr.send();
}

// Busca a lista completa de vagas do banco
// Busca a lista completa de vagas do banco e ordena
function carregarMapaDeVagas() {
    var xhr = new XMLHttpRequest();
    xhr.open('GET', baseUrl, true);
    
    xhr.onload = function() {
        if (xhr.status === 200) {
            var vagas = JSON.parse(xhr.responseText);
            
            // Ordena as vagas pelo identificador (A1, A2, A3...) antes de desenhar
            vagas.sort(function(a, b) {
                return a.identificador.localeCompare(b.identificador, undefined, { numeric: true });
            });
            
            renderizarGrid(vagas);
        } else {
            console.error('Erro ao carregar mapa de vagas.');
        }
    };
    xhr.send();
}


// ==========================================
// 2. RENDERIZAÇÃO VISUAL DO MAPA
// ==========================================

// Constrói o HTML de cada vaga e joga na tela
function renderizarGrid(vagas) {
    var container = document.getElementById('grid-vagas');
    container.innerHTML = ''; 
    
    for (var i = 0; i < vagas.length; i++) {
        var vaga = vagas[i];
        
        // Define as cores baseadas no status
        var bgClass = 'bg-success bg-opacity-25'; // Padrão: LIVRE
        var conteudoCentral = '';
        var statusTexto = '';
        
        if (vaga.status === 'OCUPADA') {
            bgClass = 'bg-secondary bg-opacity-25 border border-secondary';
            conteudoCentral = '🚗';
        } else if (vaga.status === 'MANUTENCAO') {
            bgClass = 'bg-danger bg-opacity-25 text-danger';
            statusTexto = 'Manutenção';
        }
        
        // Define os ícones baseados no tipo
        var iconeTipo = '';
        if (vaga.tipo === 'PCD') iconeTipo = '♿';
        if (vaga.tipo === 'ELETRICA') iconeTipo = '⚡';
        
        // Cria a coluna do Grid
        var divCol = document.createElement('div');
        divCol.className = 'col-md-2 mb-3';
        
        // Monta o cartão com evento de clique (passando o identificador da vaga)
        var htmlCard = 
            '<div class="card h-100 ' + bgClass + ' shadow-sm" style="min-height: 120px; cursor: pointer;" onclick="abrirPainelVaga(\'' + vaga.identificador + '\')">' +
                '<div class="card-body p-2 d-flex flex-column justify-content-between">' +
                    '<div class="d-flex justify-content-between">' +
                        '<strong>' + vaga.identificador + '</strong>' +
                        '<span>' + iconeTipo + '</span>' +
                    '</div>' +
                    '<div class="text-center h2 m-0">' + conteudoCentral + '</div>' +
                    '<div class="text-center small fw-bold mt-1">' + statusTexto + '</div>' +
                '</div>' +
            '</div>';
            
        divCol.innerHTML = htmlCard;
        container.appendChild(divCol);
    }
}


// ==========================================
// 3. LÓGICA DO PAINEL LATERAL (ENTRADA/SAÍDA)
// ==========================================

// Abre o painel lateral com o formulário correto
function abrirPainelVaga(identificador) {
    var xhr = new XMLHttpRequest();
    xhr.open('GET', baseUrl + '/' + identificador, true);
    
    xhr.onload = function() {
        if (xhr.status === 200) {
            var vaga = JSON.parse(xhr.responseText);
            var painelConteudo = document.getElementById('conteudoPainelVaga');
            
            // Monta o formulário de ENTRADA se estiver livre
            if (vaga.status === 'LIVRE') {
                painelConteudo.innerHTML = 
                    '<h3 class="mb-1 text-success">Vaga ' + vaga.identificador + '</h3>' +
                    '<p class="text-muted">Preencha os dados para registrar a entrada.</p>' +
                    '<hr>' +
                    '<div class="mb-3">' +
                        '<label class="form-label fw-bold">Placa do Veículo</label>' +
                        '<input type="text" id="inputPlaca" class="form-control text-uppercase" placeholder="ABC-1234" maxlength="8">' +
                    '</div>' +
                    '<div class="mb-4">' +
                        '<label class="form-label fw-bold">Modelo / Cor</label>' +
                        '<input type="text" id="inputModelo" class="form-control" placeholder="Ex: Corolla Prata">' +
                    '</div>' +
                    '<button class="btn btn-success w-100 py-2 fw-bold" onclick="registrarEntrada(' + vaga.id + ')">Confirmar Entrada</button>';
            } 
            // Monta o formulário de SAÍDA se estiver ocupada
            else if (vaga.status === 'OCUPADA') {
                painelConteudo.innerHTML = 
                    '<h3 class="mb-1 text-secondary">Vaga ' + vaga.identificador + '</h3>' +
                    '<span class="badge bg-secondary mb-3">Ocupada</span>' +
                    '<hr>' +
                    '<p>Para liberar esta vaga e gerar a cobrança, confirme a placa do veículo que está saindo:</p>' +
                    '<div class="mb-4">' +
                        '<label class="form-label fw-bold">Placa do Veículo</label>' +
                        '<input type="text" id="inputPlacaSaida" class="form-control text-uppercase" placeholder="ABC-1234" maxlength="8">' +
                    '</div>' +
                    '<button class="btn btn-primary w-100 py-2 fw-bold" onclick="registrarSaida()">Calcular e Liberar Vaga</button>';
            }
            // Bloqueia ações se estiver em manutenção
            else {
                painelConteudo.innerHTML = '<div class="alert alert-warning border-warning fw-bold text-center mt-3">🚧<br>Esta vaga está em manutenção e não pode ser utilizada no momento.</div>';
            }

            // Inicializa e exibe o painel do Bootstrap
            if (!offcanvasVaga) {
                offcanvasVaga = new bootstrap.Offcanvas(document.getElementById('painelVaga'));
            }
            offcanvasVaga.show();
        }
    };
    xhr.send();
}

// ==========================================
// 4. COMUNICAÇÃO COM A API DE TICKETS
// ==========================================

function registrarEntrada(idVaga) {
    var placa = document.getElementById('inputPlaca').value.toUpperCase();
    var modelo = document.getElementById('inputModelo').value;

    if (!placa || !modelo) {
        alert('Por favor, preencha a placa e o modelo!');
        return;
    }

    var xhr = new XMLHttpRequest();
    xhr.open('POST', '/api/tickets/entrada', true);
    xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    
    xhr.onload = function() {
        if (xhr.status === 200) {
            alert('Entrada registrada com sucesso!');
            offcanvasVaga.hide(); 
            
            // Atualiza os dados na tela em tempo real
            carregarResumoDashboard();
            carregarMapaDeVagas();
        } else {
            alert('Erro ao registrar entrada: ' + xhr.responseText);
        }
    };
    
    xhr.send(JSON.stringify({
        placa: placa,
        modeloVeiculo: modelo,
        idVaga: idVaga
    }));
}

function registrarSaida() {
    var placa = document.getElementById('inputPlacaSaida').value.toUpperCase();

    if (!placa) {
        alert('Por favor, informe a placa para registrar a saída!');
        return;
    }

    var xhr = new XMLHttpRequest();
    xhr.open('PUT', '/api/tickets/saida', true);
    xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    
    xhr.onload = function() {
        if (xhr.status === 200) {
            var ticket = JSON.parse(xhr.responseText);
            
            // Alerta simulando o sistema de caixa cobrando o valor
            alert('Saída registrada com sucesso!\n\nVeículo liberado.\nValor a pagar: R$ ' + ticket.valorPago.toFixed(2).replace('.', ','));
            
            offcanvasVaga.hide();
            
            // Atualiza os dados na tela em tempo real
            carregarResumoDashboard();
            carregarMapaDeVagas();
        } else {
            alert('Erro ao registrar saída: ' + xhr.responseText);
        }
    };
    
    xhr.send(JSON.stringify({ placa: placa }));
}

// ==========================================
// 5. INICIALIZAÇÃO
// ==========================================

// Executa as buscas assim que o arquivo HTML terminar de carregar
window.onload = function() {
    carregarResumoDashboard();
    carregarMapaDeVagas();
};