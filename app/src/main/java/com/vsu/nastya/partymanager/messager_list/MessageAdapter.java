package com.vsu.nastya.partymanager.messager_list;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.vsu.nastya.partymanager.R;
import com.vsu.nastya.partymanager.logic.User;

import java.util.ArrayList;


public class MessageAdapter extends RecyclerView.Adapter<CommonMessageViewHolder> {
    private ArrayList<FriendlyMessage> messages;

    public MessageAdapter(ArrayList<FriendlyMessage> messages) {
        this.messages = messages;
    }

    @Override
    public CommonMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 1: { //1 - сообщение нашего пользователя
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_message, parent, false);
                return new MyMessageViewHolder(itemView);
            }
            default: {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_others_message, parent, false);
                return new OthersMessageViewHolder(itemView);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getName().equals(User.getInstance().getFullName())) {
            return 1; //1 - сообщение нашего пользователя
        } else {
            return 0;
        }
    }

    @Override
    public void onBindViewHolder(CommonMessageViewHolder holder, int position) {
        FriendlyMessage message = messages.get(position);
        boolean isPhoto = message.getPhotoUrl() != null;
        if (isPhoto) {
            holder.messageTextView.setVisibility(View.VISIBLE);
            holder.photoImageView.setVisibility(View.VISIBLE);
            Glide.with(holder.photoImageView.getContext())
                    .load(message.getPhotoUrl())
                    .into(holder.photoImageView);
            //отобразить месседж
        } else {
            holder.messageTextView.setVisibility(View.VISIBLE);
            holder.photoImageView.setVisibility(View.GONE);
        }
        holder.messageTextView.setText(message.getText());
        holder.authorTextView.setText(message.getName());
        int i =0;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }


    public class OthersMessageViewHolder extends CommonMessageViewHolder {

        public OthersMessageViewHolder(View itemView) {
            super(itemView);
            this.photoImageView = (ImageView) itemView.findViewById(R.id.others_photo_img_view);
            this.messageTextView = (TextView) itemView.findViewById(R.id.others_message_txt);
            this.authorTextView = (TextView) itemView.findViewById(R.id.others_name_txt);
        }
    }

    public class MyMessageViewHolder extends CommonMessageViewHolder {

        public MyMessageViewHolder(View itemView) {
            super(itemView);
            this.photoImageView = (ImageView) itemView.findViewById(R.id.my_photo_img_view);
            this.messageTextView = (TextView) itemView.findViewById(R.id.my_message_txt);
            this.authorTextView = (TextView) itemView.findViewById(R.id.my_name_txt);

        }

    }
}
