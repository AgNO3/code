/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.03.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.internal;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.runtime.net.ad.ADException;
import eu.agno3.runtime.net.ad.ADRealm;
import eu.agno3.runtime.net.ad.NetlogonAuthenticator;
import eu.agno3.runtime.net.ad.NetlogonConnection;
import eu.agno3.runtime.net.ad.NetlogonOperations;
import eu.agno3.runtime.net.ad.msgs.DsrGetSiteName;
import eu.agno3.runtime.net.ad.msgs.NetrServerAuthenticate3;
import eu.agno3.runtime.net.ad.msgs.NetrServerReqChallenge;

import jcifs.dcerpc.DcerpcException;
import jcifs.dcerpc.DcerpcHandle;
import jcifs.util.Crypto;
import jcifs.util.Encdec;


/**
 * @author mbechler
 *
 */
public class NetlogonConnectionImpl implements AutoCloseable, NetlogonConnection {

    /**
     * 
     */
    private static final int DEFAULT_FLAGS = 0x6112C600;

    private static final Logger log = Logger.getLogger(NetlogonConnectionImpl.class);

    static final int AES_SUPPORT_FLAG = 0x01000000;
    static final int AES_SHA2_SUPPORT_FLAG = 0x00400000;

    private ADRealm realm;
    private int negotiatedFlags;
    private SecureRandom random;
    private byte[] sessionKey;
    private byte[] clientCredential;
    private byte[] serverCredential;

    private DcerpcHandle dcerpcHandle;

    private boolean invalid;


    /**
     * @param dcerpcHandle
     * @param realm
     * @param negotiatedFlags
     * @param random
     */
    public NetlogonConnectionImpl ( DcerpcHandle dcerpcHandle, ADRealm realm, int negotiatedFlags, SecureRandom random ) {
        this.dcerpcHandle = dcerpcHandle;
        this.realm = realm;
        this.negotiatedFlags = negotiatedFlags;
        this.random = random;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.NetlogonConnection#getRealm()
     */
    @Override
    public ADRealm getRealm () {
        return this.realm;
    }


    /**
     * @param dcerpcHandle
     * @param realm
     * @param random
     */
    public NetlogonConnectionImpl ( DcerpcHandle dcerpcHandle, ADRealm realm, SecureRandom random ) {
        this(dcerpcHandle, realm, DEFAULT_FLAGS, random);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.NetlogonConnection#getNetlogonOperations()
     */
    @Override
    public NetlogonOperations getNetlogonOperations () {
        return new NetlogonOperationsImpl(this);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.NetlogonConnection#getDcerpcHandle()
     */
    @Override
    public DcerpcHandle getDcerpcHandle () {
        return this.dcerpcHandle;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.NetlogonConnection#init()
     */
    @Override
    public void init () throws ADException, IOException {
        try {

            if ( !this.realm.isAllowLegacyCrypto() && ( this.negotiatedFlags & AES_SUPPORT_FLAG ) != AES_SUPPORT_FLAG ) {
                this.invalid = true;
                throw new ADException("Server does not support AES encryption, legacy crypto support is disabled"); //$NON-NLS-1$
            }

            byte[] clientChallenge = new byte[8];
            this.random.nextBytes(clientChallenge);

            if ( log.isTraceEnabled() ) {
                log.trace("Client challenge " + Hex.encodeHexString(clientChallenge)); //$NON-NLS-1$
            }

            NetrServerReqChallenge netrServerReqChallenge = new NetrServerReqChallenge(
                this.dcerpcHandle.getServer(),
                this.realm.getLocalNetbiosHostname(),
                clientChallenge);
            this.dcerpcHandle.sendrecv(netrServerReqChallenge);

            if ( log.isTraceEnabled() ) {
                log.debug("Server challenge " + Hex.encodeHexString(netrServerReqChallenge.getServerChallenge())); //$NON-NLS-1$
            }

            this.sessionKey = computeSessionKey(clientChallenge, netrServerReqChallenge.getServerChallenge());
            this.clientCredential = encryptSession(clientChallenge);

            if ( log.isTraceEnabled() ) {
                log.trace("Session key is " + Hex.encodeHexString(this.sessionKey)); //$NON-NLS-1$
                log.trace("Client credential is " + Hex.encodeHexString(this.clientCredential)); //$NON-NLS-1$
            }

            NetrServerAuthenticate3 netrServerAuthenticate3 = new NetrServerAuthenticate3(
                this.dcerpcHandle.getServer(),
                this.realm.getMachineAccount(),
                2,
                this.realm.getLocalNetbiosHostname(),
                this.clientCredential,
                this.negotiatedFlags);

            this.dcerpcHandle.sendrecv(netrServerAuthenticate3);

            byte[] calculatedServerCred = encryptSession(netrServerReqChallenge.getServerChallenge());

            if ( log.isTraceEnabled() ) {
                log.trace("Calc server credential is " + Hex.encodeHexString(calculatedServerCred)); //$NON-NLS-1$
                log.trace("Provided server credential is " + Hex.encodeHexString(netrServerAuthenticate3.getServerCredential())); //$NON-NLS-1$
            }

            if ( !Arrays.equals(calculatedServerCred, netrServerAuthenticate3.getServerCredential()) ) {
                throw new ADException("Session key negotiation failed: " + netrServerAuthenticate3.getResult().getErrorCode()); //$NON-NLS-1$
            }

            this.serverCredential = netrServerAuthenticate3.getServerCredential();
        }
        catch ( IOException e ) {
            this.invalid = true;
            throw e;
        }
        catch (
            ShortBufferException |
            NoSuchAlgorithmException |
            InvalidKeyException |
            NoSuchPaddingException |
            IllegalBlockSizeException |
            BadPaddingException |
            InvalidAlgorithmParameterException e ) {
            this.invalid = true;
            throw new ADException("Internal error", e); //$NON-NLS-1$
        }
    }


    /**
     * @return the sessionKey
     */
    public byte[] getSessionKey () {
        return this.sessionKey;
    }


    /**
     * @return the clientCredential
     */
    public byte[] getClientCredential () {
        return this.clientCredential;
    }


    /**
     * @return the serverCredential
     */
    public byte[] getServerCredential () {
        return this.serverCredential;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.NetlogonConnection#getNegotiatedFlags()
     */
    @Override
    public int getNegotiatedFlags () {
        return this.negotiatedFlags;
    }


    /**
     * {@inheritDoc}
     *
     * @throws ShortBufferException
     *
     * @see eu.agno3.runtime.net.ad.NetlogonConnection#encryptSession(byte[])
     */
    @Override
    public byte[] encryptSession ( byte[] input ) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, ShortBufferException {

        if ( ( this.negotiatedFlags & AES_SUPPORT_FLAG ) == AES_SUPPORT_FLAG ) {
            log.trace("Using AES encryption"); //$NON-NLS-1$
            return encryptAES(input);
        }

        if ( this.realm.isAllowLegacyCrypto() ) {
            log.trace("Using DES encryption"); //$NON-NLS-1$
            return encryptDES(input);
        }

        throw new NoSuchAlgorithmException("Legacy crypto disallowed"); //$NON-NLS-1$
    }


    protected byte[] encryptAES ( byte[] input ) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        Cipher aes = Cipher.getInstance("AES/CFB8/NoPadding"); //$NON-NLS-1$
        SecretKeySpec keySpec = new SecretKeySpec(this.sessionKey, "AES"); //$NON-NLS-1$
        aes.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(new byte[16]));
        return aes.doFinal(input);
    }


    protected byte[] encryptDES ( byte[] input ) throws ShortBufferException {
        byte[] k1 = new byte[7];
        byte[] k2 = new byte[7];
        System.arraycopy(this.sessionKey, 0, k1, 0, 7);
        System.arraycopy(this.sessionKey, 7, k2, 0, 7);

        Cipher k3 = Crypto.getDES(k1);
        Cipher k4 = Crypto.getDES(k2);

        byte[] output1 = new byte[8];
        byte[] output2 = new byte[8];

        k3.update(input, 0, input.length, output1);
        k4.update(output1, 0, output1.length, output2);
        return output2;
    }


    protected byte[] computeSessionKey ( byte[] clientChallenge, byte[] serverChallenge )
            throws NoSuchAlgorithmException, UnsupportedEncodingException, ADException, InvalidKeyException {

        if ( ( this.negotiatedFlags & AES_SUPPORT_FLAG ) == AES_SUPPORT_FLAG ) {
            log.trace("Using AES session key"); //$NON-NLS-1$
            return computeSessionKeyAES(this.realm, clientChallenge, serverChallenge);
        }

        if ( this.realm.isAllowLegacyCrypto() ) {
            log.trace("Using \"strong\" session key"); //$NON-NLS-1$
            return computeSessionKeyStrong(this.realm, clientChallenge, serverChallenge);
        }

        throw new NoSuchAlgorithmException("Legacy crypto disallowed"); //$NON-NLS-1$
    }


    protected static byte[] computeSessionKeyAES ( ADRealm realm, byte[] clientChallenge, byte[] serverChallenge )
            throws NoSuchAlgorithmException, UnsupportedEncodingException, ADException, InvalidKeyException {
        byte[] sharedSecret = getSharedSecret(realm);
        Mac mac = Mac.getInstance("HmacSHA256"); //$NON-NLS-1$
        mac.init(new SecretKeySpec(sharedSecret, StringUtils.EMPTY));
        mac.update(clientChallenge);
        mac.update(serverChallenge);
        byte[] dgst = mac.doFinal();
        byte[] truncated = new byte[16];
        System.arraycopy(dgst, 0, truncated, 0, 16);
        return truncated;
    }


    protected static byte[] computeSessionKeyStrong ( ADRealm realm, byte[] clientChallenge, byte[] serverChallenge )
            throws NoSuchAlgorithmException, UnsupportedEncodingException, ADException {
        byte[] sharedSecret = getSharedSecret(realm);
        MessageDigest messageDigest = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
        byte[] zeroes = {
            0, 0, 0, 0
        };
        messageDigest.update(zeroes, 0, 4);
        messageDigest.update(clientChallenge, 0, 8);
        messageDigest.update(serverChallenge, 0, 8);
        MessageDigest hmact64 = Crypto.getHMACT64(sharedSecret);
        hmact64.update(messageDigest.digest());
        return hmact64.digest();
    }


    /**
     * @param realm
     * @return
     * @throws UnsupportedEncodingException
     * @throws ADException
     */
    private static byte[] getSharedSecret ( ADRealm realm ) throws UnsupportedEncodingException, ADException {
        MessageDigest md4 = Crypto.getMD4();
        byte[] encMachPassword = realm.getMachinePassword().getBytes("UTF-16LE"); //$NON-NLS-1$
        md4.update(encMachPassword);
        return md4.digest();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.NetlogonConnection#authenticate()
     */
    @Override
    public NetlogonAuthenticator authenticate () throws ADException {
        int timestamp = (int) ( System.currentTimeMillis() / 1000 );
        int input = Encdec.dec_uint32le(this.clientCredential, 0) + timestamp;
        Encdec.enc_uint32le(input, this.clientCredential, 0);

        try {
            byte[] credential = encryptSession(this.clientCredential);
            return new NetlogonAuthenticator(credential, timestamp);
        }
        catch (
            InvalidKeyException |
            NoSuchAlgorithmException |
            NoSuchPaddingException |
            IllegalBlockSizeException |
            BadPaddingException |
            InvalidAlgorithmParameterException |
            ShortBufferException e ) {
            throw new ADException("Failed to build netlogon authenticator", e); //$NON-NLS-1$
        }

    }


    @Override
    public void validate ( NetlogonAuthenticator returnAuthenticator, boolean error ) throws ADException {
        int input = Encdec.dec_uint32le(this.clientCredential, 0) + 1;
        byte[] inputBytes = new byte[8];
        System.arraycopy(this.clientCredential, 0, inputBytes, 0, this.clientCredential.length);
        Encdec.enc_uint32le(input, inputBytes, 0);

        try {
            byte[] computed = encryptSession(inputBytes);

            if ( !Arrays.equals(computed, returnAuthenticator.getCredential()) ) {
                this.invalid = true;

                if ( log.isDebugEnabled() ) {
                    log.debug("Expected authenticator " + Arrays.toString(computed)); //$NON-NLS-1$
                    log.debug("Have authenticator " + Arrays.toString(returnAuthenticator.getCredential())); //$NON-NLS-1$
                }

                throw new ADException("Return credentials did not validate"); //$NON-NLS-1$
            }

            this.clientCredential = inputBytes;
            if ( log.isDebugEnabled() ) {
                // dumping this to the log should be more or less safe, as it's not valid anymore
                log.debug("Have authenticator " + Arrays.toString(returnAuthenticator.getCredential())); //$NON-NLS-1$
            }
        }
        catch (
            InvalidKeyException |
            NoSuchAlgorithmException |
            NoSuchPaddingException |
            IllegalBlockSizeException |
            BadPaddingException |
            InvalidAlgorithmParameterException |
            ShortBufferException e ) {
            this.invalid = true;
            throw new ADException("Failed to validate netlogon authenticator", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.NetlogonConnection#check()
     */
    @Override
    public boolean check () {
        if ( this.invalid ) {
            return false;
        }

        try {
            DsrGetSiteName req = new DsrGetSiteName(this.dcerpcHandle.getServer(), this.realm.getNetbiosDomainName());
            this.dcerpcHandle.sendrecv(req);
        }
        catch ( DcerpcException e ) {
            if ( e.getErrorCode() == 0x0000077F ) {
                log.trace("Recieved ERROR_NO_SITENAME", e); //$NON-NLS-1$
                return true;
            }
        }
        catch ( IOException e ) {
            log.debug("Health check failed", e); //$NON-NLS-1$
            return false;
        }
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.NetlogonConnection#close()
     */
    @Override
    public void close () throws ADException {
        try {
            log.debug("close called"); //$NON-NLS-1$
            this.invalid = true;
            this.dcerpcHandle.close();
        }
        catch ( IOException e ) {
            throw new ADException("Failed to close DCERPC channel", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.NetlogonConnection#fail()
     */
    @Override
    public void fail () {
        this.invalid = true;
        try {
            this.dcerpcHandle.close();
        }
        catch ( IOException e ) {
            log.debug("Failed to close channel", e); //$NON-NLS-1$
        }
    }

}
