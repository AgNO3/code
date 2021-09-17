/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2016 by mbechler
 */
package eu.agno3.orchestrator.system.info.network;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * @author mbechler
 *
 */
public class DHCPOptions implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2943053558715891442L;

    private List<DHCPOption> options = new ArrayList<>();


    /**
     * @return the options
     */
    public List<DHCPOption> getOptions () {
        return this.options;
    }


    /**
     * @param options
     *            the options to set
     */
    public void setOptions ( List<DHCPOption> options ) {
        this.options = options;
    }


    /**
     * @param dhcpOption
     */
    public void add ( DHCPOption dhcpOption ) {
        this.options.add(dhcpOption);
    }


    /**
     * @param key
     *            option key
     * @return the found option
     */
    public DHCPOption get ( String key ) {
        for ( DHCPOption opt : this.options ) {
            if ( key.equals(opt.getKey()) ) {
                return opt;
            }
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return this.options.toString();
    }


    /**
     * @param opts
     */
    public void addAll ( DHCPOptions opts ) {
        if ( opts != null && opts.options != null ) {
            this.options.addAll(opts.options);
        }
    }


    // +GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.options == null ) ? 0 : this.options.hashCode() );
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
        DHCPOptions other = (DHCPOptions) obj;
        if ( this.options == null ) {
            if ( other.options != null )
                return false;
        }
        else if ( !this.options.equals(other.options) )
            return false;
        return true;
    }
    // -GENERATED
}
