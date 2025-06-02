package com.example.kurssovai;

import android.content.Context;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class FirebaseAuthHelper {
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;
    private final Context context;

    public FirebaseAuthHelper(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public interface AuthCallback {
        void onSuccess();
        void onError(String message);
    }

    public void loginUser(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onError("Ошибка входа: " + task.getException().getMessage());
                    }
                });
    }

    public void registerUser(String email, String password, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        createUserInFirestore(mAuth.getCurrentUser().getUid(), callback);
                    } else {
                        callback.onError("Ошибка регистрации: " + task.getException().getMessage());
                    }
                });
    }

    private void createUserInFirestore(String userId, AuthCallback callback) {
        com.example.kurssovai.User user =
                new com.example.kurssovai.User(userId, new ArrayList<>());

        db.collection("users").document(userId)
                .set(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onError("Ошибка создания пользователя: " + task.getException().getMessage());
                    }
                });
    }
}