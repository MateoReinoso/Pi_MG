package com.pm.pi_mg.activities.client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.pm.pi_mg.R;
import com.pm.pi_mg.activities.driver.MapDriverBookingActivity;
import com.pm.pi_mg.providers.AuthProvider;
import com.pm.pi_mg.providers.ClientBookingProvider;
import com.pm.pi_mg.providers.GeofireProvider;
import com.pm.pi_mg.providers.GoogleApiProvider;
import com.pm.pi_mg.providers.TokenProvider;
import com.pm.pi_mg.utils.DecodePoints;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapClientBookingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private AuthProvider mAuthProvider;

    private GeofireProvider mGeofireProvider;
    private TokenProvider mTokenProvider;
    private ClientBookingProvider mClientBookingProvider;


    private Marker mMarkerDriver;
    private boolean mIsFirstTime = true;

    private PlacesClient mPlaces;

    private String mOrigin;
    private LatLng mOriginLatLng;

    private String mDestination;
    private LatLng mDestinationLatLng;

    private TextView mTextViewClientBooking;
    private TextView mTextViewEmailClientBooking;
    private TextView mTextViewDestinationClientBooking;

    private LatLng mDriverLatLng;

    private GoogleApiProvider mGoogleApiProvider;
    private List<LatLng> mPolylineList;
    private PolylineOptions mPolylineOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_client_booking);

        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("drivers_working");
        mTokenProvider = new TokenProvider();
        mClientBookingProvider = new ClientBookingProvider();
        mGoogleApiProvider = new GoogleApiProvider(MapClientBookingActivity.this);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }

        mTextViewClientBooking = findViewById(R.id.textViewDriverBooking);
        mTextViewEmailClientBooking = findViewById(R.id.textViewEmailDriverBooking);
        mTextViewDestinationClientBooking = findViewById(R.id.textViewDestinationDriverBooking);

        getClientBooking();
    }

    private void getClientBooking() {
        mClientBookingProvider.getClientBooking(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String destination = dataSnapshot.child("destination").getValue().toString();
                    String origin = dataSnapshot.child("origin").getValue().toString();
                    String idDriver = dataSnapshot.child("idDriver").getValue().toString();
                    double destinationLat = Double.parseDouble(dataSnapshot.child("destinationLat").getValue().toString());
                    double destinationLng = Double.parseDouble(dataSnapshot.child("destinationLng").getValue().toString());
                    double originLat = Double.parseDouble(dataSnapshot.child("originLat").getValue().toString());
                    double originLng = Double.parseDouble(dataSnapshot.child("originLng").getValue().toString());
                    mOriginLatLng = new LatLng(originLat, originLng);
                    mDestinationLatLng = new LatLng(destinationLat,destinationLng);
                    mTextViewDestinationClientBooking.setText("Lugar de entrega: " +destination);
                    mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Entregar Aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
                    getDriverLocation(idDriver);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getDriverLocation(String idDriver) {
        mGeofireProvider.getDriverLocation(idDriver).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    double lat = Double.parseDouble(dataSnapshot.child("0").getValue().toString());
                    double lng = Double.parseDouble(dataSnapshot.child("1").getValue().toString());
                    mDriverLatLng = new LatLng(lat, lng);
                    if(mMarkerDriver != null){
                        mMarkerDriver.remove();
                    }
                    mMarkerDriver = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lng))
                            .title("Tu conductor")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons8_cami_n_30)));
                    if (mIsFirstTime){
                        mIsFirstTime = false;
                        drawRoute();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void drawRoute(){
        mGoogleApiProvider.getDirections(mDriverLatLng, mDestinationLatLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject route = jsonArray.getJSONObject(0);
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    String points = polylines.getString("points");
                    mPolylineList = DecodePoints.decodePoly(points);
                    mPolylineOptions = new PolylineOptions();
                    mPolylineOptions.color(Color.DKGRAY);
                    mPolylineOptions.width(13f);
                    mPolylineOptions.startCap(new SquareCap());
                    mPolylineOptions.jointType(JointType.ROUND);
                    mPolylineOptions.addAll(mPolylineList);
                    mMap.addPolyline(mPolylineOptions);
                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");
                    String distanceText = distance.getString("text");
                    String durationText = duration.getString("text");
                }catch (Exception e){
                    Log.d("Error", "Error encontrado" + e.getMessage());
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

    }

}