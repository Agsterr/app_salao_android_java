package com.focodevsistemas.gerenciamento;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.Locale;

public class OrcamentoFooterAdapter extends RecyclerView.Adapter<OrcamentoFooterAdapter.FooterViewHolder> {

    private final FooterListener listener;
    private double totalValue = 0.0;
    
    // Armazenar valores atuais para evitar perda de estado ao rolar
    private String descontoAtual = "";
    private String acrescimoAtual = "";

    public interface FooterListener {
        void onDescontoChanged(double valor);
        void onAcrescimoChanged(double valor);
    }

    public OrcamentoFooterAdapter(FooterListener listener) {
        this.listener = listener;
    }

    public void updateTotal(double total) {
        this.totalValue = total;
        notifyItemChanged(0); // Atualiza apenas o texto, idealmente usar payload para não recriar view
    }
    
    // Método otimizado para atualizar apenas o texto sem recriar os EditTexts
    public void updateTotalTextOnly(double total) {
        this.totalValue = total;
        // Não chamamos notifyItemChanged aqui se quisermos evitar perda de foco,
        // mas como o total é apenas um TextView, podemos atualizar se tivermos referência ao ViewHolder.
        // No entanto, para simplicidade e robustez com ConcatAdapter, notifyItemChanged(0, "UPDATE_TOTAL") é melhor.
        notifyItemChanged(0, Boolean.TRUE); 
    }

    @NonNull
    @Override
    public FooterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_orcamento, parent, false);
        return new FooterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FooterViewHolder holder, int position) {
        holder.bind();
    }
    
    @Override
    public void onBindViewHolder(@NonNull FooterViewHolder holder, int position, @NonNull java.util.List<Object> payloads) {
        if (!payloads.isEmpty()) {
            holder.updateTotalText();
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    class FooterViewHolder extends RecyclerView.ViewHolder {
        TextInputEditText editDesconto, editAcrescimo;
        TextView textTotal;
        
        private TextWatcher descontoWatcher;
        private TextWatcher acrescimoWatcher;

        public FooterViewHolder(@NonNull View itemView) {
            super(itemView);
            editDesconto = itemView.findViewById(R.id.editTextDesconto);
            editAcrescimo = itemView.findViewById(R.id.editTextAcrescimo);
            textTotal = itemView.findViewById(R.id.textValorTotal);
        }

        public void bind() {
            // Configurar watchers
            if (descontoWatcher != null) editDesconto.removeTextChangedListener(descontoWatcher);
            if (acrescimoWatcher != null) editAcrescimo.removeTextChangedListener(acrescimoWatcher);
            
            // Restaurar valores
            if (!editDesconto.getText().toString().equals(descontoAtual)) {
                editDesconto.setText(descontoAtual);
            }
            if (!editAcrescimo.getText().toString().equals(acrescimoAtual)) {
                editAcrescimo.setText(acrescimoAtual);
            }
            
            updateTotalText();

            descontoWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    descontoAtual = s.toString();
                    double val = parseDouble(descontoAtual);
                    if (listener != null) listener.onDescontoChanged(val);
                }
            };
            
            acrescimoWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    acrescimoAtual = s.toString();
                    double val = parseDouble(acrescimoAtual);
                    if (listener != null) listener.onAcrescimoChanged(val);
                }
            };

            editDesconto.addTextChangedListener(descontoWatcher);
            editAcrescimo.addTextChangedListener(acrescimoWatcher);
        }
        
        public void updateTotalText() {
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            textTotal.setText("Total: " + nf.format(totalValue));
        }

        private double parseDouble(String val) {
            try {
                return Double.parseDouble(val.replace(",", "."));
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
    }
}
