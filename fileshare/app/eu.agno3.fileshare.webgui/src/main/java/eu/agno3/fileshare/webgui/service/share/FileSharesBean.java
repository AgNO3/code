/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.share;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.GrantType;
import eu.agno3.fileshare.util.GrantComparator;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "fileSharesBean" )
public class FileSharesBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -8763115384731889810L;

    @Inject
    private FileshareServiceProvider fsp;

    @Inject
    private ShareEntitySelection fileSelection;

    private List<Grant> effectiveGrants;


    /**
     * @return the fileSelection
     */
    public ShareEntitySelection getFileSelection () {
        return this.fileSelection;
    }


    /**
     * 
     * @return the effective grants for the selected entity
     */
    public List<Grant> getAllEffectiveGrants () {
        if ( this.effectiveGrants == null ) {
            this.effectiveGrants = makeEffectiveGrants();
        }

        return this.effectiveGrants;
    }


    /**
     * @return
     */
    protected List<Grant> makeEffectiveGrants ( GrantType type ) {

        try {
            List<Grant> grants = new ArrayList<>(this.fsp.getShareService().getEffectiveGrants(this.fileSelection.getSingleSelectionId(), type));
            Collections.sort(grants, new GrantComparator());
            return grants;
        }
        catch ( FileshareException e ) {
            ExceptionHandler.handleException(e);
            return Collections.EMPTY_LIST;
        }
    }


    /**
     * @return
     */
    private List<Grant> makeEffectiveGrants () {

        try {
            List<Grant> grants = new ArrayList<>(this.fsp.getShareService().getEffectiveGrants(this.fileSelection.getSingleSelectionId()));
            Collections.sort(grants, new GrantComparator());
            return grants;
        }
        catch ( FileshareException e ) {
            ExceptionHandler.handleException(e);
            return Collections.EMPTY_LIST;
        }
    }


    /**
     * 
     */
    public void refresh () {
        this.effectiveGrants = makeEffectiveGrants();
        this.fileSelection.refreshSelection();
    }
}
