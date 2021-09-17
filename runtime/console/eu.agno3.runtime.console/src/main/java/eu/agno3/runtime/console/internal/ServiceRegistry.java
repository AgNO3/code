/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.09.2015 by mbechler
 */
package eu.agno3.runtime.console.internal;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.karaf.shell.api.console.Command;
import org.apache.karaf.shell.api.console.Completer;
import org.apache.karaf.shell.api.console.Registry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;


/**
 * @author mbechler
 *
 */
@Component ( service = ServiceRegistry.class )
public class ServiceRegistry implements Registry {

    private List<Command> commands = new ArrayList<>();
    private Map<String, Command> byName = new HashMap<>();

    private Map<Class<?>, Completer> completers = new HashMap<>();


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindCompleter ( Completer c ) {
        this.completers.put(c.getClass(), c);
    }


    protected synchronized void unbindCompleter ( Completer c ) {
        this.completers.remove(c.getClass(), c);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.karaf.shell.api.console.Registry#getCommand(java.lang.String, java.lang.String)
     */
    @Override
    public Command getCommand ( String scope, String name ) {
        return this.byName.get(getCommandAlias(scope, name));
    }


    /**
     * @param scope
     * @param name
     * @return
     */
    private static String getCommandAlias ( String scope, String name ) {
        return scope + ":" + name; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.karaf.shell.api.console.Registry#getCommands()
     */
    @Override
    public List<Command> getCommands () {
        return Collections.unmodifiableList(this.commands);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.karaf.shell.api.console.Registry#getService(java.lang.Class)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public <T> T getService ( Class<T> clazz ) {
        return (T) this.completers.get(clazz);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.karaf.shell.api.console.Registry#getServices(java.lang.Class)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public <T> List<T> getServices ( Class<T> clazz ) {
        if ( Command.class.isAssignableFrom(clazz) ) {
            return (List<T>) getCommands();
        }
        return Collections.singletonList(getService(clazz));
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.karaf.shell.api.console.Registry#hasService(java.lang.Class)
     */
    @Override
    public boolean hasService ( Class<?> clazz ) {
        return this.completers.containsKey(clazz);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.karaf.shell.api.console.Registry#register(java.lang.Object)
     */
    @Override
    public void register ( Object obj ) {
        if ( obj instanceof Command ) {
            Command c = (Command) obj;
            this.byName.put(getCommandAlias(c.getScope(), c.getName()), c);
            this.commands.add(c);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.karaf.shell.api.console.Registry#register(java.util.concurrent.Callable, java.lang.Class)
     */
    @Override
    public <T> void register ( Callable<T> arg0, Class<T> arg1 ) {

    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.karaf.shell.api.console.Registry#unregister(java.lang.Object)
     */
    @Override
    public void unregister ( Object obj ) {
        if ( obj instanceof Command ) {
            Command c = (Command) obj;
            this.commands.remove(obj);
            this.byName.remove(getCommandAlias(c.getScope(), c.getName()));
        }
    }
}
