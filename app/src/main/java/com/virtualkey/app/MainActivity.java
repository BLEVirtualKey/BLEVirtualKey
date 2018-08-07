package com.virtualkey.app;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.gemalto.virtualkey.api.virtualkey.Virtualkey;
import com.gemalto.virtualkey.taadmin.IVirtualkeyAdminNotification;
import com.gemalto.virtualkey.taadmin.TaAdmin;
import com.gto.tee.agentlibrary.Utils;
import com.gto.tee.agentlibrary.proxy.AgentResultCodes;
import com.gto.tee.agentlibrary.proxy.ProgressState;
import com.virtualkey.app.adapter.DeviceAdapter;
import com.virtualkey.app.comm.ObserverManager;
import com.virtualkey.app.operation.VckActivity;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleMtuChangedCallback;
import com.clj.fastble.callback.BleRssiCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.virtualkey.app.utils.ToastUtils;

import org.spongycastle.util.encoders.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback, IVirtualkeyAdminNotification {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION = 2;

    private LinearLayout layout_setting;
    private TextView txt_setting;
    private Button btn_scan;
    private EditText et_name, et_mac, et_uuid;
    private Switch sw_auto;
    private ImageView img_loading;

    private Animation operatingAnim;
    private DeviceAdapter mDeviceAdapter;
    private ProgressDialog progressDialog;

    private TaAdmin mTaAdmin;
    private Virtualkey mVk;
    private Operation mOperation;
    private byte[] mConsent;

    enum Operation {
        Install(1), UPDATE(2),
        UNENROLL(3);
        private final int operation;
        Operation(int operation) { this.operation = operation; }
        public int getValue() { return operation; }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        App.getLoggerHelper().i("===================> onCreate: VIN:" + new String(Constant.CvkInfo).substring(0, 17));
        App.getLoggerHelper().i("===================> onCreate: PDID:" + new String(Constant.CvkInfo).substring(17, 49));
        App.getLoggerHelper().i("===================> onCreate: UUID:" + new String(Constant.CvkInfo).substring(49, 85));
        App.getLoggerHelper().i("===================> onCreate: BLE Name:" + new String(Constant.CvkInfo).substring(85, 97));

        // Get BLE instance
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(false)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);

        // Set device TEE Type
        TaAdmin.setDeviceTeeType(Utils.TEE_TYPE.GEMALTO_SOFTTEE, App.getContext());
        // Check app required permissions with the user
        checkPermissions();
        // Get VirtualKey instance
        mVk = Virtualkey.getInstance(App.getContext());

        // TODO: 向后台请求CvkInfo,获取BLE Name & Service UUID
        et_name.setText(new String(Constant.CvkInfo).substring(85, 97));
//        et_uuid.setText(new String(Constant.CvkInfo).substring(49, 85));
    }

    @Override
    protected void onResume() {
        super.onResume();
        showConnectedDevice();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BleManager.getInstance().disconnectAllDevice();
        BleManager.getInstance().destroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan:
                if (btn_scan.getText().equals(getString(R.string.start_scan))) {
                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (!bluetoothAdapter.isEnabled()) {
                        Toast.makeText(this, getString(R.string.please_open_blue), Toast.LENGTH_LONG).show();
                    } else {
                        setScanRule();
                        startScan();
                    }
                } else if (btn_scan.getText().equals(getString(R.string.stop_scan))) {
                    BleManager.getInstance().cancelScan();
                }
                break;
            case R.id.txt_setting:
                if (layout_setting.getVisibility() == View.VISIBLE) {
                    layout_setting.setVisibility(View.GONE);
                    txt_setting.setText(getString(R.string.expand_search_settings));
                } else {
                    layout_setting.setVisibility(View.VISIBLE);
                    txt_setting.setText(getString(R.string.retrieve_search_settings));
                }
                break;
        }
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btn_scan = (Button) findViewById(R.id.btn_scan);
        btn_scan.setText(getString(R.string.start_scan));
        btn_scan.setOnClickListener(this);

        et_name = (EditText) findViewById(R.id.et_name);
        et_mac = (EditText) findViewById(R.id.et_mac);
        et_uuid = (EditText) findViewById(R.id.et_uuid);
        sw_auto = (Switch) findViewById(R.id.sw_auto);

        layout_setting = (LinearLayout) findViewById(R.id.layout_setting);
        txt_setting = (TextView) findViewById(R.id.txt_setting);
        txt_setting.setOnClickListener(this);
        layout_setting.setVisibility(View.GONE);
        txt_setting.setText(getString(R.string.expand_search_settings));

        img_loading = (ImageView) findViewById(R.id.img_loading);
        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        operatingAnim.setInterpolator(new LinearInterpolator());
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);

        mDeviceAdapter = new DeviceAdapter(this);
        mDeviceAdapter.setOnDeviceClickListener(new DeviceAdapter.OnDeviceClickListener() {
            @Override
            public void onConnect(BleDevice bleDevice) {
                if (!BleManager.getInstance().isConnected(bleDevice)) {
                    BleManager.getInstance().cancelScan();
                    connect(bleDevice);
                }
            }

            @Override
            public void onDisConnect(final BleDevice bleDevice) {
                if (BleManager.getInstance().isConnected(bleDevice)) {
                    BleManager.getInstance().disconnect(bleDevice);
                }
            }

            @Override
            public void onDetail(BleDevice bleDevice) {
                if (BleManager.getInstance().isConnected(bleDevice)) {
                    Intent intent = new Intent(MainActivity.this, VckActivity.class);
                    intent.putExtra(Constant.KEY_DATA, bleDevice);
                    startActivity(intent);
                }
            }
        });
        ListView listView_device = (ListView) findViewById(R.id.list_device);
        listView_device.setAdapter(mDeviceAdapter);
    }

    private void showConnectedDevice() {
        List<BleDevice> deviceList = BleManager.getInstance().getAllConnectedDevice();
        mDeviceAdapter.clearConnectedDevice();
        for (BleDevice bleDevice : deviceList) {
            mDeviceAdapter.addDevice(bleDevice);
        }
        mDeviceAdapter.notifyDataSetChanged();
    }

    private void setScanRule() {
        String[] uuids;
        String str_uuid = et_uuid.getText().toString();
        if (TextUtils.isEmpty(str_uuid)) {
            uuids = null;
        } else {
            uuids = str_uuid.split(",");
        }
        UUID[] serviceUuids = null;
        if (uuids != null && uuids.length > 0) {
            serviceUuids = new UUID[uuids.length];
            for (int i = 0; i < uuids.length; i++) {
                String name = uuids[i];
                String[] components = name.split("-");
                if (components.length != 5) {
                    serviceUuids[i] = null;
                } else {
                    serviceUuids[i] = UUID.fromString(uuids[i]);
                }
            }
        }

        String[] names;
        String str_name = et_name.getText().toString();
        if (TextUtils.isEmpty(str_name)) {
            names = null;
        } else {
            names = str_name.split(",");
        }

        String mac = et_mac.getText().toString();

        boolean isAutoConnect = sw_auto.isChecked();

        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setServiceUuids(serviceUuids)      // 只扫描指定的服务的设备，可选
                .setDeviceName(true, names)   // 只扫描指定广播名的设备，可选
                .setDeviceMac(mac)                  // 只扫描指定mac的设备，可选
                .setAutoConnect(isAutoConnect)      // 连接时的autoConnect参数，可选，默认false
                .setScanTimeOut(10000)              // 扫描超时时间，可选，默认10秒
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    private void startScan() {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                mDeviceAdapter.clearScanDevice();
                mDeviceAdapter.notifyDataSetChanged();
                img_loading.startAnimation(operatingAnim);
                img_loading.setVisibility(View.VISIBLE);
                btn_scan.setText(getString(R.string.stop_scan));
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                img_loading.clearAnimation();
                img_loading.setVisibility(View.INVISIBLE);
                btn_scan.setText(getString(R.string.start_scan));
                switch (mDeviceAdapter.getCount()) {
                    case 0:
                        btn_scan.performClick();
                        break;
                    case 1:
                        BleDevice bleDevice = mDeviceAdapter.getItem(0);
                        if (!BleManager.getInstance().isConnected(bleDevice)) {
                            connect(bleDevice);
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void connect(final BleDevice bleDevice) {
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                progressDialog.setTitle("开始配对");
                progressDialog.show();
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                img_loading.clearAnimation();
                img_loading.setVisibility(View.INVISIBLE);
                btn_scan.setText(getString(R.string.start_scan));
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, getString(R.string.connect_fail), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                progressDialog.dismiss();
                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
                if (BleManager.getInstance().isConnected(bleDevice)) {
                    setMtu(bleDevice, 512);

                    Intent intent = new Intent(MainActivity.this, VckActivity.class);
                    intent.putExtra(Constant.KEY_DATA, bleDevice);
                    startActivity(intent);
                }
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                progressDialog.dismiss();

                mDeviceAdapter.removeDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();

                if (isActiveDisConnected) {
                    Toast.makeText(MainActivity.this, getString(R.string.active_disconnected), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.disconnected), Toast.LENGTH_LONG).show();
                    ObserverManager.getInstance().notifyObserver(bleDevice);
                }
            }
        });
    }

    private void readRssi(BleDevice bleDevice) {
        BleManager.getInstance().readRssi(bleDevice, new BleRssiCallback() {
            @Override
            public void onRssiFailure(BleException exception) {
                App.getLoggerHelper().i("onRssiFailure" + exception.toString());
            }

            @Override
            public void onRssiSuccess(int rssi) {
                App.getLoggerHelper().i("onRssiSuccess: " + rssi);
            }
        });
    }

    private void setMtu(BleDevice bleDevice, int mtu) {
        BleManager.getInstance().setMtu(bleDevice, mtu, new BleMtuChangedCallback() {
            @Override
            public void onSetMTUFailure(BleException exception) {
                App.getLoggerHelper().i("onsetMTUFailure" + exception.toString());
            }

            @Override
            public void onMtuChanged(int mtu) {
                App.getLoggerHelper().i("onMtuChanged: " + mtu);
            }
        });
    }

    @Override
    public final void onRequestPermissionsResult(int requestCode,
                                                 @NonNull String[] permissions,
                                                 @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        App.getLoggerHelper().i("clivelau onRequestPermissionsResult: called");
        for (String permission : permissions) {
            switch (permission) {
                case Manifest.permission.READ_PHONE_STATE:
                    if (grantResults.length > 0) {
                        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            mTaAdmin = TaAdmin.getInstance(this);
                            installTa();
                        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                            onPermissionGranted(permission);
                        }
                    }
                    break;
//                case Manifest.permission.ACCESS_FINE_LOCATION:
//                    if (grantResults.length > 0) {
//                        if (grantResults[1] == PackageManager.PERMISSION_DENIED) {
//                            onPermissionGranted(permission);
//                        }
//                    }
//                    break;
            }
        }
    }

    private void checkPermissions() {
        String[] permissions = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION};
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_CODE_PERMISSION);
        }
    }

    private void onPermissionGranted(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.notifyTitle)
                            .setMessage(R.string.gpsNotifyMsg)
                            .setNegativeButton(R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                            .setPositiveButton(R.string.setting,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);
                                        }
                                    })

                            .setCancelable(false)
                            .show();
                } else {
                    App.getLoggerHelper().i("clivelau onPermissionGranted(ACCESS_FINE_LOCATION): called");
                }
                break;
            case Manifest.permission.READ_PHONE_STATE:
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                    new AlertDialog.Builder(this)
                            .setMessage("Phone state permission is required to retrieve some system information to generate unique identifier for your device")
                            .setNegativeButton(R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                            .setPositiveButton(R.string.setting,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                            startActivity(intent);
                                        }
                                    })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setCancelable(false)
                            .show();
                } else {
                    App.getLoggerHelper().i("clivelau onPermissionGranted(READ_PHONE_STATE): called");
                    mTaAdmin = TaAdmin.getInstance(this);
                    installTa();
                }
                break;
        }
    }

    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        return ((locationManager != null) && (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_GPS) {
            if (checkGPSIsOpen()) {
                setScanRule();
                startScan();
            }
        }
    }

    /***********************************************************/

    @Override
    public void notifyEnd(int resultCode) {
        App.getLoggerHelper().i("Session ended (" + resultCode + ")");

        if (resultCode == AgentResultCodes.SUCCESS) {
            Dialog dialog = null;
            progressDialog.dismiss();
            if (mOperation == Operation.Install) {
                dialog = new AlertDialog.Builder(this)
                        .setMessage("Service installed successfully.")
                        .setPositiveButton("OK", null).create();
                btn_scan.performClick();
            } else if (mOperation == Operation.UPDATE) {
                dialog = new AlertDialog.Builder(this)
                        .setMessage("Service updated successfully.")
                        .setPositiveButton("OK", null).create();
            } else if (mOperation == Operation.UNENROLL) {
                dialog = new AlertDialog.Builder(this)
                        .setMessage("Service uninstallation completed.")
                        .setPositiveButton("OK", null).create();
            }

            if (dialog != null) {
                dialog.show();
            }
        }
    }

    @Override
    public void notifyProgress(ProgressState progressState) {
        App.getLoggerHelper().i("(" + progressState + ")");
    }

    private void installTa() {
        App.getLoggerHelper().i("Install Session started");
        if (mTaAdmin.isTAInstalled()) {
            App.getLoggerHelper().i("TA Service has already been installed");
            ToastUtils.showToast(this, "TA service has already been installed");
            btn_scan.performClick();
        } else {
            mOperation = Operation.Install;

            int serviceId = mTaAdmin.getServiceID();
            App.getLoggerHelper().i("serviceId:" + serviceId);
            byte[] hashOfDeviceId = mTaAdmin.getDeviceID();
            App.getLoggerHelper().i("hashOfDeviceId:" + hashOfDeviceId);

            try {
                mConsent =generateConsentData(serviceId, hashOfDeviceId, ConsentUtil.CONSENT_USAGE.ENROLLMENT);
                if (mConsent != null) {
                    try {
                        App.getLoggerHelper().i("Excute installTA");
                        progressDialog.setTitle("Installing TA service");
                        progressDialog.show();
                        mTaAdmin.installTA(mConsent, this);
                        ToastUtils.showToast(this, "Succeed to install TA service");
                    } catch (RuntimeException ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void uninstallTa() {
        mOperation = Operation.UNENROLL;
        if (!mTaAdmin.isTAInstalled()) {
            ToastUtils.showToast(this, "No TA service installed");
        } else {
            progressDialog.setTitle("Uninstalling TA service");
            progressDialog.show();
            mTaAdmin.unInstallTA(this);
            ToastUtils.showToast(this, "Succeed to uninstall TA service");
        }
    }

    /**
     * Return calculated consent based on service id, device id
     * @param  serviceId		ID of service
     * @param  teeId		    ID of the device
     * @param  consentUsage  Usage of the consent
     * returns generated consent
     */
    public byte[] generateConsentData(int serviceId, byte []teeId , ConsentUtil.CONSENT_USAGE consentUsage) throws Exception {
        final String PRIVATE_KEY_FILE = "teeConsentPrivKey.pem";
        final int PRIVATE_KEY_OFFSET = 28;
        final int PRIVATE_KEY_LENGTH = 1650;
        final String PRIVATE_KEY_STRING;
        try {
            AssetManager assetManager = getAssets();
            InputStream input = assetManager.open(PRIVATE_KEY_FILE);
            int size = input.available();
            if (size <= 0) {
                return null;
            }
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();
            // byte buffer into a string
            PRIVATE_KEY_STRING = new String(buffer, PRIVATE_KEY_OFFSET, PRIVATE_KEY_LENGTH);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

        long timeDiff = 1000 * 300;
        long timeMilis = new Date().getTime() + timeDiff;
        boolean validSignature = true;

        // private key
        byte[] keyArray = Base64.decode(PRIVATE_KEY_STRING);
        PKCS8EncodedKeySpec keySpec1 = new PKCS8EncodedKeySpec(keyArray);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privKey = kf.generatePrivate(keySpec1);

        int teeServiceId = serviceId;//5;
        byte[] staticVal = new byte[5];
        Calendar cal = Calendar.getInstance();//Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(timeMilis);

        byte[] opaqueData = null;
        try {
            opaqueData = ConsentUtil.getOpaqueData(staticVal, cal.getTime(), consentUsage);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        byte[] result = ConsentUtil.getConsentData(privKey, teeId, teeServiceId, opaqueData);
        if(!validSignature && result.length > 13) {
            result[10] = 0x00;
            result[11] = 0x00;
            result[12] = 0x00;
        }

        return result;
    }
}
