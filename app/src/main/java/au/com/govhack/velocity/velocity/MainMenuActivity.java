package au.com.govhack.velocity.velocity;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.location.Location;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

import static android.R.color.white;
import static au.com.govhack.velocity.velocity.R.id.add;
import static au.com.govhack.velocity.velocity.R.id.textView;

public class MainMenuActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnKeyListener{

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
    String raw_route = "vzhvEslfm[w@jPw@bNS`EMtBGz@MpAGXKVqCrFiD|GUh@g@bBMn@UzAw@vKQrAO|@iAbD{DnKYp@w@|A_AvAgAjAoAbA_MdHqBbA{Aj@_B^o@HkFTgGTe@DkATgA`@aAn@}@x@u@bAm@jAc@pAYlA{CzSK|@Iv@E`@Et@EnCAbAFxBb@rFTvCBp@DbBBbB?dEKfEQlD]jDc@|Cq@`D{@~CcAvCiAhCe@z@q@dAwBpC}CzDq@x@gClC_@b@}@fAuBxC]d@uAbCkAjC_@t@Uj@e@lAc@pAiAbEyBjJ{Nbn@cAxEy@zE_DtRMp@a@vBCPIEOAC[a@k@c@R[NY?W@sKBiXC{EF@p@ChGE|@Ed@Qv@QPK`@Wl@i@`AAXG`@E`@JzADXO@QFuE|@e@LMG}AZSs@GW?}H";

    boolean mLocationPermissionGranted;
    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        setContentView(R.layout.activity_main_menu);

        EditText edittextproductnumber = (EditText) findViewById(R.id.editText);
        edittextproductnumber.setOnKeyListener(this);

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
        this.edittext = (EditText) findViewById(R.id.editText);
        this.edittext.setBackgroundColor(Color.WHITE);
        this.addressAndTime = (TextView) findViewById(R.id.textView3);
        addressAndTime.setVisibility(View.INVISIBLE);
        addressAndTime.setBackgroundColor(Color.WHITE);








    }

    Thread getRoute = new Thread(new Runnable() {

        @Override
        public void run() {
            try  {
                System.out.println(getDataFromUrl("http://10.0.2.2:1234/getRoute?origin=canberra&destination="+destination+"%20Canberra&option=Fastest"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });


    String getDataFromUrl(String demoIdUrl) {

        String result = null;
        int resCode;
        InputStream in;
        try {
            URL url = new URL(demoIdUrl);
            URLConnection urlConn = url.openConnection();


            HttpURLConnection httpConn = (HttpURLConnection) urlConn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            resCode = httpConn.getResponseCode();

            if (resCode == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        in, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                in.close();
                result = sb.toString();
            } else {
                System.out.println(resCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }




    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {

        // Listen to "Enter" key press
        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER))
        {

            destination = edittext.getText().toString();

            getRoute.start();

            try {
                getRoute.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println(raw_route);







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
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.


        LatLng sydney = new LatLng(-33.852, 151.211);
        googleMap.addMarker(new MarkerOptions().position(sydney)
                .title("Marker in Sydney"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        Polyline polyline1 = googleMap.addPolyline(new PolylineOptions()
                .addAll(Decoder.decode(raw_route)));
        polyline1.setColor(0xFF0060C0);



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

//    private void showCurrentPlace() {
//        if (gmap == null) {
//            return;
//        }
//
//        if (mLocationPermissionGranted) {
//            // Get the likely places - that is, the businesses and other points of interest that
//            // are the best match for the device's current location.
//            @SuppressWarnings("MissingPermission")
//            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
//                    .getCurrentPlace(mGoogleApiClient, null);
//            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
//                @Override
//                public void onResult(@NonNull PlaceLikelihoodBuffer likelyPlaces) {
////                    int i = 0;
////                    mLikelyPlaceNames = new String[mMaxEntries];
////                    mLikelyPlaceAddresses = new String[mMaxEntries];
////                    mLikelyPlaceAttributions = new String[mMaxEntries];
////                    mLikelyPlaceLatLngs = new LatLng[mMaxEntries];
////                    for (PlaceLikelihood placeLikelihood : likelyPlaces) {
////                        // Build a list of likely places to show the user. Max 5.
////                        mLikelyPlaceNames[i] = (String) placeLikelihood.getPlace().getName();
////                        mLikelyPlaceAddresses[i] = (String) placeLikelihood.getPlace().getAddress();
////                        mLikelyPlaceAttributions[i] = (String) placeLikelihood.getPlace()
////                                .getAttributions();
////                        mLikelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();
////
////                        i++;
////                        if (i > (mMaxEntries - 1)) {
////                            break;
////                        }
////                    }
////                    // Release the place likelihood buffer, to avoid memory leaks.
////                    likelyPlaces.release();
////
////                    // Show a dialog offering the user the list of likely places, and add a
////                    // marker at the selected place.
////                    openPlacesDialog();
//                }
//            });
//        } else {
//            // Add a default marker, because the user hasn't selected a place.
//            gmap.addMarker(new MarkerOptions()
//                    .title(getString(R.string.default_info_title))
//                    .position(mDefaultLocation)
//                    .snippet(getString(R.string.default_info_snippet)));
//        }
//
//
//    }

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


}
