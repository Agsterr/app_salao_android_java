package com.example.appdetestes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.graphics.Color;

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
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_agenda, null);
        }

        Agendamento agendamento = (Agendamento) getChild(groupPosition, childPosition);
        TextView textViewHorario = convertView.findViewById(R.id.textViewHorario);
        TextView textViewServico = convertView.findViewById(R.id.textViewServico);
        TextView textViewStatus = convertView.findViewById(R.id.textViewStatus);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Date horaInicio = new Date(agendamento.getDataHoraInicio());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(horaInicio);
        calendar.add(Calendar.MINUTE, agendamento.getTempoServico());
        Date horaFim = calendar.getTime();

        String horario = sdf.format(horaInicio) + " - " + sdf.format(horaFim);
        textViewHorario.setText(horario);

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        String valorFormatado = currencyFormatter.format(agendamento.getValor());

        textViewServico.setText("Serviço: " + agendamento.getNomeServico() + " • Valor: " + valorFormatado);

        String status = agendamento.getStatus();
        textViewStatus.setText("Status: " + status);
        if ("Cancelado".equals(status)) {
            textViewStatus.setTextColor(Color.parseColor("#E53935"));
        } else if ("Em andamento".equals(status)) {
            textViewStatus.setTextColor(Color.parseColor("#1976D2"));
        } else if ("Finalizado".equals(status)) {
            textViewStatus.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            textViewStatus.setTextColor(Color.parseColor("#616161"));
        }

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

        // Calcula o valor total dos agendamentos do cliente neste grupo (ignorando cancelados)
        double totalCliente = 0.0;
        List<Agendamento> agendamentosDoCliente = listDataChild.get(headerTitle);
        if (agendamentosDoCliente != null) {
            for (Agendamento a : agendamentosDoCliente) {
                if (!a.isCancelado() && a.getValor() > 0) {
                    totalCliente += a.getValor();
                }
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
