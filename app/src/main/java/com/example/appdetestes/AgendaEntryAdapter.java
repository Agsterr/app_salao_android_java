package com.example.appdetestes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.graphics.Color;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AgendaEntryAdapter extends ArrayAdapter<AgendaEntry> {
    private final LayoutInflater inflater;
    private final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public AgendaEntryAdapter(Context context, List<AgendaEntry> items) {
        super(context, 0, items);
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_agenda_cliente, parent, false);
            holder = new ViewHolder();
            holder.title = convertView.findViewById(R.id.textViewAgendaTitle);
            holder.time = convertView.findViewById(R.id.textViewAgendaTime);
            holder.status = convertView.findViewById(R.id.textViewAgendaStatus);
            holder.endereco = convertView.findViewById(R.id.textViewAgendaEndereco);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        AgendaEntry item = getItem(position);
        if (item != null) {
            holder.title.setText(item.getTitle());

            String timeStr = sdfDate.format(new Date(item.getTimestamp()));
            holder.time.setText(timeStr);

            String end = item.getEndereco();
            if (end != null && !end.isEmpty()) {
                holder.endereco.setText(end);
                holder.endereco.setVisibility(View.VISIBLE);
            } else {
                holder.endereco.setVisibility(View.GONE);
            }

            String st = item.getStatus() == null ? "" : item.getStatus();
            holder.status.setText(st);
            int color;
            if ("Finalizado".equalsIgnoreCase(st)) {
                color = Color.parseColor("#2E7D32");
            } else if ("Cancelado".equalsIgnoreCase(st)) {
                color = Color.parseColor("#C62828");
            } else {
                color = Color.parseColor("#1565C0");
            }
            holder.status.setTextColor(color);
        }

        return convertView;
    }

    static class ViewHolder {
        TextView title;
        TextView time;
        TextView status;
        TextView endereco;
    }
}