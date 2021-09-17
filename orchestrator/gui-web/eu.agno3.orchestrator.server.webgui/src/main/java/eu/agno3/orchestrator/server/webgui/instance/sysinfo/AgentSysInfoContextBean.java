/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.03.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.instance.sysinfo;


import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.AgentStateTracker;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;
import eu.agno3.orchestrator.system.info.network.NetworkInformation;
import eu.agno3.orchestrator.system.info.network.NetworkInterface;
import eu.agno3.orchestrator.system.info.platform.PlatformInformation;
import eu.agno3.orchestrator.system.info.storage.StorageInformation;


/**
 * @author mbechler
 * 
 */
@Named ( "agentSysInfoContext" )
@ViewScoped
public class AgentSysInfoContextBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4338591484487573077L;

    @Inject
    private AgentSysInfoBean sysInfoBean;

    @Inject
    private AgentStateTracker agentStateTracker;

    @Inject
    private StructureViewContextBean structureContext;

    private PlatformInformation cachedPlatformInformation;
    private boolean platformInformationLoaded;

    private StorageInformation cachedStorageInformation;
    private boolean storageInformationLoaded;

    private NetworkInformation cachedNetworkInformation;
    private boolean networkInformationLoaded;

    private Map<String, String> cachedAliasMap;
    private boolean aliasMapLoaded;


    public void init ( ComponentSystemEvent ev ) {
        try {
            getPlatformInformation();
            getNetworkInformation();
            getStorageInformation();
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }

    }


    public void refresh () {
        this.cachedNetworkInformation = null;
        this.networkInformationLoaded = false;
        this.cachedPlatformInformation = null;
        this.platformInformationLoaded = false;
        this.cachedStorageInformation = null;
        this.storageInformationLoaded = false;
        this.cachedAliasMap = null;
        this.aliasMapLoaded = false;
        try {
            getPlatformInformation();
            getNetworkInformation();
            getStorageInformation();
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
    }


    public synchronized String refreshInformation () {
        try {
            this.sysInfoBean.triggerRefresh(getSelectedInstance());
            refresh();
        }
        catch ( Exception e ) {
            if ( e instanceof AgentOfflineException ) {
                refreshAgentState();
            }
            ExceptionHandler.handle(e);
            return null;
        }
        this.cachedNetworkInformation = null;
        this.networkInformationLoaded = false;
        this.cachedPlatformInformation = null;
        this.platformInformationLoaded = false;
        this.cachedStorageInformation = null;
        this.storageInformationLoaded = false;
        this.cachedAliasMap = null;
        this.aliasMapLoaded = false;
        return null;
    }


    public boolean haveData () {
        return this.cachedNetworkInformation != null && this.cachedPlatformInformation != null && this.cachedStorageInformation != null;
    }


    /**
     * @param e
     */
    private void refreshAgentState () {
        try {
            this.agentStateTracker.forceRefresh(getSelectedInstance());
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
    }


    public boolean haveSystemInformation () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return this.structureContext.isInstanceSelected() && this.structureContext.getSelectedInstance().getAgentId() != null;
    }


    protected InstanceStructuralObject getSelectedInstance () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        InstanceStructuralObject selectedInstance = this.structureContext.getSelectedInstance();

        if ( selectedInstance == null && this.structureContext.getSelectedAnchor() instanceof InstanceStructuralObject ) {
            selectedInstance = (InstanceStructuralObject) this.structureContext.getSelectedAnchor();
        }
        return selectedInstance;
    }


    public synchronized NetworkInformation getNetworkInformation ()
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {

        InstanceStructuralObject selectedInstance = getSelectedInstance();
        if ( selectedInstance == null || selectedInstance.getAgentId() == null || !this.agentStateTracker.isAgentOnline(selectedInstance) ) {
            return null;
        }

        if ( !this.networkInformationLoaded ) {
            this.networkInformationLoaded = true;
            try {
                this.cachedNetworkInformation = this.sysInfoBean.getNetworkInformation(selectedInstance);
            }
            catch ( Exception e ) {
                ExceptionHandler.handle(e);
                refreshAgentState();
            }
        }
        return this.cachedNetworkInformation;
    }


    public synchronized PlatformInformation getPlatformInformation ()
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {

        InstanceStructuralObject selectedInstance = getSelectedInstance();
        if ( selectedInstance == null || selectedInstance.getAgentId() == null || !this.agentStateTracker.isAgentOnline(selectedInstance) ) {
            return null;
        }

        if ( !this.platformInformationLoaded ) {
            this.platformInformationLoaded = true;
            try {
                this.cachedPlatformInformation = this.sysInfoBean.getPlatformInformation(selectedInstance);
            }
            catch ( Exception e ) {
                ExceptionHandler.handle(e);
                refreshAgentState();
            }
        }
        return this.cachedPlatformInformation;
    }


    public synchronized StorageInformation getStorageInformation ()
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {

        InstanceStructuralObject selectedInstance = getSelectedInstance();
        if ( selectedInstance == null || selectedInstance.getAgentId() == null || !this.agentStateTracker.isAgentOnline(selectedInstance) ) {
            return null;
        }

        if ( !this.storageInformationLoaded ) {
            this.storageInformationLoaded = true;
            try {
                this.cachedStorageInformation = this.sysInfoBean.getStorageInformation(selectedInstance);
            }
            catch ( Exception e ) {
                ExceptionHandler.handle(e);
                refreshAgentState();
            }
        }
        return this.cachedStorageInformation;
    }


    /**
     * @return interface alias map
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    public synchronized Map<String, String> getNetworkInterfaceAliasMap ()
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {

        InstanceStructuralObject selectedInstance = getSelectedInstance();
        if ( selectedInstance == null || selectedInstance.getAgentId() == null || !this.agentStateTracker.isAgentOnline(selectedInstance) ) {
            return null;
        }

        if ( !this.aliasMapLoaded ) {
            this.aliasMapLoaded = true;
            Map<String, String> aliasMap;
            try {
                NetworkInformation ni = getNetworkInformation();
                if ( ni == null ) {
                    aliasMap = Collections.EMPTY_MAP;
                }
                else {
                    aliasMap = new HashMap<>();
                    for ( NetworkInterface intf : ni.getNetworkInterfaces() ) {
                        if ( !StringUtils.isBlank(intf.getAlias()) ) {
                            aliasMap.put(intf.getName(), intf.getAlias());
                        }
                    }
                }
            }
            catch ( Exception e ) {
                ExceptionHandler.handle(e);
                aliasMap = Collections.EMPTY_MAP;
            }
            this.cachedAliasMap = aliasMap;
        }
        return this.cachedAliasMap;
    }
}
