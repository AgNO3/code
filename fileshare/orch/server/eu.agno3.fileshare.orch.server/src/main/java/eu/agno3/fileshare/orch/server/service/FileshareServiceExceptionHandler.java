/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.09.2015 by mbechler
 */
package eu.agno3.fileshare.orch.server.service;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.management.MBeanException;
import javax.management.RuntimeMBeanException;

import org.apache.log4j.Logger;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.orchestrator.config.model.base.exceptions.RemoteCallErrorException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.server.service.AgentServerService;
import eu.agno3.runtime.messaging.CallErrorException;


/**
 * @author mbechler
 *
 */
public class FileshareServiceExceptionHandler {

    private static final Logger log = Logger.getLogger(FileshareServiceExceptionHandler.class);


    /**
     * @param e
     * @param instance
     * @param agentService
     * @return unwrapped exception
     * @throws FileshareException
     * @throws RemoteCallErrorException
     */
    public static Throwable handleJMXException ( Exception e, AgentServerService agentService, InstanceStructuralObject instance )
            throws FileshareException, RemoteCallErrorException {
        Throwable e2 = e;
        log.debug("Original exception", e); //$NON-NLS-1$
        if ( e2.getCause() instanceof InvocationTargetException ) {
            e2 = e.getCause();
        }

        if ( e2.getCause() instanceof IOException && e2.getCause().getCause() instanceof CallErrorException ) {
            e2 = e2.getCause().getCause();
        }

        if ( e2 instanceof CallErrorException ) {
            e2 = e2.getCause();
        }

        if ( e2.getCause() instanceof RuntimeMBeanException ) {
            e2 = e2.getCause();
        }

        if ( e2 instanceof RuntimeMBeanException && e2.getCause() != null ) {
            throw new RemoteCallErrorException(e2.getCause());
        }

        if ( e2 instanceof MBeanException && e2.getCause() != null ) {
            e2 = e2.getCause();
        }

        if ( e2 instanceof FileshareException ) {
            throw (FileshareException) e2;
        }

        return e2;
    }
}
