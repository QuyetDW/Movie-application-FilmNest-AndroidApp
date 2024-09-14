package com.example.filmnest.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.filmnest.Adapters.FilmListAdapter;
import com.example.filmnest.Adapters.FilmListAdapterSearch;
import com.example.filmnest.Adapters.SlidersAdapter;
import com.example.filmnest.Domains.Film;
import com.example.filmnest.Domains.SliderItems;
import com.example.filmnest.R;
import com.example.filmnest.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView btn_menu;
    private TextView[] txt = new TextView[2];
    ImageView avatar;
    ActivityMainBinding binding;
    private FirebaseDatabase database;
    private FirebaseAuth firebaseAuth;
    private Handler sliderHandles = new Handler();
    private Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            binding.viewPager2.setCurrentItem(binding.viewPager2.getCurrentItem()+1);
        }
    };

    private EditText editTextSearch;
    private RecyclerView recyclerViewSearchResults;
    private FilmListAdapterSearch filmListAdapterSearch;
    private ArrayList<Film> filmList = new ArrayList<>();
    private ArrayList<Film> filteredFilms = new ArrayList<>();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();  // Khởi tạo FirebaseAuth

        txt[0] = findViewById(R.id.txt_name);
        txt[1] = findViewById(R.id.txt_email);
        avatar = findViewById(R.id.giaodien);
        btn_menu = findViewById(R.id.bottomNavigationView);

        editTextSearch = findViewById(R.id.editTextSearch);
        recyclerViewSearchResults = findViewById(R.id.recyclerViewSearchResults);
        recyclerViewSearchResults.setLayoutManager(new LinearLayoutManager(this));

        // Ẩn RecyclerView khi khởi tạo
        recyclerViewSearchResults.setVisibility(View.GONE);

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        // Thêm sự kiện click cho "Tất cả" trong Top Phim
        binding.allTopPhim.setOnClickListener(v -> openListMoviesActivity("Top Phim"));
        // Thêm sự kiện click cho "Tất cả" trong Yêu Thích
        binding.allYeuThich.setOnClickListener(v -> openListMoviesActivity("Yêu Thích"));
        // Thêm sự kiện click cho "Tất cả" trong Sắp Chiếu
        binding.allSapChieu.setOnClickListener(v -> openListMoviesActivity("Sắp Chiếu"));

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterFilms(charSequence.toString());
            }
            @Override
            public void afterTextChanged(Editable editable) {
                filterFilms(editable.toString());
            }
        });

        // Xử lý sự kiện khi người dùng nhấn vào icon microphone
        editTextSearch.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (editTextSearch.getRight() - editTextSearch.getCompoundDrawables()[2].getBounds().width())) {
                    // Khởi động chức năng nhận dạng giọng nói
                    startVoiceRecognition();
                    return true;
                }
            }
            return false;
        });

        initBanner();
        initTopMoving();
        initUpComing();
        initFavourite();
        fetchFilmsFromFirebase();

        loadUserDataFromFirebaseDatabase();
        Menu();
    }
    private void loadUserDataFromFirebaseDatabase() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();  // Lấy người dùng hiện tại từ FirebaseAuth
        if (currentUser != null) {
            String userId = currentUser.getUid();  // Lấy UID của người dùng hiện tại

            // Tham chiếu đến nhánh "Taikhoan" trong Realtime Database
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Taikhoan").child(userId);

            // Lắng nghe thay đổi dữ liệu từ Realtime Database
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String fullname = dataSnapshot.child("fullname").getValue(String.class);
                        String email = dataSnapshot.child("email").getValue(String.class);
                        String avatarUrl = dataSnapshot.child("avatar").getValue(String.class);

                        // Cập nhật giao diện
                        txt[0].setText("Hi! " + fullname);
                        txt[1].setText(email);
                        if (avatarUrl != null) {
                            Glide.with(MainActivity.this).load(avatarUrl).into(avatar);  // Tải avatar từ URL
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Xử lý lỗi nếu có
                    Log.e("FirebaseError", "Lỗi: " + databaseError.getMessage());
                }
            });
        }
    }

    public void Menu() {
        btn_menu.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.profile) {
                    // Xử lý logic khi chọn profile
                    Intent intent = new Intent(MainActivity.this, ThongTinCaNhan.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.explorer) {
                    // Xử lý logic khi chọn explorer
                    editTextSearch.requestFocus(); // Đưa con trỏ vào thanh tìm kiếm
                    // Hiển thị bàn phím
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(editTextSearch.findFocus(), InputMethodManager.SHOW_IMPLICIT);
                    // Cuộn đến vị trí của SearchView
                    final ScrollView scrollView = findViewById(R.id.scrollView2);
                    scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.smoothScrollTo(0, editTextSearch.getTop()); // Cuộn đến vị trí của SearchView
                        }
                    });
                    return true;
                } else if (id == R.id.favorites) {
                    Intent intent = new Intent(MainActivity.this, FavoriteFilm.class);
                    startActivity(intent);
                    // Xử lý logic khi chọn favorites
                    return true;
                } else if (id == R.id.cart) {
                    // Xử lý logic khi chọn cart
                    Intent intent = new Intent(MainActivity.this, BookMarkActivity.class);
                    startActivity(intent);
                    return true;
                }

                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000 && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                editTextSearch.setText(results.get(0));  // Hiển thị kết quả nhận dạng vào EditText
            }
        }
    }

    private void fetchFilmsFromFirebase() {
        DatabaseReference favouriteRef = database.getReference("Favourite"); // Sử dụng Firebase để lấy dữ liệu phim
        DatabaseReference upcomingRef = database.getReference("Upcomming");
        DatabaseReference itemsRef = database.getReference("Items");

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Film film = dataSnapshot.getValue(Film.class);
                        if (film != null) {
                            filmList.add(film);
                        }
                    }
                    filmListAdapterSearch = new FilmListAdapterSearch(filmList);
                    recyclerViewSearchResults.setAdapter(filmListAdapterSearch);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi nếu có
            }
        };

        favouriteRef.addListenerForSingleValueEvent(listener);
        upcomingRef.addListenerForSingleValueEvent(listener);
        itemsRef.addListenerForSingleValueEvent(listener);
    }

    private void filterFilms(@NonNull String query) {
        filteredFilms.clear();
        if (query.isEmpty()) {
            // Nếu không có từ khóa tìm kiếm, ẩn RecyclerView
            recyclerViewSearchResults.setVisibility(View.GONE);
        } else {
            for (Film film : filmList) {
                if (film.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredFilms.add(film);
                }
            }
            // Nếu có kết quả tìm kiếm, hiện RecyclerView và cập nhật danh sách phim
            if (filteredFilms.isEmpty()) {
                recyclerViewSearchResults.setVisibility(View.GONE);
            } else {
                recyclerViewSearchResults.setVisibility(View.VISIBLE);
                filmListAdapterSearch.updateList(filteredFilms);
            }
        }
    }

    // Phương thức khởi động nhận dạng giọng nói
    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hãy nói tên phim bạn muốn tìm");
        startActivityForResult(intent, 1000);
    }

    // Phương thức mở AllFilmsActivity với danh mục đã chọn
    private void openListMoviesActivity(String category){
        Intent intent = new Intent(MainActivity.this, ListMoviesActivity.class);
        intent.putExtra("category", category);
        startActivity(intent);
    }

    private void initUpComing(){
        DatabaseReference myref = database.getReference("Upcomming");
        binding.progressBarUpComing.setVisibility(View.VISIBLE);
        ArrayList<Film> items = new ArrayList<>();
        myref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot issue : snapshot.getChildren()){
                        items.add(issue.getValue(Film.class));
                    }
                    if (!items.isEmpty()){
                        binding.recyclerViewUpComing.setLayoutManager(new LinearLayoutManager(MainActivity.this,
                                LinearLayoutManager.HORIZONTAL,false));
                        binding.recyclerViewUpComing.setAdapter(new FilmListAdapter(items));
                    }
                    binding.progressBarUpComing.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initTopMoving(){
        DatabaseReference myref = database.getReference("Items");
        binding.progressBarTopMovies.setVisibility(View.VISIBLE);
        ArrayList<Film> items = new ArrayList<>();
        myref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot issue : snapshot.getChildren()){
                        items.add(issue.getValue(Film.class));
                    }
                    if (!items.isEmpty()){
                        binding.recyclerViewTopMovies.setLayoutManager(new LinearLayoutManager(MainActivity.this,
                                LinearLayoutManager.HORIZONTAL,false));
                        binding.recyclerViewTopMovies.setAdapter(new FilmListAdapter(items));
                    }
                    binding.progressBarTopMovies.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initFavourite(){
        DatabaseReference myref = database.getReference("Favourite");
        binding.progressBarFavourite.setVisibility(View.VISIBLE);
        ArrayList<Film> items = new ArrayList<>();
        myref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot issue : snapshot.getChildren()){
                        items.add(issue.getValue(Film.class));
                    }
                    if (!items.isEmpty()){
                        binding.recyclerViewFavourite.setLayoutManager(new LinearLayoutManager(MainActivity.this,
                                LinearLayoutManager.HORIZONTAL,false));
                        binding.recyclerViewFavourite.setAdapter(new FilmListAdapter(items));
                    }
                    binding.progressBarFavourite.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initBanner(){
        DatabaseReference myref = database.getReference("Banners");
        binding.progressBarBanner.setVisibility(View.VISIBLE);
        ArrayList<SliderItems> items = new ArrayList<>();
        myref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot issue : snapshot.getChildren()){
                        items.add(issue.getValue(SliderItems.class));
                    }
                    banners(items);
                    binding.progressBarBanner.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void banners(ArrayList<SliderItems> items){
        binding.viewPager2.setAdapter(new SlidersAdapter(items, binding.viewPager2));
        binding.viewPager2.setClipToPadding(false);
        binding.viewPager2.setClipChildren(false);
        binding.viewPager2.setOffscreenPageLimit(3);
        binding.viewPager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float r=1-Math.abs(position);
                page.setScaleY(0.85f + r * 0.15f);
            }
        });

        binding.viewPager2.setPageTransformer(compositePageTransformer);
        binding.viewPager2.setCurrentItem(1);
        binding.viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandles.removeCallbacks(sliderRunnable);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandles.removeCallbacks(sliderRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sliderHandles.postDelayed(sliderRunnable, 2000);
    }
}