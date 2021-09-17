/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.09.2015 by mbechler
 */
package eu.agno3.runtime.ldap.client;


/**
 * @author mbechler
 *
 */
public enum AttributeMatchStyle {

    /**
     * Regular string match
     */
    STRING,

    /**
     * String match ignoring case
     */
    STRING_IGNORECASE,

    /**
     * Binary SID to string SID match
     */
    SID,

    /**
     * 
     */
    RID
}
