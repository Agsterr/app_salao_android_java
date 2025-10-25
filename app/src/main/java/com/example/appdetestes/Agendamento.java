package com.example.appdetestes;

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
}
