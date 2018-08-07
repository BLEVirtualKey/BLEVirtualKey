package com.virtualkey.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;

import com.gemalto.virtualkey.taadmin.TaAdmin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.KeyEvent;

public class Utilities {

	public static byte[] HMACSHA256(byte[] data, byte[] key) throws InvalidKeyException {
		try  {
			SecretKeySpec signingKey = new SecretKeySpec(key, "HmacSHA256");
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(signingKey);
			return mac.doFinal(data);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String byte2hex(byte[] b)
	{
		StringBuilder hs = new StringBuilder();
		String stmp;
		for (int n = 0; b!=null && n < b.length; n++) {
			stmp = Integer.toHexString(b[n] & 0XFF);
			if (stmp.length() == 1)
				hs.append('0');
			hs.append(stmp);
		}
		return hs.toString().toUpperCase();
	}
	public static byte[] encrypt(byte[] key, byte[] IV, byte[] plainMsg) {
		try {
			IvParameterSpec iv = new IvParameterSpec(IV);
			SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
			return cipher.doFinal(plainMsg);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static byte[] decrypt(byte[] key, byte[] IV, byte[] encMsg) {
		try {
			IvParameterSpec iv = new IvParameterSpec(IV);
			SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			return cipher.doFinal(encMsg);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static byte[] decrypt_ecb(byte[] key, byte[] encMsg) {
		try {
			SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			return cipher.doFinal(encMsg);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static byte[] encrypt_ecb(byte[] key, byte[] encMsg) {
		try {
			SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			return cipher.doFinal(encMsg);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}


	//This is for static key
	public static byte[] readSecretKey(Context context) throws IOException {
		InputStream is;
		byte[] key = null;
		try {
			is = context.getAssets().open("secretkeyAES.bin");
			key = new byte[is.available()];
			is.read(key);
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return key;
	}



	//This is for dynamic key
	/*public static byte[] readSecretKey(Context context) {

		InputStream is;
		byte[] key = null;
		byte[] finalKey = null;
		try {
			is = context.getAssets().open("secretkeyAES.bin");
			key = new byte[is.available()];
			is.read(key);
			is.close();

			Log.i("Utilities", "PlainSecretKey:" + bytesToHex(key));
			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(key, "HmacSHA256");
			sha256_HMAC.init(secret_key);


			byte[] teeID = TaAdmin.getInstance(context).getTeeId();




			//sha256_HMAC.update(hashOfDeviceId);
			//UUID uuid = UUID.fromString("AABBCCDD-AABB-CCDD-EEFF-200000000003");
			byte[] taID = TaAdmin.getInstance(context).getTaId();
			ByteBuffer bb = ByteBuffer.wrap(new byte[16+16]);
			bb.put(teeID);
			bb.put(taID);
			//bb.putLong(uuid.getMostSignificantBits());
			//bb.putLong(uuid.getLeastSignificantBits());
			//Log.e("tag", "TA UUID " + bytesToHex(bb.array()));


			finalKey = sha256_HMAC.doFinal(bb.array());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		//Hex.encodeHexString(finalKey);

		return finalKey;
	}*/

	public static byte[] getHash(byte[] inputData) {
		MessageDigest digest=null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		digest.reset();
		return digest.digest(inputData);
	}

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static String stringToHex(String arg) {
		try {
			return String.format("%040x", new java.math.BigInteger(1, arg.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static class ByteUtils {
		public static byte[] longToBytes(long x) {
			ByteBuffer longBuffer = ByteBuffer.allocate(Long.SIZE/Byte.SIZE);
			longBuffer.putLong(0, x);
			return longBuffer.array();
		}

		public static long bytesToLong(byte[] bytes) {
			ByteBuffer longBuffer = ByteBuffer.allocate(Long.SIZE/Byte.SIZE);
			longBuffer.put(bytes, 0, bytes.length);
			longBuffer.flip();//need flip
			return longBuffer.getLong();
		}

		public static byte[] intToBytes(int x) {
			ByteBuffer intBuffer = ByteBuffer.allocate(Integer.SIZE/Byte.SIZE);
			intBuffer.putInt(0, x);
			return intBuffer.array();
		}

		public static int bytesToInt(byte[] bytes) {
			ByteBuffer intBuffer = ByteBuffer.allocate(Integer.SIZE/Byte.SIZE);
			intBuffer.put(bytes, 0, bytes.length);
			intBuffer.flip();//need flip
			return intBuffer.getInt();
		}
	}

	/**
	 * Concatenate two arrays
	 * @param first
	 * @param second
	 * @return an array
	 */
	public static byte[] concat(byte[] first, byte[] second) {
		byte[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	public static void exitApplicationGracefully(final Context context) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setCancelable(false);
		dialog.setTitle("Exit Application");
		dialog.setMessage("Without Read Phone State permission, application runs in lower security state. Shutting down application");
		dialog.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						((Activity) context).finish();
					}
				})
				.setOnKeyListener(new DialogInterface.OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						//if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
						dialog.cancel();
						((Activity) context).finish();
						//}
						return false;
					}
				});;
		dialog.show();
	}
}
