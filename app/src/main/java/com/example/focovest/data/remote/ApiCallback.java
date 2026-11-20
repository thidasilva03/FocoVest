package com.example.focovest.data.remote;

public interface ApiCallback<T> {
    void onSuccess(T data);
    void onError(int httpCode, String message);
}
