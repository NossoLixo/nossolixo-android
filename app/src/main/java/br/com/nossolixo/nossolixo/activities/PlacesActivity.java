package br.com.nossolixo.nossolixo.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import br.com.nossolixo.nossolixo.R;
import br.com.nossolixo.nossolixo.models.Place;
import br.com.nossolixo.nossolixo.services.PlaceService;
import br.com.nossolixo.nossolixo.services.ServiceGenerator;
import br.com.nossolixo.nossolixo.utils.PermissionUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlacesActivity extends AppCompatActivity
        implements
        GoogleMap.OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            animateToMyLocation();
        } else if (mMap != null) {
            PermissionUtils.requestPermission(this,
                    LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions,
                grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            enableMyLocation();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
        setupMap();

        PlaceService client = ServiceGenerator.createService(PlaceService.class);
        Call<List<Place>> call = client.listPlaces();
        call.enqueue(new Callback<List<Place>>() {
            @Override
            public void onResponse(Call<List<Place>> call, Response<List<Place>> response) {
                if (response.isSuccessful()) {
                    List<Place> places = response.body();
                    for (Place place : places) {
                        mMap.addMarker(new MarkerOptions()
                                .position(place.getLatLng())
                                .title(place.getName()));
                    }
                } else {
                    Log.d("Error", String.valueOf(response.raw()));
                }
            }

            @Override
            public void onFailure(Call<List<Place>> call, Throwable t) {
                Log.d("Error", t.getMessage());
            }
        });
    }

    @Override
    public boolean onMyLocationButtonClick() {
        animateToMyLocation();
        return false;
    }

    private void animateToMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String provider = locationManager.getBestProvider(criteria, true);
        Location mostRecentLocation = locationManager.getLastKnownLocation(provider);
        LatLng myLocation = new LatLng(mostRecentLocation.getLatitude(),
                mostRecentLocation.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 12));
    }

    private void setupMap() {
        UiSettings mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setRotateGesturesEnabled(true);
    }
}
