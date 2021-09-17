/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.03.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.nav;


import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;


/**
 * @author mbechler
 *
 */
@Named ( "pingBean" )
@RequestScoped
public class PingBean {

    private static final Logger log = Logger.getLogger(PingBean.class);


    /**
     */
    public void ping () {
        log.trace("Ping"); //$NON-NLS-1$
        log.trace(SecurityUtils.getSubject().getSession().getLastAccessTime());
        log.trace(SecurityUtils.getSubject().getSession().getStartTimestamp());
    }

}
