/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.blecentral;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.ListFragment;
import com.example.blelibrary.BLEService;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Scans for Bluetooth Low Energy Advertisements matching a filter and displays them to the user.
 */
public class ScannerFragment extends ListFragment {
    private static final String TAG = ScannerFragment.class.getSimpleName();
    private ScanCallback mScanCallback = new BLEScanCallback();
    private ScanResultAdapter mAdapter;
    private BLEService bleService;
    /**
     * Must be called after object creation by MainActivity.
     *
     * @param bleService the local BLEService from library
     */
    public void setBluetoothAdapter(BLEService bleService) {
        this.bleService = bleService;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        // Use getActivity().getApplicationContext() instead of just getActivity() because this
        // object lives in a fragment and needs to be kept separate from the Activity lifecycle.
        mAdapter = new ScanResultAdapter(getActivity().getApplicationContext(),
                LayoutInflater.from(getActivity()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        setListAdapter(mAdapter);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListView().setDivider(null);
        getListView().setDividerHeight(0);

        setEmptyText(getString(R.string.empty_list));
        // Trigger refresh on app's 1st load
        bleService.startScanning(mScanCallback);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.scanner_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.refresh) {// disconnect & remove all item in listOfGatt
            bleService.startScanning(mScanCallback);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Custom ScanCallback object - adds to adapter on success, displays error on failure.
     */
    private class BLEScanCallback extends ScanCallback {
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);

            for (ScanResult result : results) {
                mAdapter.add(result);
            }
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            mAdapter.add(result);
            mAdapter.notifyDataSetChanged();
            connectGATT(result.getDevice());
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Toast.makeText(getActivity(), "Scan failed with error: " + errorCode, Toast.LENGTH_LONG)
                    .show();
        }
    }

    /**
     * Connect to gatt via device
     * @param device is a outside bluetooth device
     */
    private void connectGATT(BluetoothDevice device) {
        if (device != null) { // if device existed
            Log.d(TAG, "Start connecting" + device.getAddress());
            device.connectGatt(getContext(), true, bluetoothGattCallback);
        }
    }

    /**
     * A callback of connecting device with gatt
     */
    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Successful GATT " + gatt.getDevice().getAddress());
                writeCharacteristic(gatt);
                return;
            }
            if (status == BluetoothGatt.GATT_FAILURE) {
                Log.e(TAG, "Connection failed for id: " + gatt.getDevice().getAddress());
            } else {
                Log.w(TAG, "Oh no, gatt failed " + status + " retrying");
            }
        }
    };

    /**
     * Send data to peripheral device
     * @param gatt bluetooth gatt
     */
    private void writeCharacteristic(BluetoothGatt gatt) {
        if (gatt == null) {
            return;
        }
        //Simply triggers a discoverService method, which in return triggers onServiceDiscovered, which writes data into the characteristic
        BluetoothGattService bleGateService = gatt.getService(Constants.ServiceUUID);
        if (bleGateService != null) {
            // create BluetoothGattCharacteristic
            BluetoothGattCharacteristic myGattChar = bleGateService.getCharacteristic(Constants.CharacteristicUUID);
            if (myGattChar != null) {
                // do the loop of sending data to other devices on a thread
                writeCharacteristicOnThread(myGattChar, gatt);
            }
        }
    }

    /**
     * Thread of sending characteristic to other devices
     * @param myGattChar gatt characteristic
     * @param gatt bluetooth gatt
     */
    private void writeCharacteristicOnThread(BluetoothGattCharacteristic myGattChar, BluetoothGatt gatt) {
        final BluetoothGattCharacteristic characteristic = myGattChar;
        final BluetoothGatt bluetoothGatt = gatt;
        Thread thread = new Thread() {
            // run on thread
            @Override
            public void run() {
                try {
                    // send RED characteristic
                    characteristic.setValue("RED");
                    bluetoothGatt.writeCharacteristic(characteristic);
                    sleep(1000);
                    // send GREEN characteristic
                    characteristic.setValue("GREEN");
                    bluetoothGatt.writeCharacteristic(characteristic);
                    BluetoothDevice device = bluetoothGatt.getDevice();
                    // disconnect gatt
                    bluetoothGatt.disconnect();
                    // start the process again by calling a new creation
                    connectGATT(device);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        // start thread
        thread.start();
    }
}
