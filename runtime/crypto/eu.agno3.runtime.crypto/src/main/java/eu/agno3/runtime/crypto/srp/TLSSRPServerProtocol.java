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
import org.bouncycastle.crypto.tls.SRPTlsServer;
import org.bouncycastle.crypto.tls.TlsCipherFactory;
import org.bouncycastle.crypto.tls.TlsSRPIdentityManager;


/**
 * @author mbechler
 *
 */
public class TLSSRPServerProtocol extends SRPTlsServer {

    private static final Logger log = Logger.getLogger(TLSSRPServerProtocol.class);


    /**
     * @param cipherFactory
     * @param identityManager
     */
    public TLSSRPServerProtocol ( TlsCipherFactory cipherFactory, TlsSRPIdentityManager identityManager ) {
        super(cipherFactory, identityManager);
    }


    /**
     * @param identityManager
     */
    public TLSSRPServerProtocol ( TlsSRPIdentityManager identityManager ) {
        super(identityManager);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.bouncycastle.crypto.tls.SRPTlsServer#getCipherSuites()
     */
    @Override
    protected int[] getCipherSuites () {
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
     * @see org.bouncycastle.crypto.tls.AbstractTlsServer#getMinimumVersion()
     */
    @Override
    protected ProtocolVersion getMinimumVersion () {
        return ProtocolVersion.TLSv10;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.bouncycastle.crypto.tls.AbstractTlsServer#getMaximumVersion()
     */
    @Override
    protected ProtocolVersion getMaximumVersion () {
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
