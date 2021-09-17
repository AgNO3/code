/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.11.2013 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.instance;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.orchestrator.agent.AgentInfo;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.service.InstanceService;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;


/**
 * @author mbechler
 * 
 */
@Named ( "instanceAdd" )
@ViewScoped
public class InstanceAddContextBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -8041010546687762713L;

    private InstanceStructuralObjectImpl newInstance;

    private UUID selectedAgentId;

    private String hostDisplayName;

    @Inject
    private ServerServiceProvider ssp;

    private List<AgentInfo> detachedAgentsCache;

    private List<String> imageTypeCache;


    @PostConstruct
    protected void init () {
        this.newInstance = new InstanceStructuralObjectImpl();
        this.newInstance.setReleaseStream("RELEASE"); //$NON-NLS-1$
    }


    /**
     * @return the hostName
     */
    public String getHostDisplayName () {
        if ( StringUtils.isBlank(this.hostDisplayName) && this.getSelectedAgentId() != null ) {
            String hname = this.getSelectedAgent().getLastKnownHostName();
            int sep = hname.indexOf('.');
            if ( sep > 0 ) {
                hname = hname.substring(0, sep);
            }
            return hname;
        }
        return this.hostDisplayName;
    }


    /**
     * @return the selected agent info
     */
    public AgentInfo getSelectedAgent () {
        UUID agentId = this.getSelectedAgentId();

        if ( agentId == null ) {
            return null;
        }

        for ( AgentInfo i : this.detachedAgentsCache ) {
            if ( agentId.equals(i.getComponentId()) ) {
                return i;
            }
        }

        return null;
    }


    /**
     * @param hostName
     *            the hostName to set
     */
    public void setHostDisplayName ( String hostName ) {
        this.hostDisplayName = hostName;
    }


    /**
     * @return the selectedAgent
     */
    public UUID getSelectedAgentId () {
        return this.selectedAgentId;
    }


    /**
     * @param selectedAgent
     *            the selectedAgent to set
     */
    public void setSelectedAgentId ( UUID selectedAgent ) {
        this.selectedAgentId = selectedAgent;
    }


    public static String getHostNameOrAddress ( AgentInfo i ) {
        if ( i == null || StringUtils.isBlank(i.getLastKnownAddress()) ) {
            return "<unknown>"; //$NON-NLS-1$
        }

        if ( !StringUtils.isBlank(i.getLastKnownHostName()) ) {
            return i.getLastKnownHostName();
        }

        return i.getLastKnownAddress();
    }


    /**
     * @return the newInstance
     */
    @Valid
    @NotNull
    public InstanceStructuralObjectImpl getNewInstance () {
        return this.newInstance;
    }


    public List<AgentInfo> getDetachedAgents () throws GuiWebServiceException {
        if ( this.detachedAgentsCache == null ) {
            this.detachedAgentsCache = new ArrayList<>(this.ssp.getService(InstanceService.class).getDetachedAgents());
        }
        return this.detachedAgentsCache;
    }


    public List<String> getImageTypes () throws GuiWebServiceException {
        if ( this.imageTypeCache == null ) {
            this.imageTypeCache = new ArrayList<>(this.ssp.getService(InstanceService.class).getAvailableImageTypes());
        }
        return this.imageTypeCache;
    }
}
