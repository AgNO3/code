/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.06.2013 by mbechler
 */
package eu.agno3.runtime.console.osgi.internal;


import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.console.Session;
import org.apache.log4j.Logger;
import org.fusesource.jansi.Ansi;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentConfigurationDTO;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.osgi.service.component.runtime.dto.ReferenceDTO;
import org.osgi.service.component.runtime.dto.UnsatisfiedReferenceDTO;

import eu.agno3.runtime.console.CommandProvider;
import eu.agno3.runtime.console.osgi.ComponentState;


/**
 * @author mbechler
 * 
 */
@org.osgi.service.component.annotations.Component ( service = {
    CommandProvider.class
} )
public class DSCommandProvider implements CommandProvider {

    private static final Logger log = Logger.getLogger(DSCommandProvider.class);

    private BundleContext bundleContext;
    private ServiceComponentRuntime scrService;

    private static final String DISABLING_COMPONENT = "Disabling component "; //$NON-NLS-1$
    private static final String ENABLING_COMPONENT = "Enabling component "; //$NON-NLS-1$

    private static final Map<ComponentState, Ansi.Color> STATE_COLORS = new EnumMap<>(ComponentState.class);


    static {
        STATE_COLORS.put(ComponentState.ACTIVE, Ansi.Color.GREEN);
        STATE_COLORS.put(ComponentState.SATISFIED, Ansi.Color.GREEN);
        STATE_COLORS.put(ComponentState.UNSATISFIED_REFERENCE, Ansi.Color.RED);
        STATE_COLORS.put(ComponentState.UNSATISFIED_CONFIGURATION, Ansi.Color.RED);
    }


    @Activate
    protected void activate ( ComponentContext context ) {
        this.bundleContext = context.getBundleContext();
    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        this.bundleContext = null;
    }


    BundleContext getBundleContext () {
        return this.bundleContext;
    }


    Logger getLog () {
        return log;
    }


    synchronized ServiceComponentRuntime getScrService () {
        return this.scrService;
    }


    @Reference
    protected synchronized void setScrService ( ServiceComponentRuntime service ) {
        this.scrService = service;
    }


    protected synchronized void unsetScrService ( ServiceComponentRuntime service ) {
        if ( this.scrService == service ) {
            this.scrService = null;
        }
    }


    /**
     * @param out
     * @param component
     * @param verbose
     */
    public void printComponent ( Ansi out, ComponentConfigurationDTO component, boolean verbose ) {
        ComponentState state = ComponentState.fromStateCode(component.state);

        setColorForState(out, state);

        out.a(String.format("%-10s", state.toString())); //$NON-NLS-1$
        out.boldOff();
        out.fg(Ansi.Color.DEFAULT);

        out.a("  ").bold().fg(Ansi.Color.BLUE).a(component.description.name).boldOff().fg(Ansi.Color.DEFAULT); //$NON-NLS-1$
        out.a(String.format("[%d]", component.id)); //$NON-NLS-1$

        printComponentType(out, component);

        if ( !verbose && !component.description.implementationClass.equals(component.description.name) ) {
            out.a(System.lineSeparator());
            out.bold().a(String.format("%10s   Implementation: ", StringUtils.EMPTY)).a(component.description.implementationClass).boldOff(); //$NON-NLS-1$
        }

        out.a(System.lineSeparator());
        out.bold().a(String.format("%13sBundle: ", StringUtils.EMPTY));//$NON-NLS-1$
        BundleCommandUtil.printBundleReference(out, component.description.bundle);
        out.boldOff();
        out.a(System.lineSeparator());

        if ( verbose ) {
            printComponentVerbose(out, component);
        }

    }


    /**
     * @param out
     * @param component
     */
    private static void printComponentVerbose ( Ansi out, ComponentConfigurationDTO component ) {
        out.bold().a(String.format("%13sProperties: ", StringUtils.EMPTY));//$NON-NLS-1$
        out.a(System.lineSeparator());

        for ( Entry<String, Object> entry : component.properties.entrySet() ) {
            out.bold().a(String.format("%14s", StringUtils.EMPTY));//$NON-NLS-1$
            out.a(entry.getKey()).a(": ").boldOff(); //$NON-NLS-1$
            BundleCommandUtil.dumpPropertyValue(out, entry.getValue());

            out.a(System.lineSeparator());
        }

        if ( component.description.references != null && component.description.references.length > 0 ) {
            out.bold().a(String.format("%13sReferences: ", StringUtils.EMPTY)).boldOff();//$NON-NLS-1$
            out.a(System.lineSeparator());

            Set<String> unsatisfiedRefs = new HashSet<>();

            for ( UnsatisfiedReferenceDTO unsat : component.unsatisfiedReferences ) {
                unsatisfiedRefs.add(unsat.name);
            }

            for ( ReferenceDTO ref : component.description.references ) {
                printComponentReference(out, ref, unsatisfiedRefs);
            }
        }
        out.a(System.lineSeparator());
    }


    /**
     * @param out
     * @param ref
     * @param unsatisfiedRefs
     */
    private static void printComponentReference ( Ansi out, ReferenceDTO ref, Set<String> unsatisfiedRefs ) {
        out.bold().a(String.format("%14s", StringUtils.EMPTY));//$NON-NLS-1$
        out.a(ref.interfaceName);
        out.boldOff();

        out.bold().fgBright(Ansi.Color.BLACK);
        if ( unsatisfiedRefs.contains(ref.name) ) {
            out.fg(Ansi.Color.RED);
            out.a(" UNSATISFIED"); //$NON-NLS-1$
            out.bold().fgBright(Ansi.Color.BLACK);
        }

        if ( "optional".equals(ref.cardinality) || //$NON-NLS-1$
                "multiple".equals(ref.cardinality) ) { //$NON-NLS-1$
            out.a(" OPTIONAL"); //$NON-NLS-1$
        }

        if ( !"dynamic".equals(ref.policy) ) { //$NON-NLS-1$
            out.a(" DYNAMIC"); //$NON-NLS-1$
        }

        if ( "multiple".equals(ref.cardinality) || //$NON-NLS-1$
                "at-least-one".equals(ref.cardinality) ) { //$NON-NLS-1$
            out.a(" MULTIPLE"); //$NON-NLS-1$
        }

        out.boldOff().fg(Ansi.Color.DEFAULT);

        if ( ref.bind != null ) {
            out.a(" bind: " + ref.bind); //$NON-NLS-1$
        }

        if ( ref.unbind != null ) {
            out.a(" unbind: " + ref.unbind); //$NON-NLS-1$
        }

        if ( ref.updated != null ) {
            out.a(" update: " + ref.updated); //$NON-NLS-1$
        }

        out.a(System.lineSeparator());
    }


    /**
     * @param out
     * @param component
     */
    private static void printComponentType ( Ansi out, ComponentConfigurationDTO component ) {
        out.bold().fgBright(Ansi.Color.BLACK);
        if ( component.description.defaultEnabled ) {
            out.a(" DEFAULT"); //$NON-NLS-1$
        }

        if ( component.description.immediate ) {
            out.a(" IMMEDIATE"); //$NON-NLS-1$
        }

        if ( component.description.factory != null ) {
            out.fg(Ansi.Color.BLUE);
            out.a(" FACTORY"); //$NON-NLS-1$
        }
        out.boldOff().fg(Ansi.Color.DEFAULT);
    }


    /**
     * @param out
     * @param state
     */
    private static void setColorForState ( Ansi out, ComponentState state ) {

        Ansi.Color c = STATE_COLORS.get(state);

        if ( c == null ) {
            throw new IllegalArgumentException("Unknown component state " + state.name()); //$NON-NLS-1$
        }

        out.fg(c);

        if ( state == ComponentState.ACTIVE ) {
            out.bold();
        }

        if ( state == ComponentState.UNSATISFIED_CONFIGURATION || state == ComponentState.UNSATISFIED_REFERENCE ) {
            out.bold();
        }
    }


    /**
     * @return
     */
    protected Collection<ComponentConfigurationDTO> getComponentsBySpec ( String componentId ) {
        Collection<ComponentConfigurationDTO> components = new LinkedList<>();

        for ( ComponentDescriptionDTO desc : getScrService().getComponentDescriptionDTOs() ) {
            if ( componentId.matches("\\d+") ) { //$NON-NLS-1$
                for ( ComponentConfigurationDTO cc : getScrService().getComponentConfigurationDTOs(desc) ) {
                    if ( cc.id == Integer.parseInt(componentId) ) {
                        return Collections.singletonList(cc);
                    }
                }
            }
            else {
                if ( componentId.equals(desc.name) ) {
                    components.addAll(getScrService().getComponentConfigurationDTOs(desc));
                }
            }
        }

        if ( components.isEmpty() ) {
            throw new IllegalArgumentException(String.format("Cannot find any component with name \'%s\'", componentId)); //$NON-NLS-1$
        }
        return components;
    }


    /**
     * @return
     * @throws BundleException
     */
    Collection<ComponentDescriptionDTO> fetchBundleComponents ( String bundleSpec ) throws BundleException {
        Collection<ComponentDescriptionDTO> components = null;

        if ( bundleSpec != null ) {
            Bundle b = BundleCommandUtil.findBundle(getBundleContext(), bundleSpec);
            components = getScrService().getComponentDescriptionDTOs(b);
        }
        else {
            components = getScrService().getComponentDescriptionDTOs();
        }

        if ( components == null ) {
            return Collections.EMPTY_LIST;
        }

        return components;
    }

    /**
     * List components
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "ds", name = "list", description = "List components" )
    public class ListComponentsCommand implements Action {

        @Option ( name = "-verbose", aliases = "-v" )
        boolean verbose = false;

        @Argument ( index = 0, name = "bundle", required = false )
        @Completion ( BundleCompleter.class )
        String bundleSpec;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws BundleException {
            for ( ComponentDescriptionDTO component : fetchBundleComponents(this.bundleSpec) ) {
                Ansi out = Ansi.ansi();
                for ( ComponentConfigurationDTO c : getScrService().getComponentConfigurationDTOs(component) ) {
                    printComponent(out, c, this.verbose);
                }
                this.session.getConsole().print(out.toString());
            }

            return null;
        }

    }

    /**
     * Component info
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "ds", name = "info", description = "Show component info" )
    public class ComponentInfoCommand implements Action {

        @Argument ( index = 0, name = "component", required = true )
        @Completion ( ComponentIdCompleter.class )
        String componentId;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () {
            for ( ComponentConfigurationDTO component : getComponentsBySpec(this.componentId) ) {
                Ansi out = Ansi.ansi();
                printComponent(out, component, true);
                this.session.getConsole().print(out.toString());
            }

            return null;
        }

    }

    /**
     * Enable component
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "ds", name = "enable", description = "Enable Component" )
    public class ComponentEnableCommand implements Action {

        /**
         * 
         */

        @Argument ( index = 0, name = "component" )
        @Completion ( ComponentIdCompleter.class )
        String componentId;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () {
            for ( ComponentConfigurationDTO component : getComponentsBySpec(this.componentId) ) {
                this.session.getConsole().println(ENABLING_COMPONENT + component.description.name);
                try {
                    getScrService().enableComponent(component.description).getValue();
                }
                catch (
                    InvocationTargetException |
                    InterruptedException e ) {
                    e.printStackTrace(this.session.getConsole());
                }
            }

            return null;
        }

    }

    /**
     * Enable all bundle components
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "ds", name = "enableAll", description = "Enable bundles' components" )
    public class ComponentEnableAllCommand implements Action {

        @Argument ( index = 0, name = "bundle", required = false )
        @Completion ( BundleCompleter.class )
        String bundleSpec;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws BundleException {
            for ( ComponentDescriptionDTO component : fetchBundleComponents(this.bundleSpec) ) {
                this.session.getConsole().println(ENABLING_COMPONENT + component.name);
                try {
                    getScrService().enableComponent(component).getValue();
                }
                catch (
                    InvocationTargetException |
                    InterruptedException e ) {
                    e.printStackTrace(this.session.getConsole());
                }
            }

            return null;
        }

    }

    /**
     * Disable component
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "ds", name = "disable", description = "Disable Component" )
    public class ComponentDisableCommand implements Action {

        /**
         * 
         */

        @Argument ( index = 0, name = "component" )
        @Completion ( ComponentIdCompleter.class )
        String componentId;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () {
            for ( ComponentConfigurationDTO component : getComponentsBySpec(this.componentId) ) {
                this.session.getConsole().println(DISABLING_COMPONENT + component.description.name);
                try {
                    getScrService().disableComponent(component.description).getValue();
                }
                catch (
                    InvocationTargetException |
                    InterruptedException e ) {
                    e.printStackTrace(this.session.getConsole());
                }
            }

            return null;
        }

    }

    /**
     * Enable all bundle components
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "ds", name = "disableAll", description = "Disable bundles' components" )
    public class ComponentDisableAllCommand implements Action {

        @Argument ( index = 0, name = "bundle", required = false )
        @Completion ( BundleCompleter.class )
        String bundleSpec;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws BundleException {
            for ( ComponentDescriptionDTO component : fetchBundleComponents(this.bundleSpec) ) {
                this.session.getConsole().println(DISABLING_COMPONENT + component.name);
                try {
                    getScrService().disableComponent(component).getValue();
                }
                catch (
                    InvocationTargetException |
                    InterruptedException e ) {
                    e.printStackTrace(this.session.getConsole());
                }
            }
            return null;
        }

    }

}
