/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2013 by mbechler
 */
package eu.agno3.runtime.logging.bridge.internal;


import org.eclipse.equinox.log.ExtendedLogEntry;
import org.eclipse.equinox.log.ExtendedLogReaderService;
import org.ops4j.pax.logging.PaxLoggingService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import eu.agno3.runtime.logging.LogConfigurationService;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    LoggerBridge.class
}, immediate = true )
public class LoggerBridge {

    private ExtendedLogReaderService extendedLogReader;
    private PaxLoggingService backend;

    private LogConfigurationService logConfigService;


    @Reference
    protected synchronized void setExtendedLogReaderService ( ExtendedLogReaderService service ) {
        this.extendedLogReader = service;
    }


    protected synchronized void unsetExtendedLogReaderService ( ExtendedLogReaderService service ) {
        if ( this.extendedLogReader == service ) {
            this.extendedLogReader = null;
        }
    }


    @Reference ( policyOption = ReferencePolicyOption.GREEDY )
    protected synchronized void setPaxLoggingService ( PaxLoggingService backing ) {
        this.backend = backing;
    }


    protected synchronized void unsetPaxLoggingService ( PaxLoggingService backing ) {
        if ( this.backend == backing ) {
            this.backend = null;
        }
    }


    @Reference
    protected synchronized void setLogConfigService ( LogConfigurationService logConfig ) {
        this.logConfigService = logConfig;
    }


    protected synchronized void unsetLogConfigService ( LogConfigurationService logConfig ) {
        if ( this.logConfigService == logConfig ) {
            this.logConfigService = null;
        }
    }


    /**
     * @return the backend
     */
    synchronized PaxLoggingService getBackend () {
        return this.backend;
    }


    /**
     * @return the logConfigService
     */
    synchronized LogConfigurationService getLogConfigService () {
        return this.logConfigService;
    }


    String formatExtendedLogEntry ( ExtendedLogEntry e ) {
        StringBuilder formatted = new StringBuilder();

        formatted.append(e.getMessage());
        // TODO: add more information

        return formatted.toString();
    }


    @Activate
    protected void activate ( ComponentContext context ) {
        this.extendedLogReader.addLogListener(new BridgingLogListener(this));
    }
}
