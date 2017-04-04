package com.vsu.nastya.partymanager.messager_list;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.*;
import com.vsu.nastya.partymanager.R;
import com.vsu.nastya.partymanager.logic.User;
import com.vsu.nastya.partymanager.party_details.PartyDetailsActivity;
import com.vsu.nastya.partymanager.party_list.Party;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Vita Sokolova on 01.04.2017.
 */

public class MessageListFragment extends Fragment {
    private static final String TAG = "MessageListFragment";
    private static final String FRIENDLY_MSG_LENGTH_KEY = "2000";
    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    public static final int RC_SIGN_IN = 1;
    private static final int RC_PHOTO_PICKER = 2;

    private RecyclerView messageRecyclerView;
    private MessageAdapter messageAdapter;
    private ProgressBar progressBar;
    private ImageButton photoPickerButton;
    private EditText messageEditText;
    private Button sendButton;

    private String username;

    private FirebaseDatabase fireBaseDatabase;
    private DatabaseReference messagesDatabaseReference;
    private ChildEventListener childEventListner;
    private FirebaseStorage firebaseStorage;
    private StorageReference chatPhotosStorageReference;

    private Party currentParty;

    public static MessageListFragment newInstance() {
        return new MessageListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_list, container, false);

        //получаем информацию о вечеринке от родительской активити
        PartyDetailsActivity activity = (PartyDetailsActivity) getActivity();
        this.currentParty = activity.getCurrentParty();

        username = User.getInstance().getFullName();

        fireBaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        messagesDatabaseReference = fireBaseDatabase.getReference().child("parties").child(currentParty.getKey()).child("messages");

        chatPhotosStorageReference = firebaseStorage.getReference().child(currentParty.getKey()).child("chat_photos");

        // Initialize references to views
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        messageRecyclerView = (RecyclerView) view.findViewById(R.id.messageListView);
        photoPickerButton = (ImageButton) view.findViewById(R.id.photoPickerButton);
        messageEditText = (EditText) view.findViewById(R.id.messageEditText);
        sendButton = (Button) view.findViewById(R.id.sendButton);

        // Initialize message RecyclerView and its adapter
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        messageAdapter = new MessageAdapter(currentParty.getMessagesList());

        this.messageRecyclerView.setLayoutManager(layoutManager);
        this.messageRecyclerView.setItemAnimator(new DefaultItemAnimator());
        this.messageRecyclerView.setAdapter(messageAdapter);

        // Initialize progress bar
        progressBar.setVisibility(ProgressBar.INVISIBLE);

        // ImagePickerButton shows an image picker to upload a image for a message
        photoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });

        // Enable Send button when there's text to send
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    sendButton.setEnabled(true);
                } else {
                    sendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        messageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Send messages on click
                FriendlyMessage friendlyMessage = new FriendlyMessage(messageEditText.getText().toString(), username, null);
                messagesDatabaseReference.push().setValue(friendlyMessage);
                // Clear input box
                messageEditText.setText("");
                ArrayList<FriendlyMessage> messageList = currentParty.getMessagesList();

            }
        });

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        attachDatabaseReadListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        detachDatabaseReadListener();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            // если наш адрес photo/folder/4 этот метод возьмет 4
            StorageReference photoRef = chatPhotosStorageReference.child(selectedImageUri.getLastPathSegment());
            photoRef.putFile(selectedImageUri).addOnSuccessListener(getActivity(),
                    new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri downloadUri = taskSnapshot.getDownloadUrl();
                            FriendlyMessage friendlyMessage = new FriendlyMessage(messageEditText.getText().toString(), username, downloadUri.toString());
                            messagesDatabaseReference.push().setValue(friendlyMessage);
                        }

                    });
        }
    }

    //добавляем слушателя
    private void attachDatabaseReadListener() {
        if (childEventListner == null) {
            childEventListner = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    FriendlyMessage message = dataSnapshot.getValue(FriendlyMessage.class);

                    if ((!currentParty.getMessagesList().contains(message))) {
                        FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
                        currentParty.getMessagesList().add(friendlyMessage);
                        messageAdapter.notifyItemInserted(currentParty.getMessagesList().size() - 1);
                    }
                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                public void onCancelled(DatabaseError databaseError) {
                }
            };
            messagesDatabaseReference.addChildEventListener(childEventListner);
        }
    }

    private void detachDatabaseReadListener() {
        if (childEventListner != null) {
            messagesDatabaseReference.removeEventListener(childEventListner);
            childEventListner = null;
        }
    }
}
