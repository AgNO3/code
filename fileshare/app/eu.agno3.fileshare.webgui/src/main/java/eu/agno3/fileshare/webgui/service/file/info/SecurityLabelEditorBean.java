/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file.info;


import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import javax.faces.component.UISelectOne;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.jdt.annotation.Nullable;
import org.primefaces.context.RequestContext;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.InconsistentSecurityLabelException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.SecurityLabel;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.service.config.SecurityPolicyConfiguration;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.service.file.URLFileSelectionBean;


/**
 * @author mbechler
 *
 */
@Named ( "securityLabelEditorBean" )
@ViewScoped
public class SecurityLabelEditorBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1982455309622257675L;

    @Inject
    private URLFileSelectionBean fileSelection;

    @Inject
    private FileshareServiceProvider fsp;

    private boolean labelLoaded;
    private String label;

    private boolean editing;

    private boolean blocked;

    private int numBlockers;


    /**
     * @return the label
     */
    public String getLabel () {
        if ( !this.labelLoaded ) {
            this.labelLoaded = true;
            VFSEntity e = this.fileSelection.getSingleSelection();
            if ( e != null && e.getSecurityLabel() != null ) {
                this.label = e.getSecurityLabel().getLabel();
            }
        }
        return this.label;
    }


    /**
     * @param label
     *            the label to set
     */
    public void setLabel ( String label ) {
        this.label = label;
    }


    /**
     * @return the editing
     */
    public boolean getEditing () {
        return this.editing;
    }


    /**
     * @return the blocked
     */
    public boolean getBlocked () {
        return this.blocked;
    }


    /**
     * @return the numBlockers
     */
    public int getNumBlockers () {
        return this.numBlockers;
    }


    /**
     * @param editing
     *            the editing to set
     */
    public void setEditing ( boolean editing ) {
        this.editing = editing;
    }


    /**
     * @return null
     */
    public String edit () {
        this.editing = true;
        return null;
    }


    /**
     * @param ev
     * 
     */
    public void changeLabel ( AjaxBehaviorEvent ev ) {
        this.blocked = false;

        @Nullable
        VFSEntity root = this.fileSelection.getSingleSelection();
        if ( root == null ) {
            return;
        }

        SecurityPolicyConfiguration secPol = this.fsp.getConfigurationProvider().getSecurityPolicyConfiguration();

        SecurityLabel curLabel = root.getSecurityLabel();
        int res = secPol.compareLabels(this.label, curLabel != null ? curLabel.getLabel() : null);

        if ( res == 0 ) {
            // are equal
            return;
        }
        else if ( res < 0 ) {
            // the new label is lower
            if ( canLowerImmediately(root) ) {
                save();
            }
            else {

                Object comp = ev.getSource();
                if ( comp instanceof UISelectOne && "selectLabel".equals( ( (UISelectOne) comp ).getId()) ) { //$NON-NLS-1$
                    UISelectOne select = (UISelectOne) comp;
                    setLabel((String) select.getSubmittedValue());
                }

                RequestContext.getCurrentInstance().execute("PF('setLabelRecursiveButton').jq.click();"); //$NON-NLS-1$
            }
        }
        else if ( res > 0 ) {
            save();
            if ( this.blocked ) {
                RequestContext.getCurrentInstance().execute("PF('blockedOverlay').show();"); //$NON-NLS-1$
            }
        }
    }


    private static boolean canLowerImmediately ( VFSEntity root ) {
        if ( root instanceof VFSFileEntity ) {
            return true;
        }

        if ( root instanceof VFSContainerEntity ) {
            VFSContainerEntity c = (VFSContainerEntity) root;
            if ( c.getNumChildren() != null && c.getNumChildren() == 0 ) {
                return true;
            }
        }

        return false;
    }


    /**
     * 
     * @return null
     */
    public String save () {
        try {
            this.fsp.getEntityService().setSecurityLabel(this.fileSelection.getSingleSelectionId(), getLabel(), this.blocked);
            this.fileSelection.refreshSelection();
            this.blocked = false;
            this.numBlockers = 0;
            this.editing = false;
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {

            Exception ex = ExceptionHandler.unwrapException(e);

            if ( ex instanceof InconsistentSecurityLabelException ) {
                this.blocked = true;
                Map<String, Collection<EntityKey>> blockers = ( (InconsistentSecurityLabelException) ex ).getBlockers();
                this.numBlockers = blockers.values().stream().collect(Collectors.summingInt( ( x ) -> x.size()));
                return null;
            }

            ExceptionHandler.handleException(e);
        }
        return null;
    }


    /**
     * 
     * @return null
     */
    public String cancel () {
        this.editing = false;
        this.blocked = false;
        this.numBlockers = 0;
        VFSEntity e = this.fileSelection.getSingleSelection();
        if ( e != null && e.getSecurityLabel() != null ) {
            this.label = e.getSecurityLabel().getLabel();
        }
        return null;
    }
}
