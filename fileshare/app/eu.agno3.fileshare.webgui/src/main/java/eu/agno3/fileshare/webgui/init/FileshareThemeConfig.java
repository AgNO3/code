/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.06.2016 by mbechler
 */
package eu.agno3.fileshare.webgui.init;


import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

import eu.agno3.runtime.jsf.config.ThemeConfig;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Alternative
public class FileshareThemeConfig implements ThemeConfig {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.config.ThemeSelector#getTheme()
     */
    @Override
    public String getTheme () {
        return "agno3"; //$NON-NLS-1$
    }
}
