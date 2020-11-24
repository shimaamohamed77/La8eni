package com.klmni.la8eni.database;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.klmni.la8eni.R;
import com.klmni.la8eni.ui.activity.MainActivity;

public class RegisterActivity extends AppCompatActivity
{
    private EditText UserEmail, UserPassword;
    private  Button CreateAccountButton;
    private TextView AlreadyHaveAccountLink;

    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference RootReference;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);



        initializeViews();

        initializeDatabase();

        clickedViews();


    }

    private void CreateNewAccount()
    {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "Please write your email", Toast.LENGTH_SHORT).show();
            UserEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Please write your password", Toast.LENGTH_SHORT).show();
            UserPassword.requestFocus();
            return;
        }
        else
        {
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please Wait, while we are creating new account for you ..");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>()
            {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if (task.isSuccessful())
                    {
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();

                        String currentUserID = mAuth.getCurrentUser().getUid();
                        RootReference.child("Users").child(currentUserID).setValue("");

                        RootReference
                                .child("Users")
                                .child(currentUserID)
                                .child("device_token")
                                .setValue(deviceToken);

                        sendUserToMainActivity();
                        Toast.makeText(RegisterActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                    else
                    {
                        Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }

    private void initializeViews()
    {
        UserEmail = findViewById(R.id.register_email);
        UserPassword = findViewById(R.id.register_password);
        CreateAccountButton = findViewById(R.id.register_button);
        AlreadyHaveAccountLink = findViewById(R.id.already_have_account_link);
        loadingBar = new ProgressDialog(RegisterActivity.this);
    }

    private void initializeDatabase()
    {
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        RootReference = FirebaseDatabase.getInstance().getReference();
    }

    private void clickedViews()
    {
        AlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                sendUserToLoginActivity();
            }
        });

        CreateAccountButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                CreateNewAccount();
            }
        });
    }

    private void sendUserToMainActivity()
    {
        Intent loginIntent = new Intent(RegisterActivity.this , MainActivity.class);
        startActivity(loginIntent);
    }

    private void sendUserToLoginActivity()
    {
        Intent mainIntent = new Intent(RegisterActivity.this , LoginActivity.class);
       // mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}