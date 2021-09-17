/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.06.2016 by mbechler
 */
package eu.agno3.runtime.jsf.config;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "themeSelector" )
public class ThemeSelector {

    @Inject
    private ThemeConfig themeConfig;


    /**
     * 
     * @return the selected theme
     */
    public String getTheme () {
        return this.themeConfig.getTheme();
    }
}
