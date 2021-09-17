/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.datetime;


import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;
import org.joda.time.DateTimeZone;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.config.hostconfig.i18n.HostConfigurationMessages;
import eu.agno3.orchestrator.config.model.descriptors.AbstractObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;
import eu.agno3.orchestrator.types.net.name.HostOrAddress;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeDescriptor.class )
public class DateTimeConfigurationObjectTypeDescriptor extends AbstractObjectTypeDescriptor<DateTimeConfiguration, DateTimeConfigurationImpl> {

    /**
     * 
     */
    public DateTimeConfigurationObjectTypeDescriptor () {
        super(
            DateTimeConfiguration.class,
            DateTimeConfigurationImpl.class,
            HostConfigurationMessages.BASE_PACKAGE,
            "urn:agno3:objects:1.0:hostconfig"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull DateTimeConfiguration getGlobalDefaults () {
        DateTimeConfigurationMutable defaults = emptyInstance();
        defaults.setHwClockUTC(true);
        defaults.setTimezone(DateTimeZone.UTC);
        defaults.setNtpEnabled(false);
        defaults.setNtpServers(Arrays.asList(
            new HostOrAddress("0.agno3.pool.ntp.org"), //$NON-NLS-1$
            new HostOrAddress("1.agno3.pool.ntp.org"), //$NON-NLS-1$
            new HostOrAddress("2.agno3.pool.ntp.org"), //$NON-NLS-1$
            new HostOrAddress("3.agno3.pool.ntp.org"))); //$NON-NLS-1$
        return defaults;
    }


    /**
     * @return empty instance
     */
    public static DateTimeConfigurationMutable emptyInstance () {
        return new DateTimeConfigurationImpl();
    }

}
