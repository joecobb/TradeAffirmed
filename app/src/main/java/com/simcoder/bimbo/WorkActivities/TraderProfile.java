package com.simcoder.bimbo.WorkActivities;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.simcoder.bimbo.Model.Products;
import com.simcoder.bimbo.Model.Users;
import com.simcoder.bimbo.R;
import com.squareup.picasso.Picasso;

public class TraderProfile extends AppCompatActivity  implements  View.OnClickListener{
    private Button ViewBuyers;
    private ImageView productImage;
    private ElegantNumberButton numberButton;
    private TextView productPrice, productDescription, productName;
    private String productID = "", state = "Normal";
    String role;
    String traderID;
    String traderkey;
    String  traderimage;
    String coverimage;
    String traderquote;
    Button traderfollowbutton;

    String traderfollowers;
    String tradername;
    String traderphoneaddress;
    String traderjob;

    Query myproductsdetails;
    DatabaseReference mDatabaseLikeCount;
    DatabaseReference mDatabaseTraderFollowers;

    ImageView traderimageonscreen = (ImageView) findViewById(R.id.traderimageonscreen);
    ImageView tradercoverprofile = (ImageView) findViewById(R.id.tradercoverprofile);
    TextView traderquotes = (TextView) findViewById(R.id.traderquotes);
    TextView traderprofilename = (TextView) findViewById(R.id.traderprofilename);
    TextView traderprofilejob = (TextView) findViewById(R.id.traderprofilejob);
    TextView tradernumberoffollowers = (TextView) findViewById(R.id.tradernumberoffollowers);
    TextView traderprofilephoneadress = (TextView)findViewById(R.id.traderprofilephoneadress);



    String followersID;
    String theProfileID;


    private static final int RC_SIGN_IN = 1;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;


    //AUTHENTICATORS

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    private static final String TAG = "Google Activity";
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    DatabaseReference mDatabaseTrader;
    DatabaseReference mDatabaseReferenceFollowers;
    DatabaseReference LikeRef;
    Boolean mProcessLike;
    String userID;
    boolean increment;
    Button message;
    Button call;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.traderprofile);

        productID = getIntent().getStringExtra("pid");

        ImageView traderimageonscreen = (ImageView) findViewById(R.id.traderimageonscreen);
        ImageView tradercoverprofile = (ImageView) findViewById(R.id.tradercoverprofile);
        TextView traderquotes = (TextView) findViewById(R.id.traderquotes);
        TextView traderprofilename = (TextView) findViewById(R.id.traderprofilename);
        TextView traderprofilejob = (TextView) findViewById(R.id.traderprofilejob);
        TextView tradernumberoffollowers = (TextView) findViewById(R.id.tradernumberoffollowers);
        TextView traderprofilephoneadress = (TextView)findViewById(R.id.traderprofilephoneadress);


        traderfollowbutton = (Button)findViewById(R.id.traderfollowbutton);


       // FRPM HOME ACTIVITY
        if (getIntent() != null) {
            theProfileID = getIntent().getStringExtra("fromhomeactivitytotraderprofile");
        }
        if (getIntent() != null) {
            userID = getIntent().getStringExtra("fromhomeactivitytotraderprofile");
        }


        // FROM ADMINVIEWUSERS HERE
        {
            if (getIntent().getExtras().get("rolefromhometotraderprofilehere") != null) {
                role = getIntent().getExtras().get("rolefromhometotraderprofilehere").toString();
            }
        }

        if (getIntent() != null) {
            theProfileID = getIntent().getStringExtra("fromtraderinhometotraderprofilehere");
        }
        if (getIntent() != null) {
            userID = getIntent().getStringExtra("fromhometotraderprofilehere");
        }




        {
            if (getIntent().getExtras().get("rolefromdrivermaptotraderprofilehere") != null) {
                role = getIntent().getExtras().get("rolefromdrivermaptotraderprofilehere").toString();
            }
        }

        if (getIntent() != null) {
            theProfileID = getIntent().getStringExtra("fromdrivermapttotraderprofilehere");
        }



        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        if (mGoogleApiClient != null) {
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        }

        if (mGoogleApiClient != null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(com.simcoder.bimbo.WorkActivities.TraderProfile.this,
                    new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        }
                    }).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
        }

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    userID = "";
                    userID = user.getUid();
                }

                // I HAVE TO TRY TO GET THE SETUP INFORMATION , IF THEY ARE ALREADY PROVIDED WE TAKE TO THE NEXT STAGE
                // WHICH IS trader TO BE ADDED.
                // PULLING DATABASE REFERENCE IS NULL, WE CHANGE BACK TO THE SETUP PAGE ELSE WE GO STRAIGHT TO MAP PAGE
            }
        };
        getTraderInformation();

        mDatabaseTrader = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers");

        mDatabaseTrader.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {

                    traderkey = dataSnapshot.getKey();
                    traderimage = dataSnapshot.child(theProfileID).child("image").getValue().toString();
                    coverimage = dataSnapshot.child(theProfileID).child("coverimage").getValue().toString();
                    traderquote= dataSnapshot.child(theProfileID).child("quote").getValue().toString();

                    traderfollowers= dataSnapshot.child(theProfileID).child("followers").child("number").getValue().toString();
                    tradername= dataSnapshot.child(theProfileID).child("name").getValue().toString();
                    traderphoneaddress = dataSnapshot.child(theProfileID).child("address").getValue().toString();
                    traderjob= dataSnapshot.child(theProfileID).child("job").getValue().toString(); }




            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });
        // WE HAVE TO ADD ALL PROFILE DETAILS AND PROFILE IMAGES AND COVERS AS WELL

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        followersID="";
        followersID = user.getUid();
        traderfollowbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabaseReferenceFollowers =  FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(theProfileID);
                if (mDatabaseReferenceFollowers != null) {
                    mDatabaseReferenceFollowers.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {

                                if (dataSnapshot.child("followers") != null && dataSnapshot.child("followers").hasChild("number")) {
                                    Log.i("Number of followers", ".");
                                    mDatabaseReferenceFollowers.child("followers").child(followersID).removeValue();
                                    updateCounter(false);
                                    mProcessLike = false;
                                } else {
                                    Log.i("Number of follwowers", "Followers liked");
                                    mDatabaseReferenceFollowers.child("followers").child(followersID).setValue(mAuth.getCurrentUser().getUid());
                                    updateCounter(true);
                                    Log.i(dataSnapshot.getKey(), dataSnapshot.getChildrenCount() + "Count");
                                    mProcessLike = false;
                                }
                            }
                            ;



                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }


            }


            Boolean increments;

            private void updateCounter(final Boolean increment) {
                this.increments = increment;
                DatabaseReference mDatabaseLikeCount;
                mDatabaseLikeCount = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(theProfileID).child("followers").child("number");
                mDatabaseLikeCount.runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        if (mutableData.getValue() != null) {
                            int value = mutableData.getValue(Integer.class);
                            if (increment) {
                                value++;
                            } else {
                                value--;
                            }
                            mutableData.setValue(value);
                        }
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b,
                                           DataSnapshot dataSnapshot) {
                        // Transaction completed
                        Log.d(TAG, "likeTransaction:onComplete:" + databaseError);
                    }
                });
            }


        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        getTraderInformation();
    }


    private void getTraderInformation() {


        mDatabaseReferenceFollowers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Users usershere = dataSnapshot.getValue(Users.class);




                    traderimageonscreen.setImageResource(Integer.parseInt(traderimage));
                    tradercoverprofile.setImageResource(Integer.parseInt(coverimage));


                    traderquotes.setText(traderquote);
                    traderprofilename.setText(tradername);
                    traderprofilejob.setText(traderjob);
                    traderprofilephoneadress.setText(traderphoneaddress);

                    tradernumberoffollowers.setText(traderfollowers);

                    Picasso.get().load(traderimage).into(traderimageonscreen);

                    Picasso.get().load(coverimage).into(tradercoverprofile);





                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onStop() {
        super.onStop();
        //     mProgress.hide();
        if (mAuth != null) {
            mAuth.removeAuthStateListener(firebaseAuthListener);
        }
    }

    @Override
    public void onClick(View v) {

    }
}
