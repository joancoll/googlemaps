package cat.dam.andy.googlemaps;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Inicialitza fragment
        Fragment mapFragment = new MapFragment();
        //Obre fragment al frame del layout
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.map_frame, mapFragment)
                .commit();
    }

}