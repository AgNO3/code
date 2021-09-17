/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.i18n;


import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTimeZone;

import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.runtime.jsf.prefs.AbstractLocaleSettingsBean;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "localeSettingsBean" )
public class LocaleSettingsBean extends AbstractLocaleSettingsBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -8928732947807008775L;

    @Inject
    private FileshareServiceProvider fsp;


    @Override
    protected DateTimeZone getFallbackDefaultTimeZone () {
        return this.fsp.getConfigurationProvider().getFrontendConfiguration().getDefaultTimeZone();
    }

}
