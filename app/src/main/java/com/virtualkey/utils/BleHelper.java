package com.virtualkey.utils;

import android.bluetooth.BluetoothGattCharacteristic;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.scan.BleScanRuleConfig;

/**
 * @Author: GaiQS
 * @E-mail:gaiqs@sina.com
 * @Creation Date: 2018-08-08
 * @Date Modified: 2018-08-08
 * @Describe:
 * @param:<T>
 * @FIXME
 */
public class BleHelper {
    /**
     * 写入数据
     *
     * @param bleDevice
     * @param uuid_service
     * @param uuid_write
     * @param data
     * @param callback
     */
    public static void write(BleDevice bleDevice, String uuid_service, String uuid_write, byte[] data, BleWriteCallback callback) {
        BleManager.getInstance().write(bleDevice, uuid_service, uuid_write, data, callback);
    }

    public static void connectMAC(String mac, BleGattCallback bleGattCallback) {
        BleManager.getInstance().connect(mac, bleGattCallback);
    }

    public static void connectBle(BleDevice bleDevice, BleGattCallback bleGattCallback) {
        BleManager.getInstance().connect(bleDevice, bleGattCallback);
    }

    public static void bleNotify(BleDevice bleDevice, BluetoothGattCharacteristic mNotifyChara, BleNotifyCallback bleNotifyCallback) {
        BleManager.getInstance().notify(bleDevice, mNotifyChara.getService().getUuid().toString(), mNotifyChara.getUuid().toString(), bleNotifyCallback);
    }

    public static void initScanRule() {
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
//                .setServiceUuids(serviceUuids)      // 只扫描指定的服务的设备，可选
                .setDeviceName(true, "AutoLinkTBox")   // 只扫描指定广播名的设备，可选
//                .setDeviceMac(mac)                  // 只扫描指定mac的设备，可选
                .setAutoConnect(false)      // 连接时的autoConnect参数，可选，默认false
                .setScanTimeOut(10000)              // 扫描超时时间，可选，默认10秒
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

}
