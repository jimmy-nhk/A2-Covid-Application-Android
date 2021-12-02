package com.example.a2.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.a2.R;
import com.example.a2.controller.FirebaseHelper;
import com.example.a2.helper.CustomInfoWindowAdaptor;
import com.example.a2.model.Site;
import com.example.a2.model.User;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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

//                if (validateGmail()){
//
//                }

                Log.d(TAG, "Here");
                User user = new User(usernameText.getText().toString(), emailText.getText().toString());


                firebaseHelper.addUser(user);
                Log.d(TAG, "Successfully added new user in Register Activity");


                // validate name
                if (!validateUserName(usernameText.getText().toString())){
                    return;
                }

                // validate the password
                if (!validatePassword()){
                    return;
                }

            }
        });



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

//    private ArrayList<User> getAllUsers(){
//
//        ArrayList<User> userArrayList = new ArrayList<>();
//        userCollection.get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//
//                    }
//                });
//
//        return null;
//    }

    private boolean validatePassword() {

        String password = passwordText.getText().toString();
        String confirmPassword = confirmPasswordText.getText().toString();

        if (!password.equals(confirmPassword)){
            passwordText.setError("The password does not match");
            confirmPasswordText.setError("The password does not match");
            return false;
        }

        return true;
    }

//    private boolean validateGmail() {
//
//
//    }

    public void initService(){
        // Init firestone
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        firebaseHelper = new FirebaseHelper(RegisterActivity.this);
    }

    public void attachComponents(){
        emailText = findViewById(R.id.editEmail);
        usernameText = findViewById(R.id.editUserName);
        passwordText = findViewById(R.id.editPassword);
        confirmPasswordText = findViewById(R.id.editConfirmPassword);
        signUpBtn = findViewById(R.id.signUpBtn);
    }



    public void createAccount(View view) {



//        Intent intent = new Intent(RegisterActivity.this, LogInActivity.class);
//        intent.putExtra("email" , emailText.getText() );
//        setResult(RESULT_OK, intent);
//        finish();
    }
}