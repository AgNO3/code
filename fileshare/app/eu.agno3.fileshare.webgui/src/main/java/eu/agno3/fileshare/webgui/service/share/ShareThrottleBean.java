/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.06.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.share;


import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Named;


/**
 * @author mbechler
 *
 */
@Named ( "shareThrottleBean" )
@ViewScoped
public class ShareThrottleBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2555597635777754598L;

    private Integer throttleDelay;


    /**
     * @return the throttleDelay
     */
    public Integer getThrottleDelay () {
        return this.throttleDelay;
    }


    /**
     * @param throttleDelay
     *            the throttleDelay to set
     */
    public void setThrottleDelay ( Integer throttleDelay ) {
        this.throttleDelay = throttleDelay;
    }


    /**
     * 
     */
    public void throttleComplete () {
        this.throttleDelay = null;
    }
}
