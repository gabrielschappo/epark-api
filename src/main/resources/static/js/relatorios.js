var urlTickets = '/api/tickets';

function carregarRelatorios() {
    var xhr = new XMLHttpRequest();
    xhr.open('GET', urlTickets, true);
    
    xhr.onload = function() {
        if (xhr.status === 200) {
            var tickets = JSON.parse(xhr.responseText);
            preencherTabela(tickets);
        } else {
            console.error('Erro ao carregar o histórico de tickets.');
        }
    };
    
    xhr.send();
}

function preencherTabela(tickets) {
    var corpoTabela = document.getElementById('tabela-relatorios');
    corpoTabela.innerHTML = '';
    
    for (var i = 0; i < tickets.length; i++) {
        var t = tickets[i];
        
        // Formata os dados para evitar que fiquem nulos no ecrã
        var placa = t.placa ? t.placa : '-';
        var modelo = t.modeloVeiculo ? t.modeloVeiculo : '-';
        
        // Trata as datas, verificando se o veículo já saiu
        var dataEnt = t.dataEntrada ? formatarData(t.dataEntrada) : '-';
        var dataSai = t.dataSaida ? formatarData(t.dataSaida) : '<span class="badge bg-warning text-dark">Em curso</span>';
        
        // Trata o valor pago
        var valor = t.valorPago !== null ? t.valorPago.toFixed(2).replace('.', ',') : '-';
        
        var tr = document.createElement('tr');
        
        var htmlLinha = 
            '<td>' + t.id + '</td>' +
            '<td class="fw-bold">' + placa + '</td>' +
            '<td>' + modelo + '</td>' +
            '<td>' + dataEnt + '</td>' +
            '<td>' + dataSai + '</td>' +
            '<td class="fw-bold text-success">R$ ' + valor + '</td>';
            
        tr.innerHTML = htmlLinha;
        corpoTabela.appendChild(tr);
    }
}

// Função auxiliar para deixar a data amigável (Ex: 26/05/2026 14:30)
function formatarData(dataIso) {
    if (!dataIso) return '';
    var data = new Date(dataIso);
    
    var dia = ("0" + data.getDate()).slice(-2);
    var mes = ("0" + (data.getMonth() + 1)).slice(-2);
    var ano = data.getFullYear();
    
    var hora = ("0" + data.getHours()).slice(-2);
    var minuto = ("0" + data.getMinutes()).slice(-2);
    
    return dia + '/' + mes + '/' + ano + ' ' + hora + ':' + minuto;
}

window.onload = function() {
    carregarRelatorios();
};