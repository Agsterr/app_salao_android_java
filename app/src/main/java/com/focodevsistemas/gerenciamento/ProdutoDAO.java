package com.focodevsistemas.gerenciamento;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO {
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public ProdutoDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() { db = dbHelper.getWritableDatabase(); }
    public void close() { dbHelper.close(); }

    public long inserirProduto(Produto produto) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PRODUTO_NOME, produto.getNome());
        values.put(DatabaseHelper.COLUMN_PRODUTO_VALOR_PADRAO, produto.getValorPadrao());
        values.put(DatabaseHelper.COLUMN_PRODUTO_PRECO_AQUISICAO, produto.getPrecoAquisicao());
        values.put(DatabaseHelper.COLUMN_PRODUTO_PRECO_VENDA, produto.getPrecoVenda());
        values.put(DatabaseHelper.COLUMN_PRODUTO_DESCRICAO, produto.getDescricao());
        values.put(DatabaseHelper.COLUMN_PRODUTO_IMAGEM_URI, produto.getImagemUri());
        return db.insert(DatabaseHelper.TABLE_PRODUTOS, null, values);
    }

    public int atualizarProduto(Produto produto) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PRODUTO_NOME, produto.getNome());
        values.put(DatabaseHelper.COLUMN_PRODUTO_VALOR_PADRAO, produto.getValorPadrao());
        values.put(DatabaseHelper.COLUMN_PRODUTO_PRECO_AQUISICAO, produto.getPrecoAquisicao());
        values.put(DatabaseHelper.COLUMN_PRODUTO_PRECO_VENDA, produto.getPrecoVenda());
        values.put(DatabaseHelper.COLUMN_PRODUTO_DESCRICAO, produto.getDescricao());
        values.put(DatabaseHelper.COLUMN_PRODUTO_IMAGEM_URI, produto.getImagemUri());
        return db.update(DatabaseHelper.TABLE_PRODUTOS, values,
                DatabaseHelper.COLUMN_PRODUTO_ID + " = ?",
                new String[]{String.valueOf(produto.getId())});
    }

    public int apagarProduto(long id) {
        return db.delete(DatabaseHelper.TABLE_PRODUTOS,
                DatabaseHelper.COLUMN_PRODUTO_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public List<Produto> getAllProdutos() {
        List<Produto> produtos = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_PRODUTOS, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                produtos.add(cursorToProduto(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return produtos;
    }

    public Produto getProdutoById(long id) {
        Cursor cursor = db.query(DatabaseHelper.TABLE_PRODUTOS, null,
                DatabaseHelper.COLUMN_PRODUTO_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Produto p = cursorToProduto(cursor);
            cursor.close();
            return p;
        }
        return null;
    }

    private Produto cursorToProduto(Cursor cursor) {
        Produto p = new Produto();
        p.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUTO_ID)));
        p.setNome(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUTO_NOME)));
        p.setValorPadrao(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUTO_VALOR_PADRAO)));
        
        // Novos campos (podem não existir em versões antigas)
        int precoAquisicaoIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_PRODUTO_PRECO_AQUISICAO);
        if (precoAquisicaoIdx != -1) {
            p.setPrecoAquisicao(cursor.getDouble(precoAquisicaoIdx));
        }
        
        int precoVendaIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_PRODUTO_PRECO_VENDA);
        if (precoVendaIdx != -1) {
            p.setPrecoVenda(cursor.getDouble(precoVendaIdx));
        } else {
            // Se não existir, usar valorPadrao como fallback
            p.setPrecoVenda(p.getValorPadrao());
        }
        
        p.setDescricao(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUTO_DESCRICAO)));
        int imgIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_PRODUTO_IMAGEM_URI);
        if (imgIdx != -1) {
            p.setImagemUri(cursor.getString(imgIdx));
        }
        return p;
    }
}
