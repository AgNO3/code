/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.09.2013 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.client.session.impl;


import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.model.base.client.session.ClientConfigSessionFactory;
import eu.agno3.orchestrator.config.model.base.session.BaseConfigSession;
import eu.agno3.orchestrator.config.model.base.session.ConfigSession;


/**
 * @author mbechler
 * 
 */
@Component ( service = ClientConfigSessionFactory.class )
public class ClientSessionFactoryImpl implements ClientConfigSessionFactory {

    private static final Logger log = Logger.getLogger(ClientSessionFactoryImpl.class);


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.base.session.ConfigSessionFactory#createSession()
     */
    @Override
    public ConfigSession createSession () {

        log.debug("Create anonymous client session"); //$NON-NLS-1$
        return new BaseConfigSession();
    }

}
