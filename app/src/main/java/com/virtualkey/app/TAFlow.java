package com.virtualkey.app;

import android.util.Log;

import com.example.gemaltotest.EseSimulator;
import com.gemalto.virtualkey.api.virtualkey.Virtualkey;
import com.gemalto.virtualkey.api.virtualkey.VirtualkeyException;
import com.gemalto.virtualkey.taadmin.TaAdmin;
import com.virtualkey.app.operation.VckActivity;
import com.virtualkey.utilities.Utilities;

/**
 * Created by hxie on 6/15/2017.
 */

public class TAFlow {

    static Virtualkey vk;
    byte vkOperation;
    private static final int OPERATION_TYPE_DOWNLOADINGKEY = 0;
    private static final int OPERATION_TYPE_DELETEKEY = 1;

    private static final int OPERATION_TYPE_GETVCKINFO = 0;
    private static final int OPERATION_TYPE_GETAPPRANDOM = 1;
    private static final int OPERATION_TYPE_REQAUTHSS = 2;
    private static final int OPERATION_TYPE_ASSEMBLECMD = 3;
    private static final int OPERATION_TYPE_SESSIONCLOSE = (byte) 0xFF;

    public static String loadCVK(Virtualkey vk, byte[] vin, byte[] cvkinfo, byte[] Enc_CVK) {

        try {
            // byte[] _cvkinfo = new byte[1024];
            // Arrays.fill(_cvkinfo, (byte)0xFF);
            return vk.ManageCVK((byte) OPERATION_TYPE_DOWNLOADINGKEY, vin, cvkinfo, Enc_CVK);
        } catch (VirtualkeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String loadCVK(Virtualkey vk, EseSimulator simu, byte[] vin, byte[] cvkinfo, byte[] Enc_CVK) {

        try {
            // byte[] _cvkinfo = new byte[1024];
            // Arrays.fill(_cvkinfo, (byte)0xFF);
            return vk.ManageCVK((byte) OPERATION_TYPE_DOWNLOADINGKEY, vin, cvkinfo, Enc_CVK);
        } catch (VirtualkeyException e) {
            e.printStackTrace();
        }
        return null;
    }


    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] byteArrayFromHexString(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String loadCVK_kt(Virtualkey vk, byte[] vin, byte[] cvkinfo, byte[] Enc_CVK, byte[] kt) {
        String TAG = "loadCVK_kt";
        //byte[] plainKt = Plain_Kt;
        byte[] plainKt = kt;

        Log.e(TAG, "plainKt:" + bytesToHex(plainKt));
        String cvk = bytesToHex(Enc_CVK) + "00000000000000000000000000000000";
        byte[] vkDk1Arr = byteArrayFromHexString(cvk);
        Log.e(TAG, "vkDk1Arr:" + bytesToHex(vkDk1Arr));
        byte[] vkDk1Enc = Utilities.encrypt_ecb(plainKt, vkDk1Arr);
        Log.e(TAG, "vkDk1Enc:" + bytesToHex(vkDk1Enc));
        String vkDk1 = bytesToHex(vkDk1Enc);
        Log.e(TAG, "vkDk1:" + vkDk1);
        byte[] tVersion = new byte[1];
        tVersion[0] = VckActivity.bootstrapVersion[0];
        String version = bytesToHex(tVersion);
        byte[] tID = new byte[1];
        tID[0] = VckActivity.bootstrapId[0];
        String vk_kt = "00010010" + vkDk1 + bytesToHex(tID) + version;
        Log.e(TAG, "vk_kt:" + vk_kt);
        byte[] bvk_kt = byteArrayFromHexString(vk_kt);

        try {
            return vk.ManageCVK((byte) OPERATION_TYPE_DOWNLOADINGKEY, vin, cvkinfo, bvk_kt);
        } catch (VirtualkeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String loadCVK_kt(Virtualkey vk, EseSimulator simu, byte[] vin, byte[] cvkinfo, byte[] Enc_CVK, byte[] kt, byte[] kt_version) {
        String TAG = "loadCVK_kt";
        //byte[] plainKt = Plain_Kt;
        byte[] plainKt = kt;

        Log.e(TAG, "plainKt:" + bytesToHex(plainKt));
        String cvk = bytesToHex(Enc_CVK) + "00000000000000000000000000000000";
        byte[] vkDk1Arr = byteArrayFromHexString(cvk);
        Log.e(TAG, "vkDk1Arr:" + bytesToHex(vkDk1Arr));
        byte[] vkDk1Enc = Utilities.encrypt_ecb(plainKt, vkDk1Arr);
        Log.e(TAG, "vkDk1Enc:" + bytesToHex(vkDk1Enc));
        String vkDk1 = bytesToHex(vkDk1Enc);
        Log.e(TAG, "vkDk1:" + vkDk1);
        byte[] tVersion = new byte[1];
        tVersion[0] = VckActivity.bootstrapVersion[0];
        String version = bytesToHex(tVersion);

        byte[] tID = new byte[1];
        tID[0] = VckActivity.bootstrapId[0];
        String vk_kt = "00010010" + vkDk1 + bytesToHex(tID) + version;
        Log.e(TAG, "vk_kt:" + vk_kt);
        byte[] bvk_kt = byteArrayFromHexString(vk_kt);

        try {
            return vk.ManageCVK((byte) OPERATION_TYPE_DOWNLOADINGKEY, vin, cvkinfo, bvk_kt);
        } catch (VirtualkeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String loadCVK_kt(Virtualkey vk, EseSimulator simu, byte[] vin, byte[] cvkinfo, byte[] Enc_CVK, byte[] kt) {
        String TAG = "loadCVK_kt";
        //byte[] plainKt = Plain_Kt;
        byte[] plainKt = kt;

        Log.e(TAG, "plainKt:" + bytesToHex(plainKt));
        String cvk = bytesToHex(Enc_CVK) + "00000000000000000000000000000000";
        byte[] vkDk1Arr = byteArrayFromHexString(cvk);
        Log.e(TAG, "vkDk1Arr:" + bytesToHex(vkDk1Arr));
        byte[] vkDk1Enc = Utilities.encrypt_ecb(plainKt, vkDk1Arr);
        Log.e(TAG, "vkDk1Enc:" + bytesToHex(vkDk1Enc));
        String vkDk1 = bytesToHex(vkDk1Enc);
        Log.e(TAG, "vkDk1:" + vkDk1);
        byte[] tVersion = new byte[1];
        tVersion[0] = VckActivity.bootstrapVersion[0];
        String version = bytesToHex(tVersion);
        byte[] tID = new byte[1];
        tID[0] = VckActivity.bootstrapId[0];
        String vk_kt = "00010010" + vkDk1 + bytesToHex(tID) + version;
        Log.e(TAG, "vk_kt:" + vk_kt);
        byte[] bvk_kt = byteArrayFromHexString(vk_kt);

        try {
            return vk.ManageCVK((byte) OPERATION_TYPE_DOWNLOADINGKEY, vin, cvkinfo, bvk_kt);
        } catch (VirtualkeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String deleteCVK(Virtualkey vk, EseSimulator simu, byte[] vin, byte[] cvkinfo, byte[] Enc_CVK) {

        try {
            return vk.ManageCVK((byte) OPERATION_TYPE_DELETEKEY, vin, cvkinfo, Enc_CVK);
        } catch (VirtualkeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getCVKInfo(Virtualkey vk, byte[] vin) {

        try {
            byte[] operationInput = new byte[0];
            return vk.Operation((byte) OPERATION_TYPE_GETVCKINFO, vin, operationInput, 0);
        } catch (VirtualkeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String reqRandom(Virtualkey vk, byte[] vin, byte[] operationInput) {
        String response = null;

        try {
            response = vk.Operation((byte) OPERATION_TYPE_GETAPPRANDOM, vin, operationInput, operationInput.length);
        } catch (VirtualkeyException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static String reqRandom(Virtualkey vk, EseSimulator simu, byte[] vin, byte[] operationInput) {
        String response = null;

        try {
            response = vk.Operation((byte) OPERATION_TYPE_GETAPPRANDOM, vin, operationInput, operationInput.length);
        } catch (VirtualkeyException e) {
            e.printStackTrace();
        }
        return response;
    }


    public static String reqAuthSS(Virtualkey vk, byte[] vin, byte[] operationInput) {

        String ret = null;

        try {
            ret = vk.Operation((byte) OPERATION_TYPE_REQAUTHSS, vin, operationInput, operationInput.length);
        } catch (VirtualkeyException e) {
            e.printStackTrace();
        }
        return ret;
    }


    public static String reqCMD(Virtualkey vk, byte[] vin, byte[] carControlCmd) {
        String response = null;
//        byte[] operationInput = new byte[carControlCmd.length];
//        System.arraycopy(carControlCmd,0,operationInput,0,operationInput.length);
        try {
            response = vk.Operation((byte) OPERATION_TYPE_ASSEMBLECMD, vin, carControlCmd, carControlCmd.length);
        } catch (VirtualkeyException e) {
            e.printStackTrace();
        }
        return response;
    }


    public static String Terminate(Virtualkey vk, EseSimulator simu, byte[] vin) {
        try {
            return vk.Operation((byte) OPERATION_TYPE_SESSIONCLOSE, vin, null, 0);
        } catch (VirtualkeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] hexStringToByteArray(String s) throws IllegalArgumentException {
        /* 123456... --> {0x12, 0x34, 0x56, ...} */
        int len = s.length();
        int msb, lsb;
        if (len % 2 != 0) throw new IllegalArgumentException("Odd string length");

        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            msb = Character.digit(s.charAt(i), 16);
            lsb = Character.digit(s.charAt(i + 1), 16);
            if (msb < 0)
                throw new IllegalArgumentException("Invalid character in hexstring: " + s.charAt(i));
            if (lsb < 0)
                throw new IllegalArgumentException("Invalid character in hexstring: " + s.charAt(i + 1));
            data[i / 2] = (byte) (((byte) msb << 4) + (byte) lsb);
        }
        return data;
    }

    public static byte[] computeKT(TaAdmin taAdmin) {

        byte[] kt = null;
        Log.i("TAFlow", "\"compute HMACSHA256\"");

        byte[] teeID = taAdmin.getTeeId();
        //byte[] taID=taAdmin.getTaId();
        Log.i("TAFlow", "teeID " + bytesToHex(teeID));
        //Log.i("TAFlow","taID" + bytesToHex(taID));
        byte[] id = new byte[1];
        byte[] version = new byte[1];
        id[0] = VckActivity.bootstrapId[0];
        version[0] = VckActivity.bootstrapVersion[0];
        byte[] data = hexStringToByteArray(bytesToHex(teeID) +
                bytesToHex(id) + bytesToHex(version) +
                "0000000000000000000000000000");

        Log.i("TAFlow", "MT_KT " + bytesToHex(Constant.MT_KT));
        Log.i("TAFlow", "diversify data " + bytesToHex(data));
        kt = Utilities.encrypt_ecb(Constant.MT_KT, data);
        //kt =  HMACSHA256(data, Constant.MT_KT);
        Log.i("TAFlow", "diversified Kt " + bytesToHex(kt));

        return kt;
    }
}
