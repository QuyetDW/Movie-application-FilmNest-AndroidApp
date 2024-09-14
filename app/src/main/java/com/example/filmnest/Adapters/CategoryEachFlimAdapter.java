package com.example.filmnest.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filmnest.R;

import java.util.List;

public class CategoryEachFlimAdapter extends RecyclerView.Adapter<CategoryEachFlimAdapter.Viewholder> {

    List<String> items;
    Context context;

    public CategoryEachFlimAdapter(List<String> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public CategoryEachFlimAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_category,parent,false);
        return new Viewholder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryEachFlimAdapter.Viewholder holder, int position) {
        holder.titleTxt.setText(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        TextView titleTxt;
        public Viewholder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.titleTxt);
        }
    }
}
