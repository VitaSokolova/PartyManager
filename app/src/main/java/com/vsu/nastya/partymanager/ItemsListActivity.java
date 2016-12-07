package com.vsu.nastya.partymanager;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class ItemsListActivity extends AppCompatActivity {

    private ImageButton addItemFAB;

    public static void start(Context context) {
        Intent intent = new Intent(context, ItemsListActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items_list);

        addItemFAB = (ImageButton) findViewById(R.id.add_item_btn);

        addItemFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddItemActivity.start(ItemsListActivity.this);
            }
        });
    }
}
