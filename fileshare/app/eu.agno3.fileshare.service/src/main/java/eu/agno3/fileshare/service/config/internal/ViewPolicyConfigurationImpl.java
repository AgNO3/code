/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.02.2015 by mbechler
 */
package eu.agno3.fileshare.service.config.internal;


import java.util.Dictionary;
import java.util.Set;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.fileshare.service.config.ViewPolicyConfiguration;
import eu.agno3.fileshare.service.config.ViewPolicyDefaults;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = ViewPolicyConfiguration.class, configurationPid = "viewPolicy" )
public class ViewPolicyConfigurationImpl implements ViewPolicyConfiguration {

    private Set<String> viewableMimeTypes;
    private Set<String> safeMimeTypes;
    private Set<String> noSandboxMimeTypes;
    private Set<String> relaxedCSPMimeTypes;

    private long maxPreviewFileSize;


    private void parseConfig ( Dictionary<String, Object> cfg ) {
        this.viewableMimeTypes = ConfigUtil.parseStringSet(cfg, "viewableMimeTypes", ViewPolicyDefaults.DEFAULT_VIEWABLE_MIME_TYPES); //$NON-NLS-1$
        this.safeMimeTypes = ConfigUtil.parseStringSet(cfg, "safeMimeTypes", ViewPolicyDefaults.DEFAULT_SAFE_MIME_TYPES); //$NON-NLS-1$
        this.noSandboxMimeTypes = ConfigUtil.parseStringSet(cfg, "noSandboxMimeTypes", ViewPolicyDefaults.DEFAULT_NO_SANDBOX_MIME_TYPES); //$NON-NLS-1$
        this.relaxedCSPMimeTypes = ConfigUtil.parseStringSet(cfg, "relaxedCSPMimeTypes", ViewPolicyDefaults.DEFAULT_RELAXED_CSP_MIME_TYPES); //$NON-NLS-1$

        this.maxPreviewFileSize = ConfigUtil.parseInt(cfg, "maxPreviewFileSize", -1); //$NON-NLS-1$
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        parseConfig(ctx.getProperties());
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        parseConfig(ctx.getProperties());
    }


    /**
     * @return the maxPreviewFileSize
     */
    @Override
    public long getMaxPreviewFileSize () {
        return this.maxPreviewFileSize;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.ViewPolicyConfiguration#isSafe(java.lang.String)
     */
    @Override
    public boolean isSafe ( String contentType ) {
        return this.safeMimeTypes.contains(contentType);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.ViewPolicyConfiguration#isViewable(java.lang.String)
     */
    @Override
    public boolean isViewable ( String mimeType ) {
        return this.viewableMimeTypes.contains(mimeType);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.ViewPolicyConfiguration#getNoSandboxMimeTypes()
     */
    @Override
    public Set<String> getNoSandboxMimeTypes () {
        return this.noSandboxMimeTypes;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.ViewPolicyConfiguration#getRelaxedCSPMimeTypes()
     */
    @Override
    public Set<String> getRelaxedCSPMimeTypes () {
        return this.relaxedCSPMimeTypes;
    }
}
