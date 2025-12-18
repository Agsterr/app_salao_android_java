package com.focodevsistemas.gerenciamento;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "mydatabase.db";
    // Incrementar a versão para forçar a chamada ao onUpgrade
    private static final int DATABASE_VERSION = 10;

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
    public static final String COLUMN_AGENDAMENTO_CANCELADO = "cancelado";
    public static final String COLUMN_AGENDAMENTO_FINALIZADO = "finalizado";

    // Novas tabelas: Produtos, Vendas, Recebimentos
    public static final String TABLE_PRODUTOS = "produtos";
    public static final String COLUMN_PRODUTO_ID = "_id";
    public static final String COLUMN_PRODUTO_NOME = "nome";
    public static final String COLUMN_PRODUTO_VALOR_PADRAO = "valor_padrao";
    public static final String COLUMN_PRODUTO_DESCRICAO = "descricao";
    public static final String COLUMN_PRODUTO_IMAGEM_URI = "imagem_uri";

    public static final String TABLE_VENDAS = "vendas";
    public static final String COLUMN_VENDA_ID = "_id";
    public static final String COLUMN_VENDA_PRODUTO_ID = "produto_id";
    public static final String COLUMN_VENDA_CLIENTE_ID = "cliente_id";
    public static final String COLUMN_VENDA_DATA_VENDA = "data_venda"; // timestamp (ms)
    public static final String COLUMN_VENDA_TIPO_PAGAMENTO = "tipo_pagamento"; // 0=AVISTA, 1=APRAZO
    public static final String COLUMN_VENDA_VALOR_TOTAL = "valor_total";
    public static final String COLUMN_VENDA_OBSERVACAO = "observacao";

    public static final String TABLE_RECEBIMENTOS = "recebimentos";
    public static final String COLUMN_RECEBIMENTO_ID = "_id";
    public static final String COLUMN_RECEBIMENTO_VENDA_ID = "venda_id";
    public static final String COLUMN_RECEBIMENTO_NUMERO_PARCELA = "numero_parcela";
    public static final String COLUMN_RECEBIMENTO_VALOR = "valor";
    public static final String COLUMN_RECEBIMENTO_DATA_PREVISTA = "data_prevista"; // timestamp (ms)
    public static final String COLUMN_RECEBIMENTO_STATUS = "status"; // 0=A_RECEBER, 1=PAGO
    public static final String COLUMN_RECEBIMENTO_DATA_PAGAMENTO = "data_pagamento"; // timestamp (ms)

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
                    COLUMN_AGENDAMENTO_CANCELADO + " INTEGER DEFAULT 0, " +
                    COLUMN_AGENDAMENTO_FINALIZADO + " INTEGER DEFAULT 0, " +
                    "FOREIGN KEY(" + COLUMN_AGENDAMENTO_CLIENTE_ID + ") REFERENCES " + TABLE_CLIENTES + "(" + COLUMN_CLIENTE_ID + ")," +
                    "FOREIGN KEY(" + COLUMN_AGENDAMENTO_SERVICO_ID + ") REFERENCES " + TABLE_SERVICOS + "(" + COLUMN_SERVICO_ID + ") );";

    // CREATE Produtos
    private static final String TABLE_PRODUTOS_CREATE =
            "CREATE TABLE " + TABLE_PRODUTOS + " (" +
                    COLUMN_PRODUTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PRODUTO_NOME + " TEXT NOT NULL, " +
                    COLUMN_PRODUTO_VALOR_PADRAO + " REAL DEFAULT 0, " +
                    COLUMN_PRODUTO_DESCRICAO + " TEXT, " +
                    COLUMN_PRODUTO_IMAGEM_URI + " TEXT );";

    // CREATE Vendas
    private static final String TABLE_VENDAS_CREATE =
            "CREATE TABLE " + TABLE_VENDAS + " (" +
                    COLUMN_VENDA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_VENDA_PRODUTO_ID + " INTEGER, " +
                    COLUMN_VENDA_CLIENTE_ID + " INTEGER, " +
                    COLUMN_VENDA_DATA_VENDA + " INTEGER, " +
                    COLUMN_VENDA_TIPO_PAGAMENTO + " INTEGER, " +
                    COLUMN_VENDA_VALOR_TOTAL + " REAL, " +
                    COLUMN_VENDA_OBSERVACAO + " TEXT, " +
                    "FOREIGN KEY(" + COLUMN_VENDA_PRODUTO_ID + ") REFERENCES " + TABLE_PRODUTOS + "(" + COLUMN_PRODUTO_ID + "), " +
                    "FOREIGN KEY(" + COLUMN_VENDA_CLIENTE_ID + ") REFERENCES " + TABLE_CLIENTES + "(" + COLUMN_CLIENTE_ID + ") );";

    // CREATE Recebimentos
    private static final String TABLE_RECEBIMENTOS_CREATE =
            "CREATE TABLE " + TABLE_RECEBIMENTOS + " (" +
                    COLUMN_RECEBIMENTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_RECEBIMENTO_VENDA_ID + " INTEGER, " +
                    COLUMN_RECEBIMENTO_NUMERO_PARCELA + " INTEGER, " +
                    COLUMN_RECEBIMENTO_VALOR + " REAL, " +
                    COLUMN_RECEBIMENTO_DATA_PREVISTA + " INTEGER, " +
                    COLUMN_RECEBIMENTO_STATUS + " INTEGER DEFAULT 0, " +
                    COLUMN_RECEBIMENTO_DATA_PAGAMENTO + " INTEGER, " +
                    "FOREIGN KEY(" + COLUMN_RECEBIMENTO_VENDA_ID + ") REFERENCES " + TABLE_VENDAS + "(" + COLUMN_VENDA_ID + ") );";

    public static final String TABLE_VENDA_ITENS = "venda_itens";
    public static final String COLUMN_VENDA_ITEM_ID = "_id";
    public static final String COLUMN_VENDA_ITEM_VENDA_ID = "venda_id";
    public static final String COLUMN_VENDA_ITEM_PRODUTO_ID = "produto_id";
    public static final String COLUMN_VENDA_ITEM_QUANTIDADE = "quantidade";
    public static final String COLUMN_VENDA_ITEM_VALOR_UNITARIO = "valor_unitario";
    private static final String TABLE_VENDA_ITENS_CREATE =
            "CREATE TABLE " + TABLE_VENDA_ITENS + " (" +
                    COLUMN_VENDA_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_VENDA_ITEM_VENDA_ID + " INTEGER, " +
                    COLUMN_VENDA_ITEM_PRODUTO_ID + " INTEGER, " +
                    COLUMN_VENDA_ITEM_QUANTIDADE + " INTEGER, " +
                    COLUMN_VENDA_ITEM_VALOR_UNITARIO + " REAL, " +
                    "FOREIGN KEY(" + COLUMN_VENDA_ITEM_VENDA_ID + ") REFERENCES " + TABLE_VENDAS + "(" + COLUMN_VENDA_ID + "), " +
                    "FOREIGN KEY(" + COLUMN_VENDA_ITEM_PRODUTO_ID + ") REFERENCES " + TABLE_PRODUTOS + "(" + COLUMN_PRODUTO_ID + ") );";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_MYTABLE_CREATE);
        db.execSQL(TABLE_CLIENTES_CREATE);
        db.execSQL(TABLE_SERVICOS_CREATE);
        db.execSQL(TABLE_AGENDAMENTOS_CREATE);
        db.execSQL(TABLE_PRODUTOS_CREATE);
        db.execSQL(TABLE_VENDAS_CREATE);
        db.execSQL(TABLE_RECEBIMENTOS_CREATE);
        db.execSQL(TABLE_VENDA_ITENS_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Migração incremental para preservar dados
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
                return;
            }
        }
        if (oldVersion < 5) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_AGENDAMENTOS + " ADD COLUMN " + COLUMN_AGENDAMENTO_CANCELADO + " INTEGER DEFAULT 0");
            } catch (Exception e) {
                Log.w(DatabaseHelper.class.getName(), "Falha ao adicionar coluna cancelado, recriando tabelas: " + e.getMessage());
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_AGENDAMENTOS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERVICOS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLIENTES);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_MYTABLE);
                onCreate(db);
                return;
            }
        }
        if (oldVersion < 6) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_AGENDAMENTOS + " ADD COLUMN " + COLUMN_AGENDAMENTO_FINALIZADO + " INTEGER DEFAULT 0");
            } catch (Exception e) {
                Log.w(DatabaseHelper.class.getName(), "Falha ao adicionar coluna finalizado, recriando tabelas: " + e.getMessage());
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_AGENDAMENTOS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERVICOS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLIENTES);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_MYTABLE);
                onCreate(db);
                return;
            }
        }
        if (oldVersion < 8) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_VENDAS + " ADD COLUMN " + COLUMN_VENDA_CLIENTE_ID + " INTEGER");
            } catch (Exception e) {
                Log.w(DatabaseHelper.class.getName(), "Falha ao adicionar cliente_id em vendas, recriando tabelas: " + e.getMessage());
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_AGENDAMENTOS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERVICOS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLIENTES);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_MYTABLE);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUTOS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_VENDAS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECEBIMENTOS);
                onCreate(db);
            }
        }
        if (oldVersion < 7) {
            try {
                db.execSQL(TABLE_PRODUTOS_CREATE);
                db.execSQL(TABLE_VENDAS_CREATE);
                db.execSQL(TABLE_RECEBIMENTOS_CREATE);
            } catch (Exception e) {
                Log.w(DatabaseHelper.class.getName(), "Falha ao criar tabelas de produtos/vendas/recebimentos: " + e.getMessage());
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_AGENDAMENTOS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERVICOS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLIENTES);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_MYTABLE);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUTOS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_VENDAS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECEBIMENTOS);
                onCreate(db);
            }
        }
        if (oldVersion < 9) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_PRODUTOS + " ADD COLUMN " + COLUMN_PRODUTO_IMAGEM_URI + " TEXT");
            } catch (Exception e) {
                Log.w(DatabaseHelper.class.getName(), "Falha ao adicionar imagem_uri em produtos: " + e.getMessage());
            }
        }
        if (oldVersion < 10) {
            try {
                db.execSQL(TABLE_VENDA_ITENS_CREATE);
            } catch (Exception e) {
                Log.w(DatabaseHelper.class.getName(), "Falha ao criar venda_itens: " + e.getMessage());
            }
        }
        // Se versões futuras exigirem mudanças, adicionar aqui.
    }
}
