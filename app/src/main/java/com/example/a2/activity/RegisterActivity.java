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
import com.example.a2.helper.CustomInfoWindowAdaptor;
import com.example.a2.model.Site;
import com.example.a2.model.User;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {


    private EditText emailText, usernameText, passwordText, confirmPasswordText;
    private Button signUpBtn;
    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private List<User> userList;

    private TextView errorTxt;

    public static final String USER_COLLECTION = "users";

    public static final String TAG ="RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Init necessary components
        attachComponents();
        initService();
        loadUserData();

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "signUpBtn");

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

    // load the user data
    public void loadUserData(){

        // load users
        databaseReference.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.


                GenericTypeIndicator<HashMap<String, User>> genericTypeIndicator = new GenericTypeIndicator<HashMap<String, User>>() {
                };

                HashMap<String, User> users = snapshot.getValue(genericTypeIndicator);


                try {
                    for (User u : users.values()) {
//                        Log.d(TAG, "Value is: " + u.getEmail());
                        userList.add(u);
                    }


                } catch (Exception e) {
                    Log.d(TAG, "Cannot load the users");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // add to authentication
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
                            User user1 = new User(usernameText.getText().toString(), emailText.getText().toString(), false);

                            databaseReference.child("users").child(user1.getName()).setValue(user1.toMap());

                            Log.d(TAG, "Successfully added new user in Register Activity");

                            updateUI(user1);
                        } else {

                            errorTxt.setVisibility(View.VISIBLE);
                            errorTxt.setText("The gmail format is incorrect.");

                            Log.w(TAG,"createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Create account fail", Toast.LENGTH_SHORT).show();


                        }
                    }
                });
    }

    //update ui
    private void updateUI(User user) {
        Intent intent = new Intent(RegisterActivity.this , LogInActivity.class);
        intent.putExtra("email" , user.getEmail());
        setResult(RESULT_OK , intent);
        finish();
    }

    // validate user name
    private boolean validateUserName(String username){

        for (User user: userList
             ) {
            if (username.equals(user.getName())){
                return false;
            }
        }
        return true;
    }

    // validate user email
    private boolean validateMail(String mail){

        for (User user: userList
        ) {
            if (mail.equals(user.getEmail())){
                return false;
            }
        }
        return true;
    }


    // validate password
    private boolean validatePassword() {

        String password = passwordText.getText().toString();
        String confirmPassword = confirmPasswordText.getText().toString();

        // check validation
        if (!password.equals(confirmPassword)){
            passwordText.setError("The password does not match");
            confirmPasswordText.setError("The password does not match");
            return false;
        }

        // check length
        if (confirmPassword.length() < 6){
            passwordText.setError("The password cannot have less than 6 characters");
            confirmPasswordText.setError("The password cannot have less than 6 characters");
            return false;
        }

        return true;
    }


// init service
    public void initService(){
        // Init firestone
        mAuth = FirebaseAuth.getInstance();

        // init realtime db
        firebaseDatabase = FirebaseDatabase.getInstance("https://a2-android-56cbb-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference();
        userList = new ArrayList<>();


    }

    // attach components
    public void attachComponents(){
        errorTxt = findViewById(R.id.errorTxt);
        errorTxt.setVisibility(View.INVISIBLE);

        emailText = findViewById(R.id.editEmail);
        usernameText = findViewById(R.id.editUserName);
        passwordText = findViewById(R.id.editPassword);
        confirmPasswordText = findViewById(R.id.editConfirmPassword);
        signUpBtn = findViewById(R.id.signUpBtn);
    }


}