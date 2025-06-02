package com.example.kurssovai;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.kurssovai.R;
import com.example.kurssovai.Doll;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DollEditorFragment extends Fragment {
    private List<ImageView> clothingLayers = new ArrayList<>();
    private List<String> selectedClothingIds = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doll_editor, container, false);

        FrameLayout dollContainer = view.findViewById(R.id.dollContainer);
        LinearLayout clothingPanel = view.findViewById(R.id.clothingPanel);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnPrint = view.findViewById(R.id.btnPrint);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);

        // Загрузка базовой куклы
        ImageView baseDoll = new ImageView(requireContext());
        baseDoll.setImageResource(R.drawable.base_doll); // Замените на ваш ресурс
        dollContainer.addView(baseDoll);

        // Загрузка элементов одежды (пример)
        int[] clothingItems = {R.drawable.shirt1, R.drawable.pants1, R.drawable.hat1};
        String[] clothingIds = {"shirt1", "pants1", "hat1"};

        for (int i = 0; i < clothingItems.length; i++) {
            ImageView itemView = new ImageView(requireContext());
            itemView.setImageResource(clothingItems[i]);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(200, 200);
            params.setMargins(8, 0, 8, 0);
            itemView.setLayoutParams(params);

            final int index = i;
            itemView.setOnClickListener(v -> {
                ImageView layer = new ImageView(requireContext());
                layer.setImageResource(clothingItems[index]);
                dollContainer.addView(layer);
                clothingLayers.add(layer);
                selectedClothingIds.add(clothingIds[index]);
            });

            clothingPanel.addView(itemView);
        }

        btnSave.setOnClickListener(v -> saveDoll(progressBar));
        btnPrint.setOnClickListener(v -> generatePrintCode(progressBar));

        return view;
    }

    private void saveDoll(ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Doll doll = new Doll();
        doll.setUserId(userId);
        doll.setBaseDoll("base_doll"); // ID базовой куклы
        doll.setClothingLayers(selectedClothingIds);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("dolls")
                .add(doll)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Кукла сохранена", Toast.LENGTH_SHORT).show();
                    doll.setId(documentReference.getId());
                    requireActivity().onBackPressed(); // Возврат к списку
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Ошибка сохранения: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void generatePrintCode(ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        String printCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // В реальном приложении здесь нужно сохранить код печати
        // в документе куклы и в отдельной коллекции для печати

        progressBar.setVisibility(View.GONE);
        Toast.makeText(requireContext(),
                "Код печати: " + printCode + "\nСохраните его для будущей печати",
                Toast.LENGTH_LONG).show();
    }
}