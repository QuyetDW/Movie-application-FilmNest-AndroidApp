package com.example.filmnest.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.filmnest.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

public class ThongTinCaNhan extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference userDatabaseReference;
    private ImageView setting;
    private Uri imageUri;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private static final int PICK_IMAGE_REQUEST = 1;

    // Khai báo các biến TextView và ImageView
    private TextView fullNameTextView, emailTextView, phoneNumberTextView, birthDateTextView, addressTextView, changePassword;
    private ImageView close, changeAvatar, avatarImageView;
    Button logOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_tin_ca_nhan);

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        // Kết nối các View trong layout với Java code
        fullNameTextView = findViewById(R.id.fullName);
        emailTextView = findViewById(R.id.email);
        phoneNumberTextView = findViewById(R.id.phoneNumber);
        birthDateTextView = findViewById(R.id.birthDate);
        addressTextView = findViewById(R.id.address);
        avatarImageView = findViewById(R.id.avatarUrl);
        close = findViewById(R.id.img_close);
        changeAvatar = findViewById(R.id.img_camera);
        setting = findViewById(R.id.setting);
        changePassword = findViewById(R.id.changePassword);
        logOut = findViewById(R.id.logout);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("avatar");

        if (currentUser != null) {
            String userId = currentUser.getUid();
            // Truy vấn Firebase Realtime Database dựa trên UID người dùng
            databaseReference = FirebaseDatabase.getInstance().getReference("Taikhoan");
            userDatabaseReference = FirebaseDatabase.getInstance().getReference("Taikhoan").child(userId);
            // Sử dụng addValueEventListener để lắng nghe thay đổi dữ liệu theo thời gian thực
            // Thiết lập lắng nghe sự kiện Firebase
            valueEventListener = userDatabaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Kiểm tra trạng thái của Activity trước khi thực hiện các thao tác
                    if (isDestroyed() || isFinishing()) {
                        return;
                    }

                    if (snapshot.exists()) {
                        // Lấy dữ liệu từ DataSnapshot và hiển thị
                        String fullName = snapshot.child("fullname").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        String avatarUrl = snapshot.child("avatar").getValue(String.class);
                        String address = snapshot.child("diachi").getValue(String.class);
                        String birthDate = snapshot.child("ngaysinh").getValue(String.class);
                        String phoneNumber = snapshot.child("number").getValue(String.class);

                        // Hiển thị dữ liệu lên UI
                        fullNameTextView.setText(fullName);
                        emailTextView.setText(email);
                        phoneNumberTextView.setText(phoneNumber);
                        birthDateTextView.setText(birthDate);
                        addressTextView.setText(address);

                        // Sử dụng Glide để load hình ảnh từ URL vào ImageView
                        Glide.with(ThongTinCaNhan.this)
                                .load(avatarUrl)
                                .into(avatarImageView);
                    } else {
                        Toast.makeText(ThongTinCaNhan.this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Kiểm tra trạng thái của Activity trước khi hiển thị Toast
                    if (!isDestroyed() && !isFinishing()) {
                        Toast.makeText(ThongTinCaNhan.this, "Lỗi khi lấy dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        // Xử lý nút  thoát
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        // Xử lý sự kiện khi bấm vào setting
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog();
            }
        });
        // Xử lý sự kiện khi nhấn vào nút changePassword
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangePasswordDialog();
            }
        });
        // Xử lý sự kiện thay đổi avatar
        changeAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImagePicker();;
            }
        });
        // Xử lý đăng xuất
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Đăng xuất người dùng
                auth.signOut();

                // Hiển thị thông báo
                Toast.makeText(ThongTinCaNhan.this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();

                // Chuyển về màn hình đăng nhập
                Intent intent = new Intent(ThongTinCaNhan.this, DangNhap.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Xóa stack các activity trước đó
                startActivity(intent);
                finish(); // Đóng activity hiện tại
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy lắng nghe sự kiện Firebase khi Activity bị hủy
        if (valueEventListener != null) {
            userDatabaseReference.removeEventListener(valueEventListener);
        }
    }
    private void showEditDialog() {
        // Tạo Dialog
        Dialog dialog = new Dialog(ThongTinCaNhan.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.setting);

        // Kết nối các thành phần của dialog với code
        EditText nameTxt = dialog.findViewById(R.id.nameTxt);
        EditText phoneNumberTxt = dialog.findViewById(R.id.phoneNumber);
        EditText birthDateTxt = dialog.findViewById(R.id.birthDate);
        EditText addressTxt = dialog.findViewById(R.id.address);
        Button saveBtn = dialog.findViewById(R.id.saveBtn);
        Button cancelBtn = dialog.findViewById(R.id.cancelBtn);

        // Hiển thị dữ liệu người dùng hiện tại trên Dialog
        nameTxt.setText(fullNameTextView.getText().toString());
        phoneNumberTxt.setText(phoneNumberTextView.getText().toString());
        birthDateTxt.setText(birthDateTextView.getText().toString());
        addressTxt.setText(addressTextView.getText().toString());

        nameTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().isEmpty()) {
                    nameTxt.setError("Vui lòng điền tên đầy đủ");
                } else {
                    nameTxt.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        phoneNumberTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String sdtText = charSequence.toString().trim();
                if (sdtText.isEmpty()) {
                    phoneNumberTxt.setError("Vui lòng điền số điện thoại");
                } else if (!sdtText.matches("^(\\+84|0)\\d{9}$")) {
                    phoneNumberTxt.setError("Số điện thoại không hợp lệ");
                } else {
                    // Kiểm tra xem số điện thoại đã tồn tại trong cơ sở dữ liệu hay chưa
                    databaseReference.orderByChild("number").equalTo(sdtText)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                phoneNumberTxt.setError("Số điện thoại đã tồn tại");
                            }else {
                                phoneNumberTxt.setError(null);
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(ThongTinCaNhan.this, "Có lỗi xảy ra: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        birthDateTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String datePattern = "^([0-2][0-9]|3[0-1])/(0[1-9]|1[0-2])/\\d{4}$";
                if (charSequence.toString().trim().isEmpty()) {
                    birthDateTxt.setError("Vui lòng chọn ngày");
                } else if (!charSequence.toString().matches(datePattern)) {
                    birthDateTxt.setError("Ngày không hợp lệ. Định dạng: dd/MM/yyyy");
                } else {
                    birthDateTxt.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        addressTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().isEmpty()) {
                    addressTxt.setError("Vui lòng điền địa chỉ");
                } else {
                    addressTxt.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Xử lý sự kiện khi nhấn nút Lưu
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = nameTxt.getText().toString();
                String newPhoneNumber = phoneNumberTxt.getText().toString();
                String newBirthDate = birthDateTxt.getText().toString();
                String newAddress = addressTxt.getText().toString();
                FirebaseUser currentUser = auth.getCurrentUser();

                // Cập nhật dữ liệu người dùng trong Firebase
                if (currentUser != null) {
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Taikhoan").child(currentUser.getUid());
                    userRef.child("fullname").setValue(newName);
                    userRef.child("number").setValue(newPhoneNumber);
                    userRef.child("ngaysinh").setValue(newBirthDate);
                    userRef.child("diachi").setValue(newAddress).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Cập nhật UI ngay lập tức
                            fullNameTextView.setText(newName);
                            phoneNumberTextView.setText(newPhoneNumber);
                            birthDateTextView.setText(newBirthDate);
                            addressTextView.setText(newAddress);

                            Toast.makeText(ThongTinCaNhan.this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                            dialog.dismiss(); // Đóng dialog sau khi lưu thành công
                        } else {
                            Toast.makeText(ThongTinCaNhan.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        // Xử lý sự kiện khi nhấn nút Hủy
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); // Đóng dialog
            }
        });

        // Hiển thị Dialog
        dialog.show();
    }

    private void showChangePasswordDialog() {
        // Tạo Dialog
        Dialog dialog = new Dialog(ThongTinCaNhan.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.doi_mat_khau);

        // Kết nối các thành phần của dialog với code
        EditText oldPasswordEditText = dialog.findViewById(R.id.oldPassword);
        EditText newPasswordEditText = dialog.findViewById(R.id.newPassword);
        EditText confirmPasswordEditText = dialog.findViewById(R.id.comfirmPassword);
        Button saveBtn = dialog.findViewById(R.id.saveBtn);
        Button cancelBtn = dialog.findViewById(R.id.cancelBtn);

        oldPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().isEmpty()) {
                    oldPasswordEditText.setError("Hãy nhập mật khẩu cũ!");
                }else {
                    oldPasswordEditText.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        newPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String newPass = charSequence.toString().trim();
                if (newPass.isEmpty()) {
                    newPasswordEditText.setError("Vui lòng nhập mật khẩu mới");
                } else if (!Character.isUpperCase(newPass.charAt(0))) {
                    newPasswordEditText.setError("Chữ cái đầu tiên của mật khẩu phải viết hoa");
                } else if (newPass.length() < 5) {
                    newPasswordEditText.setError("Mật khẩu phải có ít nhất 5 ký tự");
                } else if (!newPass.matches(".*\\d.*")) {
                    newPasswordEditText.setError("Mật khẩu phải chứa ít nhất một chữ số");
                } else if (!newPass.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
                    newPasswordEditText.setError("Mật khẩu phải chứa ít nhất một ký tự đặc biệt");
                } else {
                    newPasswordEditText.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        confirmPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String comfirmPass = charSequence.toString().trim();
                String newPass = newPasswordEditText.getText().toString().trim();
                if (comfirmPass.isEmpty()) {
                    confirmPasswordEditText.setError("Vui lòng nhập lại mật khẩu mới");
                } else if (!comfirmPass.equals(newPass)) {
                    confirmPasswordEditText.setError("Mật khẩu mới không khớp!");
                } else {
                    confirmPasswordEditText.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        // Xử lý sự kiện khi nhấn nút Lưu
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = oldPasswordEditText.getText().toString();
                String newPassword = newPasswordEditText.getText().toString();
                String confirmPassword = confirmPasswordEditText.getText().toString();

                // Kiểm tra mật khẩu mới và xác nhận mật khẩu có khớp không
                if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
                    Toast.makeText(ThongTinCaNhan.this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                } else if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(ThongTinCaNhan.this, "Mật khẩu mới và xác nhận không khớp", Toast.LENGTH_SHORT).show();
                } else {
                    // Đổi mật khẩu trong Firebase Authentication
                    changePassword(oldPassword, newPassword, dialog);
                }
            }
        });

        // Xử lý sự kiện khi nhấn nút Hủy
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); // Đóng dialog
            }
        });

        // Hiển thị Dialog
        dialog.show();
    }

    private void changePassword(String oldPassword, String newPassword, Dialog dialog) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            // Xác thực người dùng bằng mật khẩu cũ
            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), oldPassword);
            currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Người dùng đã được xác thực, thực hiện đổi mật khẩu
                    currentUser.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            Toast.makeText(ThongTinCaNhan.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                            dialog.dismiss(); // Đóng dialog sau khi đổi mật khẩu thành công
                        } else {
                            Toast.makeText(ThongTinCaNhan.this, "Đổi mật khẩu thất bại", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(ThongTinCaNhan.this, "Xác thực mật khẩu cũ không thành công", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    // Mở trình chọn ảnh
    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh đại diện"), PICK_IMAGE_REQUEST);
    }

    // Xử lý kết quả trả về từ trình chọn ảnh
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            imageUri = data.getData();
            try {
                // Hiển thị ảnh đã chọn lên ImageView trước khi upload
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                avatarImageView.setImageBitmap(bitmap);

                // Bắt đầu quá trình upload ảnh
                uploadImageToFirebase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Tải ảnh lên Firebase Storage
    private void uploadImageToFirebase() {
        if (imageUri != null) {
            // Hiển thị ProgressDialog
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Đang cập nhật ảnh đại diện...");
            progressDialog.show();

            // Tạo tham chiếu đến vị trí lưu trữ ảnh
            StorageReference ref = storageReference.child("avatar/" + auth.getCurrentUser().getUid() + ".jpg");

            // Tải ảnh lên Firebase Storage
            ref.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Lấy URL tải xuống sau khi upload thành công
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();

                            // Cập nhật URL ảnh đại diện trong Firebase Realtime Database
                            userDatabaseReference.child("avatar").setValue(downloadUrl).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ThongTinCaNhan.this, "Cập nhật ảnh đại diện thành công", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ThongTinCaNhan.this, "Cập nhật ảnh đại diện thất bại", Toast.LENGTH_SHORT).show();
                                }
                                progressDialog.dismiss();
                            });
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(ThongTinCaNhan.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener(snapshot -> {
                        double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        progressDialog.setMessage("Đã tải lên " + (int) progress + "%");
                    });
        } else {
            Toast.makeText(this, "Không có ảnh được chọn", Toast.LENGTH_SHORT).show();
        }
    }

}