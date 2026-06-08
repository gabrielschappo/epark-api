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
                '<button class="btn btn-sm btn-outline-dark me-1" onclick="abrirModalStatus(' + v.id + ', \'' + v.identificador + '\', \'' + v.status + '\')">✏️ Alterar</button>' +
                '<button class="btn btn-sm btn-outline-danger" onclick="excluirVaga(' + v.id + ', \'' + v.identificador + '\')" ' + (v.status === 'OCUPADA' ? 'disabled title="Vaga ocupada"' : '') + '>🗑️ Excluir</button>' +
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

function excluirVaga(id, identificador) {
    if (!confirm('Tem certeza que deseja excluir a vaga ' + identificador + '? Esta ação não pode ser desfeita.')) {
        return;
    }

    var xhr = new XMLHttpRequest();
    xhr.open('DELETE', baseUrl + '/' + id, true);

    xhr.onload = function() {
        if (xhr.status === 204) {
            carregarVagas();
        } else {
            var resposta = JSON.parse(xhr.responseText);
            alert('Erro ao excluir: ' + (resposta.mensagem || xhr.responseText));
        }
    };

    xhr.send();
}

function enviarLoteVagas() {
    var setor = document.getElementById('input-setor').value;
    var prefixo = document.getElementById('input-prefixo').value.toUpperCase();
    var qtdNormal = parseInt(document.getElementById('input-qtd-normal').value) || 0;
    var qtdPcd = parseInt(document.getElementById('input-qtd-pcd').value) || 0;
    var qtdEletrica = parseInt(document.getElementById('input-qtd-eletrica').value) || 0;

    if (!setor || !prefixo) {
        alert('Por favor, preencha o Nome do Setor e o Prefixo.');
        return;
    }

    if (qtdNormal === 0 && qtdPcd === 0 && qtdEletrica === 0) {
        alert('Você precisa gerar pelo menos 1 vaga de algum tipo.');
        return;
    }

    var payload = {
        setor: setor,
        prefixo: prefixo,
        qtdNormal: qtdNormal,
        qtdPcd: qtdPcd,
        qtdEletrica: qtdEletrica
    };

    var xhr = new XMLHttpRequest();
    xhr.open('POST', baseUrl + '/lote', true);
    xhr.setRequestHeader('Content-Type', 'application/json;charset=UTF-8');
    
    xhr.onload = function() {
        if (xhr.status === 200 || xhr.status === 201) {
            alert('Vagas geradas com sucesso!');
            
            // Fecha o modal do Bootstrap
            var modalEl = document.getElementById('modalGerarVagas');
            var modalObj = bootstrap.Modal.getInstance(modalEl);
            if (modalObj) {
                modalObj.hide();
            }
            
            // Recarrega a tabela para mostrar as vagas novas
            carregarVagas();
        } else {
            alert('Erro ao gerar vagas: ' + xhr.responseText);
        }
    };
    
    xhr.send(JSON.stringify(payload));
}

// Inicializa a página
window.onload = function() {
    carregarVagas();
};