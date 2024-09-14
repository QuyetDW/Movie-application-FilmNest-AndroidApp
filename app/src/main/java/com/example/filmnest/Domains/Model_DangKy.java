package com.example.filmnest.Domains;

public class Model_DangKy {
    private String fullname;
    private String email;
    private String number;
    private String password;
    private String ngaysinh;
    private String diachi;
    private String avatar; // Thêm trường avatar

    // Constructor mặc định
    public Model_DangKy() {
    }

    // Constructor có tham số, bao gồm avatar
    public Model_DangKy(String fullname, String email, String number, String password, String ngaysinh, String diachi, String avatar) {
        this.fullname = fullname;
        this.email = email;
        this.number = number;
        this.password = password;
        this.ngaysinh = ngaysinh;
        this.diachi = diachi;
        this.avatar = avatar; // Khởi tạo avatar
    }

    // Getter và Setter cho fullname
    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getNgaysinh() {
        return ngaysinh;
    }

    public void setNgaysinh(String ngaysinh) {
        this.ngaysinh = ngaysinh;
    }

    public String getDiachi() {
        return diachi;
    }

    public void setDiachi(String diachi) {
        this.diachi = diachi;
    }


    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
