/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.03.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.subject;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "quotaColorBean" )
public class QuotaColorBean {

    /**
     * 
     * @param percent
     * @return css color string for the used part
     */
    public String getUsedColor ( float percent ) {

        if ( percent < 50 ) {
            return "rgb(50,50,50)"; //$NON-NLS-1$
        }
        else if ( percent < 90 ) {
            return "orange"; //$NON-NLS-1$
        }
        else {
            return "red"; //$NON-NLS-1$
        }

    }


    /**
     * 
     * @param percent
     * @return css color string for the free part
     */
    public String getFreeColor ( float percent ) {
        if ( percent < 50 ) {
            return "green"; //$NON-NLS-1$
        }
        else if ( percent < 90 ) {
            return "yellow"; //$NON-NLS-1$
        }
        else {
            return "black"; //$NON-NLS-1$
        }
    }
}
