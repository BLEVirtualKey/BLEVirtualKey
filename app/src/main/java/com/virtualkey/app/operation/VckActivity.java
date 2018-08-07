package com.virtualkey.app.operation;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.gemalto.virtualkey.api.virtualkey.Virtualkey;
import com.gemalto.virtualkey.api.virtualkey.VirtualkeyException;
import com.gemalto.virtualkey.taadmin.TaAdmin;
import com.virtualkey.app.App;
import com.virtualkey.app.Constant;
import com.virtualkey.app.R;
import com.virtualkey.app.TAFlow;
import com.virtualkey.app.comm.Observer;
import com.virtualkey.app.comm.ObserverManager;
import com.virtualkey.app.utils.ToastUtils;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleRssiCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;

import org.spongycastle.util.encoders.Hex;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class VckActivity extends AppCompatActivity implements Observer {
    private BleDevice mBleDevice;
    private BluetoothGattService mBluetoothGattService;
    private BluetoothGattCharacteristic mCharacteristic;
    private BluetoothGattCharacteristic mWrChara;
    private int mCharaProp;
    private Toolbar mToolbar;
    private ProgressDialog mProgressDialog;
    private TextView mTvBleIsConn, mTvBleSs, mTvBlePos, mTvBleDist;
    private ImageButton mIbEngine, mIbDoorlock, mIbAirconditon, mIbTrunk;
    private Disposable mDisposable;
    private int mRssi;

    private TaAdmin mTaAdmin;
    private Virtualkey mVk;
    private byte[] mTRnd = new byte[8];
    private byte[] mSeId = new byte[16];
    private byte[] mSeTrnd = new byte[28];
    private byte[] mCCrypto = new byte[102];
    private byte[] mTCrypto = new byte[102];
    private byte[] mTeeId = new byte[16];
    private byte[] mTaId = new byte[16];
    private byte[] mKicc = new byte[32];
    private byte[] mKifd = new byte[32];
    private static final boolean OnlineMode = true;

    public static byte [] bootstrapId = new byte[4];
    public static byte [] bootstrapVersion = new byte[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_ck);
        initView();
        initData();
        showData();
        ObserverManager.getInstance().addObserver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BleManager.getInstance().clearCharacterCallback(mBleDevice);
        ObserverManager.getInstance().deleteObserver(this);

        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }

    @Override
    public void disConnected(BleDevice device) {
        if (device != null && mBleDevice != null && device.getKey().equals(mBleDevice.getKey())) {
            finish();
        }
    }

    private void initData() {
        mBleDevice = getIntent().getParcelableExtra(Constant.KEY_DATA);
        if (mBleDevice == null)
            finish();
        if (mBleDevice != null) {
            new Handler().postDelayed(() ->
                            BleManager.getInstance().write(
                                    mBleDevice,
                                    mWrChara.getService().getUuid().toString(),
                                    mWrChara.getUuid().toString(),
                                    Constant.REQ_SE_TRND,
                                    new BleWriteCallback() {
                                        @Override
                                        public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
//                                            mTvBleSs.setText("身份：已认证");
//                                            if (mProgressDialog.isShowing())
//                                                mProgressDialog.dismiss();
                                            mProgressDialog.setTitle("请求SEID & TRND");
                                        }

                                        @Override
                                        public void onWriteFailure(final BleException exception) {
                                            ToastUtils.toastShort(exception.toString());
                                            mTvBleSs.setText("身份：未认证");
                                            if (mProgressDialog.isShowing())
                                                mProgressDialog.dismiss();
                                        }
                                    })
                    , 1500);
            readRssiInterval(200);
        }

        mVk = Virtualkey.getInstance(App.getContext());
        mTaAdmin = TaAdmin.getInstance(App.getContext());

        if (!mTaAdmin.isTAInstalled()) {
            App.getLoggerHelper().e("Please install the TA service first");
            ToastUtils.showToast(this, "Please install the TA service first");
        } else {
            mTaAdmin.setUserData("1234");

            try {
                int result = mVk.getBootstrapIdVersion(bootstrapId, bootstrapVersion);
                int bsId = ((bootstrapId[3] & 0xFF) << 24) | ((bootstrapId[2] & 0xFF) << 16)
                        | ((bootstrapId[1] & 0xFF) << 8) | (bootstrapId[0] & 0xFF);

                int bsVersion = ((bootstrapVersion[3] & 0xFF) << 24) | ((bootstrapVersion[2] & 0xFF) << 16)
                        | ((bootstrapVersion[1] & 0xFF) << 8) | (bootstrapVersion[0] & 0xFF);
                App.getLoggerHelper().e("bootstrapId = " + TAFlow.bytesToHex(bootstrapId));
                App.getLoggerHelper().e("bootstrapVersion = " + TAFlow.bytesToHex(bootstrapVersion));

                App.getLoggerHelper().e("bootstrapId: " + bsId + " bootstrapVersion: " + bsVersion);
            }
            catch (VirtualkeyException e) {
                e.printStackTrace();
                App.getLoggerHelper().e("bootstrapId failed: " + e.getMessage());
            }
            App.getLoggerHelper().e("Compute KT begain ");
            byte[] KT = TAFlow.computeKT(mTaAdmin);
            App.getLoggerHelper().e("Kt = " +  TAFlow.bytesToHex(KT));
            App.getLoggerHelper().e("Compute KT finished ");

            String ret = "0000FFFF";

            App.getLoggerHelper().i("====== loadCVK ======");
            App.getLoggerHelper().i("Dowinloading VK");
            if (OnlineMode) {
                ret = TAFlow.loadCVK_kt(mVk, Constant.VIN, Constant.CvkInfo, Constant.Enc_CVK, TAFlow.computeKT(mTaAdmin));
            } else {
                ret = TAFlow.loadCVK(mVk, Constant.VIN, Constant.CvkInfo, Constant.Enc_CVK);
            }
            App.getLoggerHelper().i("loadCVK TA Response = " + ret);
            if (!ret.equals("00000000")) {
                App.showAlertDialog(this, "loadCVK failed");
                App.getLoggerHelper().e("loadCVK failed");
            } else {
                if (mProgressDialog.isShowing())
                    mProgressDialog.setTitle("成功下载VCK");
                App.getLoggerHelper().i("loadCVK success");
            }
        }
    }

    private byte[] requestVckInfo() {
        String response = "0000FFFF";

        App.getLoggerHelper().i("====== requestVckInfo ======");
        App.getLoggerHelper().i("request VCK info");
        response = TAFlow.getCVKInfo(mVk, Constant.VIN);
        App.getLoggerHelper().i("request VCK info response = " + response);
        if (!response.substring(0, 8).equals("00000000")) {
            if (mProgressDialog.isShowing())
                mProgressDialog.dismiss();
            App.showAlertDialog(this, "requestVckInfo failed");
            App.getLoggerHelper().e("requestVckInfo failed");
        } else {
            if (mProgressDialog.isShowing())
                mProgressDialog.setTitle("成功请求VckInfo");
            App.getLoggerHelper().i("requestVckInfo success");
        }

        return response.getBytes();
    }

    private String requestRandom(byte[] seInfo) {
        String response = "0000FFFF";

        App.getLoggerHelper().i("====== reqRandom ======");
        App.getLoggerHelper().i("request random");
        response = TAFlow.reqRandom(mVk, Constant.VIN, seInfo);
        App.getLoggerHelper().i("requestRandom TA response = " + response);
        if (!response.substring(0, 8).equals("00000000")) {
            if (mProgressDialog.isShowing())
                mProgressDialog.dismiss();
            App.showAlertDialog(this, "requestRandom failed");
            App.getLoggerHelper().e("requestRandom failed");
        } else {
            if (mProgressDialog.isShowing())
                mProgressDialog.setTitle("成功请求CCrypto");
            App.getLoggerHelper().i("requestRandom success");
        }

        return response;
    }

    private byte[] requestAuthSS(byte[] ssInfo) {
        String response = "0000FFFF";

        App.getLoggerHelper().i("====== reqAuthSS ======");
        App.getLoggerHelper().i("request authentication SS");
        response = TAFlow.reqAuthSS(mVk, Constant.VIN, ssInfo);
        App.getLoggerHelper().i("request authentication SS response = " + response);
        if (!response.equals("00000000")) {
            if (mProgressDialog.isShowing())
                mProgressDialog.dismiss();
            App.showAlertDialog(this, "requestAuthSS failed");
            App.getLoggerHelper().e("requestAuthSS failed");
        } else {
            if (mProgressDialog.isShowing())
                mProgressDialog.setTitle("成功认证");
            mTvBleSs.setText("身份：已认证");
            App.getLoggerHelper().i("requestAuthSS success");
        }

        return response.getBytes();
    }

    private String requestCmd(byte[] cmd) {
        String response = "0000FFFF";

        App.getLoggerHelper().i("====== requestCmd ======");
        App.getLoggerHelper().i("request control cmd");
        response = TAFlow.reqCMD(mVk, Constant.VIN, cmd);
        App.getLoggerHelper().i("request control cmd response = " + response);
        if (!response.substring(0, 8).equals("00000000")) {
            App.showAlertDialog(this, "requestCmd failed");
            App.getLoggerHelper().e("requestCmd failed");
        } else {
            App.getLoggerHelper().i("requestCmd success");
        }

        return response;
    }

    private void initView() {
        mToolbar = findViewById(R.id.toolbar);
        mTvBleSs = findViewById(R.id.tv_ble_ss);
        mTvBlePos = findViewById(R.id.tv_ble_pos);
        mTvBleDist = findViewById(R.id.tv_ble_dist);
        mTvBleIsConn = findViewById(R.id.tv_ble_iscon);
        mIbEngine = findViewById(R.id.ib_engine);
        mIbDoorlock = findViewById(R.id.ib_doorlock);
        mIbAirconditon = findViewById(R.id.ib_airconditon);
        mIbTrunk = findViewById(R.id.ib_trunk);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(true);

        mToolbar.setTitle("");
        TextView tv_toolbar_title = findViewById(R.id.tv_toolbar_title);
        tv_toolbar_title.setText("车辆控制");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.mipmap.ic_back);
        mToolbar.setNavigationOnClickListener(v -> finish());

        mTvBleIsConn.setText("蓝牙：已连接");
        mTvBleSs.setText("身份：认证中");
        mProgressDialog.setTitle("身份认证，请稍等...");
        mProgressDialog.show();
        mCharacteristic = mWrChara = null;
    }

    private void showData() {
        BluetoothGatt gatt = BleManager.getInstance().getBluetoothGatt(mBleDevice);

        for (BluetoothGattService service : gatt.getServices()) {
            if (service.getUuid().compareTo(UUID.fromString(Constant.TBOX_SERVICE_UUID)) == 0)
                setBluetoothGattService(service);
        }

        if (getBluetoothGattService() == null) {
            finish();
            return;
        }

        for (BluetoothGattCharacteristic tmpCharacteristic : getBluetoothGattService().getCharacteristics()) {
            App.getLoggerHelper().e("====>show characteristic uuid: " + tmpCharacteristic.getUuid().toString());
            int charaProp = tmpCharacteristic.getProperties();
            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                App.getLoggerHelper().e("======>Read");
            }
            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                App.getLoggerHelper().e("======>Write");
                mWrChara = tmpCharacteristic;
            }
            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
                App.getLoggerHelper().e("======>Write No Response");
            }
            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                App.getLoggerHelper().e("======>Notify");
                if (tmpCharacteristic != null)
                    initNotify(tmpCharacteristic);
            }
            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                App.getLoggerHelper().e("======>Indicate");
            }
        }

        mIbDoorlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] reqCmd = new byte[79];
                byte[] cmdCrypto = HexUtil.hexStringToBytes(requestCmd(Constant.REQ_DOOR_CMD));
                System.arraycopy(Constant.REQ_DOOR_CMD, 0, reqCmd, 0, Constant.REQ_DOOR_CMD.length);
                System.arraycopy(cmdCrypto, 0, reqCmd, Constant.REQ_DOOR_CMD.length, cmdCrypto.length);
                reqCmd[2] = (!mIbDoorlock.isSelected() ? (byte)0x00 : (byte)0x01);
                writeBLE(!mIbDoorlock.isSelected() ? "车门打开中..." : "车门关闭中...", reqCmd);
            }
        });

        mIbEngine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] reqCmd = new byte[79];
                byte[] cmdCrypto = HexUtil.hexStringToBytes(requestCmd(Constant.REQ_ENGINE_CMD));
                System.arraycopy(Constant.REQ_ENGINE_CMD, 0, reqCmd, 0, Constant.REQ_ENGINE_CMD.length);
                System.arraycopy(cmdCrypto, 0, reqCmd, Constant.REQ_ENGINE_CMD.length, cmdCrypto.length);
                reqCmd[2] = (!mIbEngine.isSelected() ? (byte)0x10 : (byte)0x11);
                writeBLE(!mIbEngine.isSelected() ? "发动机启动中..." : "发动机停止中...", reqCmd);
            }
        });

        mIbAirconditon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] reqCmd = new byte[79];
                byte[] cmdCrypto = HexUtil.hexStringToBytes(requestCmd(Constant.REQ_AC_CMD));
                System.arraycopy(Constant.REQ_AC_CMD, 0, reqCmd, 0, Constant.REQ_AC_CMD.length);
                System.arraycopy(cmdCrypto, 0, reqCmd, Constant.REQ_AC_CMD.length, cmdCrypto.length);
                reqCmd[2] = (!mIbAirconditon.isSelected() ? (byte)0x20 : (byte)0x21);
                writeBLE(!mIbAirconditon.isSelected() ? "空调启动中..." : "空调停止中...", reqCmd);
            }
        });

        mIbTrunk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] reqCmd = new byte[79];
                byte[] cmdCrypto = HexUtil.hexStringToBytes(requestCmd(Constant.REQ_TRUNK_CMD));
                System.arraycopy(Constant.REQ_TRUNK_CMD, 0, reqCmd, 0, Constant.REQ_TRUNK_CMD.length);
                System.arraycopy(cmdCrypto, 0, reqCmd, Constant.REQ_TRUNK_CMD.length, cmdCrypto.length);
                reqCmd[2] = (!mIbTrunk.isSelected() ? (byte)0x30 : (byte)0x31);
                writeBLE(!mIbTrunk.isSelected() ? "后备箱打开中..." : "后备箱关闭中...", reqCmd);
            }
        });
    }

    private void writeBLE(String title, byte[] data) {
        mProgressDialog.setTitle(title);
        if (!mProgressDialog.isShowing())
            mProgressDialog.show();
        BleManager.getInstance().write(mBleDevice, mWrChara.getService().getUuid().toString(), mWrChara.getUuid().toString(), data,
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
//                                ToastUtils.toastShort("write success, current: " + current + " total: " + total + " justWrite: " + HexUtil.formatHexString(justWrite, true));
                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {
                        ToastUtils.toastShort(exception.toString());
                        if (mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                    }
                });
    }

    private void initNotify(BluetoothGattCharacteristic mNotifyChara) {
        BleManager.getInstance().notify(
                mBleDevice,
                mNotifyChara.getService().getUuid().toString(),
                mNotifyChara.getUuid().toString(),
                new BleNotifyCallback() {

                    @Override
                    public void onNotifySuccess() {
//                            ToastUtils.toastShort("Succeed to notify");
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {
                        ToastUtils.toastShort(exception.toString());
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
//                            ToastUtils.toastShort("Read:" + HexUtil.formatHexString(mNotifyChara.getValue(), true));
//                        App.getLoggerHelper().e("====>mNotifyChara run: " + HexUtil.formatHexString(mNotifyChara.getValue()));
                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                        if (HexUtil.formatHexString(mNotifyChara.getValue()).substring(0, 6).equals(HexUtil.formatHexString(Constant.RESP_DOOR_ON_CMD).toLowerCase())) {
                            ToastUtils.toastShort("车门开启成功");
                            mIbDoorlock.setSelected(true);
                        } else if (HexUtil.formatHexString(mNotifyChara.getValue()).substring(0, 6).equals(HexUtil.formatHexString(Constant.RESP_DOOR_OFF_CMD).toLowerCase())) {
                            ToastUtils.toastShort("车门关闭成功");
                            mIbDoorlock.setSelected(false);
                        } else if (HexUtil.formatHexString(mNotifyChara.getValue()).substring(0, 6).equals(HexUtil.formatHexString(Constant.RESP_ENGINE_ON_CMD).toLowerCase())) {
                            ToastUtils.toastShort("发动机开启成功");
                            mIbEngine.setSelected(true);
                        } else if (HexUtil.formatHexString(mNotifyChara.getValue()).substring(0, 6).equals(HexUtil.formatHexString(Constant.RESP_ENGINE_OFF_CMD).toLowerCase())) {
                            ToastUtils.toastShort("发动机关闭成功");
                            mIbEngine.setSelected(false);
                        } else if (HexUtil.formatHexString(mNotifyChara.getValue()).substring(0, 6).equals(HexUtil.formatHexString(Constant.RESP_AC_ON_CMD).toLowerCase())) {
                            ToastUtils.toastShort("空调开启成功");
                            mIbAirconditon.setSelected(true);
                        } else if (HexUtil.formatHexString(mNotifyChara.getValue()).substring(0, 6).equals(HexUtil.formatHexString(Constant.RESP_AC_OFF_CMD).toLowerCase())) {
                            ToastUtils.toastShort("空调关闭成功");
                            mIbAirconditon.setSelected(false);
                        } else if (HexUtil.formatHexString(mNotifyChara.getValue()).substring(0, 6).equals(HexUtil.formatHexString(Constant.RESP_TRUNK_ON_CMD).toLowerCase())) {
                            ToastUtils.toastShort("后备箱开启成功");
                            mIbTrunk.setSelected(true);
                        } else if (HexUtil.formatHexString(mNotifyChara.getValue()).substring(0, 6).equals(HexUtil.formatHexString(Constant.RESP_TRUNK_OFF_CMD).toLowerCase())) {
                            ToastUtils.toastShort("后备箱关闭成功");
                            mIbTrunk.setSelected(false);
                        } else if (HexUtil.formatHexString(mNotifyChara.getValue()).substring(0, 6).equals(HexUtil.formatHexString(Constant.RESP_SE_TRND).toLowerCase())) {
                            if (mProgressDialog.isShowing())
                                mProgressDialog.setTitle("应答SEID & TRND");
                            ToastUtils.toastShort("获得RESP_SE_TRND");
                            System.arraycopy(mNotifyChara.getValue(), 5, mSeId, 0, mSeId.length);
                            System.arraycopy(mNotifyChara.getValue(), 23, mTRnd, 0, mTRnd.length);
                            App.getLoggerHelper().i("获得RESP_SE_TRND");
                            App.getLoggerHelper().i("mSeId:" + HexUtil.formatHexString(mSeId));
                            App.getLoggerHelper().i("mTRnd:" + HexUtil.formatHexString(mTRnd));

                            System.arraycopy(mNotifyChara.getValue(), 3, mSeTrnd, 0, mSeTrnd.length);
                            App.getLoggerHelper().i("mSeInfo:" + HexUtil.formatHexString(mSeTrnd));

                            System.arraycopy(mTeeId, 0, mTaAdmin.getTeeId(), 0, mTeeId.length);
                            System.arraycopy(mTaId, 0, mTaAdmin.getTaId(), 0, mTaId.length);
                            App.getLoggerHelper().i("TEEID: " + HexUtil.formatHexString(mTeeId));
                            App.getLoggerHelper().i("TAID: " + HexUtil.formatHexString(mTaId));

                            byte[] reqRnd = HexUtil.hexStringToBytes(requestRandom(mSeTrnd));
                            byte[] reqVckInfo = requestVckInfo();
                            byte[] reqAuth = new byte[315];
                            System.arraycopy(Constant.REQ_AUTH, 0, reqAuth, 0, Constant.REQ_AUTH.length);
                            System.arraycopy(reqRnd, 0, reqAuth, Constant.REQ_AUTH.length, reqRnd.length);
                            System.arraycopy(reqRnd, 0, mCCrypto, 0, reqRnd.length);
                            System.arraycopy(reqVckInfo, 0, reqAuth, Constant.REQ_AUTH.length + reqRnd.length, reqVckInfo.length);
                            App.getLoggerHelper().i("reqAuth:" + HexUtil.formatHexString(reqAuth));
                            writeBLE("请求认证", reqAuth);
                            if (mProgressDialog.isShowing())
                                mProgressDialog.setTitle("请求认证");
                        } else if (HexUtil.formatHexString(mNotifyChara.getValue()).substring(0, 6).equals(HexUtil.formatHexString(Constant.RESP_AUTH).toLowerCase())) {
                            if (mProgressDialog.isShowing())
                                mProgressDialog.setTitle("应答认证");
                            ToastUtils.toastShort("获得RESP_AUTH");
                            App.getLoggerHelper().i("获得RESP_AUTH");

                            System.arraycopy(mNotifyChara.getValue(), 3, mTCrypto, 0, mTCrypto.length);
                            App.getLoggerHelper().i("mTCrypto:" + HexUtil.formatHexString(mTCrypto));
                            App.getLoggerHelper().i("mTCrypto length:" + mTCrypto.length);

                            byte[] reqSs = new byte[98];
                            System.arraycopy(mTCrypto, 4, reqSs, 0, reqSs.length);
                            App.getLoggerHelper().i("reqSs:" + HexUtil.formatHexString(reqSs));
                            App.getLoggerHelper().i(requestAuthSS(reqSs));
                        }
                    }
                });
    }

    private void readRssiInterval(int interval) {
        mDisposable = Observable.interval(interval, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        BleManager.getInstance().readRssi(
                                mBleDevice,
                                new BleRssiCallback() {

                                    @Override
                                    public void onRssiFailure(BleException exception) {
                                        mRssi = Integer.MAX_VALUE;
                                    }

                                    @Override
                                    public void onRssiSuccess(int rssi) {
                                        mRssi = rssi;
                                    }
                                });
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        Log.i("com.clj.blesample", "accept: " + mRssi);
                        mTvBleDist.setText(mRssi == Integer.MAX_VALUE ? "未知" : "RSSI：" + String.valueOf(mRssi));
                    }
                });
    }

    public BleDevice getBleDevice() {
        return mBleDevice;
    }

    public BluetoothGattService getBluetoothGattService() {
        return mBluetoothGattService;
    }

    public void setBluetoothGattService(BluetoothGattService bluetoothGattService) {
        this.mBluetoothGattService = bluetoothGattService;
    }

    public BluetoothGattCharacteristic getCharacteristic() {
        return mCharacteristic;
    }

    public void setCharacteristic(BluetoothGattCharacteristic characteristic) {
        this.mCharacteristic = characteristic;
    }

    public int getCharaProp() {
        return mCharaProp;
    }

    public void setCharaProp(int charaProp) {
        this.mCharaProp = charaProp;
    }
}
