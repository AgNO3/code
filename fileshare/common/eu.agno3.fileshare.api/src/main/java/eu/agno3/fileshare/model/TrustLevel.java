/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.05.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import java.util.Locale;


/**
 * @author mbechler
 *
 */
public interface TrustLevel {

    /**
     * 
     * @return the id
     */
    String getId ();


    /**
     * @param l
     * @return the title
     */
    String getTitle ( Locale l );


    /**
     * 
     * @return the matching priority
     */
    float getPriority ();


    /**
     * 
     * 
     * @return the color
     */
    String getColor ();


    /**
     * 
     * @param l
     * @return the message for the given locale
     */
    String getMessage ( Locale l );


    /**
     * 
     * @param s
     * @return whether the trust level matches the given subject
     */
    boolean match ( Subject s );


    /**
     * @return whether the trust level matches anonymous links
     */
    boolean matchLink ();


    /**
     * @param mailAddress
     * @return whether the trust level matches the given mail address
     */
    boolean matchMail ( String mailAddress );


    /**
     * @param g
     * @return whether this trust level matches the given group
     */
    boolean matchGroup ( Group g );

}
