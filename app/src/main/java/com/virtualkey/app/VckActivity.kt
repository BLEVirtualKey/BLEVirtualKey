package com.virtualkey.app

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import cn.cltx.mobile.dongfeng.dialog.DialogHint
import cn.cltx.mobile.dongfeng.listener.Task
import com.App
import com.clj.fastble.BleManager
import com.clj.fastble.callback.*
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.utils.HexUtil
import com.gemalto.virtualkey.api.virtualkey.Virtualkey
import com.gemalto.virtualkey.taadmin.IVirtualkeyAdminNotification
import com.gemalto.virtualkey.taadmin.TaAdmin
import com.gto.tee.agentlibrary.Utils
import com.gto.tee.agentlibrary.proxy.AgentResultCodes
import com.gto.tee.agentlibrary.proxy.ProgressState
import com.Constant
import com.virtualkey.app.comm.Observer
import com.virtualkey.app.comm.ObserverManager
import com.virtualkey.app.utils.ToastUtils
import com.virtualkey.utils.BleHelper
import com.virtualkey.utils.VckUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main_ck.*
import kotlinx.android.synthetic.main.custom_toolbar.*
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @Author: GaiQS
 * @E-mail:gaiqs@sina.com
 * @Creation Date: 2018-08-08
 * @Date Modified: 2018-08-08
 * @Describe:
 * @param:<T>
 * @FIXME
 */
class VckActivity : AppCompatActivity(), Observer {
    //    var bleDevice: BleDevice? = null
    var bluetoothGattService: BluetoothGattService? = null
    var characteristic: BluetoothGattCharacteristic? = null
    var mWrChara: BluetoothGattCharacteristic? = null
    var mDisposable: Disposable? = null
    var mRssi: Int = 0

    var mTaAdmin: TaAdmin? = null
    var mVk: Virtualkey? = null
    val mTRnd = ByteArray(8)
    val mSeId = ByteArray(16)
    val mSeTrnd = ByteArray(28)
    val mCCrypto = ByteArray(102)
    val mTCrypto = ByteArray(102)
    val mTeeId = ByteArray(16)
    val mTaId = ByteArray(16)
    val mKicc = ByteArray(32)
    val mKifd = ByteArray(32)

    private var mOperation: Operation? = null
    var dialogHint: DialogHint? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_ck)
        initView()
    }

    override fun onDestroy() {
        super.onDestroy()
        mTaAdmin = null
        mVk = null
        ObserverManager.getInstance().deleteObserver(this)
        if (mDisposable != null) {
            mDisposable!!.dispose()
        }
        System.exit(0)
    }

    override fun disConnected(device: BleDevice?) {
//        if (device != null && bleDevice != null && device.key == bleDevice!!.key) {
//            finish()
//        }
    }

    fun initView() {
        toolbar.title = ""
        tv_toolbar_title.text = "蓝牙展示"
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.mipmap.ic_back)
        toolbar.setNavigationOnClickListener { v -> finish() }
        dialogHint = DialogHint(this@VckActivity, "", null)
        dialogHint!!.setCancelable(false)

        BleManager.getInstance().init(application)
        BleManager.getInstance().enableLog(true).setReConnectCount(1, 5000).setConnectOverTime(20000).operateTimeout = 5000
        // Set device TEE Type
        TaAdmin.setDeviceTeeType(Utils.TEE_TYPE.GEMALTO_SOFTTEE, App.getContext())
        mTaAdmin = TaAdmin.getInstance(this)
        mVk = Virtualkey.getInstance(this)
        initTA()
    }

    fun initTA() {
        VckUtil.installTa(mTaAdmin, this, object : IVirtualkeyAdminNotification {
            override fun notifyEnd(resultCode: Int) {
                if (resultCode == AgentResultCodes.SUCCESS) {
                    if (mOperation == Operation.Install) {
                        initConnect()
                        App.getLoggerHelper().e("Service installed successfully")
                    } else if (mOperation == Operation.UPDATE) {
                        App.getLoggerHelper().e("Service updated successfully")
                    } else if (mOperation == Operation.UNENROLL) {
                        App.getLoggerHelper().e("Service uninstallation completed")
                    }
                }
            }

            override fun notifyProgress(p0: ProgressState?) {
                App.getLoggerHelper().e(p0)
            }
        }, object : Task<Boolean> {
            override fun run(t: Boolean) {
                if (t) {
                    initConnect()
                } else {
                    mOperation = Operation.Install
                    dialogHint!!.setDes("检测安全运行环境,请耐心等待")
                    if (!dialogHint!!.isShowing)
                        dialogHint!!.show()
                }
            }
        })
    }

    fun initConnect() {
        BleHelper.initScanRule()
        BleManager.getInstance().scan(object : BleScanCallback() {
            override fun onScanStarted(success: Boolean) {
                // 开始扫描（主线程）
                dialogHint!!.setDes("正在搜寻您的座驾")
                if (!dialogHint!!.isShowing)
                    dialogHint!!.show()
            }

            override fun onLeScan(bleDevice: BleDevice) {
                super.onLeScan(bleDevice)
            }

            override fun onScanning(bleDevice: BleDevice) {
                // 扫描到一个符合扫描规则的BLE设备（主线程）
//                dialogHint!!.setDes("一个符合的设备")
                startBleConnect(bleDevice)

            }

            override fun onScanFinished(scanResultList: List<BleDevice>) {
                // 扫描结束，列出所有扫描到的符合扫描规则的BLE设备（主线程）
//                dialogHint!!.setDes(scanResultList.size.toString() + "个符合设备")
            }
        })

    }

    fun startBleConnect(bleDevice: BleDevice) {
        BleHelper.connectBle(bleDevice, object : BleGattCallback() {
            override fun onStartConnect() {
                tv_ble_iscon!!.text = "蓝牙：连接中"
                dialogHint!!.setDes("开始连接您的座驾")
                if (!dialogHint!!.isShowing)
                    dialogHint!!.show()
            }

            override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
                tv_ble_iscon!!.text = "蓝牙：连接失败"
                dialogHint!!.setDes("连接失败," + exception!!.description)
                if (!dialogHint!!.isShowing)
                    dialogHint!!.show()
            }

            override fun onConnectSuccess(bleDevice: BleDevice?, gatt: BluetoothGatt?, status: Int) {
                // 连接成功，BleDevice即为所连接的BLE设备
                dialogHint!!.setDes("成功连接您的座驾,正在认证你的设备")
                tv_ble_iscon!!.text = "蓝牙：连接成功"
                showData(bleDevice)
            }

            override fun onDisConnected(isActiveDisConnected: Boolean, device: BleDevice?, gatt: BluetoothGatt?, status: Int) {
                // 连接中断，isActiveDisConnected表示是否是主动调用了断开连接方法
                tv_ble_iscon!!.text = "蓝牙：断开连接"
                dialogHint!!.setDes("蓝牙：断开连接")
            }
        })
    }

    fun showData(bleDevice: BleDevice?) {
        val gatt = BleManager.getInstance().getBluetoothGatt(bleDevice)
        for (service in gatt!!.services) {
            if (service.uuid.compareTo(UUID.fromString(Constant.TBOX_SERVICE_UUID)) == 0)
                bluetoothGattService = service
        }

//        if (bluetoothGattService == null) {
//            finish()
//            return
//        }

        for (tmpCharacteristic in bluetoothGattService!!.characteristics) {
            App.getLoggerHelper().e("====>show characteristic uuid: " + tmpCharacteristic!!.uuid.toString())
            val charaProp = tmpCharacteristic.properties
            if (charaProp and BluetoothGattCharacteristic.PROPERTY_READ > 0) {
                App.getLoggerHelper().e("======>Read")
            }
            if (charaProp and BluetoothGattCharacteristic.PROPERTY_WRITE > 0) {
                App.getLoggerHelper().e("======>Write")
                mWrChara = tmpCharacteristic
            }
            if (charaProp and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE > 0) {
                App.getLoggerHelper().e("======>Write No Response")
            }
            if (charaProp and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                App.getLoggerHelper().e("======>Notify")
                if (tmpCharacteristic != null)
                    initNotify(bleDevice, tmpCharacteristic)
            }
            if (charaProp and BluetoothGattCharacteristic.PROPERTY_INDICATE > 0) {
                App.getLoggerHelper().e("======>Indicate")
            }
        }

        VckUtil.initVCK(mTaAdmin, mVk, object : Task<Boolean> {
            override fun run(t: Boolean) {
                if (t) {
                    BleHelper.write(bleDevice, mWrChara!!.service.uuid.toString(), mWrChara!!.uuid.toString(), Constant.REQ_SE_TRND,
                            object : BleWriteCallback() {
                                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray) {
//                                    dialogHint!!.setDes("请求SEID & TRND")
//                                    dialogHint!!.show()
                                }

                                override fun onWriteFailure(exception: BleException) {
                                    dialogHint!!.setDes("蓝牙通讯异常：" + exception!!.description)
                                    if (!dialogHint!!.isShowing)
                                        dialogHint!!.show()
                                }
                            })
                    readRssiInterval(bleDevice, 500)
                } else {

                }
            }
        })

        ib_doorlock!!.setOnClickListener { v ->
            val reqCmd = ByteArray(79)
            val cmdCrypto = HexUtil.hexStringToBytes(VckUtil.requestCmd(mVk, Constant.REQ_DOOR_CMD))
            System.arraycopy(Constant.REQ_DOOR_CMD, 0, reqCmd, 0, Constant.REQ_DOOR_CMD.size)
            System.arraycopy(cmdCrypto, 0, reqCmd, Constant.REQ_DOOR_CMD.size, cmdCrypto.size)
            reqCmd[2] = if (!ib_doorlock!!.isSelected) 0x00.toByte() else 0x01.toByte()
            writeBLE(bleDevice, if (!ib_doorlock!!.isSelected) "车门打开中..." else "车门关闭中...", reqCmd)
        }

        ib_engine!!.setOnClickListener { v ->
            val reqCmd = ByteArray(79)
            val cmdCrypto = HexUtil.hexStringToBytes(VckUtil.requestCmd(mVk, Constant.REQ_ENGINE_CMD))
            System.arraycopy(Constant.REQ_ENGINE_CMD, 0, reqCmd, 0, Constant.REQ_ENGINE_CMD.size)
            System.arraycopy(cmdCrypto, 0, reqCmd, Constant.REQ_ENGINE_CMD.size, cmdCrypto.size)
            reqCmd[2] = if (!ib_engine!!.isSelected) 0x10.toByte() else 0x11.toByte()
            writeBLE(bleDevice, if (!ib_engine!!.isSelected) "发动机启动中..." else "发动机停止中...", reqCmd)
        }

        ib_airconditon!!.setOnClickListener { v ->
            val reqCmd = ByteArray(79)
            val cmdCrypto = HexUtil.hexStringToBytes(VckUtil.requestCmd(mVk, Constant.REQ_AC_CMD))
            System.arraycopy(Constant.REQ_AC_CMD, 0, reqCmd, 0, Constant.REQ_AC_CMD.size)
            System.arraycopy(cmdCrypto, 0, reqCmd, Constant.REQ_AC_CMD.size, cmdCrypto.size)
            reqCmd[2] = if (!ib_airconditon!!.isSelected) 0x20.toByte() else 0x21.toByte()
            writeBLE(bleDevice, if (!ib_airconditon!!.isSelected) "空调启动中..." else "空调停止中...", reqCmd)
        }

        ib_trunk!!.setOnClickListener { v ->
            val reqCmd = ByteArray(79)
            val cmdCrypto = HexUtil.hexStringToBytes(VckUtil.requestCmd(mVk, Constant.REQ_TRUNK_CMD))
            System.arraycopy(Constant.REQ_TRUNK_CMD, 0, reqCmd, 0, Constant.REQ_TRUNK_CMD.size)
            System.arraycopy(cmdCrypto, 0, reqCmd, Constant.REQ_TRUNK_CMD.size, cmdCrypto.size)
            reqCmd[2] = if (!ib_trunk!!.isSelected) 0x30.toByte() else 0x31.toByte()
            writeBLE(bleDevice, if (!ib_trunk!!.isSelected) "后备箱打开中..." else "后备箱关闭中...", reqCmd)
        }
    }

    fun writeBLE(bleDevice: BleDevice?, title: String, data: ByteArray) {
        if (!TextUtils.isEmpty(title)) {
            dialogHint!!.setDes(title)
            if (!dialogHint!!.isShowing)
                dialogHint!!.show()
        }
        BleHelper.write(bleDevice, mWrChara!!.service.uuid.toString(), mWrChara!!.uuid.toString(), data, object : BleWriteCallback() {
            override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray) {
//                        ToastUtils.toastShort("指令发送成功")
//                        ToastUtils.toastShort("write success, current: " + current + " total: " + total + " justWrite: " +
//                                HexUtil.formatHexString(justWrite, true))
            }

            override fun onWriteFailure(exception: BleException) {
//                        ToastUtils.toastShort(exception.toString())
//                if (dialogHint!!.isShowing)
//                    dialogHint!!.dismiss()
            }
        })
    }

    fun initNotify(bleDevice: BleDevice?, mNotifyChara: BluetoothGattCharacteristic) {
        BleHelper.bleNotify(bleDevice, mNotifyChara, object : BleNotifyCallback() {

            override fun onNotifySuccess() {
                //                            ToastUtils.toastShort("Succeed to notify");
            }

            override fun onNotifyFailure(exception: BleException) {
//                ToastUtils.toastShort(exception.toString())
            }

            override fun onCharacteristicChanged(data: ByteArray) {
                //                            ToastUtils.toastShort("Read:" + HexUtil.formatHexString(mNotifyChara.getValue(), true));
                //                        App.getLoggerHelper().e("====>mNotifyChara run: " + HexUtil.formatHexString(mNotifyChara.getValue()));

                if (HexUtil.formatHexString(mNotifyChara.value).substring(0, 6) == HexUtil.formatHexString(Constant.RESP_DOOR_ON_CMD).toLowerCase()) {
                    ToastUtils.toastShort("车门开启成功")
                    ib_doorlock!!.isSelected = true
                    if (dialogHint!!.isShowing)
                        dialogHint!!.dismiss()
                } else if (HexUtil.formatHexString(mNotifyChara.value).substring(0, 6) == HexUtil.formatHexString(Constant.RESP_DOOR_OFF_CMD).toLowerCase()) {
                    ToastUtils.toastShort("车门关闭成功")
                    ib_doorlock!!.isSelected = false
                    if (dialogHint!!.isShowing)
                        dialogHint!!.dismiss()
                } else if (HexUtil.formatHexString(mNotifyChara.value).substring(0, 6) == HexUtil.formatHexString(Constant.RESP_ENGINE_ON_CMD).toLowerCase()) {
                    ToastUtils.toastShort("发动机开启成功")
                    ib_engine!!.isSelected = true
                    if (dialogHint!!.isShowing)
                        dialogHint!!.dismiss()
                } else if (HexUtil.formatHexString(mNotifyChara.value).substring(0, 6) == HexUtil.formatHexString(Constant.RESP_ENGINE_OFF_CMD).toLowerCase()) {
                    ToastUtils.toastShort("发动机关闭成功")
                    ib_engine!!.isSelected = false
                    if (dialogHint!!.isShowing)
                        dialogHint!!.dismiss()
                } else if (HexUtil.formatHexString(mNotifyChara.value).substring(0, 6) == HexUtil.formatHexString(Constant.RESP_AC_ON_CMD).toLowerCase()) {
                    ToastUtils.toastShort("空调开启成功")
                    ib_airconditon!!.isSelected = true
                    if (dialogHint!!.isShowing)
                        dialogHint!!.dismiss()
                } else if (HexUtil.formatHexString(mNotifyChara.value).substring(0, 6) == HexUtil.formatHexString(Constant.RESP_AC_OFF_CMD).toLowerCase()) {
                    ToastUtils.toastShort("空调关闭成功")
                    ib_airconditon!!.isSelected = false
                    if (dialogHint!!.isShowing)
                        dialogHint!!.dismiss()
                } else if (HexUtil.formatHexString(mNotifyChara.value).substring(0, 6) == HexUtil.formatHexString(Constant.RESP_TRUNK_ON_CMD).toLowerCase()) {
                    ToastUtils.toastShort("后备箱开启成功")
                    ib_trunk!!.isSelected = true
                    if (dialogHint!!.isShowing)
                        dialogHint!!.dismiss()
                } else if (HexUtil.formatHexString(mNotifyChara.value).substring(0, 6) == HexUtil.formatHexString(Constant.RESP_TRUNK_OFF_CMD).toLowerCase()) {
                    ToastUtils.toastShort("后备箱关闭成功")
                    ib_trunk!!.isSelected = false
                    if (dialogHint!!.isShowing)
                        dialogHint!!.dismiss()
                } else if (HexUtil.formatHexString(mNotifyChara.value).substring(0, 6) == HexUtil.formatHexString(Constant.RESP_SE_TRND).toLowerCase()) {
//                    dialogHint!!.setDes("应答SEID & TRND")
//                    ToastUtils.toastShort("获得RESP_SE_TRND")
                    System.arraycopy(mNotifyChara.value, 5, mSeId, 0, mSeId.size)
                    System.arraycopy(mNotifyChara.value, 23, mTRnd, 0, mTRnd.size)
//                    App.getLoggerHelper().i("获得RESP_SE_TRND")
//                    App.getLoggerHelper().i("mSeId:" + HexUtil.formatHexString(mSeId))
//                    App.getLoggerHelper().i("mTRnd:" + HexUtil.formatHexString(mTRnd))

                    System.arraycopy(mNotifyChara.value, 3, mSeTrnd, 0, mSeTrnd.size)
//                    App.getLoggerHelper().i("mSeInfo:" + HexUtil.formatHexString(mSeTrnd))

                    System.arraycopy(mTeeId, 0, mTaAdmin!!.teeId, 0, mTeeId.size)
                    System.arraycopy(mTaId, 0, mTaAdmin!!.taId, 0, mTaId.size)
//                    App.getLoggerHelper().i("TEEID: " + HexUtil.formatHexString(mTeeId))
//                    App.getLoggerHelper().i("TAID: " + HexUtil.formatHexString(mTaId))

                    val reqRnd = HexUtil.hexStringToBytes(VckUtil.requestRandom(mVk, mSeTrnd))
                    val reqVckInfo = VckUtil.requestVckInfo(mVk)
                    val reqAuth = ByteArray(315)
                    System.arraycopy(Constant.REQ_AUTH, 0, reqAuth, 0, Constant.REQ_AUTH.size)
                    System.arraycopy(reqRnd, 0, reqAuth, Constant.REQ_AUTH.size, reqRnd.size)
                    System.arraycopy(reqRnd, 0, mCCrypto, 0, reqRnd.size)
                    System.arraycopy(reqVckInfo, 0, reqAuth, Constant.REQ_AUTH.size + reqRnd.size, reqVckInfo.size)
//                    App.getLoggerHelper().i("reqAuth:" + HexUtil.formatHexString(reqAuth))
                    writeBLE(bleDevice, "", reqAuth)
                } else if (HexUtil.formatHexString(mNotifyChara.value).substring(0, 6) == HexUtil.formatHexString(Constant.RESP_AUTH).toLowerCase()) {
//                    dialogHint!!.setDes("应答认证")
//                    ToastUtils.toastShort("获得RESP_AUTH")
//                    App.getLoggerHelper().i("获得RESP_AUTH")
                    System.arraycopy(mNotifyChara.value, 3, mTCrypto, 0, mTCrypto.size)
//                    App.getLoggerHelper().i("mTCrypto:" + HexUtil.formatHexString(mTCrypto))
//                    App.getLoggerHelper().i("mTCrypto length:" + mTCrypto.size)
                    val reqSs = ByteArray(98)
                    System.arraycopy(mTCrypto, 4, reqSs, 0, reqSs.size)
//                    App.getLoggerHelper().i("reqSs:" + HexUtil.formatHexString(reqSs))
//                    App.getLoggerHelper().i(VckUtil.requestAuthSS(mVk, reqSs))
                    ToastUtils.toastShort("设备认证通过,可以控制您的爱车了。")
                    if (dialogHint!!.isShowing)
                        dialogHint!!.dismiss()
                }
            }
        })
    }

    fun readRssiInterval(bleDevice: BleDevice?, interval: Int) {
        mDisposable = Observable.interval(interval.toLong(), TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io()).doOnNext {
                    BleManager.getInstance().readRssi(bleDevice, object : BleRssiCallback() {

                        override fun onRssiFailure(exception: BleException) {
                            mRssi = Integer.MAX_VALUE
                        }

                        override fun onRssiSuccess(rssi: Int) {
                            mRssi = rssi
                        }
                    })
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    //                    var iRssi = abs (mRssi)
//                    var power =(iRssi - 59) / (10 * 2.0)
                    tv_ble_dist!!.text = if (mRssi == Integer.MAX_VALUE) "RSSI：未知" else "RSSI：" + mRssi.toString()
//                    tv_ble_dist!!.text = if (mRssi == Integer.MAX_VALUE) "距离：未知" else "距离：" + pow(10.0, power)
                }
    }

    internal enum class Operation constructor(val value: Int) {
        Install(1), UPDATE(2),
        UNENROLL(3)
    }
}