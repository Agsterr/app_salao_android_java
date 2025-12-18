package com.focodevsistemas.gerenciamento;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class VendaItemDAO {
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public VendaItemDAO(Context context) { dbHelper = new DatabaseHelper(context); }
    public void open() { db = dbHelper.getWritableDatabase(); }
    public void close() { dbHelper.close(); }

    public long insert(VendaItem item) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_VENDA_ITEM_VENDA_ID, item.getVendaId());
        values.put(DatabaseHelper.COLUMN_VENDA_ITEM_PRODUTO_ID, item.getProdutoId());
        values.put(DatabaseHelper.COLUMN_VENDA_ITEM_QUANTIDADE, item.getQuantidade());
        values.put(DatabaseHelper.COLUMN_VENDA_ITEM_VALOR_UNITARIO, item.getValorUnitario());
        return db.insert(DatabaseHelper.TABLE_VENDA_ITENS, null, values);
    }

    public int update(VendaItem item) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_VENDA_ITEM_VENDA_ID, item.getVendaId());
        values.put(DatabaseHelper.COLUMN_VENDA_ITEM_PRODUTO_ID, item.getProdutoId());
        values.put(DatabaseHelper.COLUMN_VENDA_ITEM_QUANTIDADE, item.getQuantidade());
        values.put(DatabaseHelper.COLUMN_VENDA_ITEM_VALOR_UNITARIO, item.getValorUnitario());
        return db.update(DatabaseHelper.TABLE_VENDA_ITENS, values,
                DatabaseHelper.COLUMN_VENDA_ITEM_ID + " = ?",
                new String[]{String.valueOf(item.getId())});
    }

    public List<VendaItem> getItensByVendaId(long vendaId) {
        List<VendaItem> itens = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_VENDA_ITENS, null,
                DatabaseHelper.COLUMN_VENDA_ITEM_VENDA_ID + " = ?",
                new String[]{String.valueOf(vendaId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                itens.add(cursorToItem(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return itens;
    }

    public int deleteItensByVendaId(long vendaId) {
        return db.delete(DatabaseHelper.TABLE_VENDA_ITENS,
                DatabaseHelper.COLUMN_VENDA_ITEM_VENDA_ID + " = ?",
                new String[]{String.valueOf(vendaId)});
    }

    private VendaItem cursorToItem(Cursor cursor) {
        VendaItem item = new VendaItem();
        item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VENDA_ITEM_ID)));
        item.setVendaId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VENDA_ITEM_VENDA_ID)));
        item.setProdutoId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VENDA_ITEM_PRODUTO_ID)));
        item.setQuantidade(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VENDA_ITEM_QUANTIDADE)));
        item.setValorUnitario(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VENDA_ITEM_VALOR_UNITARIO)));
        return item;
    }
}
