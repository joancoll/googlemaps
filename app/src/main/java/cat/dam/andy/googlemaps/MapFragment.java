package cat.dam.andy.googlemaps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


public class MapFragment extends Fragment {
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 77; //numero indiferent i únic
    SupportMapFragment supportMapFragment;
    private int MAP_ZOOM = 11; //ampliació de zoom al marcador (més gran, més zoom)
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location location;
    private LocationManager locationManager;
    private LocationRequest locationRequest;
    private long UPDATE_INTERVAL = 10000;  /* 10 seg */
    private long FASTEST_INTERVAL = 10000; /* 10 seg */
    private TextView tv_latitud, tv_longitud;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inicialitza view
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        TextView tv_latitud =  this.getActivity().findViewById(R.id.tv_latitud);
        TextView tv_longitud = this.getActivity().findViewById(R.id.tv_longitud);


        //Initialitza fragment mapa
        supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_mapa);


        //Mapa asíncron
        supportMapFragment.getMapAsync((new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                //Quan es carrega el mapa
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL); //Representa un típic mapa de carretera amb noms de carrers i etiquetes
   /* MAP_TYPE_SATELLITE); //Representa una vista satèl·lit de l'àrea sense nom de carrers ni etiquetes
    MAP_TYPE_TERRAIN); //Dades topogràfiques. El mapa inclou colors, línies de nivells i etiquetes, i perspectiva ombrejada.
      			     Alguns carrers i etiquetes poden també ser visibles.
    MAP_TYPE_HYBRID); //Combina una vista de satèl·lit i la normal amb totes les etiquetes*/
                googleMap.getUiSettings().setZoomControlsEnabled(true); //mostrem botons zoom
                googleMap.getUiSettings().setZoomGesturesEnabled(true); //possibilitat d'ampliar amb dits
                googleMap.getUiSettings().setCompassEnabled(true); //mostrem bruixola
                // mMap.setTrafficEnabled(true); //podriem habilitar visió trànsit
                // Podem afegir marcadors
                LatLng girona = new LatLng(41.9802474, 2.78356);
                MarkerOptions markerOptionsGirona = new MarkerOptions().position(girona).title("Girona").snippet("Girona té un nucli jueu");
                googleMap.addMarker(markerOptionsGirona);
                LatLng besalu = new LatLng(42.1998706, 2.6890259);
                MarkerOptions markerOptionsBesalu = new MarkerOptions().position(besalu).title("Besalú").snippet("Besalú té un nucli jueu");
                googleMap.addMarker(markerOptionsBesalu);
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        //Quan es clica el mapa
                        //inicalitza el marcador d'on ha clicat
                        MarkerOptions markerOptionsClic = new MarkerOptions(); //podem canviar la icona o el color
                        markerOptionsClic.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                        googleMap.clear(); //esborrem tots els marcadors
                        googleMap.addMarker(markerOptionsClic.position(latLng).title("Ha clicat aquí (LAT:" + String.format("%.4f", latLng.latitude) + " LONG:" + String.format("%.4f", latLng.longitude) + ")"));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng)); //es situa a la posició
                        googleMap.animateCamera(CameraUpdateFactory.zoomTo(MAP_ZOOM)); //ampliació extra d'aproximació
                        //
                    }
                });

            }
        }));

        //Inicialitza localització
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.getContext());
        //Comprova permís ubicació
        if (comprovaPermisUbicacio()) {
            getCurrentLocation();
        } else {
            //Si no hi ha permisos els demana
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }

        // Return view
        return view;
    }

    private void getCurrentLocation() {
        //inicialitza tasca de localització
        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            comprovaPermisUbicacio();
            return;
        }
        Task<Location> taskLocation = fusedLocationProviderClient.getLastLocation();
        taskLocation.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                //quan trobi localitzacio
                if (location != null) {
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
                            MarkerOptions markerOptionsSocAqui = new MarkerOptions().position(latLng).title("Sóc aquí!");
                            markerOptionsSocAqui.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng)); //es situa a la posició
                            googleMap.animateCamera(CameraUpdateFactory.zoomTo(MAP_ZOOM)); //ampliació extra d'aproximació
                            googleMap.addMarker(markerOptionsSocAqui);
                        }
                    });
                }

            }

        });
    }


    public boolean comprovaPermisUbicacio() {
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
                getCurrentLocation();
            }
        }

    }
}