package com.example.appdetestes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class ClienteActivity extends AppCompatActivity {

    private EditText editTextNome;
    private Button buttonSalvar;
    private Button buttonGerenciarServicos;
    private Button buttonVerAgenda;
    private ListView listViewClientes;

    private ClienteDAO clienteDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente);

        clienteDAO = new ClienteDAO(this);
        clienteDAO.open();

        editTextNome = findViewById(R.id.editTextNome);
        buttonSalvar = findViewById(R.id.buttonSalvar);
        buttonGerenciarServicos = findViewById(R.id.buttonGerenciarServicos);
        buttonVerAgenda = findViewById(R.id.buttonVerAgenda);
        listViewClientes = findViewById(R.id.listViewClientes);

        buttonSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarCliente();
            }
        });

        buttonGerenciarServicos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClienteActivity.this, ServicoActivity.class);
                startActivity(intent);
            }
        });

        buttonVerAgenda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClienteActivity.this, AgendaActivity.class);
                startActivity(intent);
            }
        });

        atualizarListaClientes();
    }

    private void salvarCliente() {
        String nome = editTextNome.getText().toString();
        if (nome.isEmpty()) {
            Toast.makeText(this, "Informe o nome do cliente", Toast.LENGTH_SHORT).show();
            return;
        }

        Cliente cliente = new Cliente();
        cliente.setNome(nome);

        long id = clienteDAO.inserirCliente(cliente);
        Toast.makeText(this, "Cliente salvo com ID: " + id, Toast.LENGTH_SHORT).show();

        editTextNome.setText("");
        atualizarListaClientes();
    }

    private void atualizarListaClientes() {
        List<Cliente> clientes = clienteDAO.getAllClientes();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        for (Cliente cliente : clientes) {
            adapter.add(cliente.getNome());
        }
        listViewClientes.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        clienteDAO.close();
        super.onDestroy();
    }
}
