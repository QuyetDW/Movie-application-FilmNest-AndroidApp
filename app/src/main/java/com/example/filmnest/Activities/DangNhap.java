package com.example.filmnest.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.filmnest.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DangNhap extends AppCompatActivity {
    EditText usernameET, passwordET;
    TextView signUpBtn;
    ImageView passwordIcon;
    AppCompatButton signInBtn;
    private FirebaseAuth mAuth;
    private boolean passwordShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.dangnhap);

        anhXa();
        main();
    }

    public void main() {
        DangKy();
        PasswordIcon();
        DangNhap();
    }

    @SuppressLint("NotConstructor")
    public void DangNhap() {
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = usernameET.getText().toString().trim();
                String password = passwordET.getText().toString().trim();

                if (email.isEmpty()) {
                    Toast.makeText(DangNhap.this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.isEmpty()) {
                    Toast.makeText(DangNhap.this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(DangNhap.this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(DangNhap.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                                // Chuyển hướng đến MainActivity với thông tin người dùng nếu cần
                                Intent intent_dangNhap = new Intent(DangNhap.this, MainActivity.class);
                                // Bạn có thể lấy thêm thông tin người dùng từ user nếu cần
                                startActivity(intent_dangNhap);
                            } else {
                                Toast.makeText(DangNhap.this, "Tên đăng nhập hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    public void anhXa() {
        usernameET = findViewById(R.id.usernameET);
        passwordET = findViewById(R.id.passwordET);
        signUpBtn = findViewById(R.id.signUpBtn);
        signInBtn = findViewById(R.id.signInBtn);
        passwordIcon = findViewById(R.id.passwordIcon);
        mAuth = FirebaseAuth.getInstance();
    }

    public void PasswordIcon() {
        passwordIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (passwordShowing) {
                    passwordShowing = false;
                    passwordET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    passwordIcon.setImageResource(R.drawable.password);
                } else {
                    passwordShowing = true;
                    passwordET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    passwordIcon.setImageResource(R.drawable.password_hind);
                }
                passwordET.setSelection(passwordET.length());
            }
        });
    }

    public void DangKy() {
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_dangky = new Intent(DangNhap.this, DangKy.class);
                startActivity(intent_dangky);
            }
        });
    }
}
