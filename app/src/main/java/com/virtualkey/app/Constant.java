package com.virtualkey.app;

/**
 * Created by hxie on 6/19/2017.
 */

public interface Constant {

//    String DOOR_ON_CMD = "AA55000A0D";
//    String DOOR_OFF_CMD = "AA55010A0D";
//
//    String ENGINE_ON_CMD = "AA55100A0D";
//    String ENGINE_OFF_CMD = "AA55110A0D";
//
//    String AC_ON_CMD = "AA55200A0D";
//    String AC_OFF_CMD = "AA55210A0D";
//
//    String TRUNK_ON_CMD = "AA55300A0D";
//    String TRUNK_OFF_CMD = "AA55310A0D";

    public static byte[] REQ_SE_TRND = {
            (byte)0xAA, (byte)0x55, (byte)0xF0,
    };
    public static byte[] RESP_SE_TRND = {
            (byte)0xAA, (byte)0x55, (byte)0xF1,
    };

    public static byte[] REQ_AUTH = {
            (byte)0xAA, (byte)0x55, (byte)0xE0,
    };
    public static byte[] RESP_AUTH = {
            (byte)0xAA, (byte)0x55, (byte)0xE1,
    };

    public static byte[] REQ_DOOR_CMD = {
            (byte)0xAA, (byte)0x55, (byte)0x00,
    };
    public static byte[] RESP_DOOR_ON_CMD = {
            (byte)0xAA, (byte)0x55, (byte)0x0A,
    };
    public static byte[] RESP_DOOR_OFF_CMD = {
            (byte)0xAA, (byte)0x55, (byte)0x0B,
    };

    public static byte[] REQ_ENGINE_CMD = {
            (byte)0xAA, (byte)0x55, (byte)0x10,
    };
    public static byte[] RESP_ENGINE_ON_CMD = {
            (byte)0xAA, (byte)0x55, (byte)0x1A,
    };
    public static byte[] RESP_ENGINE_OFF_CMD = {
            (byte)0xAA, (byte)0x55, (byte)0x1B,
    };

    public static byte[] REQ_AC_CMD = {
            (byte)0xAA, (byte)0x55, (byte)0x20,
    };
    public static byte[] RESP_AC_ON_CMD = {
            (byte)0xAA, (byte)0x55, (byte)0x2A,
    };
    public static byte[] RESP_AC_OFF_CMD = {
            (byte)0xAA, (byte)0x55, (byte)0x2B,
    };

    public static byte[] REQ_TRUNK_CMD = {
            (byte)0xAA, (byte)0x55, (byte)0x30,
    };
    public static byte[] RESP_TRUNK_ON_CMD = {
            (byte)0xAA, (byte)0x55, (byte)0x3A,
    };
    public static byte[] RESP_TRUNK_OFF_CMD = {
            (byte)0xAA, (byte)0x55, (byte)0x3B,
    };

    String TBOX_SERVICE_UUID = "000018FF-0000-1000-8000-00805F9B34FB";
    String KEY_DATA = "key_data";

    public static byte[] VIN = {
            (byte)0x4C, (byte)0x53, (byte)0x47, (byte)0x57, (byte)0x47, (byte)0x38, (byte)0x32, (byte)0x43, (byte)0x38,
            (byte)0x32, (byte)0x53, (byte)0x39, (byte)0x33, (byte)0x30, (byte)0x36, (byte)0x34, (byte)0x33
    };

    public static byte[] CvkInfo = {
            /* VIN (17 Bytes / LSGWG82C82S930643) */
            (byte)0x4C, (byte)0x53, (byte)0x47, (byte)0x57, (byte)0x47, (byte)0x38, (byte)0x32, (byte)0x43,
            (byte)0x38, (byte)0x32, (byte)0x53, (byte)0x39, (byte)0x33, (byte)0x30, (byte)0x36, (byte)0x34,
            (byte)0x33,
            /* PDID (32 Bytes / DX0123456789ABCDDX0123456789ABCD) */
            (byte)0x44, (byte)0x58, (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x33, (byte)0x34, (byte)0x35,
            (byte)0x36, (byte)0x37, (byte)0x38, (byte)0x39, (byte)0x41, (byte)0x42, (byte)0x43, (byte)0x44,
            (byte)0x44, (byte)0x58, (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x33, (byte)0x34, (byte)0x35,
            (byte)0x36, (byte)0x37, (byte)0x38, (byte)0x39, (byte)0x41, (byte)0x42, (byte)0x43, (byte)0x44,
            /* UUID (36 Bytes / 000018FF-0000-1000-8000-00805F9B34FB)*/
            (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x38, (byte)0x46, (byte)0x46, (byte)0x2D,
            (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x2D,
            (byte)0x31, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x2D,
            (byte)0x38, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x2D,
            (byte)0x30, (byte)0x30, (byte)0x38, (byte)0x30, (byte)0x35, (byte)0x46, (byte)0x39, (byte)0x42, (byte)0x33, (byte)0x34, (byte)0x46, (byte)0x42,
            /* BLE Device Name (16 Bytes / AutoLinkTBox)*/
            (byte)0x41, (byte)0x75, (byte)0x74, (byte)0x6F, (byte)0x4C, (byte)0x69, (byte)0x6E, (byte)0x6B,
            (byte)0x54, (byte)0x42, (byte)0x6F, (byte)0x78, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
    };

    /* 等效于规范提及的VCK CMPK值 */
    public static byte[] Enc_CVK = {
            (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x05, (byte)0x06, (byte)0x07, (byte)0x08,
            (byte)0x09, (byte)0x0A, (byte)0x0B, (byte)0x0C, (byte)0x0D, (byte)0x0E, (byte)0x0F, (byte)0x00,
    };

    public static byte[] MT_KT = new byte[] {
            (byte)0xDF, (byte)0x92, (byte)0x02, (byte)0x40, (byte)0x12, (byte)0xD6, (byte)0xB4, (byte)0x6E,
            (byte)0xAB, (byte)0x79, (byte)0xA6, (byte)0x66, (byte)0x21, (byte)0x1E, (byte)0x3F, (byte)0x50,
            (byte)0x9A, (byte)0x80, (byte)0x89, (byte)0xA4, (byte)0x7E, (byte)0x2D, (byte)0x07, (byte)0x9E,
            (byte)0x45, (byte)0x69, (byte)0x04, (byte)0xED, (byte)0x59, (byte)0x17, (byte)0xCF, (byte)0x28,
    };

    public static byte[] Plain_Kt = new byte[] {
            (byte)0x26, (byte)0x58, (byte)0xE8, (byte)0xAB, (byte)0x20, (byte)0xB7, (byte)0xE6, (byte)0x5B,
            (byte)0x31, (byte)0x51, (byte)0xC2, (byte)0x9C, (byte)0xB7, (byte)0xD6, (byte)0xF6, (byte)0xE8,
            (byte)0xEE, (byte)0xBE, (byte)0x08, (byte)0x05, (byte)0x4D, (byte)0x23, (byte)0x44, (byte)0x99,
            (byte)0x07, (byte)0x15, (byte)0x09, (byte)0xCC, (byte)0xDD, (byte)0xD8, (byte)0x52, (byte)0xDE,
    };

    public static byte[] REQ_DOOR_OFF = {
            (byte)0x00, (byte)0x00,
    };
    public static byte[] REQ_DOOR_ON = {
            (byte)0x00, (byte)0x01,
    };

    public static byte[] REQ_ENGINE_OFF = {
            (byte)0x10, (byte)0x00,
    };
    public static byte[] REQ_ENGINE_ON = {
            (byte)0x10, (byte)0x01,
    };

    public static byte[] REQ_AC_OFF = {
            (byte)0x20, (byte)0x00,
    };
    public static byte[] REQ_AC_ON = {
            (byte)0x20, (byte)0x01,
    };

    public static byte[] REQ_TRUNK_OFF = {
            (byte)0x30, (byte)0x00,
    };
    public static byte[] REQ_TRUNK_ON = {
            (byte)0x30, (byte)0x01,
    };
}
