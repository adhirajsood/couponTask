package com.task.phone.coupontask;

import java.util.Iterator;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Home extends Activity implements IAsyncCallback,LocationListener{
    //The minimum distance to change updates in metters
    private static final long GPS_MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; //10 metters
    //The minimum time beetwen updates in milliseconds
    private static final long GPS_MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
    ListView lv;
    DBHandler dbHandler;
    private String lat,lng;
    Location location;
    JSONObject JsonObjStores = null;

    int i =0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        lv = (ListView) findViewById(R.id.listStores);
        dbHandler = new DBHandler(this);

        location = getLocation(this,this);
        if (location!=null){
            lat = String.valueOf(location.getLatitude());
            lng = String.valueOf(location.getLongitude());

        }else {
            Toast.makeText(this,"Location not enabled!",Toast.LENGTH_SHORT).show();
            return;
        }
        if (dbHandler.getStores().length()>0){
           setListView(dbHandler.getStores());
        }else {
            APIManager.getInstance().sendAsyncCall("GET", Home.this);
        }
    }
    @Override
    public void onSuccessResponse(String successResponse) {
        JSONArray StoresArray = new JSONArray();
        int i = 0;
        try {
            JSONObject jsonObject = new JSONObject(successResponse);

                JSONObject jsonObject1 = jsonObject.getJSONObject("data");
           Iterator<String> iter = jsonObject1.keys();
            while (iter.hasNext()) {
                String key = iter.next();
                try {
                    Object value = jsonObject1.get(key);
                    JSONObject jobj = new JSONObject(value.toString());


                    JsonObjStores = new JSONObject("{}");
                    JsonObjStores.put("BrandID",jobj.getString("BrandID"));
                    JsonObjStores.put("BrandName",jobj.getString("BrandName"));
                    JsonObjStores.put("NeighbourhoodName", jobj.getString("NeighbourhoodName"));
                    JsonObjStores.put("LogoURL", jobj.getString("LogoURL"));
                    JsonObjStores.put("Latitude", jobj.getString("Latitude"));
                    JsonObjStores.put("Longitude", jobj.getString("Longitude"));
                    Location locationA = new Location("point A");
                    locationA.setLatitude(Double.valueOf(lat));
                    locationA.setLongitude(Double.valueOf(lng));
                    Location locationB = new Location("point B");
                    locationB.setLatitude(Double.valueOf(jobj.getString("Latitude")));
                    locationB.setLongitude(Double.valueOf(jobj.getString("Longitude")));
                    float distanceYou = locationA.distanceTo(locationB)/1000;
                    JsonObjStores.put("distance", distanceYou);
                    dbHandler.addStores(JsonObjStores);
                    StoresArray.put(i, JsonObjStores);
                    i++;
                } catch (JSONException e) {
                    // Something went wrong!
                }
            }

                setListView(dbHandler.getStores());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setListView(JSONArray jsonArray){

        CustomListStores adapter = new CustomListStores(Home.this,jsonArray);
        lv.setAdapter(adapter);
    }

    @Override
    public void onErrorResponse(int errorCode, String errorResponse) {

    }




    public static Location getLocation(Context mContext, LocationListener listener)
    {
        LocationManager locationManager;
        Location location = null;

        try
        {
            locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            if(locationManager==null){
                return null;

            }

            //getting GPS status
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            //getting network status
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled && !isNetworkEnabled)
            {
                // no network provider is enabled

                return null;
            }
            else
            {
                if ( isNetworkEnabled)
                {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            GPS_MIN_TIME_BW_UPDATES,
                            GPS_MIN_DISTANCE_CHANGE_FOR_UPDATES, listener);

                    if (locationManager != null)
                    {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }
                if (isGPSEnabled)
                {
                    if (location == null)
                    {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                GPS_MIN_TIME_BW_UPDATES,
                                GPS_MIN_DISTANCE_CHANGE_FOR_UPDATES, listener);
                        if (locationManager != null)
                        {
                            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                    }
                }

            }
        }
        catch (Exception e)
        {
            Log.e("Get Loc", "Failed to connect to LocationManager", e);
        }
        return location;
    }


    public void updateStoreDistance(){
        JSONArray jsonUpdate = dbHandler.getStores();
        try {
            for (int i =0;i<jsonUpdate.length();i++) {
                Object str = jsonUpdate.get(i);
                JSONObject jobj = new JSONObject(str.toString());

                Location locationA = new Location("point A");
                locationA.setLatitude(Double.valueOf(lat));
                locationA.setLongitude(Double.valueOf(lng));
                Location locationB = new Location("point B");
                locationB.setLatitude(Double.valueOf(jobj.getString("Latitude")));
                locationB.setLongitude(Double.valueOf(jobj.getString("Longitude")));
                float distanceYou = locationA.distanceTo(locationB)/1000;
                dbHandler.updateDistance(distanceYou,jobj.getString("BrandID"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onLocationChanged(Location location) {

        location = getLocation(this,this);
        if (location!=null){
            lat = String.valueOf(location.getLatitude());
            lng = String.valueOf(location.getLongitude());
            updateStoreDistance();
        }else {
            Toast.makeText(this,"Location not enabled!",Toast.LENGTH_SHORT).show();
            return;
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
