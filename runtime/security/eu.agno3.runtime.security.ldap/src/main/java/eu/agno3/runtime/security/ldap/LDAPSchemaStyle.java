/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.03.2015 by mbechler
 */
package eu.agno3.runtime.security.ldap;


import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPException;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( "nls" )
public enum LDAPSchemaStyle {

    /**
     * inetOrgPerson, groupOfNames
     */
    LDAP("objectClass", "inetOrgPerson", "groupOfNames", true, false),

    /**
     * inetOrgPerson, groupOfUniqueNames (the unique part is not supported, yet?)
     */
    LDAP_UNIQUE("objectClass", "inetOrgPerson", "groupOfUniqueNames", true, false),

    /**
     * posixAccount, posixGroup
     */
    POSIX("objectClass", "posixAccount", "posixGroup", false, false),

    /**
     * user, group
     */
    AD("objectCategory", "user", "group", true, true);

    private String typeAttribute;
    private String userObjectClass;
    private String groupObjectClass;
    private boolean groupMembersAreDN;
    private boolean idsAreBinary;


    LDAPSchemaStyle ( String typeAttribute, String userObjectClass, String groupObjectClass, boolean groupMembersAreDN, boolean idsAreBinary ) {
        this.typeAttribute = typeAttribute;
        this.userObjectClass = userObjectClass;
        this.groupObjectClass = groupObjectClass;
        this.groupMembersAreDN = groupMembersAreDN;
        this.idsAreBinary = idsAreBinary;
    }


    /**
     * @return the userObjectClass
     */
    public String getUserObjectClass () {
        return this.userObjectClass;
    }


    /**
     * @return the groupObjectClass
     */
    public String getGroupObjectClass () {
        return this.groupObjectClass;
    }


    /**
     * @return the groupMembersAreDN
     */
    public boolean isGroupMembersAreDN () {
        return this.groupMembersAreDN;
    }


    /**
     * @return the idsAreBinary
     */
    public boolean isIdsAreBinary () {
        return this.idsAreBinary;
    }


    /**
     * @return
     */
    private String getTypeAttribute () {
        return this.typeAttribute;
    }


    /**
     * @return a filter for matching user objects
     * @throws LDAPException
     */
    public Filter createUserFilter () throws LDAPException {
        return Filter.createEqualityFilter(this.getTypeAttribute(), this.getUserObjectClass());
    }


    /**
     * @return a filter for matching group objects
     * @throws LDAPException
     */
    public Filter createGroupFilter () throws LDAPException {
        return Filter.createEqualityFilter(this.getTypeAttribute(), this.getGroupObjectClass());
    }


    /**
     * @param baseDN
     * @param entryDN
     * @return whether to exclude a given object
     */
    @SuppressWarnings ( "static-method" )
    public boolean shouldExclude ( DN baseDN, DN entryDN ) {
        return false;
    }

}
