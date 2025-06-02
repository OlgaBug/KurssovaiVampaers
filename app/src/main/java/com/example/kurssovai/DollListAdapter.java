package com.example.kurssovai;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.kurssovai.R;
import com.example.kurssovai.Doll;

import java.util.List;

public class DollListAdapter extends ArrayAdapter<Doll> {
    private final Context context;
    private final List<Doll> dolls;

    public DollListAdapter(Context context, List<Doll> dolls) {
        super(context, R.layout.item_doll, dolls);
        this.context = context;
        this.dolls = dolls;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_doll, parent, false);
        }

        Doll doll = dolls.get(position);

        ImageView dollImage = convertView.findViewById(R.id.ivDoll);
        TextView dollName = convertView.findViewById(R.id.tvDollName);
        TextView printCode = convertView.findViewById(R.id.tvPrintCode);

        // Здесь должна быть загрузка изображения куклы (например, из Firebase Storage)
        // Вместо этого используем временное изображение
        Glide.with(context)
                .load(R.drawable.base_doll)
                .into(dollImage);

        dollName.setText("Кукла #" + (position + 1));

        if (doll.getPrintCode() != null && !doll.getPrintCode().isEmpty()) {
            printCode.setText("Код печати: " + doll.getPrintCode());
            printCode.setVisibility(View.VISIBLE);
        } else {
            printCode.setVisibility(View.GONE);
        }

        return convertView;
    }
}