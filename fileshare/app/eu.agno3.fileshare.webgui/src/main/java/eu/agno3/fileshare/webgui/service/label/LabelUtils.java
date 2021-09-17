/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.03.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.label;


import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.fileshare.model.SecurityLabel;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.webgui.i18n.FileshareMessages;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "labelUtils" )
public class LabelUtils {

    @Inject
    private FileshareServiceProvider fsp;


    /**
     * @param e
     * @param parent
     * @return a message if higher or null
     * 
     */
    public FacesMessage getWarningIfHigherThanContainer ( VFSEntity e, VFSContainerEntity parent ) {
        if ( parent == null || e == null ) {
            return null;
        }
        SecurityLabel entityLabel = e.getSecurityLabel();
        SecurityLabel parentLabel = parent.getSecurityLabel();

        if ( this.fsp.getConfigurationProvider().getSecurityPolicyConfiguration().compareLabels(entityLabel, parentLabel) > 0 ) {
            return new FacesMessage(
                FacesMessage.SEVERITY_WARN,
                FileshareMessages.format(
                    "entity.label.higherFmt", //$NON-NLS-1$
                    e.getLocalName(),
                    entityLabel != null ? entityLabel.getLabel() : FileshareMessages.get(FileshareMessages.UNLABELED_ENTITY),
                    parentLabel != null ? parentLabel.getLabel() : FileshareMessages.get(FileshareMessages.UNLABELED_ENTITY)),
                StringUtils.EMPTY);
        }

        return null;
    }


    /**
     * @param e
     * @param parent
     */
    public void addWarningIfHigherThanContainer ( VFSFileEntity e, VFSContainerEntity parent ) {
        FacesMessage msg = getWarningIfHigherThanContainer(e, parent);
        if ( msg != null ) {
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }
}
