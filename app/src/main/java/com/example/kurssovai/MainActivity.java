package com.example.kurssovai;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        checkCurrentUser();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            loadFragment(new LoginFragment());
        }
    }

    private void checkCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            loadDollListFragment();
        } else {
            loadLoginFragment();
        }
    }

    public void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void loadFragmentWithBackStack(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void loadLoginFragment() {
        loadFragment(new LoginFragment());
    }

    public void loadRegisterFragment() {
        loadFragmentWithBackStack(new RegisterFragment());
    }

    public void loadDollListFragment() {
        loadFragment(new DollListFragment());
    }

    public void loadDollEditorFragment() {
        loadFragmentWithBackStack(new DollEditorFragment());
    }

    public void loadDollEditorFragment2(String dollId) {
        Log.d("LOAD", "Loading editor for doll: " + dollId);

        DollEditorFragment fragment = DollEditorFragment.newInstance(dollId);
        loadFragmentWithBackStack(fragment);
    }
}