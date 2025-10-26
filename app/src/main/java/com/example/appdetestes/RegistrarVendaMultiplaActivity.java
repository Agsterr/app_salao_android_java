package com.example.appdetestes;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RegistrarVendaMultiplaActivity extends AppCompatActivity {

    private Spinner spinnerCliente;
    private Button buttonNovoCliente;
    private Button buttonAdicionarProduto;
    private ListView listViewItensVenda;
    private TextView textViewTotal;
    private Spinner spinnerFormaPagamento;
    private RadioGroup radioGroupTipoPagamento;
    private RadioButton radioAvista;
    private RadioButton radioAprazo;
    private EditText editTextNumeroParcelas;
    private EditText editTextDataPrimeiraParcela;
    private Button buttonSalvarVenda;
    private Button buttonCancelarVenda;

    private ClienteDAO clienteDAO;
    private ProdutoDAO produtoDAO;
    private VendaDAO vendaDAO;
    private VendaItemDAO vendaItemDAO;

    private List<VendaItem> itensVenda = new ArrayList<>();
    private ArrayAdapter<String> itensAdapter;

    private Long dataPrimeiraParcelaMillis = null;
    private long vendaIdEdit = -1;

    private final NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_venda_multipla);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Registrar Venda");
        }

        clienteDAO = new ClienteDAO(this);
        clienteDAO.open();
        produtoDAO = new ProdutoDAO(this);
        produtoDAO.open();
        vendaDAO = new VendaDAO(this);
        vendaDAO.open();
        vendaItemDAO = new VendaItemDAO(this);
        vendaItemDAO.open();

        spinnerCliente = findViewById(R.id.spinnerCliente);
        buttonNovoCliente = findViewById(R.id.buttonNovoCliente);
        buttonAdicionarProduto = findViewById(R.id.buttonAdicionarProduto);
        listViewItensVenda = findViewById(R.id.listViewItensVenda);
        textViewTotal = findViewById(R.id.textViewTotal);
        spinnerFormaPagamento = findViewById(R.id.spinnerFormaPagamento);
        radioGroupTipoPagamento = findViewById(R.id.radioGroupTipoPagamento);
        radioAvista = findViewById(R.id.radioAvista);
        radioAprazo = findViewById(R.id.radioAprazo);
        editTextNumeroParcelas = findViewById(R.id.editTextNumeroParcelas);
        editTextDataPrimeiraParcela = findViewById(R.id.editTextDataPrimeiraParcela);
        buttonSalvarVenda = findViewById(R.id.buttonSalvarVenda);
        buttonCancelarVenda = findViewById(R.id.buttonCancelarVenda);

        // Clientes com placeholder
        List<Cliente> clientes = clienteDAO.getAllClientes();
        List<Cliente> clientesComPlaceholder = new ArrayList<>();
        Cliente placeholder = new Cliente();
        placeholder.setId(0);
        placeholder.setNome("Selecione um cliente");
        clientesComPlaceholder.add(placeholder);
        clientesComPlaceholder.addAll(clientes);
        ArrayAdapter<Cliente> clienteAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, clientesComPlaceholder);
        clienteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCliente.setAdapter(clienteAdapter);
        spinnerCliente.setSelection(0);

        buttonNovoCliente.setOnClickListener(v -> mostrarDialogNovoCliente());

        // Forma de pagamento
        ArrayAdapter<String> formaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Dinheiro", "Crédito", "Pix"});
        formaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFormaPagamento.setAdapter(formaAdapter);

        // Tipo pagamento
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

        itensAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listViewItensVenda.setAdapter(itensAdapter);

        buttonAdicionarProduto.setOnClickListener(v -> mostrarDialogAdicionarItem());

        buttonSalvarVenda.setOnClickListener(v -> salvarVenda());
        buttonCancelarVenda.setOnClickListener(v -> finish());

        // Suporte a edição de venda existente
        vendaIdEdit = getIntent().getLongExtra("venda_id", -1);
        if (vendaIdEdit != -1) {
            carregarVendaExistente(vendaIdEdit);
        }
    }

    private void mostrarDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (DatePicker view, int year, int month, int dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth, 0, 0, 0);
            dataPrimeiraParcelaMillis = cal.getTimeInMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
            editTextDataPrimeiraParcela.setText(sdf.format(cal.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void mostrarDialogAdicionarItem() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_item_venda, null);
        Spinner spinnerProduto = dialogView.findViewById(R.id.spinnerProduto);
        EditText editTextQuantidade = dialogView.findViewById(R.id.editTextQuantidade);
        EditText editTextValorUnitario = dialogView.findViewById(R.id.editTextValorUnitario);

        List<Produto> produtos = produtoDAO.getAllProdutos();
        ArrayAdapter<Produto> produtoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, produtos);
        produtoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProduto.setAdapter(produtoAdapter);

        if (!produtos.isEmpty()) {
            editTextValorUnitario.setText(String.valueOf(produtos.get(0).getValorPadrao()));
        }

        spinnerProduto.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                Produto p = produtos.get(position);
                editTextValorUnitario.setText(String.valueOf(p.getValorPadrao()));
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Adicionar produto")
                .setView(dialogView)
                .setPositiveButton("Adicionar", null)
                .setNegativeButton("Cancelar", (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> {
            Button btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btn.setOnClickListener(v -> {
                Produto produtoSelecionado = (Produto) spinnerProduto.getSelectedItem();
                String qtdStr = editTextQuantidade.getText().toString().trim();
                String valorStr = editTextValorUnitario.getText().toString().trim();

                if (produtoSelecionado == null) {
                    Toast.makeText(this, "Selecione um produto", Toast.LENGTH_SHORT).show();
                    return;
                }
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

                double valorUnitario;
                try {
                    valorUnitario = Double.parseDouble(valorStr);
                } catch (NumberFormatException e) {
                    editTextValorUnitario.setError("Valor inválido");
                    editTextValorUnitario.requestFocus();
                    return;
                }
                if (valorUnitario <= 0) {
                    editTextValorUnitario.setError("Deve ser maior que zero");
                    editTextValorUnitario.requestFocus();
                    return;
                }

                VendaItem item = new VendaItem();
                item.setProdutoId(produtoSelecionado.getId());
                item.setQuantidade(quantidade);
                item.setValorUnitario(valorUnitario);
                itensVenda.add(item);
                atualizarListaItens();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void atualizarListaItens() {
        List<String> linhas = new ArrayList<>();
        double total = 0;
        for (VendaItem item : itensVenda) {
            Produto p = produtoDAO.getProdutoById(item.getProdutoId());
            String nome = p != null ? p.getNome() : "(Produto)";
            linhas.add(nome + " x" + item.getQuantidade() + " • " + nf.format(item.getValorUnitario()) + " • total " + nf.format(item.getTotal()));
            total += item.getTotal();
        }
        itensAdapter.clear();
        itensAdapter.addAll(linhas);
        itensAdapter.notifyDataSetChanged();
        textViewTotal.setText("Total: " + nf.format(total));
    }

    private void salvarVenda() {
        Cliente clienteSelecionado = (Cliente) spinnerCliente.getSelectedItem();
        if (clienteSelecionado == null || clienteSelecionado.getId() == 0) {
            Toast.makeText(this, "Selecione um cliente", Toast.LENGTH_SHORT).show();
            spinnerCliente.requestFocus();
            return;
        }

        if (itensVenda.isEmpty()) {
            Toast.makeText(this, "Adicione ao menos 1 produto", Toast.LENGTH_SHORT).show();
            return;
        }

        double total = 0;
        for (VendaItem item : itensVenda) total += item.getTotal();

        String formaSelecionada = (String) spinnerFormaPagamento.getSelectedItem();
        String observacao = "Forma: " + formaSelecionada;
        long dataVenda = System.currentTimeMillis();

        int checkedId = radioGroupTipoPagamento.getCheckedRadioButtonId();
        if (checkedId == -1) {
            Toast.makeText(this, "Selecione o tipo de pagamento", Toast.LENGTH_SHORT).show();
            return;
        }

        if (vendaIdEdit != -1) {
            int tipo = (checkedId == R.id.radioAvista) ? VendaDAO.TIPO_AVISTA : VendaDAO.TIPO_APRAZO;
            Integer numeroParcelas = null;
            Long primeiraParcela = null;
            if (tipo == VendaDAO.TIPO_APRAZO) {
                String parcelasStr = editTextNumeroParcelas.getText().toString().trim();
                if (parcelasStr.isEmpty()) {
                    editTextNumeroParcelas.setError("Informe o número de parcelas");
                    editTextNumeroParcelas.requestFocus();
                    return;
                }
                int np;
                try { np = Integer.parseInt(parcelasStr); }
                catch (NumberFormatException e) { editTextNumeroParcelas.setError("Número inválido"); editTextNumeroParcelas.requestFocus(); return; }
                if (np <= 0) { editTextNumeroParcelas.setError("Deve ser maior que zero"); editTextNumeroParcelas.requestFocus(); return; }
                if (dataPrimeiraParcelaMillis == null) {
                    editTextDataPrimeiraParcela.setError("Informe a data da primeira parcela");
                    editTextDataPrimeiraParcela.requestFocus();
                    return;
                }
                numeroParcelas = np;
                primeiraParcela = dataPrimeiraParcelaMillis;
            }

            Venda venda = new Venda();
            venda.setId(vendaIdEdit);
            venda.setProdutoId(0);
            venda.setClienteId(clienteSelecionado.getId());
            venda.setDataVenda(dataVenda);
            venda.setTipoPagamento(tipo);
            venda.setValorTotal(total);
            venda.setObservacao(observacao);
            int rows = vendaDAO.atualizarVenda(venda);
            if (rows > 0) {
                vendaItemDAO.deleteItensByVendaId(vendaIdEdit);
                for (VendaItem item : itensVenda) {
                    item.setVendaId(vendaIdEdit);
                    vendaItemDAO.insert(item);
                }
                vendaDAO.recriarRecebimentosParaVenda(vendaIdEdit, tipo, total, numeroParcelas, primeiraParcela, dataVenda);
                Toast.makeText(this, "Venda atualizada", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Erro ao atualizar venda", Toast.LENGTH_SHORT).show();
            }
        } else {
            long vendaId;
            if (checkedId == R.id.radioAvista) {
                vendaId = vendaDAO.registrarVendaAVista(0, total, dataVenda, clienteSelecionado.getId(), observacao);
            } else if (checkedId == R.id.radioAprazo) {
                String parcelasStr = editTextNumeroParcelas.getText().toString().trim();
                if (parcelasStr.isEmpty()) {
                    editTextNumeroParcelas.setError("Informe o número de parcelas");
                    editTextNumeroParcelas.requestFocus();
                    return;
                }
                int numeroParcelas;
                try { numeroParcelas = Integer.parseInt(parcelasStr); }
                catch (NumberFormatException e) { editTextNumeroParcelas.setError("Número inválido"); editTextNumeroParcelas.requestFocus(); return; }
                if (numeroParcelas <= 0) { editTextNumeroParcelas.setError("Deve ser maior que zero"); editTextNumeroParcelas.requestFocus(); return; }

                if (dataPrimeiraParcelaMillis == null) {
                    editTextDataPrimeiraParcela.setError("Informe a data da primeira parcela");
                    editTextDataPrimeiraParcela.requestFocus();
                    return;
                }
                vendaId = vendaDAO.registrarVendaAPrazo(0, total, dataVenda, numeroParcelas, dataPrimeiraParcelaMillis, clienteSelecionado.getId(), observacao);
            } else {
                Toast.makeText(this, "Selecione o tipo de pagamento", Toast.LENGTH_SHORT).show();
                return;
            }

            if (vendaId > 0) {
                for (VendaItem item : itensVenda) {
                    item.setVendaId(vendaId);
                    vendaItemDAO.insert(item);
                }
                Toast.makeText(this, "Venda registrada", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Erro ao registrar venda", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void carregarVendaExistente(long vendaId) {
        Venda v = vendaDAO.getVendaById(vendaId);
        if (v == null) return;

        // Seleciona cliente
        ArrayAdapter<Cliente> adapter = (ArrayAdapter<Cliente>) spinnerCliente.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            Cliente c = adapter.getItem(i);
            if (c != null && c.getId() == v.getClienteId()) {
                spinnerCliente.setSelection(i);
                break;
            }
        }

        // Carrega itens; se não houver, mapeia produto único para item
        List<VendaItem> itens = vendaItemDAO.getItensByVendaId(vendaId);
        if (itens.isEmpty() && v.getProdutoId() != 0) {
            Produto p = produtoDAO.getProdutoById(v.getProdutoId());
            VendaItem item = new VendaItem();
            item.setProdutoId(v.getProdutoId());
            item.setQuantidade(1);
            item.setValorUnitario(v.getValorTotal());
            itensVenda.add(item);
        } else {
            itensVenda.addAll(itens);
        }
        atualizarListaItens();

        // Define tipo de pagamento conforme venda existente
        if (v.getTipoPagamento() == VendaDAO.TIPO_AVISTA) {
            radioAvista.setChecked(true);
        } else {
            radioAprazo.setChecked(true);
        }
        // Observação e parcelas não são carregadas aqui por simplicidade.
    }

    private void mostrarDialogNovoCliente() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Novo Cliente")
                .setView(getLayoutInflater().inflate(R.layout.dialog_add_cliente, null))
                .setPositiveButton("Salvar", null)
                .setNegativeButton("Cancelar", (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> {
            // Obtém views do layout
            EditText editTextDialogNome = dialog.findViewById(R.id.editTextDialogNome);
            Button buttonDialogSalvar = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            buttonDialogSalvar.setOnClickListener(v -> {
                String nome = editTextDialogNome.getText().toString().trim();
                if (nome.isEmpty()) {
                    editTextDialogNome.setError("Informe o nome");
                    editTextDialogNome.requestFocus();
                    return;
                }
                Cliente novo = new Cliente();
                novo.setNome(nome);
                long id = clienteDAO.inserirCliente(novo);
                if (id > 0) {
                    // Atualiza spinner
                    List<Cliente> clientes = clienteDAO.getAllClientes();
                    List<Cliente> clientesComPlaceholder = new ArrayList<>();
                    Cliente placeholder = new Cliente(); placeholder.setId(0); placeholder.setNome("Selecione um cliente");
                    clientesComPlaceholder.add(placeholder);
                    clientesComPlaceholder.addAll(clientes);
                    ArrayAdapter<Cliente> clienteAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, clientesComPlaceholder);
                    clienteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCliente.setAdapter(clienteAdapter);
                    // Seleciona recém-criado
                    for (int i = 0; i < clienteAdapter.getCount(); i++) {
                        Cliente c = clienteAdapter.getItem(i);
                        if (c != null && c.getId() == id) { spinnerCliente.setSelection(i); break; }
                    }
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Erro ao salvar cliente", Toast.LENGTH_SHORT).show();
                }
            });
        });
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        if (clienteDAO != null) clienteDAO.close();
        if (produtoDAO != null) produtoDAO.close();
        if (vendaDAO != null) vendaDAO.close();
        if (vendaItemDAO != null) vendaItemDAO.close();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }
}