/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file;


import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;


/**
 * @author mbechler
 *
 */
@Named ( "userTokenBean" )
@ViewScoped
public class UserTokenBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3286725448707412510L;
    private String token;


    /**
     * @return the token
     */
    public String getToken () {
        return this.token;
    }


    /**
     * @param token
     *            the token to set
     */
    public void setToken ( String token ) {
        this.token = token;
    }


    /**
     * @return the token as single argument query string
     */
    public String getTokenQueryString () {
        if ( this.token == null || StringUtils.isBlank(this.token) ) {
            return StringUtils.EMPTY;
        }

        return "?token=" + this.token; //$NON-NLS-1$
    }


    /**
     * @return the token to append to a query string
     */
    public String getTokenQueryArg () {
        if ( this.token == null || StringUtils.isBlank(this.token) ) {
            return StringUtils.EMPTY;
        }

        return "&token=" + this.token; //$NON-NLS-1$
    }

}
