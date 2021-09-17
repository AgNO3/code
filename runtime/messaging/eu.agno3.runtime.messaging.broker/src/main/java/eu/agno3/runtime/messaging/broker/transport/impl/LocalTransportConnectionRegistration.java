/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.broker.transport.impl;


import java.util.Dictionary;
import java.util.Hashtable;

import javax.jms.XAConnectionFactory;

import org.apache.log4j.Logger;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.messaging.broker.EmbeddedBrokerService;
import eu.agno3.runtime.messaging.broker.transport.TransportConfiguration;
import eu.agno3.runtime.messaging.broker.transport.TransportFactory;
import eu.agno3.runtime.util.osgi.DsUtil;


/**
 * @author mbechler
 * 
 */
@Component ( immediate = true )
public class LocalTransportConnectionRegistration {

    private static final String LOCAL_TYPE = "vm"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(LocalTransportConnectionRegistration.class);

    private TransportFactory transport;
    private ServiceRegistration<XAConnectionFactory> serviceRegistration;


    @Activate
    protected synchronized void activate ( ComponentContext context ) {
        log.debug("Registering embedded broker connection factory"); //$NON-NLS-1$
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put(TransportConfiguration.CONNECTION_FACTORY_TYPE_PROP, LOCAL_TYPE);
        properties.put(TransportConfiguration.CONNECTION_FACTORY_URL_PROP, this.transport.getBrokerURI().toString());
        properties.put(TransportConfiguration.CONNECTION_FACTORY_BORROW_TIMEOUT, this.transport.getBorrowTimeout());
        properties.put(TransportConfiguration.CONNECTION_FACTORY_MAX_POOL_SIZE, this.transport.getPoolSize());
        this.serviceRegistration = DsUtil.registerSafe(context, XAConnectionFactory.class, this.transport.createConnectionFactory(), properties);
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext context ) {
        if ( this.serviceRegistration != null ) {
            DsUtil.unregisterSafe(context, this.serviceRegistration);
            this.serviceRegistration = null;
        }
    }


    @Reference
    protected synchronized void setBrokerService ( EmbeddedBrokerService service ) {}


    protected synchronized void unsetBrokerService ( EmbeddedBrokerService service ) {}


    @Reference ( target = "(" + TransportConfiguration.CONNECTION_FACTORY_TYPE_PROP + "=" + LOCAL_TYPE + ")" )
    protected synchronized void setVMTransport ( TransportFactory vmTransport ) {
        this.transport = vmTransport;
    }


    protected synchronized void unsetVMTransport ( TransportFactory vmTransport ) {
        if ( this.transport == vmTransport ) {
            this.transport = null;
        }
    }
}
