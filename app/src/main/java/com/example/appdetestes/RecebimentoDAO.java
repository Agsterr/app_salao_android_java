package com.example.appdetestes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class RecebimentoDAO {
    public static final int STATUS_A_RECEBER = 0;
    public static final int STATUS_PAGO = 1;

    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public RecebimentoDAO(Context context) { dbHelper = new DatabaseHelper(context); }
    public void open() { db = dbHelper.getWritableDatabase(); }
    public void close() { dbHelper.close(); }

    public List<Recebimento> getPorStatus(int status) {
        List<Recebimento> lista = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_RECEBIMENTOS, null,
                DatabaseHelper.COLUMN_RECEBIMENTO_STATUS + " = ?",
                new String[]{String.valueOf(status)}, null, null,
                DatabaseHelper.COLUMN_RECEBIMENTO_DATA_PREVISTA + " ASC");
        if (cursor.moveToFirst()) {
            do {
                lista.add(cursorToRecebimento(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public int marcarComoPago(long recebimentoId, long dataPagamento) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_RECEBIMENTO_STATUS, STATUS_PAGO);
        values.put(DatabaseHelper.COLUMN_RECEBIMENTO_DATA_PAGAMENTO, dataPagamento);
        return db.update(DatabaseHelper.TABLE_RECEBIMENTOS, values,
                DatabaseHelper.COLUMN_RECEBIMENTO_ID + " = ?",
                new String[]{String.valueOf(recebimentoId)});
    }

    private Recebimento cursorToRecebimento(Cursor cursor) {
        Recebimento r = new Recebimento();
        r.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECEBIMENTO_ID)));
        r.setVendaId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECEBIMENTO_VENDA_ID)));
        r.setNumeroParcela(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECEBIMENTO_NUMERO_PARCELA)));
        r.setValor(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECEBIMENTO_VALOR)));
        r.setDataPrevista(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECEBIMENTO_DATA_PREVISTA)));
        r.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECEBIMENTO_STATUS)));
        int idxPagamento = cursor.getColumnIndex(DatabaseHelper.COLUMN_RECEBIMENTO_DATA_PAGAMENTO);
        if (idxPagamento >= 0 && !cursor.isNull(idxPagamento)) {
            r.setDataPagamento(cursor.getLong(idxPagamento));
        }
        return r;
    }
}