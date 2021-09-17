/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.share;


import javax.faces.event.ActionEvent;

import org.joda.time.DateTime;
import org.primefaces.event.SelectEvent;

import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.TokenGrant;


/**
 * @author mbechler
 *
 */
public class GrantInfoWrapper {

    private Grant grant;
    private GrantInfoBean grantInfoBean;


    /**
     * @param grantInfoBean
     * @param g
     */
    public GrantInfoWrapper ( GrantInfoBean grantInfoBean, Grant g ) {
        this.grantInfoBean = grantInfoBean;
        this.grant = g;
    }


    /**
     * @param ev
     */
    public void handleExpirySelect ( SelectEvent ev ) {
        if ( ev.getObject() instanceof DateTime ) {
            this.grantInfoBean.setExpiryDate(this.grant, (DateTime) ev.getObject());
        }
    }


    /**
     * @param ev
     */
    public void handleRemoveExpiry ( ActionEvent ev ) {
        this.grantInfoBean.setExpiryDate(this.grant, null);
    }


    /**
     * 
     * @return the permissions
     */
    public int getPermissions () {
        return GrantPermission.toInt(this.grant.getPermissions());
    }


    /**
     * 
     * @param perms
     */
    public void setPermissions ( int perms ) {
        this.grantInfoBean.setPermissions(this.grant, GrantPermission.fromInt(perms));
    }


    /**
     * 
     * @return the identifier
     */
    public String getIdentifier () {
        if ( this.grant instanceof TokenGrant ) {
            return ( (TokenGrant) this.grant ).getIdentifier();
        }
        return null;
    }


    /**
     * @param identifier
     */
    public void setIdentifier ( String identifier ) {
        if ( this.grant instanceof TokenGrant ) {
            this.grantInfoBean.updateIdentifier((TokenGrant) this.grant, identifier);
        }
    }


    /**
     * 
     * @return the grant comment
     */
    public String getComment () {
        if ( this.grant instanceof TokenGrant ) {
            return ( (TokenGrant) this.grant ).getComment();
        }
        return null;
    }


    /**
     * 
     * @param comment
     */
    public void setComment ( String comment ) {
        if ( this.grant instanceof TokenGrant ) {
            this.grantInfoBean.updateComment((TokenGrant) this.grant, comment);
        }
    }

}
