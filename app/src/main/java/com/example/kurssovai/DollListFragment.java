package com.example.kurssovai;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStructure;
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
    private ListView dollListView;
    private ProgressBar progressBar;
    private List<Doll> dollList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Убедитесь, что используете параметр inflater, а не создаете новую переменную view
        View rootView = inflater.inflate(R.layout.fragment_doll_list, container, false);

        dollListView = rootView.findViewById(R.id.dollListView);
        Button btnCreateDoll = rootView.findViewById(R.id.btnCreateDoll);
        Button btnLogout = rootView.findViewById(R.id.btnLogout);
        progressBar = rootView.findViewById(R.id.progressBar);

        DollListAdapter adapter = new DollListAdapter(requireContext(), dollList);
        dollListView.setAdapter(adapter);

        dollListView.setOnItemClickListener((parent, view, position, id) -> {
            if (!isAdded() || isDetached() || getActivity() == null || getActivity().isFinishing()) {
                Log.e("LIFECYCLE", "Фрагмент не активен!");
                return;
            }
            if (Looper.myLooper() != Looper.getMainLooper()) {
                Log.e("THREAD", "Это НЕ главный поток!");
            } else {
                Log.d("THREAD", "Это главный UI поток");
            }
            Toast.makeText(requireContext(), "Клик обработан", Toast.LENGTH_SHORT).show();
            Doll selectedDoll = dollList.get(position);
            Log.d("CLICK", "Clicked doll ID: " + selectedDoll.getId());

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).loadDollEditorFragment2(selectedDoll.getId());
            } else {
                Log.e("ERROR", "Activity is not MainActivity");
            }
        });


        btnCreateDoll.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).loadDollEditorFragment();
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            ((MainActivity) requireActivity()).loadLoginFragment();
        });

        loadUserDolls();
        return rootView;
    }

    private void loadUserDolls() {
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
                        ((DollListAdapter) dollListView.getAdapter()).notifyDataSetChanged();
                    } else {
                        Toast.makeText(requireContext(),
                                "Ошибка загрузки: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserDolls();
    }
}