package com.example.filmnest.Activities;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.filmnest.Adapters.BookMarkAdapter;
import com.example.filmnest.Adapters.FilmListAdapter;
import com.example.filmnest.Domains.Film;
import com.example.filmnest.R;
import com.example.filmnest.databinding.ActivityBookMarkBinding;
import com.example.filmnest.databinding.ActivityListMoviesBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BookMarkActivity extends AppCompatActivity {

    private ActivityBookMarkBinding binding;
    private BookMarkAdapter filmListAdapter;
    private ArrayList<Film> filmList;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookMarkBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        // Nút quay lại
        binding.backBtn.setOnClickListener(v -> finish());

        // Khởi tạo RecyclerView và Adapter
        filmList = new ArrayList<>();
        filmListAdapter = new BookMarkAdapter(filmList);
        binding.recyclerViewAllFilms.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recyclerViewAllFilms.setAdapter(filmListAdapter);

        // Lấy thông tin người dùng hiện tại từ Firebase Authentication
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            loadBookMarks();
        } else {
            Toast.makeText(this, "Không thể lấy thông tin tài khoản!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadBookMarks() {
        // Hiển thị ProgressBar trong khi tải dữ liệu
        binding.progressBarAllFilms.setVisibility(View.VISIBLE);

        // Tham chiếu đến nhánh "BookMark" của tài khoản người dùng
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Taikhoan")
                .child(currentUser.getUid()).child("BookMark");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                filmList.clear(); // Xóa danh sách cũ trước khi thêm mới
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Film film = snapshot.getValue(Film.class);
                    if (film != null) {
                        filmList.add(film); // Thêm từng bộ phim vào danh sách
                    }
                }

                // Kiểm tra nếu danh sách rỗng
                if (filmList.isEmpty()) {
                    binding.textNull.setVisibility(View.VISIBLE); // Hiển thị TextView thông báo
                } else {
                    binding.textNull.setVisibility(View.GONE); // Ẩn TextView nếu có phim
                }
                filmListAdapter.notifyDataSetChanged(); // Cập nhật Adapter
                binding.progressBarAllFilms.setVisibility(View.GONE); // Ẩn ProgressBar
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBarAllFilms.setVisibility(View.GONE);
                Toast.makeText(BookMarkActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
