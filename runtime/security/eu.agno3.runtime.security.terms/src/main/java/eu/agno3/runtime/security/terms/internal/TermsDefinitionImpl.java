/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.09.2016 by mbechler
 */
package eu.agno3.runtime.security.terms.internal;


import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.runtime.i18n.StaticMapResourceBundleControl;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.security.terms.TermsDefinition;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = TermsDefinition.class, configurationPid = TermsDefinitionImpl.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class TermsDefinitionImpl implements TermsDefinition {

    /**
     * 
     */
    public static final String PID = "terms"; //$NON-NLS-1$

    private String id;

    private Map<Locale, String> labels = new HashMap<>();
    private Map<Locale, String> descriptions = new HashMap<>();

    private transient Control labelCtrl;
    private transient Control descriptionCtrl;

    private float priority;

    private DateTime lastModified;

    private boolean persistAcceptance;
    private Duration unauthPersistanceMaxAge;

    private String contentId;

    private boolean onlyUnauth;
    private boolean excludeUnauth;

    private Set<String> includeRoles;
    private Set<String> excludeRoles;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.id = ConfigUtil.parseString(ctx.getProperties(), "instanceId", null); //$NON-NLS-1$
        this.priority = ConfigUtil.parseFloat(ctx.getProperties(), "priority", 0.0f); //$NON-NLS-1$
        parseConfig(ctx.getProperties());
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        parseConfig(ctx.getProperties());
    }


    /**
     * @return the id
     */
    @Override
    public String getId () {
        return this.id;
    }


    @Override
    public String getContentId () {
        return this.contentId;
    }


    /**
     * @return the priority
     */
    @Override
    public float getPriority () {
        return this.priority;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.terms.TermsDefinition#isPersistAcceptance()
     */
    @Override
    public boolean isPersistAcceptance () {
        return this.persistAcceptance;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.terms.TermsDefinition#getUnauthPersistenceMaxAge()
     */
    @Override
    public Duration getUnauthPersistenceMaxAge () {
        return this.unauthPersistanceMaxAge;
    }


    /**
     * @param properties
     */
    private void parseConfig ( Dictionary<String, Object> cfg ) {
        Enumeration<String> keys = cfg.keys();
        while ( keys.hasMoreElements() ) {
            String key = keys.nextElement();
            Object value = cfg.get(key);
            if ( key.startsWith("label_") ) { //$NON-NLS-1$
                this.labels.put(Locale.forLanguageTag(key.substring(4)), (String) value);
            }
            else if ( key.startsWith("description_") ) { //$NON-NLS-1$
                this.descriptions.put(Locale.forLanguageTag(key.substring(5)), (String) value);
            }
            else if ( key.equals("label") ) { //$NON-NLS-1$
                this.labels.put(Locale.ROOT, (String) value);
            }
            else if ( key.equals("description") ) { //$NON-NLS-1$
                this.descriptions.put(Locale.ROOT, (String) value);
            }
        }

        if ( this.labels.isEmpty() ) {
            this.labels.put(Locale.ROOT, this.id);
        }

        if ( this.descriptions.isEmpty() ) {
            this.descriptions.put(Locale.ROOT, StringUtils.EMPTY);
        }

        String lmodStr = ConfigUtil.parseString(cfg, "lastModified", null); //$NON-NLS-1$

        if ( !StringUtils.isBlank(lmodStr) ) {
            this.lastModified = new DateTime(Long.parseLong(lmodStr));
        }

        this.persistAcceptance = ConfigUtil.parseBoolean(cfg, "persistAcceptance", true); //$NON-NLS-1$
        this.contentId = ConfigUtil.parseString(cfg, "contentId", this.id); //$NON-NLS-1$
        this.unauthPersistanceMaxAge = ConfigUtil.parseDuration(cfg, "unauthPersistanceMaxAge", Duration.standardDays(30)); //$NON-NLS-1$

        this.excludeUnauth = ConfigUtil.parseBoolean(cfg, "excludeUnauth", false); //$NON-NLS-1$
        this.onlyUnauth = ConfigUtil.parseBoolean(cfg, "onlyUnauth", false); //$NON-NLS-1$
        this.includeRoles = ConfigUtil.parseStringSet(cfg, "includeRoles", null); //$NON-NLS-1$
        this.excludeRoles = ConfigUtil.parseStringSet(cfg, "excludeRoles", null); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.terms.TermsDefinition#isApplicable(eu.agno3.runtime.security.principal.UserPrincipal,
     *      java.util.Set)
     */
    @Override
    public boolean isApplicable ( UserPrincipal up, Set<String> roles ) {
        if ( ( this.onlyUnauth && up != null ) || ( this.excludeUnauth && up == null ) ) {
            return false;
        }
        if ( roles != null ) {
            if ( this.includeRoles != null && !this.includeRoles.isEmpty() && Collections.disjoint(this.includeRoles, roles) ) {
                return false;
            }
            if ( this.excludeRoles != null && !this.excludeRoles.isEmpty() && !Collections.disjoint(this.excludeRoles, roles) ) {
                return false;
            }
        }
        return true;
    }


    /**
     * @return the lastModified
     */
    @Override
    public DateTime getLastModified () {
        return this.lastModified;
    }


    /**
     * @return the title
     */
    @Override
    public String getLabel ( Locale l ) {
        return ResourceBundle.getBundle(StringUtils.EMPTY, l, this.getLabelBundleControl()).getString("msg"); //$NON-NLS-1$
    }


    @Override
    public String getDescription ( Locale l ) {
        return ResourceBundle.getBundle(StringUtils.EMPTY, l, this.getDescriptionBundleControl()).getString("msg"); //$NON-NLS-1$
    }


    /**
     * @return the resource bundle control
     */
    private Control getLabelBundleControl () {
        Control c = this.labelCtrl;
        if ( c == null ) {
            c = new StaticMapResourceBundleControl(this.labels);
            this.labelCtrl = c;
        }
        return c;
    }


    /**
     * @return the resource bundle control
     */
    private Control getDescriptionBundleControl () {
        Control c = this.descriptionCtrl;
        if ( c == null ) {
            c = new StaticMapResourceBundleControl(this.descriptions);
            this.descriptionCtrl = c;
        }
        return c;
    }

}
