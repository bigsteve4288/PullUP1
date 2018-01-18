package pullup.ctd.com.myapplication;

import android.Manifest;

import org.apache.http.NameValuePair;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.vision.barcode.Barcode;
import com.seatgeek.placesautocomplete.OnPlaceSelectedListener;
import com.seatgeek.placesautocomplete.PlacesAutocompleteTextView;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import ctd.solutions.pullup.com.R;

public class MainMapActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private ProgressDialog pDialog;
    // url to create new product
    private static String url_create_new_event = "http://ec2-54-174-234-251.compute-1.amazonaws.com/insert.php";

    // JSON Node names
    JSONParser jsonParser = new JSONParser();
    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    final Context context = this;
    String UserEnteredPlace = null;
    AlertDialog.Builder alertDialogBuilder;
    EditText editTextEventName;
    EditText editTextEventStartTime;
    EditText editTextEventStatus;
    EditText editTextEventImage;

    EditText editTextEventHostName;
    PlacesAutocompleteTextView placesAutocompleteTextView;
    double lat;
    double lng;
    LinearLayout layout;
    double latitude;
    double longitude;
    String[] s = {"Strip Club ", "Night Club", "Kick Back ", "Charity", "Concert", "Football Game",
            "BasketBall Game ", "Shopping", "Advertisement 1", "Advertisement 2"};

    String eventtype;
    String eventname;
    String eventAddress;
    String eventstarttime;
    String eventhost;
    String eventstatus;
    String eventimage;

    ArrayList<PullUpEvents> eventsList = new ArrayList<PullUpEvents>();
    HashMap<String, String> map = new HashMap<String, String>();

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    // url to get all events list
    private static String url_checkForEvents = "http://ec2-54-174-234-251.compute-1.amazonaws.com/getAll.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";

    private static final String TAG_PULLUPEVENT = "pullupevent";

    private static final String TAG_EVENT_TYPE = "eventtype";
    private static final String TAG_EVENT_NAME = "eventname";
    private static final String TAG_EVENT_ADDRESS = "eventAddress";
    private static final String TAG_EVENT_STARTTIME = "eventstarttime";
    private static final String TAG_EVENT_HOST = "eventhost";
    private static final String TAG_EVENT_STATUS = "eventstatus";
    private static final String TAG_EVENT_IMAGE = "eventimage";

    // events JSONArray
    JSONArray events = null;

    Connection conn = null;
    Statement stmt = null;
    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://pulluptest.cf2qkbf5sehb.us-east-1.rds.amazonaws.com/PullUpTest";

    //  Database credentials
    static final String USER = "bigsteve4288";
    static final String PASS = "Raven0209";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_map);

        getSupportActionBar().setTitle("Pull Up");

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        checkForEvents();
    }

    private void checkForEvents() {

        new LoadAllEvents().execute();

    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(100000);
        mLocationRequest.setFastestInterval(100000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

        //move map camera
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainMapActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.add:

                // set title
                alertDialogBuilder = new AlertDialog.Builder(context);
                layout = new LinearLayout(MainMapActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);

                final ArrayAdapter<String> adp = new ArrayAdapter<String>(MainMapActivity.this,
                        android.R.layout.simple_spinner_item, s);

                final Spinner sp = new Spinner(MainMapActivity.this);
                sp.setLayoutParams(new LinearLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                        ActionBar.LayoutParams.WRAP_CONTENT));
                sp.setAdapter(adp);
                eventtype = sp.getSelectedItem().toString();
                layout.addView(sp);


                editTextEventName = new EditText(MainMapActivity.this);
                editTextEventName.setHint("Enter Event Name");
                layout.addView(editTextEventName);

                placesAutocompleteTextView = new PlacesAutocompleteTextView(context, "AIzaSyDGWXjVQJsii7LYWV0DUgjXaXsgbKrOIFs");
                placesAutocompleteTextView.setHint("Enter Address");
                placesAutocompleteTextView.setOnPlaceSelectedListener(
                        new OnPlaceSelectedListener() {
                            @Override
                            public void onPlaceSelected(@NonNull com.seatgeek.placesautocomplete.model.Place place) {
                                Log.d("String", place.description);

                                UserEnteredPlace = place.description;
                                try {
                                    latitude = getLocationFromAddress(UserEnteredPlace).lat / 1E6;
                                    longitude = getLocationFromAddress(UserEnteredPlace).lng / 1E6;


                                } catch (IOException e) {
                                    e.printStackTrace();
                                }


                                try {
                                    Log.d("Lat", String.valueOf(getLocationFromAddress(UserEnteredPlace)));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                );
                layout.addView(placesAutocompleteTextView);

                editTextEventStartTime = new EditText(MainMapActivity.this);
                editTextEventStartTime.setHint("Enter Event Start Time");
                layout.addView(editTextEventStartTime);

                editTextEventHostName = new EditText(MainMapActivity.this);
                editTextEventHostName.setHint("Enter Event Host Name");
                layout.addView(editTextEventHostName);

                editTextEventStatus = new EditText(MainMapActivity.this);
                editTextEventStatus.setHint("Enter Event Status");
                layout.addView(editTextEventStatus);

                editTextEventImage = new EditText(MainMapActivity.this);
                editTextEventImage.setHint("Enter Event Image");
                layout.addView(editTextEventImage);


                alertDialogBuilder.setTitle("Manually Added Event");
                alertDialogBuilder.setView(layout);

                alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        eventname = editTextEventName.getText().toString();
                        eventAddress = UserEnteredPlace;
                        eventstarttime = editTextEventStartTime.getText().toString();
                        eventhost = editTextEventHostName.getText().toString();
                        eventstatus = editTextEventStatus.getText().toString();
                        eventimage = editTextEventImage.getText().toString();
                        mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(UserEnteredPlace));
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
                        new CreateNewEvent(eventtype, eventname, eventAddress, eventstarttime, eventhost, eventstatus, eventimage).execute();


                    }
                });
                alertDialogBuilder.show();

                /*case R.id.reset:
                    Intent i = new Intent(getApplicationContext(), AllProductsActivity.class);
                    startActivity(i);*/
        }
        return super.onOptionsItemSelected(item);
    }

    public Barcode.GeoPoint getLocationFromAddress(String strAddress) throws IOException {

        Geocoder coder = new Geocoder(this);
        List<Address> address;
        Barcode.GeoPoint p1 = null;


        address = coder.getFromLocationName(strAddress, 5);
        if (address == null) {
            return null;
        }
        Address location = address.get(0);
        location.getLatitude();
        location.getLongitude();

        p1 = new Barcode.GeoPoint((double) (location.getLatitude() * 1E6),
                (double) (location.getLongitude() * 1E6));

        return p1;

    }

    /**
     * Background Async Task to Create new event
     */
    class CreateNewEvent extends AsyncTask<String, Void, Void> {

        /**
         * Before starting background thread Show Progress Dialog
         */

        String eventtype1;
        String eventName1;
        String eventAddress1;
        String eventstarttime1;
        String eventhost1;
        String eventstatus1;
        String eventimage1;

        public CreateNewEvent(String eventtype, String eventname, String eventAddress, String eventstarttime,
                              String eventhost, String eventstatus, String eventimage) {

            eventtype1 = eventtype;
            eventName1 = eventname;
            eventAddress1 = eventAddress;
            eventstarttime1 = eventstarttime;
            eventhost1 = eventhost;
            eventstatus1 = eventstatus;
            eventimage1 = eventimage;


        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(String... args) {
            try{
                //STEP 2: Register JDBC driver
                Class.forName("com.mysql.jdbc.Driver");

                //STEP 3: Open a connection
                System.out.println("Connecting to a selected database...");
                conn = DriverManager.getConnection(DB_URL, USER, PASS);
                System.out.println("Connected database successfully...");

                //STEP 4: Execute a query
                System.out.println("Inserting records into the table...");
                stmt = conn.createStatement();

                String sql = "INSERT INTO pullup (eventtype, eventname, eventAddress, eventstarttime, eventhost, eventstatus, eventimage) " +
                        "VALUES ('" +eventtype1+"', '"+eventName1+"', '"+eventAddress1+ "', '"+eventstarttime1+"','"+eventhost1+"','"+eventstatus1+"','"+eventimage1+"')";
                Log.d("sql statement : ", sql);
                stmt.executeUpdate(sql);

                System.out.println("Inserted records into the table...");

            }catch(SQLException se){
                //Handle errors for JDBC
                se.printStackTrace();
            }catch(Exception e){
                //Handle errors for Class.forName
                e.printStackTrace();
            }finally{
                //finally block used to close resources
                try{
                    if(stmt!=null)
                        conn.close();
                }catch(SQLException se){
                }// do nothing
                try{
                    if(conn!=null)
                        conn.close();
                }catch(SQLException se){
                    se.printStackTrace();
                }//end finally try
            }//end try
            return null;
        }


        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            pDialog.dismiss();

        }

    }

    /**
     * Background Async Task to Load all product by making HTTP Request
     */
    class LoadAllEvents extends AsyncTask<String, Void, Void> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
       /*     pDialog = new ProgressDialog(MainMapActivity.this);
            pDialog.setMessage("Loading events. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();*/
        }

        /**
         * getting All events from url
         */
        protected Void doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONArray json = jParser.makeHttpRequest(url_checkForEvents, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("All Events: ", json.toString());

            for (int i = 0; i < json.length(); i++) {

                JSONObject c = null;
                try {
                    c = json.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                PullUpEvents p = null;
                try {
                    p = new PullUpEvents(c.getString(TAG_EVENT_TYPE), c.getString(TAG_EVENT_NAME), c.getString(TAG_EVENT_ADDRESS),
                            c.getString(TAG_EVENT_STARTTIME), c.getString(TAG_EVENT_HOST), c.getString(TAG_EVENT_STATUS), c.getString(TAG_EVENT_IMAGE));
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                eventsList.add(p);
                Log.d("Events List", eventsList.toString());
            }

            onPostExecute();

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute() {
            // dismiss the dialog after getting all events
            //   pDialog.dismiss();
            // updating UI from Background Thread

            Log.d("Events List", eventsList.toString());
            runOnUiThread(new Runnable() {
                public void run() {

                    for (PullUpEvents p : eventsList) {

                        try {
                            latitude = getLocationFromAddress(p.getEventAddress()).lat / 1E6;
                            // Log.d("Latitude", String.valueOf(latitude));
                            longitude = getLocationFromAddress(p.getEventAddress()).lng / 1E6;
                            //  Log.d("Longitude", String.valueOf(longitude));
                            mGoogleMap.addMarker(new MarkerOptions()
                                            .title(p.getEventName())
                                            .position(new LatLng(latitude, longitude))
                                    // etc.
                            );
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    // mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(map.get(TAG_EVENT_NAME)));

                }
            });



     /*class AddMarker {
        public AddMarker(LoadAllEvents loadAllEvents) {
        }

         public AddMarker(MainMapActivity mainMapActivity) {
         }

         public void addMarker(final GoogleMap mGoogleMap, String eventname, String eventAddress) throws IOException {

             final MarkerOptions opts = new MarkerOptions();
             Handler handler = new Handler(Looper.getMainLooper());
             MainMapActivity m = new MainMapActivity();
             //m.getLocationFromAddress(eventAddress);

             latitude = m.getLocationFromAddress(eventAddress).lat / 1E6;
             longitude = m.getLocationFromAddress(eventAddress).lng / 1E6;

             opts.title(eventname);
            // mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(UserEnteredPlace));

             opts.position(new LatLng(latitude, longitude)).title(eventname);
             handler.post(new Runnable() {
                 public void run() {
                     mGoogleMap.addMarker(opts);
                 }
             });
         }
         }*/
        }
    }
}



