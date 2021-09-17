/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file.info;


import java.io.Serializable;
import java.util.Date;

import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.webgui.service.file.URLFileSelectionBean;
import eu.agno3.fileshare.webgui.service.share.SharePolicyBean;


/**
 * @author mbechler
 *
 */
@Named ( "expirationEditorBean" )
@ViewScoped
public class ExpirationEditorBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1982455309622257675L;

    @Inject
    private URLFileSelectionBean fileSelection;

    @Inject
    private EntityInfoController controller;

    @Inject
    private SharePolicyBean sharePolicy;

    private boolean expirationLoaded;
    private DateTime expires;


    /**
     * @return the expiration date
     */
    public DateTime getExpires () {
        if ( !this.expirationLoaded ) {
            this.expirationLoaded = true;
            VFSEntity e = this.fileSelection.getSingleSelection();
            if ( e != null ) {
                this.expires = e.getExpires();
            }
        }
        return this.expires;
    }


    /**
     * 
     * @param expires
     */
    public void setExpires ( DateTime expires ) {
        this.expires = expires;
    }


    /**
     * 
     * @param ev
     */
    public void unset ( ActionEvent ev ) {
        unset();
    }


    /**
     * 
     */
    void unset () {
        this.controller.updateExpirationDate((VFSFileEntity) this.fileSelection.getSingleSelection(), null);
        this.expires = null;
        this.expirationLoaded = true;
    }


    /**
     * 
     * @param ev
     */
    public void reset ( AjaxBehaviorEvent ev ) {
        this.expires = null;
        this.expirationLoaded = false;
    }


    /**
     * 
     * @return the minimum selectable expiry date
     */
    public Date getMinExpires () {
        return DateTime.now().plusDays(1).toDate();
    }


    /**
     * 
     * @return whether unsetting the expiration is possible.
     */
    public boolean canUnsetExpiration () {
        return this.sharePolicy.getMaximumExpirationDuration(this.fileSelection.getSingleSelection()) == null;
    }


    /**
     * 
     * @return the maximum selectable expiry date
     */
    public Date getMaxExpires () {
        Duration maximumExpirationDuration = this.sharePolicy.getMaximumExpirationDuration(this.fileSelection.getSingleSelection());
        if ( maximumExpirationDuration != null ) {
            return DateTime.now().plus(maximumExpirationDuration).withTime(0, 0, 0, 0).toDate();
        }

        return null;
    }

}
