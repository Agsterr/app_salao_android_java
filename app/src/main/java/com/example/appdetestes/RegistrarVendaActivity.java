package com.example.appdetestes;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.appcompat.app.AlertDialog;

public class RegistrarVendaActivity extends AppCompatActivity {

    private TextView textViewProduto;
    private EditText editTextValorVenda;
    private EditText editTextQuantidade;
    private Spinner spinnerFormaPagamento; // Dinheiro, Crédito
    private RadioGroup radioGroupTipoPagamento; // À vista, A prazo
    private RadioButton radioAvista;
    private RadioButton radioAprazo;
    private EditText editTextNumeroParcelas;
    private EditText editTextDataPrimeiraParcela;
    private Button buttonSalvarVenda;
    private Button buttonCancelarVenda; // novo
    private Spinner spinnerCliente;
    private Button buttonNovoCliente;

    private long produtoId;
    private ProdutoDAO produtoDAO;
    private VendaDAO vendaDAO;
    private ClienteDAO clienteDAO;
    private VendaItemDAO vendaItemDAO;

    private Long dataPrimeiraParcelaMillis = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_venda);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Registrar Venda");
        }

        produtoDAO = new ProdutoDAO(this);
        produtoDAO.open();
        vendaDAO = new VendaDAO(this);
        vendaDAO.open();
        clienteDAO = new ClienteDAO(this);
        clienteDAO.open();
        vendaItemDAO = new VendaItemDAO(this);
        vendaItemDAO.open();

        textViewProduto = findViewById(R.id.textViewProduto);
        editTextValorVenda = findViewById(R.id.editTextValorVenda);
        editTextQuantidade = findViewById(R.id.editTextQuantidade);
        spinnerFormaPagamento = findViewById(R.id.spinnerFormaPagamento);
        radioGroupTipoPagamento = findViewById(R.id.radioGroupTipoPagamento);
        radioAvista = findViewById(R.id.radioAvista);
        radioAprazo = findViewById(R.id.radioAprazo);
        editTextNumeroParcelas = findViewById(R.id.editTextNumeroParcelas);
        editTextDataPrimeiraParcela = findViewById(R.id.editTextDataPrimeiraParcela);
        buttonSalvarVenda = findViewById(R.id.buttonSalvarVenda);
        buttonCancelarVenda = findViewById(R.id.buttonCancelarVenda);
        spinnerCliente = findViewById(R.id.spinnerCliente);
        buttonNovoCliente = findViewById(R.id.buttonNovoCliente);

        ArrayAdapter<String> formaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Selecione a forma", "Dinheiro", "Crédito", "Pix"});
        formaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFormaPagamento.setAdapter(formaAdapter);
        spinnerFormaPagamento.setSelection(0);

        // Prepara lista de clientes com placeholder
        java.util.List<Cliente> clientes = new java.util.ArrayList<>();
        clientes.add(new Cliente(0, "Selecione um cliente", null));
        clientes.addAll(clienteDAO.getAllClientes());
        ArrayAdapter<Cliente> clienteAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, clientes);
        clienteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCliente.setAdapter(clienteAdapter);
        spinnerCliente.setSelection(0);

        buttonNovoCliente.setOnClickListener(v -> mostrarDialogNovoCliente(clienteAdapter));
        buttonCancelarVenda.setOnClickListener(v -> finish());
        produtoId = getIntent().getLongExtra("produto_id", -1);
        if (produtoId <= 0) {
            Toast.makeText(this, "Produto inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Produto produto = produtoDAO.getProdutoById(produtoId);
        if (produto == null) {
            Toast.makeText(this, "Produto não encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        textViewProduto.setText("Produto: " + produto.getNome() + " (" + nf.format(produto.getValorPadrao()) + ")");
        editTextValorVenda.setText(String.valueOf(produto.getValorPadrao()));
        if (editTextQuantidade != null) editTextQuantidade.setText("1");

        radioGroupTipoPagamento.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioAvista) {
                editTextNumeroParcelas.setVisibility(View.GONE);
                editTextDataPrimeiraParcela.setVisibility(View.GONE);
            } else if (checkedId == R.id.radioAprazo) {
                editTextNumeroParcelas.setVisibility(View.VISIBLE);
                editTextDataPrimeiraParcela.setVisibility(View.VISIBLE);
            }
        });

        editTextDataPrimeiraParcela.setOnClickListener(v -> mostrarDatePicker());

        buttonSalvarVenda.setOnClickListener(v -> salvarVenda());
    }

    @Override
    protected void onDestroy() {
        produtoDAO.close();
        vendaDAO.close();
        if (clienteDAO != null) clienteDAO.close();
        if (vendaItemDAO != null) vendaItemDAO.close();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void mostrarDatePicker() {
        final Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (DatePicker view, int year, int month, int dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth, 0, 0, 0);
            selected.set(Calendar.MILLISECOND, 0);
            dataPrimeiraParcelaMillis = selected.getTimeInMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
            editTextDataPrimeiraParcela.setText(sdf.format(selected.getTime()));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void salvarVenda() {
        // Limpa erros anteriores
        editTextValorVenda.setError(null);
        editTextNumeroParcelas.setError(null);
        editTextDataPrimeiraParcela.setError(null);
        if (editTextQuantidade != null) editTextQuantidade.setError(null);

        // Cliente obrigatório
        if (spinnerCliente.getAdapter() == null || spinnerCliente.getAdapter().getCount() <= 1) {
            Toast.makeText(this, "Nenhum cliente cadastrado. Crie um cliente.", Toast.LENGTH_SHORT).show();
            buttonNovoCliente.performClick();
            return;
        }
        int posCliente = spinnerCliente.getSelectedItemPosition();
        if (posCliente <= 0) {
            Toast.makeText(this, "Selecione um cliente", Toast.LENGTH_SHORT).show();
            spinnerCliente.requestFocus();
            return;
        }
        Cliente clienteSelecionado = (Cliente) spinnerCliente.getSelectedItem();

        // Forma de pagamento obrigatória
        int posForma = spinnerFormaPagamento.getSelectedItemPosition();
        if (posForma <= 0) {
            Toast.makeText(this, "Selecione a forma de pagamento", Toast.LENGTH_SHORT).show();
            spinnerFormaPagamento.requestFocus();
            return;
        }
        String formaSelecionada = spinnerFormaPagamento.getSelectedItem().toString();

        // Quantidade obrigatória e válida
        String qtdStr = editTextQuantidade.getText().toString().trim();
        if (qtdStr.isEmpty()) {
            editTextQuantidade.setError("Informe a quantidade");
            editTextQuantidade.requestFocus();
            return;
        }
        int quantidade;
        try {
            quantidade = Integer.parseInt(qtdStr);
        } catch (NumberFormatException e) {
            editTextQuantidade.setError("Quantidade inválida");
            editTextQuantidade.requestFocus();
            return;
        }
        if (quantidade <= 0) {
            editTextQuantidade.setError("Deve ser maior que zero");
            editTextQuantidade.requestFocus();
            return;
        }

        // Valor unitário obrigatório e válido
        String valorStr = editTextValorVenda.getText().toString().trim();
        if (valorStr.isEmpty()) {
            editTextValorVenda.setError("Informe o valor unitário");
            editTextValorVenda.requestFocus();
            return;
        }
        double valorUnitario;
        try {
            valorUnitario = Double.parseDouble(valorStr);
        } catch (NumberFormatException e) {
            editTextValorVenda.setError("Valor inválido");
            editTextValorVenda.requestFocus();
            return;
        }
        if (valorUnitario <= 0) {
            editTextValorVenda.setError("O valor deve ser maior que zero");
            editTextValorVenda.requestFocus();
            return;
        }

        double total = quantidade * valorUnitario;

        int checkedId = radioGroupTipoPagamento.getCheckedRadioButtonId();
        if (checkedId == R.id.radioAvista) {
            // Observação inclui forma de pagamento
            String observacao = "Forma: " + formaSelecionada;
            long dataVenda = System.currentTimeMillis();
            long vendaId = vendaDAO.registrarVendaAVista(produtoId, total, dataVenda, clienteSelecionado.getId(), observacao);
            if (vendaId > 0) {
                // Registrar item único com quantidade e valor unitário
                VendaItem item = new VendaItem();
                item.setVendaId(vendaId);
                item.setProdutoId(produtoId);
                item.setQuantidade(quantidade);
                item.setValorUnitario(valorUnitario);
                vendaItemDAO.insert(item);

                Toast.makeText(this, "Venda à vista registrada", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Erro ao registrar venda", Toast.LENGTH_SHORT).show();
            }
        } else if (checkedId == R.id.radioAprazo) {
            String parcelasStr = editTextNumeroParcelas.getText().toString().trim();
            if (parcelasStr.isEmpty()) {
                editTextNumeroParcelas.setError("Informe o número de parcelas");
                editTextNumeroParcelas.requestFocus();
                return;
            }
            int numeroParcelas;
            try {
                numeroParcelas = Integer.parseInt(parcelasStr);
            } catch (NumberFormatException e) {
                editTextNumeroParcelas.setError("Número de parcelas inválido");
                editTextNumeroParcelas.requestFocus();
                return;
            }
            if (numeroParcelas <= 0) {
                editTextNumeroParcelas.setError("Deve ser maior que zero");
                editTextNumeroParcelas.requestFocus();
                return;
            }

            if (dataPrimeiraParcelaMillis == null) {
                editTextDataPrimeiraParcela.setError("Informe a data da primeira parcela");
                editTextDataPrimeiraParcela.requestFocus();
                return;
            }

            String observacao = "Forma: " + formaSelecionada;
            long dataVenda = System.currentTimeMillis();
            long vendaId = vendaDAO.registrarVendaAPrazo(produtoId, total, dataVenda, numeroParcelas, dataPrimeiraParcelaMillis, clienteSelecionado.getId(), observacao);
            if (vendaId > 0) {
                // Registrar item único com quantidade e valor unitário
                VendaItem item = new VendaItem();
                item.setVendaId(vendaId);
                item.setProdutoId(produtoId);
                item.setQuantidade(quantidade);
                item.setValorUnitario(valorUnitario);
                vendaItemDAO.insert(item);

                Toast.makeText(this, "Venda a prazo registrada", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Erro ao registrar venda", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Selecione o tipo de pagamento", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarDialogNovoCliente(ArrayAdapter<Cliente> clienteAdapter) {
        final EditText input = new EditText(this);
        input.setHint("Nome do cliente");
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Novo Cliente")
                .setView(input)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", null)
                .create();
        dialog.setOnShowListener(d -> {
            Button btnSalvar = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnSalvar.setOnClickListener(v -> {
                String nome = input.getText().toString().trim();
                if (nome.isEmpty()) {
                    input.setError("Informe o nome");
                    input.requestFocus();
                    return; // Não fecha
                }
                Cliente novo = new Cliente();
                novo.setNome(nome);
                long id = clienteDAO.inserirCliente(novo);
                novo.setId(id);
                clienteAdapter.add(novo);
                clienteAdapter.notifyDataSetChanged();
                spinnerCliente.setSelection(clienteAdapter.getPosition(novo));
                dialog.dismiss();
            });
        });
        dialog.show();
    }
}