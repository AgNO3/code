/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.07.2013 by mbechler
 */
package eu.agno3.runtime.http.service.handler;


import java.nio.channels.AsynchronousCloseException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.runtime.util.log.LogFormatter;


/**
 * @author mbechler
 * 
 */
public class JettyLoggerBridge implements org.eclipse.jetty.util.log.Logger {

    private static final String FORMAT_PLACEHOLDER = "%s"; //$NON-NLS-1$
    private static final String JETTY_PLACEHOLDER = "{}"; //$NON-NLS-1$

    /**
     * 
     */
    private static final String EXCEPTION = "Exception:"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(JettyLoggerBridge.class);

    private Logger backend;


    /**
     * @param backend
     * 
     */
    public JettyLoggerBridge ( Logger backend ) {
        this.backend = backend;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.jetty.util.log.Logger#debug(java.lang.Throwable)
     */
    @Override
    public void debug ( Throwable t ) {

        if ( t instanceof AsynchronousCloseException ) {
            return;
        }

        this.backend.debug("Exception in ContextHandler", t); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.jetty.util.log.Logger#debug(java.lang.String, java.lang.Object[])
     */
    @Override
    public void debug ( String arg0, Object... arg1 ) {
        if ( this.backend.isDebugEnabled() ) {
            this.backend.debug(formatPlaceholders(arg0, arg1));
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.jetty.util.log.Logger#debug(java.lang.String, long)
     */
    @Override
    public void debug ( String arg0, long arg1 ) {
        if ( this.backend.isDebugEnabled() ) {
            this.debug(arg0, (Object) arg1);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.jetty.util.log.Logger#debug(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void debug ( String arg0, Throwable arg1 ) {
        this.backend.debug(formatPlaceholders(arg0, new Object[] {
            arg1.getMessage()
        }), arg1);
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.jetty.util.log.Logger#getLogger(java.lang.String)
     */
    @Override
    public org.eclipse.jetty.util.log.Logger getLogger ( String arg0 ) {
        return new JettyLoggerBridge(Logger.getLogger(arg0));
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.jetty.util.log.Logger#getName()
     */
    @Override
    public String getName () {
        return this.backend.getName();
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.jetty.util.log.Logger#ignore(java.lang.Throwable)
     */
    @Override
    public void ignore ( Throwable arg0 ) {
        this.backend.trace("Ignored exception", arg0); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.jetty.util.log.Logger#info(java.lang.Throwable)
     */
    @Override
    public void info ( Throwable arg0 ) {
        this.backend.info(EXCEPTION, arg0);
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.jetty.util.log.Logger#info(java.lang.String, java.lang.Object[])
     */
    @Override
    public void info ( String arg0, Object... arg1 ) {
        if ( this.backend.isInfoEnabled() ) {
            this.backend.info(formatPlaceholders(arg0, arg1));
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.jetty.util.log.Logger#info(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void info ( String arg0, Throwable arg1 ) {
        this.backend.info(formatPlaceholders(arg0, new Object[] {
            arg1.getMessage()
        }), arg1);
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.jetty.util.log.Logger#isDebugEnabled()
     */
    @Override
    public boolean isDebugEnabled () {
        return this.backend.isDebugEnabled();
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.jetty.util.log.Logger#setDebugEnabled(boolean)
     */
    @Override
    public void setDebugEnabled ( boolean arg0 ) {
        this.backend.warn("Trying to enable debugging programatically"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.jetty.util.log.Logger#warn(java.lang.Throwable)
     */
    @Override
    public void warn ( Throwable arg0 ) {
        this.backend.warn(EXCEPTION, arg0);
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.jetty.util.log.Logger#warn(java.lang.String, java.lang.Object[])
     */
    @Override
    public void warn ( String arg0, Object... arg1 ) {
        this.backend.warn(formatPlaceholders(arg0, arg1));
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.jetty.util.log.Logger#warn(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void warn ( String arg0, Throwable arg1 ) {
        this.backend.warn(formatPlaceholders(arg0, new Object[] {
            arg1.getMessage()
        }), arg1);
    }


    private static String formatPlaceholders ( String fmt, Object[] data ) {

        if ( data == null || data.length == 0 ) {
            return fmt;
        }

        String realFmt = fmt;
        for ( int i = 0; i < data.length; i++ ) {
            realFmt = StringUtils.replaceOnce(realFmt, JETTY_PLACEHOLDER, FORMAT_PLACEHOLDER);
        }

        try {
            return String.format(realFmt, data);
        }
        catch ( IllegalArgumentException e ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Format string is " + fmt); //$NON-NLS-1$
                log.debug("Data is " + LogFormatter.format(data)); //$NON-NLS-1$
                log.debug("Failed to format jetty log entry:", e); //$NON-NLS-1$
            }
            return fmt;
        }
    }
}
