package com.example.appdetestes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class ClienteActivity extends AppCompatActivity {

    private ListView listViewClientes;
    private Button buttonAdicionarCliente;
    private Button buttonVoltar;
    private TextInputEditText editTextBuscarClientes;
    private ArrayAdapter<Cliente> clientesAdapter;
    private ClienteDAO clienteDAO;
    private List<Cliente> todosClientes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        clienteDAO = new ClienteDAO(this);
        clienteDAO.open();

        bindViews();
        setupListeners();
        atualizarListaClientes();
        registerForContextMenu(listViewClientes);
    }

    private void bindViews() {
        listViewClientes = findViewById(R.id.listViewClientes);
        buttonAdicionarCliente = findViewById(R.id.buttonAdicionarCliente);
        buttonVoltar = findViewById(R.id.buttonVoltar);
        editTextBuscarClientes = findViewById(R.id.editTextBuscarClientes);
    }

    private void setupListeners() {
        buttonAdicionarCliente.setOnClickListener(v -> mostrarDialogCliente());
        
        if (buttonVoltar != null) {
            buttonVoltar.setOnClickListener(v -> finish());
        }

        editTextBuscarClientes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarClientes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void atualizarListaClientes() {
        todosClientes = clienteDAO.getAllClientes();
        if (clientesAdapter == null) {
            clientesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
            listViewClientes.setAdapter(clientesAdapter);
        }
        filtrarClientes(editTextBuscarClientes.getText().toString());
    }

    private void filtrarClientes(String filtro) {
        clientesAdapter.clear();
        if (filtro == null || filtro.trim().isEmpty()) {
            for (Cliente cliente : todosClientes) {
                clientesAdapter.add(cliente);
            }
        } else {
            String filtroLower = filtro.toLowerCase();
            for (Cliente cliente : todosClientes) {
                if (cliente.getNome() != null && cliente.getNome().toLowerCase().contains(filtroLower)) {
                    clientesAdapter.add(cliente);
                }
            }
        }
        clientesAdapter.notifyDataSetChanged();
    }

    private void mostrarDialogCliente() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Novo Cliente");
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_cliente, null);
        android.widget.EditText editTextDialogNome = dialogView.findViewById(R.id.editTextDialogNome);
        builder.setView(dialogView);
        builder.setPositiveButton("Salvar", null);
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            Button btnSalvar = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnSalvar.setOnClickListener(v -> {
                String nome = editTextDialogNome.getText().toString().trim();
                if (nome.isEmpty()) {
                    editTextDialogNome.setError("Informe o nome do cliente");
                    editTextDialogNome.requestFocus();
                    return;
                }
                inserirClienteENotificar(nome);
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    private void inserirClienteENotificar(String nome) {
        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        long id = clienteDAO.inserirCliente(cliente);
        Toast.makeText(this, "Cliente salvo com ID: " + id, Toast.LENGTH_SHORT).show();
        atualizarListaClientes();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.listViewClientes) {
            getMenuInflater().inflate(R.menu.menu_contexto_item, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        Cliente clienteSelecionado = clientesAdapter.getItem(position);
        
        if (item.getItemId() == R.id.menu_editar) {
            mostrarDialogEditarCliente(clienteSelecionado);
            return true;
        } else if (item.getItemId() == R.id.menu_apagar) {
            confirmarApagarCliente(clienteSelecionado);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void mostrarDialogEditarCliente(Cliente cliente) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Cliente");
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_cliente, null);
        android.widget.EditText editTextDialogNome = dialogView.findViewById(R.id.editTextDialogNome);
        editTextDialogNome.setText(cliente.getNome());
        builder.setView(dialogView);
        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String nome = editTextDialogNome.getText().toString().trim();
            if (nome.isEmpty()) {
                Toast.makeText(this, "Informe o nome do cliente", Toast.LENGTH_SHORT).show();
                return;
            }
            cliente.setNome(nome);
            clienteDAO.atualizarCliente(cliente);
            Toast.makeText(this, "Cliente atualizado", Toast.LENGTH_SHORT).show();
            atualizarListaClientes();
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void confirmarApagarCliente(Cliente cliente) {
        new AlertDialog.Builder(this)
            .setTitle("Confirmar Exclusão")
            .setMessage("Tem certeza que deseja apagar o cliente \"" + cliente.getNome() + "\"?")
            .setPositiveButton("Sim", (dialog, which) -> {
                clienteDAO.apagarCliente(cliente.getId());
                Toast.makeText(this, "Cliente apagado", Toast.LENGTH_SHORT).show();
                atualizarListaClientes();
            })
            .setNegativeButton("Não", null)
            .show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        clienteDAO.close();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        atualizarListaClientes();
    }
}
