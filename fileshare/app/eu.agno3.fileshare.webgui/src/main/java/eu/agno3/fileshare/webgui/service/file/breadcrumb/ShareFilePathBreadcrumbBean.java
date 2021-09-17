/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file.breadcrumb;


import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.webgui.service.share.ShareTabsBean;
import eu.agno3.runtime.jsf.view.stacking.DialogContext;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "shareFilePathBreadcrumbBean" )
public class ShareFilePathBreadcrumbBean extends AbstractFilePathBreadcrumbBean {

    /**
     * 
     */
    private static final long serialVersionUID = 5341200894711557136L;

    @Inject
    private ShareTabsBean shareTab;


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.file.breadcrumb.AbstractFilePathBreadcrumbBean#makeOutcome(eu.agno3.fileshare.model.VFSEntity,
     *      eu.agno3.fileshare.model.Grant)
     */
    @Override
    protected String makeOutcome ( VFSEntity e, Grant g ) {

        if ( !e.hasParent() ) {
            return null;
        }

        String outerReturn = StringUtils.EMPTY;
        if ( DialogContext.isInDialog() ) {
            outerReturn = DialogContext.getCurrentStack().get(0).getId();
        }

        return String.format("/actions/share.xhtml?entity=%s&tab=%s&returnTo=%s", e.getEntityKey(), this.shareTab.getTab(), outerReturn); //$NON-NLS-1$
    }

}
