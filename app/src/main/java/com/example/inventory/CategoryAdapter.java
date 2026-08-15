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

public class CategoryAdapter
        extends RecyclerView.Adapter<CategoryAdapter.ViewHolder>
        implements Filterable {

    private final Context context;
    private final ArrayList<Category> categoryList;
    private final ArrayList<Category> categoryListFull;
    private final OnCategoryActionListener listener;


    // =========================================================
    // INTERFACE
    // =========================================================

    public interface OnCategoryActionListener {

        void onEdit(Category category);

        void onDelete(Category category);
    }


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public CategoryAdapter(
            Context context,
            ArrayList<Category> categoryList,
            OnCategoryActionListener listener) {

        this.context = context;
        this.categoryList = categoryList;
        this.listener = listener;

        this.categoryListFull =
                new ArrayList<>(categoryList);
    }


    // =========================================================
    // CREATE VIEW HOLDER
    // =========================================================

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater.from(context)
                        .inflate(
                                R.layout.item_category,
                                parent,
                                false
                        );

        return new ViewHolder(view);
    }


    // =========================================================
    // BIND VIEW HOLDER
    // =========================================================

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        Category category =
                categoryList.get(position);


        // Category Name

        holder.tvCategoryName.setText(
                category.getCategoryName()
        );


        // Description

        String description =
                category.getDescription();

        if (description == null ||
                description.trim().isEmpty()) {

            holder.tvCategoryDescription
                    .setVisibility(View.GONE);

        } else {

            holder.tvCategoryDescription
                    .setVisibility(View.VISIBLE);

            holder.tvCategoryDescription
                    .setText(description);
        }


        // =====================================================
        // PRODUCT COUNT
        // =====================================================

        int count =
                category.getProductCount();

        if (count == 1) {

            holder.tvProductCount.setText(
                    "1 Product"
            );

        } else {

            holder.tvProductCount.setText(
                    count + " Products"
            );
        }


        // =====================================================
        // STATUS
        // =====================================================

        String status =
                category.getStatus();

        if (status == null) {
            status = "Inactive";
        }

        holder.tvCategoryStatus.setText(
                status
        );


        if ("Active".equalsIgnoreCase(status)) {

            holder.tvCategoryStatus
                    .setBackgroundResource(
                            R.drawable.bg_status_active
                    );

        } else {

            holder.tvCategoryStatus
                    .setBackgroundResource(
                            R.drawable.bg_status_inactive
                    );
        }


        // =====================================================
        // POPUP MENU
        // =====================================================

        holder.imgMenu.setOnClickListener(v -> {

            PopupMenu popupMenu =
                    new PopupMenu(
                            context,
                            holder.imgMenu
                    );

            MenuInflater inflater =
                    popupMenu.getMenuInflater();

            inflater.inflate(
                    R.menu.menu_category,
                    popupMenu.getMenu()
            );


            popupMenu.setOnMenuItemClickListener(
                    item -> {

                        int id =
                                item.getItemId();


                        if (id == R.id.menuEdit) {

                            listener.onEdit(category);

                            return true;
                        }


                        if (id == R.id.menuDelete) {

                            listener.onDelete(category);

                            return true;
                        }


                        return false;
                    }
            );

            popupMenu.show();
        });
    }


    // =========================================================
    // ITEM COUNT
    // =========================================================

    @Override
    public int getItemCount() {

        return categoryList.size();
    }


    // =========================================================
    // VIEW HOLDER
    // =========================================================

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvCategoryName;
        TextView tvCategoryDescription;
        TextView tvProductCount;
        TextView tvCategoryStatus;

        ImageView imgMenu;


        public ViewHolder(
                @NonNull View itemView) {

            super(itemView);


            tvCategoryName =
                    itemView.findViewById(
                            R.id.tvCategoryName
                    );


            tvCategoryDescription =
                    itemView.findViewById(
                            R.id.tvCategoryDescription
                    );


            tvProductCount =
                    itemView.findViewById(
                            R.id.tvProductCount
                    );


            tvCategoryStatus =
                    itemView.findViewById(
                            R.id.tvCategoryStatus
                    );


            imgMenu =
                    itemView.findViewById(
                            R.id.imgMenu
                    );
        }
    }


    // =========================================================
    // SEARCH FILTER
    // =========================================================

    @Override
    public Filter getFilter() {

        return categoryFilter;
    }


    private final Filter categoryFilter =
            new Filter() {

                @Override
                protected FilterResults performFiltering(
                        CharSequence constraint) {

                    ArrayList<Category> filteredList =
                            new ArrayList<>();


                    if (constraint == null ||
                            constraint.length() == 0) {

                        filteredList.addAll(
                                categoryListFull
                        );

                    } else {

                        String filterPattern =
                                constraint
                                        .toString()
                                        .toLowerCase()
                                        .trim();


                        for (Category item :
                                categoryListFull) {

                            String name =
                                    item.getCategoryName();

                            String description =
                                    item.getDescription();

                            String status =
                                    item.getStatus();


                            if (name == null) {
                                name = "";
                            }

                            if (description == null) {
                                description = "";
                            }

                            if (status == null) {
                                status = "";
                            }


                            if (name.toLowerCase()
                                    .contains(filterPattern)

                                    || description
                                    .toLowerCase()
                                    .contains(filterPattern)

                                    || status
                                    .toLowerCase()
                                    .contains(filterPattern)) {

                                filteredList.add(item);
                            }
                        }
                    }


                    FilterResults results =
                            new FilterResults();

                    results.values =
                            filteredList;

                    return results;
                }


                @SuppressWarnings("unchecked")
                @Override
                protected void publishResults(
                        CharSequence constraint,
                        FilterResults results) {

                    categoryList.clear();

                    categoryList.addAll(
                            (ArrayList<Category>)
                                    results.values
                    );

                    notifyDataSetChanged();
                }
            };


    // =========================================================
    // REFRESH LIST
    // =========================================================

    public void refreshList(ArrayList<Category> newList) {

        categoryList.clear();
        categoryList.addAll(newList);

        categoryListFull.clear();
        categoryListFull.addAll(newList);

        notifyDataSetChanged();
    }
}