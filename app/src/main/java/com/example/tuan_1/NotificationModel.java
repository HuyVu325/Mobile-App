package com.example.tuan_1;

import com.google.firebase.Timestamp;

// Lớp này đại diện cho một đối tượng thông báo
public class NotificationModel {
    private String title;
    private String content;
    private Timestamp timestamp; // Để sắp xếp thông báo theo thời gian

    // Constructor mặc định là bắt buộc cho Firestore
    public NotificationModel() { }

    public NotificationModel(String title, String content, Timestamp timestamp) {
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Getters và Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
