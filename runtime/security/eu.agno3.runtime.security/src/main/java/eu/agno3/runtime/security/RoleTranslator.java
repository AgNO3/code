/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.07.2015 by mbechler
 */
package eu.agno3.runtime.security;


import java.util.Locale;


/**
 * @author mbechler
 *
 */
public interface RoleTranslator {

    /**
     * 
     * @param role
     * @param l
     * @return translated role name
     */
    public String getRoleTitle ( String role, Locale l );


    /**
     * 
     * @param role
     * @param l
     * @return translated role description
     */
    public String getRoleDescription ( String role, Locale l );
}
