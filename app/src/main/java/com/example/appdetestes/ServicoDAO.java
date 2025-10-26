package com.example.appdetestes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class ServicoDAO {

    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public ServicoDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long inserirServico(Servico servico) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_SERVICO_NOME, servico.getNome());
        values.put(DatabaseHelper.COLUMN_SERVICO_TEMPO, servico.getTempo());
        return db.insert(DatabaseHelper.TABLE_SERVICOS, null, values);
    }

    public List<Servico> getAllServicos() {
        List<Servico> servicos = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_SERVICOS, null, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Servico servico = cursorToServico(cursor);
            servicos.add(servico);
            cursor.moveToNext();
        }
        cursor.close();
        return servicos;
    }

    public int atualizarServico(Servico servico) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_SERVICO_NOME, servico.getNome());
        values.put(DatabaseHelper.COLUMN_SERVICO_TEMPO, servico.getTempo());
        return db.update(DatabaseHelper.TABLE_SERVICOS, values,
                DatabaseHelper.COLUMN_SERVICO_ID + " = ?",
                new String[]{String.valueOf(servico.getId())});
    }

    public int apagarServico(long id) {
        return db.delete(DatabaseHelper.TABLE_SERVICOS,
                DatabaseHelper.COLUMN_SERVICO_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    private Servico cursorToServico(Cursor cursor) {
        Servico servico = new Servico();
        servico.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SERVICO_ID)));
        servico.setNome(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SERVICO_NOME)));
        servico.setTempo(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SERVICO_TEMPO)));
        return servico;
    }
}
