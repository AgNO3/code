/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2013 by mbechler
 */
package eu.agno3.runtime.logging.internal;


import java.nio.charset.Charset;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.spi.PaxAppender;
import org.ops4j.pax.logging.spi.PaxLayout;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import eu.agno3.runtime.logging.Appender;
import eu.agno3.runtime.logging.config.internal.LoggingConfigModule;


/**
 * @author mbechler
 * 
 */
public class LoggingActivator implements BundleActivator {

    private static final Logger log = Logger.getLogger(LoggingActivator.class);

    private static final String CONSOLE_ENABLE = "console.enable"; //$NON-NLS-1$
    private static final String CONSOLE_COLOR = "console.color"; //$NON-NLS-1$
    private static final String CONSOLE_STDERR = "console.stderr"; //$NON-NLS-1$
    private static final String CONSOLE_CHARSET = "console.charset"; //$NON-NLS-1$


    Logger getLog () {
        return LoggingActivator.log;
    }

    private LoggingConfigModule configModule;

    private AppenderBridge appenderBridge;


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start ( BundleContext context ) {

        Dictionary<String, String> appenderRegProperties = new Hashtable<>();
        appenderRegProperties.put(PaxLoggingService.APPENDER_NAME_PROPERTY, "consoleAppender"); //$NON-NLS-1$
        Dictionary<String, String> layoutRegProperties = new Hashtable<>();
        layoutRegProperties.put(PaxLoggingService.LAYOUT_NAME_PROPERTY, "consoleLayout"); //$NON-NLS-1$

        ConsoleLayout layout = new ConsoleLayout();
        ColoringConsoleAppender consoleAppender = createConsoleAppender(layout);
        if ( Boolean.parseBoolean(System.getProperty(CONSOLE_ENABLE, Boolean.FALSE.toString())) ) {
            context.registerService(Appender.class, consoleAppender, appenderRegProperties);
        }

        context.registerService(PaxLayout.class, layout, layoutRegProperties);
        this.appenderBridge = new AppenderBridge(context, consoleAppender);
        this.appenderBridge.start();
        Dictionary<String, String> appenderBridgeProperties = new Hashtable<>();
        appenderBridgeProperties.put(PaxLoggingService.APPENDER_NAME_PROPERTY, "appenderBridge"); //$NON-NLS-1$
        context.registerService(PaxAppender.class, this.appenderBridge, appenderBridgeProperties);

        this.configModule = new LoggingConfigModule(layout);
        this.configModule.start(context);

    }


    private static ColoringConsoleAppender createConsoleAppender ( ConsoleLayout layout ) {
        Charset cs = Charset.defaultCharset();
        String csSpec = System.getProperty(CONSOLE_CHARSET);

        if ( csSpec != null ) {
            cs = Charset.forName(csSpec);
        }

        boolean doColor = false;
        String colorSpec = System.getProperty(CONSOLE_COLOR);

        if ( colorSpec != null && Boolean.parseBoolean(colorSpec) ) {
            doColor = true;
        }

        boolean toStderr = true;
        String stderrSpec = System.getProperty(CONSOLE_STDERR);

        if ( stderrSpec != null && !Boolean.parseBoolean(stderrSpec) ) {
            toStderr = false;
        }

        return new ColoringConsoleAppender(layout, cs, doColor, toStderr);
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop ( BundleContext context ) {
        if ( this.configModule != null ) {
            this.configModule.stop(context);
            this.configModule = null;
        }

        if ( this.appenderBridge != null ) {
            this.appenderBridge.stop();
            this.appenderBridge = null;
        }
    }

}
