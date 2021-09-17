/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.datetime;


import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.OrderColumn;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;
import org.joda.time.DateTimeZone;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.types.net.name.HostOrAddress;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@MapAs ( DateTimeConfiguration.class )
@Entity
@Table ( name = "config_hostconfig_datetime" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "hc_dt" )
public class DateTimeConfigurationImpl extends AbstractConfigurationObject<DateTimeConfiguration> implements DateTimeConfiguration,
        DateTimeConfigurationMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -8397913995117289103L;
    private Boolean hwClockUTC;
    private Boolean ntpEnabled;
    private DateTimeZone timezone;

    private List<HostOrAddress> ntpServers = new ArrayList<>();


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<DateTimeConfiguration> getType () {
        return DateTimeConfiguration.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.datetime.DateTimeConfiguration#getHwClockUTC()
     */
    @Override
    @Column ( nullable = true )
    @Basic
    public Boolean getHwClockUTC () {
        return this.hwClockUTC;
    }


    /**
     * @param hwClockUTC
     *            the hwClockUTC to set
     */
    @Override
    public void setHwClockUTC ( Boolean hwClockUTC ) {
        this.hwClockUTC = hwClockUTC;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.datetime.DateTimeConfiguration#getTimezone()
     */
    @Override
    @Column ( nullable = true )
    public DateTimeZone getTimezone () {
        return this.timezone;
    }


    /**
     * @param timezone
     *            the timezone to set
     */
    @Override
    public void setTimezone ( DateTimeZone timezone ) {
        this.timezone = timezone;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.datetime.DateTimeConfiguration#getNtpEnabled()
     */
    @Override
    @Column ( nullable = true )
    @Basic
    public Boolean getNtpEnabled () {
        return this.ntpEnabled;
    }


    /**
     * @param ntpEnabled
     *            the ntpEnabled to set
     */
    @Override
    public void setNtpEnabled ( Boolean ntpEnabled ) {
        this.ntpEnabled = ntpEnabled;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.datetime.DateTimeConfiguration#getNtpServers()
     */
    @Override
    @Column ( name = "server", nullable = false )
    @ElementCollection
    @CollectionTable ( name = "config_hostconfig_datetime_ntpservers" )
    @OrderColumn ( name = "idx" )
    public List<HostOrAddress> getNtpServers () {
        return this.ntpServers;
    }


    /**
     * @param ntpServers
     *            the ntpServers to set
     */
    @Override
    public void setNtpServers ( List<HostOrAddress> ntpServers ) {
        this.ntpServers = ntpServers;
    }

}
