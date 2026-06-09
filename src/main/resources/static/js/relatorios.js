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

        var placa = t.placa || '-';
        var modelo = t.modeloVeiculo || '-';

        var dataEnt = t.horaEntrada ? formatarData(t.horaEntrada) : '-';
        var dataSai = t.horaSaida ? formatarData(t.horaSaida) : '<span class="badge bg-warning text-dark">Em curso</span>';

        var valorCelula;
        if (t.valorPago !== null && t.valorPago !== undefined) {
            valorCelula = '<span class="fw-bold text-success">R$ ' + t.valorPago.toFixed(2).replace('.', ',') + '</span>';
        } else if (t.horaEntrada) {
            var estimado = calcularValorEstimado(t.horaEntrada);
            valorCelula = '<span class="text-warning fw-bold" title="Valor estimado até agora">~R$ ' + estimado + '</span>';
        } else {
            valorCelula = '-';
        }

        var tr = document.createElement('tr');
        tr.innerHTML =
            '<td>' + t.id + '</td>' +
            '<td class="fw-bold">' + placa + '</td>' +
            '<td>' + modelo + '</td>' +
            '<td>' + dataEnt + '</td>' +
            '<td>' + dataSai + '</td>' +
            '<td>' + valorCelula + '</td>';

        corpoTabela.appendChild(tr);
    }
}

// Mesma lógica do TarifaService: tolerância de 15 min, arredonda hora para cima, R$ 10/h
function calcularValorEstimado(horaEntradaIso) {
    var entrada = new Date(horaEntradaIso);
    var agora = new Date();
    var minutos = Math.floor((agora - entrada) / 60000);
    if (minutos <= 15) return '0,00';
    var horas = Math.ceil(minutos / 60);
    return (horas * 10).toFixed(2).replace('.', ',');
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