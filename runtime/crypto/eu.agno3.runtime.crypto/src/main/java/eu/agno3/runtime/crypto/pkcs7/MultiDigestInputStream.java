/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.12.2015 by mbechler
 */
package eu.agno3.runtime.crypto.pkcs7;


import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;


/**
 * @author mbechler
 *
 */
public class MultiDigestInputStream extends FilterInputStream {

    private static final Set<String> DEFAULT_ALGORITHMS = new HashSet<>(Arrays.asList("SHA-256", "SHA-384", "SHA-512")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    private Map<String, MessageDigest> dgsts = new HashMap<>();
    private DefaultDigestAlgorithmIdentifierFinder algIdFinder = new DefaultDigestAlgorithmIdentifierFinder();


    /**
     * @param in
     * @throws NoSuchAlgorithmException
     * 
     */
    public MultiDigestInputStream ( InputStream in ) throws NoSuchAlgorithmException {
        this(in, DEFAULT_ALGORITHMS);
    }


    /**
     * @param in
     * @param digestAlgs
     * @throws NoSuchAlgorithmException
     */
    public MultiDigestInputStream ( InputStream in, Set<String> digestAlgs ) throws NoSuchAlgorithmException {
        super(in);

        for ( String dgstAlg : digestAlgs ) {
            MessageDigest instance = MessageDigest.getInstance(dgstAlg);
            this.dgsts.put(dgstAlg, instance);
        }
    }


    @Override
    public int read () throws IOException {
        int ch = this.in.read();
        if ( ch != -1 ) {
            for ( MessageDigest messageDigest : this.dgsts.values() ) {
                messageDigest.update((byte) ch);
            }
        }
        return ch;
    }


    @Override
    public int read ( byte[] b, int off, int len ) throws IOException {
        int result = this.in.read(b, off, len);
        if ( result != -1 ) {
            for ( MessageDigest messageDigest : this.dgsts.values() ) {
                messageDigest.update(b, off, result);
            }
        }
        return result;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.io.FilterInputStream#markSupported()
     */
    @Override
    public boolean markSupported () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.io.FilterInputStream#skip(long)
     */
    @Override
    public long skip ( long n ) throws IOException {
        throw new UnsupportedOperationException();
    }


    /**
     * 
     * @return the finalized digest values
     */
    public Map<String, byte[]> getDigests () {
        Map<String, byte[]> digests = new HashMap<>();
        for ( Entry<String, MessageDigest> entry : this.dgsts.entrySet() ) {
            digests.put(entry.getKey(), entry.getValue().digest());
        }
        return digests;
    }


    /**
     * 
     * @return the finalized digest values in BC format
     */
    public Map<ASN1ObjectIdentifier, byte[]> getBCDigests () {
        Map<ASN1ObjectIdentifier, byte[]> digests = new HashMap<>();
        for ( Entry<String, MessageDigest> entry : this.dgsts.entrySet() ) {
            digests.put(this.algIdFinder.find(entry.getKey()).getAlgorithm(), entry.getValue().digest());
        }
        return digests;
    }
}
