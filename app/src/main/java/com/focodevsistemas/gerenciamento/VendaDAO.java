package com.focodevsistemas.gerenciamento;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class VendaDAO {
    public static final int TIPO_AVISTA = 0;
    public static final int TIPO_APRAZO = 1;

    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public VendaDAO(Context context) { dbHelper = new DatabaseHelper(context); }
    public void open() { db = dbHelper.getWritableDatabase(); }
    public void close() { dbHelper.close(); }

    public long inserirVenda(Venda venda) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_VENDA_PRODUTO_ID, venda.getProdutoId());
        values.put(DatabaseHelper.COLUMN_VENDA_CLIENTE_ID, venda.getClienteId());
        values.put(DatabaseHelper.COLUMN_VENDA_DATA_VENDA, venda.getDataVenda());
        values.put(DatabaseHelper.COLUMN_VENDA_TIPO_PAGAMENTO, venda.getTipoPagamento());
        values.put(DatabaseHelper.COLUMN_VENDA_VALOR_TOTAL, venda.getValorTotal());
        values.put(DatabaseHelper.COLUMN_VENDA_OBSERVACAO, venda.getObservacao());
        return db.insert(DatabaseHelper.TABLE_VENDAS, null, values);
    }

    public long registrarVendaAVista(long produtoId, double valorTotal, long dataVenda, long clienteId, String observacao) {
        Venda venda = new Venda();
        venda.setProdutoId(produtoId);
        venda.setClienteId(clienteId);
        venda.setDataVenda(dataVenda);
        venda.setTipoPagamento(TIPO_AVISTA);
        venda.setValorTotal(valorTotal);
        venda.setObservacao(observacao);
        long vendaId = inserirVenda(venda);
        criarRecebimentoAVista(vendaId, valorTotal, dataVenda);
        return vendaId;
    }

    private void criarRecebimentoAVista(long vendaId, double valor, long dataPagamento) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_RECEBIMENTO_VENDA_ID, vendaId);
        values.put(DatabaseHelper.COLUMN_RECEBIMENTO_NUMERO_PARCELA, 1);
        values.put(DatabaseHelper.COLUMN_RECEBIMENTO_VALOR, valor);
        values.put(DatabaseHelper.COLUMN_RECEBIMENTO_DATA_PREVISTA, dataPagamento);
        values.put(DatabaseHelper.COLUMN_RECEBIMENTO_STATUS, 1); // Pago
        values.put(DatabaseHelper.COLUMN_RECEBIMENTO_DATA_PAGAMENTO, dataPagamento);
        db.insert(DatabaseHelper.TABLE_RECEBIMENTOS, null, values);
    }

    public long registrarVendaAPrazo(long produtoId, double valorTotal, long dataVenda, int numeroParcelas, long dataPrimeiraParcela, long clienteId, String observacao) {
        Venda venda = new Venda();
        venda.setProdutoId(produtoId);
        venda.setClienteId(clienteId);
        venda.setDataVenda(dataVenda);
        venda.setTipoPagamento(TIPO_APRAZO);
        venda.setValorTotal(valorTotal);
        venda.setObservacao(observacao);
        long vendaId = inserirVenda(venda);
        criarRecebimentosParcelados(vendaId, valorTotal, numeroParcelas, dataPrimeiraParcela);
        return vendaId;
    }

    private void criarRecebimentosParcelados(long vendaId, double valorTotal, int numeroParcelas, long dataPrimeiraParcela) {
        double valorParcela = numeroParcelas > 0 ? (valorTotal / numeroParcelas) : valorTotal;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dataPrimeiraParcela);
        for (int i = 1; i <= numeroParcelas; i++) {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_RECEBIMENTO_VENDA_ID, vendaId);
            values.put(DatabaseHelper.COLUMN_RECEBIMENTO_NUMERO_PARCELA, i);
            values.put(DatabaseHelper.COLUMN_RECEBIMENTO_VALOR, valorParcela);
            values.put(DatabaseHelper.COLUMN_RECEBIMENTO_DATA_PREVISTA, cal.getTimeInMillis());
            values.put(DatabaseHelper.COLUMN_RECEBIMENTO_STATUS, 0); // A receber
            values.putNull(DatabaseHelper.COLUMN_RECEBIMENTO_DATA_PAGAMENTO);
            db.insert(DatabaseHelper.TABLE_RECEBIMENTOS, null, values);
            // Próxima parcela em +30 dias (simples)
            cal.add(Calendar.DAY_OF_MONTH, 30);
        }
    }

    public List<Venda> getAllVendas() {
        List<Venda> vendas = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_VENDAS, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                vendas.add(cursorToVenda(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return vendas;
    }

    public Venda getVendaById(long id) {
        Cursor cursor = db.query(DatabaseHelper.TABLE_VENDAS, null,
                DatabaseHelper.COLUMN_VENDA_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Venda v = cursorToVenda(cursor);
            cursor.close();
            return v;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public int atualizarVenda(Venda venda) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_VENDA_PRODUTO_ID, venda.getProdutoId());
        values.put(DatabaseHelper.COLUMN_VENDA_CLIENTE_ID, venda.getClienteId());
        values.put(DatabaseHelper.COLUMN_VENDA_DATA_VENDA, venda.getDataVenda());
        values.put(DatabaseHelper.COLUMN_VENDA_TIPO_PAGAMENTO, venda.getTipoPagamento());
        values.put(DatabaseHelper.COLUMN_VENDA_VALOR_TOTAL, venda.getValorTotal());
        values.put(DatabaseHelper.COLUMN_VENDA_OBSERVACAO, venda.getObservacao());
        return db.update(DatabaseHelper.TABLE_VENDAS, values,
                DatabaseHelper.COLUMN_VENDA_ID + " = ?",
                new String[]{String.valueOf(venda.getId())});
    }

    public void recriarRecebimentosParaVenda(long vendaId, int tipoPagamento, double valorTotal, Integer numeroParcelas, Long dataPrimeiraParcela, Long dataVenda) {
        // Remove recebimentos existentes desta venda
        db.delete(DatabaseHelper.TABLE_RECEBIMENTOS,
                DatabaseHelper.COLUMN_RECEBIMENTO_VENDA_ID + " = ?",
                new String[]{String.valueOf(vendaId)});
        if (tipoPagamento == TIPO_AVISTA) {
            long data = (dataVenda != null) ? dataVenda : System.currentTimeMillis();
            criarRecebimentoAVista(vendaId, valorTotal, data);
        } else {
            int parcelas = (numeroParcelas != null && numeroParcelas > 0) ? numeroParcelas : 1;
            long primeira = (dataPrimeiraParcela != null) ? dataPrimeiraParcela : ((dataVenda != null) ? dataVenda : System.currentTimeMillis());
            criarRecebimentosParcelados(vendaId, valorTotal, parcelas, primeira);
        }
    }

    private Venda cursorToVenda(Cursor cursor) {
        Venda v = new Venda();
        v.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VENDA_ID)));
        v.setProdutoId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VENDA_PRODUTO_ID)));
        v.setClienteId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VENDA_CLIENTE_ID)));
        v.setDataVenda(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VENDA_DATA_VENDA)));
        v.setTipoPagamento(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VENDA_TIPO_PAGAMENTO)));
        v.setValorTotal(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VENDA_VALOR_TOTAL)));
        v.setObservacao(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VENDA_OBSERVACAO)));
        return v;
    }
}
