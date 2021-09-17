/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 11, 2016 by mbechler
 */
package eu.agno3.orchestrator.system.info.network;


import java.io.Serializable;

import org.joda.time.DateTime;

import eu.agno3.orchestrator.types.net.NetworkSpecification;


/**
 * @author mbechler
 *
 */
public class LeaseEntry implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8696439160201263146L;

    private NetworkSpecification networkSpecification;

    private DateTime expiresTime;
    private DateTime rebindTime;
    private DateTime renewTime;
    private DHCPAssociationType associationType;
    private DHCPLeaseStatus status;
    private DHCPOptions options;


    /**
     * @return the networkSpecification
     */
    public NetworkSpecification getNetworkSpecification () {
        return this.networkSpecification;
    }


    /**
     * @param networkSpecification
     */
    public void setNetworkSpecification ( NetworkSpecification networkSpecification ) {
        this.networkSpecification = networkSpecification;
    }


    /**
     * @return the expiresTime
     */
    public DateTime getExpiresTime () {
        return this.expiresTime;
    }


    /**
     * @param expiresTime
     */
    public void setExpiresTime ( DateTime expiresTime ) {
        this.expiresTime = expiresTime;
    }


    /**
     * @return the rebindTime
     */
    public DateTime getRebindTime () {
        return this.rebindTime;
    }


    /**
     * @param rebindTime
     */
    public void setRebindTime ( DateTime rebindTime ) {
        this.rebindTime = rebindTime;
    }


    /**
     * @return the renewTime
     */
    public DateTime getRenewTime () {
        return this.renewTime;
    }


    /**
     * @param renewTime
     */
    public void setRenewTime ( DateTime renewTime ) {
        this.renewTime = renewTime;
    }


    /**
     * @return the associationType
     */
    public DHCPAssociationType getAssociationType () {
        return this.associationType;
    }


    /**
     * @param associationType
     */
    public void setAssociationType ( DHCPAssociationType associationType ) {
        this.associationType = associationType;
    }


    /**
     * @return the status
     */
    public DHCPLeaseStatus getStatus () {
        return this.status;
    }


    /**
     * @param status
     */
    public void setStatus ( DHCPLeaseStatus status ) {
        this.status = status;
    }


    /**
     * @return the options
     */
    public DHCPOptions getOptions () {
        return this.options;
    }


    /**
     * @param options
     */
    public void setOptions ( DHCPOptions options ) {
        this.options = options;
    }


    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.associationType == null ) ? 0 : this.associationType.hashCode() );
        result = prime * result + ( ( this.expiresTime == null ) ? 0 : this.expiresTime.hashCode() );
        result = prime * result + ( ( this.networkSpecification == null ) ? 0 : this.networkSpecification.hashCode() );
        result = prime * result + ( ( this.options == null ) ? 0 : this.options.hashCode() );
        result = prime * result + ( ( this.rebindTime == null ) ? 0 : this.rebindTime.hashCode() );
        result = prime * result + ( ( this.renewTime == null ) ? 0 : this.renewTime.hashCode() );
        result = prime * result + ( ( this.status == null ) ? 0 : this.status.hashCode() );
        return result;
    }


    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        LeaseEntry other = (LeaseEntry) obj;
        if ( this.associationType != other.associationType )
            return false;
        if ( this.expiresTime == null ) {
            if ( other.expiresTime != null )
                return false;
        }
        else if ( !this.expiresTime.equals(other.expiresTime) )
            return false;
        if ( this.networkSpecification == null ) {
            if ( other.networkSpecification != null )
                return false;
        }
        else if ( !this.networkSpecification.equals(other.networkSpecification) )
            return false;
        if ( this.options == null ) {
            if ( other.options != null )
                return false;
        }
        else if ( !this.options.equals(other.options) )
            return false;
        if ( this.rebindTime == null ) {
            if ( other.rebindTime != null )
                return false;
        }
        else if ( !this.rebindTime.equals(other.rebindTime) )
            return false;
        if ( this.renewTime == null ) {
            if ( other.renewTime != null )
                return false;
        }
        else if ( !this.renewTime.equals(other.renewTime) )
            return false;
        if ( this.status != other.status )
            return false;
        return true;
    }

}
