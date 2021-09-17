/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.pkcs11.internal;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.AuthProvider;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x9.X962Parameters;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9ECPoint;
import org.bouncycastle.jce.provider.JCEECPublicKey;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.keystore.KeyType;
import eu.agno3.runtime.crypto.pkcs11.PKCS11Util;

import sun.security.pkcs11.SunPKCS11;
import sun.security.pkcs11.wrapper.CK_ATTRIBUTE;
import sun.security.pkcs11.wrapper.PKCS11;
import sun.security.pkcs11.wrapper.PKCS11Constants;
import sun.security.pkcs11.wrapper.PKCS11Exception;


/**
 * @author mbechler
 *
 */
@Component ( service = PKCS11Util.class )
public class PKCS11UtilImpl implements PKCS11Util {

    private static final Logger log = Logger.getLogger(PKCS11UtilImpl.class);

    /**
     * 
     */
    private static final String PKCS11 = "PKCS11"; //$NON-NLS-1$

    private static final Field P11_GET_KEY_ID;


    static {
        Field keyIdField = null;
        try {
            Class<?> p11Key = Class.forName("sun.security.pkcs11.P11Key"); //$NON-NLS-1$
            keyIdField = p11Key.getDeclaredField("keyID"); //$NON-NLS-1$
            keyIdField.setAccessible(true);
        }
        catch ( Exception e ) {
            log.error("Failed to get keyId field", e); //$NON-NLS-1$
        }
        P11_GET_KEY_ID = keyIdField;
    }


    /**
     * 
    */
    public PKCS11UtilImpl () {}


    /**
     * {@inheritDoc}
     * 
     * @throws CryptoException
     *
     * @see eu.agno3.runtime.crypto.pkcs11.PKCS11Util#getProviderFor(java.lang.String, java.lang.String,
     *      java.lang.String, int)
     */
    @Override
    public AuthProvider getProviderFor ( String libraryName, String name, String pin, int slotIndex ) throws CryptoException {
        return setupPINCallback(pin, getProviderInternal(libraryName, name, null, slotIndex, null, null));
    }


    /**
     * {@inheritDoc}
     * 
     * @throws CryptoException
     * 
     *
     * @see eu.agno3.runtime.crypto.pkcs11.PKCS11Util#getProviderFor(java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, int, java.lang.String, java.lang.String)
     */
    @Override
    public AuthProvider getProviderFor ( String library, String id, String pin, String slotId, int slotIndex, String extraConfig, String initArgs )
            throws CryptoException {
        return setupPINCallback(pin, getProviderInternal(library, id, slotId, slotIndex, extraConfig, initArgs));
    }


    private static SunPKCS11 getProviderInternal ( String library, String id, String slotId, int slotIndex, String extraConfig, String initArgs )
            throws CryptoException {
        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Creating PKCS11 provider (library=%s id=%s)", library, id)); //$NON-NLS-1$
        }

        ExecutorService exec = Executors.newSingleThreadExecutor();
        Future<SunPKCS11> submit = exec.submit(new Callable<SunPKCS11>() {

            @Override
            public SunPKCS11 call () throws Exception {
                return new sun.security.pkcs11.SunPKCS11(getConfig(id, library, slotId, slotIndex, extraConfig, initArgs));
            }
        });

        try {
            return submit.get(5, TimeUnit.SECONDS);
        }
        catch ( ExecutionException e ) {
            if ( e.getCause() instanceof ProviderException ) {
                if ( e.getCause() instanceof PKCS11Exception ) {
                    throw new CryptoException(e.getCause());
                }
                throw (ProviderException) e.getCause();
            }
            throw new CryptoException("Unhandled exception in PKCS11 connection", e); //$NON-NLS-1$
        }
        catch (
            InterruptedException |
            TimeoutException e ) {
            throw new CryptoException("Interrupted waiting for PKCS11 connnection", e); //$NON-NLS-1$
        }
    }


    private static PKCS11 getWrapperFor ( AuthProvider prov ) throws CryptoException {
        SunPKCS11 pkcs11Prov = unwrapProvider(prov);
        try {
            Field field = pkcs11Prov.getClass().getDeclaredField("p11"); //$NON-NLS-1$
            field.setAccessible(true);
            return (sun.security.pkcs11.wrapper.PKCS11) field.get(pkcs11Prov);
        }
        catch (
            NoSuchFieldException |
            SecurityException |
            IllegalArgumentException |
            IllegalAccessException e ) {
            throw new CryptoException("Cannot access P11 Wrapper instance", e); //$NON-NLS-1$
        }
    }


    static Object getSessionFor ( AuthProvider prov ) throws CryptoException {
        SunPKCS11 pkcs11Prov = unwrapProvider(prov);

        try {
            log.trace("Open session"); //$NON-NLS-1$
            Method getToken = pkcs11Prov.getClass().getDeclaredMethod("getToken"); //$NON-NLS-1$
            getToken.setAccessible(true);
            Object token = getToken.invoke(pkcs11Prov);

            Method getObjSession = token.getClass().getDeclaredMethod("getObjSession"); //$NON-NLS-1$
            getObjSession.setAccessible(true);
            return getObjSession.invoke(token);
        }
        catch (
            SecurityException |
            IllegalArgumentException |
            IllegalAccessException |
            InvocationTargetException |
            NoSuchMethodException e ) {
            throw new CryptoException("Cannot access P11 Token instance", e); //$NON-NLS-1$
        }
    }


    static void releaseSession ( AuthProvider prov, Object session ) throws CryptoException {
        SunPKCS11 pkcs11Prov = unwrapProvider(prov);

        try {
            log.trace("Release session"); //$NON-NLS-1$
            Method getToken = pkcs11Prov.getClass().getDeclaredMethod("getToken"); //$NON-NLS-1$
            getToken.setAccessible(true);
            Object token = getToken.invoke(pkcs11Prov);

            Method getObjSession = token.getClass().getDeclaredMethod("releaseSession", session.getClass()); //$NON-NLS-1$
            getObjSession.setAccessible(true);
            getObjSession.invoke(token, session);
        }
        catch (
            SecurityException |
            IllegalArgumentException |
            IllegalAccessException |
            InvocationTargetException |
            NoSuchMethodException e ) {
            throw new CryptoException("Cannot access P11 Token instance", e); //$NON-NLS-1$
        }
    }


    static void killSession ( AuthProvider prov, Object session ) throws CryptoException {
        SunPKCS11 pkcs11Prov = unwrapProvider(prov);

        try {
            log.trace("Kill session"); //$NON-NLS-1$
            Method getToken = pkcs11Prov.getClass().getDeclaredMethod("getToken"); //$NON-NLS-1$
            getToken.setAccessible(true);
            Object token = getToken.invoke(pkcs11Prov);

            Method killSession = token.getClass().getDeclaredMethod("killSession", session.getClass()); //$NON-NLS-1$
            killSession.setAccessible(true);
            killSession.invoke(token, session);
        }
        catch (
            SecurityException |
            IllegalArgumentException |
            IllegalAccessException |
            InvocationTargetException |
            NoSuchMethodException e ) {
            throw new CryptoException("Cannot access P11 Token instance", e); //$NON-NLS-1$
        }
    }


    static void killAllSessions ( AuthProvider prov ) throws CryptoException {
        SunPKCS11 pkcs11Prov = unwrapProvider(prov);

        try {
            log.trace("Kill all sessions"); //$NON-NLS-1$
            Object session = getSessionFor(prov);
            releaseSession(prov, session);

            Method getToken = pkcs11Prov.getClass().getDeclaredMethod("getToken"); //$NON-NLS-1$
            getToken.setAccessible(true);
            Object token = getToken.invoke(pkcs11Prov);

            Method killSession = token.getClass().getDeclaredMethod("killSession", session.getClass()); //$NON-NLS-1$
            killSession.setAccessible(true);

            Field sessionManagerField = token.getClass().getDeclaredField("sessionManager"); //$NON-NLS-1$
            sessionManagerField.setAccessible(true);
            Object sessionManager = sessionManagerField.get(token);

            Field objSessionsPoolField = sessionManager.getClass().getDeclaredField("objSessions"); //$NON-NLS-1$
            objSessionsPoolField.setAccessible(true);
            Object objSessionPool = objSessionsPoolField.get(sessionManager);

            killAllSessions(objSessionPool, token, killSession);

            Field opSessionsPoolField = sessionManager.getClass().getDeclaredField("opSessions"); //$NON-NLS-1$
            opSessionsPoolField.setAccessible(true);
            Object opSessionPool = opSessionsPoolField.get(sessionManager);

            killAllSessions(opSessionPool, token, killSession);

        }
        catch (
            SecurityException |
            IllegalArgumentException |
            IllegalAccessException |
            InvocationTargetException |
            NoSuchMethodException |
            NoSuchFieldException e ) {
            throw new CryptoException("Cannot access P11 Token instance", e); //$NON-NLS-1$
        }
    }


    /**
     * @param pool
     * @param token
     * @param killSession
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    private static void killAllSessions ( Object pool, Object token, Method killSession )
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Field opSessionsPoolField = pool.getClass().getDeclaredField("pool"); //$NON-NLS-1$
        opSessionsPoolField.setAccessible(true);
        @SuppressWarnings ( "unchecked" )
        Deque<Object> sessions = (Deque<Object>) opSessionsPoolField.get(pool);

        for ( Object session : sessions ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Killing session " + session); //$NON-NLS-1$
            }
            killSession.invoke(token, session);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws CryptoException
     *
     * @see eu.agno3.runtime.crypto.pkcs11.PKCS11Util#close(java.security.AuthProvider)
     */
    @Override
    public void close ( AuthProvider provider ) throws CryptoException {
        killAllSessions(provider);
        // logout does not seem to work properly
    }


    private static long getSessionId ( Object obj ) throws CryptoException {
        try {
            Method getToken = obj.getClass().getDeclaredMethod("id"); //$NON-NLS-1$
            getToken.setAccessible(true);
            return (long) getToken.invoke(obj);
        }
        catch (
            SecurityException |
            IllegalArgumentException |
            IllegalAccessException |
            InvocationTargetException |
            NoSuchMethodException e ) {
            throw new CryptoException("Failed to get session id", e); //$NON-NLS-1$
        }
    }


    private static long getKeyId ( Key k ) throws CryptoException {
        try {
            if ( P11_GET_KEY_ID == null ) {
                throw new CryptoException("Key id field not found, incompatible library"); //$NON-NLS-1$
            }
            return P11_GET_KEY_ID.getLong(k);
        }
        catch (
            SecurityException |
            IllegalArgumentException |
            IllegalAccessException e ) {
            throw new CryptoException("Failed to get key id", e); //$NON-NLS-1$
        }
    }


    /**
     * @param prov
     * @return
     * @throws CryptoException
     */
    protected static SunPKCS11 unwrapProvider ( AuthProvider prov ) throws CryptoException {
        if ( ! ( prov instanceof SunPKCS11 ) ) {
            throw new CryptoException("Not a SunPKCS11 provider"); //$NON-NLS-1$
        }

        return (SunPKCS11) prov;
    }


    /**
     * @param sunPKCS11
     * @return
     */
    private static AuthProvider setupPINCallback ( String pin, SunPKCS11 provider ) {
        provider.setCallbackHandler(new PINCallBackHandler(pin));
        return provider;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws CryptoException
     *
     * @see eu.agno3.runtime.crypto.pkcs11.PKCS11Util#getProviderFor(java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public AuthProvider getProviderFor ( String libraryName, String name, String pin, String slotId ) throws CryptoException {
        return setupPINCallback(pin, getProviderInternal(name, libraryName, slotId, -1, null, null));
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.pkcs11.PKCS11Util#getKeyStore(java.security.AuthProvider, java.lang.String)
     */
    @Override
    public KeyStore getKeyStore ( AuthProvider p, String pin ) throws CryptoException {
        try {
            if ( log.isTraceEnabled() ) {
                log.trace("Loading keystore, provider is " + p); //$NON-NLS-1$
            }
            KeyStore ks = KeyStore.getInstance(PKCS11, p);
            if ( pin != null ) {
                ks.load(null, pin.toCharArray());
            }
            else {
                ks.load(null, null);
            }
            return ks;
        }
        catch (
            IOException |
            CertificateException |
            NoSuchAlgorithmException |
            KeyStoreException e ) {
            throw new CryptoException("Failed to open PKCS11 key store", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.pkcs11.PKCS11Util#prepareKeyPair(java.security.AuthProvider, java.security.KeyStore,
     *      java.lang.String, java.lang.String, java.security.spec.AlgorithmParameterSpec)
     */
    @Override
    public KeyPair prepareKeyPair ( AuthProvider p, KeyStore ks, String algo, String alias, AlgorithmParameterSpec keyParams )
            throws CryptoException {
        try {
            if ( ks.containsAlias(alias) ) {
                throw new CryptoException(String.format("Key with alias '%s' does already exist", alias)); //$NON-NLS-1$
            }

            if ( keyAliasExists(p, alias) ) {
                throw new CryptoException(String.format("Unaccessible key with alias '%s' does already exist", alias)); //$NON-NLS-1$
            }

            KeyPairGenerator kpGen = KeyPairGenerator.getInstance(algo, p);
            kpGen.initialize(keyParams);
            KeyPair kp = kpGen.generateKeyPair();
            setKeyAlias(p, alias, kp.getPrivate());
            setKeyAlias(p, alias, kp.getPublic());
            return kp;
        }
        catch (
            InvalidAlgorithmParameterException |
            NoSuchAlgorithmException |
            KeyStoreException e ) {
            throw new CryptoException("Failed to generate key pair", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.pkcs11.PKCS11Util#prepareKeyPair(java.security.AuthProvider, java.security.KeyStore,
     *      java.lang.String, eu.agno3.runtime.crypto.keystore.KeyType)
     */
    @Override
    public KeyPair prepareKeyPair ( AuthProvider p, KeyStore ks, String alias, KeyType type ) throws CryptoException {
        return prepareKeyPair(p, ks, type.getAlgo(), alias, type.getParams());
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.pkcs11.PKCS11Util#keyAliasExists(java.security.AuthProvider, java.lang.String)
     */
    @Override
    public boolean keyAliasExists ( AuthProvider p, String alias ) throws CryptoException {
        sun.security.pkcs11.wrapper.PKCS11 wrapper = getWrapperFor(p);
        Object session = getSessionFor(p);
        try {
            long sessionId = getSessionId(session);
            try {
                CK_ATTRIBUTE[] attrs = new CK_ATTRIBUTE[] {
                    new CK_ATTRIBUTE(PKCS11Constants.CKA_CLASS, PKCS11Constants.CKO_PRIVATE_KEY), new CK_ATTRIBUTE(PKCS11Constants.CKA_ID, alias)
                };
                wrapper.C_FindObjectsInit(sessionId, attrs);

                long[] c_FindObjects = wrapper.C_FindObjects(sessionId, 1);

                if ( c_FindObjects != null && c_FindObjects.length > 0 ) {
                    return true;
                }
                return false;
            }
            finally {
                wrapper.C_FindObjectsFinal(sessionId);
            }
        }
        catch ( PKCS11Exception e ) {
            throw new CryptoException("Failed to check whether key exists", e); //$NON-NLS-1$
        }
        finally {
            releaseSession(p, session);
        }
    }


    private boolean keyExistsWithDifferentAlias ( AuthProvider p, String expectAlias, PublicKey pub ) throws CryptoException {
        PKCS11 p11 = getWrapperFor(p);
        Object session = getSessionFor(p);
        try {
            long sessionId = getSessionId(session);
            CK_ATTRIBUTE[] attrs;
            if ( pub instanceof RSAPublicKey ) {
                attrs = new CK_ATTRIBUTE[] {
                    new CK_ATTRIBUTE(PKCS11Constants.CKA_CLASS, PKCS11Constants.CKO_PUBLIC_KEY),
                    new CK_ATTRIBUTE(PKCS11Constants.CKA_MODULUS, ( (RSAPublicKey) pub ).getModulus()),
                    new CK_ATTRIBUTE(PKCS11Constants.CKA_PUBLIC_EXPONENT, ( (RSAPublicKey) pub ).getPublicExponent()),
                    new CK_ATTRIBUTE(PKCS11Constants.CKA_KEY_TYPE, PKCS11Constants.CKK_RSA)
                };
            }
            else if ( pub instanceof ECPublicKey ) {
                ECPublicKey ecpub = (ECPublicKey) pub;
                attrs = new CK_ATTRIBUTE[] {
                    new CK_ATTRIBUTE(PKCS11Constants.CKA_CLASS, PKCS11Constants.CKO_PUBLIC_KEY),
                    new CK_ATTRIBUTE(PKCS11Constants.CKA_KEY_TYPE, PKCS11Constants.CKK_EC),
                    new CK_ATTRIBUTE(PKCS11Constants.CKA_EC_PARAMS, makeECParams(ecpub)),
                    new CK_ATTRIBUTE(PKCS11Constants.CKA_EC_POINT, makeECPoint(ecpub))
                };
            }
            else {
                throw new CryptoException("Unsupported key type " + pub.getClass().getName()); //$NON-NLS-1$
            }

            try {
                p11.C_FindObjectsInit(sessionId, attrs);
                long[] c_FindObjects = p11.C_FindObjects(sessionId, 256);
                if ( c_FindObjects == null || c_FindObjects.length == 0 ) {
                    return false;
                }

                for ( long objId : c_FindObjects ) {
                    attrs = new CK_ATTRIBUTE[] {
                        new CK_ATTRIBUTE(PKCS11Constants.CKA_ID), new CK_ATTRIBUTE(PKCS11Constants.CKA_LABEL)
                    };
                    p11.C_GetAttributeValue(sessionId, objId, attrs);

                    String idValue = toString(attrs[ 0 ].pValue);
                    String labelValue = toString(attrs[ 1 ].pValue);

                    if ( log.isDebugEnabled() ) {
                        log.debug(String.format("Found public key with id %s label %s", idValue, labelValue)); //$NON-NLS-1$
                    }

                    String alias;
                    if ( !Objects.equals(idValue, labelValue) ) {
                        if ( labelValue != null ) {
                            alias = labelValue;
                        }
                        alias = idValue;
                    }
                    else {
                        alias = idValue;
                    }

                    if ( !Objects.equals(alias, expectAlias) && keyAliasExists(p, alias) ) {
                        return true;
                    }
                }
            }
            finally {
                p11.C_FindObjectsFinal(sessionId);
            }

            return false;
        }
        catch ( PKCS11Exception e ) {
            throw new CryptoException("Failed to set key attributes", e); //$NON-NLS-1$
        }
        finally {
            releaseSession(p, session);
        }
    }


    /**
     * @param pValue
     * @return
     */
    private static String toString ( Object pValue ) {
        if ( pValue instanceof char[] ) {
            return new String((char[]) pValue);
        }
        else if ( pValue instanceof byte[] ) {
            return new String((byte[]) pValue, StandardCharsets.US_ASCII);
        }
        return null;
    }


    /**
     * @param p
     * @param alias
     * @param kp
     * @throws CryptoException
     */
    protected void setKeyAlias ( AuthProvider p, String alias, Key k ) throws CryptoException {
        long keyId = getKeyId(k);

        PKCS11 p11 = getWrapperFor(p);
        Object session = getSessionFor(p);
        try {
            long sessionId = getSessionId(session);

            CK_ATTRIBUTE[] attrs = new CK_ATTRIBUTE[] {
                new CK_ATTRIBUTE(PKCS11Constants.CKA_ID, alias), new CK_ATTRIBUTE(PKCS11Constants.CKA_LABEL, alias)
            };

            p11.C_SetAttributeValue(sessionId, keyId, attrs);
        }
        catch ( PKCS11Exception e ) {
            throw new CryptoException("Failed to set key attributes", e); //$NON-NLS-1$
        }
        finally {
            releaseSession(p, session);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.pkcs11.PKCS11Util#storeKeyPair(java.security.AuthProvider, java.security.KeyStore,
     *      java.lang.String, java.security.KeyPair, java.security.cert.Certificate[])
     */
    @Override
    public void storeKeyPair ( AuthProvider p, KeyStore ks, String alias, KeyPair kp, Certificate[] chain ) throws CryptoException {
        try {
            if ( ks.containsAlias(alias) ) {
                throw new CryptoException(String.format("Alias '%s' does already exist", alias)); //$NON-NLS-1$
            }

            if ( keyExistsWithDifferentAlias(p, alias, kp.getPublic()) ) {
                // stupid Java PKCS11 keystore is going to relabel the the old key if this happens
                throw new CryptoException(String.format("Same key '%s' already exists under a different alias, unsupported", alias)); //$NON-NLS-1$
            }

            Certificate[] storeChain = chain;

            if ( chain == null || chain.length == 0 ) {
                throw new CryptoException("Cannot store key pair without a certificate (e.g. create a fake self-signed certificate first)"); //$NON-NLS-1$
            }

            PrivateKeyEntry pke = new PrivateKeyEntry(kp.getPrivate(), storeChain);
            ks.setEntry(alias, pke, null);
            ks.store(null);
        }
        catch (
            KeyStoreException |
            NoSuchAlgorithmException |
            CertificateException |
            IOException e ) {
            throw new CryptoException("Failed to store key pair", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.pkcs11.PKCS11Util#importKeyPair(java.security.AuthProvider, java.security.KeyStore,
     *      java.lang.String, java.security.KeyPair, java.security.cert.Certificate[])
     */
    @Override
    public void importKeyPair ( AuthProvider p, KeyStore ks, String alias, KeyPair kp, Certificate[] chain ) throws CryptoException {

        if ( alias == null ) {
            throw new CryptoException("Alias cannot be NULL"); //$NON-NLS-1$
        }

        if ( kp.getPublic() == null || kp.getPrivate() == null ) {
            throw new CryptoException("Private/public key cannot be NULL"); //$NON-NLS-1$
        }

        this.storeKeyPair(p, ks, alias, kp, chain);
        try {
            Key key = ks.getKey(alias, null);
            if ( key == null ) {
                throw new CryptoException("Newly imported key not found"); //$NON-NLS-1$
            }
            this.setKeyAlias(p, alias, key);
        }
        catch (
            UnrecoverableKeyException |
            KeyStoreException |
            NoSuchAlgorithmException e ) {
            throw new CryptoException("Failed to retrieve newly stored key", e); //$NON-NLS-1$
        }
        storePublicKey(p, alias, kp.getPublic());
    }


    /**
     * @param p
     * @param alias
     * @param public1
     * @throws CryptoException
     */
    private static void storePublicKey ( AuthProvider p, String alias, PublicKey pub ) throws CryptoException {
        PKCS11 p11 = getWrapperFor(p);
        Object session = getSessionFor(p);
        try {
            long sessionId = getSessionId(session);
            CK_ATTRIBUTE[] attrs;
            if ( pub instanceof RSAPublicKey ) {
                attrs = new CK_ATTRIBUTE[] {
                    new CK_ATTRIBUTE(PKCS11Constants.CKA_CLASS, PKCS11Constants.CKO_PUBLIC_KEY), new CK_ATTRIBUTE(PKCS11Constants.CKA_ID, alias),
                    new CK_ATTRIBUTE(PKCS11Constants.CKA_LABEL, alias),
                    new CK_ATTRIBUTE(PKCS11Constants.CKA_MODULUS, ( (RSAPublicKey) pub ).getModulus()),
                    new CK_ATTRIBUTE(PKCS11Constants.CKA_PUBLIC_EXPONENT, ( (RSAPublicKey) pub ).getPublicExponent()),
                    new CK_ATTRIBUTE(PKCS11Constants.CKA_KEY_TYPE, PKCS11Constants.CKK_RSA), new CK_ATTRIBUTE(PKCS11Constants.CKA_TOKEN, true)
                };
            }
            else if ( pub instanceof ECPublicKey ) {
                ECPublicKey ecpub = (ECPublicKey) pub;
                attrs = new CK_ATTRIBUTE[] {
                    new CK_ATTRIBUTE(PKCS11Constants.CKA_CLASS, PKCS11Constants.CKO_PUBLIC_KEY), new CK_ATTRIBUTE(PKCS11Constants.CKA_ID, alias),
                    new CK_ATTRIBUTE(PKCS11Constants.CKA_LABEL, alias), new CK_ATTRIBUTE(PKCS11Constants.CKA_KEY_TYPE, PKCS11Constants.CKK_EC),
                    new CK_ATTRIBUTE(PKCS11Constants.CKA_EC_PARAMS, makeECParams(ecpub)),
                    new CK_ATTRIBUTE(PKCS11Constants.CKA_EC_POINT, makeECPoint(ecpub)), new CK_ATTRIBUTE(PKCS11Constants.CKA_TOKEN, true)
                };
            }
            else {
                throw new CryptoException("Unsupported key type " + pub.getAlgorithm()); //$NON-NLS-1$
            }

            try {
                p11.C_FindObjectsInit(sessionId, attrs);

                long[] c_FindObjects = p11.C_FindObjects(sessionId, 1);

                if ( c_FindObjects != null && c_FindObjects.length > 0 ) {
                    log.debug("Public key does already exist " + alias); //$NON-NLS-1$
                    return;
                }
            }
            finally {
                p11.C_FindObjectsFinal(sessionId);
            }
            p11.C_CreateObject(sessionId, attrs);
        }
        catch ( PKCS11Exception e ) {
            throw new CryptoException("Failed to set key attributes", e); //$NON-NLS-1$
        }
        finally {
            releaseSession(p, session);
        }
    }


    /**
     * @param ecpub
     * @return
     * @throws CryptoException
     */
    private static byte[] makeECParams ( ECPublicKey ecpub ) throws CryptoException {
        JCEECPublicKey pk = new JCEECPublicKey(ecpub);
        org.bouncycastle.jce.spec.ECParameterSpec params = pk.getParameters();
        X962Parameters x9ecParameters = new X962Parameters(
            new X9ECParameters(params.getCurve(), params.getG(), params.getN(), params.getH(), params.getSeed()));
        try {
            return x9ecParameters.getEncoded();
        }
        catch ( IOException e ) {
            throw new CryptoException("Failed to produce encoded EC key parameters", e); //$NON-NLS-1$
        }
    }


    /**
     * @param ecpub
     * @return
     * @throws CryptoException
     */
    private static byte[] makeECPoint ( ECPublicKey ecpub ) throws CryptoException {
        JCEECPublicKey pk = new JCEECPublicKey(ecpub);

        X9ECPoint x9ecPoint = new X9ECPoint(pk.getQ());
        try {
            return x9ecPoint.getEncoded();
        }
        catch ( IOException e ) {
            throw new CryptoException("Failed to produce encoded EC curve point", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws CryptoException
     *
     * @see eu.agno3.runtime.crypto.pkcs11.PKCS11Util#cleanupAlias(java.security.AuthProvider, java.security.KeyStore,
     *      java.lang.String)
     */
    @Override
    public void cleanupAlias ( AuthProvider provider, KeyStore ks, String alias ) throws CryptoException {
        // also remove associated public key entry
        PKCS11 p11 = getWrapperFor(provider);
        Object session = getSessionFor(provider);
        try {
            long sessionId = getSessionId(session);

            CK_ATTRIBUTE[] attrs = new CK_ATTRIBUTE[] {
                new CK_ATTRIBUTE(PKCS11Constants.CKA_CLASS, PKCS11Constants.CKO_PUBLIC_KEY), new CK_ATTRIBUTE(PKCS11Constants.CKA_ID, alias),
                new CK_ATTRIBUTE(PKCS11Constants.CKA_LABEL, alias)
            };

            p11.C_FindObjectsInit(sessionId, attrs);

            try {
                for ( long obj : p11.C_FindObjects(sessionId, 10) ) {
                    p11.C_DestroyObject(sessionId, obj);
                }
            }
            finally {
                p11.C_FindObjectsFinal(sessionId);
            }
        }
        catch ( PKCS11Exception e ) {
            throw new CryptoException("Failed to set key attributes", e); //$NON-NLS-1$
        }
        finally {
            releaseSession(provider, session);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.pkcs11.PKCS11Util#updateCertificate(java.security.AuthProvider,
     *      java.security.KeyStore, java.lang.String, java.security.cert.Certificate[])
     */
    @Override
    public void updateCertificate ( AuthProvider p, KeyStore ks, String alias, Certificate[] chain ) throws CryptoException {
        try {
            if ( !ks.containsAlias(alias) ) {
                throw new CryptoException(String.format("Alias '%s' does not exist", alias)); //$NON-NLS-1$
            }

            PrivateKeyEntry entry = (PrivateKeyEntry) ks.getEntry(alias, null);
            X509Certificate oldEeCert = (X509Certificate) entry.getCertificate();
            X509Certificate newEeCert = (X509Certificate) chain[ 0 ];

            if ( !pubkeysEqual(oldEeCert.getPublicKey(), newEeCert.getPublicKey()) ) {
                throw new CryptoException("Certificate public key does not match key"); //$NON-NLS-1$
            }
            ks.setEntry(alias, new PrivateKeyEntry(entry.getPrivateKey(), chain), null);
        }
        catch (
            KeyStoreException |
            NoSuchAlgorithmException |
            UnrecoverableEntryException e ) {
            throw new CryptoException("Could not update certificates", e); //$NON-NLS-1$
        }

    }


    /**
     * @param publicKeyInternal
     * @param publicKey
     * @return
     */
    private static boolean pubkeysEqual ( PublicKey a, PublicKey b ) {
        if ( a instanceof RSAPublicKey && b instanceof RSAPublicKey ) {
            RSAPublicKey rsaa = (RSAPublicKey) a;
            RSAPublicKey rsab = (RSAPublicKey) b;
            return rsaa.getModulus().compareTo(rsab.getModulus()) == 0 && rsaa.getPublicExponent().compareTo(rsab.getPublicExponent()) == 0;
        }
        else if ( a instanceof ECPublicKey && b instanceof ECPublicKey ) {
            ECPublicKey eca = (ECPublicKey) a;
            ECPublicKey ecb = (ECPublicKey) b;

            return Objects.equals(eca.getAlgorithm(), ecb.getAlgorithm()) && eca.getW().equals(ecb.getW());
        }

        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.pkcs11.PKCS11Util#removeKey(java.security.AuthProvider, java.security.KeyStore,
     *      java.lang.String)
     */
    @Override
    public void removeKey ( AuthProvider p, KeyStore ks, String alias ) throws CryptoException {
        try {
            ks.deleteEntry(alias);
        }
        catch ( KeyStoreException e ) {
            throw new CryptoException("Failed to remove key", e); //$NON-NLS-1$
        }

        this.cleanupAlias(p, ks, alias);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws CryptoException
     *
     * @see eu.agno3.runtime.crypto.pkcs11.PKCS11Util#getProviderFor(java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public AuthProvider getProviderFor ( String libraryName, String name, String pin ) throws CryptoException {
        return getProviderFor(libraryName, name, pin, 0);
    }


    /**
     * @param initArgs
     * @return
     */
    static InputStream getConfig ( String name, String libraryPath, String slotId, int slotIndex, String extraConfig, String initArgs ) {
        StringBuilder sb = new StringBuilder();
        sb.append("name=").append(name).append(System.lineSeparator()); //$NON-NLS-1$
        sb.append("library=").append(libraryPath).append(System.lineSeparator()); //$NON-NLS-1$

        if ( initArgs != null ) {
            sb.append("nssArgs=").append('"').append(initArgs).append('"').append(System.lineSeparator()); //$NON-NLS-1$
        }

        if ( slotId != null ) {
            sb.append("slot=").append(slotId).append(System.lineSeparator()); //$NON-NLS-1$
        }
        else {
            sb.append("slotListIndex=").append(slotIndex).append(System.lineSeparator()); //$NON-NLS-1$
        }

        sb.append("attributes(generate, *, *) = { CKA_TOKEN = true }").append(System.lineSeparator()); //$NON-NLS-1$
        sb.append("attributes(generate, CKO_CERTIFICATE, *) = { CKA_PRIVATE = false }").append(System.lineSeparator()); //$NON-NLS-1$
        sb.append("attributes(generate, CKO_PUBLIC_KEY, *) = { CKA_PRIVATE = false }").append(System.lineSeparator()); //$NON-NLS-1$
        if ( extraConfig != null ) {
            sb.append(extraConfig);
        }
        if ( log.isTraceEnabled() ) {
            log.trace(sb.toString());
        }
        return new ByteArrayInputStream(sb.toString().getBytes(Charset.forName("UTF-8"))); //$NON-NLS-1$
    }

    private static final class PINCallBackHandler implements CallbackHandler {

        private String pin;


        protected PINCallBackHandler ( String pin ) {
            this.pin = pin;
        }


        @Override
        public void handle ( Callback[] callbacks ) throws IOException, UnsupportedCallbackException {
            if ( this.pin == null ) {
                return;
            }
            for ( Callback cb : callbacks ) {
                if ( cb instanceof PasswordCallback ) {
                    ( (PasswordCallback) cb ).setPassword(this.pin.toCharArray());
                }
            }
        }
    }

}
