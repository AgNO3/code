/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 18, 2016 by mbechler
 */
package eu.agno3.runtime.crypto.tls.internal;


import javax.net.ssl.SNIMatcher;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.StandardConstants;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;


/**
 * @author mbechler
 *
 */
@Component ( service = DefaultSNIMatcher.class )
public class DefaultSNIMatcher extends SNIMatcher {

    private static final Logger log = Logger.getLogger(DefaultSNIMatcher.class);


    /**
     */
    public DefaultSNIMatcher () {
        super(StandardConstants.SNI_HOST_NAME);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.net.ssl.SNIMatcher#matches(javax.net.ssl.SNIServerName)
     */
    @Override
    public boolean matches ( SNIServerName serverName ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Requested SNI server name " + serverName); //$NON-NLS-1$
        }
        return true;
    }

}
