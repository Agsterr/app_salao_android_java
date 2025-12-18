package com.focodevsistemas.gerenciamento;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public ClienteDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long inserirCliente(Cliente cliente) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CLIENTE_NOME, cliente.getNome());
        return db.insert(DatabaseHelper.TABLE_CLIENTES, null, values);
    }

    public List<Cliente> getAllClientes() {
        List<Cliente> clientes = new ArrayList<>();
        // CORREÇÃO: Garante que a busca é feita na tabela de clientes.
        Cursor cursor = db.query(DatabaseHelper.TABLE_CLIENTES, null, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Cliente cliente = cursorToCliente(cursor);
            clientes.add(cliente);
            cursor.moveToNext();
        }
        cursor.close();
        return clientes;
    }

    public int atualizarCliente(Cliente cliente) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CLIENTE_NOME, cliente.getNome());
        return db.update(DatabaseHelper.TABLE_CLIENTES, values,
                DatabaseHelper.COLUMN_CLIENTE_ID + " = ?",
                new String[]{String.valueOf(cliente.getId())});
    }

    public int apagarCliente(long id) {
        return db.delete(DatabaseHelper.TABLE_CLIENTES,
                DatabaseHelper.COLUMN_CLIENTE_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    private Cliente cursorToCliente(Cursor cursor) {
        Cliente cliente = new Cliente();
        // CORREÇÃO: Garante que os dados são lidos das colunas de cliente.
        cliente.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLIENTE_ID)));
        cliente.setNome(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLIENTE_NOME)));
        return cliente;
    }

    public Cliente getClienteById(long id) {
        Cursor cursor = db.query(DatabaseHelper.TABLE_CLIENTES, null,
                DatabaseHelper.COLUMN_CLIENTE_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Cliente c = cursorToCliente(cursor);
            cursor.close();
            return c;
        }
        if (cursor != null) cursor.close();
        return null;
    }
}
