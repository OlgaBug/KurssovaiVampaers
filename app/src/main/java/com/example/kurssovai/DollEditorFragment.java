package com.example.kurssovai;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DollEditorFragment extends Fragment {
    private static final String TYPE_SHIRT = "shirt";
    private static final String TYPE_PANTS = "pants";
    private static final String TYPE_HAIR = "hair";
    private static final String TYPE_SHOES = "shoes";

    // Базовые размеры куклы (оригинальные размеры)
    private static final int BASE_DOLL_WIDTH = 500;
    private static final int BASE_DOLL_HEIGHT = 700;
    private float scaleFactor = 1.0f;

    private FrameLayout dollContainer;
    private List<ClothingItem> clothingItems = new ArrayList<>();
    private List<String> selectedClothingIds = new ArrayList<>();
    private String dollId;
    private Doll currentDoll;
    private Map<String, Integer> clothingCounts = new HashMap<>();

    private class ClothingItem {
        ImageView imageView;
        String id;
        String type;
        int originalX; // Оригинальная позиция X (до масштабирования)
        int originalY; // Оригинальная позиция Y (до масштабирования)
        int currentX;
        int currentY;
        int originalWidth; // Оригинальная ширина (до масштабирования)
        int originalHeight; // Оригинальная высота (до масштабирования)

        public ClothingItem(ImageView imageView, String id, String type,
                            int originalX, int originalY, int originalWidth, int originalHeight) {
            this.imageView = imageView;
            this.id = id;
            this.type = type;
            this.originalX = originalX;
            this.originalY = originalY;
            this.originalWidth = originalWidth;
            this.originalHeight = originalHeight;
            // Текущие позиции рассчитываются с учетом масштаба
            this.currentX = (int)(originalX * scaleFactor);
            this.currentY = (int)(originalY * scaleFactor);
        }
    }

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

        clothingCounts.put(TYPE_SHIRT, 0);
        clothingCounts.put(TYPE_PANTS, 0);
        clothingCounts.put(TYPE_HAIR, 0);
        clothingCounts.put(TYPE_SHOES, 0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doll_editor, container, false);
        dollContainer = view.findViewById(R.id.dollContainer);

        LinearLayout clothingPanel = view.findViewById(R.id.clothingPanel);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnPrint = view.findViewById(R.id.btnPrint);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);

        // Добавляем базовую куклу
        ImageView baseDoll = new ImageView(requireContext());
        baseDoll.setImageResource(R.drawable.base_doll);
        baseDoll.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        dollContainer.addView(baseDoll);

        // Ожидаем завершения макета для расчета масштаба
        dollContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                dollContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Рассчитываем масштабный коэффициент
                int containerWidth = dollContainer.getWidth();
                int containerHeight = dollContainer.getHeight();

                // Выбираем минимальный коэффициент масштабирования
                float widthScale = (float)containerWidth / BASE_DOLL_WIDTH;
                float heightScale = (float)containerHeight / BASE_DOLL_HEIGHT;
                scaleFactor = Math.min(widthScale, heightScale);

                // Обновляем размеры базовой куклы
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) baseDoll.getLayoutParams();
                params.width = (int)(BASE_DOLL_WIDTH * scaleFactor);
                params.height = (int)(BASE_DOLL_HEIGHT * scaleFactor);
                baseDoll.setLayoutParams(params);

                // Загружаем остальные элементы
                setupClothingPanel(clothingPanel, progressBar);

                if (dollId != null) {
                    loadDollData(progressBar);
                } else {
                    currentDoll = new Doll();
                }
            }
        });

        btnSave.setOnClickListener(v -> saveDoll(progressBar));
        btnPrint.setOnClickListener(v -> generatePrintCode(progressBar));

        return view;
    }

    private void setupClothingPanel(LinearLayout clothingPanel, ProgressBar progressBar) {
        int[] clothingItems = {R.drawable.shirt1, R.drawable.pants1, R.drawable.hat1};
        String[] clothingIds = {"shirt1", "pants1", "hat1"};
        String[] clothingTypes = {TYPE_SHIRT, TYPE_PANTS, TYPE_HAIR};

        for (int i = 0; i < clothingItems.length; i++) {
            ImageView itemView = new ImageView(requireContext());
            itemView.setImageResource(clothingItems[i]);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(150, 150);
            params.setMargins(8, 0, 8, 0);
            itemView.setLayoutParams(params);
            itemView.setContentDescription("Элемент одежды " + clothingIds[i]);

            final int index = i;
            itemView.setOnClickListener(v -> {
                if (clothingCounts.get(clothingTypes[index]) > 0) {
                    removeClothingOfType(clothingTypes[index]);
                }
                addClothingLayer(clothingItems[index], clothingIds[index], clothingTypes[index]);
            });

            clothingPanel.addView(itemView);
        }
    }

    private void removeClothingOfType(String type) {
        for (int i = clothingItems.size() - 1; i >= 0; i--) {
            ClothingItem item = clothingItems.get(i);
            if (item.type.equals(type)) {
                dollContainer.removeView(item.imageView);
                clothingItems.remove(i);
                selectedClothingIds.remove(item.id);
                clothingCounts.put(type, clothingCounts.get(type) - 1);
                break;
            }
        }
    }

    private void addClothingLayer(int resId, String clothingId, String clothingType) {
        ImageView layer = new ImageView(requireContext());

        // Оригинальные параметры позиционирования (до масштабирования)
        int originalWidth, originalHeight, originalX, originalY;

        switch (clothingType) {
            case TYPE_SHIRT:
                originalWidth = 220; originalHeight = 160; originalX = 140; originalY = 190;
                break;
            case TYPE_PANTS:
                originalWidth = 150; originalHeight = 190; originalX = 175; originalY = 320;
                break;
            case TYPE_HAIR:
                originalWidth = 150; originalHeight = 130; originalX = 175; originalY = 67;
                break;
            case TYPE_SHOES:
                originalWidth = 180; originalHeight = 120; originalX = 110; originalY = 550;
                break;
            default:
                originalWidth = 200; originalHeight = 200; originalX = 100; originalY = 100;
        }

        // Применяем масштабирование
        int scaledWidth = (int)(originalWidth * scaleFactor);
        int scaledHeight = (int)(originalHeight * scaleFactor);
        int scaledX = (int)(originalX * scaleFactor);
        int scaledY = (int)(originalY * scaleFactor);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(scaledWidth, scaledHeight);
        params.leftMargin = scaledX;
        params.topMargin = scaledY;
        layer.setLayoutParams(params);
        layer.setImageResource(resId);

        // Анимация появления
        layer.setAlpha(0f);
        dollContainer.addView(layer);
        layer.animate().alpha(1f).setDuration(300).start();

        ClothingItem item = new ClothingItem(layer, clothingId, clothingType,
                originalX, originalY, originalWidth, originalHeight);
        clothingItems.add(item);
        selectedClothingIds.add(clothingId);
        clothingCounts.put(clothingType, 1);

        setupClothingItemInteractions(item);
    }

    private void setupClothingItemInteractions(ClothingItem item) {
        item.imageView.setOnClickListener(v -> {
            removeClothingItem(item);
        });
    }

    private void removeClothingItem(ClothingItem item) {
        // Анимация удаления
        item.imageView.animate()
                .alpha(0f)
                .scaleX(0.5f)
                .scaleY(0.5f)
                .setDuration(200)
                .withEndAction(() -> {
                    dollContainer.removeView(item.imageView);
                    clothingItems.remove(item);
                    selectedClothingIds.remove(item.id);
                    clothingCounts.put(item.type, clothingCounts.get(item.type) - 1);
                })
                .start();
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
        // Очищаем текущие элементы
        for (ClothingItem item : clothingItems) {
            dollContainer.removeView(item.imageView);
        }
        clothingItems.clear();
        selectedClothingIds.clear();

        // Сбрасываем счетчики
        for (String key : clothingCounts.keySet()) {
            clothingCounts.put(key, 0);
        }

        // Восстанавливаем элементы одежды
        for (String clothingId : currentDoll.getClothingLayers()) {
            String clothingType = getClothingTypeById(clothingId);
            Integer resId = getResourceIdForClothing(clothingId);
            if (resId != null && clothingType != null) {
                // Значения по умолчанию (такие же как в addClothingLayer)
                int originalWidth, originalHeight, originalX, originalY;

                switch (clothingType) {
                    case TYPE_SHIRT:
                        originalWidth = 220; originalHeight = 160; originalX = 140; originalY = 190;
                        break;
                    case TYPE_PANTS:
                        originalWidth = 150; originalHeight = 190; originalX = 175; originalY = 320;
                        break;
                    case TYPE_HAIR:
                        originalWidth = 150; originalHeight = 130; originalX = 175; originalY = 67;
                        break;
                    case TYPE_SHOES:
                        originalWidth = 180; originalHeight = 120; originalX = 110; originalY = 550;
                        break;
                    default:
                        originalWidth = 200; originalHeight = 200; originalX = 100; originalY = 100;
                }

                // Если есть сохраненные позиции и размеры, используем их
                if (currentDoll.getClothingPositions() != null &&
                        currentDoll.getClothingPositions().containsKey(clothingId)) {
                    String[] pos = currentDoll.getClothingPositions().get(clothingId).split(",");
                    if (pos.length >= 4) {
                        originalX = Integer.parseInt(pos[0]);
                        originalY = Integer.parseInt(pos[1]);
                        originalWidth = Integer.parseInt(pos[2]);
                        originalHeight = Integer.parseInt(pos[3]);
                    }
                }

                // Применяем масштабирование
                int scaledWidth = (int)(originalWidth * scaleFactor);
                int scaledHeight = (int)(originalHeight * scaleFactor);
                int scaledX = (int)(originalX * scaleFactor);
                int scaledY = (int)(originalY * scaleFactor);

                // Создаем и добавляем элемент одежды
                ImageView layer = new ImageView(requireContext());
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(scaledWidth, scaledHeight);
                params.leftMargin = scaledX;
                params.topMargin = scaledY;
                layer.setLayoutParams(params);
                layer.setImageResource(resId);
                dollContainer.addView(layer);

                ClothingItem item = new ClothingItem(layer, clothingId, clothingType,
                        originalX, originalY, originalWidth, originalHeight);
                clothingItems.add(item);
                selectedClothingIds.add(clothingId);
                clothingCounts.put(clothingType, 1);

                setupClothingItemInteractions(item);
            }
        }
    }

    private String getClothingTypeById(String clothingId) {
        if (clothingId.startsWith("shirt")) return TYPE_SHIRT;
        if (clothingId.startsWith("pants")) return TYPE_PANTS;
        if (clothingId.startsWith("hat")) return TYPE_HAIR;
        if (clothingId.startsWith("shoes")) return TYPE_SHOES;
        return null;
    }

    private Integer getResourceIdForClothing(String clothingId) {
        Map<String, Integer> clothingMap = new HashMap<>();
        clothingMap.put("shirt1", R.drawable.shirt1);
        clothingMap.put("pants1", R.drawable.pants1);
        clothingMap.put("hat1", R.drawable.hat1);
        return clothingMap.get(clothingId);
    }

    private void saveDoll(ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, String> clothingPositions = new HashMap<>();
        for (ClothingItem item : clothingItems) {
            // Сохраняем оригинальные координаты и размеры (до масштабирования)
            clothingPositions.put(item.id,
                    item.originalX + "," + item.originalY + "," +
                            item.originalWidth + "," + item.originalHeight);
        }

        currentDoll.setUserId(userId);
        currentDoll.setBaseDoll("base_doll");
        currentDoll.setClothingLayers(new ArrayList<>(selectedClothingIds));
        currentDoll.setClothingPositions(clothingPositions);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (dollId != null) {
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

        progressBar.setVisibility(View.GONE);
        Toast.makeText(requireContext(),
                "Код печати: " + printCode + "\nСохраните его для будущей печати",
                Toast.LENGTH_LONG).show();
    }
}