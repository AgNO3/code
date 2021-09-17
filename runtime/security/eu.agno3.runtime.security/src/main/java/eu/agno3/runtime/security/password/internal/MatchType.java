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
@SuppressWarnings ( "javadoc" )
public enum MatchType {

    REPEAT,

    SEQUENCE,

    DIGITS,

    YEAR,

    DATE,

    SPATIAL,

    DICTIONARY,

    BRUTEFORCE, CHARPATTERN
}
