package com.example.shubham.mapstest;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapsActivity extends FragmentActivity implements LocationListener {
    public final String ROUTES="routes";
    public final String SUMMARY= "summary";
    public final String LEGS = "legs";
    public final String DISTANCE = "distance";
    public final String DURATION = "duration";
    public final String TEXT = "text";
    public final String VALUE = "value";
    public final String STEPS  = "steps";
    public final String START_LOCATION = "start_location";
    public final String END_LOCATION = "end_location";
    public final String HTML_INSTRUCTION = "html_instructions";
    public final String LATITUDE = "lat";
    public final String LONGITUDE = "lng";
    public final String POLYLINE = "polyline";
    public final String POINTS = "points";
    public final String directionAPI_KEY = "AIzaSyAtcuFQfkWxx7l-OGsaYovHrUyHCMfh5vg";


    public String directionAPIResponse;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LatLng latLng;
    private static double latitude;
    private static double longitude;
    public static String directionAPI = "https://maps.googleapis.com/maps/api/directions/json?";
    public Context context;
    public ArrayList<Long> distanceArray;
    public ArrayList<Long> durationArray;
    public ArrayList<LatLng> ltlngArray;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setLatLng();
        setUpMapIfNeeded();
        context = getApplicationContext();
        //For direction Api
        GetAPIResponse directionResponse = new GetAPIResponse();
        try{
           URL directionURL = new URL(directionAPI+"origin=Toronto&destination=Montreal&key="+directionAPI_KEY);
            directionResponse.execute(directionURL,null,null);

        }catch(Exception e){
            System.out.println(e);
        }
        distanceArray = new ArrayList<Long>();
        durationArray = new ArrayList<Long>();
        ltlngArray = new ArrayList<LatLng>();
    }
    protected void setLatLng(){
        GPSTracker gpsTracker = new GPSTracker(this);
        if (gpsTracker.getIsGPSTrackingEnabled())
        {
            String stringLatitude = String.valueOf(gpsTracker.latitude);
            latitude = gpsTracker.getLatitude();

            System.out.println(latitude + "lat");
            String stringLongitude = String.valueOf(gpsTracker.longitude);
            longitude = gpsTracker.getLongitude();
            System.out.println(longitude + "lng");
            latLng = new LatLng(latitude,longitude);

        }
        else
        {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gpsTracker.showSettingsAlert();
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        System.out.println(latitude+longitude+"");
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude,longitude))
                .title("My Loc")
                .snippet("ABC"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude), 12.0f));

    }

    @Override
    public void onLocationChanged(Location location) {
            Log.d("Latitde:", location.getLatitude() + "");
        //txtLat.setText("Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude());
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude", "disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude", "enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude","status");
    }

    //##############################################################################
    public List<Route> parse(String routesJSONString) throws Exception {
        try {
            List<Route> routeList = new ArrayList<Route>();
            final JSONObject jSONObject = new JSONObject(routesJSONString);
            JSONArray routeJSONArray = jSONObject.getJSONArray(ROUTES);
            Route route;
            JSONObject routesJSONObject;
            for (int m = 0; m < routeJSONArray.length(); m++) {
                route = new Route(context);
                routesJSONObject = routeJSONArray.getJSONObject(m);
                JSONArray legsJSONArray;
                route.setSummary(routesJSONObject.getString(SUMMARY));
                legsJSONArray = routesJSONObject.getJSONArray(LEGS);
                JSONObject legJSONObject;
                Leg leg;
                JSONArray stepsJSONArray;
                for (int b = 0; b < legsJSONArray.length(); b++) {
                    leg = new Leg();
                    legJSONObject = legsJSONArray.getJSONObject(b);
                    leg.setDistance(new Distance(legJSONObject.optJSONObject(DISTANCE).optString(TEXT), legJSONObject.optJSONObject(DISTANCE).optLong(VALUE)));
                    leg.setDuration(new Duration(legJSONObject.optJSONObject(DURATION).optString(TEXT), legJSONObject.optJSONObject(DURATION).optLong(VALUE)));
                    stepsJSONArray = legJSONObject.getJSONArray(STEPS);
                    JSONObject stepJSONObject, stepDurationJSONObject, legPolyLineJSONObject, stepStartLocationJSONObject, stepEndLocationJSONObject;
                    Step step;
                    String encodedString;
                    LatLng stepStartLocationLatLng, stepEndLocationLatLng;
                    for (int i = 0; i < stepsJSONArray.length(); i++) {
                        stepJSONObject = stepsJSONArray.getJSONObject(i);
                        step = new Step();
                        JSONObject stepDistanceJSONObject = stepJSONObject.getJSONObject(DISTANCE);
                        Distance temp_dist = new Distance(stepDistanceJSONObject.getString(TEXT), stepDistanceJSONObject.getLong(VALUE));
                        long distance_int = temp_dist.getValue();
                        step.setDistance(temp_dist);
                        if(i==0){
                            distanceArray.add((long)0.0);
                        }else{
                            distanceArray.add(distance_int);

                        }
                        stepDurationJSONObject = stepJSONObject.getJSONObject(DURATION);
                        Duration temp_duration = new Duration(stepDurationJSONObject.getString(TEXT), stepDurationJSONObject.getLong(VALUE));
                        step.setDuration(temp_duration);
                        long duration_int = temp_duration.getValue();
                        if(i==0){
                            durationArray.add((long)0.0);
                        }else{
                            durationArray.add(duration_int);


                        }

                        stepEndLocationJSONObject = stepJSONObject.getJSONObject(END_LOCATION);
                        stepEndLocationLatLng = new LatLng(stepEndLocationJSONObject.getDouble(LATITUDE), stepEndLocationJSONObject.getDouble(LONGITUDE));
                        step.setEndLocation(stepEndLocationLatLng);
                        step.setHtmlInstructions(stepJSONObject.getString(HTML_INSTRUCTION));
                        legPolyLineJSONObject = stepJSONObject.getJSONObject(POLYLINE);
                        encodedString = legPolyLineJSONObject.getString(POINTS);
                      //  step.setPoints(decodePolyLines(encodedString));
                        stepStartLocationJSONObject = stepJSONObject.getJSONObject(START_LOCATION);
                        stepStartLocationLatLng = new LatLng(stepStartLocationJSONObject.getDouble(LATITUDE), stepStartLocationJSONObject.getDouble(LONGITUDE));
                        ltlngArray.add(stepStartLocationLatLng);
                        step.setStartLocation(stepStartLocationLatLng);
                        leg.addStep(step);
                    }
                    route.addLeg(leg);
                }
                routeList.add(route);
            }
            return routeList;
        } catch (Exception e) {
            throw e;
        }
    }
    //##############################################################################
    private class GetAPIResponse extends AsyncTask<URL, Void, String> {
        @Override
        protected String doInBackground(URL... url) {
            String str=null;
            try {
                //url = new URL("http://sandbox-t.olacabs.com/v1/bookings/create?pickup_lat=12.950072&pickup_lng=77.642684&pickup_mode=NOW&category=sedan");

                HttpURLConnection urlConnection = (HttpURLConnection) url[0].openConnection();
                if(url[1]!=null)
                    urlConnection.setRequestProperty("X-APP-TOKEN", "e5241730a6a84073a8a015603b207c1a");
                if(url[2]!=null)
                    urlConnection.setRequestProperty("Authorization","Bearer bb4919b942904a599e91bd2bb26a355c");
        /*   if
                System.out.println((urlConnection.getResponseCode()));/*== HttpURLConnection.HTTP_OK)
                System.out.println("ok");
            else
                System.out.println("false");
*///System.out.println("ok");
                try {
                    InputStream in = null;
                    try {
                        System.out.println("Hello");
                        in = new BufferedInputStream(urlConnection.getInputStream());
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("hello");
                    }
                    str=readStream(in);
                    System.out.println("result=" + str);

                } catch (Exception e) {
             
                }
            } catch (Exception e) {
                e.printStackTrace();
             
            }
            return str;
        }
        protected void onPostExecute(String result) {
            System.out.println(result+"");
            directionAPIResponse = result;
            try {
                parse(directionAPIResponse);
                int distanceArrLen = distanceArray.size();
                for(int i =0;i<distanceArrLen;i++)
                    System.out.println(distanceArray.get(i));
            }
            catch(Exception e){
                System.out.println(e);
            }
            }


    }



    private String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while(i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }

    /*#############################################################################
     *
     * Route Tracking
     * #############################################################################
     */
    private double distance(double lat1, double lon1, double lat2, double lon2) { double theta = lon1 - lon2; double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta)); dist = Math.acos(dist); dist = rad2deg(dist); dist = dist * 60 * 1.1515; return (dist); } private double deg2rad(double deg) { return (deg * Math.PI / 180.0); } private double rad2deg(double rad) { return (rad * 180.0 / Math.PI); }
    public boolean onTrack(int index){
        if(index==0)
                return true;
        Double act_x,act_y;
        Double exp_x,exp_y;
        GPSTracker gpsTkr = new GPSTracker(context);
        act_x = gpsTkr.getLatitude();
        act_y = gpsTkr.getLongitude();
        exp_x = ltlngArray.get(index).latitude;
        exp_y = ltlngArray.get(index).longitude;
        Double dist = distance(act_x,act_y,exp_x,exp_y);
        if(dist>500)
            return false;
        return true;
    }

}
