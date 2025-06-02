package com.example.kurssovai;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.kurssovai.MainActivity;
import com.example.kurssovai.R;
import com.example.kurssovai.FirebaseAuthHelper;

public class RegisterFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        FirebaseAuthHelper authHelper = new FirebaseAuthHelper(requireActivity());

        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etPassword = view.findViewById(R.id.etPassword);
        EditText etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        Button btnRegister = view.findViewById(R.id.btnRegister);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);

        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(requireContext(), "Пароли не совпадают", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            authHelper.registerUser(email, password, new FirebaseAuthHelper.AuthCallback() {
                @Override
                public void onSuccess() {
                    progressBar.setVisibility(View.GONE);
                    ((MainActivity) requireActivity()).loadDollListFragment();
                }

                @Override
                public void onError(String message) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                }
            });
        });

        return view;
    }
}