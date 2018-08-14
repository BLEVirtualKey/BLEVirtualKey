package com.virtualkey.utils;

import android.content.Context;
import android.content.res.AssetManager;

import com.App;
import com.gemalto.virtualkey.api.virtualkey.Virtualkey;
import com.gemalto.virtualkey.taadmin.IVirtualkeyAdminNotification;
import com.gemalto.virtualkey.taadmin.TaAdmin;
import com.Constant;
import com.virtualkey.app.ConsentUtil;
import com.virtualkey.app.TAFlow;
import com.virtualkey.app.utils.ToastUtils;

import org.spongycastle.util.encoders.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Calendar;
import java.util.Date;

import cn.cltx.mobile.dongfeng.listener.Task;

/**
 * @Author: GaiQS
 * @E-mail:gaiqs@sina.com
 * @Creation Date: 2018-08-08
 * @Date Modified: 2018-08-08
 * @Describe:
 * @param:<T>
 * @FIXME
 */
public class VckUtil {
    /**
     * 获取vck信息
     *
     * @return
     */
    public static byte[] requestVckInfo(Virtualkey mVk) {
        String response = "0000FFFF";

//        App.getLoggerHelper().i("====== requestVckInfo ======");
//        App.getLoggerHelper().i("request VCK info");
        response = TAFlow.getCVKInfo(mVk, Constant.VIN);
//        App.getLoggerHelper().i("request VCK info response = " + response);
        if (!response.substring(0, 8).equals("00000000")) {
//            if (mProgressDialog.isShowing())
//                mProgressDialog.dismiss();
//            App.showAlertDialog(this, "requestVckInfo failed");
//            App.getLoggerHelper().e("requestVckInfo failed");
            ToastUtils.toastShort("requestVckInfo failed");
        } else {
//            mProgressDialog.setTitle("成功请求VckInfo");
//            App.getLoggerHelper().i("requestVckInfo success");
        }
        return response.getBytes();
    }

    /**
     * 随机数
     *
     * @param seInfo
     * @return
     */
    public static String requestRandom(Virtualkey mVk, byte[] seInfo) {
        String response = "0000FFFF";

//        App.getLoggerHelper().i("====== reqRandom ======");
//        App.getLoggerHelper().i("request random");
        response = TAFlow.reqRandom(mVk, Constant.VIN, seInfo);
//        App.getLoggerHelper().i("requestRandom TA response = " + response);
        if (!response.substring(0, 8).equals("00000000")) {
//            if (mProgressDialog.isShowing())
//                mProgressDialog.dismiss();
//            App.showAlertDialog(this, "requestRandom failed");
//            App.getLoggerHelper().e("requestRandom failed");
//            ToastUtils.toastShort( "requestRandom failed");
//            finish();
        } else {
//            mProgressDialog.setTitle("成功请求CCrypto");
//            App.getLoggerHelper().i("requestRandom success");
        }
        return response;
    }

    /**
     * 请求认证
     *
     * @param mVk
     * @param ssInfo
     * @return
     */
    public static byte[] requestAuthSS(Virtualkey mVk, byte[] ssInfo) {
        String response = "0000FFFF";

        App.getLoggerHelper().i("====== reqAuthSS ======");
        App.getLoggerHelper().i("request authentication SS");
        response = TAFlow.reqAuthSS(mVk, Constant.VIN, ssInfo);
        App.getLoggerHelper().i("request authentication SS response = " + response);
        if (!response.equals("00000000")) {
//            if (mProgressDialog.isShowing())
//                mProgressDialog.dismiss();
//            App.showAlertDialog(this, "requestAuthSS failed");
//            App.getLoggerHelper().e("requestAuthSS failed");
//            ToastUtils.toastShort( "requestAuthSS failed");
//            finish();
        } else {
//            if (mProgressDialog.isShowing())
//                mProgressDialog.dismiss();
//            ToastUtils.showToast(this, "成功认证");
//            mTvBleSs.setText("身份：已认证");
//            App.getLoggerHelper().i("requestAuthSS success");
        }

        return response.getBytes();
    }

    /**
     * 发送车控指令
     *
     * @param mVk
     * @param cmd
     * @return
     */
    public static String requestCmd(Virtualkey mVk, byte[] cmd) {
        String response = "0000FFFF";

//        App.getLoggerHelper().i("====== requestCmd ======");
//        App.getLoggerHelper().i("request control cmd");
        response = TAFlow.reqCMD(mVk, Constant.VIN, cmd);
//        App.getLoggerHelper().i("request control cmd response = " + response);
        if (!response.substring(0, 8).equals("00000000")) {
//            App.showAlertDialog(this, "requestCmd failed");
//            App.getLoggerHelper().e("requestCmd failed");
        } else {
//            App.getLoggerHelper().i("requestCmd success");
        }
        return response;
    }

    public static byte[] bootstrapId = new byte[4];
    public static byte[] bootstrapVersion = new byte[4];

    /**
     * 初始化Ta，判断是否安装
     *
     * @param mTaAdmin
     * @param context
     * @param notification
     */
    public static void installTa(TaAdmin mTaAdmin, Context context, IVirtualkeyAdminNotification notification, Task<Boolean> task) {
        byte[] mConsent;
        if (mTaAdmin.isTAInstalled()) {
            if (task != null)
                task.run(true);
        } else {
            int serviceId = mTaAdmin.getServiceID();
            byte[] hashOfDeviceId = mTaAdmin.getDeviceID();
            try {
                mConsent = generateConsentData(context, serviceId, hashOfDeviceId, ConsentUtil.CONSENT_USAGE.ENROLLMENT);
                if (mConsent != null) {
                    mTaAdmin.installTA(mConsent, notification);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (task != null)
                task.run(false);
        }
    }

    public static void initVCK(TaAdmin mTaAdmin, Virtualkey mVk, Task<Boolean> task) {
        mTaAdmin.setUserData("1234");
//        try {
//            int result = mVk.getBootstrapIdVersion(bootstrapId, bootstrapVersion);
//            int bsId = ((bootstrapId[3] & 0xFF) << 24) | ((bootstrapId[2] & 0xFF) << 16)
//                    | ((bootstrapId[1] & 0xFF) << 8) | (bootstrapId[0] & 0xFF);
//
//            int bsVersion = ((bootstrapVersion[3] & 0xFF) << 24) | ((bootstrapVersion[2] & 0xFF) << 16) | ((bootstrapVersion[1] & 0xFF) << 8) | (bootstrapVersion[0] & 0xFF);
//            App.getLoggerHelper().e("bootstrapId = " + TAFlow.bytesToHex(bootstrapId));
//            App.getLoggerHelper().e("bootstrapVersion = " + TAFlow.bytesToHex(bootstrapVersion));
//            App.getLoggerHelper().e("bootstrapId: " + bsId + " bootstrapVersion: " + bsVersion);
//        } catch (VirtualkeyException e) {
//            e.printStackTrace();
//            App.getLoggerHelper().e("bootstrapId failed: " + e.getMessage());
//        }
//        App.getLoggerHelper().e("Compute KT begain ");
        byte[] KT = TAFlow.computeKT(mTaAdmin);
//        App.getLoggerHelper().e("Kt = " + TAFlow.bytesToHex(KT));
//        App.getLoggerHelper().e("Compute KT finished ");

        String ret = "0000FFFF";
//        App.getLoggerHelper().i("====== loadCVK ======");
//        App.getLoggerHelper().i("Dowinloading VK");
        if (true) {
            ret = TAFlow.loadCVK_kt(mVk, Constant.VIN, Constant.CvkInfo, Constant.Enc_CVK, TAFlow.computeKT(mTaAdmin));
        } else {
            ret = TAFlow.loadCVK(mVk, Constant.VIN, Constant.CvkInfo, Constant.Enc_CVK);
        }
//        App.getLoggerHelper().i("loadCVK TA Response = " + ret);
        if (!ret.equals("00000000")) {
//            App.showAlertDialog(this, "loadCVK failed");
//            App.getLoggerHelper().e("loadCVK failed");
            if (task != null)
                task.run(false);
        } else {
//            mProgressDialog.setTitle("成功下载VCK");
//            App.getLoggerHelper().i("loadCVK success");
            if (task != null)
                task.run(true);
        }
    }

    /**
     * Return calculated consent based on service id, device id
     *
     * @param serviceId    ID of service
     * @param teeId        ID of the device
     * @param consentUsage Usage of the consent
     *                     returns generated consent
     */
    public static byte[] generateConsentData(Context context, int serviceId, byte[] teeId, ConsentUtil.CONSENT_USAGE consentUsage) throws Exception {
        final String PRIVATE_KEY_FILE = "teeConsentPrivKey.pem";
        final int PRIVATE_KEY_OFFSET = 28;
        final int PRIVATE_KEY_LENGTH = 1650;
        final String PRIVATE_KEY_STRING;
        try {
            AssetManager assetManager = context.getAssets();
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
        if (!validSignature && result.length > 13) {
            result[10] = 0x00;
            result[11] = 0x00;
            result[12] = 0x00;
        }

        return result;
    }
}
