package com.focodevsistemas.gerenciamento;

public class Agendamento {

    private long id;
    private long clienteId;
    private long servicoId;
    private long dataHoraInicio; // Armazenado como timestamp (milissegundos)
    private double valor;

    // Campos extras para facilitar a exibição (não são colunas no DB)
    private String nomeCliente;
    private String nomeServico;
    private int tempoServico;

    // Novo: controle de cancelamento
    private int cancelado; // 0 = não cancelado, 1 = cancelado
    // Novo: controle de finalização
    private int finalizado; // 0 = não finalizado, 1 = finalizado

    public Agendamento() {
    }

    // Getters e Setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getClienteId() {
        return clienteId;
    }

    public void setClienteId(long clienteId) {
        this.clienteId = clienteId;
    }

    public long getServicoId() {
        return servicoId;
    }

    public void setServicoId(long servicoId) {
        this.servicoId = servicoId;
    }

    public long getDataHoraInicio() {
        return dataHoraInicio;
    }

    public void setDataHoraInicio(long dataHoraInicio) {
        this.dataHoraInicio = dataHoraInicio;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public String getNomeServico() {
        return nomeServico;
    }

    public void setNomeServico(String nomeServico) {
        this.nomeServico = nomeServico;
    }

    public int getTempoServico() {
        return tempoServico;
    }

    public void setTempoServico(int tempoServico) {
        this.tempoServico = tempoServico;
    }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public int getCancelado() { return cancelado; }
    public void setCancelado(int cancelado) { this.cancelado = cancelado; }
    public boolean isCancelado() { return cancelado == 1; }

    public int getFinalizado() { return finalizado; }
    public void setFinalizado(int finalizado) { this.finalizado = finalizado; }
    public boolean isFinalizado() { return finalizado == 1; }

    // Status calculado dinamicamente
    public String getStatus() {
        if (isCancelado()) return "Cancelado";
        if (isFinalizado()) return "Finalizado";
        long agora = System.currentTimeMillis();
        long inicio = dataHoraInicio;
        long fim = inicio + (tempoServico * 60000L);
        if (agora < inicio) return "Na fila";
        if (agora >= inicio && agora <= fim) return "Em andamento";
        return "Finalizado"; // passou do fim e não está cancelado
    }
}
