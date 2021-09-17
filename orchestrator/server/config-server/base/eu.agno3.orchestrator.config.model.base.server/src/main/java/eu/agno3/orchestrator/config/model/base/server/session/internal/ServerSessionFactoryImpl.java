/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.09.2013 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.server.session.internal;


import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.model.base.server.session.ServerConfigSessionFactory;
import eu.agno3.orchestrator.config.model.base.session.BaseConfigSession;
import eu.agno3.orchestrator.config.model.base.session.ConfigSession;


/**
 * @author mbechler
 * 
 */
@Component ( service = ServerConfigSessionFactory.class )
public class ServerSessionFactoryImpl implements ServerConfigSessionFactory {

    private static final Logger log = Logger.getLogger(ServerSessionFactoryImpl.class);


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.base.session.ConfigSessionFactory#createSession()
     */
    @Override
    public ConfigSession createSession () {

        log.debug("Create server session"); //$NON-NLS-1$
        return new BaseConfigSession();
    }

}
