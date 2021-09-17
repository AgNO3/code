/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.02.2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.prefs;


import java.util.TimeZone;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.joda.time.DateTimeZone;

import eu.agno3.runtime.jsf.prefs.AbstractLocaleSettingsBean;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "localeSettingsBean" )
public class LocaleSettingsBean extends AbstractLocaleSettingsBean {

    /**
     * 
     */
    private static final long serialVersionUID = -7100771095263164040L;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.prefs.AbstractLocaleSettingsBean#getFallbackDefaultTimeZone()
     */
    @Override
    protected DateTimeZone getFallbackDefaultTimeZone () {
        return DateTimeZone.forTimeZone(TimeZone.getDefault());
    }

}
