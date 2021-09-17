/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.02.2015 by mbechler
 */
package eu.agno3.runtime.tpl.internal;


import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.configloader.file.ConfigFileLoaderBuilder;
import eu.agno3.runtime.tpl.TemplateConfigurationBuilder;

import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.core.TemplateClassResolver;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.Version;


/**
 * @author mbechler
 *
 */
@Component ( service = TemplateConfigurationBuilder.class )
public class ConfigurationBuilderImpl implements TemplateConfigurationBuilder {

    private static final Version TPL_VERSION = new Version(2, 3, 21);
    private ConfigFileLoaderBuilder cfBuilder;


    @Reference
    protected synchronized void setConfigFileLoaderBuilder ( ConfigFileLoaderBuilder cflb ) {
        this.cfBuilder = cflb;
    }


    protected synchronized void unsetConfigFileLoaderBuilder ( ConfigFileLoaderBuilder cflb ) {
        if ( this.cfBuilder == cflb ) {
            this.cfBuilder = null;
        }
    }


    @Override
    public Configuration create ( TemplateLoader... loaders ) {
        Configuration cfg = new Configuration(TPL_VERSION);
        cfg.setTemplateLoader(new MultiTemplateLoader(loaders));
        this.setup(cfg);
        return cfg;
    }


    @Override
    public void setup ( Configuration cfg ) {
        cfg.setDefaultEncoding("UTF-8"); //$NON-NLS-1$
        cfg.setIncompatibleImprovements(new Version(2, 3, 20));
        cfg.setAutoFlush(true);
        cfg.setNewBuiltinClassResolver(TemplateClassResolver.ALLOWS_NOTHING_RESOLVER);

        BeansWrapperBuilder objectWrapperBuilder = new BeansWrapperBuilder(TPL_VERSION);
        objectWrapperBuilder.setStrict(true);
        objectWrapperBuilder.setUseModelCache(false);
        objectWrapperBuilder.setExposeFields(false);
        objectWrapperBuilder.setExposureLevel(BeansWrapper.EXPOSE_PROPERTIES_ONLY);
        objectWrapperBuilder.setOuterIdentity(new ExtendedObjectWrapper(cfg.getIncompatibleImprovements()));
        cfg.setObjectWrapper(objectWrapperBuilder.build());
        cfg.setTagSyntax(Configuration.SQUARE_BRACKET_TAG_SYNTAX);
    }


    @Override
    public void destroy ( Configuration cfg ) {
        cfg.clearSharedVariables();
        cfg.clearTemplateCache();
        // new version no longer allows to clear the introspection cache... lets hope this does not leak
    }


    @Override
    public BundleTemplateLoader makeBundleLoader ( Bundle bundle ) {
        return new BundleTemplateLoader(bundle);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.tpl.TemplateConfigurationBuilder#makeConfigFileLoader(org.osgi.framework.Bundle)
     */
    @Override
    public TemplateLoader makeConfigFileLoader ( Bundle bundle ) {
        return new ConfigTemplateLoader(this.cfBuilder.createForBundle(bundle));
    }
}
