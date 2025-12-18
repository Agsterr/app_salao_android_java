package com.focodevsistemas.gerenciamento;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class AgendamentoDAO {

    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    private static final String ALIAS_CLIENTE_NOME = "cliente_nome";
    private static final String ALIAS_SERVICO_NOME = "servico_nome";

    public AgendamentoDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long inserirAgendamento(Agendamento agendamento) {
        Log.d("CONFLITO_DAO", "=== INICIANDO INSERÇÃO DE AGENDAMENTO ===");

        // Esta verificação de segurança previne o crash se o tempo não for informado.
        if (agendamento.getTempoServico() <= 0) {
            Log.e("CONFLITO_DAO", "Erro: Tentativa de inserir agendamento com tempo de serviço inválido.");
            return -1;
        }

        // Calcula início e fim do novo agendamento
        long inicio = agendamento.getDataHoraInicio();
        long fim = inicio + (agendamento.getTempoServico() * 60000L);

        Log.d("CONFLITO_DAO", "Dados do agendamento a inserir:");
        Log.d("CONFLITO_DAO", "Cliente ID: " + agendamento.getClienteId());
        Log.d("CONFLITO_DAO", "Serviço ID: " + agendamento.getServicoId());
        Log.d("CONFLITO_DAO", "Tempo Serviço: " + agendamento.getTempoServico() + " min");
        Log.d("CONFLITO_DAO", "Início: " + inicio);
        Log.d("CONFLITO_DAO", "Fim: " + fim);
        Log.d("CONFLITO_DAO", "Período: " + new java.util.Date(inicio) + " até " + new java.util.Date(fim));

        // --- ESTA É A CHAMADA QUE ESTAVA FALTANDO ---
        // Chama o método de verificação de conflito ANTES de tentar inserir
        Agendamento conflito = getAgendamentoConflitante(inicio, fim, -1);
        
        // Se o método acima encontrar um conflito, 'conflito' não será nulo
        if (conflito != null) {
            Log.e("CONFLITO_DAO", "INSERÇÃO BLOQUEADA: Conflito encontrado com o agendamento ID: " + conflito.getId());
            Log.d("CONFLITO_DAO", "=== FIM DA INSERÇÃO (BLOQUEADA) ===");
            return -1; // Retorna -1 para indicar falha por conflito
        }

        Log.d("CONFLITO_DAO", "Nenhum conflito encontrado. Prosseguindo com a inserção...");
        
        // Se não houve conflito, prossiga com a inserção
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_AGENDAMENTO_CLIENTE_ID, agendamento.getClienteId());
        values.put(DatabaseHelper.COLUMN_AGENDAMENTO_SERVICO_ID, agendamento.getServicoId());
        values.put(DatabaseHelper.COLUMN_AGENDAMENTO_DATA_HORA_INICIO, agendamento.getDataHoraInicio());
        values.put(DatabaseHelper.COLUMN_AGENDAMENTO_VALOR, agendamento.getValor());
        values.put(DatabaseHelper.COLUMN_AGENDAMENTO_CANCELADO, agendamento.getCancelado());
        values.put(DatabaseHelper.COLUMN_AGENDAMENTO_FINALIZADO, agendamento.getFinalizado());
        long resultado = db.insert(DatabaseHelper.TABLE_AGENDAMENTOS, null, values);

        if (resultado > 0) {
            Log.d("CONFLITO_DAO", "✅ AGENDAMENTO INSERIDO COM SUCESSO! ID: " + resultado);
        } else {
            Log.e("CONFLITO_DAO", "❌ FALHA NA INSERÇÃO DO AGENDAMENTO (erro no db.insert)!");
        }
        Log.d("CONFLITO_DAO", "=== FIM DA INSERÇÃO ===");

        return resultado;
    }

    // Inserção forçada, sem verificação de conflito
    public long inserirAgendamentoIgnorandoConflito(Agendamento agendamento) {
        Log.w("CONFLITO_DAO", "Inserção FORÇADA: ignorando verificação de conflito.");
        if (agendamento.getTempoServico() <= 0) {
            Log.e("CONFLITO_DAO", "Erro: Tentativa de inserir agendamento com tempo de serviço inválido.");
            return -1;
        }
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_AGENDAMENTO_CLIENTE_ID, agendamento.getClienteId());
        values.put(DatabaseHelper.COLUMN_AGENDAMENTO_SERVICO_ID, agendamento.getServicoId());
        values.put(DatabaseHelper.COLUMN_AGENDAMENTO_DATA_HORA_INICIO, agendamento.getDataHoraInicio());
        values.put(DatabaseHelper.COLUMN_AGENDAMENTO_VALOR, agendamento.getValor());
        values.put(DatabaseHelper.COLUMN_AGENDAMENTO_CANCELADO, agendamento.getCancelado());
        values.put(DatabaseHelper.COLUMN_AGENDAMENTO_FINALIZADO, agendamento.getFinalizado());
        long resultado = db.insert(DatabaseHelper.TABLE_AGENDAMENTOS, null, values);
        if (resultado > 0) {
            Log.d("CONFLITO_DAO", "✅ AGENDAMENTO INSERIDO (forçado) ID: " + resultado);
        } else {
            Log.e("CONFLITO_DAO", "❌ FALHA NA INSERÇÃO (forçado).");
        }
        return resultado;
    }

    public int atualizarAgendamento(Agendamento agendamento) {
        // A Activity agora é responsável por nos dar o tempo de serviço.
        // Esta verificação de segurança previne o crash se o tempo não for informado.
        if (agendamento.getTempoServico() <= 0) {
            Log.e("CONFLITO_DAO", "Erro: Tentativa de atualizar agendamento com tempo de serviço inválido.");
            return 0;
        }

        long inicio = agendamento.getDataHoraInicio();
        long fim = inicio + (agendamento.getTempoServico() * 60000L);

        // Chama o método que tem os LOGS para depuração
        Agendamento conflito = getAgendamentoConflitante(inicio, fim, agendamento.getId());
        if (conflito != null) {
            return 0; // Conflito encontrado
        }

        // Prossiga com a atualização
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_AGENDAMENTO_CLIENTE_ID, agendamento.getClienteId());
        values.put(DatabaseHelper.COLUMN_AGENDAMENTO_SERVICO_ID, agendamento.getServicoId());
        values.put(DatabaseHelper.COLUMN_AGENDAMENTO_DATA_HORA_INICIO, agendamento.getDataHoraInicio());
        values.put(DatabaseHelper.COLUMN_AGENDAMENTO_VALOR, agendamento.getValor());
        values.put(DatabaseHelper.COLUMN_AGENDAMENTO_CANCELADO, agendamento.getCancelado());
        values.put(DatabaseHelper.COLUMN_AGENDAMENTO_FINALIZADO, agendamento.getFinalizado());
        return db.update(DatabaseHelper.TABLE_AGENDAMENTOS, values, DatabaseHelper.COLUMN_AGENDAMENTO_ID + " = ?", new String[]{String.valueOf(agendamento.getId())});
    }

    // Atualização forçada, sem verificação de conflito
    public int atualizarAgendamentoIgnorandoConflito(Agendamento agendamento) {
        Log.w("CONFLITO_DAO", "Atualização FORÇADA: ignorando verificação de conflito.");
        if (agendamento.getTempoServico() <= 0) {
            Log.e("CONFLITO_DAO", "Erro: Tentativa de atualizar agendamento com tempo de serviço inválido.");
            return 0;
        }
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_AGENDAMENTO_CLIENTE_ID, agendamento.getClienteId());
        values.put(DatabaseHelper.COLUMN_AGENDAMENTO_SERVICO_ID, agendamento.getServicoId());
        values.put(DatabaseHelper.COLUMN_AGENDAMENTO_DATA_HORA_INICIO, agendamento.getDataHoraInicio());
        values.put(DatabaseHelper.COLUMN_AGENDAMENTO_VALOR, agendamento.getValor());
        values.put(DatabaseHelper.COLUMN_AGENDAMENTO_CANCELADO, agendamento.getCancelado());
        values.put(DatabaseHelper.COLUMN_AGENDAMENTO_FINALIZADO, agendamento.getFinalizado());
        return db.update(DatabaseHelper.TABLE_AGENDAMENTOS, values, DatabaseHelper.COLUMN_AGENDAMENTO_ID + " = ?", new String[]{String.valueOf(agendamento.getId())});
    }

    public int apagarAgendamento(long id) {
        return db.delete(DatabaseHelper.TABLE_AGENDAMENTOS, DatabaseHelper.COLUMN_AGENDAMENTO_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public Agendamento getAgendamentoById(long id) {
        String query = "SELECT a.*, c." + DatabaseHelper.COLUMN_CLIENTE_NOME + " AS " + ALIAS_CLIENTE_NOME + ", s." + DatabaseHelper.COLUMN_SERVICO_NOME + " AS " + ALIAS_SERVICO_NOME + ", s." + DatabaseHelper.COLUMN_SERVICO_TEMPO + " " +
                "FROM " + DatabaseHelper.TABLE_AGENDAMENTOS + " a " +
                "LEFT JOIN " + DatabaseHelper.TABLE_CLIENTES + " c ON a." + DatabaseHelper.COLUMN_AGENDAMENTO_CLIENTE_ID + " = c." + DatabaseHelper.COLUMN_CLIENTE_ID + " " +
                "LEFT JOIN " + DatabaseHelper.TABLE_SERVICOS + " s ON a." + DatabaseHelper.COLUMN_AGENDAMENTO_SERVICO_ID + " = s." + DatabaseHelper.COLUMN_SERVICO_ID + " " +
                "WHERE a." + DatabaseHelper.COLUMN_AGENDAMENTO_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});

        if (cursor != null && cursor.moveToFirst()) {
            Agendamento agendamento = cursorToAgendamento(cursor);
            cursor.close();
            return agendamento;
        }
        return null;
    }
    
    public boolean verificarConflito(long novoInicio, long novoFim, long idAgendamentoExcluir) {
        return getAgendamentoConflitante(novoInicio, novoFim, idAgendamentoExcluir) != null;
    }

    // Em AgendamentoDAO.java

    public Agendamento getAgendamentoConflitante(long novoInicio, long novoFim, long idAgendamentoExcluir) {
        Log.d("CONFLITO_DAO", "Verificando conflito...");
        Log.d("CONFLITO_DAO", "novoInicio: " + novoInicio);
        Log.d("CONFLITO_DAO", "novoFim: " + novoFim);
        Log.d("CONFLITO_DAO", "idAgendamentoExcluir: " + idAgendamentoExcluir);

        // Primeiro, vamos listar todos os agendamentos existentes para debug
        String debugQuery = "SELECT a.*, s." + DatabaseHelper.COLUMN_SERVICO_TEMPO + " FROM " + 
                DatabaseHelper.TABLE_AGENDAMENTOS + " a " +
                "LEFT JOIN " + DatabaseHelper.TABLE_SERVICOS + " s ON a." + DatabaseHelper.COLUMN_AGENDAMENTO_SERVICO_ID + " = s." + DatabaseHelper.COLUMN_SERVICO_ID;
        
        Cursor debugCursor = db.rawQuery(debugQuery, null);
        Log.d("CONFLITO_DAO", "Agendamentos existentes no banco:");
        if (debugCursor != null && debugCursor.moveToFirst()) {
            do {
                int idIndex = debugCursor.getColumnIndex(DatabaseHelper.COLUMN_AGENDAMENTO_ID);
                int inicioIndex = debugCursor.getColumnIndex(DatabaseHelper.COLUMN_AGENDAMENTO_DATA_HORA_INICIO);
                int tempoIndex = debugCursor.getColumnIndex(DatabaseHelper.COLUMN_SERVICO_TEMPO);
                
                if (idIndex >= 0 && inicioIndex >= 0 && tempoIndex >= 0) {
                    long existenteId = debugCursor.getLong(idIndex);
                    long existenteInicio = debugCursor.getLong(inicioIndex);
                    int tempo = debugCursor.getInt(tempoIndex);
                    long existenteFim = existenteInicio + (tempo * 60000L);
                    Log.d("CONFLITO_DAO", "ID: " + existenteId + ", Início: " + existenteInicio + ", Fim: " + existenteFim + ", Tempo: " + tempo + " min");
                }
            } while (debugCursor.moveToNext());
        } else {
            Log.d("CONFLITO_DAO", "Nenhum agendamento encontrado no banco!");
        }
        if (debugCursor != null) debugCursor.close();

        // Agora a query de conflito corrigida
        String existenteInicio = "a." + DatabaseHelper.COLUMN_AGENDAMENTO_DATA_HORA_INICIO;
        String existenteFim = "(a." + DatabaseHelper.COLUMN_AGENDAMENTO_DATA_HORA_INICIO + " + (COALESCE(s." + DatabaseHelper.COLUMN_SERVICO_TEMPO + ", 0) * 60000))";

        // Vamos testar uma query mais simples primeiro
        // Usando INNER JOIN em vez de LEFT JOIN para garantir que o serviço existe
        String query = "SELECT a.*, " +
                "c." + DatabaseHelper.COLUMN_CLIENTE_NOME + " AS " + ALIAS_CLIENTE_NOME + ", " +
                "s." + DatabaseHelper.COLUMN_SERVICO_NOME + " AS " + ALIAS_SERVICO_NOME + ", " +
                "s." + DatabaseHelper.COLUMN_SERVICO_TEMPO + " " +
                "FROM " + DatabaseHelper.TABLE_AGENDAMENTOS + " a " +
                "INNER JOIN " + DatabaseHelper.TABLE_CLIENTES + " c ON a." + DatabaseHelper.COLUMN_AGENDAMENTO_CLIENTE_ID + " = c." + DatabaseHelper.COLUMN_CLIENTE_ID + " " +
                "INNER JOIN " + DatabaseHelper.TABLE_SERVICOS + " s ON a." + DatabaseHelper.COLUMN_AGENDAMENTO_SERVICO_ID + " = s." + DatabaseHelper.COLUMN_SERVICO_ID + " " +
                "WHERE (? <= (a." + DatabaseHelper.COLUMN_AGENDAMENTO_DATA_HORA_INICIO + " + (s." + DatabaseHelper.COLUMN_SERVICO_TEMPO + " * 60000)) AND a." + DatabaseHelper.COLUMN_AGENDAMENTO_DATA_HORA_INICIO + " <= ?) " +
                "AND a." + DatabaseHelper.COLUMN_AGENDAMENTO_ID + " != ? " +
                "LIMIT 1";

        String[] selectionArgs = {
                String.valueOf(novoInicio),
                String.valueOf(novoFim),
                String.valueOf(idAgendamentoExcluir)
        };

        Log.d("CONFLITO_DAO", "Query de conflito: " + query);
        Log.d("CONFLITO_DAO", "Parâmetros: " + java.util.Arrays.toString(selectionArgs));
        
        // Debug: vamos testar manualmente cada condição
        Log.d("CONFLITO_DAO", "=== TESTE MANUAL DAS CONDIÇÕES ===");
        
        // Primeiro, vamos buscar o agendamento existente
        String existenteQuery = "SELECT a.*, s.tempo FROM " + DatabaseHelper.TABLE_AGENDAMENTOS + " a " +
                "INNER JOIN " + DatabaseHelper.TABLE_SERVICOS + " s ON a." + DatabaseHelper.COLUMN_AGENDAMENTO_SERVICO_ID + " = s." + DatabaseHelper.COLUMN_SERVICO_ID + 
                " WHERE a._id = 1";
        Cursor existenteCursor = db.rawQuery(existenteQuery, null);
        if (existenteCursor != null && existenteCursor.moveToFirst()) {
              int inicioIndex = existenteCursor.getColumnIndex("data_hora_inicio");
              int tempoIndex = existenteCursor.getColumnIndex("tempo");
              
              if (inicioIndex >= 0 && tempoIndex >= 0) {
                  long debugExistenteInicio = existenteCursor.getLong(inicioIndex);
                  int debugTempo = existenteCursor.getInt(tempoIndex);
                  long debugExistenteFim = debugExistenteInicio + (debugTempo * 60000L);
            
            Log.d("CONFLITO_DAO", "Agendamento existente - Início: " + debugExistenteInicio + ", Fim: " + debugExistenteFim);
             Log.d("CONFLITO_DAO", "Novo agendamento - Início: " + novoInicio + ", Fim: " + novoFim);
             
             // Teste das condições
             boolean condicao1 = novoInicio <= debugExistenteFim;
             boolean condicao2 = debugExistenteInicio <= novoFim;
             
             Log.d("CONFLITO_DAO", "Condição 1 (novoInicio <= existenteFim): " + novoInicio + " <= " + debugExistenteFim + " = " + condicao1);
              Log.d("CONFLITO_DAO", "Condição 2 (existenteInicio <= novoFim): " + debugExistenteInicio + " <= " + novoFim + " = " + condicao2);
             Log.d("CONFLITO_DAO", "Resultado final (ambas TRUE = conflito): " + (condicao1 && condicao2));
               }
         }
        if (existenteCursor != null) existenteCursor.close();
        
        // Debug: vamos testar se o INNER JOIN está funcionando
        Log.d("CONFLITO_DAO", "=== TESTANDO INNER JOIN ===");
        String testQuery = "SELECT a.*, s.tempo FROM " + DatabaseHelper.TABLE_AGENDAMENTOS + " a " +
                "INNER JOIN " + DatabaseHelper.TABLE_SERVICOS + " s ON a." + DatabaseHelper.COLUMN_AGENDAMENTO_SERVICO_ID + " = s." + DatabaseHelper.COLUMN_SERVICO_ID;
        Cursor testCursor = db.rawQuery(testQuery, null);
        if (testCursor != null && testCursor.moveToFirst()) {
            do {
                int idIndex = testCursor.getColumnIndex(DatabaseHelper.COLUMN_AGENDAMENTO_ID);
                int servicoIdIndex = testCursor.getColumnIndex(DatabaseHelper.COLUMN_AGENDAMENTO_SERVICO_ID);
                int tempoIndex = testCursor.getColumnIndex(DatabaseHelper.COLUMN_SERVICO_TEMPO);
                
                if (idIndex >= 0 && servicoIdIndex >= 0 && tempoIndex >= 0) {
                    long agendamentoId = testCursor.getLong(idIndex);
                    long servicoId = testCursor.getLong(servicoIdIndex);
                    int tempo = testCursor.getInt(tempoIndex);
                    Log.d("CONFLITO_DAO", "JOIN Test - Agendamento ID: " + agendamentoId + ", Serviço ID: " + servicoId + ", Tempo: " + tempo);
                }
            } while (testCursor.moveToNext());
        }
        testCursor.close();

        // Lógica de detecção de conflito em Java (sem depender da query)
        String allQuery = "SELECT a.*, c." + DatabaseHelper.COLUMN_CLIENTE_NOME + " AS " + ALIAS_CLIENTE_NOME + ", s." + DatabaseHelper.COLUMN_SERVICO_NOME + " AS " + ALIAS_SERVICO_NOME + ", COALESCE(s." + DatabaseHelper.COLUMN_SERVICO_TEMPO + ", 0) AS " + DatabaseHelper.COLUMN_SERVICO_TEMPO + " " +
                "FROM " + DatabaseHelper.TABLE_AGENDAMENTOS + " a " +
                "LEFT JOIN " + DatabaseHelper.TABLE_CLIENTES + " c ON a." + DatabaseHelper.COLUMN_AGENDAMENTO_CLIENTE_ID + " = c." + DatabaseHelper.COLUMN_CLIENTE_ID + " " +
                "LEFT JOIN " + DatabaseHelper.TABLE_SERVICOS + " s ON a." + DatabaseHelper.COLUMN_AGENDAMENTO_SERVICO_ID + " = s." + DatabaseHelper.COLUMN_SERVICO_ID + " " +
                "WHERE a." + DatabaseHelper.COLUMN_AGENDAMENTO_ID + " != ? " +
                "ORDER BY a." + DatabaseHelper.COLUMN_AGENDAMENTO_DATA_HORA_INICIO + " ASC";

        Cursor allCursor = db.rawQuery(allQuery, new String[]{String.valueOf(idAgendamentoExcluir)});
        Log.d("CONFLITO_DAO", "Checando conflitos via lógica (Java)...");
        if (allCursor != null && allCursor.moveToFirst()) {
            do {
                long existenteInicioVal = allCursor.getLong(allCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AGENDAMENTO_DATA_HORA_INICIO));
                int tempoVal = 0;
                int tempoIdx = allCursor.getColumnIndex(DatabaseHelper.COLUMN_SERVICO_TEMPO);
                if (tempoIdx != -1) tempoVal = allCursor.getInt(tempoIdx);
                long existenteFimVal = existenteInicioVal + (tempoVal * 60000L);

                boolean overlap = (existenteInicioVal <= novoFim) && (novoInicio <= existenteFimVal); // inclusivo
                Log.d("CONFLITO_DAO", "Check: existenteInicio=" + existenteInicioVal + " existenteFim=" + existenteFimVal + " tempo=" + tempoVal + " => overlap=" + overlap);
                if (overlap) {
                    Log.d("CONFLITO_DAO", "CONFLITO ENCONTRADO (via lógica)");
                    Agendamento agendamento = cursorToAgendamento(allCursor);
                    allCursor.close();
                    return agendamento;
                }
            } while (allCursor.moveToNext());
        }
        if (allCursor != null) allCursor.close();

        Log.d("CONFLITO_DAO", "Nenhum conflito encontrado (via lógica)");
        return null;
    }

    public List<Agendamento> getAgendamentosPorDia(long diaTimestamp) {
        List<Agendamento> agendamentos = new ArrayList<>();
        // Define o período de 24h para o dia selecionado
        long fimDoDiaTimestamp = diaTimestamp + (24 * 60 * 60 * 1000) - 1;

        // --- CORREÇÃO: USANDO LEFT JOIN PARA CONSISTÊNCIA ---
        String query = "SELECT a.*, c." + DatabaseHelper.COLUMN_CLIENTE_NOME + " AS " + ALIAS_CLIENTE_NOME + ", s." + DatabaseHelper.COLUMN_SERVICO_NOME + " AS " + ALIAS_SERVICO_NOME + ", s." + DatabaseHelper.COLUMN_SERVICO_TEMPO + " " +
                "FROM " + DatabaseHelper.TABLE_AGENDAMENTOS + " a " +
                "LEFT JOIN " + DatabaseHelper.TABLE_CLIENTES + " c ON a." + DatabaseHelper.COLUMN_AGENDAMENTO_CLIENTE_ID + " = c." + DatabaseHelper.COLUMN_CLIENTE_ID + " " +
                "LEFT JOIN " + DatabaseHelper.TABLE_SERVICOS + " s ON a." + DatabaseHelper.COLUMN_AGENDAMENTO_SERVICO_ID + " = s." + DatabaseHelper.COLUMN_SERVICO_ID + " " +
                "WHERE a." + DatabaseHelper.COLUMN_AGENDAMENTO_DATA_HORA_INICIO + " >= ? AND a." + DatabaseHelper.COLUMN_AGENDAMENTO_DATA_HORA_INICIO + " <= ? " +
                "ORDER BY a." + DatabaseHelper.COLUMN_AGENDAMENTO_DATA_HORA_INICIO + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(diaTimestamp), String.valueOf(fimDoDiaTimestamp)});
        
        // Loop seguro para percorrer o cursor
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Agendamento agendamento = cursorToAgendamento(cursor);
                agendamentos.add(agendamento);
            }
            cursor.close();
        }
        return agendamentos;
    }

    public List<Agendamento> getAgendamentosPorPeriodo(long inicioTimestamp, long fimTimestamp) {
        List<Agendamento> agendamentos = new ArrayList<>();

        String query = "SELECT a.*, c." + DatabaseHelper.COLUMN_CLIENTE_NOME + " AS " + ALIAS_CLIENTE_NOME + ", s." + DatabaseHelper.COLUMN_SERVICO_NOME + " AS " + ALIAS_SERVICO_NOME + ", s." + DatabaseHelper.COLUMN_SERVICO_TEMPO + " " +
                "FROM " + DatabaseHelper.TABLE_AGENDAMENTOS + " a " +
                "LEFT JOIN " + DatabaseHelper.TABLE_CLIENTES + " c ON a." + DatabaseHelper.COLUMN_AGENDAMENTO_CLIENTE_ID + " = c." + DatabaseHelper.COLUMN_CLIENTE_ID + " " +
                "LEFT JOIN " + DatabaseHelper.TABLE_SERVICOS + " s ON a." + DatabaseHelper.COLUMN_AGENDAMENTO_SERVICO_ID + " = s." + DatabaseHelper.COLUMN_SERVICO_ID + " " +
                "WHERE a." + DatabaseHelper.COLUMN_AGENDAMENTO_DATA_HORA_INICIO + " >= ? AND a." + DatabaseHelper.COLUMN_AGENDAMENTO_DATA_HORA_INICIO + " <= ? " +
                "ORDER BY a." + DatabaseHelper.COLUMN_AGENDAMENTO_DATA_HORA_INICIO + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(inicioTimestamp), String.valueOf(fimTimestamp)});
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Agendamento agendamento = cursorToAgendamento(cursor);
                agendamentos.add(agendamento);
            }
            cursor.close();
        }
        return agendamentos;
    }

    private Agendamento cursorToAgendamento(Cursor cursor) {
        Agendamento agendamento = new Agendamento();

        // Campos principais
        agendamento.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AGENDAMENTO_ID)));
        agendamento.setDataHoraInicio(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AGENDAMENTO_DATA_HORA_INICIO)));
        agendamento.setClienteId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AGENDAMENTO_CLIENTE_ID)));
        agendamento.setServicoId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AGENDAMENTO_SERVICO_ID)));

        // Nome do cliente (alias da query principal)
        int idxClienteNome = cursor.getColumnIndex("cliente_nome");
        if (idxClienteNome != -1) {
            agendamento.setNomeCliente(cursor.getString(idxClienteNome));
        }

        // Nome do serviço (alias da query principal)
        int idxServicoNome = cursor.getColumnIndex("servico_nome");
        if (idxServicoNome != -1) {
            agendamento.setNomeServico(cursor.getString(idxServicoNome));
        }

        // Tempo do serviço (alias da query principal)
        int idxServicoTempo = cursor.getColumnIndex(DatabaseHelper.COLUMN_SERVICO_TEMPO);
        if (idxServicoTempo != -1) {
            agendamento.setTempoServico(cursor.getInt(idxServicoTempo));
        }

        // Valor do serviço (persistido em agendamentos)
        int idxValor = cursor.getColumnIndex(DatabaseHelper.COLUMN_AGENDAMENTO_VALOR);
        if (idxValor != -1) {
            agendamento.setValor(cursor.getDouble(idxValor));
        }

        int idxCancelado = cursor.getColumnIndex(DatabaseHelper.COLUMN_AGENDAMENTO_CANCELADO);
        if (idxCancelado != -1) {
            agendamento.setCancelado(cursor.getInt(idxCancelado));
        }
        int idxFinalizado = cursor.getColumnIndex(DatabaseHelper.COLUMN_AGENDAMENTO_FINALIZADO);
        if (idxFinalizado != -1) {
            agendamento.setFinalizado(cursor.getInt(idxFinalizado));
        }
        return agendamento;
    }

    private int getTempoServicoPorId(long servicoId) {
        int tempo = 0;
        Cursor cursor = db.rawQuery(
                "SELECT " + DatabaseHelper.COLUMN_SERVICO_TEMPO + " FROM " + DatabaseHelper.TABLE_SERVICOS + " WHERE " + DatabaseHelper.COLUMN_SERVICO_ID + " = ?",
                new String[]{String.valueOf(servicoId)}
        );
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                tempo = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SERVICO_TEMPO));
            }
            cursor.close();
        }
        return tempo;
    }

    public double getTotalValorPeriodo(long inicio, long fim) {
        double total = 0.0;
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + DatabaseHelper.COLUMN_AGENDAMENTO_VALOR + ") FROM " + DatabaseHelper.TABLE_AGENDAMENTOS +
                        " WHERE " + DatabaseHelper.COLUMN_AGENDAMENTO_DATA_HORA_INICIO + " >= ? AND " + DatabaseHelper.COLUMN_AGENDAMENTO_DATA_HORA_INICIO + " <= ? AND " + DatabaseHelper.COLUMN_AGENDAMENTO_CANCELADO + " = 0 AND " + DatabaseHelper.COLUMN_AGENDAMENTO_VALOR + " > 0",
                new String[]{String.valueOf(inicio), String.valueOf(fim)}
        );
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    // SUM retorna o resultado na primeira coluna (índice 0)
                    if (!cursor.isNull(0)) {
                        total = cursor.getDouble(0);
                        if (Double.isNaN(total)) {
                            total = 0.0;
                        }
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return total;
    }
}
