/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.07.2013 by mbechler
 */
package eu.agno3.runtime.http.service.handler;


import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import eu.agno3.runtime.http.service.HttpConfigurationException;


/**
 * @author mbechler
 * 
 */
public class BaseContextHandler extends org.eclipse.jetty.server.handler.ContextHandler implements ExtendedHandler {

    private static final Logger log = Logger.getLogger(BaseContextHandler.class);
    private ErrorHandler errorHandler;
    private String contextName;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.http.service.handler.ExtendedHandler#getPriority()
     */
    @Override
    public float getPriority () {
        return 0f;
    }


    @Activate
    @Modified
    protected synchronized void activate ( ComponentContext context ) {

        try {
            this.contextName = (String) context.getProperties().get(ContextHandlerConfig.CONTEXT_NAME_ATTR);

            if ( this.contextName == null ) {
                throw new HttpConfigurationException("ContextHandler has no name set"); //$NON-NLS-1$
            }

            configureContextHandler(this, context.getProperties(), this.getActualClassloader());
        }
        catch ( HttpConfigurationException e ) {
            log.error("Failed to configure ContextHandler:", e); //$NON-NLS-1$
        }

    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext context ) {
        try {
            this.stop();
        }
        catch ( Exception e ) {
            log.warn("Error while stopping ContextHandler", e); //$NON-NLS-1$
        }
    }


    @Reference ( policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY )
    protected synchronized void bindErrorHandler ( ErrorHandler handler ) {
        this.errorHandler = handler;
        this.setErrorHandler(this.errorHandler);
    }


    protected synchronized void unbindErrorHandler ( ErrorHandler handler ) {
        if ( this.errorHandler == handler ) {
            this.errorHandler = null;
            this.setErrorHandler(null);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.http.service.handler.ExtendedHandler#getContextName()
     */
    @Override
    public String getContextName () {
        return this.contextName;
    }


    /**
     * Configure a ContextHandler using service properties.
     * 
     * @param handler
     * @param props
     * @param cl
     * @throws HttpConfigurationException
     */
    public static void configureContextHandler ( ContextHandler handler, Dictionary<String, Object> props, ClassLoader cl )
            throws HttpConfigurationException {

        String contextPath = (String) props.get(ContextHandlerConfig.CONTEXT_PATH_ATTR);
        if ( contextPath != null ) {
            handler.setContextPath(contextPath);
        }

        String virtualHostsSpec = (String) props.get(ContextHandlerConfig.VIRTUAL_HOSTS_ATTR);
        if ( virtualHostsSpec != null ) {
            handler.setVirtualHosts(virtualHostsSpec.split(Pattern.quote(","))); //$NON-NLS-1$
        }

        String displayName = (String) props.get(ContextHandlerConfig.DISPLAY_NAME_ATTR);
        if ( displayName != null ) {
            handler.setDisplayName(displayName);
        }

        configurePathHandling(handler, props);
        configureFormHandling(handler, props);
        configureResources(handler, props);
        configureMimeTypes(handler, props);
        configureLocales(handler, props);

        Enumeration<String> keys = props.keys();

        while ( keys.hasMoreElements() ) {
            String key = keys.nextElement();
            handler.setInitParameter(key, props.get(key).toString());
        }

        handler.setLogger(new JettyLoggerBridge(Logger.getLogger(handler.getClass())));
        handler.setClassLoader(cl);
    }


    /**
     * @param handler
     * @param props
     */
    private static void configurePathHandling ( ContextHandler handler, Dictionary<String, Object> props ) {
        if ( props.get(ContextHandlerConfig.ALLOW_NULL_PATHINFO_ATTR) != null ) {
            if ( props.get(ContextHandlerConfig.ALLOW_NULL_PATHINFO_ATTR).equals(Boolean.TRUE.toString()) ) {
                handler.setAllowNullPathInfo(true);
            }
            else {
                handler.setAllowNullPathInfo(false);
            }
        }

        if ( props.get(ContextHandlerConfig.COMPACT_PATH_ATTR) != null ) {
            if ( props.get(ContextHandlerConfig.COMPACT_PATH_ATTR).equals(Boolean.TRUE.toString()) ) {
                handler.setCompactPath(true);
            }
            else {
                handler.setCompactPath(false);
            }
        }
    }


    /**
     * @param handler
     * @param props
     */
    private static void configureFormHandling ( ContextHandler handler, Dictionary<String, Object> props ) {
        String maxFormContentSize = (String) props.get(ContextHandlerConfig.MAX_FORM_CONTENT_SIZE_ATTR);
        if ( maxFormContentSize != null ) {
            handler.setMaxFormContentSize(Integer.parseInt(maxFormContentSize));
        }

        String maxFormKeys = (String) props.get(ContextHandlerConfig.MAX_FORM_KEYS_ATTR);
        if ( maxFormKeys != null ) {
            handler.setMaxFormKeys(Integer.parseInt(maxFormKeys));
        }
    }


    /**
     * @param handler
     * @param props
     */
    private static void configureResources ( ContextHandler handler, Dictionary<String, Object> props ) {
        String resourceBase = (String) props.get(ContextHandlerConfig.RESOURCE_BASE_ATTR);
        if ( resourceBase != null ) {
            handler.setResourceBase(resourceBase);
        }

        String welcomeFilesSpec = (String) props.get(ContextHandlerConfig.WELCOME_FILES_ATTR);
        if ( welcomeFilesSpec != null ) {
            handler.setWelcomeFiles(welcomeFilesSpec.split(Pattern.quote(","))); //$NON-NLS-1$
        }

        String protectedTargetsSpec = (String) props.get(ContextHandlerConfig.PROTECTED_TARGETS_ATTR);
        if ( protectedTargetsSpec != null ) {
            handler.setProtectedTargets(protectedTargetsSpec.split(Pattern.quote(","))); //$NON-NLS-1$
        }
    }


    /**
     * @param handler
     * @param props
     * @throws HttpConfigurationException
     */
    private static void configureLocales ( ContextHandler handler, Dictionary<String, Object> props ) throws HttpConfigurationException {
        String localesSpecs = (String) props.get(ContextHandlerConfig.LOCALE_ENCODING_ATTR);
        if ( localesSpecs != null ) {
            for ( String localeEncodingSpec : localesSpecs.split(Pattern.quote(",")) ) { //$NON-NLS-1$
                String[] parts = localeEncodingSpec.split(Pattern.quote("="), 2); //$NON-NLS-1$
                if ( parts.length != 2 ) {
                    throw new HttpConfigurationException("Illegal localeEncoding value for " + ContextHandlerConfig.LOCALE_ENCODING_ATTR); //$NON-NLS-1$
                }

                handler.addLocaleEncoding(parts[ 0 ], parts[ 1 ]);
            }

        }
    }


    /**
     * @param handler
     * @param props
     * @throws HttpConfigurationException
     */
    private static void configureMimeTypes ( ContextHandler handler, Dictionary<String, Object> props ) throws HttpConfigurationException {
        String mimeTypesSpecs = (String) props.get(ContextHandlerConfig.MIME_TYPES_ATTR);
        if ( mimeTypesSpecs != null ) {
            MimeTypes types = new MimeTypes();
            for ( String mimeTypeSpec : mimeTypesSpecs.split(Pattern.quote(",")) ) { //$NON-NLS-1$
                String[] parts = mimeTypeSpec.split(Pattern.quote("="), 2); //$NON-NLS-1$
                if ( parts.length != 2 ) {
                    throw new HttpConfigurationException("Illegal mimeType value for " + ContextHandlerConfig.MIME_TYPES_ATTR); //$NON-NLS-1$
                }

                types.addMimeMapping(parts[ 0 ], parts[ 1 ]);
            }

            handler.setMimeTypes(types);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.jetty.server.handler.ContextHandler#doHandle(java.lang.String, org.eclipse.jetty.server.Request,
     *      javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void doHandle ( String target, Request req, HttpServletRequest httpReq, HttpServletResponse httpResp )
            throws IOException, ServletException {
        httpResp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }


    protected ClassLoader getActualClassloader () {
        return this.getClass().getClassLoader();
    }

}
