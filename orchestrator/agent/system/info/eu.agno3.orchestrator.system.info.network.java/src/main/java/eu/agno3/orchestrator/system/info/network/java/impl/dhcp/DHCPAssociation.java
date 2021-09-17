/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2016 by mbechler
 */
package eu.agno3.orchestrator.system.info.network.java.impl.dhcp;


import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import eu.agno3.orchestrator.system.info.network.DHCPAssociationType;
import eu.agno3.orchestrator.system.info.network.DHCPOptions;


/**
 * @author mbechler
 *
 */
public class DHCPAssociation {

    private DHCPAssociationType associationType;
    private List<DHCPAddressEntry> addresses = new ArrayList<>();
    private DHCPOptions options = new DHCPOptions();

    private DateTime startTime;
    private DateTime renewTime;
    private DateTime rebindTime;
    private DateTime expireTime;

    private Duration renewInterval;
    private Duration rebindInterval;


    /**
     * @return the associationType
     */
    public DHCPAssociationType getAssociationType () {
        return this.associationType;
    }


    /**
     * @param associationType
     *            the associationType to set
     */
    public void setAssociationType ( DHCPAssociationType associationType ) {
        this.associationType = associationType;
    }


    /**
     * @return the addresses
     */
    public List<DHCPAddressEntry> getAddresses () {
        return this.addresses;
    }


    /**
     * @param addresses
     *            the addresses to set
     */
    public void setAddresses ( List<DHCPAddressEntry> addresses ) {
        this.addresses = addresses;
    }


    /**
     * @return the options
     */
    public DHCPOptions getOptions () {
        return this.options;
    }


    /**
     * @param options
     *            the options to set
     */
    public void setOptions ( DHCPOptions options ) {
        this.options = options;
    }


    /**
     * @return the startTime
     */
    public DateTime getStartTime () {
        return this.startTime;
    }


    /**
     * @param startTime
     *            the startTime to set
     */
    public void setStartTime ( DateTime startTime ) {
        this.startTime = startTime;
    }


    /**
     * @return the renewTime
     */
    public DateTime getRenewTime () {
        if ( this.renewTime == null && this.startTime != null && this.renewInterval != null ) {
            return this.startTime.plus(this.renewInterval);
        }
        return this.renewTime;
    }


    /**
     * @param renewTime
     *            the renewTime to set
     */
    public void setRenewTime ( DateTime renewTime ) {
        this.renewTime = renewTime;
    }


    /**
     * @return the rebindTime
     */
    public DateTime getRebindTime () {
        if ( this.rebindTime == null && this.startTime != null && this.rebindInterval != null ) {
            return this.startTime.plus(this.rebindInterval);
        }
        return this.rebindTime;
    }


    /**
     * @param rebindTime
     *            the rebindTime to set
     */
    public void setRebindTime ( DateTime rebindTime ) {
        this.rebindTime = rebindTime;
    }


    /**
     * @return the expireTime
     */
    public DateTime getExpireTime () {
        return this.expireTime;
    }


    /**
     * @param expireTime
     *            the expireTime to set
     */
    public void setExpireTime ( DateTime expireTime ) {
        this.expireTime = expireTime;
    }


    /**
     * @return the renewInterval
     */
    public Duration getRenewInterval () {
        return this.renewInterval;
    }


    /**
     * @param renewInterval
     */
    public void setRenewInterval ( Duration renewInterval ) {
        this.renewInterval = renewInterval;
    }


    /**
     * @return the rebindInterval
     */
    public Duration getRebindInterval () {
        return this.rebindInterval;
    }


    /**
     * @param rebindInterval
     */
    public void setRebindInterval ( Duration rebindInterval ) {
        this.rebindInterval = rebindInterval;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format(
            "%s[addrs=%s,opts=%s,start=%s,renewTime=%s,renewInt=%s,rebind=%s,rebindInt=%s,expire=%s]", //$NON-NLS-1$
            this.associationType,
            this.addresses,
            this.options,
            this.startTime,
            this.getRebindTime(),
            this.rebindInterval,
            this.getRenewTime(),
            this.renewInterval,
            this.expireTime);
    }
}
