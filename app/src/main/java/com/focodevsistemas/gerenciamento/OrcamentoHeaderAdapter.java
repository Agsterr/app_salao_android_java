package com.focodevsistemas.gerenciamento;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class OrcamentoHeaderAdapter extends RecyclerView.Adapter<OrcamentoHeaderAdapter.HeaderViewHolder> {

    private final List<Cliente> listaClientes;
    private final HeaderListener listener;
    private final boolean isPremium;
    private String observacoesAtuais = "";
    private int clienteSelecionadoIndex = 0;
    private String tipoOrcamentoAtual = "SERVICO";

    public interface HeaderListener {
        void onTipoOrcamentoChanged(String tipo);
        void onClienteSelecionado(int position);
        void onObservacoesChanged(String texto);
        void onAdicionarItemClicked();
        void onGerarPDFClicked();
    }

    public OrcamentoHeaderAdapter(List<Cliente> clientes, boolean isPremium, HeaderListener listener) {
        this.listaClientes = clientes;
        this.isPremium = isPremium;
        this.listener = listener;
    }

    public void updateObservacoes(String obs) {
        this.observacoesAtuais = obs;
    }

    @NonNull
    @Override
    public HeaderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_orcamento, parent, false);
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HeaderViewHolder holder, int position) {
        holder.bind();
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        RadioGroup radioGroupTipo;
        RadioButton radioServico, radioProduto;
        Spinner spinnerCliente;
        TextInputEditText editTextObservacoes;
        View cardAvisoPremium;
        View btnAdicionar, btnGerarPDF;

        // TextWatcher para remover e adicionar para evitar loops infinitos ou múltiplas chamadas
        private TextWatcher obsTextWatcher;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            radioGroupTipo = itemView.findViewById(R.id.radioGroupTipoOrcamento);
            radioServico = itemView.findViewById(R.id.radioServico);
            radioProduto = itemView.findViewById(R.id.radioProduto);
            spinnerCliente = itemView.findViewById(R.id.spinnerCliente);
            editTextObservacoes = itemView.findViewById(R.id.editTextObservacoes);
            cardAvisoPremium = itemView.findViewById(R.id.cardAvisoPremium);
            btnAdicionar = itemView.findViewById(R.id.buttonAdicionarItem);
            btnGerarPDF = itemView.findViewById(R.id.buttonGerarPDF);
        }

        public void bind() {
            // Configurar Premium
            cardAvisoPremium.setVisibility(isPremium ? View.GONE : View.VISIBLE);

            // Configurar RadioGroup
            radioGroupTipo.setOnCheckedChangeListener(null); // Evitar trigger durante config
            if ("SERVICO".equals(tipoOrcamentoAtual)) {
                radioServico.setChecked(true);
            } else {
                radioProduto.setChecked(true);
            }
            radioGroupTipo.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.radioServico) {
                    tipoOrcamentoAtual = "SERVICO";
                } else {
                    tipoOrcamentoAtual = "PRODUTO";
                }
                if (listener != null) listener.onTipoOrcamentoChanged(tipoOrcamentoAtual);
            });

            // Configurar Spinner Clientes
            ArrayAdapter<Cliente> clienteAdapter = new ArrayAdapter<>(itemView.getContext(),
                    android.R.layout.simple_spinner_item, listaClientes) {
                @Override
                public View getView(int position, View convertView, android.view.ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text = (TextView) view.findViewById(android.R.id.text1);
                    text.setText(listaClientes.get(position).getNome());
                    return view;
                }

                @Override
                public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    TextView text = (TextView) view.findViewById(android.R.id.text1);
                    text.setText(listaClientes.get(position).getNome());
                    return view;
                }
            };
            clienteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCliente.setAdapter(clienteAdapter);
            spinnerCliente.setSelection(clienteSelecionadoIndex);
            
            spinnerCliente.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    clienteSelecionadoIndex = position;
                    if (listener != null) listener.onClienteSelecionado(position);
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            // Configurar Observações
            if (obsTextWatcher != null) editTextObservacoes.removeTextChangedListener(obsTextWatcher);
            editTextObservacoes.setText(observacoesAtuais);
            
            obsTextWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    observacoesAtuais = s.toString();
                    if (listener != null) listener.onObservacoesChanged(observacoesAtuais);
                }
            };
            editTextObservacoes.addTextChangedListener(obsTextWatcher);

            // Botões
            btnAdicionar.setOnClickListener(v -> {
                if (listener != null) listener.onAdicionarItemClicked();
            });
            btnGerarPDF.setOnClickListener(v -> {
                if (listener != null) listener.onGerarPDFClicked();
            });
        }
    }
}
