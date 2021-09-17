/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2014 by mbechler
 */
package eu.agno3.runtime.console.internal;


import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;

import eu.agno3.runtime.console.ConsoleConfiguration;


/**
 * @author mbechler
 *
 */
public abstract class AbstractConsoleConfiguration implements ConsoleConfiguration {

    /**
     * 
     */
    public AbstractConsoleConfiguration () {
        super();
    }


    protected abstract String getPromptString ();


    protected abstract Color getPromptColor ();


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.console.ConsoleConfiguration#getPrompt()
     */
    @Override
    public String getPrompt () {
        return Ansi.ansi().bold().fg(getPromptColor()).a(getPromptString()).fg(Ansi.Color.DEFAULT).reset().toString();
    }

}