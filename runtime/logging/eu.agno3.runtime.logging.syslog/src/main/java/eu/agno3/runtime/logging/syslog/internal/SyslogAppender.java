/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.08.2014 by mbechler
 */
package eu.agno3.runtime.logging.syslog.internal;


import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.graylog2.syslog4j.Syslog;
import org.graylog2.syslog4j.SyslogIF;
import org.ops4j.pax.logging.spi.PaxLayout;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import eu.agno3.runtime.logging.Appender;


/**
 * @author mbechler
 * 
 */
@Component ( service = Appender.class, configurationPid = SyslogConfiguration.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class SyslogAppender implements Appender {

    /**
     * 
     */
    private static final String LOG_NAME = "syslog"; //$NON-NLS-1$
    private PaxLayout layout;
    private SyslogIF syslog;

    private boolean dumpExceptions = false;
    private int minLevel;


    @Activate
    protected void activate ( ComponentContext ctx ) {
        this.syslog = Syslog.createInstance(LOG_NAME, UnixSyslogConfiguration.createConfig(ctx.getProperties()));

        String dumpExceptionsSpec = (String) ctx.getProperties().get("dumpExceptions"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(dumpExceptionsSpec) && Boolean.parseBoolean(dumpExceptionsSpec) ) {
            this.dumpExceptions = true;
        }

        String minLevelSpec = (String) ctx.getProperties().get("minLevel"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(minLevelSpec) ) {
            Level l = Level.toLevel(minLevelSpec, Level.ALL);
            this.minLevel = l.toInt();
        }

    }


    @Deactivate
    protected void deactivate ( ComponentContext ctx ) {
        Syslog.destroyInstance(this.syslog);
    }


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, target = "(appender=syslog)" )
    protected synchronized void setLayout ( PaxLayout l ) {
        this.layout = l;
    }


    protected synchronized void unsetLayout ( PaxLayout l ) {
        if ( this.layout == l ) {
            this.layout = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.ops4j.pax.logging.spi.PaxAppender#doAppend(org.ops4j.pax.logging.spi.PaxLoggingEvent)
     */
    @Override
    public void doAppend ( PaxLoggingEvent ev ) {

        if ( ev.getLevel().toInt() < this.minLevel ) {
            return;
        }

        int level = ev.getLevel().getSyslogEquivalent();
        if ( this.layout != null ) {
            String message = this.layout.doLayout(ev);
            this.syslog.log(level, message);
        }
        else {
            String message = ev.getRenderedMessage();
            this.syslog.log(level, message);
            if ( this.dumpExceptions ) {
                for ( String line : ev.getThrowableStrRep() ) {
                    this.syslog.log(level, line);
                }
            }
        }
    }
}
