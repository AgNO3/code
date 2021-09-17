/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.share;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.MailGrant;
import eu.agno3.fileshare.model.SubjectGrant;
import eu.agno3.fileshare.model.TokenGrant;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "shareDisplayBean" )
public class ShareDisplayBean {

    /**
     * 
     * @param g
     * @return whether g is a subject grant
     */
    public static boolean isSubjectGrant ( Grant g ) {
        return g instanceof SubjectGrant;
    }


    /**
     * 
     * @param g
     * @return whether g is a mail grant
     */
    public static boolean isMailGrant ( Grant g ) {
        return g instanceof MailGrant;
    }


    /**
     * 
     * @param g
     * @return whether g is a token grant
     */
    public static boolean isTokenGrant ( Grant g ) {
        return g instanceof TokenGrant && ! ( g instanceof MailGrant );
    }


    /**
     * 
     * @param g
     * @return whether the grant has a target subject
     */
    public static boolean hasTarget ( Grant g ) {
        return isSubjectGrant(g) && ( (SubjectGrant) g ).getTarget() != null;
    }
}
