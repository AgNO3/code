/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.03.2015 by mbechler
 */
package eu.agno3.runtime.security.password.internal;


/**
 * @author mbechler
 *
 */
public interface Dictionary {

    /**
     * @return the dictionary name
     */
    String getName ();


    /**
     * @param word
     * @return >= when word
     */
    int getMatchRank ( String word );

}
