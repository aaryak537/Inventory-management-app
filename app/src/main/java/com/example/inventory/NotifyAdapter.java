package com.example.inventory;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NotifyAdapter extends RecyclerView.Adapter<NotifyAdapter.NotificationViewHolder> {

    private Context context;
    private ArrayList<NotifyModel> notifyList;

    public NotifyAdapter(Context context, ArrayList<NotifyModel> notifyList) {
        this.context = context;
        this.notifyList = notifyList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_notify, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {

        NotifyModel model = notifyList.get(position);



        holder.tvTitle.setText(model.getTitle());
        holder.tvMessage.setText(model.getMessage());
        holder.tvTime.setText(model.getTime());

        if (model.isRead()) {
            holder.cardNotification.setCardBackgroundColor(Color.WHITE);
            holder.viewUnreadDot.setVisibility(View.GONE);
        } else {
            holder.cardNotification.setCardBackgroundColor(Color.parseColor("#EAF4FF"));
            holder.viewUnreadDot.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            model.setRead(true);

            holder.cardNotification.setCardBackgroundColor(Color.WHITE);
            holder.viewUnreadDot.setVisibility(View.GONE);

            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return notifyList == null ? 0 : notifyList.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {

        CardView cardNotification;
        ImageView imgIcon;
        TextView tvTitle;
        TextView tvMessage;
        TextView tvTime;
        View viewUnreadDot;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);

            cardNotification = itemView.findViewById(R.id.cardNotification);
            imgIcon = itemView.findViewById(R.id.imgNotificationIcon);
            tvTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvTime = itemView.findViewById(R.id.tvNotificationTime);
            viewUnreadDot = itemView.findViewById(R.id.viewUnreadDot);
        }
    }
}