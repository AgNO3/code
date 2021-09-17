/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.10.2015 by mbechler
 */
package eu.agno3.runtime.update.internal;


import java.net.URI;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.eclipse.equinox.internal.p2.core.ProvisioningAgent;
import org.eclipse.equinox.p2.core.IAgentLocation;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;

import eu.agno3.runtime.util.osgi.DsUtil;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( "restriction" )
public class AgentProviderImpl implements IProvisioningAgentProvider, AutoCloseable {

    private static final Logger log = Logger.getLogger(AgentProviderImpl.class);

    private URI targetArea;
    private ComponentContext componentContext;

    private ServiceRegistration<IProvisioningAgent> reg;


    /**
     * @param componentContext
     * @param targetArea
     */
    public AgentProviderImpl ( ComponentContext componentContext, URI targetArea ) {
        this.componentContext = componentContext;
        this.targetArea = targetArea;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.equinox.p2.core.IProvisioningAgentProvider#createAgent(java.net.URI)
     */
    @Override
    public IProvisioningAgent createAgent ( URI location ) throws ProvisionException {
        IProvisioningAgent existing = getExistingAgent();
        if ( existing != null ) {
            return existing;
        }
        return registerNewAgent(location);
    }


    /**
     * @param location
     * @return
     */
    protected IProvisioningAgent registerNewAgent ( URI location ) {
        log.debug("Registering new agent"); //$NON-NLS-1$
        ProvisioningAgent result = new ProvisioningAgent();
        result.setBundleContext(this.componentContext.getBundleContext());
        result.setLocation(location);
        IAgentLocation agentLocation = (IAgentLocation) result.getService(IAgentLocation.SERVICE_NAME);
        Dictionary<String, Object> properties = new Hashtable<>(5);
        if ( agentLocation != null ) {
            properties.put("locationURI", String.valueOf(agentLocation.getRootLocation())); //$NON-NLS-1$
        }
        // make the currently running system have a higher service ranking
        if ( location == null ) {
            properties.put(Constants.SERVICE_RANKING, Integer.valueOf(100));
            properties.put(IProvisioningAgent.SERVICE_CURRENT, Boolean.TRUE.toString());
        }
        if ( this.componentContext != null ) {
            this.reg = DsUtil.registerSafe(this.componentContext, IProvisioningAgent.class, result, properties);
        }
        return result;
    }


    /**
     * @return
     * @throws ProvisionException
     */
    protected IProvisioningAgent getExistingAgent () throws ProvisionException {
        String filter = "(locationURI=" + encodeForFilter(this.targetArea.toString()) + ')'; //$NON-NLS-1$
        try {
            if ( this.componentContext != null ) {
                Collection<ServiceReference<IProvisioningAgent>> refs = this.componentContext.getBundleContext()
                        .getServiceReferences(IProvisioningAgent.class, filter);
                if ( !refs.isEmpty() ) {
                    log.debug("Using existing agent"); //$NON-NLS-1$
                    ServiceReference<IProvisioningAgent> ref = refs.iterator().next();
                    IProvisioningAgent result = this.componentContext.getBundleContext().getService(ref);
                    this.componentContext.getBundleContext().ungetService(ref);
                    return result;
                }
                else if ( this.reg != null ) {
                    throw new ProvisionException("Tried to register multiple agent instances"); //$NON-NLS-1$
                }
            }
        }
        catch ( InvalidSyntaxException e ) {
            throw new ProvisionException("Failed to build filter", e); //$NON-NLS-1$
        }

        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close () {
        if ( this.reg != null ) {
            log.debug("Unregistering agent"); //$NON-NLS-1$
            DsUtil.unregisterSafe(this.componentContext, this.reg);
            this.reg = null;
        }
    }


    /**
     * Encodes a string so that it is suitable for use as a value for a filter property.
     * Any reserved filter characters are escaped.
     */
    private static String encodeForFilter ( String string ) {
        StringBuffer result = new StringBuffer(string.length());
        char[] input = string.toCharArray();
        for ( int i = 0; i < input.length; i++ ) {
            switch ( input[ i ] ) {
            case '(':
            case ')':
            case '*':
            case '\\':
                result.append('\\');
                result.append(input[ i ]);
                break;
            default:
                result.append(input[ i ]);
            }
        }
        return result.toString();
    }
}
