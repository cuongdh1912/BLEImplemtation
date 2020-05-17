package com.example.blecentral;

import android.os.ParcelUuid;

import java.util.UUID;

/**
 * Constants for use in the Bluetooth Advertisements sample
 */
public class Constants {

    /**
     * UUID identified with this app - set as Service UUID for BLE Advertisements.
     *
     * Bluetooth requires a certain format for UUIDs associated with Services.
     * The official specification can be found here:
     * {@link https://www.bluetooth.org/en-us/specification/assigned-numbers/service-discovery}
     */
    public static final UUID ServiceUUID = UUID
            .fromString("c876a6c4-9817-11ea-bb37-0242ac130002");
    public static final UUID CharacteristicUUID = UUID
            .fromString("16de7828-9818-11ea-bb37-0242ac130002");
    public static final int REQUEST_ENABLE_BT = 1;

}
