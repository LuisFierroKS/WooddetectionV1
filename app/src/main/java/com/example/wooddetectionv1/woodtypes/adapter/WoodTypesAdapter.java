package com.example.wooddetectionv1.woodtypes.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wooddetectionv1.R;
import com.example.wooddetectionv1.woodtypes.loader.AssetImageLoader;
import com.example.wooddetectionv1.woodtypes.model.WoodType;

import java.util.ArrayList;
import java.util.List;

public class WoodTypesAdapter extends RecyclerView.Adapter<WoodTypesAdapter.ViewHolder> {

    private final Context context;
    private final List<WoodType> originalList;
    private final List<WoodType> filteredList;

    public WoodTypesAdapter(Context context, List<WoodType> woodList) {
        this.context = context;
        this.originalList = woodList;
        this.filteredList = new ArrayList<>(woodList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wood_type, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WoodType item = filteredList.get(position);
        holder.txtWoodName.setText(item.getName());
        AssetImageLoader.getInstance().loadImage(context, item.getAssetPath(), holder.imgWood);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String text) {
        filteredList.clear();
        if (text.isEmpty()) {
            filteredList.addAll(originalList);
        } else {
            String query = text.toLowerCase().trim();
            for (WoodType item : originalList) {
                if (item.getName().toLowerCase().contains(query)) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgWood;
        TextView txtWoodName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgWood = itemView.findViewById(R.id.imgWood);
            txtWoodName = itemView.findViewById(R.id.txtWoodName);
        }
    }
}
