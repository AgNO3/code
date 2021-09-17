/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.05.2014 by mbechler
 */
package eu.agno3.runtime.jsf.config;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.context.ExternalContext;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.apache.myfaces.config.ConfigFilesXmlValidationUtils;
import org.apache.myfaces.config.FacesConfigUnmarshaller;
import org.apache.myfaces.config.element.FacesConfig;
import org.apache.myfaces.config.element.facelets.FaceletTagLibrary;
import org.apache.myfaces.config.impl.digester.DigesterFacesConfigUnmarshallerImpl;
import org.apache.myfaces.config.impl.digester.elements.FacesConfigImpl;
import org.apache.myfaces.shared.config.MyfacesConfig;
import org.apache.myfaces.spi.FacesConfigurationProvider;
import org.apache.myfaces.spi.FacesConfigurationProviderWrapper;
import org.apache.myfaces.view.facelets.compiler.TagLibraryConfigUnmarshallerImpl;
import org.osgi.framework.Bundle;
import org.xml.sax.SAXException;

import eu.agno3.runtime.util.osgi.BundleUtil;


/**
 * @author mbechler
 * 
 */
public class OSGIConfigurationProvider extends FacesConfigurationProviderWrapper {

    /**
     * 
     */
    private static final String TAGLIB_XML_PATTERN = "*.taglib.xml"; //$NON-NLS-1$
    private static final String FACES_CONFIG_XML = "faces-config.xml"; //$NON-NLS-1$
    private static final String META_INF = "/META-INF"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(OSGIConfigurationProvider.class);

    private FacesConfigurationProvider wrapped;

    private DigesterFacesConfigUnmarshallerImpl unmarshaller;


    /**
     * @param wrapped
     */
    public OSGIConfigurationProvider ( FacesConfigurationProvider wrapped ) {
        this.wrapped = wrapped;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.faces.FacesWrapper#getWrapped()
     */
    @Override
    public FacesConfigurationProvider getWrapped () {
        return this.wrapped;
    }


    private static Bundle getContextBundle ( ExternalContext ectx ) {
        return (Bundle) ( (ServletContext) ectx.getContext() ).getAttribute("context.bundle"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.apache.myfaces.spi.FacesConfigurationProviderWrapper#getClassloaderFacesConfig(javax.faces.context.ExternalContext)
     */
    @Override
    public List<FacesConfig> getClassloaderFacesConfig ( ExternalContext ectx ) {
        List<FacesConfig> cfgs = new ArrayList<>();

        Bundle ctxBundle = getContextBundle(ectx);

        Enumeration<URL> findEntries = ctxBundle.findEntries(META_INF, FACES_CONFIG_XML, false);
        Set<URL> inBundle = new HashSet<>();
        while ( findEntries != null && findEntries.hasMoreElements() ) {
            inBundle.add(findEntries.nextElement());
        }
        addFacesConfigs(ectx, cfgs, ctxBundle, inBundle);
        addTaglibs(ectx, cfgs, ctxBundle);

        for ( Bundle reqBundle : BundleUtil.getRequiredBundles(ctxBundle) ) {

            Enumeration<URL> facesConfigs = reqBundle.findEntries(META_INF, FACES_CONFIG_XML, false);
            if ( facesConfigs == null || !facesConfigs.hasMoreElements() ) {
                continue;
            }

            Set<URL> facesCfg = new HashSet<>();
            while ( facesConfigs.hasMoreElements() ) {
                URL cfgElement = facesConfigs.nextElement();
                if ( inBundle.contains(cfgElement) ) {
                    continue;
                }
                facesCfg.add(cfgElement);
            }

            addFacesConfigs(ectx, cfgs, reqBundle, facesCfg);
            addTaglibs(ectx, cfgs, reqBundle);

        }

        return cfgs;

    }


    private static void addTaglibs ( ExternalContext ectx, List<FacesConfig> cfgs, Bundle reqBundle ) {
        Enumeration<URL> taglibs = reqBundle.findEntries(META_INF, TAGLIB_XML_PATTERN, false);

        if ( taglibs == null || !taglibs.hasMoreElements() ) {
            return;
        }

        while ( taglibs.hasMoreElements() ) {
            URL u = taglibs.nextElement();
            try {
                log.debug(String.format("Adding taglib from %s: %s", reqBundle.getSymbolicName(), u)); //$NON-NLS-1$
                FacesConfigImpl config = new FacesConfigImpl();
                config.addFaceletTagLibrary(parseTaglibConfig(ectx, u));
                cfgs.add(config);
            }
            catch ( IOException e ) {
                log.warn("Failed to parse taglib " + u, e); //$NON-NLS-1$
            }
        }
    }


    private void addFacesConfigs ( ExternalContext ectx, List<FacesConfig> res, Bundle reqBundle, Set<URL> cfgs ) {
        for ( URL facesCfg : cfgs ) {
            try {
                log.debug(String.format("Adding faces config from %s: %s", reqBundle.getSymbolicName(), facesCfg)); //$NON-NLS-1$
                res.add(this.parseFacesConfig(ectx, facesCfg));
            }
            catch (
                IOException |
                SAXException e ) {
                log.warn("Failed to parse faces config " + facesCfg, e); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.apache.myfaces.spi.FacesConfigurationProviderWrapper#getFacesFlowFacesConfig(javax.faces.context.ExternalContext)
     */
    @Override
    public List<FacesConfig> getFacesFlowFacesConfig ( ExternalContext ectx ) {
        // TODO: unimplemented
        return super.getFacesFlowFacesConfig(ectx);
    }


    private FacesConfig parseFacesConfig ( ExternalContext ectx, URL u ) throws IOException, SAXException {
        if ( MyfacesConfig.getCurrentInstance(ectx).isValidateXML() ) {
            validateFacesConfig(ectx, u);

        }

        try ( InputStream s = u.openStream() ) {
            return getUnmarshaller(ectx).getFacesConfig(s, u.toExternalForm());
        }
    }


    protected FacesConfigUnmarshaller<? extends FacesConfig> getUnmarshaller ( ExternalContext ectx ) {

        if ( this.unmarshaller == null ) {
            this.unmarshaller = new DigesterFacesConfigUnmarshallerImpl(ectx);
        }
        return this.unmarshaller;
    }


    private static void validateFacesConfig ( ExternalContext ectx, URL url ) throws IOException, SAXException {
        String version = ConfigFilesXmlValidationUtils.getFacesConfigVersion(url);
        if ( "1.2".equals(version) //$NON-NLS-1$
                || "2.0".equals(version) //$NON-NLS-1$
                || "2.1".equals(version) //$NON-NLS-1$
                || "2.2".equals(version) ) { //$NON-NLS-1$
            ConfigFilesXmlValidationUtils.validateFacesConfigFile(url, ectx, version);
        }
    }


    private static FaceletTagLibrary parseTaglibConfig ( ExternalContext ectx, URL url ) throws IOException {
        return TagLibraryConfigUnmarshallerImpl.create(ectx, url);
    }
}
