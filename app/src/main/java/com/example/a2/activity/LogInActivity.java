package com.example.a2.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a2.R;
import com.example.a2.controller.FirebaseHelper;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class LogInActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    public final static int REGISTER_CODE = 101;
    public final static int MAPS_CODE = 201;

    private EditText emailText;
    private EditText passwordText;
    private TextView errorLoginTxt;

    private static final int GOOGLE_SUCCESSFULLY_SIGN_IN = 1;

    private static final String TAG = "LogInActivity";

    private FirebaseAuth firebaseAuth;
    private FirebaseHelper firebaseHelper;

    private FirebaseAuth.AuthStateListener authStateListener;
    private GoogleApiClient googleApiClient;
    private List<User> userList;


    String idToken;

    private SignInButton signInGoogleButton;

    private GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);


        attachComponents();
        initService();




        signInGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Before going into google");
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent,GOOGLE_SUCCESSFULLY_SIGN_IN);
            }
        });


    }

    public void attachComponents(){
        passwordText = findViewById(R.id.passwordTxt);
        emailText = findViewById(R.id.editEmailLogInTxt);
        errorLoginTxt = findViewById(R.id.errorLoginTxt);
        errorLoginTxt.setVisibility(View.INVISIBLE);
        signInGoogleButton = findViewById(R.id.signInWithGoogle);
    }

    public void initService(){

        // init firebase services
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseHelper = new FirebaseHelper(LogInActivity.this);
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

        loadUsersFromDb(new FirebaseHelperCallback() {
            @Override
            public void onDataChanged(List<User> userList) {
                Log.d(TAG, "Successfully added");
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
            firebaseAuth.signInWithEmailAndPassword(emailText.getText().toString(), passwordText.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){

                                // Sign in success, update UI with signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                Toast.makeText(LogInActivity.this, "Authentication success", Toast.LENGTH_SHORT).show();

                                FirebaseUser userFirebase = firebaseAuth.getCurrentUser();

                                User user;
                                Log.d(TAG, userFirebase.getEmail() + " mail1");

                                try {
                                    user = searchUser(userFirebase.getEmail());
                                    Log.d(TAG, user.getEmail().toString());

                                    Intent intent = new Intent(LogInActivity.this, MapsActivity.class);
                                    intent.putExtra("user" , user);
                                    startActivity(intent);

                                } catch (Exception e){
                                    Log.d(TAG, "Cannot validate the user in firestone");

                                }

                            }else {

                                // if sign in fails, display a message to the user
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(LogInActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        } catch (Exception e){
            errorLoginTxt.setVisibility(View.VISIBLE);
            errorLoginTxt.setText("Cannot log in");
            return;
        }


    }


    public User searchUser(String mail){

        Log.d(TAG, userList.get(0).getEmail() + " test email");


        Log.d(TAG, mail + " mail2");

        Log.d(TAG, userList.size() + " size");
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
            Toast.makeText(this, "Login Unsuccessful", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(AuthCredential credential){

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        if(task.isSuccessful()){
                            Toast.makeText(LogInActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                            // get the current logged in user
                            FirebaseUser userFirebase = firebaseAuth.getCurrentUser();
                            Log.d(TAG,userFirebase.getDisplayName() + " name" );
                            Log.d(TAG, Objects.requireNonNull(userFirebase).getEmail() + " email");


                            // if create through gg, the user is super user
                            // create the user
                            User user = new User(userFirebase.getDisplayName(), userFirebase.getEmail(), true);


                            // update the UI
                            updateUI(user);
                        }else{
                            Log.w(TAG, "signInWithCredential" + task.getException().getMessage());
                            task.getException().printStackTrace();
                            errorLoginTxt.setVisibility(View.VISIBLE);
                            errorLoginTxt.setText("Cannot found the account in the system.\nPlease check again the password and mail");
                            Toast.makeText(LogInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    // update UI
    private void updateUI(User user) {

        Intent intent = new Intent(LogInActivity.this, MapsActivity.class);

        intent.putExtra("user",user );
        startActivityForResult(intent, MAPS_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check if from maps
        if (requestCode == MAPS_CODE){

            if (resultCode == RESULT_OK){

                firebaseAuth.signOut();
                Log.d(TAG, "Signout successfully");
            }
        }

        // check if from register
        if (requestCode == REGISTER_CODE){

            if (resultCode == RESULT_OK){

                loadUsersFromDb(new FirebaseHelperCallback() {
                    @Override
                    public void onDataChanged(List<User> userList) {
                        Log.d(LogInActivity.class.getName(), "successfully load db again");
                    }
                });

                Log.d(TAG, userList.size() + " size after loaded");
                emailText.setText(data.getExtras().get("email").toString());
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


//    // This solves the asynchronous problem with fetch data
    public interface FirebaseHelperCallback {
        void onDataChanged(List<User> userList);
    }

    public void loadUsersFromDb(LogInActivity.FirebaseHelperCallback myCallback) {

        try {
            firebaseHelper.getAllUsersForLogin(new LogInActivity.FirebaseHelperCallback() {

                @Override
                public void onDataChanged(List<User> users) {

                    userList = users;
                }
            });
        }catch (Exception e){
            Log.d(TAG, e.getMessage());
        }


    }


}