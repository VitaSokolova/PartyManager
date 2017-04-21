package com.vsu.nastya.partymanager.messager_list;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by vita7 on 19.04.2017.
 */

public class CommonMessageViewHolder extends RecyclerView.ViewHolder {
    public TextView messageTextView, authorTextView;
    public ImageView photoImageView;

    public CommonMessageViewHolder(View itemView) {
        super(itemView);
    }
}
