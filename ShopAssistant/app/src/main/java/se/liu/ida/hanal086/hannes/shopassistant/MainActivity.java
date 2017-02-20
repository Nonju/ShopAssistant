package se.liu.ida.hanal086.hannes.shopassistant;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.database.ValueEventListener;

import MainPage.Entities.UserData;
import MainPage.LocationModule.Enums.CompassState;
import General.Exceptions.StringEmptyException;
import MainPage.LocationModule.Entities.Coordinate;
import MainPage.LocationModule.Statics.ImageHandler;
import MainPage.LocationModule.UserLocationListener;


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        SensorEventListener,
        View.OnClickListener {

    // Activity global values
    private String TAG = "----MAIN----";
    private String ERROR_TAG = "----ERROR----";
    private int LOGIN_REQUEST_CODE = 4711;
    private int ACTIVITY_REQUEST_CODE = 8888;

    // Enumerations
    CompassState compassState;

    // Login Layouts
    private RelativeLayout loginLayout;
    private RelativeLayout loggedInLayout;

    // Compass Layouts
    private RelativeLayout compassLayout;
    private TextView compassNotActivatedTextView;
    private ImageView compassNeedleImageView;

    // Google Auth Components
    private GoogleApiClient mGoogleApiClient;
    private TextView loggedInUser;

    // Firebase User
    FirebaseUser currentUser;

    // Firebase Auth Components
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    // Database variables
    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

    // Location module components
    private UserLocationListener userLocListener;
    private LocationRequest mLocationRequest;
    private Location userLastLocation;
    private float locationRawAngle;
    private final int LOCATION_UPDATE_INTERVAL = 2000;
    private final int LOCATION_FASTEST_UPDATE_INTERVAL = 1000;
    private final float LOCATION_SMALLEST_DISPLACEMENT = 0.5F;

    // PhoneSensor Components
    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float rotationFromNorth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connect Login-layouts
        loginLayout = (RelativeLayout) findViewById(R.id.main_loginLayout);
        loggedInLayout = (RelativeLayout) findViewById(R.id.main_loggedInLayout);

        // Connect Compass-layouts / Values
        compassNotActivatedTextView = (TextView) findViewById(R.id.main_compassNotActivated);
        compassLayout = (RelativeLayout) findViewById(R.id.main_compassLayout);
        compassLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleCompassState();
            }
        });
        compassState = CompassState.OFF; // default state
        compassNeedleImageView = (ImageView) findViewById(R.id.main_compassNeedleImage);

        // Set up Google authentication
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.firebase_clientID))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /*FragmentActivity*/, this /*OnConnectionFailedListener*/)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Set up Firebase authentication
        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) Log.d(TAG, "signed in user: " + user.getUid() + "!");
                else Log.d(TAG, "signed out!");
            }
        };

        // Handle login
        findViewById(R.id.main_signInButton).setOnClickListener(this);

        // Handle logout
        findViewById(R.id.main_signOutButton).setOnClickListener(this);


        // Textview for currently logged in user
        loggedInUser = (TextView) findViewById(R.id.main_currentLoggedInUser);

        // SavePosition Button
        Button savePositionButton = (Button) findViewById(R.id.main_savePosButton);
        savePositionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCurrentPosButtonClicked();
            }
        });

        // ShoppingList Button
        Button shoppingListButton = (Button) findViewById(R.id.main_shopListButton);
        shoppingListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToShoppingList();
            }
        });

        // Settings Button
        Button settingsButton = (Button) findViewById(R.id.main_settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSettings();
            }
        });

        // About Button
        Button aboutButton = (Button) findViewById(R.id.main_aboutButton);
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAbout();
            }
        });

        // Setup for location modules
        userLocListener = new UserLocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                handleLocationChange(location);
            }
        };
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(LOCATION_UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_FASTEST_UPDATE_INTERVAL);
        mLocationRequest.setSmallestDisplacement(LOCATION_SMALLEST_DISPLACEMENT);
        locationRawAngle = 0f;
        updateUserLastLocation();

        // Initialize Android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        rotationFromNorth = 0f;


        Log.d("TAG", "after onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Set authStateListener to mAuth
        mAuth.addAuthStateListener(mAuthStateListener);

        // Reload current logged in user (If any)
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Toggle correct screen depending on whether or not user is logged in at start
        if (currentUser != null) {
            toggleLoggedInScreen();
            loggedInUser.setText(currentUser.getDisplayName());
        } else toggleLoginScreen();


        // Set default compassState and see if can find any old set dest-locations
        updateCurrentUserCompassState();
        updateCurrentUserCompass();

    }

    @Override
    public void onClick(View v) { // Overridden onClick used for logging user In/Out
        switch (v.getId()) {
            case R.id.main_signInButton:
                signInUser();
                break;
            case R.id.main_signOutButton:
                signOutUser();
                break;
        }
    }


    private void updateCurrentUserCompassState() {
        Log.d(TAG, "UpdateCurrentUserCompassState!");
        // Update compassState
        DatabaseReference userCompassStateRef;
        try {
            userCompassStateRef = dbRef.child("users").child(currentUser.getUid()).child("compassState");
        } catch (Exception e) {
            Log.d(ERROR_TAG, "CurrentUser not yet set: " + e.getMessage());
            return;
        }
        userCompassStateRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Retrieve state-value stored in database
                String cStateValue = dataSnapshot.getValue(String.class);
                Log.d(TAG, "CSTATEVALUE: " + cStateValue);
                if (cStateValue.equals("ON")) {
                    compassState = CompassState.ON;
                    compassNotActivatedTextView.setVisibility(View.INVISIBLE);
                    startLocationUpdates();
                }
                else {
                    compassState = CompassState.OFF;
                    compassNotActivatedTextView.setVisibility(View.VISIBLE);
                    stopLocationUpdates();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(ERROR_TAG, databaseError.getMessage());
            }
        });
    }

    private void updateCurrentUserCompass() {
        Log.d(TAG, "UpdateCurrentUserCompass!");
        // Update dest-location
        DatabaseReference userPosRef;
        try {
            userPosRef = dbRef.child("users").child(currentUser.getUid()).child("position");
        } catch (Exception e) {
            Log.d(ERROR_TAG, "Could not retrieve user location from database --> returning");
            Log.d(ERROR_TAG, e.getMessage());
            return;
        }
        userPosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Compass OnDataChange");
                String latSTR = dataSnapshot.child("latitude").getValue(Double.class).toString();
                String longSTR = dataSnapshot.child("longitude").getValue(Double.class).toString();
                Log.d(TAG, "LAT : " + latSTR);
                Log.d(TAG, "LONG: " + longSTR);
                double latitude = dataSnapshot.child("latitude").getValue(Double.class);
                double longitude = dataSnapshot.child("longitude").getValue(Double.class);
                userLocListener.setDestCoord(new Coordinate(longitude, latitude));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void handleLocationChange(Location location) {
        Log.d(TAG, "HandleLocationChange!");

        // Update userLocation then the compassNeedles angle
        userLastLocation = location;
        updateCompassAngle();
    }

    float[] mGravity;
    float[] mGeomagnetic;
    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) mGeomagnetic = event.values;

        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                //Log.d(TAG, "OnSensorChanged!");
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                // Get Azimut
                //rotationFromNorth = -orientation[0]; // orientation contains: azimut, pitch and roll
                rotationFromNorth = (float) Math.toDegrees(Double.valueOf(String.valueOf(orientation[0]))); // ....
                // Update compassNeedles angle
                updateCompassAngle();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    private void updateCompassAngle() {

        // Get current angle towards destination
        double rotationAngle = angleFromCoordinate(
                userLastLocation.getLatitude(),
                userLastLocation.getLongitude(),
                userLocListener.getDestCoord().getLatitude(),
                userLocListener.getDestCoord().getLongitude()
        );

        // Correct angle based on phone rotation from north
        rotationAngle -= rotationFromNorth;

        // Apply rotation on imageView
        ImageHandler.rotateImage(compassNeedleImageView, rotationAngle);

    }

    private double angleFromCoordinate(double lat1, double long1, double lat2, double long2) {
        //Log.d(TAG, "AngleFromCoordinate!");

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;
        brng = 360 - brng;

        return brng;
    }

    // Window toggle-functions
    private void toggleLoginScreen() {
        loginLayout.setVisibility(View.VISIBLE);
        loggedInLayout.setVisibility(View.GONE);
        compassState = CompassState.OFF; // Default value when logging in
        updateCurrentUserCompassState();
    }

    private void toggleLoggedInScreen() {
        loginLayout.setVisibility(View.GONE);
        loggedInLayout.setVisibility(View.VISIBLE);
    }

    private void toggleCompassState() {
        // Toggle values and visibilities based on current compassState
        if (compassState == CompassState.OFF) {
            compassState = CompassState.ON;
            updateCurrentUserCompassState();
        }
        else /*if (compassState == CompassState.ON)*/ {
            compassState = CompassState.OFF;
            updateCurrentUserCompassState();
        }

        // Store result in table "position"
        DatabaseReference userCompassStateRef = dbRef.child("users")
                .child(currentUser.getUid()).child("compassState");
        try { userCompassStateRef.setValue(compassState.toString()); }
        catch (Exception e) { // Make sure table exists
            Log.d(ERROR_TAG, e.getMessage());
            userCompassStateRef.setValue("placeholder");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();

    }

    @Override
    protected void onResume() {
        super.onPostResume();
        if (mGoogleApiClient.isConnected() && compassState == CompassState.OFF) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Remove listener on stop
        if (mAuthStateListener != null) mAuth.removeAuthStateListener(mAuthStateListener);
    }

    private void signInUser() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(intent, LOGIN_REQUEST_CODE);
    }

    private void signOutUser() {
        //Firebase signout
        mAuth.signOut();

        // Google signout
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                loggedInUser.setText(R.string.main_currentLoggedInUserDefaultText);
            }
        });

        toggleLoginScreen(); // Toggle to login-screen
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "RESULT_CODE:" + RESULT_CANCELED);

        // Check if login worked properly
        if (requestCode == LOGIN_REQUEST_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d(TAG, "RESULT: " + result.isSuccess());

            //TAG TEST
            if (resultCode == RESULT_OK) Log.d(TAG, "RESULT_OK!");
            else if (resultCode == RESULT_CANCELED) Log.d(TAG, "RESULT_CANCELLED!");

            if (result.isSuccess()) {
                // User managed to log in --> show username
                GoogleSignInAccount account = result.getSignInAccount();
                loggedInUser.setText(account.getDisplayName());
                // Handle Firebase authentication
                Log.d(TAG, "NAME SET");
                firebaseAuthWithGoogle(account);
            }
        }

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseWithGoogle: " + account.getId());

        // Get credential
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        Log.d(TAG, "GOT CREDENTIAL");
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "ONCOMPLETE!");

                        // If authentication passes --> assign current user
                        if (task.isSuccessful()) {
                            currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            Log.d(TAG, "-UID-: " + currentUser.getUid());
                            addUserIfNotInDatabase();
                            toggleLoggedInScreen(); // Toggle to logged-in-screen
                            updateCurrentUserCompassState(); // Get current users compassState

                            // Begin requesting locationUpdates from provider
                            //startLocationUpdates(); // already does this in function above
                        }
                        // else if task isn't successfull --> Tell user
                        else if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Authentication failed!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void addUserIfNotInDatabase() {
        Log.d(TAG, "AddUserIfNotIdDatabase");

        // Get reference to "users" table
        final DatabaseReference userRef = dbRef.child("users"); // Get "users" subtree
        // Make checks if current user is stored in database or not
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange!!!");

                // User doesn't exist --> add it to db
                if (!dataSnapshot.hasChild(currentUser.getUid())) {
                    // Data about new user
                    try {
                        Log.d(TAG, "Child didn't exist --> Creating new user!");

                        UserData data = new UserData(currentUser.getUid(), currentUser.getDisplayName());
                        // Append base structure of user to database
                        userRef.child(currentUser.getUid()).setValue(data);

                    } catch (StringEmptyException e) {
                        Log.d(ERROR_TAG, e.getMessage());
                    }
                } else Log.d(TAG, "Child existed --> Returning without creating new user!");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

    }

    private void saveCurrentPosButtonClicked() {
        Log.d(TAG, "SaveCurrentPosButtonClicked");
        Toast.makeText(this, "Saving position!", Toast.LENGTH_SHORT).show();


        // Get current location
        updateUserLastLocation();

        // Get reference to positions-table
        DatabaseReference userPosRef = dbRef.child("users").child(currentUser.getUid()).child("position");

        // Insert coordinate for later use in database
        userPosRef.child("longitude").setValue(userLastLocation.getLongitude());
        userPosRef.child("latitude").setValue(userLastLocation.getLatitude());

        // Update locationListener with new position
        userLocListener.setDestCoord(
                new Coordinate(userLastLocation.getLongitude(),
                        userLastLocation.getLatitude())
        );

    }
    private void updateUserLastLocation() {
        Log.d(TAG, "UpdateUserLastLocation!");

        // Check for needed location permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permissions to ask user for
            String[] permissionsToAskFor = new String[] {
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
            };
            // Ask user
            ActivityCompat.requestPermissions(this, permissionsToAskFor, 9999);
        }

        // Get current location
        userLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (userLastLocation == null) { // If can't find lastLocation --> initiate with default values
            userLastLocation = new Location("");
            userLastLocation.setLatitude(0.0d);
            userLastLocation.setLongitude(0.0d);
        }
        Log.d(TAG, "USER LONGITUDE: " + userLastLocation.getLongitude());
        Log.d(TAG, "USER LATITUDE : " + userLastLocation.getLatitude());
    }

    private void startLocationUpdates() {
        // Check for needed location permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permissions to ask user for
            String[] permissionsToAskFor = new String[] {
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
            };
            // Ask user
            ActivityCompat.requestPermissions(this, permissionsToAskFor, 9999);
        }

        Log.d(TAG, "StartLocationUpdates!");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, userLocListener);

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    private void stopLocationUpdates() {
        Log.d(TAG, "StopLocationUpdates!");

        // End requests for locationUpdates
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, userLocListener);

        // to stop the sensor-listener and save battery
        mSensorManager.unregisterListener(this);
    }

    private void goToShoppingList() {
        Intent intent = new Intent(this, se.liu.ida.hanal086.hannes.shopassistant.ShoppingListActivity.class);
        // Start new Activity
        startActivityForResult(intent, ACTIVITY_REQUEST_CODE);
    }

    private void goToSettings() {
        Intent intent = new Intent(this, se.liu.ida.hanal086.hannes.shopassistant.SettingsActivity.class);
        // Start new Activity
        startActivityForResult(intent, ACTIVITY_REQUEST_CODE);
    }

    private void goToAbout() {
        Intent intent = new Intent(this, se.liu.ida.hanal086.hannes.shopassistant.AboutActivity.class);
        // Start new Activity
        startActivityForResult(intent, ACTIVITY_REQUEST_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) { // TODO Check if needed
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        /*
           Eventuall handler of permissionResults like:
            - User didn't allow persmission
            - User only partly allowed permissions
            - etc
        */
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) { // TODO Check if needed
        Log.d(TAG, "OnConnected");
    }



    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(ERROR_TAG, connectionResult.getErrorMessage());
        return; // TODO Implement GPS connectiong failure Exception-message | OR REMOVE??
    }

}
