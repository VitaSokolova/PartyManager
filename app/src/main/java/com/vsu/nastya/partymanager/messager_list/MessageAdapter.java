package com.vsu.nastya.partymanager.messager_list;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.vsu.nastya.partymanager.R;
import com.vsu.nastya.partymanager.logic.User;

import java.util.ArrayList;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {
    private ArrayList<FriendlyMessage> messages;

    public MessageAdapter(ArrayList<FriendlyMessage> messages) {
        this.messages = messages;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        FriendlyMessage message = messages.get(position);
        boolean isPhoto = message.getPhotoUrl() != null;
        //если сообщение от нашего пользователя
        if (message.getName().equals(User.getInstance().getFullName())) {
            RelativeLayout.LayoutParams paramsAuthor = (RelativeLayout.LayoutParams) holder.authorTextView.getLayoutParams();
            paramsAuthor.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            holder.authorTextView.setLayoutParams(paramsAuthor);

            RelativeLayout.LayoutParams paramsMsg = (RelativeLayout.LayoutParams) holder.messageTextView.getLayoutParams();
            paramsMsg.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            holder.messageTextView.setLayoutParams(paramsMsg);

            RelativeLayout.LayoutParams paramsPhoto = (RelativeLayout.LayoutParams) holder.photoImageView.getLayoutParams();
            paramsPhoto.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            holder.photoImageView.setLayoutParams(paramsPhoto);


        }
        if (isPhoto) {
            holder.messageTextView.setVisibility(View.VISIBLE);
            holder.photoImageView.setVisibility(View.VISIBLE);
            Glide.with(holder.photoImageView.getContext())
                    .load(message.getPhotoUrl())
                    .into(holder.photoImageView);
        } else {
            holder.messageTextView.setVisibility(View.VISIBLE);
            holder.photoImageView.setVisibility(View.GONE);
            holder.messageTextView.setText(message.getText());
        }
        holder.authorTextView.setText(message.getName());


    }

    @Override
    public int getItemCount() {
        return messages.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextView, authorTextView;
        public ImageView photoImageView;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.photoImageView = (ImageView) itemView.findViewById(R.id.photoImageView);
            this.messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            this.authorTextView = (TextView) itemView.findViewById(R.id.nameTextView);
        }

    }


}
