/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 11, 2017 by mbechler
 */
package eu.agno3.orchestrator.gui.server.internal;


import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.orchestrator.gui.server.EagerServicesActive;
import eu.agno3.runtime.util.osgi.DsUtil;
import eu.agno3.runtime.ws.common.SOAPServiceClientDescriptor;
import eu.agno3.runtime.ws.server.ActiveWebService;


/**
 * @author mbechler
 *
 */
@Component ( immediate = true, service = EagerServiceTracker.class )
public class EagerServiceTracker {

    private static final Logger log = Logger.getLogger(EagerServiceTracker.class);

    private Set<String> eagerServices = new TreeSet<>();
    private Set<String> activeServices = new TreeSet<>();
    private ComponentContext componentContext;
    private boolean delayed = false;
    private ServiceRegistration<EagerServicesActive> registration;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.componentContext = ctx;
        if ( this.delayed ) {
            check();
        }
    }


    protected synchronized void deactivate ( ComponentContext ctx ) {
        if ( this.registration != null ) {
            DsUtil.unregisterSafe(this.componentContext, this.registration);
        }
        this.delayed = false;
        this.componentContext = null;
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE )
    protected synchronized void bindServiceDescriptor ( SOAPServiceClientDescriptor<?> desc ) {
        if ( desc.isEagerInitialize() ) {
            this.eagerServices.add(desc.getServiceClass().getName());
            check();
        }
    }


    protected synchronized void unbindServiceDescriptor ( SOAPServiceClientDescriptor<?> desc ) {
        if ( desc.isEagerInitialize() ) {
            this.eagerServices.remove(desc.getServiceClass().getName());
            check();
        }
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindActiveWebService ( ActiveWebService aws ) {
        this.activeServices.add(aws.getSeiName());
        check();
    }


    protected synchronized void unbindActiveWebService ( ActiveWebService aws ) {
        this.activeServices.remove(aws.getSeiName());
        check();
    }


    /**
     * 
     */
    private void check () {
        if ( this.activeServices.containsAll(this.eagerServices) ) {
            if ( this.componentContext == null ) {
                log.debug("All services active but not yet activated"); //$NON-NLS-1$
                this.delayed = true;
            }
            else {
                log.debug("All services active"); //$NON-NLS-1$
                this.registration = DsUtil.registerSafe(this.componentContext, EagerServicesActive.class, new EagerServicesActive(), null);
            }
        }
        else {
            log.debug("Not all services active"); //$NON-NLS-1$
            this.delayed = false;
            if ( this.registration != null && this.componentContext != null ) {
                log.debug("Services gone away"); //$NON-NLS-1$
                DsUtil.unregisterSafe(this.componentContext, this.registration);
                this.registration = null;
            }
        }
    }

}
