/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 23, 2017 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.instance;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.DashboardColumn;
import org.primefaces.model.DashboardModel;
import org.primefaces.model.DefaultDashboardColumn;
import org.primefaces.model.DefaultDashboardModel;

import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.server.webgui.instance.sysinfo.AgentSysInfoContextBean;
import eu.agno3.orchestrator.server.webgui.licensing.InstanceLicensingBean;
import eu.agno3.orchestrator.server.webgui.structure.AgentStateTracker;
import eu.agno3.orchestrator.server.webgui.update.UpdateContextBean;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "instanceDashboard" )
public class InstanceDashboardBean {

    private DashboardModel model;

    @Inject
    private AgentStateTracker agentState;
    @Inject
    private AgentSysInfoContextBean sysinfoState;
    @Inject
    private UpdateContextBean updateState;
    @Inject
    private InstanceLicensingBean licenseState;


    /**
     * 
     */
    public InstanceDashboardBean () {
        this.model = new DefaultDashboardModel();
        DashboardColumn col1 = new DefaultDashboardColumn();
        DashboardColumn col2 = new DefaultDashboardColumn();
        DashboardColumn col3 = new DefaultDashboardColumn();

        col1.addWidget("hostmemory"); //$NON-NLS-1$
        col2.addWidget("hostswap"); //$NON-NLS-1$
        col3.addWidget("hostload"); //$NON-NLS-1$
        col1.addWidget("hoststorage"); //$NON-NLS-1$
        col2.addWidget("hostupdates"); //$NON-NLS-1$
        col3.addWidget("hostlicense"); //$NON-NLS-1$

        this.model.addColumn(col1);
        this.model.addColumn(col2);
        this.model.addColumn(col3);
    }


    public void refresh ( InstanceStructuralObject instance ) {
        this.agentState.forceRefresh(instance);
        this.sysinfoState.refresh();
        this.updateState.refresh();
        this.licenseState.refresh();
    }


    /**
     * @return the model
     */
    public DashboardModel getModel () {
        return this.model;
    }
}
