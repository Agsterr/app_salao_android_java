package com.example.appdetestes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import java.util.Calendar;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import android.content.SharedPreferences;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.net.Uri;
import java.util.Arrays;

public class ClienteActivity extends AppCompatActivity {

    public static final String EXTRA_MOSTRAR_SERVICOS = "mostrar_servicos";

    private EditText editTextNome;
    private Button buttonSalvar;
    private Button buttonGerenciarServicos;
    private Button buttonVerAgenda;
    private Button buttonAdicionarServicoInline;
    private ListView listViewClientes;
    private ListView listViewServicos;
    private TextView textViewListaClientesLegenda;
    private TextView textViewListaServicosLegenda;
    private View layoutServicosHeader;
    private Button buttonAdicionarClienteInline;
    private View layoutClientesHeader;
    // Agenda Pessoal
    private View layoutAgendaHeader;
    private TextView buttonAgendaToggle;
    private View layoutAgendaContent;
    private Button buttonAdicionarAgendaInline;
    private ListView listViewAgenda;
    private ArrayAdapter<AgendaEntry> agendaAdapter;
    private List<AgendaEntry> agendaItems = new ArrayList<>();
    private static final String PREFS_AGENDA = "AgendaPrefs";
    private static final String PREF_AGENDA_ITEMS = "agenda_items";
    private CalendarView calendarAgendaView;
    private Button buttonToggleCalendar;
    private long agendaSelectedDateStart;
    private Spinner spinnerAgendaFilter;
    private List<AgendaEntry> agendaFilteredItems = new ArrayList<>();

    private boolean clientesAbertos = false;
    private boolean servicosAbertos = false;
    private EditText editTextBuscarClientes;
    private EditText editTextBuscarServicos;
    private ArrayAdapter<Cliente> clientesAdapter;
    private ArrayAdapter<Servico> servicosAdapter;
    private ClienteDAO clienteDAO;
    private ServicoDAO servicoDAO;
    private int contextMenuSourceViewId = -1;
    private Button buttonGerenciarProdutos;
    private Button buttonBackup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente);
        // Solicitar permissão de notificações no Android 13+
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

        clienteDAO = new ClienteDAO(this);
        clienteDAO.open();

        servicoDAO = new ServicoDAO(this);
        servicoDAO.open();

        editTextNome = findViewById(R.id.editTextNome);
        buttonSalvar = findViewById(R.id.buttonSalvar);
        buttonGerenciarServicos = findViewById(R.id.buttonGerenciarServicos);
        buttonVerAgenda = findViewById(R.id.buttonVerAgenda);
        listViewClientes = findViewById(R.id.listViewClientes);
        textViewListaClientesLegenda = findViewById(R.id.textViewListaClientesLegenda);
        listViewServicos = findViewById(R.id.listViewServicos);
        textViewListaServicosLegenda = findViewById(R.id.textViewListaServicosLegenda);
        buttonAdicionarServicoInline = findViewById(R.id.buttonAdicionarServicoInline);
        layoutServicosHeader = findViewById(R.id.layoutServicosHeader);
        buttonAdicionarClienteInline = findViewById(R.id.buttonAdicionarClienteInline);
        layoutClientesHeader = findViewById(R.id.layoutClientesHeader);
        buttonGerenciarProdutos = findViewById(R.id.buttonGerenciarProdutos);
        buttonBackup = findViewById(R.id.buttonBackup);
        // Agenda Pessoal UI
        layoutAgendaHeader = findViewById(R.id.layoutAgendaHeader);
        buttonAgendaToggle = findViewById(R.id.textViewAgendaLegenda);
        layoutAgendaContent = findViewById(R.id.layoutAgendaContent);
        buttonAdicionarAgendaInline = findViewById(R.id.buttonAdicionarAgendaInline);
        listViewAgenda = findViewById(R.id.listViewAgenda);
        calendarAgendaView = findViewById(R.id.calendarAgendaView);
        buttonToggleCalendar = findViewById(R.id.buttonToggleCalendar);
        spinnerAgendaFilter = findViewById(R.id.spinnerAgendaFilter);
        agendaSelectedDateStart = getTimestampInicioDoDia(System.currentTimeMillis());
        if (calendarAgendaView != null) {
            calendarAgendaView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                Calendar c = Calendar.getInstance();
                c.set(year, month, dayOfMonth, 0, 0, 0);
                c.set(Calendar.MILLISECOND, 0);
                agendaSelectedDateStart = c.getTimeInMillis();
            });
        }
        if (buttonToggleCalendar != null && calendarAgendaView != null) {
            // Texto inicial de acordo com a visibilidade padrão (visível)
            buttonToggleCalendar.setText("Fechar calendário");
            buttonToggleCalendar.setOnClickListener(v -> {
                if (calendarAgendaView.getVisibility() == View.VISIBLE) {
                    calendarAgendaView.setVisibility(View.GONE);
                    buttonToggleCalendar.setText("Mostrar calendário");
                } else {
                    calendarAgendaView.setVisibility(View.VISIBLE);
                    buttonToggleCalendar.setText("Fechar calendário");
                }
            });
        }

        editTextBuscarClientes = findViewById(R.id.editTextBuscarClientes);
        editTextBuscarServicos = findViewById(R.id.editTextBuscarServicos);

        clientesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        servicosAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listViewClientes.setAdapter(clientesAdapter);
        listViewServicos.setAdapter(servicosAdapter);
        // Agenda adapter agora usa lista filtrada com layout custom e cores
        agendaAdapter = new AgendaEntryAdapter(this, agendaFilteredItems);
        listViewAgenda.setAdapter(agendaAdapter);
        registerForContextMenu(listViewClientes);
        registerForContextMenu(listViewServicos);
        registerForContextMenu(listViewAgenda);

        // Configura o Spinner de filtro (Todos, Em andamento, Finalizado, Cancelado)
        if (spinnerAgendaFilter != null) {
            ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                    Arrays.asList("Todos", "Em andamento", "Finalizado", "Cancelado"));
            filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerAgendaFilter.setAdapter(filterAdapter);
            spinnerAgendaFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    applyAgendaFilter();
                }
                @Override public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        editTextBuscarClientes.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                clientesAdapter.getFilter().filter(s);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        editTextBuscarServicos.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                servicosAdapter.getFilter().filter(s);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        buttonAdicionarClienteInline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogCliente();
            }
        });

        // Toggle Agenda content e fechar outras seções
        buttonAgendaToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Fechar Clientes e Serviços ao clicar em Agenda
                if (clientesAbertos) { esconderClientes(); clientesAbertos = false; }
                if (servicosAbertos) { esconderServicos(); servicosAbertos = false; }

                // Alternar conteúdo da Agenda
                if (layoutAgendaContent.getVisibility() == View.VISIBLE) {
                    layoutAgendaContent.setVisibility(View.GONE);
                } else {
                    layoutAgendaContent.setVisibility(View.VISIBLE);
                }
            }
        });

        // Adicionar compromisso na agenda
        buttonAdicionarAgendaInline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogAdicionarAgendaInline();
            }
        });

        buttonAdicionarServicoInline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogServicoInline();
            }
        });

        buttonSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutAgendaContent != null) layoutAgendaContent.setVisibility(View.GONE);
                if (clientesAbertos) {
                    esconderClientes();
                    clientesAbertos = false;
                } else {
                    mostrarListaClientesInline();
                    clientesAbertos = true;
                    if (servicosAbertos) {
                        esconderServicos();
                        servicosAbertos = false;
                    }
                }
            }
        });

        buttonGerenciarServicos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutAgendaContent != null) layoutAgendaContent.setVisibility(View.GONE);
                if (servicosAbertos) {
                    esconderServicos();
                    servicosAbertos = false;
                } else {
                    mostrarListaServicosInline();
                    servicosAbertos = true;
                    if (clientesAbertos) {
                        esconderClientes();
                        clientesAbertos = false;
                    }
                }
            }
        });

        buttonVerAgenda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutAgendaContent != null) layoutAgendaContent.setVisibility(View.GONE);
                Intent intent = new Intent(ClienteActivity.this, AgendaActivity.class);
                startActivity(intent);
            }
        });

        buttonBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutAgendaContent != null) layoutAgendaContent.setVisibility(View.GONE);
                Intent intent = new Intent(ClienteActivity.this, BackupActivity.class);
                startActivity(intent);
            }
        });

        buttonGerenciarProdutos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutAgendaContent != null) layoutAgendaContent.setVisibility(View.GONE);
                Intent intent = new Intent(ClienteActivity.this, ProdutoActivity.class);
                startActivity(intent);
            }
        });

        atualizarListaClientes();

        // Carrega agenda e exibe bloco abaixo do cliente
        try {
            loadAgendaItems();
            mostrarAgendaInicial();
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao iniciar Agenda: " + e.getMessage(), Toast.LENGTH_LONG).show();
            esconderAgenda();
        }

        if (getIntent().getBooleanExtra(EXTRA_MOSTRAR_SERVICOS, false)) {
            mostrarListaServicosInline();
            servicosAbertos = true;
            clientesAbertos = false;
        }
    }

    private void mostrarDialogCliente() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Novo Cliente");
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_cliente, null);
        EditText editTextDialogNome = dialogView.findViewById(R.id.editTextDialogNome);
        builder.setView(dialogView);
        builder.setPositiveButton("Salvar", null);
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            Button btnSalvar = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
            btnSalvar.setOnClickListener(v -> {
                String nome = editTextDialogNome.getText().toString().trim();
                if (nome.isEmpty()) {
                    editTextDialogNome.setError("Informe o nome do cliente");
                    editTextDialogNome.requestFocus();
                    return; // Não fecha o diálogo
                }
                inserirClienteENotificar(nome);
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    private void mostrarListaClientesInline() {
        List<Cliente> clientes = clienteDAO.getAllClientes();
        if (clientesAdapter == null) {
            clientesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
            listViewClientes.setAdapter(clientesAdapter);
        }
        clientesAdapter.clear();
        for (Cliente cliente : clientes) {
            clientesAdapter.add(cliente);
        }
        clientesAdapter.notifyDataSetChanged();
        // Mostrar cabeçalho e seção de clientes
        layoutClientesHeader.setVisibility(View.VISIBLE);
        textViewListaClientesLegenda.setVisibility(View.VISIBLE);
        editTextBuscarClientes.setVisibility(View.VISIBLE);
        editTextBuscarClientes.setText("");
        listViewClientes.setVisibility(View.VISIBLE);
        // Esconder seção de serviços
        if (layoutServicosHeader != null) layoutServicosHeader.setVisibility(View.GONE);
        textViewListaServicosLegenda.setVisibility(View.GONE);
        if (buttonAdicionarServicoInline != null) buttonAdicionarServicoInline.setVisibility(View.GONE);
        editTextBuscarServicos.setVisibility(View.GONE);
        listViewServicos.setVisibility(View.GONE);
    }

    private void mostrarDialogServicoInline() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Novo Serviço");
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_servico, null);
        EditText editTextDialogNomeServico = dialogView.findViewById(R.id.editTextDialogNomeServico);
        EditText editTextDialogTempoServico = dialogView.findViewById(R.id.editTextDialogTempoServico);
        builder.setView(dialogView);
        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String nome = editTextDialogNomeServico.getText().toString().trim();
            String tempoStr = editTextDialogTempoServico.getText().toString().trim();
            if (nome.isEmpty() || tempoStr.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }
            int tempo;
            try {
                tempo = Integer.parseInt(tempoStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Tempo inválido", Toast.LENGTH_SHORT).show();
                return;
            }
            Servico servico = new Servico();
            servico.setNome(nome);
            servico.setTempo(tempo);
            long id = servicoDAO.inserirServico(servico);
            Toast.makeText(this, "Serviço salvo com ID: " + id, Toast.LENGTH_SHORT).show();
            mostrarListaServicosInline();
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    protected void onDestroy() {
        clienteDAO.close();
        if (servicoDAO != null) servicoDAO.close();
        super.onDestroy();
    }

// Opcional: após inserir novo cliente, garanta que a lista inline atualize
private void inserirClienteENotificar(String nome) {
    Cliente cliente = new Cliente();
    cliente.setNome(nome);
    long id = clienteDAO.inserirCliente(cliente);
    Toast.makeText(this, "Cliente salvo com ID: " + id, Toast.LENGTH_SHORT).show();
    mostrarListaClientesInline();
}

private void mostrarListaServicosInline() {
    List<Servico> servicos = servicoDAO.getAllServicos();
    if (servicosAdapter == null) {
        servicosAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listViewServicos.setAdapter(servicosAdapter);
    }
    servicosAdapter.clear();
    for (Servico servico : servicos) {
        servicosAdapter.add(servico);
    }
    servicosAdapter.notifyDataSetChanged();
    // Mostrar cabeçalho e seção de serviços
    if (layoutServicosHeader != null) layoutServicosHeader.setVisibility(View.VISIBLE);
    textViewListaServicosLegenda.setVisibility(View.VISIBLE);
    if (buttonAdicionarServicoInline != null) buttonAdicionarServicoInline.setVisibility(View.VISIBLE);
    editTextBuscarServicos.setVisibility(View.VISIBLE);
    editTextBuscarServicos.setText("");
    listViewServicos.setVisibility(View.VISIBLE);
    // Esconder seção de clientes
    if (layoutClientesHeader != null) layoutClientesHeader.setVisibility(View.GONE);
    textViewListaClientesLegenda.setVisibility(View.GONE);
    editTextBuscarClientes.setVisibility(View.GONE);
    listViewClientes.setVisibility(View.GONE);
}

private void esconderClientes() {
    if (layoutClientesHeader != null) layoutClientesHeader.setVisibility(View.GONE);
    textViewListaClientesLegenda.setVisibility(View.GONE);
    editTextBuscarClientes.setVisibility(View.GONE);
    listViewClientes.setVisibility(View.GONE);
}

private void esconderServicos() {
    if (layoutServicosHeader != null) layoutServicosHeader.setVisibility(View.GONE);
    textViewListaServicosLegenda.setVisibility(View.GONE);
    if (buttonAdicionarServicoInline != null) buttonAdicionarServicoInline.setVisibility(View.GONE);
    editTextBuscarServicos.setVisibility(View.GONE);
    listViewServicos.setVisibility(View.GONE);
}

private void atualizarListaClientes() {
    // Substitui chamada obsoleta por estado inicial oculto
    esconderClientes();
    esconderServicos();
}

// Context menu
@Override
public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    if (v.getId() == R.id.listViewClientes || v.getId() == R.id.listViewServicos) {
        getMenuInflater().inflate(R.menu.menu_contexto_item, menu);
        contextMenuSourceViewId = v.getId();
    } else if (v.getId() == R.id.listViewAgenda) {
        getMenuInflater().inflate(R.menu.menu_contexto_agendamento, menu);
        contextMenuSourceViewId = v.getId();
    }
}

@Override
public boolean onContextItemSelected(MenuItem item) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    int position = info.position;
    if (contextMenuSourceViewId == R.id.listViewClientes) {
        Cliente clienteSelecionado = clientesAdapter.getItem(position);
        if (item.getItemId() == R.id.menu_editar) {
            mostrarDialogEditarCliente(clienteSelecionado);
            return true;
        } else if (item.getItemId() == R.id.menu_apagar) {
            confirmarApagarCliente(clienteSelecionado);
            return true;
        }
    } else if (contextMenuSourceViewId == R.id.listViewServicos) {
        Servico servicoSelecionado = servicosAdapter.getItem(position);
        if (item.getItemId() == R.id.menu_editar) {
            mostrarDialogEditarServico(servicoSelecionado);
            return true;
        } else if (item.getItemId() == R.id.menu_apagar) {
            confirmarApagarServico(servicoSelecionado);
            return true;
        }
    } else if (contextMenuSourceViewId == R.id.listViewAgenda) {
        AgendaEntry agendaSelecionado = agendaAdapter.getItem(position);
        if (agendaSelecionado == null) return super.onContextItemSelected(item);
        if (item.getItemId() == R.id.menu_finalizar) {
            agendaSelecionado.setStatus("Finalizado");
            saveAgendaItems();
            applyAgendaFilter();
            cancelRemindersForEntryWithTs(agendaSelecionado, agendaSelecionado.getTimestamp());
            Toast.makeText(this, "Compromisso finalizado", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.menu_editar) {
            mostrarDialogEditarAgendaInline(agendaSelecionado, position);
            return true;
        } else if (item.getItemId() == R.id.menu_apagar) {
            agendaItems.remove(agendaSelecionado);
            saveAgendaItems();
            applyAgendaFilter();
            return true;
        } else if (item.getItemId() == R.id.menu_reagendar) {
            selecionarHoraParaAgendaEditar(agendaSelecionado, position);
            return true;
        } else if (item.getItemId() == R.id.menu_cancelar) {
            agendaSelecionado.setStatus("Cancelado");
            saveAgendaItems();
            applyAgendaFilter();
            cancelRemindersForEntryWithTs(agendaSelecionado, agendaSelecionado.getTimestamp());
            return true;
        }
    }
    return super.onContextItemSelected(item);
}

private void mostrarDialogEditarCliente(Cliente cliente) {
    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
    builder.setTitle("Editar Cliente");
    View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_cliente, null);
    EditText editTextDialogNome = dialogView.findViewById(R.id.editTextDialogNome);
    editTextDialogNome.setText(cliente.getNome());
    builder.setView(dialogView);
    builder.setPositiveButton("Salvar", (dialog, which) -> {
        String nome = editTextDialogNome.getText().toString().trim();
        if (nome.isEmpty()) {
            Toast.makeText(this, "Informe o nome do cliente", Toast.LENGTH_SHORT).show();
            return;
        }
        cliente.setNome(nome);
        int rows = clienteDAO.atualizarCliente(cliente);
        if (rows > 0) {
            Toast.makeText(this, "Cliente atualizado", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Falha ao atualizar cliente", Toast.LENGTH_SHORT).show();
        }
        mostrarListaClientesInline();
    });
    builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
    builder.show();
}

private void confirmarApagarCliente(Cliente cliente) {
    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
    builder.setTitle("Apagar Cliente");
    builder.setMessage("Tem certeza que deseja apagar este cliente?");
    builder.setPositiveButton("Apagar", (dialog, which) -> {
        int rows = clienteDAO.apagarCliente(cliente.getId());
        if (rows > 0) {
            Toast.makeText(this, "Cliente apagado", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Falha ao apagar cliente", Toast.LENGTH_SHORT).show();
        }
        mostrarListaClientesInline();
    });
    builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
    builder.show();
}

private void mostrarDialogEditarServico(Servico servico) {
    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
    builder.setTitle("Editar Serviço");
    View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_servico, null);
    EditText editTextDialogNomeServico = dialogView.findViewById(R.id.editTextDialogNomeServico);
    EditText editTextDialogTempoServico = dialogView.findViewById(R.id.editTextDialogTempoServico);
    editTextDialogNomeServico.setText(servico.getNome());
    editTextDialogTempoServico.setText(String.valueOf(servico.getTempo()));
    builder.setView(dialogView);
    builder.setPositiveButton("Salvar", (dialog, which) -> {
        String nome = editTextDialogNomeServico.getText().toString().trim();
        String tempoStr = editTextDialogTempoServico.getText().toString().trim();
        if (nome.isEmpty() || tempoStr.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }
        int tempo;
        try {
            tempo = Integer.parseInt(tempoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Tempo inválido", Toast.LENGTH_SHORT).show();
            return;
        }
        servico.setNome(nome);
        servico.setTempo(tempo);
        int rows = servicoDAO.atualizarServico(servico);
        if (rows > 0) {
            Toast.makeText(this, "Serviço atualizado", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Falha ao atualizar serviço", Toast.LENGTH_SHORT).show();
        }
        mostrarListaServicosInline();
    });
    builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
    builder.show();
}

private void confirmarApagarServico(Servico servico) {
    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
    builder.setTitle("Apagar Serviço");
    builder.setMessage("Tem certeza que deseja apagar este serviço?");
    builder.setPositiveButton("Apagar", (dialog, which) -> {
        int rows = servicoDAO.apagarServico(servico.getId());
        if (rows > 0) {
            Toast.makeText(this, "Serviço apagado", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Falha ao apagar serviço", Toast.LENGTH_SHORT).show();
        }
        mostrarListaServicosInline();
    });
    builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
    builder.show();
}

    private void mostrarAgendaInicial() {
        if (layoutAgendaHeader != null) layoutAgendaHeader.setVisibility(View.VISIBLE);
        if (layoutAgendaContent != null) layoutAgendaContent.setVisibility(View.GONE);
    }

    private void esconderAgenda() {
        if (layoutAgendaHeader != null) layoutAgendaHeader.setVisibility(View.GONE);
        if (layoutAgendaContent != null) layoutAgendaContent.setVisibility(View.GONE);
    }

    private void mostrarDialogAdicionarAgendaInline() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Novo compromisso");
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_agenda_inline, null);
        EditText inputTitulo = dialogView.findViewById(R.id.editTextAgendaTitulo);
        Spinner spinnerStatus = dialogView.findViewById(R.id.spinnerAgendaStatus);
        CheckBox checkBoxTemLocal = dialogView.findViewById(R.id.checkBoxTemLocal);
        LinearLayout layoutEndereco = dialogView.findViewById(R.id.layoutEndereco);
        EditText editTextEndereco = dialogView.findViewById(R.id.editTextAgendaEndereco);
        Button buttonAbrirMaps = dialogView.findViewById(R.id.buttonAbrirMaps);
        android.widget.TimePicker timePickerPrevDayReminder = dialogView.findViewById(R.id.timePickerPrevDayReminder);
        timePickerPrevDayReminder.setIs24HourView(true);
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            timePickerPrevDayReminder.setHour(20);
            timePickerPrevDayReminder.setMinute(0);
        } else {
            timePickerPrevDayReminder.setCurrentHour(20);
            timePickerPrevDayReminder.setCurrentMinute(0);
        }
    
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList("Em andamento", "Finalizado", "Cancelado"));
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);
    
        checkBoxTemLocal.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutEndereco.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
    
        buttonAbrirMaps.setOnClickListener(v -> {
            String query = editTextEndereco.getText().toString().trim();
            if (query.isEmpty()) {
                Toast.makeText(this, "Digite um endereço para buscar", Toast.LENGTH_SHORT).show();
                return;
            }
            Uri gmmIntentUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(query));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            try {
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            } catch (Exception e) {
                startActivity(new Intent(Intent.ACTION_VIEW, gmmIntentUri));
            }
        });
    
        builder.setView(dialogView);
        // Não fechar automaticamente; vamos validar antes de permitir escolher horário
        builder.setPositiveButton("Escolher horário", null);
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        androidx.appcompat.app.AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(dlg -> {
            android.widget.Button positiveButton = alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String titulo = inputTitulo.getText().toString().trim();
                String status = spinnerStatus.getSelectedItem() != null ? spinnerStatus.getSelectedItem().toString().trim() : "";
                boolean temLocal = checkBoxTemLocal.isChecked();
                String endereco = temLocal ? editTextEndereco.getText().toString().trim() : "";
                if (titulo.isEmpty()) {
                    Toast.makeText(this, "Informe o título", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (status.isEmpty()) {
                    Toast.makeText(this, "Selecione o status", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (temLocal && endereco.isEmpty()) {
                    Toast.makeText(this, "Informe o endereço", Toast.LENGTH_SHORT).show();
                    return;
                }
                int reminderHour, reminderMinute;
                if (android.os.Build.VERSION.SDK_INT >= 23) {
                    reminderHour = timePickerPrevDayReminder.getHour();
                    reminderMinute = timePickerPrevDayReminder.getMinute();
                } else {
                    reminderHour = timePickerPrevDayReminder.getCurrentHour();
                    reminderMinute = timePickerPrevDayReminder.getCurrentMinute();
                }
                alertDialog.dismiss();
                selecionarHoraParaAgendaNovo(titulo, status, endereco, reminderHour, reminderMinute);
            });
        });
        alertDialog.show();
    }

    private void selecionarDataHora(String titulo) {
        final Calendar cal = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            TimePickerDialog tpd = new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                cal.set(Calendar.MINUTE, minute);
                long ts = cal.getTimeInMillis();
                AgendaEntry entry = new AgendaEntry(titulo, ts);
                agendaItems.add(entry);
                agendaAdapter.notifyDataSetChanged();
                saveAgendaItems();
                Toast.makeText(this, "Compromisso adicionado", Toast.LENGTH_SHORT).show();
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
            tpd.show();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    private void loadAgendaItems() {
        SharedPreferences prefs = getSharedPreferences(PREFS_AGENDA, Context.MODE_PRIVATE);
        String json = prefs.getString(PREF_AGENDA_ITEMS, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            agendaItems.clear();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                agendaItems.add(AgendaEntry.fromJson(obj));
            }
            if (agendaAdapter != null) applyAgendaFilter();
        } catch (JSONException e) {
            // ignora erro e mantém lista vazia
        }
    }

    private void applyAgendaFilter() {
        String filtro = "Todos";
        if (spinnerAgendaFilter != null && spinnerAgendaFilter.getSelectedItem() != null) {
            filtro = spinnerAgendaFilter.getSelectedItem().toString();
        }
        agendaFilteredItems.clear();
        for (AgendaEntry e : agendaItems) {
            String status = e.getStatus() == null ? "" : e.getStatus();
            if ("Todos".equals(filtro)) {
                agendaFilteredItems.add(e);
            } else if ("Cancelado".equals(filtro)) {
                if ("Cancelado".equals(status)) agendaFilteredItems.add(e);
            } else {
                if (!"Cancelado".equals(status) && filtro.equals(status)) agendaFilteredItems.add(e);
            }
        }
        agendaAdapter.notifyDataSetChanged();
    }

    private void saveAgendaItems() {
        SharedPreferences prefs = getSharedPreferences(PREFS_AGENDA, Context.MODE_PRIVATE);
        JSONArray arr = new JSONArray();
        for (AgendaEntry e : agendaItems) {
            try {
                arr.put(e.toJson());
            } catch (JSONException ignore) {}
        }
        prefs.edit().putString(PREF_AGENDA_ITEMS, arr.toString()).apply();
    }

    private void selecionarHoraParaAgendaNovo(String titulo, String status, String endereco, int reminderHour, int reminderMinute) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(agendaSelectedDateStart);
        TimePickerDialog tpd = new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
            cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long ts = cal.getTimeInMillis();
            AgendaEntry entry = new AgendaEntry(titulo, ts, status, endereco, reminderHour, reminderMinute);
            agendaItems.add(entry);
            saveAgendaItems();
            applyAgendaFilter();
            scheduleRemindersForEntry(entry);
            Toast.makeText(this, "Compromisso adicionado", Toast.LENGTH_SHORT).show();
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
        tpd.show();
    }

    private void mostrarDialogEditarAgendaInline(AgendaEntry entry, int position) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Editar compromisso");
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_agenda_inline, null);
        EditText inputTitulo = dialogView.findViewById(R.id.editTextAgendaTitulo);
        Spinner spinnerStatus = dialogView.findViewById(R.id.spinnerAgendaStatus);
        CheckBox checkBoxTemLocal = dialogView.findViewById(R.id.checkBoxTemLocal);
        LinearLayout layoutEndereco = dialogView.findViewById(R.id.layoutEndereco);
        EditText editTextEndereco = dialogView.findViewById(R.id.editTextAgendaEndereco);
        Button buttonAbrirMaps = dialogView.findViewById(R.id.buttonAbrirMaps);
         android.widget.TimePicker timePickerPrevDayReminder = dialogView.findViewById(R.id.timePickerPrevDayReminder);
        timePickerPrevDayReminder.setIs24HourView(true);
        int initHour = entry.getReminderPrevDayHour();
        int initMinute = entry.getReminderPrevDayMinute();
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            timePickerPrevDayReminder.setHour(initHour);
            timePickerPrevDayReminder.setMinute(initMinute);
        } else {
            timePickerPrevDayReminder.setCurrentHour(initHour);
            timePickerPrevDayReminder.setCurrentMinute(initMinute);
        }

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList("Em andamento", "Finalizado", "Cancelado"));
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        inputTitulo.setText(entry.getTitle());
        int statusPos;
        if ("Finalizado".equals(entry.getStatus())) {
            statusPos = 1;
        } else if ("Cancelado".equals(entry.getStatus())) {
            statusPos = 2;
        } else {
            statusPos = 0;
        }
        spinnerStatus.setSelection(statusPos);
        if (entry.getEndereco() != null && !entry.getEndereco().isEmpty()) {
            checkBoxTemLocal.setChecked(true);
            layoutEndereco.setVisibility(View.VISIBLE);
            editTextEndereco.setText(entry.getEndereco());
        } else {
            checkBoxTemLocal.setChecked(false);
            layoutEndereco.setVisibility(View.GONE);
        }

        buttonAbrirMaps.setOnClickListener(v -> {
            String query = editTextEndereco.getText().toString().trim();
            if (query.isEmpty()) {
                Toast.makeText(this, "Digite um endereço para buscar", Toast.LENGTH_SHORT).show();
                return;
            }
            Uri gmmIntentUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(query));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            try {
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            } catch (Exception e) {
                startActivity(new Intent(Intent.ACTION_VIEW, gmmIntentUri));
            }
        });

        builder.setView(dialogView);
        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String titulo = inputTitulo.getText().toString().trim();
            if (titulo.isEmpty()) {
                Toast.makeText(this, "Informe o título", Toast.LENGTH_SHORT).show();
                return;
            }
            String status = (String) spinnerStatus.getSelectedItem();
            String endereco = checkBoxTemLocal.isChecked() ? editTextEndereco.getText().toString().trim() : "";
            int reminderHour, reminderMinute;
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                reminderHour = timePickerPrevDayReminder.getHour();
                reminderMinute = timePickerPrevDayReminder.getMinute();
            } else {
                reminderHour = timePickerPrevDayReminder.getCurrentHour();
                reminderMinute = timePickerPrevDayReminder.getCurrentMinute();
            }
            entry.setTitle(titulo);
            entry.setStatus(status);
            entry.setEndereco(endereco);
            entry.setReminderPrevDayHour(reminderHour);
            entry.setReminderPrevDayMinute(reminderMinute);
            saveAgendaItems();
            applyAgendaFilter();
            if ("Cancelado".equals(status) || "Finalizado".equals(status)) {
                cancelRemindersForEntryWithTs(entry, entry.getTimestamp());
            } else {
                scheduleRemindersForEntry(entry);
            }
        });
        builder.setNeutralButton("Alterar horário", (dialog, which) -> {
            selecionarHoraParaAgendaEditar(entry, position);
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void selecionarHoraParaAgendaEditar(AgendaEntry entry, int position) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(entry.getTimestamp());
        TimePickerDialog tpd = new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
            long oldTs = entry.getTimestamp();
            Calendar base = Calendar.getInstance();
            base.setTimeInMillis(agendaSelectedDateStart);
            base.set(Calendar.HOUR_OF_DAY, hourOfDay);
            base.set(Calendar.MINUTE, minute);
            base.set(Calendar.SECOND, 0);
            base.set(Calendar.MILLISECOND, 0);
            cancelRemindersForEntryWithTs(entry, oldTs);
            entry.setTimestamp(base.getTimeInMillis());
            saveAgendaItems();
            applyAgendaFilter();
            scheduleRemindersForEntry(entry);
            Toast.makeText(this, "Horário atualizado", Toast.LENGTH_SHORT).show();
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
        tpd.show();
    }

    private long getTimestampInicioDoDia(long ts) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ts);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }


private void scheduleRemindersForEntry(AgendaEntry entry) {
    long now = System.currentTimeMillis();
    long eventTs = entry.getTimestamp();
    Calendar prev = Calendar.getInstance();
    prev.setTimeInMillis(eventTs);
    prev.add(Calendar.DAY_OF_YEAR, -1);
    prev.set(Calendar.HOUR_OF_DAY, entry.getReminderPrevDayHour());
    prev.set(Calendar.MINUTE, entry.getReminderPrevDayMinute());
    prev.set(Calendar.SECOND, 0);
    prev.set(Calendar.MILLISECOND, 0);
    long prevTs = prev.getTimeInMillis();

    long dayOfTs = eventTs - (90L * 60L * 1000L); // 1h30 antes

    if (prevTs > now) {
        scheduleAlarm(entry, prevTs, "previous_day", 1);
    }
    if (dayOfTs > now) {
        scheduleAlarm(entry, dayOfTs, "day_of", 2);
    }
}

private void scheduleAlarm(AgendaEntry entry, long triggerAtMillis, String type, int typeCode) {
    android.app.AlarmManager alarmManager = (android.app.AlarmManager) getSystemService(Context.ALARM_SERVICE);
    if (alarmManager == null) return;
    Intent intent = new Intent(this, AgendaReminderReceiver.class);
    intent.putExtra("title", entry.getTitle());
    intent.putExtra("timestamp", entry.getTimestamp());
    intent.putExtra("endereco", entry.getEndereco());
    intent.putExtra("type", type);
    int baseCode = (int) (entry.getTimestamp() % Integer.MAX_VALUE);
    int requestCode = baseCode + typeCode;
    android.app.PendingIntent pi = android.app.PendingIntent.getBroadcast(this, requestCode, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);

    try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
        } else {
            alarmManager.setExact(android.app.AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
        }
    } catch (SecurityException se) {
        // Sem permissão de alarme exato: usa agendamento aproximado e sugere habilitar.
        alarmManager.set(android.app.AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            try {
                android.content.Intent permIntent = new android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                permIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(permIntent);
                android.widget.Toast.makeText(this, "Habilite 'Alarmes exatos' para lembretes precisos.", android.widget.Toast.LENGTH_LONG).show();
            } catch (Exception ignored) {}
        }
    }
}

private void cancelRemindersForEntryWithTs(AgendaEntry entry, long oldTs) {
    android.app.AlarmManager alarmManager = (android.app.AlarmManager) getSystemService(Context.ALARM_SERVICE);
    if (alarmManager == null) return;
    Intent intentPrev = new Intent(this, AgendaReminderReceiver.class);
    intentPrev.putExtra("title", entry.getTitle());
    intentPrev.putExtra("timestamp", oldTs);
    intentPrev.putExtra("endereco", entry.getEndereco());
    intentPrev.putExtra("type", "previous_day");
    int baseCodePrev = (int) (oldTs % Integer.MAX_VALUE);
    android.app.PendingIntent piPrev = android.app.PendingIntent.getBroadcast(this, baseCodePrev + 1, intentPrev, android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);

    Intent intentDayOf = new Intent(this, AgendaReminderReceiver.class);
    intentDayOf.putExtra("title", entry.getTitle());
    intentDayOf.putExtra("timestamp", oldTs);
    intentDayOf.putExtra("endereco", entry.getEndereco());
    intentDayOf.putExtra("type", "day_of");
    int baseCodeDay = (int) (oldTs % Integer.MAX_VALUE);
    android.app.PendingIntent piDay = android.app.PendingIntent.getBroadcast(this, baseCodeDay + 2, intentDayOf, android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);

    alarmManager.cancel(piPrev);
    alarmManager.cancel(piDay);
}
}
