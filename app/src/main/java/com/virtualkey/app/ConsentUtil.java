// -------------------------------------------------------------------------
// Copyright (c) 2015 GEMALTO group. All Rights Reserved.
//
// This software is the confidential and proprietary information of
// GEMALTO.
//
// Project name: TEE-TSM
//
// Platform : Java virtual machine
// Language : JAVA 6.0
//
// Original author: hranjali <HazratPradipta.Ranjali at gemalto.com>
//
// -------------------------------------------------------------------------
// GEMALTO MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
// THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
// TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
// PARTICULAR PURPOSE, OR NON-INFRINGEMENT. GEMALTO SHALL NOT BE
// LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING,
// MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
//
// THIS SOFTWARE IS NOT DESIGNED OR INTENDED FOR USE OR RESALE AS ON-LINE
// CONTROL EQUIPMENT IN HAZARDOUS ENVIRONMENTS REQUIRING FAIL-SAFE
// PERFORMANCE, SUCH AS IN THE OPERATION OF NUCLEAR FACILITIES, AIRCRAFT
// NAVIGATION OR COMMUNICATION SYSTEMS, AIR TRAFFIC CONTROL, DIRECT LIFE
// SUPPORT MACHINES, OR WEAPONS SYSTEMS, IN WHICH THE FAILURE OF THE
// SOFTWARE COULD LEAD DIRECTLY TO DEATH, PERSONAL INJURY, OR SEVERE
// PHYSICAL OR ENVIRONMENTAL DAMAGE ("HIGH RISK ACTIVITIES"). GEMALTO
// SPECIFICALLY DISCLAIMS ANY EXPRESS OR IMPLIED WARRANTY OF FITNESS FOR
// HIGH RISK ACTIVITIES.
// -------------------------------------------------------------------------
package com.virtualkey.app;


import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Date;
//import org.apache.commons.codec.binary.Hex;
import com.gto.tee.agentlibrary.util.HexStringHelper;
import org.spongycastle.asn1.ASN1Encodable;
import org.spongycastle.asn1.ASN1EncodableVector;
import org.spongycastle.asn1.ASN1StreamParser;
import org.spongycastle.asn1.DEREnumerated;
import org.spongycastle.asn1.DERInteger;
import org.spongycastle.asn1.DEROctetString;
import org.spongycastle.asn1.DERSequence;
import org.spongycastle.asn1.DERSequenceParser;
import org.spongycastle.asn1.DERUTCTime;

/**
 * Utility class to handle Consent Authorization
 */
public class ConsentUtil {

    /**
     * Algorithm to sign the consent
     */
    public static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    /**
     * Acceptable value for Consent Usage type
     */
    public enum CONSENT_USAGE {

        ENROLLMENT(1),
        UPDATE(2);

        private final int _value;

        CONSENT_USAGE(int val) {
            _value = val;
        }

        public int getValue() {
            return _value;
        }
        
        public static CONSENT_USAGE fromInt(int val) {
            if(val == 1) {
                return ENROLLMENT;
            } else if(val == 2) {
                return UPDATE;
            }
            return null;
        }

    }

    /**
     * Convert byte array to DERSequence object
     *
     * @param value byte array
     * @return DERSequence if successful and null if there is error
     * @throws Exception
     */
    protected static DERSequence getDerSequence(byte[] value)
            throws Exception {
        try {
            ASN1StreamParser strParser = new ASN1StreamParser(value);
            ASN1Encodable obj = strParser.readObject();
            if (obj != null && obj instanceof DERSequenceParser) {
                DERSequenceParser seqParser = (DERSequenceParser) obj;
                DERSequence loadedObject = (DERSequence) seqParser.getLoadedObject();
                return loadedObject;
            }
            throw new Exception("invalid asn type");
        } catch (IOException ex) {
            throw new Exception(ex.getMessage());
        }
    }

    /**
     * Generate ASN1 DER byte array of Opaque Data
     *
     * @param staticVal
     * @param validTime
     * @param usage
     * @return opaque data in byte array if successful and null if there is
     * error
     * @throws Exception
     */
    public static byte[] getOpaqueData(byte[] staticVal, Date validTime, CONSENT_USAGE usage)
            throws Exception {
        try {
            ASN1EncodableVector ls = new ASN1EncodableVector();
            if (staticVal != null) {
                DEROctetString derOct = new DEROctetString(staticVal);
                ls.add(derOct);
            }

            if (validTime != null) {
                DERUTCTime derTime = new DERUTCTime(validTime);
                ls.add(derTime);
            }

            if (usage != null) {
                DEREnumerated derEnum = new DEREnumerated(usage.getValue());
                ls.add(derEnum);
            }

            DERSequence derSeq = new DERSequence(ls);
            return derSeq.getEncoded("UTF-8");
        } catch (IOException ex) {
            throw new Exception(ex.getMessage());
        }
    }

    /**
     * Generate sign request byte array to be signed or verified
     *
     * @param deviceId
     * @param teeServiceId
     * @param opaqueData
     * @return sign request in byte array or null if there is error
     * @throws Exception
     */
    public static byte[] getSignRequest(String deviceId, int teeServiceId, byte[] opaqueData)
            throws Exception {
        return getSignRequest(HexStringHelper.hexaToBytes(deviceId), teeServiceId, opaqueData);
    }
    
    /**
     * Generate sign request byte array to be signed or verified
     *
     * @param deviceId
     * @param teeServiceId
     * @param opaqueData
     * @return sign request in byte array or null if there is error
     * @throws Exception
     */
    public static byte[] getSignRequest(byte[] deviceId, int teeServiceId, byte[] opaqueData)
            throws Exception {
        try {
            ASN1EncodableVector ls = new ASN1EncodableVector();

            DEROctetString derOct = new DEROctetString(deviceId);
            ls.add(derOct);

            DERInteger derInt = new DERInteger(teeServiceId);
            ls.add(derInt);

            ls.add(getDerSequence(opaqueData));

            DERSequence derSeq = new DERSequence(ls);
            return derSeq.getEncoded();
        } catch (IOException ex) {
            throw new Exception(ex.getMessage());
        }
    }

    /**
     * Generate consent data which contain signature and opaque data
     *
     * @param privKey
     * @param deviceId
     * @param teeServiceId
     * @param opaqueData
     * @return consent data or null if there is error
     * @throws Exception
     */
    public static byte[] getConsentData(PrivateKey privKey, String deviceId, int teeServiceId, byte[] opaqueData)
            throws Exception {
        return getConsentData(privKey, HexStringHelper.hexaToBytes(deviceId), teeServiceId, opaqueData);
    }
    
    /**
     * Generate consent data which contain signature and opaque data
     *
     * @param privKey
     * @param deviceId
     * @param teeServiceId
     * @param opaqueData
     * @return consent data or null if there is error
     * @throws Exception
     */
    public static byte[] getConsentData(PrivateKey privKey, byte[] deviceId, int teeServiceId, byte[] opaqueData)
            throws Exception {
        try {
            byte[] signRequest = getSignRequest(deviceId, teeServiceId, opaqueData);

            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privKey);
            signature.update(signRequest);
            byte[] signValue = signature.sign();

            ASN1EncodableVector ls = new ASN1EncodableVector();

            DEROctetString derOct1 = new DEROctetString(signValue);
            ls.add(derOct1);

            DEROctetString derOct2 = new DEROctetString(opaqueData);
            ls.add(derOct2);

            DERSequence derSeq = new DERSequence(ls);
            return derSeq.getEncoded();
        } catch (NoSuchAlgorithmException ex) {
            throw new Exception(ex.getMessage());
        } catch (SignatureException ex) {
            throw new Exception(ex.getMessage());
        } catch (InvalidKeyException ex) {
            throw new Exception(ex.getMessage());
        } catch (IOException ex) {
            throw new Exception(ex.getMessage());
        }
    }
}
