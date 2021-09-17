/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin.user;


import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.fileshare.model.SecurityLabel;


/**
 * @author mbechler
 *
 */
@Named ( "app_fs_adm_userRootSecurityLabelEditorBean" )
@ViewScoped
public class UserRootSecurityLabelEditorBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1982455309622257675L;

    @Inject
    private UserSelectionBean userSelection;

    private boolean labelLoaded;
    private String label;


    /**
     * @return the label
     */
    public String getLabel () {
        if ( !this.labelLoaded ) {
            this.labelLoaded = true;
            SecurityLabel e = this.userSelection.getSubjectRootLabel();
            if ( e != null ) {
                this.label = e.getLabel();
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
     * 
     */
    public void refresh () {
        this.labelLoaded = false;
        this.userSelection.refreshSubjectRootLabel();
        getLabel();
    }

}
