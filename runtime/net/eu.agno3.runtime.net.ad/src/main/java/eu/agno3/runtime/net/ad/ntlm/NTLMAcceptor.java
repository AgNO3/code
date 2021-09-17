/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.03.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.ntlm;


import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.agno3.runtime.net.ad.ADException;
import eu.agno3.runtime.net.ad.ADRealm;
import eu.agno3.runtime.net.ad.ADUserInfo;
import eu.agno3.runtime.net.ad.NetlogonConnection;
import eu.agno3.runtime.net.krb5.KerberosException;

import jcifs.ntlmssp.NtlmFlags;
import jcifs.ntlmssp.Type1Message;
import jcifs.ntlmssp.Type2Message;
import jcifs.ntlmssp.Type3Message;
import jcifs.util.Encdec;
import jcifs.util.Strings;


/**
 * @author mbechler
 *
 */
public class NTLMAcceptor {

    private static final Logger log = Logger.getLogger(NTLMAcceptor.class);

    private Map<Long, NonceEntry> nonces;
    private Map<Long, NTLMContextImpl> contextCache;

    private final SecureRandom random;
    private ADRealm realm;

    private static final byte[] NTLMSSP = new byte[] {
        0x4e, 0x54, 0x4c, 0x4d, 0x53, 0x53, 0x50, 0x00
    };

    private static final int _NTLMSSP_NEGOTIATE_EXT_SECURITY = 0x00080000;

    private static final long NONCE_TIMEOUT = 10000;
    private static final int CONTEXT_TIMEOUT = 120000;


    /**
     * @param random
     * @param realm
     * @throws NTLMException
     * @throws ADException
     * 
     */
    public NTLMAcceptor ( SecureRandom random, ADRealm realm ) throws NTLMException, ADException {
        this.realm = realm;
        this.nonces = Collections.synchronizedMap(new HashMap<>());
        this.contextCache = Collections.synchronizedMap(new HashMap<>());
        this.random = random;
    }


    /**
     * 
     * @param byteToken
     * @return whether the token begins with NTLMSSP
     */
    public static boolean isNTLMToken ( byte[] byteToken ) {
        if ( byteToken.length < NTLMSSP.length ) {
            return false;
        }
        for ( int i = 0; i < NTLMSSP.length; i++ ) {
            if ( byteToken[ i ] != NTLMSSP[ i ] ) {
                return false;
            }
        }
        return true;
    }


    /**
     * @param context
     * @param token
     * @return output token
     * @throws NTLMException
     */
    public byte[] accept ( NTLMContext context, byte[] token ) throws NTLMException {
        NTLMContextImpl ctx = (NTLMContextImpl) context;

        if ( !isNTLMToken(token) ) {
            throw new NTLMException("Not a NTLM token"); //$NON-NLS-1$
        }

        if ( token[ 8 ] == 1 ) {
            // type 1 message
            return handleType1(ctx, token);
        }

        return handleType3(ctx, token);

    }


    /**
     * @param connId
     * @param conn
     * @param cacheable
     * @return a new NTLM context
     */
    public NTLMContextImpl create ( long connId, Object conn, boolean cacheable ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Connection id is " + connId); //$NON-NLS-1$
        }

        if ( cacheable ) {
            NTLMContextImpl cached = this.contextCache.get(connId);
            if ( cached != null ) {
                Object cachedConn = cached.getConnection();
                if ( cachedConn != null && cachedConn.equals(conn) ) {
                    log.debug("Returning cached connection"); //$NON-NLS-1$
                    cached.used();
                    return cached;
                }

                log.warn("Connections did not match"); //$NON-NLS-1$
            }
        }

        return new NTLMContextImpl(connId, CONTEXT_TIMEOUT, conn, cacheable);
    }


    /**
     * @param token
     * @param connId
     * @return
     * @throws IOException
     * @throws KerberosException
     */
    private byte[] handleType3 ( NTLMContextImpl context, byte[] token ) throws NTLMException {
        byte[] nonce = restoreNonce(context.getConnectionId());
        try ( NetlogonConnection netlogonConnection = this.realm.getNetlogonConnection() ) {
            Type3Message auth = new Type3Message(token);
            ADUserInfo info = netlogonConnection.getNetlogonOperations().ntlmValidate(nonce, auth);
            context.setUserInfo(info);
            this.removeNonce(context.getConnectionId());

            if ( context.isCacheable() ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Caching context for connid " + context.getConnectionId()); //$NON-NLS-1$
                }
                this.contextCache.put(context.getConnectionId(), context);
            }
        }
        catch (
            ADException |
            IOException e ) {
            throw new NTLMException("Failed to authenticate user", e); //$NON-NLS-1$
        }

        return null;
    }


    /**
     * @param token
     * @param connId
     * @return
     * @throws KerberosException
     */
    private byte[] handleType1 ( NTLMContextImpl ctx, byte[] token ) throws NTLMException {
        byte[] nonce = new byte[8];
        this.random.nextBytes(nonce);

        try {
            Type1Message type1Message = new Type1Message(token);
            Type2Message type2Message = new Type2Message(this.realm.getCIFSContext(), type1Message.getFlags(), nonce, this.realm.getDomainName());

            if ( type2Message.getFlag(_NTLMSSP_NEGOTIATE_EXT_SECURITY) ) {

                type2Message.setFlag(NtlmFlags.NTLMSSP_NEGOTIATE_LM_KEY, false);
                type2Message.setFlag(NtlmFlags.NTLMSSP_NEGOTIATE_TARGET_INFO, true);
                type2Message.setTargetInformation(getTargetInformation());
            }
            this.storeNonce(ctx.getConnectionId(), nonce);
            return type2Message.toByteArray();
        }
        catch ( IOException e ) {
            throw new NTLMException("Failed to create NTLM type 2 message", e); //$NON-NLS-1$
        }
    }


    protected byte[] getTargetInformation () {
        return createTargetInformation(this.realm);
    }


    /**
     * @param rlm
     * @return target information blob
     */
    public static final byte[] createTargetInformation ( ADRealm rlm ) {
        String hostName = rlm.getLocalNetbiosHostname();
        String nsDomainName = rlm.getNetbiosDomainName();
        String dnsDomainName = rlm.getDomainName();
        String fqdn = rlm.getLocalHostname();

        int bufferSize = 5 * 4 + 2 * ( hostName.length() + dnsDomainName.length() + nsDomainName.length() + fqdn.length() + 1 );
        int bufferIdx = 0;
        byte[] buffer = new byte[bufferSize];
        bufferIdx += writeString(1, hostName, bufferIdx, buffer);
        bufferIdx += writeString(2, nsDomainName, bufferIdx, buffer);
        bufferIdx += writeString(3, fqdn, bufferIdx, buffer);
        bufferIdx += writeString(4, dnsDomainName, bufferIdx, buffer);
        bufferIdx += writeString(0, " ", bufferIdx, buffer); //$NON-NLS-1$
        return buffer;
    }


    /**
     * @param str
     * @param bufferIdx
     * @param buffer
     * @return
     */
    private static int writeString ( int id, String str, int start, byte[] buffer ) {
        int bufferIdx = start;
        Encdec.enc_uint16le((short) id, buffer, bufferIdx);
        bufferIdx += 2;
        Encdec.enc_uint16le((short) ( 2 * str.length() ), buffer, bufferIdx);
        bufferIdx += 2;
        System.arraycopy(Strings.getUNIBytes(str), 0, buffer, bufferIdx, 2 * str.length());
        bufferIdx += 2 * str.length();
        return bufferIdx - start;
    }


    /**
     * @param connId
     * @param nonce
     */
    private void storeNonce ( long connId, byte[] nonce ) {
        this.nonces.put(connId, new NonceEntry(System.currentTimeMillis() + NONCE_TIMEOUT, nonce));
    }


    /**
     * @param connId
     * @return
     */
    private byte[] restoreNonce ( long connId ) throws NTLMException {
        NonceEntry nonce = this.nonces.get(connId);

        if ( nonce == null ) {
            throw new NTLMException("Nonce not found"); //$NON-NLS-1$
        }

        return nonce.getNonce();
    }


    /**
     * @param connectionId
     * @throws NTLMException
     */
    private void removeNonce ( long connectionId ) throws NTLMException {
        if ( this.nonces.remove(connectionId) == null ) {
            throw new NTLMException("Illegal nonce"); //$NON-NLS-1$
        }
    }


    /**
     * 
     */
    public void cleanUp () {
        long now = System.currentTimeMillis();
        Set<Long> toRemove = new HashSet<>();
        for ( Entry<Long, NonceEntry> e : this.nonces.entrySet() ) {
            if ( e.getValue().getExpires() < now ) {
                toRemove.add(e.getKey());
            }
        }

        for ( Long connId : toRemove ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Removing nonce for " + connId); //$NON-NLS-1$
            }
            this.nonces.remove(connId);
        }

        toRemove.clear();
        for ( Entry<Long, NTLMContextImpl> e : this.contextCache.entrySet() ) {
            if ( e.getValue().getExpires() < now ) {
                toRemove.add(e.getKey());
            }
        }

        for ( Long connId : toRemove ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Removing context for " + connId); //$NON-NLS-1$
            }
            this.contextCache.remove(connId);
        }

    }

    private static class NonceEntry {

        private long expires;
        private byte[] nonce;


        /**
         * @param expires
         * @param nonce
         * 
         */
        public NonceEntry ( long expires, byte[] nonce ) {
            this.expires = expires;
            this.nonce = Arrays.copyOf(nonce, nonce.length);
        }


        /**
         * @return the expires
         */
        public long getExpires () {
            return this.expires;
        }


        /**
         * @return the nonce
         */
        public byte[] getNonce () {
            return Arrays.copyOf(this.nonce, this.nonce.length);
        }
    }

}
