/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2015 by mbechler
 */
package eu.agno3.runtime.security.ldap;


import com.unboundid.ldap.sdk.SearchResultEntry;


/**
 * @author mbechler
 * @param <T>
 * @param <TAttrs>
 *
 */
public interface LDAPObjectMapper <T, TAttrs extends Enum<?>> {

    /**
     * @param entry
     * @return the mapped object
     */
    T mapObject ( SearchResultEntry entry );


    /**
     * 
     * @param attr
     * @return a mapped attribute name
     */
    String getAttributeName ( TAttrs attr );
}
