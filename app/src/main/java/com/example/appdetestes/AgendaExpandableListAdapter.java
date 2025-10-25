package com.example.appdetestes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AgendaExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> listDataHeader; // Nomes dos clientes
    private HashMap<String, List<Agendamento>> listDataChild; // Agendamentos por cliente

    public AgendaExpandableListAdapter(Context context, List<String> listDataHeader, HashMap<String, List<Agendamento>> listDataChild) {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listDataChild = listDataChild;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final Agendamento agendamento = (Agendamento) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_agenda, null);
        }

        TextView textViewHorario = convertView.findViewById(R.id.textViewHorario);
        TextView textViewServico = convertView.findViewById(R.id.textViewServico);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Date horaInicio = new Date(agendamento.getDataHoraInicio());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(horaInicio);
        calendar.add(Calendar.MINUTE, agendamento.getTempoServico());
        Date horaFim = calendar.getTime();

        String horario = sdf.format(horaInicio) + " - " + sdf.format(horaFim);
        textViewHorario.setText(horario);

        // Formata o valor pago como moeda (pt-BR)
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        String valorFormatado = currencyFormatter.format(agendamento.getValor());

        // Exibe serviço e valor pago
        textViewServico.setText("Serviço: " + agendamento.getNomeServico() + " • Valor: " + valorFormatado);

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_group_agenda, null);
        }

        TextView textViewClientName = convertView.findViewById(R.id.textViewClientName);

        // Calcula o valor total dos agendamentos do cliente neste grupo
        double totalCliente = 0.0;
        List<Agendamento> agendamentosDoCliente = listDataChild.get(headerTitle);
        if (agendamentosDoCliente != null) {
            for (Agendamento a : agendamentosDoCliente) {
                totalCliente += a.getValor();
            }
        }
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        String totalFormatado = currencyFormatter.format(totalCliente);

        // Mostra "Nome do Cliente • Valor: R$ X"
        textViewClientName.setText(headerTitle + " • Valor: " + totalFormatado);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
