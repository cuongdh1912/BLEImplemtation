package com.example.blecentral;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blelibrary.BLEService;

public class MainActivity extends FragmentActivity {
    BLEService bleService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getActionBar().setTitle(R.string.main_title);
        // construct bleService
        bleService = new BLEService(this);
        if (savedInstanceState == null) {
            BLEService.BluetoothAdapterStatus bleAdapterStatus = bleService.createBluetoothAdapter();
            switch (bleAdapterStatus) {
                case ENABLED:
                    // Everything is supported and enabled, load the fragments.
                    setupFragments();
                case ADVERTISEMENTS_NOT_SUPPORTED:
                    // Bluetooth Advertisements are not supported.
                    showErrorText(R.string.bt_ads_not_supported);
                case REQUEST_BLUETOOTH:
                    // Prompt user to turn on Bluetooth (logic continues in onActivityResult()).
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    // this function will trigger "protected void onActivityResult(int requestCode, int resultCode, Intent data)" after completes
                    startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
                case BLUETOOTH_NOT_SUPPORTED:
                    // Bluetooth is not supported.
                    showErrorText(R.string.bt_not_supported);
            }
//            mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE))
//                    .getAdapter();
//            // Is Bluetooth supported on this device?
//            if (mBluetoothAdapter != null) {
//                // Is Bluetooth turned on?
//                if (mBluetoothAdapter.isEnabled()) {
//                    // Are Bluetooth Advertisements supported on this device?
//                    if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {
//                        // Everything is supported and enabled, load the fragments.
//                        setupFragments();
//                    } else {
//                        // Bluetooth Advertisements are not supported.
//                        showErrorText(R.string.bt_ads_not_supported);
//                    }
//                } else {
//                    // Prompt user to turn on Bluetooth (logic continues in onActivityResult()).
//                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                    // this function will trigger "protected void onActivityResult(int requestCode, int resultCode, Intent data)" after completes
//                    startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
//                }
//            } else {
//                // Bluetooth is not supported.
//                showErrorText(R.string.bt_not_supported);
//            }
//            // request coarse & fine location for skd version >= M
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                int PERMISSION_REQUEST_COARSE_LOCATION = 100;
//                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
//                        PERMISSION_REQUEST_COARSE_LOCATION);
//            }
        }
    }
    // triggered after request bluetooth
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    // Bluetooth is now Enabled, are Bluetooth Advertisements supported on
                    // this device?
                    if (bleService.isMultipleAdvertisementSupported()) {
                        // Everything is supported and enabled, load the fragments.
                        setupFragments();
                    } else {
                        // Bluetooth Advertisements are not supported.
                        Toast.makeText(this, R.string.bt_ads_not_supported, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // User declined to enable Bluetooth, exit the app.
                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    finish();
                }

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
    // add fragment to show list of device
    private void setupFragments() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        ScannerFragment scannerFragment = new ScannerFragment();
        // Fragments can't access system services directly, so pass it the BluetoothAdapter
        scannerFragment.setBluetoothAdapter(bleService);
        transaction.replace(R.id.scanner_fragment_container, scannerFragment);
        transaction.commit();
        clearErrorText();
    }
    private void showErrorText(int messageId) {
        TextView view = (TextView) findViewById(R.id.error_textview);
        view.setText(getString(messageId));
    }
    private void clearErrorText() {
        TextView view = (TextView) findViewById(R.id.error_textview);
        view.setText("");
    }
}
