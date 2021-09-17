/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.02.2015 by mbechler
 */
package eu.agno3.fileshare.service.config.internal;


import java.util.Dictionary;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MediaTypeRegistry;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.fileshare.exceptions.DisallowedMimeTypeException;
import eu.agno3.fileshare.service.config.MimeTypePolicyConfiguration;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = MimeTypePolicyConfiguration.class, configurationPid = "mimeTypes" )
public class MimeTypePolicyConfigurationImpl implements MimeTypePolicyConfiguration {

    private boolean allowMimeTypeChanges;
    private SortedSet<String> allowedMimeTypes;
    private Set<String> blacklistedMimeTypes;
    private String fallbackMimeType = "application/octet-stream"; //$NON-NLS-1$
    private boolean useUserSuppliedTypes;


    /**
     * @param ctx
     */
    private void parseConfig ( ComponentContext ctx ) {
        Dictionary<String, Object> cfg = ctx.getProperties();
        this.allowMimeTypeChanges = ConfigUtil.parseBoolean(cfg, "allowMimeTypeChanges", false); //$NON-NLS-1$
        this.fallbackMimeType = ConfigUtil.parseString(
            cfg,
            "fallbackMimeType", //$NON-NLS-1$
            "application/octet-stream"); //$NON-NLS-1$
        this.blacklistedMimeTypes = ConfigUtil.parseStringSet(cfg, "blacklistedMimeTypes", new HashSet<>()); //$NON-NLS-1$
        SortedSet<String> defaultAllowedTypes = makeDefaultAllowedTypes();
        defaultAllowedTypes.removeAll(this.blacklistedMimeTypes);
        this.allowedMimeTypes = new TreeSet<>(ConfigUtil.parseStringSet(cfg, "allowedMimeTypes", defaultAllowedTypes)); //$NON-NLS-1$
        this.useUserSuppliedTypes = ConfigUtil.parseBoolean(cfg, "useUserSuppliedTypes", true); //$NON-NLS-1$
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        parseConfig(ctx);

    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        parseConfig(ctx);
    }


    /**
     * @return
     */
    private static SortedSet<String> makeDefaultAllowedTypes () {
        SortedSet<String> allowed = new TreeSet<>();

        for ( MediaType type : MediaTypeRegistry.getDefaultRegistry().getTypes() ) {
            allowed.add(type.toString());
        }
        return allowed;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.MimeTypePolicyConfiguration#isAllowMimeTypeChanges()
     */
    @Override
    public boolean isAllowMimeTypeChanges () {
        return this.allowMimeTypeChanges;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.MimeTypePolicyConfiguration#getAllowedMimeTypes()
     */
    @Override
    public SortedSet<String> getAllowedMimeTypes () {
        return this.allowedMimeTypes;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.MimeTypePolicyConfiguration#checkMimeType(java.lang.String, boolean)
     */
    @Override
    public void checkMimeType ( String type, boolean ignoreUnknown ) throws DisallowedMimeTypeException {
        if ( this.allowedMimeTypes != null ) {
            if ( !this.allowedMimeTypes.contains(type) ) {
                if ( ignoreUnknown && this.blacklistedMimeTypes != null && !this.blacklistedMimeTypes.contains(type)
                        && this.allowedMimeTypes.contains(getFallbackMimeType()) ) {
                    return;
                }
                throw new DisallowedMimeTypeException(type);
            }
        }

        if ( this.blacklistedMimeTypes != null ) {
            if ( this.blacklistedMimeTypes.contains(type) ) {
                throw new DisallowedMimeTypeException(type);
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.MimeTypePolicyConfiguration#getBlacklistedMimeTypes()
     */
    @Override
    public Set<String> getBlacklistedMimeTypes () {
        return this.blacklistedMimeTypes;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.MimeTypePolicyConfiguration#getFallbackMimeType()
     */
    @Override
    public String getFallbackMimeType () {
        return this.fallbackMimeType;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.MimeTypePolicyConfiguration#isUseUserSuppliedTypes()
     */
    @Override
    public boolean isUseUserSuppliedTypes () {
        return this.useUserSuppliedTypes;
    }

}
