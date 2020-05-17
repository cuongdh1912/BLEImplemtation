package com.example.blelibrary;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BLEService {
    // status of all states after create a bluetooth adapter
    public enum BluetoothAdapterStatus {
        ENABLED,
        ADVERTISEMENTS_NOT_SUPPORTED,
        REQUEST_BLUETOOTH,
        BLUETOOTH_NOT_SUPPORTED
    }
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private Handler mHandler;
    private Activity context;
    private ScanCallback scanCallback;
    /**
     * Stops scanning after 10 seconds.
     */
    private static final long SCAN_PERIOD = 10000;

    public BLEService(Activity context) {
        this.context = context;
        mHandler = new Handler();
    }

    /**
     * Create a bluetooth adapter
     * @return BluetoothAdapterStatus
     */
    public BluetoothAdapterStatus createBluetoothAdapter() {
        // request coarse & fine location for skd version >= M
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int PERMISSION_REQUEST_COARSE_LOCATION = 100;
            context.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_COARSE_LOCATION);
        }
        mBluetoothAdapter = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        // Is Bluetooth supported on this device?
        if (mBluetoothAdapter != null) {
            // Is Bluetooth turned on?
            if (mBluetoothAdapter.isEnabled()) {
                // Are Bluetooth Advertisements supported on this device?
                if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {
                    // Everything is supported and enabled, load the fragments.
                    mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
                    return BluetoothAdapterStatus.ENABLED;
                } else {
                    // Bluetooth Advertisements are not supported.
                    return BluetoothAdapterStatus.ADVERTISEMENTS_NOT_SUPPORTED;
                }
            } else {
                // Prompt user to turn on Bluetooth (logic continues in onActivityResult()).
                return BluetoothAdapterStatus.REQUEST_BLUETOOTH;
            }
        } else {
            // Bluetooth is not supported.
            return BluetoothAdapterStatus.BLUETOOTH_NOT_SUPPORTED;
        }
    }

    /**
     * Start scanning for BLE Advertisements (& set it up to stop after a set period of time).
     */
    public void startScanning(ScanCallback scanCallback) {
        this.scanCallback = scanCallback;
        // Will stop the scanning after a set time.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScanning();
            }
        }, SCAN_PERIOD);

        // Kick off a new scan.
        mBluetoothLeScanner.startScan(buildScanFilters(), buildScanSettings(), scanCallback);
        String toastText = context.getString(R.string.bleservice_scan_start_toast) + " "
                + TimeUnit.SECONDS.convert(SCAN_PERIOD, TimeUnit.MILLISECONDS) + " "
                + context.getString(R.string.bleservice_seconds);
        Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();
    }

    /**
     * Stop scanning for BLE Advertisements.
     */
    private void stopScanning() {
        // Stop the scan, wipe the callback.
        mBluetoothLeScanner.stopScan(scanCallback);
    }
    /**
     * Return a {@link ScanSettings} object set to use low power (to preserve battery life).
     */
    private ScanSettings buildScanSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        return builder.build();
    }

    /**
     * Return a List of {@link ScanFilter} objects to filter by Service UUID.
     */
    private List<ScanFilter> buildScanFilters() {
        List<ScanFilter> scanFilters = new ArrayList<>();

        ScanFilter.Builder builder = new ScanFilter.Builder();
        scanFilters.add(builder.build());
        return scanFilters;
    }
    public boolean isMultipleAdvertisementSupported() {
        return mBluetoothAdapter.isMultipleAdvertisementSupported();
    }
}
