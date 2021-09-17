/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.09.2015 by mbechler
 */
package eu.agno3.orchestrator.server.auth.cas;


import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.jasig.cas.authentication.principal.Principal;


/**
 * @author mbechler
 *
 */
public class SimplePrincipal implements Principal {

    /**
     * 
     */
    private static final long serialVersionUID = -2325707428492882938L;
    private String id;
    private Map<String, Object> attributes;


    /**
     * 
     */
    public SimplePrincipal () {}


    /**
     * 
     * @param id
     */
    public SimplePrincipal ( String id ) {
        this.id = id;
        this.attributes = Collections.EMPTY_MAP;
    }


    /**
     * @param id
     * @param princAttrs
     */
    public SimplePrincipal ( String id, Map<String, Object> princAttrs ) {
        this.id = id;
        this.attributes = princAttrs;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.jasig.cas.authentication.principal.Principal#getAttributes()
     */
    @Override
    public Map<String, Object> getAttributes () {
        return this.attributes;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.jasig.cas.authentication.principal.Principal#getId()
     */
    @Override
    public String getId () {
        return this.id;
    }


    @Override
    public boolean equals ( Object obj ) {
        if ( obj instanceof SimplePrincipal ) {
            return Objects.equals(this.id, ( (SimplePrincipal) obj ).id);
        }
        return super.equals(obj);
    }


    @Override
    public int hashCode () {
        return Objects.hashCode(this.id);
    }


    @Override
    public String toString () {
        return String.format("%s %s", this.id, this.attributes); //$NON-NLS-1$
    }

}
