/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.09.2016 by mbechler
 */
package eu.agno3.runtime.security.terms;


import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Locale;

import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public interface TermsService {

    /**
     * @param up
     * @return terms definitions that the user has not accepted
     */
    Collection<TermsDefinition> getRequiredTerms ( UserPrincipal up );


    /**
     * @param up
     * @return terms definitions applicable
     */
    Collection<TermsDefinition> getAllTerms ( UserPrincipal up );


    /**
     * 
     * @param id
     * @return terms definition for the given id
     */
    TermsDefinition getTermsById ( String id );


    /**
     * @param up
     * @param id
     */
    void markAccepted ( UserPrincipal up, String id );


    /**
     * @param id
     * @param format
     * @param l
     * @return input stream for the contents
     * @throws IOException
     */
    URL getContents ( String id, String format, Locale l ) throws IOException;


    /**
     * Makes temporary accpentance permanent for the given principal
     * 
     * @param principal
     */
    void persistTemporary ( UserPrincipal principal );

}
