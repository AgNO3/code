/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.12.2014 by mbechler
 */
package eu.agno3.orchestrator.bootstrap;


import eu.agno3.orchestrator.config.hostconfig.HostConfiguration;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.orchestrator.OrchestratorConfiguration;


/**
 * @author mbechler
 *
 */
public class BootstrapContext {

    private InstanceStructuralObject instance;
    private ServiceStructuralObject hostConfigService;
    private ServiceStructuralObject orchConfigService;

    private HostConfiguration hostConfig;
    private OrchestratorConfiguration orchConfig;

    private boolean requirePasswordChange;
    private int minimumPasswordEntropy;

    private String changeAdminPassword;


    /**
     * 
     * @return type of this bootstrap context
     */
    public String getType () {
        return "standalone"; //$NON-NLS-1$
    }


    /**
     * @return the instance
     */
    public InstanceStructuralObject getInstance () {
        return this.instance;
    }


    /**
     * @param instance
     *            the instance to set
     */
    public void setInstance ( InstanceStructuralObject instance ) {
        this.instance = instance;
    }


    /**
     * @return the hostConfigService
     */
    public ServiceStructuralObject getHostConfigService () {
        return this.hostConfigService;
    }


    /**
     * @param hostConfigService
     *            the hostConfigService to set
     */
    public void setHostConfigService ( ServiceStructuralObject hostConfigService ) {
        this.hostConfigService = hostConfigService;
    }


    /**
     * @return the orchConfigService
     */
    public ServiceStructuralObject getOrchConfigService () {
        return this.orchConfigService;
    }


    /**
     * @param orchConfigService
     *            the orchConfigService to set
     */
    public void setOrchConfigService ( ServiceStructuralObject orchConfigService ) {
        this.orchConfigService = orchConfigService;
    }


    /**
     * @return the hostConfig
     */
    public HostConfiguration getHostConfig () {
        return this.hostConfig;
    }


    /**
     * @param hostConfig
     *            the hostConfig to set
     */
    public void setHostConfig ( HostConfiguration hostConfig ) {
        this.hostConfig = hostConfig;
    }


    /**
     * @return the orchConfig
     */
    public OrchestratorConfiguration getOrchConfig () {
        return this.orchConfig;
    }


    /**
     * @param orchConfig
     *            the orchConfig to set
     */
    public void setOrchConfig ( OrchestratorConfiguration orchConfig ) {
        this.orchConfig = orchConfig;
    }


    /**
     * @return the requirePasswordChange
     */
    public boolean getRequirePasswordChange () {
        return this.requirePasswordChange;
    }


    /**
     * @param requirePasswordChange
     *            the requirePasswordChange to set
     */
    public void setRequirePasswordChange ( boolean requirePasswordChange ) {
        this.requirePasswordChange = requirePasswordChange;
    }


    /**
     * @return minimum password entropy for change
     */
    public int getMinimumPasswordEntropy () {
        return this.minimumPasswordEntropy;
    }


    /**
     * @param minimumPasswordEntropy
     *            the minimumPasswordEntropy to set
     */
    public void setMinimumPasswordEntropy ( int minimumPasswordEntropy ) {
        this.minimumPasswordEntropy = minimumPasswordEntropy;
    }


    /**
     * @return the changeAdminPassword
     */
    public String getChangeAdminPassword () {
        return this.changeAdminPassword;
    }


    /**
     * @param changeAdminPassword
     *            the changeAdminPassword to set
     */
    public void setChangeAdminPassword ( String changeAdminPassword ) {
        this.changeAdminPassword = changeAdminPassword;
    }
}
