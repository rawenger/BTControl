package edu.ucsb.ryanwenger.btcontrol;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import edu.ucsb.ryanwenger.btcontrol.bluetooth.BTDeviceManager;
import edu.ucsb.ryanwenger.btcontrol.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private static final int RC_HANDLE_LOCATION_PERM = 2; // Request code for Location Permission

    private static final int RC_HANDLE_BLUETOOTH_PERM = 3; // Request code for Bluetooth Permission

    private static final int RC_ENABLE_BT = 4;

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarDeviceListing.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_device_listing);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        checkBluetoothPermission();
        checkLocationPermission();

        switch (BTDeviceManager.getInstance().getBTStatus()) {
            case UNSUPPORTED:
                Snackbar.make(binding.getRoot(), R.string.capability_bluetooth_unsupported, Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", view -> {}).show();
                break;
            case DISABLED:
                Snackbar.make(binding.getRoot(), R.string.capability_bluetooth_disabled, Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", view -> {
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBtIntent, RC_ENABLE_BT);
                        })
                        .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.device_listing, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_device_listing);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getBaseContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            return;
        }

        final String[] permissions = new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION};

        if(!ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                && ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_LOCATION_PERM);
            return;
        }

        final Activity thisActivity = this;
        Snackbar.make(binding.getRoot(), R.string.permission_location_rationale, Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", view ->
                        ActivityCompat.requestPermissions(thisActivity,
                                permissions, RC_HANDLE_LOCATION_PERM)).show();
    }

    private void checkBluetoothPermission() {
        final String[] permissions = new String[] {
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
        };

        boolean hasPermissions = true;
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(getBaseContext(), perm)
                    != PackageManager.PERMISSION_GRANTED)
            {
                hasPermissions = false;
            }
        }

        if (hasPermissions)
            return;

        Log.w("RYANRYAN", "Camera permission is not granted. Requesting permission.");

        for (String perm : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, perm))
            {
                final Activity thisActivity = this;
                Snackbar.make(binding.getRoot(), R.string.permission_bluetooth_rationale,
                                Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", view ->
                                ActivityCompat.requestPermissions(thisActivity,
                                        permissions, RC_HANDLE_BLUETOOTH_PERM)).show();
                return;
            }
        }

        ActivityCompat.requestPermissions(this,
                permissions, RC_HANDLE_BLUETOOTH_PERM);
    }

    public void onSettings(MenuItem item) {
        // TODO: show settings view
    }
}