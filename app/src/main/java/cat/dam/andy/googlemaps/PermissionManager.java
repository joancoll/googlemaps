package cat.dam.andy.googlemaps;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class PermissionManager extends AppCompatActivity {
    public ActivityResultLauncher activityResultLauncher;

    // Constructor
    public PermissionManager(Context context, PermissionRequired permissionRequired) {
        // Members
        initPermissionLauncher(context, permissionRequired);
    }

    private void initPermissionLauncher(Context context, PermissionRequired permissionRequired) {
        //Inicialitza el launcher per demanar permisos
        activityResultLauncher = ((AppCompatActivity) context).registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    // Check if all permissions are granted
                    if (permissions.containsValue(false)) {
                        // Check every permission
                        for (String permission : permissions.keySet()){
                            if (ActivityCompat.shouldShowRequestPermissionRationale((AppCompatActivity) context,permission)){
                                // Permission denied
                                Toast.makeText(context, permissionRequired.getpermissionDeniedMessage(), Toast.LENGTH_LONG).show();
                            } else {
                                // Permission denied permanently
                                new AlertDialog.Builder(context)
                                        .setTitle("Permission denied")
                                        .setMessage(permissionRequired.getpermissionPermanentDeniedMessage())
                                        .setCancelable(true)
                                        .setPositiveButton("Ok", (dialogInterface, c) -> {
                                            //*************************************************
                                            // if user denied permanently the permissions,
                                            //  he should go to settings to granted the permissions
                                            //*************************************************
                                            Intent intent = new Intent();
                                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                                            intent.setData(uri);
                                            context.startActivity(intent);
                                        })
                                        .setNegativeButton("Cancel", (dialogInterface, c) -> {
                                            //*************************************************
                                            // if user denied permanently the permissions,
                                            //  he should go to settings to granted the permissions
                                            //*************************************************
                                            Intent intent = new Intent();
                                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                                            intent.setData(uri);
                                            context.startActivity(intent);
                                        })
                                        .show();
                            }
                        }
                    } else {
                        // Permission granted
                        Toast.makeText(context, permissionRequired.getPermissionGrantedMessage(), Toast.LENGTH_LONG).show();
                    }
////                    if (permissions.values().contains(false)) {
////                        // If at least one permission is denied
////                        if (ActivityCompat.shouldShowRequestPermissionRationale((AppCompatActivity) context, Manifest.permission.ACCESS_FINE_LOCATION) ||
////                                ActivityCompat.shouldShowRequestPermissionRationale((AppCompatActivity) context, Manifest.permission.ACCESS_COARSE_LOCATION)) {
////                            // Permission denied
////                            Toast.makeText(context, permissionRequired.getpermissionDeniedMessage(), Toast.LENGTH_SHORT).show();
////                        } else {
////                            // Permission denied permanently
////                            new AlertDialog.Builder(context)
////                                    .setTitle("Permission denied")
////                                    .setMessage(permissionRequired.getpermissionPermanentDeniedMessage())
////                                    .setCancelable(true)
////                                    .setPositiveButton("Ok", (dialogInterface, c) -> {
////                                        //*************************************************
////                                        // if user denied permanently the permissions,
////                                        //  he should go to settings to granted the permissions
////                                        //*************************************************
////                                        Intent intent = new Intent();
////                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
////                                        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
////                                        intent.setData(uri);
////                                        context.startActivity(intent);
////                                    })
////                                    .setNegativeButton("Cancel", (dialogInterface, c) -> {
////                                        //*************************************************
////                                        // if user denied permanently the permissions,
////                                        //  he should go to settings to granted the permissions
////                                        //*************************************************
////                                        Intent intent = new Intent();
////                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
////                                        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
////                                        intent.setData(uri);
////                                        context.startActivity(intent);
////                                    })
////                                    .create()
////                                    .show();
//                        }
//                    } else {
//                        // Permission granted
//                        Toast.makeText(context, permissionRequired.getPermissionGrantedMessage(), Toast.LENGTH_SHORT).show();
//                    }
                }
        );
    }

//        activityResultLauncher= registerForActivityResult(
//                new ActivityResultContracts.RequestPermission(),
//                isGranted -> {
//                    if (isGranted) {
//                        Toast.makeText(context, permissionRequired.getPermissionGrantedMessage(), Toast.LENGTH_SHORT).show();
//                    } else {
//                        if (shouldShowRequestPermissionRationale(permissionRequired.getPermission())) {
//                            Toast.makeText(context, permissionRequired.getpermissionExplanation(), Toast.LENGTH_SHORT).show();
//                        } else {
//                            new AlertDialog.Builder(this)
//                                    .setTitle("Permission denied")
//                                    .setMessage(permissionRequired.getpermissionPermanentDeniedMessage())
//                                    .setCancelable(true)
//                                    .setPositiveButton("Ok", (dialogInterface, c) -> {
//                                        //*************************************************
//                                        // if user denied permanently the permissions,
//                                        //  he should go to settings to granted the permissions
//                                        //*************************************************
//                                        Intent intent = new Intent();
//                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
//                                        intent.setData(uri);
//                                        this.startActivity(intent);
//                                    })
//                                    .setNegativeButton("Cancel", null)
//                                    .create()
//                                    .show();
//                        }
//                    }
//    });
//    }

    public boolean hasAllNeededPermissions(Context context, String[] permissions) {
        //comprova que tingui els permisos necessaris
        for (String permission : permissions) {
            if (!hasPermission(context, permission)) {
                askForPermission(context, permission);
                return false;
            }
        }
        return true;
    }

    private boolean hasPermission(Context context, String permission) {
        return(ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED);
        //return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED; //no funciona sense context en classe/fragment
    }


    public void askForPermission(Context context, String permission) {
        //Demana permís necessari
        Toast.makeText(context, "Permission required", Toast.LENGTH_LONG).show();
        //activityResultLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
        activityResultLauncher.launch(new String[]{permission});
    }
}
