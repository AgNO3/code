/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2017 by mbechler
 */
package eu.agno3.runtime.crypto.srp;


import org.apache.log4j.Logger;
import org.bouncycastle.crypto.tls.CipherSuite;
import org.bouncycastle.crypto.tls.CompressionMethod;
import org.bouncycastle.crypto.tls.ProtocolVersion;
import org.bouncycastle.crypto.tls.SRPTlsClient;
import org.bouncycastle.crypto.tls.TlsCipherFactory;
import org.bouncycastle.crypto.tls.TlsSRPGroupVerifier;


/**
 * @author mbechler
 *
 */
public class TLSSRPClientProtocol extends SRPTlsClient {

    private static final Logger log = Logger.getLogger(TLSSRPClientProtocol.class);


    /**
     * @param identity
     * @param secret
     */
    public TLSSRPClientProtocol ( byte[] identity, byte[] secret ) {
        super(identity, secret);
    }


    /**
     * @param cipherFactory
     * @param identity
     * @param secret
     */
    public TLSSRPClientProtocol ( TlsCipherFactory cipherFactory, byte[] identity, byte[] secret ) {
        super(cipherFactory, identity, secret);
    }


    /**
     * @param cipherFactory
     * @param groupVerifier
     * @param identity
     * @param secret
     */
    public TLSSRPClientProtocol ( TlsCipherFactory cipherFactory, TlsSRPGroupVerifier groupVerifier, byte[] identity, byte[] secret ) {
        super(cipherFactory, groupVerifier, identity, secret);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see org.bouncycastle.crypto.tls.SRPTlsClient#getCipherSuites()
     */
    @Override
    public int[] getCipherSuites () {
        return new int[] {
            CipherSuite.TLS_SRP_SHA_WITH_AES_128_CBC_SHA, CipherSuite.TLS_SRP_SHA_WITH_AES_256_CBC_SHA
        };
    }


    @Override
    public short[] getCompressionMethods () {
        return new short[] {
            CompressionMethod._null
        };
    }


    /**
     * {@inheritDoc}
     *
     * @see org.bouncycastle.crypto.tls.AbstractTlsClient#getClientVersion()
     */
    @Override
    public ProtocolVersion getClientVersion () {
        return ProtocolVersion.TLSv12;
    }


    @Override
    public void notifyAlertRaised ( short alertLevel, short alertDescription, String message, Throwable cause ) {
        log.debug(String.format("Raising alert %d %d: %s", alertLevel, alertDescription, message), cause); //$NON-NLS-1$ s
    }


    @Override
    public void notifyAlertReceived ( short alertLevel, short alertDescription ) {
        log.debug(String.format("Received alert %d %d", alertLevel, alertDescription)); //$NON-NLS-1$
    }
}
