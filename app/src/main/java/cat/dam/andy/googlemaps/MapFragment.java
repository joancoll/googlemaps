package cat.dam.andy.googlemaps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapFragment extends Fragment {
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 77; //numero indiferent i únic
    SupportMapFragment supportMapFragment;
    private long UPDATE_INTERVAL = 10000;  /* 10 segons */
    private long FASTEST_INTERVAL = 5000; /* 5 segons */
    private double DEFAULT_LAT= 42.1152668, DEFAULT_LONG=2.7656192; //Ubicació per defecte (Banyoles)
    private int MAP_ZOOM = 10; //ampliació de zoom al marcador (més gran, més zoom)
    private int MAP_LOCATION_ZOOM = 15; //ampliació de zoom al marcador ubicació
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location ubicacio;
    private Marker markerUbicacio;
    private Boolean ubicacioTrobada=false;
    private LocationCallback locationCallback;
    private TextView tv_latitud, tv_longitud;
    private Button btn_localitzar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inicialitza view
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        tv_latitud =  this.getActivity().findViewById(R.id.tv_latitud);
        tv_longitud = this.getActivity().findViewById(R.id.tv_longitud);
        btn_localitzar = this.getActivity().findViewById(R.id.btn_localitzar);
        btn_localitzar.setText("Esperant ubicació...");
        btn_localitzar.setEnabled(false);
        btn_localitzar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tePermisUbicacio() && ubicacioTrobada) {
                    mostrarPosicio(ubicacio,true);
                }
            }
        });

        //Initialitza fragment mapa
        supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_mapa);

        //Mapa asíncron
        supportMapFragment.getMapAsync((new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                //Quan es carrega el mapa
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL); //Representa un típic mapa de carretera amb noms de carrers i etiquetes
                /* MAP_TYPE_SATELLITE); //Representa una vista satèl·lit de l'àrea sense nom de carrers ni etiquetes
                   MAP_TYPE_TERRAIN); //Dades topogràfiques. El mapa inclou colors, línies de nivells i etiquetes, i perspectiva ombrejada. Alguns carrers i etiquetes poden també ser visibles.
                   MAP_TYPE_HYBRID); //Combina una vista de satèl·lit i la normal amb totes les etiquetes*/
                googleMap.getUiSettings().setZoomControlsEnabled(true); //mostrem botons zoom
                googleMap.getUiSettings().setZoomGesturesEnabled(true); //possibilitat d'ampliar amb dits
                googleMap.getUiSettings().setCompassEnabled(true); //mostrem bruixola
                // mMap.setTrafficEnabled(true); //podriem habilitar visió trànsit
                // Podem afegir marcadors
                LatLng latLngGirona = new LatLng(41.9802474, 2.78356);
                MarkerOptions markerOptionsGirona = new MarkerOptions().position(latLngGirona).title("Girona").snippet("Girona té un nucli jueu");
                markerOptionsGirona.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                googleMap.addMarker(markerOptionsGirona);
                LatLng latLngBesalu = new LatLng(42.1998706, 2.6890259);
                MarkerOptions markerOptionsBesalu = new MarkerOptions().position(latLngBesalu).title("Besalú").snippet("Besalú té un nucli jueu");
                markerOptionsBesalu.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                googleMap.addMarker(markerOptionsBesalu);
                LatLng latLngDefault = new LatLng(DEFAULT_LAT,DEFAULT_LONG);
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLngDefault)); //es situa a la posició per defecte
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(MAP_ZOOM)); //ampliació extra d'aproximació
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        // Quan es clica el mapa inicialitza el marcador a on ha clicat
                        MarkerOptions markerOptions = new MarkerOptions(); //podem canviar la icona o el color
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                        //googleMap.clear(); //esborrem tots els marcadors
                        googleMap.addMarker(markerOptions.position(latLng).title("Ha clicat aquí (LAT:" + String.format("%.4f", latLng.latitude) + " LONG:" + String.format("%.4f", latLng.longitude) + ")"));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng)); //es situa a la posició
                        googleMap.animateCamera(CameraUpdateFactory.zoomTo(MAP_ZOOM)); //ampliació extra d'aproximació
                        //
                    }
                });

            }
        }));
        if (tePermisUbicacio()) {
            //Inicialitza localització
            obteUbicacio();
        }

        //Retorna view
        return view;
    }

    private void obteUbicacio() {
        //Comprova permisos i inicialitza tasca d'ubicació
        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            tePermisUbicacio();
            return;
        }
        //Inicialitza l'objecte necessari per conèixer la ubicació
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.getContext());
        //Configura l'actualització de les peticions d'ubicació'
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(UPDATE_INTERVAL);
        //Aquest mètode estableix la velocitat en mil·lisegons en què l'aplicació prefereix rebre actualitzacions d'ubicació. Tingueu en compte que les actualitzacions d'ubicació poden ser una mica més ràpides o més lentes que aquesta velocitat per optimitzar l'ús de la bateria, o pot ser que no hi hagi actualitzacions (si el dispositiu no té connectivitat, per exemple).
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        //Aquest mètode estableix la taxa més ràpida en mil·lisegons en què la vostra aplicació pot gestionar les actualitzacions d'ubicació. A menys que la vostra aplicació es beneficiï de rebre actualitzacions més ràpidament que la taxa especificada a setInterval (), no cal que toqueu a aquest mètode.
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        /*
        PRIORITY_BALANCED_POWER_ACCURACY - Utilitzeu aquest paràmetre per sol·licitar la precisió de la ubicació a un bloc de la ciutat, que té una precisió aproximada de 100 metres. Es considera un nivell aproximat de precisió i és probable que consumeixi menys energia. Amb aquesta configuració, és probable que els serveis d’ubicació utilitzin el WiFi i el posicionament de la torre cel·lular. Tingueu en compte, però, que l'elecció del proveïdor d'ubicació depèn de molts altres factors, com ara quines fonts estan disponibles.
        PRIORITY_HIGH_ACCURACY - Utilitzeu aquesta configuració per sol·licitar la ubicació més precisa possible. Amb aquesta configuració, és més probable que els serveis d’ubicació utilitzin el GPS per determinar la ubicació.
        PRIORITY_LOW_POWER - Utilitzeu aquest paràmetre per sol·licitar una precisió a nivell de ciutat, que té una precisió d'aproximadament 10 quilòmetres. Es considera un nivell aproximat de precisió i és probable que consumeixi menys energia.
        PRIORITY_NO_POWER - Utilitzeu aquesta configuració si necessiteu un impacte insignificant en el consum d'energia, però voleu rebre actualitzacions d'ubicació quan estiguin disponibles. Amb aquesta configuració, l'aplicació no activa cap actualització d'ubicació, sinó que rep ubicacions activades per altres aplicacions.
         */
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        mostrarPosicio(location, false);
                        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                    }
                }
            }
        };
        // Si volem actualitzacions periodiques
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    public void mostrarPosicio(Location location, Boolean zoom) {
        //mostra posició
        tv_latitud.setText(String.format("%f",location.getLatitude()));
        tv_longitud.setText(String.format("%f",location.getLongitude()));
        //Sincronitza mapa
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                //Inicialitza Lat i Long
                LatLng latLng = new LatLng(location.getLatitude()
                        , location.getLongitude());
                //Crea el marcador
                MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Sóc aquí!");
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                if (ubicacioTrobada) {
                    markerUbicacio.remove(); //eliminem antic marcador si s'havia trobat Ubicació
                }
                else {
                    ubicacioTrobada=true; //cert per indicar que ja s'ha trobat una Ubicació
                }
                markerUbicacio=googleMap.addMarker(markerOptions);
                btn_localitzar.setText("Veure ubicació");
                btn_localitzar.setEnabled(true);
                ubicacio=location;
                if (zoom) { //en cas de prèmer botó o altres casos necessaris fem zoom
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng)); //es situa a la posició
                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(MAP_LOCATION_ZOOM)); //ampliació extra d'aproximació
                }
            }
        });
    }


    public boolean tePermisUbicacio() {
        if (ContextCompat.checkSelfPermission(this.getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this.getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this.getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                //Quan tingui permisos crida al mètode
                obteUbicacio();
            }
        }

    }
}