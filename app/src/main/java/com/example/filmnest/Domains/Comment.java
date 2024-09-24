package com.example.filmnest.Domains;

public class Comment {
    private String userName;
    private String content;
    private float rating;

    public Comment() {
    }

    public Comment(String userName, String content, float rating) {
        this.userName = userName;
        this.content = content;
        this.rating = rating;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
