package com.example.kurssovai;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;


public class LoginFragment extends Fragment {
    private FirebaseAuthHelper authHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        authHelper = new FirebaseAuthHelper(requireActivity());

        Button btnLogin = view.findViewById(R.id.btnLogin);
        Button btnRegister = view.findViewById(R.id.btnRegister);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> {
            String email = ((EditText) view.findViewById(R.id.etEmail)).getText().toString();
            String password = ((EditText) view.findViewById(R.id.etPassword)).getText().toString();

            progressBar.setVisibility(View.VISIBLE);
            authHelper.loginUser(email, password, new FirebaseAuthHelper.AuthCallback() {
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

        btnRegister.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).loadRegisterFragment();
        });

        return view;
    }
}