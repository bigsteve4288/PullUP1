package pullup.ctd.com.myapplication;

import android.Manifest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
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

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import ctd.solutions.pullup.com.R;

public class MainMapActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private ProgressDialog pDialog;
    // url to create new product
    private static String url_create_product = "http://192.168.1.69/pullup/insert_events_to_db.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";

    JSONParser jsonParser = new JSONParser();

    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    final Context context = this;
    LatLng userEnteredLocation;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    String UserEnteredPlace = null;
    String placeToShowInDialogBuilder;
    AlertDialog.Builder alertDialogBuilder;
    EditText editTextEventName;
    EditText editTextEventStartTime;
    EditText editTextEventStatus;
    EditText editTextEventImage;

    EditText editTextEventHostName;
    Intent intent = getIntent();
    AutoCompleteTextView atvPlaces;
    PlacesAutocompleteTextView placesAutocompleteTextView;
    double lat;
    double lng;
    LinearLayout layout;
    TextView textView;
    Place placeToGo;
    double latitude;
    double longitude;
    String[] s = {"Strip Club ", "Night Club", "Kick Back ", "Charity", "Concert", "Football Game",
            "BasketBall Game ", "Shopping", "Advertisement 1", "Advertisement 2"};
    InputStream is = null;
    String result = null;
    String line = null;
    int code;


    String eventtype;
    String eventname;
    String eventAddress;
    String eventstarttime;
    String eventhost;
    String eventstatus;
    String eventimage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_map);

        getSupportActionBar().setTitle("Pull Up");

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);
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

                //  tx= (TextView)findViewById(R.id.text1);
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

                                /*double latitude = geoPoint.getLatitudeE6() / 1E6;
                                double longitude = geoPoint.getLongitudeE6() / 1E6;

                                location.setLatitude(latitude);
                                location.setLongitude(longitude);*/
                                try {
                                    latitude = getLocationFromAddress(UserEnteredPlace).lat / 1E6;
                                    longitude = getLocationFromAddress(UserEnteredPlace).lng / 1E6;


                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                // placeToGo.
                                // userEnteredLocation = placeToGo.getLatLng();


                                try {
                                    Log.d("Lat", String.valueOf(getLocationFromAddress(UserEnteredPlace)));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                // Log.d("Lng", String.valueOf(lng));
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
                        new CreateNewEvent().execute();



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
     * Background Async Task to Create new product
     */
    class CreateNewEvent extends AsyncTask<String, Void, Void> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
          /*  pDialog = new ProgressDialog(context);
            pDialog.setMessage("Creating Event..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();*/
        }

        /**
         * Creating product
         */
        protected Void doInBackground(String... args) {

            // Building Parameters
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("eventtype", eventtype));
            nameValuePairs.add(new BasicNameValuePair("eventname", eventname));
            nameValuePairs.add(new BasicNameValuePair("eventAddress", eventAddress));
            nameValuePairs.add(new BasicNameValuePair("eventstarttime", eventstarttime));
            nameValuePairs.add(new BasicNameValuePair("eventhost", eventhost));
            nameValuePairs.add(new BasicNameValuePair("eventstatus", eventstatus));
            nameValuePairs.add(new BasicNameValuePair("eventimage", eventimage));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_create_product,
                    "POST", nameValuePairs);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // successfully created product
                   /* Intent i = new Intent(getApplicationContext(), AllProductsActivity.class);
                    startActivity(i);*/
                    Log.d("Success", "Inserted Them IN DB");

                    // closing this screen
                   // finish();
                } else {
                    // failed to create product
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            pDialog.dismiss();

        }

/*    private void insertEventintoDB()  {

        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        nameValuePairs.add(new BasicNameValuePair("eventtype",eventtype));
        nameValuePairs.add(new BasicNameValuePair("eventname",eventname));
        nameValuePairs.add(new BasicNameValuePair("eventAddress",eventAddress));
        nameValuePairs.add(new BasicNameValuePair("eventstarttime",eventstarttime));
        nameValuePairs.add(new BasicNameValuePair("eventhost",eventhost));
        nameValuePairs.add(new BasicNameValuePair("eventstatus",eventstatus));
        nameValuePairs.add(new BasicNameValuePair("eventimage",eventimage));


        try
        {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://192.168.1.69/pullup/insert.php");
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
            Log.e("pass 1", "connection success ");
        }
        catch(Exception e)
        {
            Log.e("Fail 1", e.toString());
            Toast.makeText(getApplicationContext(), "Invalid IP Address",
                    Toast.LENGTH_LONG).show();
        }

        try
        {
            BufferedReader reader = new BufferedReader
                    (new InputStreamReader(is,"iso-8859-1"),8);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
            is.close();
            result = sb.toString();
            Log.e("pass 2", "connection success ");
        }
        catch(Exception e)
        {
            Log.e("Fail 2", e.toString());
        }

        try
        {
            JSONObject json_data = new JSONObject(result);
            code=(json_data.getInt("code"));

            if(code==1)
            {
                Toast.makeText(getBaseContext(), "Inserted Successfully",
                        Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(getBaseContext(), "Sorry, Try Again",
                        Toast.LENGTH_LONG).show();
            }
        }
        catch(Exception e)
        {
            Log.e("Fail 3", e.toString());
        }

    }*/


/*    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                userEnteredLocation = place.getLatLng();
                UserEnteredPlace = place.getName().toString();

                Log.i("Place", "Place: " + place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i("Place Error", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }*/

        public void getLatLongFromAddress(String youraddress) {
            String uri = "http://maps.google.com/maps/api/geocode/json?address=" +
                    youraddress + "&sensor=false";
            HttpGet httpGet = new HttpGet(uri);
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            StringBuilder stringBuilder = new StringBuilder();

            try {
                response = client.execute(httpGet);
                HttpEntity entity = response.getEntity();
                InputStream stream = entity.getContent();
                int b;
                while ((b = stream.read()) != -1) {
                    stringBuilder.append((char) b);
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject = new JSONObject(stringBuilder.toString());

                lng = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lng");

                lat = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lat");

                Log.d("latitude", String.valueOf(lat));
                Log.d("longitude", String.valueOf(lng));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}


