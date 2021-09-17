/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.06.2013 by mbechler
 */
package eu.agno3.runtime.logging.internal;


import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.ops4j.pax.logging.spi.PaxLayout;
import org.ops4j.pax.logging.spi.PaxLocationInfo;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;

import eu.agno3.runtime.logging.DynamicVerbosityLayout;
import eu.agno3.runtime.logging.TracingVerbosity;
import eu.agno3.runtime.logging.layouts.WithExceptionLayout;


/**
 * @author mbechler
 * 
 */
public class ConsoleLayout implements PaxLayout, DynamicVerbosityLayout {

    private static final String BUNDLE_VERSION_PROPERTY = "bundle.version"; //$NON-NLS-1$
    private static final String BUNDLE_NAME_PROPERTY = "bundle.name"; //$NON-NLS-1$
    private static final String BUNDLE_ID_PROPERTY = "bundle.id"; //$NON-NLS-1$

    private TracingVerbosity verbosity = TracingVerbosity.COMPACT;

    private PaxLayout delegate = new WithExceptionLayout();


    /**
     * {@inheritDoc}
     * 
     * @see org.ops4j.pax.logging.spi.PaxLayout#doLayout(org.ops4j.pax.logging.spi.PaxLoggingEvent)
     */
    @Override
    public String doLayout ( PaxLoggingEvent event ) {
        StringBuilder buf = new StringBuilder();

        String loggerName = getLoggerName(event);

        buf.append(String.format("<%d> [%s] %s - %s", //$NON-NLS-1$
            event.getLevel().getSyslogEquivalent(),
            event.getLevel().toString(),
            loggerName,
            this.delegate.doLayout(event)));

        if ( this.verbosity.equals(TracingVerbosity.EXTENDED) || this.verbosity.equals(TracingVerbosity.FULL) ) {
            doVerboseLayout(event, buf);
        }

        if ( event.getThrowableStrRep() != null ) {
            doThrowableLayout(event, buf);
        }

        return buf.toString();
    }


    /**
     * @param event
     * @param buf
     */
    private static void doThrowableLayout ( PaxLoggingEvent event, StringBuilder buf ) {
        buf.append(System.lineSeparator());
        for ( String throwableRep : event.getThrowableStrRep() ) {
            buf.append(throwableRep);
            buf.append(System.lineSeparator());
        }
    }


    /**
     * @param event
     * @param buf
     */
    private void doVerboseLayout ( PaxLoggingEvent event, StringBuilder buf ) {
        buf.append("("); //$NON-NLS-1$

        if ( this.verbosity.equals(TracingVerbosity.FULL) ) {
            buf.append(String.format(" TS: %d,", event.getTimeStamp())); //$NON-NLS-1$
        }
        buf.append(String.format(" Thread: %s,", event.getThreadName())); //$NON-NLS-1$

        if ( event.getProperties().containsKey(BUNDLE_ID_PROPERTY) ) {
            String bundleId = event.getProperties().get(BUNDLE_ID_PROPERTY).toString();
            String bundleName = event.getProperties().get(BUNDLE_NAME_PROPERTY).toString();
            String bundleVersion = event.getProperties().get(BUNDLE_VERSION_PROPERTY).toString();
            buf.append(String.format(" Bundle: %s-%s [%s],", bundleName, bundleVersion, bundleId)); //$NON-NLS-1$
        }

        if ( this.verbosity.equals(TracingVerbosity.FULL) ) {
            for ( Entry<Object, Object> e : (Set<Entry<Object, Object>>) event.getProperties().entrySet() ) {
                buf.append(String.format(" %s:%s,", e.getKey(), e.getValue())); //$NON-NLS-1$
            }
        }

        if ( this.verbosity.equals(TracingVerbosity.FULL) ) {
            if ( event.locationInformationExists() ) {
                PaxLocationInfo locInfo = event.getLocationInformation();
                buf.append(String.format(" %s:%s:%s", locInfo.getClassName(), locInfo.getMethodName(), locInfo.getLineNumber())); //$NON-NLS-1$
            }
            else {
                buf.append(" No location info"); //$NON-NLS-1$
            }
        }
        buf.append(" )"); //$NON-NLS-1$
    }


    /**
     * @param event
     * @return
     */
    private static String getLoggerName ( PaxLoggingEvent event ) {
        String loggerName = event.getLoggerName();

        String[] components = loggerName.split(Pattern.quote(".")); //$NON-NLS-1$

        // shorten package names
        if ( components.length >= 4 ) {

            for ( int i = 0; i < components.length - 2; i++ ) {
                components[ i ] = components[ i ].substring(0, 1);
            }

            StringBuilder loggerBuf = new StringBuilder();
            boolean first = true;

            for ( String component : components ) {
                if ( !first ) {
                    loggerBuf.append("."); //$NON-NLS-1$
                }
                first = false;

                loggerBuf.append(component);
            }

            loggerName = loggerBuf.toString();
        }
        return loggerName;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.ops4j.pax.logging.spi.PaxLayout#getContentType()
     */
    @Override
    public String getContentType () {
        return "text/plain"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.ops4j.pax.logging.spi.PaxLayout#getFooter()
     */
    @Override
    public String getFooter () {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.ops4j.pax.logging.spi.PaxLayout#getHeader()
     */
    @Override
    public String getHeader () {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.logging.DynamicVerbosityLayout#getVerbosity()
     */
    @Override
    public TracingVerbosity getVerbosity () {
        return this.verbosity;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.logging.DynamicVerbosityLayout#setVerbosity(eu.agno3.runtime.logging.TracingVerbosity)
     */
    @Override
    public void setVerbosity ( TracingVerbosity verbosity ) {
        this.verbosity = verbosity;
    }

}
