/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.02.2016 by mbechler
 */
package eu.agno3.fileshare.orch.webgui.config;


import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.fileshare.orch.common.config.FileshareWebConfig;
import eu.agno3.orchestrator.config.hostconfig.HostIdentification;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.components.AbstractObjectEditor;
import eu.agno3.orchestrator.server.webgui.components.OuterWrapper;
import eu.agno3.orchestrator.server.webgui.config.web.SSLConfigContext;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.hostconfig.EffectiveHostConfigProvider;


/**
 * @author mbechler
 *
 */
@Named ( "fs_webSSLConfigContext" )
@ApplicationScoped
public class FileshareWebSSLConfigContext implements SSLConfigContext {

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
     * @param outer
     * @return
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    private static String getOverrideBaseHostname ( OuterWrapper<?> outer )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        OuterWrapper<?> outerWrapper = outer.get("urn:agno3:objects:1.0:fileshare:web"); //$NON-NLS-1$
        AbstractObjectEditor<?> editor = outerWrapper.getEditor();
        if ( editor == null ) {
            return null;
        }

        Object cur = editor.getCurrent();
        if ( cur instanceof FileshareWebConfig ) {
            FileshareWebConfig fwc = (FileshareWebConfig) cur;
            if ( fwc.getOverrideBaseURI() != null && !StringUtils.isBlank(fwc.getOverrideBaseURI().getHost()) ) {
                return fwc.getOverrideBaseURI().getHost();
            }
        }

        Object def = editor.getDefaults();
        if ( def instanceof FileshareWebConfig ) {
            FileshareWebConfig fwc = (FileshareWebConfig) def;
            if ( fwc.getOverrideBaseURI() != null && !StringUtils.isBlank(fwc.getOverrideBaseURI().getHost()) ) {
                return fwc.getOverrideBaseURI().getHost();
            }
        }

        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.config.web.SSLConfigContext#getSANs(eu.agno3.orchestrator.server.webgui.components.OuterWrapper)
     */
    @Override
    public List<String> getSANs ( OuterWrapper<?> outer ) {
        List<String> sans = new ArrayList<>();

        try {
            String overrideBase = getOverrideBaseHostname(outer);
            if ( !StringUtils.isBlank(overrideBase) ) {
                sans.add(overrideBase);
            }

            String hostName = getHostName();
            if ( !hostName.equals(overrideBase) ) {
                sans.add(hostName);
            }
        }
        catch (
            ModelObjectNotFoundException |
            ModelServiceException |
            GuiWebServiceException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handle(e);
        }

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
