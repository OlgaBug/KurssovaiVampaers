package com.example.kurssovai;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.kurssovai.MainActivity;
import com.example.kurssovai.R;
import com.example.kurssovai.DollListAdapter;
import com.example.kurssovai.Doll;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DollListFragment extends Fragment {
    private List<Doll> dollList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doll_list, container, false);

        ListView dollListView = view.findViewById(R.id.dollListView);
        Button btnCreateDoll = view.findViewById(R.id.btnCreateDoll);
        Button btnLogout = view.findViewById(R.id.btnLogout);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);

        DollListAdapter adapter = new DollListAdapter(requireContext(), dollList);
        dollListView.setAdapter(adapter);

        btnCreateDoll.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).loadDollEditorFragment();
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            ((MainActivity) requireActivity()).loadLoginFragment();
        });

        // Загрузка кукол пользователя
        progressBar.setVisibility(View.VISIBLE);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("dolls")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        dollList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Doll doll = document.toObject(Doll.class);
                            doll.setId(document.getId());
                            dollList.add(doll);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(requireContext(),
                                "Ошибка загрузки: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });

        return view;
    }
}