/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2014 by mbechler
 */
package eu.agno3.runtime.console.internal;


import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import eu.agno3.runtime.console.ConsoleConfiguration;


/**
 * @author mbechler
 *
 */
@Component ( service = ConsoleConfiguration.class, configurationPid = ConsoleConfiguration.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class ConsoleConfigurationImpl extends AbstractConsoleConfiguration implements ConsoleConfiguration {

    private static final String DEFAULT_APPLICATION_NAME = "Unknown"; //$NON-NLS-1$
    private static final String DEFAULT_PROMPT_STRING = "console> "; //$NON-NLS-1$
    private static final Color DEFAULT_PROMPT_COLOR = Ansi.Color.BLUE;
    private static final String DEFAULT_SCOPES = "*"; //$NON-NLS-1$

    private String applicationName = DEFAULT_APPLICATION_NAME;
    private String prompt = DEFAULT_PROMPT_STRING;
    private Color promptColor = DEFAULT_PROMPT_COLOR;
    private String scopes = DEFAULT_SCOPES;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {

        String appNameSpec = (String) ctx.getProperties().get("appName"); //$NON-NLS-1$

        if ( !StringUtils.isBlank(appNameSpec) ) {
            this.applicationName = appNameSpec.trim();
        }

        String promptSpec = (String) ctx.getProperties().get("prompt"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(promptSpec) ) {
            this.prompt = promptSpec;
        }

        String promptColorSpec = (String) ctx.getProperties().get("promptColor"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(promptColorSpec) ) {
            this.promptColor = Ansi.Color.valueOf(promptColorSpec.trim());
        }

        String scopesSpec = (String) ctx.getProperties().get("scopes"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(scopesSpec) ) {
            this.scopes = scopesSpec.trim();
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.console.ConsoleConfiguration#getApplicationName()
     */
    @Override
    public String getApplicationName () {
        return this.applicationName;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.console.ConsoleConfiguration#getScopes()
     */
    @Override
    public String getScopes () {
        return this.scopes;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.console.internal.AbstractConsoleConfiguration#getPromptString()
     */
    @Override
    protected String getPromptString () {
        return this.prompt;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.console.internal.AbstractConsoleConfiguration#getPromptColor()
     */
    @Override
    protected Color getPromptColor () {
        return this.promptColor;
    }

}
