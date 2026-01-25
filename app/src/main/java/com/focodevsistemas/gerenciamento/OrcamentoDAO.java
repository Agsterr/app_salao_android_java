package com.focodevsistemas.gerenciamento;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gerenciar orçamentos no banco de dados.
 */
public class OrcamentoDAO {
    
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;
    
    public OrcamentoDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }
    
    public void open() {
        db = dbHelper.getWritableDatabase();
    }
    
    public void close() {
        dbHelper.close();
    }
    
    /**
     * Insere um novo orçamento no banco de dados.
     */
    public long inserirOrcamento(Orcamento orcamento) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_ORCAMENTO_CLIENTE_ID, orcamento.getClienteId());
        values.put(DatabaseHelper.COLUMN_ORCAMENTO_TIPO, orcamento.getTipo());
        values.put(DatabaseHelper.COLUMN_ORCAMENTO_DATA_CRIACAO, orcamento.getDataCriacao());
        values.put(DatabaseHelper.COLUMN_ORCAMENTO_VALOR_TOTAL, orcamento.getValorTotal());
        values.put(DatabaseHelper.COLUMN_ORCAMENTO_DESCONTO, orcamento.getDesconto());
        values.put(DatabaseHelper.COLUMN_ORCAMENTO_ACRESCIMO, orcamento.getAcrescimo());
        values.put(DatabaseHelper.COLUMN_ORCAMENTO_OBSERVACOES, orcamento.getObservacoes());
        values.put(DatabaseHelper.COLUMN_ORCAMENTO_STATUS, orcamento.getStatus());
        
        long orcamentoId = db.insert(DatabaseHelper.TABLE_ORCAMENTOS, null, values);
        
        // Inserir itens
        if (orcamentoId > 0) {
            inserirItensServicos(orcamentoId, orcamento.getItensServicos());
            inserirItensProdutos(orcamentoId, orcamento.getItensProdutos());
        }
        
        return orcamentoId;
    }
    
    /**
     * Insere itens de serviço do orçamento.
     */
    private void inserirItensServicos(long orcamentoId, List<Orcamento.OrcamentoItemServico> itens) {
        for (Orcamento.OrcamentoItemServico item : itens) {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_ORCAMENTO_ITEM_SERVICO_ORCAMENTO_ID, orcamentoId);
            values.put(DatabaseHelper.COLUMN_ORCAMENTO_ITEM_SERVICO_SERVICO_ID, item.getServicoId());
            values.put(DatabaseHelper.COLUMN_ORCAMENTO_ITEM_SERVICO_QUANTIDADE, item.getQuantidade());
            values.put(DatabaseHelper.COLUMN_ORCAMENTO_ITEM_SERVICO_VALOR_UNITARIO, item.getValorUnitario());
            values.put(DatabaseHelper.COLUMN_ORCAMENTO_ITEM_SERVICO_VALOR_TOTAL, item.getValorTotal());
            db.insert(DatabaseHelper.TABLE_ORCAMENTO_ITENS_SERVICOS, null, values);
        }
    }
    
    /**
     * Insere itens de produto do orçamento.
     */
    private void inserirItensProdutos(long orcamentoId, List<Orcamento.OrcamentoItemProduto> itens) {
        for (Orcamento.OrcamentoItemProduto item : itens) {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_ORCAMENTO_ITEM_PRODUTO_ORCAMENTO_ID, orcamentoId);
            values.put(DatabaseHelper.COLUMN_ORCAMENTO_ITEM_PRODUTO_PRODUTO_ID, item.getProdutoId());
            values.put(DatabaseHelper.COLUMN_ORCAMENTO_ITEM_PRODUTO_QUANTIDADE, item.getQuantidade());
            values.put(DatabaseHelper.COLUMN_ORCAMENTO_ITEM_PRODUTO_VALOR_UNITARIO, item.getValorUnitario());
            values.put(DatabaseHelper.COLUMN_ORCAMENTO_ITEM_PRODUTO_VALOR_TOTAL, item.getValorTotal());
            db.insert(DatabaseHelper.TABLE_ORCAMENTO_ITENS_PRODUTOS, null, values);
        }
    }
    
    /**
     * Busca um orçamento por ID.
     */
    public Orcamento getOrcamentoById(long id) {
        Cursor cursor = db.query(DatabaseHelper.TABLE_ORCAMENTOS, null,
                DatabaseHelper.COLUMN_ORCAMENTO_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            Orcamento orcamento = cursorToOrcamento(cursor);
            cursor.close();
            
            // Carregar itens
            orcamento.setItensServicos(getItensServicosByOrcamentoId(id));
            orcamento.setItensProdutos(getItensProdutosByOrcamentoId(id));
            
            return orcamento;
        }
        return null;
    }
    
    /**
     * Lista todos os orçamentos.
     */
    public List<Orcamento> getAllOrcamentos() {
        List<Orcamento> orcamentos = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ORCAMENTOS, null, null, null, null, null,
                DatabaseHelper.COLUMN_ORCAMENTO_DATA_CRIACAO + " DESC");
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Orcamento orcamento = cursorToOrcamento(cursor);
                long id = orcamento.getId();
                
                // Carregar itens
                orcamento.setItensServicos(getItensServicosByOrcamentoId(id));
                orcamento.setItensProdutos(getItensProdutosByOrcamentoId(id));
                
                orcamentos.add(orcamento);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return orcamentos;
    }
    
    /**
     * Busca itens de serviço de um orçamento.
     */
    private List<Orcamento.OrcamentoItemServico> getItensServicosByOrcamentoId(long orcamentoId) {
        List<Orcamento.OrcamentoItemServico> itens = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ORCAMENTO_ITENS_SERVICOS, null,
                DatabaseHelper.COLUMN_ORCAMENTO_ITEM_SERVICO_ORCAMENTO_ID + " = ?",
                new String[]{String.valueOf(orcamentoId)}, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Orcamento.OrcamentoItemServico item = new Orcamento.OrcamentoItemServico();
                item.setServicoId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORCAMENTO_ITEM_SERVICO_SERVICO_ID)));
                item.setQuantidade(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORCAMENTO_ITEM_SERVICO_QUANTIDADE)));
                item.setValorUnitario(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORCAMENTO_ITEM_SERVICO_VALOR_UNITARIO)));
                item.setValorTotal(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORCAMENTO_ITEM_SERVICO_VALOR_TOTAL)));
                
                // Buscar nome do serviço diretamente do banco
                Cursor servicoCursor = db.query(DatabaseHelper.TABLE_SERVICOS, 
                        new String[]{DatabaseHelper.COLUMN_SERVICO_NOME},
                        DatabaseHelper.COLUMN_SERVICO_ID + " = ?",
                        new String[]{String.valueOf(item.getServicoId())}, null, null, null);
                if (servicoCursor != null && servicoCursor.moveToFirst()) {
                    item.setNomeServico(servicoCursor.getString(0));
                    servicoCursor.close();
                }
                
                itens.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return itens;
    }
    
    /**
     * Busca itens de produto de um orçamento.
     */
    private List<Orcamento.OrcamentoItemProduto> getItensProdutosByOrcamentoId(long orcamentoId) {
        List<Orcamento.OrcamentoItemProduto> itens = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ORCAMENTO_ITENS_PRODUTOS, null,
                DatabaseHelper.COLUMN_ORCAMENTO_ITEM_PRODUTO_ORCAMENTO_ID + " = ?",
                new String[]{String.valueOf(orcamentoId)}, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Orcamento.OrcamentoItemProduto item = new Orcamento.OrcamentoItemProduto();
                item.setProdutoId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORCAMENTO_ITEM_PRODUTO_PRODUTO_ID)));
                item.setQuantidade(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORCAMENTO_ITEM_PRODUTO_QUANTIDADE)));
                item.setValorUnitario(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORCAMENTO_ITEM_PRODUTO_VALOR_UNITARIO)));
                item.setValorTotal(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORCAMENTO_ITEM_PRODUTO_VALOR_TOTAL)));
                
                // Buscar nome do produto diretamente do banco
                Cursor produtoCursor = db.query(DatabaseHelper.TABLE_PRODUTOS, 
                        new String[]{DatabaseHelper.COLUMN_PRODUTO_NOME},
                        DatabaseHelper.COLUMN_PRODUTO_ID + " = ?",
                        new String[]{String.valueOf(item.getProdutoId())}, null, null, null);
                if (produtoCursor != null && produtoCursor.moveToFirst()) {
                    item.setNomeProduto(produtoCursor.getString(0));
                    produtoCursor.close();
                }
                
                itens.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return itens;
    }
    
    /**
     * Converte um Cursor em um objeto Orcamento.
     */
    private Orcamento cursorToOrcamento(Cursor cursor) {
        Orcamento orcamento = new Orcamento();
        orcamento.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORCAMENTO_ID)));
        orcamento.setClienteId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORCAMENTO_CLIENTE_ID)));
        orcamento.setTipo(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORCAMENTO_TIPO)));
        orcamento.setDataCriacao(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORCAMENTO_DATA_CRIACAO)));
        orcamento.setValorTotal(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORCAMENTO_VALOR_TOTAL)));
        
        int obsIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_ORCAMENTO_OBSERVACOES);
        if (obsIdx != -1) {
            orcamento.setObservacoes(cursor.getString(obsIdx));
        }
        
        int statusIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_ORCAMENTO_STATUS);
        if (statusIdx != -1) {
            orcamento.setStatus(cursor.getInt(statusIdx));
        }
        
        return orcamento;
    }
    
    /**
     * Deleta um orçamento e seus itens.
     */
    public int deletarOrcamento(long id) {
        // Deletar itens primeiro
        db.delete(DatabaseHelper.TABLE_ORCAMENTO_ITENS_SERVICOS,
                DatabaseHelper.COLUMN_ORCAMENTO_ITEM_SERVICO_ORCAMENTO_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.delete(DatabaseHelper.TABLE_ORCAMENTO_ITENS_PRODUTOS,
                DatabaseHelper.COLUMN_ORCAMENTO_ITEM_PRODUTO_ORCAMENTO_ID + " = ?",
                new String[]{String.valueOf(id)});
        
        // Deletar orçamento
        return db.delete(DatabaseHelper.TABLE_ORCAMENTOS,
                DatabaseHelper.COLUMN_ORCAMENTO_ID + " = ?",
                new String[]{String.valueOf(id)});
    }
}

