package pullup.ctd.com.myapplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import ctd.solutions.pullup.com.R;

public class AllProductsActivity extends ListActivity {

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> eventsList;

    // url to get all events list
    private static String url_all_products = "http://192.168.1.69/pullup/get_all_products.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PULLUPEVENT = "pullupEvent";
    private static final String TAG_EVENT_TYPE = "eventtype";
    private static final String TAG_EVENT_NAME = "eventname";
    private static final String TAG_EVENT_STARTTIME = "eventstarttime";
    private static final String TAG_EVENT_HOST = "eventhost";
    private static final String TAG_EVENT_STATUS = "eventstatus";

    // events JSONArray
    JSONArray events = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_products);

        // Hashmap for ListView
        eventsList = new ArrayList<HashMap<String, String>>();

        // Loading events in Background Thread
        new LoadAllProducts().execute();

        // Get listview
        ListView lv = getListView();

        // on seleting single product
        // launching Edit Product Screen
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                String pid = ((TextView) view.findViewById(R.id.pid)).getText()
                        .toString();

                // Starting new intent
                Intent in = new Intent(getApplicationContext(),
                        NewProductActivity.class);
                // sending pid to next activity
                in.putExtra(TAG_EVENT_HOST, pid);

                // starting new activity and expecting some response back
                startActivityForResult(in, 100);
            }
        });

    }

    // Response from Edit Product Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if result code 100
        if (resultCode == 100) {
            // if result code 100 is received
            // means user edited/deleted product
            // reload this screen again
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        pDialog.dismiss();
    }

    /**
     * Background Async Task to Load all product by making HTTP Request
     */
    class LoadAllProducts extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AllProductsActivity.this);
            pDialog.setMessage("Loading events. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All events from url
         */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_all_products, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("All Events: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // events found
                    // Getting Array of Events
                    events = json.getJSONArray(TAG_PULLUPEVENT);

                    // looping through All Events
                    for (int i = 0; i < events.length(); i++) {
                        JSONObject c = events.getJSONObject(i);

                        // Storing each json item in variable
                        String EVENTYPE = c.getString(TAG_EVENT_TYPE);
                        String EVENTNAME = c.getString(TAG_EVENT_NAME);
                        String EVENTSTARTTIME = c.getString(TAG_EVENT_STARTTIME);
                        String EVENTHOST = c.getString(TAG_EVENT_HOST);
                        String EVENTSTATUS = c.getString(TAG_EVENT_STATUS);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_EVENT_TYPE, EVENTYPE);
                        map.put(TAG_EVENT_NAME, EVENTNAME);
                        map.put(TAG_EVENT_STARTTIME, EVENTSTARTTIME);
                        map.put(TAG_EVENT_HOST, EVENTHOST);
                        map.put(TAG_EVENT_STATUS, EVENTSTATUS);


                        // adding HashList to ArrayList
                        eventsList.add(map);

                        /*Intent intent = new Intent(AllProductsActivity.this, MainMapActivity.class);
                       // Bundle bundle = new Bundle();
                       // bundle.putString(“stuff”, map);
                        intent.putExtra("map", map);
                        startActivity(intent);*/
                    }
                } else {
                    // no events found
                    // Launch Add New product Activity
                    Intent i = new Intent(getApplicationContext(),
                            NewProductActivity.class);
                    // Closing all previous activities
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
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
            // dismiss the dialog after getting all events
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            AllProductsActivity.this, eventsList,
                            R.layout.list_item, new String[]{TAG_EVENT_TYPE,
                            TAG_EVENT_NAME},
                            new int[]{R.id.pid, R.id.name});
                    // updating listview
                    setListAdapter(adapter);
                }
            });

        }

    }
}