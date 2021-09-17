/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.09.2016 by mbechler
 */
package eu.agno3.runtime.security.terms;


import java.util.Locale;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public interface TermsDefinition {

    /**
     * @return the id of these terms
     */
    String getId ();


    /**
     * @param l
     * @return the label to display
     */
    String getLabel ( Locale l );


    /**
     * @param l
     * @return the description to display
     */
    String getDescription ( Locale l );


    /**
     * @return id for the attached terms content
     */
    String getContentId ();


    /**
     * @return the ordering priority (lower is earlier)
     */
    float getPriority ();


    /**
     * @return the time the policy contents were last modified (need to be reaccepted afterwards)
     */
    DateTime getLastModified ();


    /**
     * @return whether to persist the acceptance state
     */
    boolean isPersistAcceptance ();


    /**
     * @return maximum time to store acceptance state for unauthenticated users
     */
    Duration getUnauthPersistenceMaxAge ();


    /**
     * @param up
     * @param roles
     * @return whether these terms are applicable for the given user
     */
    boolean isApplicable ( UserPrincipal up, Set<String> roles );

}
