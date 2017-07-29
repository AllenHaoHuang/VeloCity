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
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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


public class MainMenuActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnKeyListener {

    EditText edittext;
    TextView addressAndTime;
    String origin = "Canberra";
    String destination = "";
    String raw_route = "vzhvEslfm[w@jPw@bNS`EMtBGz@MpAGXKVqCrFiD|GUh@g@bBMn@UzAw@vKQrAO|@iAbD{DnKYp@w@|A_AvAgAjAoAbA_MdHqBbA{Aj@_B^o@HkFTgGTe@DkATgA`@aAn@}@x@u@bAm@jAc@pAYlA{CzSK|@Iv@E`@Et@EnCAbAFxBb@rFTvCBp@DbBBbB?dEKfEQlD]jDc@|Cq@`D{@~CcAvCiAhCe@z@q@dAwBpC}CzDq@x@gClC_@b@}@fAuBxC]d@uAbCkAjC_@t@Uj@e@lAc@pAiAbEyBjJ{Nbn@cAxEy@zE_DtRMp@a@vBCPIEOAC[a@k@c@R[NY?W@sKBiXC{EF@p@ChGE|@Ed@Qv@QPK`@Wl@i@`AAXG`@E`@JzADXO@QFuE|@e@LMG}AZSs@GW?}H";

    boolean mLocationPermissionGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        EditText edittextproductnumber = (EditText) findViewById(R.id.editText);
        edittextproductnumber.setOnKeyListener(this);

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        // TODO Auto-generated method stub

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
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        LatLng sydney = new LatLng(-33.852, 151.211);
        googleMap.addMarker(new MarkerOptions().position(sydney)
                .title("Marker in Sydney"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

       /* Polyline polyline1 = googleMap.addPolyline(new PolylineOptions()
                .add(
                        new LatLng(-35.016, 143.322),
                        new LatLng(-34.747, 145.592),
                        new LatLng(-34.364, 147.891),
                        new LatLng(-33.501, 150.217),
                        new LatLng(-32.306, 149.248),
                        new LatLng(-32.491, 147.309)));*/

        Polyline polyline1 = googleMap.addPolyline(new PolylineOptions()
                .addAll(Decoder.decode(raw_route)));
        polyline1.setColor(0xFF0060C0);



    }


}
