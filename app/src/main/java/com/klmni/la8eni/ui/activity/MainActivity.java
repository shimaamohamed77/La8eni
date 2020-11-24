package com.klmni.la8eni.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.klmni.la8eni.database.LoginActivity;
import com.klmni.la8eni.R;
import com.klmni.la8eni.view.TabsLayoutAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
{

    private Toolbar mToolbar;
    private TabLayout myTabLayout;
    private ViewPager myViewPager;
    private TabsLayoutAdapter myTabsLayoutAdapter;

    private FirebaseAuth mAuth;
    private DatabaseReference RootReference;

    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialViews();

        initialDatabase();


    }

    private void initialViews()
    {
        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("La8eni");

        myTabLayout = findViewById(R.id.main_tabs);
        myViewPager = findViewById(R.id.main_tabs_pager);
        myTabsLayoutAdapter = new TabsLayoutAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsLayoutAdapter);
        myTabLayout.setupWithViewPager(myViewPager);
    }

    private void initialDatabase()
    {
        mAuth = FirebaseAuth.getInstance();
        RootReference = FirebaseDatabase.getInstance().getReference();
    }

    private void sendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this , LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.main_find_friends_option:
                sendUserToFindFriendsActivity();
                break;
            case R.id.main_create_group_option:
                requestNewGroup();
                break;
            case R.id.main_settings_option:
                sendUserToSettingsActivity();
                break;
            case R.id.main_logout_option:
                updateUserStatus("offline");

                mAuth.signOut();
                sendUserToLoginActivity();
                break;
            default:
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    private void requestNewGroup()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this , R.style.AlertDialog);
        builder.setTitle("Write Group Name");

        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("Ex: The Family");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                String groupName = groupNameField.getText().toString();

                if (TextUtils.isEmpty(groupName))
                {
                    Toast.makeText(MainActivity.this, "Please write group name", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    createNewGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                dialogInterface.cancel();
            }
        });

        builder.show();
    }

    private void createNewGroup(String groupName)
    {
       // String currentGroupsID = mAuth.getCurrentUser().getUid();
        RootReference.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    Toast.makeText(MainActivity.this, "Group name is created Successfully", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendUserToSettingsActivity()
    {
        Intent settingIntent = new Intent(MainActivity.this , SettingsActivity.class);
        startActivity(settingIntent);
    }

    private void sendUserToFindFriendsActivity()
    {
        Intent findFriendIntent = new Intent(MainActivity.this , FindFriendsActivity.class);
       // findFriendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(findFriendIntent);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null)
        {
            sendUserToLoginActivity();
        }
        else
        {
            updateUserStatus("online");
            verifyUserExistance();
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null)
        {
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null)
        {
            updateUserStatus("offline");
        }
    }

    private void verifyUserExistance()
    {
        currentUserID = mAuth.getCurrentUser().getUid();
        RootReference.child("Users").child(currentUserID);
    }

    private void updateUserStatus(String state)
    {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat(" MMM:dd:yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat(" hh-mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("state", state);


        currentUserID = mAuth.getCurrentUser().getUid();

        RootReference.child("Users").child(currentUserID).child("userState").updateChildren(onlineStateMap);
    }
}