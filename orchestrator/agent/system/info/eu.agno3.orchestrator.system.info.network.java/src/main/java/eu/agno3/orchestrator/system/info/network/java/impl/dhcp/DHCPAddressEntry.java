/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2016 by mbechler
 */
package eu.agno3.orchestrator.system.info.network.java.impl.dhcp;


import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import eu.agno3.orchestrator.system.info.network.DHCPOptions;
import eu.agno3.orchestrator.types.net.NetworkAddress;


/**
 * @author mbechler
 *
 */
public class DHCPAddressEntry implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3231461432280530116L;

    private NetworkAddress networkAddress;
    private int prefixLength = -1;
    private DateTime startTime;
    private DateTime expiresTime;
    private Duration preferredLife;
    private Duration maxLife;
    private DHCPOptions options;


    /**
     * 
     */
    public DHCPAddressEntry () {}


    /**
     * @param na
     */
    public DHCPAddressEntry ( NetworkAddress na ) {
        this.networkAddress = na;
    }


    /**
     * @param networkAddress
     * @param prefixLength
     * @param startTime
     * @param preferredLife
     * @param maxLife
     */
    public DHCPAddressEntry ( NetworkAddress networkAddress, int prefixLength, DateTime startTime, Duration preferredLife, Duration maxLife ) {
        this.networkAddress = networkAddress;
        this.prefixLength = prefixLength;
        this.startTime = startTime;
        this.preferredLife = preferredLife;
        this.maxLife = maxLife;
    }


    /**
     * @return the networkAddress
     */
    public NetworkAddress getNetworkAddress () {
        return this.networkAddress;
    }


    /**
     * @param networkAddress
     *            the networkAddress to set
     */
    public void setNetworkAddress ( NetworkAddress networkAddress ) {
        this.networkAddress = networkAddress;
    }


    /**
     * @return the prefixLength
     */
    public int getPrefixLength () {
        return this.prefixLength;
    }


    /**
     * @param prefixLength
     *            the prefixLength to set
     */
    public void setPrefixLength ( int prefixLength ) {
        this.prefixLength = prefixLength;
    }


    /**
     * @return the starts
     */
    public DateTime getStartTime () {
        return this.startTime;
    }


    /**
     * @param starts
     *            the starts to set
     */
    public void setStartTime ( DateTime starts ) {
        this.startTime = starts;
    }


    /**
     * @return the maxLife
     */
    public Duration getMaxLife () {
        return this.maxLife;
    }


    /**
     * @param maxLife
     *            the maxLife to set
     */
    public void setMaxLife ( Duration maxLife ) {
        this.maxLife = maxLife;
    }


    /**
     * @return the preferredLife
     */
    public Duration getPreferredLife () {
        return this.preferredLife;
    }


    /**
     * @param preferredLife
     *            the preferredLife to set
     */
    public void setPreferredLife ( Duration preferredLife ) {
        this.preferredLife = preferredLife;
    }


    /**
     * @return the expiresTime
     */
    public DateTime getExpiresTime () {
        if ( this.expiresTime == null && this.startTime != null && this.maxLife != null ) {
            return this.startTime.plus(this.maxLife);
        }
        return this.expiresTime;
    }


    /**
     * @param expiresTime
     *            the expiresTime to set
     */
    public void setExpiresTime ( DateTime expiresTime ) {
        this.expiresTime = expiresTime;
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
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format(
            "%s%s[start=%s,pref-life=%s,max-life=%s,expires=%s,opts=%s]", //$NON-NLS-1$
            this.networkAddress,
            this.prefixLength >= 0 ? ( "/" + this.prefixLength ) : StringUtils.EMPTY, //$NON-NLS-1$
            this.startTime,
            this.preferredLife,
            this.maxLife,
            this.getExpiresTime(),
            this.options);
    }
}
