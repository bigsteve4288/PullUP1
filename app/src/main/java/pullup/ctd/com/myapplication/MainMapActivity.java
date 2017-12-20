package pullup.ctd.com.myapplication;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.eegeo.indoors.IndoorMapView;
import com.eegeo.mapapi.EegeoApi;
import com.eegeo.mapapi.EegeoMap;
import com.eegeo.mapapi.MapView;
import com.eegeo.mapapi.camera.CameraPosition;
import com.eegeo.mapapi.camera.CameraUpdateFactory;
import com.eegeo.mapapi.geometry.LatLng;
import com.eegeo.mapapi.map.OnInitialStreamingCompleteListener;
import com.eegeo.mapapi.map.OnMapReadyCallback;
import com.eegeo.mapapi.markers.Marker;
import com.eegeo.mapapi.markers.MarkerOptions;
import com.google.android.gms.vision.barcode.Barcode;
import com.seatgeek.placesautocomplete.OnPlaceSelectedListener;
import com.seatgeek.placesautocomplete.PlacesAutocompleteTextView;

import org.json.JSONArray;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ctd.solutions.pullup.com.R;

import static pullup.ctd.com.myapplication.JDBC.DB_URL;
import static pullup.ctd.com.myapplication.JDBC.PASS;
import static pullup.ctd.com.myapplication.JDBC.USER;

public class MainMapActivity extends AppCompatActivity {

    private MapView m_mapView;
    private EegeoMap m_eegeoMap = null;
    private IndoorMapView m_interiorView = null;
    private ProgressDialog pDialog;
    // url to create new product
    private static String url_create_new_event = "http://ec2-54-174-234-251.compute-1.amazonaws.com/insert.php";

    // url to get all events list
    private static String url_checkForEvents = "http://ec2-54-174-234-251.compute-1.amazonaws.com/get_all_events.php";
    private Marker m_marker = null;

    // JSON Node names
    JSONParser jsonParser = new JSONParser();
    final Context context = this;
    String UserEnteredPlace = null;
    AlertDialog.Builder alertDialogBuilder;
    EditText editTextEventName;
    EditText editTextEventStartTime;
    EditText editTextEventStatus;
    EditText editTextEventImage;
    EditText editTextEventHostName;
    PlacesAutocompleteTextView placesAutocompleteTextView;
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


    // JSON Node names
    private static final String TAG_SUCCESS = "pullupevent";
    private static final String TAG_PULLUPEVENT = "pullupevent";
    private static final String TAG_EVENT_TYPE = "eventtype";
    private static final String TAG_EVENT_NAME = "eventname";
    private static final String TAG_EVENT_ADDRESS = "eventAddress";
    private static final String TAG_EVENT_STARTTIME = "eventstarttime";
    private static final String TAG_EVENT_HOST = "eventhost";
    private static final String TAG_EVENT_STATUS = "eventstatus";
    private static final String TAG_EVENT_IMAGE = "eventimage";

    Connection conn = null;
    Statement stmt = null;
    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://pulluptest.cf2qkbf5sehb.us-east-1.rds.amazonaws.com/PullUpTest";

    //  Database credentials
    static final String USER = "bigsteve4288";
    static final String PASS = "Raven0209";

    // events JSONArray
    JSONArray events = null;
    EegeoMap mapEEgeo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EegeoApi.init(this, getString(R.string.eegeo_api_key));

        setContentView(R.layout.basic_map_activity);

        getSupportActionBar().setTitle("Pull Up");


        LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        boolean network_enabled = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Location location;

        if (network_enabled) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            location = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if(location!=null){
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            }
        }
        m_mapView = (MapView) findViewById(R.id.mapView);
        m_mapView.onCreate(savedInstanceState);

        m_mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final EegeoMap map) {
                m_eegeoMap = map;

                RelativeLayout uiContainer = (RelativeLayout) findViewById(R.id.eegeo_ui_container);
                m_interiorView = new IndoorMapView(m_mapView, uiContainer, m_eegeoMap);

                map.addInitialStreamingCompleteListener(new OnInitialStreamingCompleteListener() {
                    @Override
                    public void onInitialStreamingComplete() {
                        CameraPosition position = new CameraPosition.Builder()
                                .target(latitude, longitude)
                                .zoom(360)
                                .build();

                        map.moveCamera(CameraUpdateFactory.newCameraPosition(position));
                    }
                });


                Toast.makeText(MainMapActivity.this, "Hello World!", Toast.LENGTH_LONG).show();
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        m_mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        m_mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        m_mapView.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
                        mapEEgeo.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).labelText(UserEnteredPlace));
                        mapEEgeo.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
                        //  new CreateNewEvent().execute();
                        insertDataIntoDatabase(eventtype, eventname, eventAddress, eventstarttime, eventhost, eventstatus, eventimage);


                    }
                });
                alertDialogBuilder.show();

               /* case R.id.reset:
                Intent i = new Intent(getApplicationContext(), AllProductsActivity.class);
                startActivity(i);*/
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertDataIntoDatabase(String eventtype,String eventname, String eventAddress, String eventstarttime, String eventhost, String eventstatus, String eventimage) {
        //new CreateNewEvent().execute();

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
                    "VALUES (" +eventtype +", '"+eventname+"', '"+eventAddress+ "', '"+eventstarttime+"','"+eventhost+"','"+eventstatus+"','"+eventimage+"')";
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
    }

    public Barcode.GeoPoint getLocationFromAddress(String strAddress) throws IOException {

        Geocoder coder = new Geocoder(getApplicationContext());
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
}