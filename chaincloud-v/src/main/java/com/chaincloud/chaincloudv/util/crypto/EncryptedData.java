package com.chaincloud.chaincloudv.util.crypto;

import android.util.Log;

import com.chaincloud.chaincloudv.util.qr.QRCodeUtil;
import com.chaincloud.chaincloudv.util.qr.SaltForQRCode;

public class EncryptedData {

    private byte[] encryptedData;
    private byte[] initialisationVector;
    private SaltForQRCode saltForQRCode;


    public EncryptedData(String str) {
        String[] strs = QRCodeUtil.splitOfPasswordSeed(str);
        if (strs.length != 3) {
            Log.e("EncryptedData", "decryption: EncryptedData format error");
        }
        initialisationVector = BitcoinUtils.hexStringToByteArray
                (strs[1]);
        encryptedData = BitcoinUtils.hexStringToByteArray(strs[0]);
        byte[] saltQRCodes = BitcoinUtils.hexStringToByteArray(strs[2]);
        saltForQRCode = new SaltForQRCode(saltQRCodes);
    }

    public EncryptedData(byte[] dataToEncrypt, CharSequence password) {
        this(dataToEncrypt, password, true, false);
    }

    public EncryptedData(byte[] dataToEncrypt, CharSequence password,
                         boolean isFromXRandom) {
        this(dataToEncrypt, password, true, isFromXRandom);
    }

    public EncryptedData(byte[] dataToEncrypt, CharSequence password, boolean isCompress,
                         boolean isFromXRandom) {
        KeyCrypterScrypt crypter = new KeyCrypterScrypt();
        byte[] salt = crypter.getSalt();
        EncryptedPrivateKey k = crypter.encrypt(dataToEncrypt, crypter.deriveKey(password));
        encryptedData = k.getEncryptedBytes();
        initialisationVector = k.getInitialisationVector();
        saltForQRCode = new SaltForQRCode(salt, isCompress, isFromXRandom);
    }

    public byte[] decrypt(CharSequence password) {
        KeyCrypterScrypt crypter = new KeyCrypterScrypt(saltForQRCode.getSalt());
        return crypter.decrypt(new EncryptedPrivateKey(initialisationVector, encryptedData), crypter.deriveKey(password));
    }

    public String toEncryptedString() {
        return BitcoinUtils.bytesToHexString(encryptedData).toUpperCase()
                + QRCodeUtil.QR_CODE_SPLIT + BitcoinUtils.bytesToHexString(initialisationVector).toUpperCase()
                + QRCodeUtil.QR_CODE_SPLIT + BitcoinUtils.bytesToHexString(saltForQRCode.getSalt()).toUpperCase();
    }

    public String toEncryptedStringForQRCode() {
        return BitcoinUtils.bytesToHexString(encryptedData).toUpperCase()
                + QRCodeUtil.QR_CODE_SPLIT + BitcoinUtils.bytesToHexString(initialisationVector).toUpperCase()
                + QRCodeUtil.QR_CODE_SPLIT + BitcoinUtils.bytesToHexString(saltForQRCode.getQrCodeSalt()).toUpperCase();
    }

    public String toEncryptedStringForQRCode(boolean isCompress, boolean isFromXRandom) {
        SaltForQRCode newSaltForQRCode = new SaltForQRCode(saltForQRCode.getSalt(), isCompress, isFromXRandom);
        return BitcoinUtils.bytesToHexString(encryptedData).toUpperCase()
                + QRCodeUtil.QR_CODE_SPLIT + BitcoinUtils.bytesToHexString(initialisationVector).toUpperCase()
                + QRCodeUtil.QR_CODE_SPLIT + BitcoinUtils.bytesToHexString(newSaltForQRCode.getQrCodeSalt()).toUpperCase();
    }

    public boolean isXRandom() {
        return saltForQRCode.isFromXRandom();
    }

    public boolean isCompressed() {
        return saltForQRCode.isCompressed();
    }

    public static String changePwd(String encryptStr, CharSequence oldPassword, CharSequence newPassword) {
        EncryptedData encrypted = new EncryptedData(encryptStr);
        return new EncryptedData(encrypted.decrypt(oldPassword), newPassword).toEncryptedString();
    }

    public static String changePwdKeepFlag(String encryptStr, CharSequence oldPassword, CharSequence newPassword) {
        EncryptedData encrypted = new EncryptedData(encryptStr);
        return new EncryptedData(encrypted.decrypt(oldPassword), newPassword, encrypted.isCompressed(), encrypted.isXRandom()).toEncryptedString();
    }
}
