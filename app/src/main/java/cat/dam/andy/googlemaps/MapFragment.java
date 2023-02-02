package cat.dam.andy.googlemaps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Locale;


public class MapFragment extends Fragment {
    //Members
    private final long UPDATE_INTERVAL = 10000;  /* 10 segons */
    private final long FASTEST_INTERVAL = 5000; /* 5 segons */
    private final double DEFAULT_LAT = 42.1152668, DEFAULT_LONG = 2.7656192; //Ubicació per defecte (Banyoles)
    private final int MAP_ZOOM = 10; //ampliació de zoom al marcador (més gran, més zoom)
    private final int MAP_LOCATION_ZOOM = 15; //ampliació de zoom al marcador ubicació
    ArrayList<PermissionData> permissionsRequired=new ArrayList<>();
    private SupportMapFragment supportMapFragment;
    private Location myLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker locationMarker;
    private Boolean locationFound = false;
    private LocationCallback locationCallback;
    private TextView tv_latitude, tv_longitude;
    private Button btn_find_me;
    private PermissionManager permissionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        initViews();
        initPermissions();
        initMap();
        initListeners();
        showMap();
        //Retorna view del fragment
        return view;

    }

    private void initViews() {
        tv_latitude = requireActivity().findViewById(R.id.tv_latitude);
        tv_longitude = requireActivity().findViewById(R.id.tv_longitude);
        btn_find_me = requireActivity().findViewById(R.id.btn_find_me);
    }

    private void initPermissions() {
        //TO DO: CONFIGURE ALL NECESSARY PERMISSIONS

        //BEGIN
        permissionsRequired.add(new PermissionData(Manifest.permission.ACCESS_FINE_LOCATION,
                getString(R.string.locationPermissionNeeded),
                "",
                getString(R.string.locationPermissionThanks),
                getString(R.string.locationPermissionSettings)));

        //END
        //DON'T DELETE == call permission manager ==
        permissionManager= new PermissionManager(requireActivity(), permissionsRequired);
    }

    private void initMap() {
        //Initialitza fragment mapa
        supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);

        //Mapa asíncron
        if (supportMapFragment != null) {
            supportMapFragment.getMapAsync((googleMap -> {
                //Paràmetres del mapa
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL); //Representa un típic mapa de carretera amb noms de carrers i etiquetes
            /* MAP_TYPE_SATELLITE); //Representa una vista satèl·lit de l'àrea sense nom de carrers ni etiquetes
               MAP_TYPE_TERRAIN); //Dades topogràfiques. El mapa inclou colors, línies de nivells i etiquetes, i perspectiva ombrejada. Alguns carrers i etiquetes poden també ser visibles.
               MAP_TYPE_HYBRID); //Combina una vista de satèl·lit i la normal amb totes les etiquetes*/
                googleMap.getUiSettings().setZoomControlsEnabled(true); //mostrem botons zoom
                googleMap.getUiSettings().setZoomGesturesEnabled(true); //possibilitat d'ampliar amb dits
                googleMap.getUiSettings().setCompassEnabled(true); //mostrem bruixola
                // googleMap.setTrafficEnabled(true); //podriem habilitar visió trànsit
                // Podem afegir marcadors
                LatLng latLngGirona = new LatLng(41.9802474, 2.78356);
                MarkerOptions markerOptionsGirona = new MarkerOptions().position(latLngGirona).title("Girona").snippet(getString(R.string.jewishGirona));
                markerOptionsGirona.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                googleMap.addMarker(markerOptionsGirona);
                LatLng latLngBesalu = new LatLng(42.1998706, 2.6890259);
                MarkerOptions markerOptionsBesalu = new MarkerOptions().position(latLngBesalu).title("Besalú").snippet(getString(R.string.jewishBesalu));
                markerOptionsBesalu.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                googleMap.addMarker(markerOptionsBesalu);
                LatLng latLngDefault = new LatLng(DEFAULT_LAT, DEFAULT_LONG);
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLngDefault)); //es situa a la posició per defecte
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(MAP_ZOOM)); //ampliació extra d'aproximació
                // Podem afegir listeners al mapa
                googleMap.setOnMapClickListener(latLng -> {
                    // Quan es clica el mapa inicialitza el marcador a on ha clicat
                    MarkerOptions markerOptions = new MarkerOptions(); //podem canviar la icona o el color
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    //googleMap.clear(); //esborrem tots els marcadors
                    googleMap.addMarker(markerOptions.position(latLng).title(getString(R.string.clickedHere)+" (LAT:" + String.format(Locale.getDefault(),"%.4f", latLng.latitude) + " LONG:" + String.format(Locale.getDefault(),"%.4f", latLng.longitude) + ")"));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng)); //es situa a la posició
                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(MAP_ZOOM)); //ampliació extra d'aproximació
                    //
                });
            }));
        }
    }

    private void initListeners() {
        btn_find_me.setOnClickListener(v -> showMap());
    }


    @SuppressLint("MissingPermission")
    private void getLocation() {
        // Obté la posició actual
        // mentre cerca la localització no es permet clicar de nou el botó
        btn_find_me.setText(R.string.waitingLocation);
        btn_find_me.setEnabled(false);
        //Inicialitza l'objecte necessari per conèixer la ubicació
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.requireContext());
        //Configura l'actualització de les peticions d'ubicació
        LocationRequest.Builder locationRequest= new LocationRequest.Builder(UPDATE_INTERVAL);
        //Aquest mètode estableix la velocitat en mil·lisegons en què l'aplicació prefereix rebre actualitzacions d'ubicació. Tingueu en compte que les actualitzacions d'ubicació poden ser una mica més ràpides o més lentes que aquesta velocitat per optimitzar l'ús de la bateria, o pot ser que no hi hagi actualitzacions (si el dispositiu no té connectivitat, per exemple).
        locationRequest.setMinUpdateIntervalMillis(FASTEST_INTERVAL);
        //Aquest mètode estableix la taxa més ràpida en mil·lisegons en què la vostra aplicació pot gestionar les actualitzacions d'ubicació gràcies a peticions d'altres apps. A menys que la vostra aplicació es beneficiï de rebre actualitzacions més ràpidament que la taxa especificada a setInterval (), no cal que toqueu a aquest mètode.
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
         /*
        PRIORITY_BALANCED_POWER_ACCURACY - Utilitzeu aquest paràmetre per sol·licitar la precisió de la ubicació a un bloc de la ciutat, que té una precisió aproximada de 100 metres. Es considera un nivell aproximat de precisió i és probable que consumeixi menys energia. Amb aquesta configuració, és probable que els serveis d’ubicació utilitzin el WiFi i el posicionament de la torre cel·lular. Tingueu en compte, però, que l'elecció del proveïdor d'ubicació depèn de molts altres factors, com ara quines fonts estan disponibles.
        PRIORITY_HIGH_ACCURACY - Utilitzeu aquesta configuració per sol·licitar la ubicació més precisa possible. Amb aquesta configuració, és més probable que els serveis d’ubicació utilitzin el GPS per determinar la ubicació i consumeixi molta més energia.
        PRIORITY_LOW_POWER - Utilitzeu aquest paràmetre per sol·licitar una precisió a nivell de ciutat, que té una precisió d'aproximadament 10 quilòmetres. Es considera un nivell aproximat de precisió i és probable que consumeixi menys energia.
        PRIORITY_NO_POWER - Utilitzeu aquesta configuració si necessiteu un impacte insignificant en el consum d'energia, però voleu rebre actualitzacions d'ubicació quan estiguin disponibles. Amb aquesta configuració, l'aplicació no activa cap actualització d'ubicació, sinó que rep ubicacions activades per altres aplicacions.
         */
        //Crea un objecte de petició d'ubicació
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        showLocation(location, false);
                        // Si volem aturar les actualitzacions, podem fer-ho amb aquesta línia
                        // fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                    }
                }
            }
        };
        // Farem actualitzacions periodiques sempre i quan tinguem permisos, sinó els demanem i retornem
        if (!permissionManager.hasAllNeededPermissions(requireActivity(), permissionsRequired))
        { //Si manquen permisos els demanem
            permissionManager.askForPermissions(requireActivity(), permissionManager.getRejectedPermissions(requireActivity(), permissionsRequired));
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest.build(), locationCallback, Looper.getMainLooper());
    }

    public void showLocation(Location location, Boolean zoom) {
        //mostra posició
        tv_latitude.setText(String.format(Locale.getDefault(),"%.4f",location.getLatitude()));
        tv_longitude.setText(String.format(Locale.getDefault(),"%.4f",location.getLongitude()));
        //Sincronitza mapa
        supportMapFragment.getMapAsync(googleMap -> {
            //Inicialitza Lat i Long
            LatLng latLng = new LatLng(location.getLatitude()
                    , location.getLongitude());
            //Crea el marcador
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(getString(R.string.currentLocation));
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
            if (locationFound) {
                locationMarker.remove(); //eliminem antic marcador si s'havia trobat Ubicació
            }
            else {
                locationFound =true; //cert per indicar que ja s'ha trobat una Ubicació
            }
            locationMarker=googleMap.addMarker(markerOptions);
            btn_find_me.setText(R.string.get_location);
            btn_find_me.setEnabled(true);
            myLocation=location;
            if (zoom) { //en cas de prèmer botó o altres casos necessaris fem zoom
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng)); //es situa a la posició
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(MAP_LOCATION_ZOOM)); //ampliació extra d'aproximació
            }
        });
    }

    private void showMap() {
        if (!permissionManager.hasAllNeededPermissions(requireActivity(), permissionsRequired))
        { //Si manquen permisos els demanem
            permissionManager.askForPermissions(requireActivity(), permissionManager.getRejectedPermissions(requireActivity(), permissionsRequired));
        } else {
            //Si tenim permisos demanem la posició o la mostrem si ja la tenim
            if (!locationFound) {
                //Demanem la posició (per defecte ja es mostrarà el mapa)
                getLocation();
            } else {
                //mostrem localització
                showLocation(myLocation, true);
            }
        }
    }

}