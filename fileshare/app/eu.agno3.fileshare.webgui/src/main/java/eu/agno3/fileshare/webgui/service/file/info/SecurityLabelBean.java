/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file.info;


import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.ContainerEntity;
import eu.agno3.fileshare.model.ContentEntity;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.SecurityLabel;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.i18n.FileshareMessages;
import eu.agno3.fileshare.webgui.policy.PolicyBean;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.service.file.FilePermissionBean;
import eu.agno3.fileshare.webgui.subject.CurrentUserBean;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "securityLabelBean" )
public class SecurityLabelBean {

    @Inject
    private FileshareServiceProvider fsp;

    @Inject
    private FilePermissionBean perm;

    @Inject
    private CurrentUserBean currentUser;

    @Inject
    private PolicyBean policyBean;


    /**
     * @param e
     * @param g
     * @return whether mime type changes are allowed for this entity
     */
    public boolean isSecurityLabelChangeAllowed ( VFSEntity e, Grant g ) {
        return this.currentUser.hasPermission("manage:subjects:userRootSecurityLabel") //$NON-NLS-1$
                || this.currentUser.hasPermission("entity:changeSecurityLabel") && this.perm.canEditOrEditSelfEntity(e, g); //$NON-NLS-1$
    }


    /**
     * @param obj
     * @return the display value for the security label
     */
    public String translateSecurityLabel ( Object obj ) {
        if ( ! ( obj instanceof SecurityLabel ) ) {
            return FileshareMessages.get(FileshareMessages.UNLABELED_ENTITY);
        }
        SecurityLabel sl = (SecurityLabel) obj;
        return sl.getLabel();
    }


    /**
     * 
     * @return the defined security labels
     */
    public List<String> getDefinedSecurityLabels () {
        return new ArrayList<>(this.fsp.getConfigurationProvider().getSecurityPolicyConfiguration().getDefinedLabels());
    }


    /**
     * @param o
     * @return the parents security label
     */
    public SecurityLabel getParentLabel ( Object o ) {
        if ( o instanceof ContentEntity ) {
            ContainerEntity parent = ( (ContentEntity) o ).getParent();
            if ( parent != null ) {
                return parent.getSecurityLabel();
            }
        }

        if ( o instanceof VFSEntity ) {
            try {
                VFSContainerEntity parent = this.fsp.getEntityService().getParent( ( (VFSEntity) o ).getEntityKey());
                if ( parent != null ) {
                    return parent.getSecurityLabel();
                }
            }
            catch (
                FileshareException |
                UndeclaredThrowableException e ) {
                ExceptionHandler.handleException(e);
            }
        }

        return null;
    }


    /**
     * 
     * @param securityLabel
     * @return the defined security labels higher (or equals) to the given one
     */
    public List<SelectItem> getDefinedAndFulfilledLabelsHigherThan ( Object securityLabel ) {
        List<SelectItem> res = getDefinedLabelsHigherThan(securityLabel);

        for ( SelectItem label : res ) {
            if ( this.policyBean.getViolationForString((String) label.getValue()) != null ) {
                label.setDisabled(true);
                label.setDescription("You do not currently fulfill the requirements for this policy"); //$NON-NLS-1$
                continue;
            }
        }
        return res;
    }


    /**
     * 
     * @param securityLabel
     * @return the defined security labels higher (or equals) to the given one
     */
    public List<SelectItem> getDefinedLabelsHigherThan ( Object securityLabel ) {
        if ( ! ( securityLabel instanceof SecurityLabel ) ) {
            return toSelectItems(getDefinedSecurityLabels());
        }

        List<String> labels = getDefinedSecurityLabels();
        int idx = labels.indexOf( ( (SecurityLabel) securityLabel ).getLabel());

        if ( idx < 0 ) {
            return toSelectItems(labels);
        }

        List<SelectItem> actual = new ArrayList<>();

        for ( SelectItem itm : toSelectItems(labels.subList(0, idx)) ) {
            itm.setDisabled(true);
            itm.setDescription("Cannot be lower than parent's label"); //$NON-NLS-1$
            actual.add(itm);
        }

        actual.addAll(toSelectItems(labels.subList(idx, labels.size())));
        return actual;
    }


    /**
     * @param labels
     * @return
     */
    private static List<SelectItem> toSelectItems ( List<String> labels ) {
        List<SelectItem> itms = new ArrayList<>();
        for ( String label : labels ) {
            itms.add(new SelectItem(label, label));
        }
        return itms;
    }

}
