package com.example.filmnest.Activities;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.filmnest.Domains.Model_DangKy;
import com.example.filmnest.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DangKy extends AppCompatActivity {
    EditText email, mobile, password, conPassword, name;
    ImageView passwordIcon, conPasswordIcon, uploadImage;
    AppCompatButton signUpBtn;
    TextView signInBtn;
    private boolean passwordShowing = false;
    private boolean conPasswordShowing = false;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private String fixedImageUrl = "https://firebasestorage.googleapis.com/v0/b/filmnest-2b84b.appspot.com/o/avatar_filmnest.png?alt=media&token=98e5714c-30cd-4d86-94e2-0f5e29545110";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.dangky);
        main();

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Taikhoan");
    }

    public void anhXa() {
        email = findViewById(R.id.emailET);
        mobile = findViewById(R.id.mobileET);
        name = findViewById(R.id.fullNameET);
        password = findViewById(R.id.passwordET);
        conPassword = findViewById(R.id.conPasswordET);
        passwordIcon = findViewById(R.id.passwordIcon);
        conPasswordIcon = findViewById(R.id.conPasswordIcon);
        signUpBtn = findViewById(R.id.signUpBtn);
        signInBtn = findViewById(R.id.signInBtn);
    }

    public void main() {
        anhXa();
        NgoaiLe();
        SuKien();
    }

    public void SuKien() {
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DangKy();
            }
        });
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DangNhap();
            }
        });
        passwordIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PasswordIcon();
            }
        });
        conPasswordIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConPasswordIcon();
            }
        });
    }

    @SuppressLint("NotConstructor")
    private void DangKy() {
        String fullname = name.getText().toString().trim();
        String mail = email.getText().toString().trim();
        String matkhau = password.getText().toString().trim();
        String nhapMK = conPassword.getText().toString().trim();
        String phoneNumber = mobile.getText().toString().trim();

        if (fullname.isEmpty() || name.getError() != null) {
            name.setError("Nhập đủ thông tin");
            return;
        }
        if (mail.isEmpty() || email.getError() != null) {
            email.setError("Nhập đủ thông tin");
            return;
        }
        if (phoneNumber.isEmpty() || mobile.getError() != null) {
            mobile.setError("Nhập đủ thông tin");
            return;
        }
        if (matkhau.isEmpty() || password.getError() != null) {
            password.setError("Nhập đủ thông tin");
            return;
        }
        if (nhapMK.isEmpty() || conPassword.getError() != null) {
            conPassword.setError("Nhập đủ thông tin");
            return;
        }

        // Đăng ký với Firebase Authentication
        mAuth.createUserWithEmailAndPassword(mail, matkhau)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Đăng ký thành công
                        FirebaseUser user = mAuth.getCurrentUser();

                        // Cập nhật tên người dùng (fullname) vào Firebase Authentication
                        if (user != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullname)  // Lưu tên đầy đủ
                                    .setPhotoUri(Uri.parse(fixedImageUrl)) // Lưu ảnh mặc định
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            // Lưu số điện thoại vào Realtime Database
                                            String userId = user.getUid();
                                            Model_DangKy dangky = new Model_DangKy(fullname, mail, phoneNumber, matkhau, "", "", fixedImageUrl);
                                            databaseReference.child(userId).setValue(dangky)
                                                    .addOnCompleteListener(task2 -> {
                                                        if (task2.isSuccessful()) {
                                                            Toast.makeText(DangKy.this, "Đăng ký thành công và hồ sơ được cập nhật", Toast.LENGTH_SHORT).show();
                                                            Intent dangkyIntent = new Intent(DangKy.this, DangNhap.class);
                                                            startActivity(dangkyIntent);
                                                        } else {
                                                            Toast.makeText(DangKy.this, "Lỗi khi lưu thông tin người dùng", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(DangKy.this, "Lỗi khi cập nhật hồ sơ", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(DangKy.this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void PasswordIcon() {
        if (passwordShowing) {
            passwordShowing = false;
            password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordIcon.setImageResource(R.drawable.password);
        } else {
            passwordShowing = true;
            password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordIcon.setImageResource(R.drawable.password_hind);
        }
        password.setSelection(password.length());
    }

    public void ConPasswordIcon() {
        if (conPasswordShowing) {
            conPasswordShowing = false;
            conPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            conPasswordIcon.setImageResource(R.drawable.password);
        } else {
            conPasswordShowing = true;
            conPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            conPasswordIcon.setImageResource(R.drawable.password_hind);
        }
        conPassword.setSelection(conPassword.length());
    }

    public void NgoaiLe() {
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().isEmpty()) {
                    name.setError("Nhập đủ thông tin");
                } else {
                    name.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String mail = charSequence.toString().trim();
                if(mail.isEmpty()){
                    email.setError("Điền đủ thông tin");
                } else if (!mail.matches("^[a-zA-Z0-9._%+-]+@gmail\\.com$")) {
                    email.setError("Email phải có định dạng đúng và kết thúc bằng @gmail.com");
                }else{
                    databaseReference.orderByChild("email").equalTo(mail)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        email.setError("Email đã tồn tại");
                                    }else {
                                        email.setError(null);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(DangKy.this, "Có lỗi xảy ra: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String sdt = charSequence.toString().trim();
                if(sdt.isEmpty()){
                    mobile.setError("Nhập Đủ Thông Tin");
                } else if (!sdt.matches("^(\\+84|0)\\d{9}$")) {
                    mobile.setError("Số điện thoại không hợp lệ");
                }else {
                    databaseReference.orderByChild("number").equalTo(sdt)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        mobile.setError("Số điện thoại đã tồn tại");
                                    }else {
                                        mobile.setError(null);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(DangKy.this, "Có lỗi xảy ra: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String mk = charSequence.toString().trim();
                if(mk.isEmpty()){
                    password.setError("Nhập đủ thông tin");
                } else if (!Character.isUpperCase(mk.charAt(0))) {
                    password.setError("Viết hoa chữ cái đầu");
                } else if (mk.length() < 5) {
                    password.setError("Mật khẩu phải dài hơn 5 kí tự");
                } else if (!mk.matches(".*\\d.*")){
                    password.setError("Mật khẩu phải chứa ít nhất một chữ số");
                } else if (!mk.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
                    password.setError("Mật khẩu phải chứa ít nhất một ký tự đặc biệt");
                }else {
                    password.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        conPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String mkNhapLai = charSequence.toString().trim();
                String mkMoi = password.getText().toString().trim();
                if (mkNhapLai.isEmpty()) {
                    conPassword.setError("Vui lòng nhập lại mật khẩu mới");
                } else if (!mkNhapLai.equals(mkMoi)) {
                    conPassword.setError("Mật khẩu mới không khớp");
                }else {
                    conPassword.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    public void DangNhap() {
        finish();
    }
}
