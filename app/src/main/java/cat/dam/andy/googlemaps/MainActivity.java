package cat.dam.andy.googlemaps;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private static final String TAG = "MainActivity";
    private TextView tv_latitud;
    private TextView tv_longitud;
    private GoogleApiClient googleApiClient;
    private Location location;
    private LocationManager locationManager;
    private LocationRequest locationRequest;
    private long UPDATE_INTERVAL = 10000;  /* 10 seg */
    private long FASTEST_INTERVAL = 10000; /* 10 seg */
    private int MAP_ZOOM = 11; //ampliació de zoom al marcador (més gran, més zoom)
    private LatLng latLng; //per guardar latitud, longitud
    MarkerOptions markerOptions; //per opcions marcador mapa
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 77;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (comprovaPermisUbicacio()) { // comprova el permis d'ubicació es troba actiu en el dispositiu)
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            //Crea el client API de Google per accedir als mapes
            crearClientApiGoogle();
            // Obté suport pel fragment del mapa per ser notificat quan mapa estigui llest (OnMapReady).
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            tv_latitud = (TextView) findViewById((R.id.tv_latitud));
            tv_longitud = (TextView) findViewById((R.id.tv_longitud));
            comprovarUbicacio();
        }
    }

    /**
     * Prepara el mapa un cop es troba disponible
     * Aquest mètode callback es crida quan el mapa està llest per ser utilitzat
     * Aquí podem afegir-hi marcadors o línies, listeners o moure la càmera
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL); //Representa un típic mapa de carretera amb noms de carrers i etiquetes
   /* MAP_TYPE_SATELLITE); //Representa una vista satèl·lit de l'àrea sense nom de carrers ni etiquetes
    MAP_TYPE_TERRAIN); //Dades topogràfiques. El mapa inclou colors, línies de nivells i etiquetes, i perspectiva ombrejada.
      			     Alguns carrers i etiquetes poden també ser visibles.
    MAP_TYPE_HYBRID); //Combina una vista de satèl·lit i la normal amb totes les etiquetes*/
        mMap.getUiSettings().setZoomControlsEnabled(true); //mostrem botons zoom
        mMap.getUiSettings().setZoomGesturesEnabled(true); //possibilitat d'ampliar amb dits
        mMap.getUiSettings().setCompassEnabled(true); //mostrem bruixola
        // mMap.setTrafficEnabled(true); //podriem habilitar visió trànsit
        // Podem afegir marcadors
        LatLng girona = new LatLng(41.9802474, 2.78356);
        markerOptions = new MarkerOptions().position(girona).title("Girona").snippet("Girona té un nucli jueu");
        mMap.addMarker(markerOptions);
        LatLng besalu = new LatLng(42.1998706, 2.6890259);
        markerOptions = new MarkerOptions().position(besalu).title("Besalú").snippet("Besalú té un nucli jueu");
        mMap.addMarker(markerOptions);
        if (latLng != null) {
            markerOptions = new MarkerOptions(); //podem canviar la icona o el color
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            mMap.addMarker(markerOptions.position(latLng).title("Vostè es troba aquí"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng)); //es situa a la posició
            mMap.animateCamera(CameraUpdateFactory.zoomTo(MAP_ZOOM)); //ampliació extra d'aproximació
        }
    }

    protected void iniciarActualitzacionsUbicacio() {
        // Crea les peticions d'ubicació en en interval determinat
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        // Configura les actualitzacions de la ubicació si aquesta està activada correctament
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,
                locationRequest, this);
        Log.d("Mapa: petició ubicació", "--->>>>");
    }

    @Override
    public void onLocationChanged(Location location) {
        String msg = "Ubicació actualitzada: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        tv_latitud.setText(String.valueOf(location.getLatitude()));
        tv_longitud.setText(String.valueOf(location.getLongitude()));
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        // Es crea un objecte LatLng amb la nova posició per actualitzar el mapa
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        //Demana suport pel fragment del mapa per ser notificat quan mapa estigui actualitzat i llest (OnMapReady).
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    protected synchronized void crearClientApiGoogle() {
        //Crea el client API de Google per accedir als mapes
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this) //Suport per quan el dispositiu es connecti/desconnecti.
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        iniciarActualitzacionsUbicacio();//actualitzarà periodicament ubicació
        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location == null) {
            Toast.makeText(this, "No es detecta la ubicació", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Mapa: Connexió suspesa");//log
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Mapa: Error de connexió: " + connectionResult.getErrorCode()); //log
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    private boolean comprovarUbicacio() {
        if (!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Activi la ubicació")
                .setMessage("La configuració de la seva ubicació es troba desactivada.\nSi us plau, activeu-la " +
                        "per utilitzar aquesta aplicació")
                .setPositiveButton("Configuració ubicació", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    public boolean comprovaPermisUbicacio() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    private boolean isLocationEnabled() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
}