package com.focodevsistemas.gerenciamento;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrcamentoItensAdapter extends RecyclerView.Adapter<OrcamentoItensAdapter.ViewHolder> {

    private final List<ItemDisplay> itens = new ArrayList<>();
    private OnItemRemoveListener onItemRemoveListener;
    private OnItemClickListener onItemClickListener;

    public interface OnItemRemoveListener {
        void onRemove(int position);
    }

    public interface OnItemClickListener {
        void onClick(int position);
    }

    public void setOnItemRemoveListener(OnItemRemoveListener listener) {
        this.onItemRemoveListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setAllItens(List<Orcamento.OrcamentoItemServico> servicos, List<Orcamento.OrcamentoItemProduto> produtos) {
        this.itens.clear();
        if (servicos != null) {
            for (Orcamento.OrcamentoItemServico s : servicos) {
                this.itens.add(new ItemDisplay(s.getNomeServico(), s.getQuantidade(), s.getValorUnitario(), s.getValorTotal(), true, s));
            }
        }
        if (produtos != null) {
            for (Orcamento.OrcamentoItemProduto p : produtos) {
                this.itens.add(new ItemDisplay(p.getNomeProduto(), p.getQuantidade(), p.getValorUnitario(), p.getValorTotal(), false, p));
            }
        }
        notifyDataSetChanged();
    }
    
    public void clear() {
        this.itens.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_orcamento, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemDisplay item = itens.get(position);
        holder.bind(item, position, onItemRemoveListener, onItemClickListener);
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textNome;
        TextView textDetalhes;
        TextView textTotal;
        ImageView btnRemover;
        ImageView btnEditar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textNome = itemView.findViewById(R.id.textNomeItem);
            textDetalhes = itemView.findViewById(R.id.textDetalhesItem);
            textTotal = itemView.findViewById(R.id.textPrecoTotalItem);
            btnRemover = itemView.findViewById(R.id.btnRemoverItem);
            btnEditar = itemView.findViewById(R.id.btnEditarItem);
        }

        public void bind(ItemDisplay item, int position, OnItemRemoveListener removeListener, OnItemClickListener clickListener) {
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            
            String prefixo = item.isServico ? "🔧 " : "📦 ";
            textNome.setText(prefixo + item.nome);
            textDetalhes.setText(String.format(Locale.getDefault(), "%d x %s", item.quantidade, nf.format(item.valorUnitario)));
            textTotal.setText(nf.format(item.valorTotal));
            
            btnRemover.setOnClickListener(v -> {
                if (removeListener != null) {
                    removeListener.onRemove(position);
                }
            });
            
            View.OnClickListener editAction = v -> {
                if (clickListener != null) {
                    clickListener.onClick(position);
                }
            };

            btnEditar.setOnClickListener(editAction);
            itemView.setOnClickListener(editAction);
        }
    }

    public ItemDisplay getItem(int position) {
        return itens.get(position);
    }

    public static class ItemDisplay {
        String nome;
        int quantidade;
        double valorUnitario;
        double valorTotal;
        boolean isServico;
        Object originalItem; // Para referência se precisar saber qual item exato é na lista original

        public ItemDisplay(String nome, int quantidade, double valorUnitario, double valorTotal, boolean isServico, Object originalItem) {
            this.nome = nome;
            this.quantidade = quantidade;
            this.valorUnitario = valorUnitario;
            this.valorTotal = valorTotal;
            this.isServico = isServico;
            this.originalItem = originalItem;
        }
    }
}
