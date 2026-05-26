var baseUrl = '/api/vagas';
var modalBootstrap = null;

// Carrega todas as vagas para a tabela
function carregarVagas() {
    var xhr = new XMLHttpRequest();
    xhr.open('GET', baseUrl, true);
    
    xhr.onload = function() {
        if (xhr.status === 200) {
            var vagas = JSON.parse(xhr.responseText);
            
            // Ordena alfabeticamente/numericamente (A1, A2, A10)
            vagas.sort(function(a, b) {
                return a.identificador.localeCompare(b.identificador, undefined, { numeric: true });
            });
            
            preencherTabelaVagas(vagas);
        } else {
            console.error('Erro ao carregar as vagas.');
        }
    };
    
    xhr.send();
}

function preencherTabelaVagas(vagas) {
    var tbody = document.getElementById('tabela-vagas');
    tbody.innerHTML = '';
    
    for (var i = 0; i < vagas.length; i++) {
        var v = vagas[i];
        
        // Formatação visual da etiqueta de estado
        var badgeClass = 'bg-success';
        if (v.status === 'OCUPADA') badgeClass = 'bg-secondary';
        if (v.status === 'MANUTENCAO') badgeClass = 'bg-danger';
        
        var tr = document.createElement('tr');
        
        var htmlLinha = 
            '<td>' + v.id + '</td>' +
            '<td class="fw-bold">' + v.identificador + '</td>' +
            '<td>' + v.setor + '</td>' +
            '<td>' + v.tipo + '</td>' +
            '<td><span class="badge ' + badgeClass + '">' + v.status + '</span></td>' +
            '<td class="text-center">' +
                '<button class="btn btn-sm btn-outline-dark" onclick="abrirModalStatus(' + v.id + ', \'' + v.identificador + '\', \'' + v.status + '\')">✏️ Alterar</button>' +
            '</td>';
            
        tr.innerHTML = htmlLinha;
        tbody.appendChild(tr);
    }
}

// Abre a janela para escolher o novo estado
function abrirModalStatus(id, identificador, statusAtual) {
    document.getElementById('modal-id-vaga').value = id;
    document.getElementById('modal-identificador-vaga').innerText = identificador;
    document.getElementById('select-novo-status').value = statusAtual;
    
    if (!modalBootstrap) {
        modalBootstrap = new bootstrap.Modal(document.getElementById('modalStatus'));
    }
    modalBootstrap.show();
}

// Envia a requisição PUT para o Back-end
function guardarNovoEstado() {
    var idVaga = document.getElementById('modal-id-vaga').value;
    var novoStatus = document.getElementById('select-novo-status').value;
    
    var xhr = new XMLHttpRequest();
    // O endpoint espera um parâmetro na URL: /api/vagas/{id}/status?novoStatus=LIVRE
    xhr.open('PUT', baseUrl + '/' + idVaga + '/status?novoStatus=' + novoStatus, true);
    
    xhr.onload = function() {
        if (xhr.status === 200) {
            alert('Estado da vaga alterado com sucesso!');
            modalBootstrap.hide();
            carregarVagas(); // Recarrega a tabela para mostrar a alteração
        } else {
            alert('Erro ao alterar estado: ' + xhr.responseText);
        }
    };
    
    xhr.send();
}

// Inicializa a página
window.onload = function() {
    carregarVagas();
};