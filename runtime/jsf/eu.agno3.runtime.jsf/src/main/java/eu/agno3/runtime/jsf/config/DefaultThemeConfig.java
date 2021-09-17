/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.06.2016 by mbechler
 */
package eu.agno3.runtime.jsf.config;


import javax.enterprise.context.ApplicationScoped;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class DefaultThemeConfig implements ThemeConfig {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.jsf.config.ThemeConfig#getTheme()
     */
    @Override
    public String getTheme () {
        return "agno3"; //$NON-NLS-1$
    }

}
