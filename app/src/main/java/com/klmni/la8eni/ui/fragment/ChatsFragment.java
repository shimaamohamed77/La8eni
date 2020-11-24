package com.klmni.la8eni.ui.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.klmni.la8eni.R;
import com.klmni.la8eni.model.Contacts;
import com.klmni.la8eni.ui.activity.ChatActivity;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment
{
    private View privateChatView;

    private RecyclerView chatList;

    private DatabaseReference chatReference, userReference;
    private FirebaseAuth mAuth;

    private String currentUserID;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        privateChatView = inflater.inflate(R.layout.fragment_chats, container, false);
        return privateChatView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        initialView();
        initialDatabase();

    }

    private void initialView()
    {
        chatList = privateChatView.findViewById(R.id.chat_list);
        chatList.setLayoutManager(new LinearLayoutManager(getContext() , RecyclerView.VERTICAL , false));
    }

    private void initialDatabase()
    {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        chatReference = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions
                .Builder<Contacts>()
                .setQuery(chatReference,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, chatViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, chatViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final chatViewHolder holder, final int position, @NonNull Contacts model)
            {
                final String userID = getRef(position).getKey();
                final String[] retImage = {"default_image"};

                userReference.child(userID).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if (snapshot.exists())
                        {
                            if (snapshot.hasChild("image"))
                            {
                                retImage[0] = snapshot.child("image").getValue().toString();
                                String retName = snapshot.child("name").getValue().toString();

                                holder.userName.setText(retName);
                                Picasso.get()
                                        .load(retImage[0])
                                        .placeholder(R.drawable.profile_image)
                                        .into(holder.profileImage);
                            }

                            String retStatus = snapshot.child("status").getValue().toString();
                            final String retName = snapshot.child("name").getValue().toString();

                            holder.userName.setText(retName);
                            holder.userStatus.setText("Last seen:" + "\n" + "Date" + "Time");

                            if (snapshot.child("userState").hasChild("state"))
                            {
                                String date = snapshot.child("userState").child("date").getValue().toString();
                                String time = snapshot.child("userState").child("time").getValue().toString();
                                String state = snapshot.child("userState").child("state").getValue().toString();

                                if (state.equals("online"))
                                {
                                    holder.userStatus.setText("online");
                                }
                                else if (state.equals("offline"))
                                {
                                    holder.userStatus.setText("Last seen: " + date + " " + time);
                                }
                            }
                            else
                            {
                                holder.userStatus.setText("offline");
                            }


                                holder.itemView.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View view)
                                    {

                                        Intent privateChatIntent = new Intent(getContext(), ChatActivity.class);
                                        privateChatIntent.putExtra("Visit_User_ID",userID);
                                        privateChatIntent.putExtra("Visit_User_Name",retName);
                                        privateChatIntent.putExtra("Visit_User_Image", retImage[0]);
                                        startActivity(privateChatIntent);
                                    }
                                });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @NonNull
            @Override
            public chatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout , parent , false);
                return new chatViewHolder(view);
            }
        };

        chatList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class chatViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, userStatus;
        CircleImageView profileImage;

        public chatViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);

        }
    }
}