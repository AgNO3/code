/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.09.2016 by mbechler
 */
package eu.agno3.runtime.security.web.gui.terms;


import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Locale;

import eu.agno3.runtime.security.terms.TermsDefinition;


/**
 * @author mbechler
 *
 */
public interface TermsBean {

    /**
     * 
     * @param id
     * @param fmt
     * @param l
     * @return URL for the terms contents
     * @throws IOException
     */
    URL getContents ( String id, String fmt, Locale l ) throws IOException;


    /**
     * 
     * @param id
     * @param fmt
     * @return URL for the terms content in the current view locale
     * @throws IOException
     */
    URL getContents ( String id, String fmt ) throws IOException;


    /**
     * 
     * @param id
     * @return label for the given terms
     */
    String getTermsLabel ( String id );


    /**
     * 
     * @param id
     * @return description for the given terms
     */
    String getTermsDescription ( String id );


    /**
     * @return the first terms that have not been accepted by the user
     */
    TermsDefinition getFirstUnaccepted ();


    /**
     * 
     * @return unaccepted terms
     */
    Collection<TermsDefinition> getUnacceptedTerms ();


    /**
     * @param termsId
     */
    void doAccept ( String termsId );


    /**
     * @return page to redirect to for unaccepted terms
     */
    String getUnacceptedRedirect ();


    /**
     * 
     * @param id
     * @return page to view terms
     */
    String getViewLocation ( String id );

}