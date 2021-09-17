/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.05.2015 by mbechler
 */
package eu.agno3.fileshare.service.config.internal;


import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.TrustLevel;
import eu.agno3.runtime.i18n.StaticMapResourceBundleControl;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = TrustLevel.class, configurationPid = "trustLevel", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class TrustLevelImpl implements TrustLevel {

    private static final Logger log = Logger.getLogger(TrustLevelImpl.class);

    private boolean active;
    private float priority;

    private Control titleCtrl;

    private String id;
    private String color;
    private Map<Locale, String> messages = new HashMap<>();
    private Map<Locale, String> titles = new HashMap<>();

    private Set<String> matchRoles = new HashSet<>();
    private boolean matchLink;
    private boolean matchAnyMail;
    private boolean matchGroups;

    private Control msgCtrl;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.id = ConfigUtil.parseString(ctx.getProperties(), "instanceId", null); //$NON-NLS-1$
        if ( StringUtils.isBlank(this.id) ) {
            this.active = false;
            return;
        }
        parseConfig(ctx.getProperties());
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        parseConfig(ctx.getProperties());
    }


    /**
     * @param cfg
     */
    private void parseConfig ( Dictionary<String, Object> cfg ) {
        this.active = ConfigUtil.parseBoolean(cfg, "active", true); //$NON-NLS-1$
        this.priority = ConfigUtil.parseFloat(cfg, "priority", 0.0f); //$NON-NLS-1$
        this.color = ConfigUtil.parseString(cfg, "color", //$NON-NLS-1$
            "red"); //$NON-NLS-1$
        this.matchRoles = ConfigUtil.parseStringSet(cfg, "matchRoles", new HashSet<>()); //$NON-NLS-1$

        this.matchLink = ConfigUtil.parseBoolean(cfg, "matchLinks", false); //$NON-NLS-1$
        this.matchGroups = ConfigUtil.parseBoolean(cfg, "matchGroups", false); //$NON-NLS-1$
        this.matchAnyMail = ConfigUtil.parseBoolean(cfg, "matchAnyMail", false); //$NON-NLS-1$

        Enumeration<String> keys = cfg.keys();
        this.messages.clear();
        while ( keys.hasMoreElements() ) {
            String key = keys.nextElement();
            Object value = cfg.get(key);
            if ( key.startsWith("msg_") ) { //$NON-NLS-1$
                this.messages.put(Locale.forLanguageTag(key.substring(4)), (String) value);
            }
            else if ( key.startsWith("title_") ) { //$NON-NLS-1$
                this.titles.put(Locale.forLanguageTag(key.substring(5)), (String) value);
            }
            else if ( key.equals("msg") ) { //$NON-NLS-1$
                this.messages.put(Locale.ROOT, (String) value);
            }
            else if ( key.equals("title") ) { //$NON-NLS-1$
                this.titles.put(Locale.ROOT, (String) value);
            }
        }
        if ( this.messages.isEmpty() ) {
            log.error("No messages found for trust level " + this.id); //$NON-NLS-1$
        }

        if ( this.titles.isEmpty() ) {
            this.titles.put(Locale.ROOT, this.id);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.TrustLevel#getId()
     */
    @Override
    public String getId () {
        return this.id;
    }


    /**
     * @return the title
     */
    @Override
    public String getTitle ( Locale l ) {
        return ResourceBundle.getBundle(StringUtils.EMPTY, l, this.getTitleBundleControl()).getString("msg"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.TrustLevel#getPriority()
     */
    @Override
    public float getPriority () {
        return this.priority;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.TrustLevel#getColor()
     */
    @Override
    public String getColor () {
        return this.color;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.TrustLevel#getMessage(java.util.Locale)
     */
    @Override
    public String getMessage ( Locale l ) {
        try {
            return ResourceBundle.getBundle(StringUtils.EMPTY, l, this.getMessageBundleControl()).getString("msg"); //$NON-NLS-1$
        }
        catch ( MissingResourceException e ) {
            log.debug("Missing message for trustlevel " + this.id, e); //$NON-NLS-1$
            return this.id;
        }
    }


    /**
     * @return the resource bundle control
     */
    private Control getMessageBundleControl () {
        Control c = this.msgCtrl;
        if ( c == null ) {
            c = new StaticMapResourceBundleControl(this.messages);
            this.msgCtrl = c;
        }
        return c;
    }


    /**
     * @return the resource bundle control
     */
    private Control getTitleBundleControl () {
        Control c = this.titleCtrl;
        if ( c == null ) {
            c = new StaticMapResourceBundleControl(this.titles);
            this.titleCtrl = c;
        }
        return c;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.TrustLevel#match(eu.agno3.fileshare.model.Subject)
     */
    @Override
    public boolean match ( Subject s ) {
        if ( !this.active ) {
            return false;
        }

        if ( this.matchRoles.isEmpty() ) {
            return true;
        }

        for ( String role : this.matchRoles ) {
            if ( s.getRoles() != null && s.getRoles().contains(role) ) {
                return true;
            }
        }
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.TrustLevel#matchLink()
     */
    @Override
    public boolean matchLink () {
        return this.matchLink;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.TrustLevel#matchMail(java.lang.String)
     */
    @Override
    public boolean matchMail ( String mailAddress ) {
        return this.matchAnyMail;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.TrustLevel#matchGroup(eu.agno3.fileshare.model.Group)
     */
    @Override
    public boolean matchGroup ( Group g ) {
        return this.matchGroups;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return this.id;
    }
}
