package com.example.filmnest.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.filmnest.Activities.DetailActivity;
import com.example.filmnest.Domains.Film;
import com.example.filmnest.R;

import java.util.ArrayList;

public class FilmListAdapterSearch extends RecyclerView.Adapter<FilmListAdapterSearch.Viewholder> {
    ArrayList<Film> items;
    Context context;

    public FilmListAdapterSearch(ArrayList<Film> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public FilmListAdapterSearch.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.film_viewholder_search,parent,false);
        return new Viewholder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull FilmListAdapterSearch.Viewholder holder, int position) {
        holder.titleTxt.setText(items.get(position).getTitle());
        holder.hour.setText(items.get(position).getTime());
        holder.year.setText("" + items.get(position).getYear() + " | ");
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transform(new CenterCrop(),new RoundedCorners(30));

        Glide.with(context)
                .load(items.get(position).getPoster())
                .apply(requestOptions)
                .into(holder.pic);

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("object", items.get(position));
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateList(ArrayList<Film> newItems) {
        items = newItems;
        notifyDataSetChanged(); // Thông báo cho adapter rằng dữ liệu đã thay đổi
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        TextView titleTxt, year, hour;
        ImageView pic;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.nameTxt);
            year = itemView.findViewById(R.id.year);
            hour = itemView.findViewById(R.id.hour);
            pic = itemView.findViewById(R.id.pic);
        }
    }
}
