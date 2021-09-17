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
public enum CharacterClass {

    LOWERCASE(26),

    UPPERCASE(26),

    DIGIT(10),

    SYMBOL(33),

    EXTENDED(100);

    private int bruteforceCardinality;


    /**
     * 
     */
    private CharacterClass ( int bruteforceCardinality ) {
        this.bruteforceCardinality = bruteforceCardinality;
    }


    /**
     * @return the bruteforceCardinality
     */
    public int getBruteforceCardinality () {
        return this.bruteforceCardinality;
    }
}
