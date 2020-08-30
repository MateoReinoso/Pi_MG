package com.pm.pi_mg.activities.client;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieAnimationView;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseError;
import com.pm.pi_mg.R;
import com.pm.pi_mg.providers.GeofireProvider;

public class RequestDriverActivity extends AppCompatActivity {

    private LottieAnimationView mAnimation;
    private TextView mTextViewLookingFor;
    private Button mButtonCancelRequest;

    private GeofireProvider mGeofireProvider;

    private double mExtraOriginLat;
    private double mExtraOriginLng;
    private LatLng mOriginLatLng;

    private double mRadius = 0.1;
    private boolean mDriverFound = false;
    private String mIdDriverFound = "";
    private LatLng mDriverFoundLatLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_driver);
        mAnimation = findViewById(R.id.animation);
        mTextViewLookingFor = findViewById(R.id.textViewLookingFor);
        mButtonCancelRequest = findViewById(R.id.btnCancelRequest);

        mAnimation.playAnimation();

        mExtraOriginLat = getIntent().getDoubleExtra("origin_lat", 0);
        mExtraOriginLng = getIntent().getDoubleExtra("origin_lng", 0);

        mOriginLatLng = new LatLng(mExtraOriginLat, mExtraOriginLng);

        mGeofireProvider = new GeofireProvider();

        getClosesDriver();

    }

    private void getClosesDriver(){

        mGeofireProvider.getActiveDrivers(mOriginLatLng, mRadius).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!mDriverFound){
                    mDriverFound = true;
                    mIdDriverFound = key;
                    mDriverFoundLatLng = new LatLng(location.latitude, location.longitude);
                    mTextViewLookingFor.setText("Repartidor Encontrado\nEsperando Respuesta");
                    Log.d("DRIVER", "ID: "+mIdDriverFound);
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                //ingresa cuando termina la busqueda
                if (!mDriverFound){
                    mRadius = mRadius + 0.1f;
                    if (mRadius>3){
                        mTextViewLookingFor.setText("Repartidor no encontrado.");
                        return;
                    }else {
                        getClosesDriver();
                    }
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }
}