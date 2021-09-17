package eu.agno3.orchestrator.server.webgui.config.web;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.config.hostconfig.HostIdentification;
import eu.agno3.orchestrator.server.webgui.components.OuterWrapper;
import eu.agno3.orchestrator.server.webgui.hostconfig.EffectiveHostConfigProvider;


/**
 * @author mbechler
 *
 */
@Named ( "baseWebSSLConfigContext" )
@ApplicationScoped
public class BaseWebSSLConfigContext implements SSLConfigContext {

    @Inject
    private EffectiveHostConfigProvider hcprov;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.web.SSLConfigContext#getSubject(eu.agno3.orchestrator.server.webgui.components.OuterWrapper)
     */
    @Override
    public String getSubject ( OuterWrapper<?> outer ) {
        return "CN=" + getHostName(); //$NON-NLS-1$
    }


    /**
     * @return
     */
    private String getHostName () {
        HostIdentification hostIdentification = this.hcprov.getEffectiveHostConfiguration().getHostIdentification();
        return String.format("%s.%s", hostIdentification.getHostName(), hostIdentification.getDomainName()); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.web.SSLConfigContext#getSANs(eu.agno3.orchestrator.server.webgui.components.OuterWrapper)
     */
    @Override
    public List<String> getSANs ( OuterWrapper<?> outer ) {
        List<String> sans = new ArrayList<>();
        return sans;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.web.SSLConfigContext#getEKUs(eu.agno3.orchestrator.server.webgui.components.OuterWrapper)
     */
    @Override
    public Set<String> getEKUs ( OuterWrapper<?> outer ) {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.web.SSLConfigContext#getKeyUsage(eu.agno3.orchestrator.server.webgui.components.OuterWrapper)
     */
    @Override
    public Set<String> getKeyUsage ( OuterWrapper<?> outer ) {
        return null;
    }

}
