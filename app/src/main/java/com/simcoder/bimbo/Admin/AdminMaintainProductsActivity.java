package  com.simcoder.bimbo.Admin;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import  com.simcoder.bimbo.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;




public class AdminMaintainProductsActivity extends AppCompatActivity {
    private Button applyChangesBtn, deleteBtn;
    private EditText name, price, description;
    private ImageView imageView;

    private String productID = "";
    private DatabaseReference productsRef;
    String role;
    FirebaseUser user;
    DatabaseReference productTraderReference;
    HashMap<String, Object> traderMap;
    HashMap<String, Object> productMap;
    //AUTHENITICATORS
    private static final int RC_SIGN_IN = 1;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    String traderID;

    //AUTHENTICATORS

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    private static final String TAG = "Google Activity";
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_maintain_products);

        // WE GET THE PRODUCT ID FROM PREVIOUS ACTIVITIES
        productID = getIntent().getStringExtra("pid");
        productsRef = FirebaseDatabase.getInstance().getReference().child("Product").child(productID);
        productTraderReference = FirebaseDatabase.getInstance().getReference().child("Product").child(productID).child("trader");

        applyChangesBtn = findViewById(R.id.apply_changes_btn);
        name = findViewById(R.id.product_name_maintain);
        price = findViewById(R.id.product_price_maintain);
        description = findViewById(R.id.product_description_maintain);
        imageView = findViewById(R.id.product_image_maintain);
        deleteBtn = findViewById(R.id.delete_product_btn);


        // THE AUTHENTICATORS

        //AUTHENTICATORS
        FirebaseAuth.getInstance();
        mAuth = FirebaseAuth.getInstance();


        // KEYS PASSED IN FROM ADMINCATEGORY

        { if (getIntent().getExtras().get("maintainrolefromadmincategory") != null) {
            role = getIntent().getExtras().get("maintainrolefromadmincategory").toString();     } }

        if (getIntent() != null) {
            traderID = getIntent().getStringExtra("maintainfromadmincategoryactivity");
        }
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        if (mGoogleApiClient != null) {
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        }

        if (mGoogleApiClient != null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(AdminMaintainProductsActivity.this,
                    new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        }
                    }).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
        }

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                 user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    traderID = "";
                    traderID = user.getUid();
                }

                // I HAVE TO TRY TO GET THE SETUP INFORMATION , IF THEY ARE ALREADY PROVIDED WE TAKE TO THE NEXT STAGE
                // WHICH IS CUSTOMER TO BE ADDED.
                // PULLING DATABASE REFERENCE IS NULL, WE CHANGE BACK TO THE SETUP PAGE ELSE WE GO STRAIGHT TO MAP PAGE
            }
        };


        displaySpecificProductInfo();


        if (applyChangesBtn != null) {
            applyChangesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    applyChanges();
                }
            });
        }
        if (deleteBtn != null) {
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteThisProduct();
                }
            });
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        //     mProgress.hide();
        if (mAuth != null) {
            mAuth.removeAuthStateListener(firebaseAuthListener);
        }
    }


    private void deleteThisProduct() {
        if (productsRef != null) {
            productsRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Intent intent = new Intent(AdminMaintainProductsActivity.this, AdminCategoryActivity.class);
                    startActivity(intent);
                    finish();

                    Toast.makeText(AdminMaintainProductsActivity.this, "The Product Is deleted successfully.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void applyChanges() {
        if (name != null) {
            String pName = name.getText().toString();

            if (price != null) {
                String pPrice = price.getText().toString();

                if (description != null) {
                    String pDescription = description.getText().toString();

                    if (pName.equals("")) {
                        Toast.makeText(this, "Write down Product Name.", Toast.LENGTH_SHORT).show();
                    } else if (pPrice.equals("")) {
                        Toast.makeText(this, "Write down Product Price.", Toast.LENGTH_SHORT).show();
                    } else if (pDescription.equals("")) {
                        Toast.makeText(this, "Write down Product Description.", Toast.LENGTH_SHORT).show();
                    } else {
                        HashMap<String, Object> productMap = new HashMap<>();
                        traderMap = new HashMap<>();
                        productMap.put("pid", productID);
                        productMap.put("desc", pDescription);
                        productMap.put("price", pPrice);
                        productMap.put("name", pName);



                        traderMap.put("tid", traderID);
                        traderMap.put("image", traderID);
                        traderMap.put("name", traderID);

                        if (productsRef != null) {
                            productsRef.updateChildren(productMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        productTraderReference.child(traderID).updateChildren(traderMap);


                                        Toast.makeText(AdminMaintainProductsActivity.this, "Changes applied successfully.", Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(AdminMaintainProductsActivity.this, AdminCategoryActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                        }
                    }

                }
            }
        }
    }

    private void displaySpecificProductInfo() {
        if (productsRef != null) {
            productsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String pName = dataSnapshot.child("pname").getValue().toString();
                        String pPrice = dataSnapshot.child("price").getValue().toString();
                        String pDescription = dataSnapshot.child("description").getValue().toString();
                        String pImage = dataSnapshot.child("image").getValue().toString();


                              if (name != null) {
                                  name.setText(pName);
                              }
                              if (price != null) {
                                  price.setText(pPrice);
                              }
                              if (description != null) {
                                  description.setText(pDescription);
                              };
                              if (Picasso.get() != null) {
                                  Picasso.get().load(pImage).into(imageView);

                              }}
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}