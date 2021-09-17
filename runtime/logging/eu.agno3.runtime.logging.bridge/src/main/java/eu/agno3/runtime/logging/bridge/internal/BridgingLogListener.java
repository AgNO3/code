/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2014 by mbechler
 */
package eu.agno3.runtime.logging.bridge.internal;


import org.eclipse.equinox.log.ExtendedLogEntry;
import org.ops4j.pax.logging.PaxLogger;
import org.ops4j.pax.logging.PaxLoggingService;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogService;


/**
 * @author mbechler
 * 
 */
class BridgingLogListener implements LogListener {

    /**
     * 
     */
    private final LoggerBridge loggerBridge;
    private static final String BUNDLE_EVENT = "BundleEvent"; //$NON-NLS-1$
    private static final String SERVICE_EVENT = "ServiceEvent"; //$NON-NLS-1$


    /**
     * @param loggerBridge
     * 
     */
    public BridgingLogListener ( LoggerBridge loggerBridge ) {
        this.loggerBridge = loggerBridge;
    }


    @Override
    public void logged ( LogEntry entry ) {

        ExtendedLogEntry e = (ExtendedLogEntry) entry;

        if ( e.getLevel() == LogService.LOG_INFO && isServiceOrBundleEvent(e) ) {
            return;
        }

        PaxLogger log = fetchLogger(e);

        if ( log == null ) {
            return;
        }

        deliver(e, log);
    }


    private static boolean isServiceOrBundleEvent ( ExtendedLogEntry e ) {
        return e.getMessage().startsWith(SERVICE_EVENT) || e.getMessage().startsWith(BUNDLE_EVENT);
    }


    protected void deliver ( ExtendedLogEntry e, PaxLogger log ) {
        String message = this.loggerBridge.formatExtendedLogEntry(e);
        Throwable exception = e.getException();
        int level = e.getLevel();
        deliver(log, message, exception, level);
    }


    private static void deliver ( PaxLogger log, String message, Throwable exception, int level ) {
        switch ( level ) {
        case LogService.LOG_DEBUG:
            log.debug(message, exception);
            break;
        case LogService.LOG_INFO:
            log.inform(message, exception);
            break;
        case LogService.LOG_WARNING:
            log.warn(message, exception);
            break;
        case LogService.LOG_ERROR:
            log.error(message, exception);
            break;
        default:
            log.fatal(message, exception);
            break;
        }
    }


    private PaxLogger fetchLogger ( ExtendedLogEntry e ) {
        String logger = e.getLoggerName();

        if ( logger == null ) {
            logger = e.getBundle().getSymbolicName();
        }

        PaxLoggingService backend = this.loggerBridge.getBackend();

        if ( backend == null ) {
            return null;
        }

        return backend.getLogger(e.getBundle(), logger, null);
    }
}