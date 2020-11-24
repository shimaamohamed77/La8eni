package com.klmni.la8eni.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.klmni.la8eni.R;
import com.klmni.la8eni.model.Contacts;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity
{

    private Toolbar mToolbar;
    private RecyclerView findFriendsRecyclerList;

    private DatabaseReference userReference;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        initializeFields();

        userReference = FirebaseDatabase.getInstance().getReference().child("Users");


    }

    private void initializeFields()
    {
        findFriendsRecyclerList = findViewById(R.id.find_friends_recycler_list);
        findFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(FindFriendsActivity.this , RecyclerView.VERTICAL , false));

        mToolbar = findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find Friends");
    }

    @Override
    protected void onStart()
    {
        super.onStart();


        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions
                .Builder<Contacts>()
                .setQuery(userReference , Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, findFriendViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, findFriendViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull findFriendViewHolder holder, final int position, @NonNull Contacts model)
            {
                holder.userName.setText(model.getName());
                holder.userStatus.setText(model.getStatus());
                Picasso.get()
                        .load(model.getImage())
                        .placeholder(R.drawable.profile_image)
                        .into(holder.profileImage);

                holder.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        String visitUserID = getRef(position).getKey();

                        Intent profileIntent = new Intent(FindFriendsActivity.this , ProfileActivity.class);
                        profileIntent.putExtra("visit_user_id", visitUserID);
                        startActivity(profileIntent);
                    }
                });
            }

            @NonNull
            @Override
            public findFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout , parent , false);
                return new findFriendViewHolder(view);
            }
        };

        findFriendsRecyclerList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class findFriendViewHolder extends RecyclerView.ViewHolder
    {

        TextView userName, userStatus;
        CircleImageView profileImage;

        public findFriendViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);

        }
    }
}