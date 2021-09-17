/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.02.2015 by mbechler
 */
package eu.agno3.runtime.security.ldap;


import java.io.Serializable;

import com.unboundid.ldap.sdk.DN;


/**
 * @author mbechler
 *
 */
public class LDAPPrincipal implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8333754954570030997L;
    private String userDn;
    private String userName;


    /**
     * @param userName
     * @param userDn
     */
    public LDAPPrincipal ( String userName, DN userDn ) {
        this.userName = userName;
        this.userDn = userDn.toNormalizedString();
    }


    /**
     * @return the userDn
     */
    public String getUserDn () {
        return this.userDn;
    }


    /**
     * @return the userName
     */
    public String getUserName () {
        return this.userName;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("ldap: userDn=%s", this.userDn); //$NON-NLS-1$
    }


    // +GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.userDn == null ) ? 0 : this.userDn.hashCode() );
        return result;
    }


    // -GENERATED

    // +GENERATED
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        LDAPPrincipal other = (LDAPPrincipal) obj;
        if ( this.userDn == null ) {
            if ( other.userDn != null )
                return false;
        }
        else if ( !this.userDn.equals(other.userDn) )
            return false;
        return true;
    }
    // -GENERATED

}
