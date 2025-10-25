package com.example.appdetestes;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "mydatabase.db";
    // Incrementar a versão para forçar a chamada ao onUpgrade
    private static final int DATABASE_VERSION = 4;

    // Nomes de tabela e colunas
    public static final String TABLE_MYTABLE = "mytable";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATA = "data";

    public static final String TABLE_CLIENTES = "clientes";
    public static final String COLUMN_CLIENTE_ID = "_id";
    public static final String COLUMN_CLIENTE_NOME = "nome";
    public static final String COLUMN_CLIENTE_EMAIL = "email";

    public static final String TABLE_SERVICOS = "servicos";
    public static final String COLUMN_SERVICO_ID = "_id";
    public static final String COLUMN_SERVICO_NOME = "nome";
    public static final String COLUMN_SERVICO_TEMPO = "tempo";

    public static final String TABLE_AGENDAMENTOS = "agendamentos";
    public static final String COLUMN_AGENDAMENTO_ID = "_id";
    public static final String COLUMN_AGENDAMENTO_CLIENTE_ID = "cliente_id";
    public static final String COLUMN_AGENDAMENTO_SERVICO_ID = "servico_id";
    public static final String COLUMN_AGENDAMENTO_DATA_HORA_INICIO = "data_hora_inicio";
    public static final String COLUMN_AGENDAMENTO_VALOR = "valor";

    // Instruções SQL para criar as tabelas
    private static final String TABLE_MYTABLE_CREATE =
            "CREATE TABLE " + TABLE_MYTABLE + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DATA + " TEXT);";

    private static final String TABLE_CLIENTES_CREATE =
            "CREATE TABLE " + TABLE_CLIENTES + " (" +
                    COLUMN_CLIENTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_CLIENTE_NOME + " TEXT, " +
                    COLUMN_CLIENTE_EMAIL + " TEXT);";

    private static final String TABLE_SERVICOS_CREATE =
            "CREATE TABLE " + TABLE_SERVICOS + " (" +
                    COLUMN_SERVICO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_SERVICO_NOME + " TEXT, " +
                    COLUMN_SERVICO_TEMPO + " INTEGER);";

    private static final String TABLE_AGENDAMENTOS_CREATE =
            "CREATE TABLE " + TABLE_AGENDAMENTOS + " (" +
                    COLUMN_AGENDAMENTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_AGENDAMENTO_CLIENTE_ID + " INTEGER, " +
                    COLUMN_AGENDAMENTO_SERVICO_ID + " INTEGER, " +
                    COLUMN_AGENDAMENTO_DATA_HORA_INICIO + " INTEGER, " +
                    COLUMN_AGENDAMENTO_VALOR + " REAL, " +
                    "FOREIGN KEY(" + COLUMN_AGENDAMENTO_CLIENTE_ID + ") REFERENCES " + TABLE_CLIENTES + "(" + COLUMN_CLIENTE_ID + ")," +
                    "FOREIGN KEY(" + COLUMN_AGENDAMENTO_SERVICO_ID + ") REFERENCES " + TABLE_SERVICOS + "(" + COLUMN_SERVICO_ID + ") );";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_MYTABLE_CREATE);
        db.execSQL(TABLE_CLIENTES_CREATE);
        db.execSQL(TABLE_SERVICOS_CREATE);
        db.execSQL(TABLE_AGENDAMENTOS_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_AGENDAMENTOS + " ADD COLUMN " + COLUMN_AGENDAMENTO_VALOR + " REAL DEFAULT 0");
            } catch (Exception e) {
                Log.w(DatabaseHelper.class.getName(), "Falha ao adicionar coluna valor, recriando tabelas: " + e.getMessage());
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_AGENDAMENTOS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERVICOS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLIENTES);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_MYTABLE);
                onCreate(db);
            }
            return;
        }
        // Fallback para versões mais antigas: recriar tudo
        Log.w(DatabaseHelper.class.getName(),
                "Atualizando banco de dados da versão " + oldVersion + " para "
                        + newVersion + ", recriando tabelas");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_AGENDAMENTOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERVICOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLIENTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MYTABLE);
        onCreate(db);
    }
}
