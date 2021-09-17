/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 28, 2016 by mbechler
 */
package eu.agno3.fileshare.orch.webgui.bootstrap;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import eu.agno3.fileshare.orch.common.bootstrap.FileshareBootstrapContext;
import eu.agno3.orchestrator.bootstrap.BootstrapContext;
import eu.agno3.orchestrator.server.webgui.bootstrap.BootstrapControllerPlugin;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class FileshareBootstrapController implements BootstrapControllerPlugin {

    @Inject
    private BootstrapFileshareConfigContextBean fcContext;

    @Inject
    private FileshareBootstrapExtraContext extraContext;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.bootstrap.BootstrapControllerPlugin#getId()
     */
    @Override
    public String getId () {
        return "fileshare"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.bootstrap.BootstrapControllerPlugin#getIncludeTemplate()
     */
    @Override
    public String getIncludeTemplate () {
        return "/tpl/bootstrap/fileshare.xhtml"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.bootstrap.BootstrapControllerPlugin#getLastStep()
     */
    @Override
    public String getLastStep () {
        return "fileshare"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.bootstrap.BootstrapControllerPlugin#contributeContext(eu.agno3.orchestrator.bootstrap.BootstrapContext)
     */
    @Override
    public void contributeContext ( BootstrapContext context ) {
        if ( ! ( context instanceof FileshareBootstrapContext ) ) {
            return;
        }

        FileshareBootstrapContext c = (FileshareBootstrapContext) context;
        c.setFileshareConfig(this.fcContext.getCurrent());

        if ( this.extraContext.getCreateUser() ) {
            c.setCreateUser(true);
            c.setCreateUserAdmin(this.extraContext.getCreateUserAdmin());
            c.setCreateUserName(this.extraContext.getCreateUserName());
            c.setCreateUserPassword(this.extraContext.getCreateUserPassword());
        }
    }

}
