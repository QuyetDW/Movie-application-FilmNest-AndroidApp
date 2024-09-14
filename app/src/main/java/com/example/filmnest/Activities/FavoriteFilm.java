package com.example.filmnest.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filmnest.Adapters.FilmListAdapter;
import com.example.filmnest.Domains.Film;
import com.example.filmnest.R;
import com.example.filmnest.databinding.ActivityListMoviesBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FavoriteFilm extends AppCompatActivity {
    private ActivityListMoviesBinding binding;
    private FirebaseDatabase database;
    private String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListMoviesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        // Hiển thị nút quay lại trên ActionBar
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Nút quay lại
        binding.backBtn.setOnClickListener(v -> finish());

        database = FirebaseDatabase.getInstance();

        Intent intent = getIntent();
        category = intent.getStringExtra("category");

        // Thiết lập tiêu đề dựa trên danh mục
        TextView textViewTitle = binding.textViewTitle;
        if (category != null){
            textViewTitle.setText(category);
        } else {
            textViewTitle.setText("Top Rating");
        }


        // Cài đặt RecyclerView với GridLayoutManager 2 cột
        binding.recyclerViewAllFilms.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recyclerViewAllFilms.setHasFixedSize(true);
        FilmListAdapter adapter = new FilmListAdapter(new ArrayList<>());
        binding.recyclerViewAllFilms.setAdapter(adapter);

        // Lấy dữ liệu phim từ Firebase dựa trên danh mục
        fetchFavouriteAndUpcomingFilms();
    }


    private void fetchFavouriteAndUpcomingFilms(){
        binding.progressBarAllFilms.setVisibility(View.VISIBLE);

        DatabaseReference favouriteRef = database.getReference("Favourite");
        DatabaseReference upcomingRef = database.getReference("Upcomming");
        DatabaseReference itemsRef = database.getReference("Items");

        ArrayList<Film> films = new ArrayList<>();

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot issue : snapshot.getChildren()) {
                    Film film = issue.getValue(Film.class);
                    if (film != null && film.getImdb() >= 8) {
                        films.add(film);
                    }
                }

                // Sau khi dữ liệu từ tất cả các danh mục đã được nạp
                if (snapshot.getRef().equals(itemsRef)) {
                    if (!films.isEmpty()) {
                        FilmListAdapter adapter = new FilmListAdapter(films);
                        binding.recyclerViewAllFilms.setAdapter(adapter);
                    } else {
                        Toast.makeText(FavoriteFilm.this, "Không có phim nào có điểm IMDB trên 8.", Toast.LENGTH_SHORT).show();
                    }
                    binding.progressBarAllFilms.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBarAllFilms.setVisibility(View.GONE);
                Toast.makeText(FavoriteFilm.this, "Lỗi khi lấy dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        // Lấy dữ liệu từ ba danh mục
        favouriteRef.addListenerForSingleValueEvent(listener);
        upcomingRef.addListenerForSingleValueEvent(listener);
        itemsRef.addListenerForSingleValueEvent(listener);
    }

    // Xử lý sự kiện quay lại khi nhấn nút trên ActionBar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}