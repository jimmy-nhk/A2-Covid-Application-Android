package com.example.a2.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a2.R;
import com.example.a2.model.User;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class LogInActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    public final static int REGISTER_CODE = 101;

    private EditText emailText;
    private EditText passwordText;
    private TextView errorLoginTxt;

    private static final int GOOGLE_SUCCESSFULLY_SIGN_IN = 1;

    private static final String TAG = "LogInActivity";

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private FirebaseAuth.AuthStateListener authStateListener;
    private GoogleApiClient googleApiClient;
    private List<User> userList;


    String idToken;

    private SignInButton signInGoogleButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        // init services
        attachComponents();
        initService();

        // sign in gg btn
        signInGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Before going into google");
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent,GOOGLE_SUCCESSFULLY_SIGN_IN);
            }
        });


    }
    // attach components with xml
    public void attachComponents(){
        passwordText = findViewById(R.id.passwordTxt);
        emailText = findViewById(R.id.editEmailLogInTxt);
        errorLoginTxt = findViewById(R.id.errorLoginTxt);
        errorLoginTxt.setVisibility(View.INVISIBLE);
        signInGoogleButton = findViewById(R.id.signInWithGoogle);

        TextView textView = (TextView) signInGoogleButton.getChildAt(0);
        textView.setText("Sign in with Google");
    }

    // init services
    public void initService(){

        // init firebase services
        firebaseAuth = FirebaseAuth.getInstance();

        // init realtime db
        firebaseDatabase = FirebaseDatabase.getInstance("https://a2-android-56cbb-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference();

        //this is where we start the Auth state Listener to listen for whether the user is signed in or not
        authStateListener = firebaseAuth -> {
            // Get signedIn user
            FirebaseUser user = firebaseAuth.getCurrentUser();

            //if user is signed in, we call a helper method to save the user details to Firebase
            if (user != null) {
                // User is signed in
                // you could place other firebase code
                //logic to save the user details to Firebase
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged:signed_out");
            }
        };

        userList = new ArrayList<>();

        // load users
        databaseReference.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                GenericTypeIndicator<HashMap<String, User>> genericTypeIndicator =new GenericTypeIndicator<HashMap<String, User>>(){};

                HashMap<String,User> users= snapshot.getValue(genericTypeIndicator);

                try {
                    for (User u : users.values() ){
                        Log.d(TAG, "Value is: " + u.getEmail());
                        userList.add(u);
                    }
                } catch (Exception e){
                    Log.d(TAG, "Cannot load the users");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        GoogleSignInOptions gso =  new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))//you can also use R.string.default_web_client_id
                .requestEmail()
                .build();

        googleApiClient=new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

    }

    // Normal log in
    public void normalLogIn(View view) {

        // validate in case it cannot sign in with authentication
        try {
            //TODO: remember to change back to normal way
            firebaseAuth.signInWithEmailAndPassword(emailText.getText().toString(), passwordText.getText().toString())
//            firebaseAuth.signInWithEmailAndPassword("2@gmail.com" , "123456")
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){

                                // Sign in success, update UI with signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
//                                Toast.makeText(LogInActivity.this, "Authentication success", Toast.LENGTH_SHORT).show();

                                FirebaseUser userFirebase = firebaseAuth.getCurrentUser();

                                User user;
                                Log.d(TAG, userFirebase.getEmail() + " mail1");

                                try {
                                    // get the user from realtime db
                                    user = searchUser(userFirebase.getEmail());
                                    Log.d(TAG, user.getEmail().toString());

                                    // update UI (send intent)
                                    updateUI(user);


                                } catch (Exception e){
                                    Log.d(TAG, "Cannot validate the user in firestone");

                                }

                            }else {

                                // if sign in fails, display a message to the user
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
//                                Toast.makeText(LogInActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        } catch (Exception e){
            errorLoginTxt.setVisibility(View.VISIBLE);
            errorLoginTxt.setText("Please enter your mail and password.");
            return;
        }


    }


    public User searchUser(String mail){


        // validate the user
        for (User u: userList
             ) {

            Log.d(TAG, u.getEmail() + " mail3");
            if (u.getEmail().equals(mail)){
                Log.d(TAG, u.getName() + " name");
                return u;
            }
        }

        return null;
    }


    // move to sign up page
    public void signUpActivity(View view) {

        Intent intent = new Intent(LogInActivity.this, RegisterActivity.class);
        startActivityForResult(intent, REGISTER_CODE);
    }

    // handle sign in with google
    private void handleSignInResult(GoogleSignInResult result){

        // Check if the result is successful
        if(result.isSuccess()){
            // get the account
            GoogleSignInAccount account = result.getSignInAccount();
            idToken = account.getIdToken();


            // you can store user data to SharedPreference
            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
            firebaseAuthWithGoogle(credential);
        }else{
            // Google Sign In failed, update UI appropriately
            Log.e(TAG, "Login Unsuccessful. "+result);
//            Toast.makeText(this, "Login Unsuccessful", Toast.LENGTH_SHORT).show();
        }
    }

    // firebaseAuth with GG
    private void firebaseAuthWithGoogle(AuthCredential credential){

        // sign in with gg
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        if(task.isSuccessful()){
//                            Toast.makeText(LogInActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                            // get the current logged in user
                            FirebaseUser userFirebase = firebaseAuth.getCurrentUser();
                            Log.d(TAG,userFirebase.getDisplayName() + " name" );
                            Log.d(TAG, Objects.requireNonNull(userFirebase).getEmail() + " email");



                            // create the user
                            User user = new User(userFirebase.getDisplayName(), userFirebase.getEmail(), false);

                            databaseReference.child("users").child(user.getName()).setValue(user.toMap());

                            // update the UI
                            updateUI(user);
                        }else{
                            Log.w(TAG, "signInWithCredential" + task.getException().getMessage());
                            task.getException().printStackTrace();
                            errorLoginTxt.setVisibility(View.VISIBLE);
                            errorLoginTxt.setText("Cannot found the account in the system.\nPlease check again the password and mail");
//                            Toast.makeText(LogInActivity.this, "Authentication failed.",
//                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    // update UI
    private void updateUI(User user) {


        Intent intent = new Intent(LogInActivity.this, MapsActivity.class);

        intent.putExtra("user",user );
        setResult(RESULT_OK, intent);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        // check if from register
        if (requestCode == REGISTER_CODE){

            if (resultCode == RESULT_OK){

                Log.d(TAG, userList.size() + " size after loaded");
                emailText.setText(data.getExtras().get("email").toString());

                // display the result alert dialog
                final AlertDialog dialog1 = new AlertDialog.Builder(LogInActivity.this)
                        // validate the result of adding the item to the database
                        .setTitle("Success")
                        .setIcon(R.drawable.thumb_up)
                        .setMessage("You have sign up successfully.")
                        .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                        .create();


                dialog1.show();
                return;
            }
        }

        // check if from google
        if (requestCode == GOOGLE_SUCCESSFULLY_SIGN_IN){

            // The Task returned from this call is always completed, no need to attach
            // a listener.
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);

            return;
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


}