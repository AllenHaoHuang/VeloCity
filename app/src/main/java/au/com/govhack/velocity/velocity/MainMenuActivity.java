package au.com.govhack.velocity.velocity;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MainMenuActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        View.OnKeyListener, GoogleMap.OnMapLongClickListener {

    private static final String TAG = MainMenuActivity.class.getSimpleName();
    private GoogleMap gmap;
    private CameraPosition mCameraPosition;
    private boolean mLocationPermissionGranted;
    private GoogleApiClient mGoogleApiClient;
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private Location mLastKnownLocation;
    private static final int DEFAULT_ZOOM = 14;

    EditText edittext;
    TextView addressAndTime;
    String origin = "Canberra";
    String destination = "";
    String raw_route = "";

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    ToggleButton toggleShortest, toggleFastest, toggleSafest, toggleScenic;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main_menu);

        toggleShortest = (ToggleButton) findViewById(R.id.buttonShortest);
        toggleFastest = (ToggleButton) findViewById(R.id.buttonFastest);
        toggleSafest = (ToggleButton) findViewById(R.id.buttonSafest);
        toggleSafest.setChecked(true);
        toggleScenic = (ToggleButton) findViewById(R.id.buttonScenic);

        EditText edittextproductnumber = (EditText) findViewById(R.id.editText);
        edittextproductnumber.setOnKeyListener(this);

        this.edittext = (EditText) edittextproductnumber;
        this.edittext.setBackgroundColor(Color.WHITE);
        this.addressAndTime = (TextView) findViewById(R.id.textView);
        addressAndTime.setVisibility(View.INVISIBLE);

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (gmap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, gmap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        // Listen to "Enter" key press
        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            if (mLocationPermissionGranted) {
                double lat = LocationServices.FusedLocationApi
                        .getLastLocation(mGoogleApiClient).getLatitude();
                double lon = LocationServices.FusedLocationApi
                        .getLastLocation(mGoogleApiClient).getLongitude();
                origin = lat + "," + lon;
            }

            destination = edittext.getText().toString();
            origin = origin.replace(" ", "+");
            destination = destination.replace(" ", "+");

            // Create the thread
            Thread getRoute = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String url = "http://api.velocity.shen.nz:1234/getRoute" +
                                "?origin=" + origin + "&destination=" + destination + "&option=" + option;
                        Log.d("Network", "Requesting data from " + url);
                        raw_route = Network.getDataFromUrl(url);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            // Start the thread
            getRoute.start();

            // Wait until thread ends
            try {
                getRoute.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d("Response", raw_route);

            // Server has returned an error response
            if (raw_route.contains("Error!")) {
                Toast.makeText(getApplicationContext(),
                        "Error! Please be more specific in your search."
                        , Toast.LENGTH_LONG).show();
                return false;
            }

            // Parse response
            DirectionsObject directions = JSON.parse(raw_route, DirectionsObject.class);
            Log.d("Directions", directions.getOverviewPolyline().getPoints());

            // Clear map first, then add points to Polyline
            gmap.clear();
            Polyline routeTo = gmap.addPolyline(new PolylineOptions()
                    .addAll(Decoder.decode(directions.getOverviewPolyline().getPoints())));
            routeTo.setColor(0xFF0060C0);
            gmapEmpty = false;

            LatLng endBounds = new LatLng(directions.getLegs().get(0).getEndLocation().getLat(), directions.getLegs().get(0).getEndLocation().getLng());
            LatLng startBounds = new LatLng(directions.getLegs().get(0).getStartLocation().getLat(), directions.getLegs().get(0).getStartLocation().getLng());

            // Add origin and destination markers
            ArrayList<Marker> markers = new ArrayList<>();
            markers.add(gmap.addMarker(new MarkerOptions().position(startBounds)
                    .title("Origin").snippet(directions.getLegs().get(0).getStartAddress())));
            markers.add(gmap.addMarker(new MarkerOptions().position(endBounds)
                    .title("Destination").snippet(directions.getLegs().get(0).getEndAddress())));

            // Add cycle crash markers
            if (directions.getCrashes() != null) {
                for (CycleCrashes.CycleCrash crash : directions.getCrashes()) {
                    LatLng crashBounds = new LatLng(crash.getLatitude(), crash.getLongitude());
                    markers.add(gmap.addMarker(new MarkerOptions()
                            .position(crashBounds)
                            .title("Bike Crash (" + crash.getDate() + ")")
                            .snippet(crash.toString())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))));
                }
            }

            // Add car speeding markers
            if (directions.getSpeeding() != null) {
                int id = 1;
                for (String speeder : directions.getSpeeding()) {
                    String lat = speeder.split(",")[0];
                    String longi = speeder.split(",")[1];
                    LatLng speedBounds = new LatLng(Double.parseDouble(lat), Double.parseDouble(longi));
                    markers.add(gmap.addMarker(new MarkerOptions()
                            .position(speedBounds)
                            .title("Car Speeding #" + id++)
                            .snippet(lat + ", " + longi)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
                }
            }

            // Add landmarks
            if (directions.getScenicspots() != null) {
                for (ScenicSpots.ScenicSpot spot : directions.getScenicspots()) {
                    LatLng scenicBounds = new LatLng(spot.getLatitude(), spot.getLongitude());
                    markers.add(gmap.addMarker(new MarkerOptions()
                            .position(scenicBounds)
                            .title(spot.getName())
                            .snippet(spot.getLatitude() + ", " + spot.getLongitude())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))));
                }
            }

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : markers) {
                builder.include(marker.getPosition());
            }
            LatLngBounds bounds = builder.build();

            int padding = 0; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

            // Move camera accordingly
            gmap.moveCamera(cu);
            DirectionsObject.Leg leg = directions.getLegs().get(0);
            addressAndTime.setText(leg.getDistance().getText() + ", " + leg.getDuration().getText()
                    + " via " + directions.getSummary());

            Animation a = new AlphaAnimation(0.00f, 1.00f);
            a.setDuration(1000);
            a.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    // TODO Auto-generated method stub
                }

                public void onAnimationRepeat(Animation animation) {
                    // TODO Auto-generated method stub
                }

                public void onAnimationEnd(Animation animation) {
                    addressAndTime.setVisibility(View.VISIBLE);
                }
            });
            addressAndTime.startAnimation(a);
            return true;
        }
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap = googleMap;
        gmap.setOnMapLongClickListener(this);
        // Move camera to Canberra
        LatLng canberra = new LatLng(-35.281, 149.130);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(canberra));
        // Get device location and update accordingly
        updateLocationUI();
        getDeviceLocation();
    }

    private void getDeviceLocation() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        if (mLocationPermissionGranted) {
            mLastKnownLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
        }

        // Set the map's camera position to the current location of the device.
        if (mCameraPosition != null) {
            gmap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mLastKnownLocation != null) {
            gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
        } else {
            Log.d(TAG, "Current location is null. Using defaults.");
            gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            gmap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d("", "Play services connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        // Refer to the reference doc for ConnectionResult to see what error codes might
        // be returned in onConnectionFailed.
        Log.d("???", "Play services connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }


    private void updateLocationUI() {
        if (gmap == null) {
            return;
        }

        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }

        if (mLocationPermissionGranted) {
            gmap.setMyLocationEnabled(true);
            gmap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            gmap.setMyLocationEnabled(false);
            gmap.getUiSettings().setMyLocationButtonEnabled(false);
            mLastKnownLocation = null;
        }
    }

    private String option = "Safest";

    public void buttonClick(View view) {
        // Get option string
        option = ((ToggleButton) view).getText().toString();
        Log.d("Toggled option", option);
        // Untoggle toggled buttons if any
        switch (option) {
            case "Safest":
                toggleShortest.setChecked(false);
                toggleFastest.setChecked(false);
                toggleScenic.setChecked(false);
                break;
            case "Shortest":
                toggleSafest.setChecked(false);
                toggleFastest.setChecked(false);
                toggleScenic.setChecked(false);
                break;
            case "Fastest":
                toggleSafest.setChecked(false);
                toggleShortest.setChecked(false);
                toggleScenic.setChecked(false);
                break;
            case "Scenic":
                toggleSafest.setChecked(false);
                toggleFastest.setChecked(false);
                toggleShortest.setChecked(false);
                break;
        }
    }

    @Override
    public void onMapLongClick(LatLng point) {
        String lat = String.format("%.6f", point.latitude);
        String lng = String.format("%.6f", point.longitude);
        gmap.addMarker(new MarkerOptions()
                .position(point)
                .title("Origin")
                .snippet(lat + ", " + lng));
        edittext.setText(lat + "," + lng);
    }

    private boolean gmapEmpty = true;

    @Override
    public void onBackPressed() {
        if (!gmapEmpty) {
            gmap.clear();
            gmapEmpty = true;
            addressAndTime.setVisibility(View.INVISIBLE);
            edittext.setText("");
        } else {
            super.onBackPressed();
        }
    }
}
