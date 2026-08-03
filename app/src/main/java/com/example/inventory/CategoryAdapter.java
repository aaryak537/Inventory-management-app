package com.example.inventory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder>
        implements Filterable {

    private final Context context;
    private final ArrayList<Category> categoryList;
    private final ArrayList<Category> categoryListFull;
    private final OnCategoryActionListener listener;

    public interface OnCategoryActionListener {
        void onEdit(Category category);
        void onDelete(Category category);
    }

    public CategoryAdapter(Context context,
                           ArrayList<Category> categoryList,
                           OnCategoryActionListener listener) {

        this.context = context;
        this.categoryList = categoryList;
        this.listener = listener;
        this.categoryListFull = new ArrayList<>(categoryList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_category, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Category category = categoryList.get(position);

        holder.tvCategoryName.setText(category.getCategoryName());
        holder.tvCategoryDescription.setText(category.getDescription());
        holder.tvCategoryStatus.setText(category.getStatus());

        if ("Active".equalsIgnoreCase(category.getStatus())) {
            holder.tvCategoryStatus.setBackgroundResource(R.drawable.bg_status_active);
        } else {
            holder.tvCategoryStatus.setBackgroundResource(R.drawable.bg_status_inactive);
        }

        holder.imgMenu.setOnClickListener(v -> {

            PopupMenu popupMenu = new PopupMenu(context, holder.imgMenu);
            MenuInflater inflater = popupMenu.getMenuInflater();
           inflater.inflate(R.menu.menu_category, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {

                int id = item.getItemId();

                if (id == R.id.menuEdit) {

                    listener.onEdit(category);
                    return true;

                } else if (id == R.id.menuDelete) {

                    listener.onDelete(category);
                    return true;
               }

                return false;
            });
            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvCategoryName;
        TextView tvCategoryDescription;
        TextView tvCategoryStatus;
        ImageView imgMenu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvCategoryDescription = itemView.findViewById(R.id.tvCategoryDescription);
            tvCategoryStatus = itemView.findViewById(R.id.tvCategoryStatus);
            imgMenu = itemView.findViewById(R.id.imgMenu);
        }
    }

    @Override
    public Filter getFilter() {
        return categoryFilter;
    }

    private final Filter categoryFilter = new Filter() {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            ArrayList<Category> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {

                filteredList.addAll(categoryListFull);

            } else {

                String filterPattern = constraint.toString()
                        .toLowerCase()
                        .trim();

                for (Category item : categoryListFull) {

                    if (item.getCategoryName().toLowerCase().contains(filterPattern)
                            || item.getDescription().toLowerCase().contains(filterPattern)
                            || item.getStatus().toLowerCase().contains(filterPattern)) {

                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {

            categoryList.clear();
            categoryList.addAll((ArrayList<Category>) results.values);
            notifyDataSetChanged();
        }
    };

    public void refreshList(ArrayList<Category> newList) {

        categoryList.clear();
        categoryList.addAll(newList);

        categoryListFull.clear();
        categoryListFull.addAll(newList);

        notifyDataSetChanged();
    }
}