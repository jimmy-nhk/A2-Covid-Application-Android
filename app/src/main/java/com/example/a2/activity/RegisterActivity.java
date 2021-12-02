package com.example.a2.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a2.R;
import com.example.a2.controller.FirebaseHelper;
import com.example.a2.helper.CustomInfoWindowAdaptor;
import com.example.a2.model.Site;
import com.example.a2.model.User;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {


    private EditText emailText, usernameText, passwordText, confirmPasswordText;
    private Button signUpBtn;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseHelper firebaseHelper;
    private List<User> users;

    private TextView errorTxt;
    private CheckBox isSuperUser;

    public static final String USER_COLLECTION = "users";

    public static final String TAG ="RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Init necessary components
        attachComponents();
        initService();

        // load all the current users in the db to validate
        loadUsersFromDb(new FirebaseHelperCallback() {
            @Override
            public void onDataChanged(List<User> userList) {
                Log.d(TAG, userList.toString());
            }
        });



        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Log.d(TAG, "Here");



                // validate name
                if (!validateUserName(usernameText.getText().toString())){
                    usernameText.setError("This username is already existed");
                    Log.d(TAG, "username already exists");
                    return;
                }

                // validate mail
                if (!validateMail(emailText.getText().toString())){
                    emailText.setError("This email is already existed");
                    Log.d(TAG, "email already exists");
                    return;
                }

                // validate the password
                if (!validatePassword()){
                    Log.d(TAG, "Password does not match or less than 6 characters ");
                    return;
                }

                addUserToAuthentication(emailText.getText().toString(),  confirmPasswordText.getText().toString());

            }
        });

    }

    public void addUserToAuthentication(String mail, String password){

        mAuth.createUserWithEmailAndPassword(mail, password)
                .addOnCompleteListener(this,new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){
                            // Sign in success, update UI
                            Log.d(TAG,"createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(RegisterActivity.this, "Successfully created", Toast.LENGTH_SHORT).show();


                            // create user
                            User user1 = new User(usernameText.getText().toString(), emailText.getText().toString(), isSuperUser.isChecked());

                            firebaseHelper.addUser(user1);
                            Log.d(TAG, "Successfully added new user in Register Activity");

                            updateUI(user1);
                        } else {

                            errorTxt.setVisibility(View.INVISIBLE);
                            errorTxt.setText("The account cannot be added. Please try again");

                            Log.w(TAG,"createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Create account fail", Toast.LENGTH_SHORT).show();


                        }
                    }
                });
    }

    private void updateUI(User user) {
        Intent intent = new Intent(RegisterActivity.this , LogInActivity.class);
        intent.putExtra("email" , user.getEmail());
        setResult(RESULT_OK , intent);
        finish();
    }

    // validate user name
    private boolean validateUserName(String username){

        for (User user: users
             ) {
            if (username.equals(user.getName())){
                return false;
            }
        }
        return true;
    }

    // validate user email
    private boolean validateMail(String mail){

        for (User user: users
        ) {
            if (mail.equals(user.getEmail())){
                return false;
            }
        }
        return true;
    }


    // This solves the asynchronous problem with fetch data
    public interface FirebaseHelperCallback {
        void onDataChanged(List<User> siteList);
    }

    public void loadUsersFromDb(RegisterActivity.FirebaseHelperCallback myCallback) {

        firebaseHelper.getAllUsers(new RegisterActivity.FirebaseHelperCallback() {

            @Override
            public void onDataChanged(List<User> userList) {

                users = userList;
            }
        });
    }

    // validate password
    private boolean validatePassword() {

        String password = passwordText.getText().toString();
        String confirmPassword = confirmPasswordText.getText().toString();

        if (!password.equals(confirmPassword)){
            passwordText.setError("The password does not match");
            confirmPasswordText.setError("The password does not match");
            return false;
        }

        if (confirmPassword.length() < 6){
            passwordText.setError("The password cannot have less than 6 characters");
            confirmPasswordText.setError("The password cannot have less than 6 characters");
            return false;
        }

        return true;
    }


    public void initService(){
        // Init firestone
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        firebaseHelper = new FirebaseHelper(RegisterActivity.this);
    }

    public void attachComponents(){
        errorTxt = findViewById(R.id.errorTxt);
        errorTxt.setVisibility(View.INVISIBLE);

        emailText = findViewById(R.id.editEmail);
        usernameText = findViewById(R.id.editUserName);
        passwordText = findViewById(R.id.editPassword);
        confirmPasswordText = findViewById(R.id.editConfirmPassword);
        signUpBtn = findViewById(R.id.signUpBtn);
        isSuperUser = findViewById(R.id.isSuperUser);
    }


}