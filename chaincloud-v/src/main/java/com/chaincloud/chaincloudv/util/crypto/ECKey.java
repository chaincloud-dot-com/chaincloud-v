/*
 * Copyright 2014 http://Bither.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chaincloud.chaincloudv.util.crypto;

import android.util.Log;

import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1Integer;
import org.spongycastle.asn1.DERBitString;
import org.spongycastle.asn1.DEROctetString;
import org.spongycastle.asn1.DERSequenceGenerator;
import org.spongycastle.asn1.DERTaggedObject;
import org.spongycastle.asn1.DLSequence;
import org.spongycastle.asn1.sec.SECNamedCurves;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.asn1.x9.X9IntegerConverter;
import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.ec.CustomNamedCurves;
import org.spongycastle.crypto.generators.ECKeyPairGenerator;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECKeyGenerationParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.crypto.signers.HMacDSAKCalculator;
import org.spongycastle.math.ec.ECAlgorithms;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.math.ec.FixedPointUtil;
import org.spongycastle.math.ec.custom.sec.SecP256K1Curve;
import org.spongycastle.util.encoders.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.Arrays;

/**
 * <p>Represents an elliptic curve public and (optionally) private key, usable for digital
 * signatures but not encryption.
 * Creating a new ECKey with the empty constructor will generate a new random keypair. Other
 * constructors can be used
 * when you already have the public or private parts. If you create a key with only the public
 * part, you can check
 * signatures but not create them.</p>
 * <p/>
 * <p>ECKey also provides access to Bitcoin-Qt compatible text message signing, as accessible via
 * the UI or JSON-RPC.
 * This is slightly different to signing raw bytes - if you want to sign your own data and it
 * won't be exposed as
 * text to people, you don't want to use this. If in doubt, ask on the mailing list.</p>
 * <p/>
 * <p>The ECDSA algorithm supports <i>key recovery</i> in which a signature plus a couple of
 * discriminator bits can
 * be reversed to find the public key used to calculate it. This can be convenient when you have
 * a message and a
 * signature and want to find out who signed it, rather than requiring the user to provide the
 * expected identity.</p>
 */
public class ECKey implements Serializable {
    /**
     * The parameters of the secp256k1 curve that Bitcoin uses.
     */
    public static final ECDomainParameters CURVE;
    public static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");

    /**
     * Equal to CURVE.getN().shiftRight(1), used for canonicalising the S value of a signature.
     * If you aren't
     * sure what this is about, you can ignore it.
     */
    public static final BigInteger HALF_CURVE_ORDER;

    private static final long serialVersionUID = -728224901792295832L;

    static {
        // Tell Bouncy Castle to precompute data that's needed during secp256k1 calculations.
        // Increasing the width
        // number makes calculations faster, but at a cost of extra memory usage and with
        // decreasing returns. 12 was
        // picked after consulting with the BC team.
        FixedPointUtil.precompute(CURVE_PARAMS.getG(), 12);
        CURVE = new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS
                .getN(), CURVE_PARAMS.getH());
        HALF_CURVE_ORDER = CURVE_PARAMS.getN().shiftRight(1);

    }

    // The two parts of the key. If "priv" is set, "pub" can always be calculated. If "pub" is
    // set but not "priv", we
    // can only verify signatures not make them.
    // TODO: Redesign this class to use consistent internals and more efficient serialization.
    protected BigInteger priv;
    protected byte[] pub;

    // Transient because it's calculated on demand.
    transient private byte[] pubKeyHash;

    public final static ECPoint compressPoint(ECPoint uncompressed) {
        return CURVE.getCurve().decodePoint(uncompressed.getEncoded(true));
    }

    public final static ECPoint checkPoint(byte[] pubs) {
        return CURVE.getCurve().decodePoint(pubs);
    }


    /**
     * Creates an ECKey given the private key only.  The public key is calculated from it (this
     * is slow)
     */
    public ECKey(BigInteger privKey) {
        this(privKey, (byte[]) null);
    }

    /**
     * Creates an ECKey given only the private key bytes. This is the same as using the
     * BigInteger constructor, but
     * is more convenient if you are importing a key from elsewhere. The public key will be
     * automatically derived
     * from the private key.
     */
    public ECKey(byte[] privKeyBytes, byte[] pubKey) {
        this(privKeyBytes == null ? null : new BigInteger(1, privKeyBytes), pubKey);
    }

    /**
     * Creates an ECKey given either the private key only, the public key only, or both. If only
     * the private key
     * is supplied, the public key will be calculated from it (this is slow). If both are
     * supplied, it's assumed
     * the public key already correctly matches the public key. If only the public key is
     * supplied, this ECKey cannot
     * be used for signing.
     *
     * @param compressed If set to true and pubKey is null, the derived public key will be in
     *                   compressed form.
     */
    public ECKey(BigInteger privKey, byte[] pubKey, boolean compressed) {
        if (privKey == null && pubKey == null) {
            throw new IllegalArgumentException("ECKey requires at least private or public key");
        }
        this.priv = privKey;
        this.pub = null;
        if (pubKey == null) {
            // Derive public from private.
            this.pub = publicKeyFromPrivate(privKey, compressed);
        } else {
            // We expect the pubkey to be in regular encoded form, just as a BigInteger.
            // Therefore the first byte is
            // a special marker byte.
            // TODO: This is probably not a useful API and may be confusing.
            this.pub = pubKey;
        }
    }

    /**
     * Creates an ECKey given either the private key only, the public key only, or both. If only
     * the private key
     * is supplied, the public key will be calculated from it (this is slow). If both are
     * supplied, it's assumed
     * the public key already correctly matches the public key. If only the public key is
     * supplied, this ECKey cannot
     * be used for signing.
     */
    private ECKey(BigInteger privKey, byte[] pubKey) {
        this(privKey, pubKey, true);
    }

    public boolean isPubKeyOnly() {
        return priv == null;
    }

    public boolean hasPrivKey() {
        return priv != null;
    }

    /**
     * Output this ECKey as an ASN.1 encoded private key, as understood by OpenSSL or used by the
     * BitCoin reference
     * implementation in its wallet storage format.
     */
    public byte[] toASN1() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(400);
            DERSequenceGenerator seq = new DERSequenceGenerator(baos);
            seq.addObject(new ASN1Integer(1)); // version
            seq.addObject(new DEROctetString(priv.toByteArray()));
            seq.addObject(new DERTaggedObject(0, SECNamedCurves.getByName("secp256k1")
                    .toASN1Primitive()));
            seq.addObject(new DERTaggedObject(1, new DERBitString(getPubKey())));
            seq.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);  // Cannot happen, writing to memory stream.
        }
    }

    /**
     * Returns public key bytes from the given private key. To convert a byte array into a
     * BigInteger, use <tt>
     * new BigInteger(1, bytes);</tt>
     */
    public static byte[] publicKeyFromPrivate(BigInteger privKey, boolean compressed) {
        ECPoint point = CURVE.getG().multiply(privKey);
        if (compressed) {
            point = compressPoint(point);
        }
        return point.getEncoded();
    }

    /**
     * Gets the hash160 form of the public key (as seen in addresses).
     */
    public byte[] getPubKeyHash() {
        if (pubKeyHash == null) {
            pubKeyHash = BitcoinUtils.sha256hash160(this.pub);
        }
        return pubKeyHash;
    }

    /**
     * Gets the raw public key value. This appears in transaction scriptSigs. Note that this is
     * <b>not</b> the same
     * as the pubKeyHash/address.
     */
    public byte[] getPubKey() {
        return pub;
    }

    /**
     * Gets the public key in the form of an elliptic curve point object from Bouncy Castle.
     */
    public ECPoint getPubKeyPoint() {
        return CURVE.getCurve().decodePoint(pub);
    }

    /**
     * Returns whether this key is using the compressed form or not. Compressed pubkeys are only
     * 33 bytes, not 64.
     */
    public boolean isCompressed() {
        return pub.length == 33;
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("pub:").append(BitcoinUtils.bytesToHexString(pub));
        return b.toString();
    }

    /**
     * Produce a string rendering of the ECKey INCLUDING the private key.
     * Unless you absolutely need the private key it is better for security reasons to just use
     * toString().
     */
    public String toStringWithPrivate() {
        StringBuilder b = new StringBuilder();
        b.append(toString());
        if (priv != null) {
            b.append(" priv:").append(BitcoinUtils.bytesToHexString(priv.toByteArray()));
        }
        return b.toString();
    }

    /**
     * Returns the address that corresponds to the public part of this ECKey. Note that an
     * address is derived from
     * the RIPEMD-160 hash of the public key and is not the public key itself (which is too large
     * to be convenient).
     */
    public String toAddress() {
        return BitcoinUtils.toAddress(BitcoinUtils.sha256hash160(pub));
    }

    /**
     * Clears all the ECKey private key contents from memory.
     * WARNING - this method irreversibly deletes the private key information.
     * It turns the ECKEy into a watch only key.
     */
    public void clearPrivateKey() {
        priv = BigInteger.ZERO;
    }

    /**
     * Groups the two components that make up a signature, and provides a way to encode to DER
     * form, which is
     * how ECDSA signatures are represented when embedded in other data structures in the Bitcoin
     * protocol. The raw
     * components can be useful for doing further EC maths on them.
     */
    public static class ECDSASignature {
        /**
         * The two components of the signature.
         */
        public BigInteger r, s;

        /**
         * Constructs a signature with the given components. Does NOT automatically canonicalise
         * the signature.
         */
        public ECDSASignature(BigInteger r, BigInteger s) {
            this.r = r;
            this.s = s;
        }

        /**
         * Will automatically adjust the S component to be less than or equal to half the curve
         * order, if necessary.
         * This is required because for every signature (r,s) the signature (r, -s (mod N)) is a
         * valid signature of
         * the same message. However, we dislike the ability to modify the bits of a Bitcoin
         * transaction after it's
         * been signed, as that violates various assumed invariants. Thus in future only one of
         * those forms will be
         * considered legal and the other will be banned.
         */
        public void ensureCanonical() {
            if (s.compareTo(HALF_CURVE_ORDER) > 0) {
                // The order of the curve is the number of valid points that exist on that curve.
                // If S is in the upper
                // half of the number of valid points, then bring it back to the lower half.
                // Otherwise, imagine that
                //    N = 10
                //    s = 8, so (-8 % 10 == 2) thus both (r, 8) and (r, 2) are valid solutions.
                //    10 - 8 == 2, giving us always the latter solution, which is canonical.
                s = CURVE.getN().subtract(s);
            }
        }

        /**
         * DER is an international standard for serializing data structures which is widely used
         * in cryptography.
         * It's somewhat like protocol buffers but less convenient. This method returns a
         * standard DER encoding
         * of the signature, as recognized by OpenSSL and other libraries.
         */
        public byte[] encodeToDER() {
            try {
                return derByteStream().toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);  // Cannot happen.
            }
        }

        public static ECDSASignature decodeFromDER(byte[] bytes) {
            try {
                ASN1InputStream decoder = new ASN1InputStream(bytes);
                DLSequence seq = (DLSequence) decoder.readObject();
                ASN1Integer r, s;
                try {
                    r = (ASN1Integer) seq.getObjectAt(0);
                    s = (ASN1Integer) seq.getObjectAt(1);
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException(e);
                }
                decoder.close();
                // OpenSSL deviates from the DER spec by interpreting these values as unsigned,
                // though they should not be
                // Thus, we always use the positive versions. See: http://r6
                // .ca/blog/20111119T211504Z.html
                return new ECDSASignature(r.getPositiveValue(), s.getPositiveValue());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        protected ByteArrayOutputStream derByteStream() throws IOException {
            // Usually 70-ic_launcher bytes.
            ByteArrayOutputStream bos = new ByteArrayOutputStream(72);
            DERSequenceGenerator seq = new DERSequenceGenerator(bos);
            seq.addObject(new ASN1Integer(r));
            seq.addObject(new ASN1Integer(s));
            seq.close();
            return bos;
        }
    }

    public ECDSASignature sign(byte[] input) {
        ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
        ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(priv, CURVE);
        signer.init(true, privKey);
        BigInteger[] components = signer.generateSignature(input);
        final ECDSASignature signature = new ECDSASignature(components[0], components[1]);
        signature.ensureCanonical();
        return signature;
    }

    public static boolean verify(byte[] data, ECDSASignature signature, byte[] pub) {
        if (NativeSecp256k1.enabled) {
            return NativeSecp256k1.verify(data, signature.encodeToDER(), pub);
        }

        ECDSASigner signer = new ECDSASigner();
        ECPublicKeyParameters params = new ECPublicKeyParameters(CURVE.getCurve().decodePoint
                (pub), CURVE);
        signer.init(false, params);
        try {
            return signer.verifySignature(data, signature.r, signature.s);
        } catch (NullPointerException e) {
            // Bouncy Castle contains a bug that can cause NPEs given specially crafted
            // signatures. Those signatures
            // are inherently invalid/attack sigs so we just fail them here rather than crash the
            // thread.
            Log.e("ECKey", "Caught NPE inside bouncy castle");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verifies the given ASN.1 encoded ECDSA signature against a hash using the public key.
     *
     * @param data      Hash of the data to verify.
     * @param signature ASN.1 encoded signature.
     * @param pub       The public key bytes to use.
     */
    public static boolean verify(byte[] data, byte[] signature, byte[] pub) {
        if (NativeSecp256k1.enabled) {
            return NativeSecp256k1.verify(data, signature, pub);
        }
        return verify(data, ECDSASignature.decodeFromDER(signature), pub);
    }

    /**
     * Verifies the given ASN.1 encoded ECDSA signature against a hash using the public key.
     *
     * @param data      Hash of the data to verify.
     * @param signature ASN.1 encoded signature.
     */
    public boolean verify(byte[] data, byte[] signature) {
        return ECKey.verify(data, signature, getPubKey());
    }

    /**
     * Verifies the given R/S pair (signature) against a hash using the public key.
     */
    public boolean verify(Sha256Hash sigHash, ECDSASignature signature) {
        return ECKey.verify(sigHash.getBytes(), signature, getPubKey());
    }

    /**
     * Returns true if this pubkey is canonical, i.e. the correct length taking into account
     * compression.
     */
    public boolean isPubKeyCanonical() {
        return isPubKeyCanonical(pub);
    }

    /**
     * Returns true if the given pubkey is canonical, i.e. the correct length taking into account
     * compression.
     */
    public static boolean isPubKeyCanonical(byte[] pubkey) {
        if (pubkey.length < 33) {
            return false;
        }
        if (pubkey[0] == 0x04) {
            // Uncompressed pubkey
            if (pubkey.length != 65) {
                return false;
            }
        } else if (pubkey[0] == 0x02 || pubkey[0] == 0x03) {
            // Compressed pubkey
            if (pubkey.length != 33) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    public String signMessage(String message) {
        byte[] data = BitcoinUtils.formatMessageForSigning(message);
        byte[] hash = BitcoinUtils.doubleDigest(data);
        byte[] sigData = signHash(hash);
        return new String(Base64.encode(sigData), Charset.forName("UTF-8"));
    }

    public byte[] signHash(byte[] hash) {
        ECDSASignature sig = sign(hash);
        // Now we have to work backwards to figure out the recId needed to recover the signature.
        int recId = -1;
        for (int i = 0;
             i < 4;
             i++) {
            ECKey k = ECKey.recoverFromSignature(i, sig, hash, isCompressed());
            if (k != null && Arrays.equals(k.pub, pub)) {
                recId = i;
                break;
            }
        }
        if (recId == -1) {
            throw new RuntimeException("Could not construct a recoverable key. This should never " +
                    "" + "happen.");
        }
        int headerByte = recId + 27 + (isCompressed() ? 4 : 0);
        byte[] sigData = new byte[65];  // 1 header + 32 bytes for R + 32 bytes for S
        sigData[0] = (byte) headerByte;
        System.arraycopy(BitcoinUtils.bigIntegerToBytes(sig.r, 32), 0, sigData, 1, 32);
        System.arraycopy(BitcoinUtils.bigIntegerToBytes(sig.s, 32), 0, sigData, 33, 32);
        return sigData;
    }

    /**
     * Given an arbitrary piece of text and a Bitcoin-format message signature encoded in base64,
     * returns an ECKey
     * containing the public key that was used to sign it. This can then be compared to the
     * expected public key to
     * determine if the signature was correct. These sorts of signatures are compatible with the
     * Bitcoin-Qt/bitcoind
     * format generated by signmessage/verifymessage RPCs and GUI menu options. They are intended
     * for humans to verify
     * their communications with each other, hence the base64 format and the fact that the input
     * is text.
     *
     * @param message         Some piece of human readable text.
     * @param signatureBase64 The Bitcoin-format message signature in base64
     * @throws SignatureException If the public key could not be recovered or if there was a
     *                            signature format error.
     */
    public static ECKey signedMessageToKey(String message, String signatureBase64) throws
            SignatureException {
        byte[] signatureEncoded;
        try {
            signatureEncoded = Base64.decode(signatureBase64);
        } catch (RuntimeException e) {
            // This is what you get back from Bouncy Castle if base64 doesn't decode :(
            throw new SignatureException("Could not decode base64", e);
        }
        byte[] messageBytes = BitcoinUtils.formatMessageForSigning(message);
        return signedMessageToKey(messageBytes, signatureEncoded);

    }

    public static ECKey signedMessageToKey(byte[] messageBytes, byte[] signatureEncoded) throws
            SignatureException {
        // Parse the signature bytes into r/s and the selector value.
        if (signatureEncoded.length < 65) {
            throw new SignatureException("Signature truncated, expected 65 bytes and got " +
                    signatureEncoded.length);
        }
        int header = signatureEncoded[0] & 0xFF;
        // The header byte: 0x1B = first key with even y, 0x1C = first key with odd y,
        //                  0x1D = second key with even y, 0x1E = second key with odd y
        if (header < 27 || header > 34) {
            throw new SignatureException("Header byte out of range: " + header);
        }
        BigInteger r = new BigInteger(1, Arrays.copyOfRange(signatureEncoded, 1, 33));
        BigInteger s = new BigInteger(1, Arrays.copyOfRange(signatureEncoded, 33, 65));
        ECDSASignature sig = new ECDSASignature(r, s);
        // Note that the C++ code doesn't actually seem to specify any character encoding.
        // Presumably it's whatever
        // JSON-SPIRIT hands back. Assume UTF-8 for now.
        byte[] messageHash = BitcoinUtils.doubleDigest(messageBytes);
        boolean compressed = false;
        if (header >= 31) {
            compressed = true;
            header -= 4;
        }
        int recId = header - 27;
        ECKey key = ECKey.recoverFromSignature(recId, sig, messageHash, compressed);
        if (key == null) {
            throw new SignatureException("Could not recover public key from signature");
        }
        return key;
    }

    public void verifyMessage(String message, String signatureBase64) throws SignatureException {
        ECKey key = ECKey.signedMessageToKey(message, signatureBase64);
        if (!Arrays.equals(key.getPubKey(), pub)) {
            throw new SignatureException("Signature did not match for message");
        }
    }

    public static ECKey recoverFromSignature(int recId, ECDSASignature sig, byte[] message,
                                             boolean compressed) {
        ECPoint q = recoverECPointFromSignature(recId, sig, message);
        if (q != null) {
            return new ECKey((BigInteger) null, (byte[]) q.getEncoded(compressed));
        } else {
            return null;
        }
    }

    public static ECPoint recoverECPointFromSignature(int recId, ECDSASignature sig, byte[]
            message) {
        BigInteger n = CURVE.getN();
        BigInteger i = BigInteger.valueOf((long) recId / 2L);
        BigInteger x = sig.r.add(i.multiply(n));
        BigInteger prime = SecP256K1Curve.q;
        if (x.compareTo(prime) >= 0) {
            return null;
        } else {
            ECPoint R = decompressKey(x, (recId & 1) == 1);
            if (!R.multiply(n).isInfinity()) {
                return null;
            } else {
                BigInteger e = new BigInteger(1, message);
                BigInteger eInv = BigInteger.ZERO.subtract(e).mod(n);
                BigInteger rInv = sig.r.modInverse(n);
                BigInteger srInv = rInv.multiply(sig.s).mod(n);
                BigInteger eInvrInv = rInv.multiply(eInv).mod(n);
                ECPoint q = ECAlgorithms.sumOfTwoMultiplies(CURVE.getG(), eInvrInv, R, srInv);
                return q;
            }

        }
    }

    /**
     * Decompress a compressed public key (x co-ord and low-bit of y-coord).
     */
    private static ECPoint decompressKey(BigInteger xBN, boolean yBit) {
        X9IntegerConverter x9 = new X9IntegerConverter();
        byte[] compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(CURVE.getCurve()));
        compEnc[0] = (byte) (yBit ? 0x03 : 0x02);
        return CURVE.getCurve().decodePoint(compEnc);
    }

    /**
     * Generates an entirely new keypair. Point compression is used so the resulting public key will be 33 bytes
     * (32 for the co-ordinate and 1 byte to represent the y bit).
     */
    public static ECKey generateECKey(SecureRandom secureRandom) {

        ECKeyPairGenerator generator = new ECKeyPairGenerator();
        ECKeyGenerationParameters keygenParams = new ECKeyGenerationParameters(CURVE, secureRandom);
        generator.init(keygenParams);
        AsymmetricCipherKeyPair keypair = generator.generateKeyPair();
        ECPrivateKeyParameters privParams = (ECPrivateKeyParameters) keypair.getPrivate();
        ECPublicKeyParameters pubParams = (ECPublicKeyParameters) keypair.getPublic();
        BigInteger priv = privParams.getD();
        boolean compressed = true;
        ECKey ecKey = new ECKey(priv, pubParams.getQ().getEncoded(compressed));
//        ecKey.setCreationTimeSeconds(Utils.currentTimeSeconds());
        return ecKey;
    }

    /**
     * Returns a 32 byte array containing the private key, or null if the key is encrypted or
     * public only
     */
    public byte[] getPrivKeyBytes() {
        return BitcoinUtils.bigIntegerToBytes(priv, 32);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof ECKey)) {
            return false;
        }

        ECKey ecKey = (ECKey) o;

        return Arrays.equals(pub, ecKey.pub);
    }

    @Override
    public int hashCode() {
        // Public keys are random already so we can just use a part of them as the hashcode. Read
        // from the start to
        // avoid picking up the type code (compressed vs uncompressed) which is tacked on the end.
        return (pub[0] & 0xFF) | ((pub[1] & 0xFF) << 8) | ((pub[2] & 0xFF) << 16) | ((pub[3] &
                0xFF) << 24);
    }
}
