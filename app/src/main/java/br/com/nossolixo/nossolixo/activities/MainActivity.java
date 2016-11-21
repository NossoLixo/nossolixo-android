package br.com.nossolixo.nossolixo.activities;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;

import br.com.nossolixo.nossolixo.R;
import br.com.nossolixo.nossolixo.fragments.PlaceDetailFragment;
import br.com.nossolixo.nossolixo.helpers.ProgressDialogHelper;
import br.com.nossolixo.nossolixo.helpers.ToastHelper;
import br.com.nossolixo.nossolixo.models.Category;
import br.com.nossolixo.nossolixo.models.Filters.Filter;
import br.com.nossolixo.nossolixo.models.Place;
import br.com.nossolixo.nossolixo.services.CategoryService;
import br.com.nossolixo.nossolixo.services.FilterService;
import br.com.nossolixo.nossolixo.services.PlaceService;
import br.com.nossolixo.nossolixo.services.ServiceGenerator;
import br.com.nossolixo.nossolixo.utils.PermissionUtils;
import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleMap.OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private NavigationView navigationView;
    private HashMap<Marker, String> markers = new HashMap<>();
    private FilterService filterService;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        filterService = new FilterService(this);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        buildGoogleApiClient();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
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
                Manifest.permission.ACCESS_COARSE_LOCATION)) {
            enableMyLocation();
            animateToMyLocation();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(this);
        setupMap();
        bindMarkers();
        loadCategories();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    private void setupMap() {
        UiSettings mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setRotateGesturesEnabled(true);
    }

    private void loadPlaces() {
        PlaceService client = ServiceGenerator.createService(PlaceService.class);
        Call<List<Place>> call = client.listPlaces();
        buildMapPlaces(call);
    }

    private void loadPlaces(String category) {
        PlaceService client = ServiceGenerator.createService(PlaceService.class);
        Call<List<Place>> call = client.listPlacesByCategory(category);
        buildMapPlaces(call);
    }

    private void buildMapPlaces(Call<List<Place>> call) {
        final MainActivity context = this;
        final ProgressDialogHelper progressDialog = new ProgressDialogHelper(context,
                getResources().getString(R.string.loading));
        progressDialog.show();

        call.enqueue(new Callback<List<Place>>() {
            @Override
            public void onResponse(Call<List<Place>> call, Response<List<Place>> response) {
                if (response.isSuccessful()) {
                    List<Place> places = response.body();
                    mMap.clear();
                    for (Place place : places) {
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(place.getLatLng())
                                .title(place.getName()));
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher));
                        markers.put(marker, place.getId());
                    }
                } else {
                    ToastHelper.show(context, R.string.places_load_error);
                    Log.d("Error", String.valueOf(response.raw()));
                }
                if (progressDialog.isShowing()) {
                    progressDialog.hide();
                }
            }

            @Override
            public void onFailure(Call<List<Place>> call, Throwable t) {
                Log.d("Error", t.getMessage());
                ToastHelper.show(context, R.string.places_load_error);
                if (progressDialog.isShowing()) {
                    progressDialog.hide();
                }
            }
        });
    }

    private void bindMarkers() {
        final MainActivity context = this;

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Intent intent = new Intent(context, PlaceDetailActivity.class);
                intent.putExtra(PlaceDetailFragment.ARG_ITEM_ID, markers.get(marker));
                context.startActivity(intent);
                return true;
            }
        });
    }

    private void loadCategories() {
        final MainActivity context = this;

        CategoryService client = ServiceGenerator.createService(CategoryService.class);
        Call<List<Category>> call = client.listCategories();
        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful()) {
                    final List<Category> categories = response.body();

                    Menu menu = navigationView.getMenu();
                    SubMenu subMenu = menu.addSubMenu(R.string.nav_categories);

                    final MenuItem resetFilter = subMenu.add(R.string.reset_filter);
                    resetFilter.setIcon(R.drawable.ic_highlight_off_black_24dp);
                    resetFilter.setVisible(false);
                    resetFilter.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            resetFilter(resetFilter);
                            return false;
                        }
                    });

                    Category currentFilter = currentFilter();
                    if (currentFilter == null) {
                        loadPlaces();
                    } else {
                        setFilter(currentFilter, resetFilter);
                        ToastHelper.show(context, getString(R.string.saved_filter, currentFilter.getName()));
                    }

                    for (final Category category : categories) {
                        MenuItem item = subMenu.add(category.getName());
                        item.setIcon(R.drawable.ic_place_black_24dp);
                        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                setFilter(category, resetFilter);
                                return false;
                            }
                        });
                    }

                    MenuItem about = menu.add(R.string.about);
                    about.setIcon(R.drawable.ic_error_black_24dp);
                    about.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            Intent intent = new Intent(context, AboutActivity.class);
                            context.startActivity(intent);
                            return false;
                        }
                    });
                } else {
                    ToastHelper.show(context, R.string.categories_load_error);
                    Log.d("Error", String.valueOf(response.raw()));
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                ToastHelper.show(context, R.string.categories_load_error);
                Log.d("Error", t.getMessage());
            }
        });
    }

    private Category currentFilter() {
        SQLiteDatabase db = filterService.getReadableDatabase();
        String[] projection = {Filter._ID, Filter.COLUMN_NAME_CATEGORY_ID, Filter.COLUMN_NAME_CATEGORY_NAME};
        String selection = "";
        String[] selectionArgs = {};

        Cursor c = db.query(Filter.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
        if (c.getCount() == 0) {
            return null;
        }
        c.moveToFirst();
        Category category = new Category();
        category.setId(c.getString(c.getColumnIndexOrThrow(Filter.COLUMN_NAME_CATEGORY_ID)));
        category.setName(c.getString(c.getColumnIndexOrThrow(Filter.COLUMN_NAME_CATEGORY_NAME)));
        return category;
    }

    private void setFilter(Category category, MenuItem resetFilter) {
        SQLiteDatabase db = filterService.getWritableDatabase();
        loadPlaces(category.getId());
        resetFilter.setTitle(getResources().getString(R.string.reset_filter, category.getName()));
        resetFilter.setVisible(true);
        db.delete(Filter.TABLE_NAME, "", new String[]{});
        ContentValues values = new ContentValues();
        values.put(Filter.COLUMN_NAME_CATEGORY_ID, category.getId());
        values.put(Filter.COLUMN_NAME_CATEGORY_NAME, category.getName());
        db.insert(Filter.TABLE_NAME, null, values);
    }

    private void resetFilter(MenuItem resetFilter) {
        loadPlaces();
        resetFilter.setVisible(false);
        SQLiteDatabase db = filterService.getWritableDatabase();
        db.delete(Filter.TABLE_NAME, "", new String[]{});
    }

    private void animateToMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            PermissionUtils.requestPermission(this,
                    LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_COARSE_LOCATION);
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            LatLng myLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 12));
        } else {
            notifyEnableLocation();
        }
    }

    private void notifyEnableLocation() {
        NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_laucher_notification)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                    .setContentTitle(getResources().getString(R.string.no_location_detected))
                    .setContentText(getResources().getString(R.string.enable_your_location))
                    .setAutoCancel(true);

        Intent resultIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

        PendingIntent resultPendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            );

        mBuilder.setContentIntent(resultPendingIntent);
        int mNotificationId = 001;
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (mMap == null) {
            return;
        }
        enableMyLocation();
        animateToMyLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
}
