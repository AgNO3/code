/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2015 by mbechler
 */
package eu.agno3.runtime.security.ldap.internal;


import eu.agno3.runtime.security.ldap.LDAPGroup;


/**
 * @author mbechler
 *
 */
public class LDAPGroupImpl implements LDAPGroup {

    private String displayName;
    private String name;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPGroup#getDisplayName()
     */
    @Override
    public String getDisplayName () {
        return this.displayName;
    }


    /**
     * @param displayName
     *            the displayName to set
     */
    public void setDisplayName ( String displayName ) {
        this.displayName = displayName;
    }


    /**
     * @return the name
     */
    @Override
    public String getName () {
        return this.name;
    }


    /**
     * @param name
     */
    public void setName ( String name ) {
        this.name = name;
    }
}
