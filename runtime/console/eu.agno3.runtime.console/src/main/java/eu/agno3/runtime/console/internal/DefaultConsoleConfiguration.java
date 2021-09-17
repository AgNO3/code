/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.06.2013 by mbechler
 */
package eu.agno3.runtime.console.internal;


import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;

import eu.agno3.runtime.console.ConsoleConfiguration;


/**
 * @author mbechler
 * 
 */
public class DefaultConsoleConfiguration extends AbstractConsoleConfiguration implements ConsoleConfiguration {

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.console.ConsoleConfiguration#getApplicationName()
     */
    @Override
    public String getApplicationName () {
        return "Console"; //$NON-NLS-1$
    }


    @Override
    protected Color getPromptColor () {
        return Ansi.Color.BLUE;
    }


    @Override
    protected String getPromptString () {
        return "console> "; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.console.ConsoleConfiguration#getScopes()
     */
    @Override
    public String getScopes () {
        return "bundles:services:*"; //$NON-NLS-1$
    }
}
