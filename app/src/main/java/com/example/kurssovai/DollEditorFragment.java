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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DollEditorFragment extends Fragment {
    private FrameLayout dollContainer;
    private List<ImageView> clothingLayers = new ArrayList<>();
    private List<String> selectedClothingIds = new ArrayList<>();
    private String dollId;
    private Doll currentDoll;

    // Фабричный метод для создания фрагмента с аргументами
    public static DollEditorFragment newInstance(String dollId) {
        DollEditorFragment fragment = new DollEditorFragment();
        Bundle args = new Bundle();
        args.putString("doll_id", dollId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dollId = getArguments().getString("doll_id");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doll_editor, container, false);
        dollContainer = view.findViewById(R.id.dollContainer);

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

        if (dollId != null) {
            loadDollData(progressBar);
        } else {
            // Режим создания новой куклы
            currentDoll = new Doll();
        }

        return view;
    }


    private void loadDollData(ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("dolls").document(dollId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    if (documentSnapshot.exists()) {
                        currentDoll = documentSnapshot.toObject(Doll.class);
                        currentDoll.setId(documentSnapshot.getId());
                        restoreDollState();
                    } else {
                        Toast.makeText(requireContext(), "Кукла не найдена", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Ошибка загрузки: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void restoreDollState() {
        // Очищаем текущие слои
        for (ImageView layer : clothingLayers) {
            dollContainer.removeView(layer);
        }
        clothingLayers.clear();
        selectedClothingIds.clear();

        // Добавляем базовую куклу (уже есть)
        // Добавляем сохраненные слои одежды
        for (String clothingId : currentDoll.getClothingLayers()) {
            // Здесь нужно получить ресурс по clothingId
            // Предположим, что у нас есть маппинг
            Integer resId = getResourceIdForClothing(clothingId);
            if (resId != null) {
                addClothingLayer(resId, clothingId);
            }
        }
    }

    private Integer getResourceIdForClothing(String clothingId) {
        // Реализуйте маппинг ID одежды на ресурсы
        Map<String, Integer> clothingMap = new HashMap<>();
        clothingMap.put("shirt1", R.drawable.shirt1);
        clothingMap.put("pants1", R.drawable.pants1);
        clothingMap.put("hat1", R.drawable.hat1);
        return clothingMap.get(clothingId);
    }

    private void addClothingLayer(int resId, String clothingId) {
        ImageView layer = new ImageView(requireContext());
        layer.setImageResource(resId);
        dollContainer.addView(layer);
        clothingLayers.add(layer);
        selectedClothingIds.add(clothingId);

        // Добавляем возможность удаления слоя при долгом нажатии
        layer.setOnLongClickListener(v -> {
            dollContainer.removeView(layer);
            clothingLayers.remove(layer);
            selectedClothingIds.remove(clothingId);
            return true;
        });
        layer.setAlpha(0f);
        layer.animate()
                .alpha(1f)
                .setDuration(300)
                .start();
    }

    // Изменяем обработчик добавления одежды
    private void setupClothingPanel(LinearLayout clothingPanel, ProgressBar progressBar) {
        int[] clothingItems = {R.drawable.shirt1, R.drawable.pants1, R.drawable.hat1};
        String[] clothingIds = {"shirt1", "pants1", "hat1"};

        for (int i = 0; i < clothingItems.length; i++) {
            ImageView itemView = new ImageView(requireContext());
            itemView.setImageResource(clothingItems[i]);
            // ... параметры ...

            final int index = i;
            itemView.setOnClickListener(v -> {
                addClothingLayer(clothingItems[index], clothingIds[index]);
            });

            clothingPanel.addView(itemView);
        }
    }

    private void saveDoll(ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Обновляем данные куклы
        currentDoll.setUserId(userId);
        currentDoll.setBaseDoll("base_doll");
        currentDoll.setClothingLayers(new ArrayList<>(selectedClothingIds));

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (dollId != null) {
            // Обновляем существующую куклу
            db.collection("dolls").document(dollId)
                    .set(currentDoll)
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Кукла обновлена", Toast.LENGTH_SHORT).show();
                        requireActivity().onBackPressed();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Ошибка обновления: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Создаем новую куклу
            db.collection("dolls")
                    .add(currentDoll)
                    .addOnSuccessListener(documentReference -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Кукла сохранена", Toast.LENGTH_SHORT).show();
                        currentDoll.setId(documentReference.getId());
                        requireActivity().onBackPressed();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Ошибка сохранения: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }


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

