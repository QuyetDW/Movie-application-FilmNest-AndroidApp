package com.example.filmnest.Activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.filmnest.Adapters.CastListAdapter;
import com.example.filmnest.Adapters.CategoryEachFlimAdapter;
import com.example.filmnest.Adapters.CommentAdapter;
import com.example.filmnest.Domains.Comment;
import com.example.filmnest.Domains.Film;
import com.example.filmnest.R;
import com.example.filmnest.databinding.ActivityDetailBinding;
import com.example.filmnest.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import eightbitlab.com.blurview.RenderEffectBlur;
import eightbitlab.com.blurview.RenderScriptBlur;

public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private FirebaseDatabase database;
    private DatabaseReference commentsRef;
    private List<Comment> commentList;
    private CommentAdapter commentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setVariable();

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    private void setVariable() {
        database = FirebaseDatabase.getInstance();
        Film item = (Film) getIntent().getSerializableExtra("object");
        commentsRef = database.getReference("Comments").child(String.valueOf(item.getId()));
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transform(new CenterCrop(), new GranularRoundedCorners(0,0,50,50));


        // Thiết lập RecyclerView cho bình luận
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(commentList);
        binding.commentsRecyclerView.setAdapter(commentAdapter);
        binding.commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Lấy dữ liệu bình luận từ Firebase
        loadComments();

        // Xử lý khi người dùng gửi bình luận
        binding.submitCommentBtn.setOnClickListener(v -> {
            String commentText = binding.commentInput.getText().toString();
            float ratingValue = binding.ratingBar.getRating();
            if (!commentText.isEmpty() && ratingValue > 0) {
                submitComment(commentText, ratingValue);
            } else {
                Toast.makeText(DetailActivity.this, "Vui lòng nhập bình luận và đánh giá!", Toast.LENGTH_SHORT).show();
            }
        });

        Glide.with(this)
                .load(item.getPoster())
                .apply(requestOptions)
                .into(binding.filmPic);
        binding.titleTxt.setText(item.getTitle());
        binding.imdbTxt.setText("IMDB: " + item.getImdb()+"/10");
        binding.movieTimesTxt.setText(item.getYear() +" - "+ item.getTime());
        binding.movieSummery.setText(item.getDescription());


        binding.watchTrailerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = item.getTrailer().replace("https://www.youtube.com/watch?v=", "");
                Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:"+id));
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getTrailer()));

                try {
                    startActivity(appIntent);
                }catch (ActivityNotFoundException ex){
                    startActivity(webIntent);
                }
            }
        });

        binding.watchFilmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String videoUrl = item.getMovies(); // Lấy URL từ đối tượng Film

                if (videoUrl != null && !videoUrl.isEmpty()) {
                    // Chuyển sang PlayerActivity và truyền URL video
                    Intent intent = new Intent(DetailActivity.this, PlayerActivity.class);
                    intent.putExtra("videoUrl", videoUrl);
                    startActivity(intent);
                } else {
                    Toast.makeText(DetailActivity.this, "Phim này chưa ra mắt !!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // Xử lý nút back
        binding.backImg.setOnClickListener(view -> finish());

        // Hiển thị thể loại phim
        if (item.getGenre() != null) {
            binding.genreView.setAdapter(new CategoryEachFlimAdapter(item.getGenre()));
            binding.genreView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        }

        // Hiển thị diễn viên
        if (item.getCasts() != null) {
            binding.castView.setAdapter(new CastListAdapter(item.getCasts()));
            binding.castView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        }

        // Xử lý nút BookMark
        binding.bookMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToBookMark(item);
            }
        });
        // BlurView hiệu ứng mờ

        float radius = 10f;
        View decorView = getWindow().getDecorView();
        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
        Drawable windowBackground = decorView.getBackground();

        binding.blurView.setupWith(rootView, new RenderScriptBlur(this))
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(radius);
        binding.blurView.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        binding.blurView.setClipToOutline(true);
    }

    private void loadComments() {
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Comment comment = dataSnapshot.getValue(Comment.class);
                    commentList.add(comment);
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailActivity.this, "Lỗi tải bình luận!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitComment(String content, float rating) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userName = user.getDisplayName();
            Comment comment = new Comment(userName, content, rating);

            commentsRef.push().setValue(comment).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(DetailActivity.this, "Bình luận đã được gửi!", Toast.LENGTH_SHORT).show();
                    binding.commentInput.setText("");
                    binding.ratingBar.setRating(0);
                } else {
                    Toast.makeText(DetailActivity.this, "Lỗi gửi bình luận!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void addToBookMark(Film item) {
        // Lấy userID từ Firebase Authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userID = currentUser.getUid();

            // Tham chiếu đến nhánh "Taikhoan" của tài khoản người dùng
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Taikhoan").child(userID).child("BookMark");
            // Kiểm tra nếu phim đã có trong bookmark
            ref.orderByChild("id").equalTo(item.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Phim đã có trong bookmark, hiển thị thông báo
                        Toast.makeText(DetailActivity.this, "Phim đã có trong bookmark!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Thêm phim vào bookmark
                        ref.push().setValue(item).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(DetailActivity.this, "Thêm vào bookmark thành công!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(DetailActivity.this, "Lỗi khi thêm phim vào bookmark!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(DetailActivity.this, "Lỗi kiểm tra bookmark!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}