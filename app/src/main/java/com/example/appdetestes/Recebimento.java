package com.example.appdetestes;

public class Recebimento {
    private long id;
    private long vendaId;
    private int numeroParcela;
    private double valor;
    private long dataPrevista; // timestamp
    private int status; // 0=A_RECEBER, 1=PAGO
    private Long dataPagamento; // timestamp nullable

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getVendaId() { return vendaId; }
    public void setVendaId(long vendaId) { this.vendaId = vendaId; }
    public int getNumeroParcela() { return numeroParcela; }
    public void setNumeroParcela(int numeroParcela) { this.numeroParcela = numeroParcela; }
    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
    public long getDataPrevista() { return dataPrevista; }
    public void setDataPrevista(long dataPrevista) { this.dataPrevista = dataPrevista; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public Long getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(Long dataPagamento) { this.dataPagamento = dataPagamento; }
}