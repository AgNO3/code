/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.06.2013 by mbechler
 */
package eu.agno3.runtime.console.system;


import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.console.Session;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.fusesource.jansi.Ansi;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.console.CommandProvider;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    CommandProvider.class
}, immediate = true )
@SuppressWarnings ( "restriction" )
public class SystemCommandProvider implements CommandProvider {

    private static final Logger log = Logger.getLogger(SystemCommandProvider.class);


    static String formatBytes ( long bytes ) {
        long amount = bytes / 1024 / 1024;
        String unit = "MB"; //$NON-NLS-1$
        return String.format("% 6d %s", amount, unit); //$NON-NLS-1$
    }


    /**
     * @return the log
     */
    Logger getLog () {
        return log;
    }

    /**
     * Shutdown the running runtime
     * 
     * @author mbechler
     *
     */
    @Command ( scope = "sys", name = "shutdown", description = "Shutdown service" )
    public static class ShutdownCommand implements Action {

        /**
         * {@inheritDoc}
         *
         * @see org.apache.karaf.shell.api.action.Action#execute()
         */

        @Override
        public Object execute () throws Exception {
            EclipseStarter.getSystemBundleContext();
            EclipseStarter.shutdown();
            System.exit(0);
            return null;
        }

    }

    /**
     * List memory usage
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "sys", name = "memory", description = "Show memory usage" )
    public static class MemoryCommand implements Action {

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        /**
         * 
         * {@inheritDoc}
         *
         * @see org.apache.karaf.shell.api.action.Action#execute()
         */
        @Override
        public Object execute () {

            Runtime r = Runtime.getRuntime();

            Ansi out = Ansi.ansi();

            long used = r.totalMemory() - r.freeMemory();
            long free = r.freeMemory();
            long total = r.totalMemory();
            long max = r.maxMemory();

            out.bold().a("Used:  ").a(formatBytes(used)).boldOff().a(System.lineSeparator()); //$NON-NLS-1$
            out.bold().a("Free:  ").a(formatBytes(free)).boldOff().a(System.lineSeparator()); //$NON-NLS-1$
            out.bold().a("Total: ").a(formatBytes(total)).boldOff().a(System.lineSeparator()); //$NON-NLS-1$

            if ( max != Long.MAX_VALUE ) {
                out.bold().a("Limit: ").a(formatBytes(max)).boldOff().a(System.lineSeparator()); //$NON-NLS-1$
            }

            for ( MemoryPoolMXBean mx : ManagementFactory.getMemoryPoolMXBeans() ) {
                out.bold().a(String.format("%-20s:", mx.getName())).a(formatBytes(mx.getUsage().getUsed())).boldOff().newline(); //$NON-NLS-1$
            }

            this.session.getConsole().print(out.toString());

            return null;
        }
    }

    /**
     * Run the garbage collector
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "sys", name = "gc", description = "Run garbage collector" )
    public static class GCCommand implements Action {

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        /**
         * 
         * {@inheritDoc}
         *
         * @see org.apache.karaf.shell.api.action.Action#execute()
         */
        @Override
        public Object execute () {
            Runtime r = Runtime.getRuntime();

            this.session.getConsole().println("Running Garbage Collector..."); //$NON-NLS-1$

            r.gc();
            r.runFinalization();

            return null;
        }

    }

    /**
     * List threads
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "sys", name = "threads", description = "List threads" )
    public static class ThreadsCommand implements Action {

        @Option ( name = "-verbose", aliases = "-v" )
        boolean verbose;

        @Argument ( index = 0, name = "filter", required = false )
        String filter;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        /**
         * 
         * {@inheritDoc}
         *
         * @see org.apache.karaf.shell.api.action.Action#execute()
         */
        @Override
        public Object execute () {
            Map<Thread, StackTraceElement[]> threadSet = Thread.getAllStackTraces();

            Ansi out = Ansi.ansi();

            SortedSet<Thread> sorted = new TreeSet<>(new Comparator<Thread>() {

                @Override
                public int compare ( Thread o1, Thread o2 ) {
                    return Long.compare(o1.getId(), o2.getId());
                }
            });

            sorted.addAll(threadSet.keySet());

            for ( Thread t : sorted ) {

                if ( this.filter != null && !t.getName().startsWith(this.filter) ) {
                    continue;
                }

                printThread(threadSet, out, t);
            }

            this.session.getConsole().print(out.toString());

            return null;
        }


        /**
         * @param threadSet
         * @param out
         * @param t
         */
        private void printThread ( Map<Thread, StackTraceElement[]> threadSet, Ansi out, Thread t ) {
            if ( t == null ) {
                return;
            }
            out.bold().a(String.format("%-10s %-14s", t.getThreadGroup() != null ? t.getThreadGroup().getName() : null, t.getState())).boldOff(); //$NON-NLS-1$
            out.a(String.format(" #%02d - %s (prio: %d)", t.getId(), t.getName(), t.getPriority())); //$NON-NLS-1$

            out.a(" ["); //$NON-NLS-1$

            if ( t.isAlive() ) {
                out.a("L"); //$NON-NLS-1$
            }
            if ( t.isDaemon() ) {
                out.a("D"); //$NON-NLS-1$
            }
            if ( t.isInterrupted() ) {
                out.a("I"); //$NON-NLS-1$
            }

            out.a("]"); //$NON-NLS-1$

            if ( this.verbose ) {

                StackTraceElement[] st = threadSet.get(t);

                if ( st.length > 0 ) {
                    out.a(System.lineSeparator());
                    out.a(String.format(" @%d - %s", st.length, st[ st.length - 1 ].toString())); //$NON-NLS-1$

                }
            }

            out.a(System.lineSeparator());
        }
    }

    /**
     * Show system properties
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "sys", name = "properties", description = "Show system properties" )
    public static class PropertiesCommand implements Action {

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        /**
         * 
         * {@inheritDoc}
         *
         * @see org.apache.karaf.shell.api.action.Action#execute()
         */
        @Override
        public Object execute () {

            Ansi out = Ansi.ansi();

            for ( Object property : System.getProperties().keySet() ) {

                out.bold().a(property.toString()).a(": ").boldOff(); //$NON-NLS-1$

                Object value = System.getProperty((String) property);

                if ( value == null ) {
                    out.a("<NULL>"); //$NON-NLS-1$
                }
                else {
                    out.a(value.toString());
                }

                out.a(System.lineSeparator());
            }

            this.session.getConsole().print(out);
            return null;
        }
    }

}
