package com.example.filmnest.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.filmnest.Activities.DetailActivity;
import com.example.filmnest.Domains.Film;
import com.example.filmnest.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BookMarkAdapter extends RecyclerView.Adapter<BookMarkAdapter.Viewholder> {
    ArrayList<Film> items;
    Context context;

    public BookMarkAdapter(ArrayList<Film> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public BookMarkAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.film_viewholder,parent,false);
        return new Viewholder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull BookMarkAdapter.Viewholder holder, int position) {
        holder.titleTxt.setText(items.get(position).getTitle());
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transform(new CenterCrop(),new RoundedCorners(30));

        Glide.with(context)
                .load(items.get(position).getPoster())
                .apply(requestOptions)
                .into(holder.pic);
        // Sự kiện click vào item để mở chi tiết phim
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("object", items.get(position));
            context.startActivity(intent);
        });

        // Sự kiện nhấn giữ để xóa phim khỏi bookmark
        holder.itemView.setOnLongClickListener(view -> {
            Film film = items.get(position);
            removeFilmFromBookMark(film);
            return true;
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        TextView titleTxt;
        ImageView pic;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.nameTxt);
            pic = itemView.findViewById(R.id.pic);
        }
    }
    // Phương thức xóa phim khỏi bookmark
    private void removeFilmFromBookMark(Film film) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Taikhoan")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("BookMark");

        // Tìm phim theo ID và xóa khỏi bookmark
        ref.orderByChild("id").equalTo(film.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().removeValue(); // Xóa phim
                }
                Toast.makeText(context, "Đã xóa phim khỏi bookmark!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Lỗi khi xóa phim khỏi bookmark!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
