/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2017 by mbechler
 */
package eu.agno3.runtime.net.icap.internal;


import java.io.IOException;
import java.net.URISyntaxException;

import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.TLSContext;
import eu.agno3.runtime.net.icap.ICAPConfiguration;
import eu.agno3.runtime.net.icap.ICAPConnection;
import eu.agno3.runtime.net.icap.ICAPConnectionFactory;
import eu.agno3.runtime.net.icap.ICAPException;


/**
 * @author mbechler
 *
 */
@Component ( service = ICAPConnectionFactory.class )
public class ICAPConnectionFactoryImpl implements ICAPConnectionFactory {

    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.icap.ICAPConnectionFactory#createICAPConnection(eu.agno3.runtime.net.icap.ICAPConfiguration,
     *      eu.agno3.runtime.crypto.tls.TLSContext)
     */
    @Override
    public ICAPConnection createICAPConnection ( ICAPConfiguration cfg, TLSContext tc ) throws ICAPException {
        try {
            ICAPConnection ic = new ICAPConnectionImpl(cfg, tc);
            ic.ensureConnected();
            return ic;
        }
        catch (
            CryptoException |
            URISyntaxException |
            IOException e ) {
            throw new ICAPException("Failed to create connection", e); //$NON-NLS-1$
        }
    }
}
