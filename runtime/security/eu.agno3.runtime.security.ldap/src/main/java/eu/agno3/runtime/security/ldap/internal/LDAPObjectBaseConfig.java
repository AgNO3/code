/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2015 by mbechler
 */
package eu.agno3.runtime.security.ldap.internal;


import java.util.Dictionary;

import org.apache.commons.lang3.StringUtils;

import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchScope;


/**
 * @author mbechler
 *
 */
public class LDAPObjectBaseConfig {

    private String baseDN;
    private Filter filter;
    private SearchScope scope;


    /**
     * @param baseDN
     * @param scope
     * @param filter
     */
    public LDAPObjectBaseConfig ( String baseDN, SearchScope scope, Filter filter ) {
        this.baseDN = baseDN;
        this.scope = scope;
        this.filter = filter;
    }


    /**
     * @return the baseDN
     */
    public String getBaseDN () {
        return this.baseDN;
    }


    /**
     * @return the filter
     */
    public Filter getFilter () {
        return this.filter;
    }


    /**
     * @return the scope
     */
    public SearchScope getScope () {
        return this.scope;
    }


    /**
     * @param properties
     * @param prefix
     * @param defaultFilter
     * @return a object config
     * @throws LDAPException
     */
    public static LDAPObjectBaseConfig parseConfig ( Dictionary<String, Object> properties, String prefix, Filter defaultFilter )
            throws LDAPException {

        String baseDN = StringUtils.EMPTY;
        String baseDNAttr = (String) properties.get(prefix + "baseDn"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(baseDNAttr) ) {
            baseDN = baseDN.trim();
        }

        SearchScope scope = SearchScope.SUB;
        String scopeAttr = (String) properties.get(prefix + "scope"); //$NON-NLS-1$
        if ( scopeAttr != null && "SUB".equalsIgnoreCase(scopeAttr.trim()) ) { //$NON-NLS-1$
            scope = SearchScope.SUB;
        }
        else if ( scopeAttr != null && "ONE".equalsIgnoreCase(scopeAttr.trim()) ) { //$NON-NLS-1$
            scope = SearchScope.ONE;
        }

        Filter filter = defaultFilter;
        String filterAttr = (String) properties.get(prefix + "filter"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(filterAttr) ) {
            filter = Filter.create(filterAttr.trim());
        }

        return new LDAPObjectBaseConfig(baseDN, scope, filter);
    }

}
