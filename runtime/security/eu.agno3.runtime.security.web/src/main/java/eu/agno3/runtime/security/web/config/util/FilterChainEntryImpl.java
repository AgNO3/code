/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.10.2013 by mbechler
 */
package eu.agno3.runtime.security.web.config.util;


import eu.agno3.runtime.security.web.config.FilterChainEntry;


/**
 * @author mbechler
 * 
 */
public class FilterChainEntryImpl implements FilterChainEntry {

    private int priority;
    private String path;
    private String filterChainSpec;


    /**
     * @param priority
     * @param path
     * @param filterChainSpec
     */
    public FilterChainEntryImpl ( int priority, String path, String filterChainSpec ) {
        super();
        this.priority = priority;
        this.path = path;
        this.filterChainSpec = filterChainSpec;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.security.web.config.FilterChainEntry#getPriority()
     */
    @Override
    public int getPriority () {
        return this.priority;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.security.web.config.FilterChainEntry#getPath()
     */
    @Override
    public String getPath () {
        return this.path;
    }


    /**
     * @return the filterChainSpec
     */
    @Override
    public String getChainDefinition () {
        return this.filterChainSpec;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo ( FilterChainEntry o ) {

        if ( o == null ) {
            return -1;
        }

        int res = Integer.compare(this.getPriority(), o.getPriority());

        if ( res == 0 ) {
            if ( o.hashCode() == this.hashCode() && !this.equals(o) ) {
                return Integer.compare(System.identityHashCode(this), System.identityHashCode(o));
            }

            return Integer.compare(this.hashCode(), o.hashCode());
        }

        return res;
    }


    @Override
    @SuppressWarnings ( "all" )
    // +GENERATED
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.filterChainSpec == null ) ? 0 : this.filterChainSpec.hashCode() );
        result = prime * result + ( ( this.path == null ) ? 0 : this.path.hashCode() );
        result = prime * result + this.priority;
        return result;
    }


    // -GENERATED

    @Override
    @SuppressWarnings ( "all" )
    // +GENERATED
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( ! ( obj instanceof FilterChainEntryImpl ) )
            return false;
        FilterChainEntryImpl other = (FilterChainEntryImpl) obj;
        if ( this.filterChainSpec == null ) {
            if ( other.filterChainSpec != null )
                return false;
        }
        else if ( !this.filterChainSpec.equals(other.filterChainSpec) )
            return false;
        if ( this.path == null ) {
            if ( other.path != null )
                return false;
        }
        else if ( !this.path.equals(other.path) )
            return false;
        if ( this.priority != other.priority )
            return false;
        return true;
    }
    // -GENERATED

}
