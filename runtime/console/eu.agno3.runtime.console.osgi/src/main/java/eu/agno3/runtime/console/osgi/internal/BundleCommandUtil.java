/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2013 by mbechler
 */
package eu.agno3.runtime.console.osgi.internal;


import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.osgi.framework.dto.BundleDTO;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;

import eu.agno3.runtime.console.osgi.BundleState;


/**
 * @author mbechler
 * 
 */
public final class BundleCommandUtil {

    /**
     * 
     */
    private static final String LAZY = "<<LAZY>>"; //$NON-NLS-1$
    static Map<BundleState, Ansi.Color> colorMap = new EnumMap<>(BundleState.class);


    static {
        BundleCommandUtil.colorMap.put(BundleState.UNINSTALLED, Color.RED);
        BundleCommandUtil.colorMap.put(BundleState.INSTALLED, Color.DEFAULT);
        BundleCommandUtil.colorMap.put(BundleState.RESOLVED, Color.CYAN);
        BundleCommandUtil.colorMap.put(BundleState.STARTING, Color.YELLOW);
        BundleCommandUtil.colorMap.put(BundleState.STOPPING, Color.YELLOW);
        BundleCommandUtil.colorMap.put(BundleState.ACTIVE, Color.GREEN);
    }


    private BundleCommandUtil () {}


    protected static boolean filterMatch ( Bundle b, String filter ) {
        return b.getSymbolicName().matches(filter);
    }


    static List<String> completeBundles ( BundleContext ctx ) {
        List<String> candidates = new ArrayList<>();

        for ( Bundle b : ctx.getBundles() ) {
            candidates.add(getBundleSpec(b));
        }

        return candidates;
    }


    /**
     * @param state
     * @return
     */
    static Color stateToColor ( BundleState state ) {
        return colorMap.get(state);
    }


    /**
     * @param out
     * @param sigs
     * @param b
     */
    static void printBundleFormattedVerbose ( PrintStream out, Bundle b, LazyBundleTracker lazyBundles ) {
        printBundleFormattedCompact(out, b, lazyBundles);

        BundleStartLevel bsl = b.adapt(BundleStartLevel.class);

        Ansi ansi = Ansi.ansi();

        ansi.bold().a("\t Last updated: ").boldOff(); //$NON-NLS-1$
        Date lastUpdatedDate = new Date(b.getLastModified());
        ansi.a(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault()).format(lastUpdatedDate));

        ansi.a(System.lineSeparator());
        ansi.bold().a("\t Location: ").boldOff(); //$NON-NLS-1$

        ansi.a(shortenLocationUrl(b));

        ansi.a(System.lineSeparator());
        ansi.bold().a("\t Start level: ").boldOff(); //$NON-NLS-1$
        ansi.a(String.format("%d", bsl.getStartLevel())); //$NON-NLS-1$

        out.println(ansi.toString());
    }


    /**
     * @return
     */
    private static String shortenLocationUrl ( Bundle b ) {
        try {
            URL locationUrl = new URL(b.getLocation());

            if ( "reference".equals(locationUrl.getProtocol()) ) { //$NON-NLS-1$
                URL fileUrl = new URL(locationUrl.getFile());

                if ( "file".equals(fileUrl.getProtocol()) ) { //$NON-NLS-1$
                    return fileUrl.getFile();
                }

                return locationUrl.getFile();
            }

            return b.getLocation();

        }
        catch ( MalformedURLException e ) {
            return b.getLocation();
        }
    }


    /**
     * @param out
     * @param sigs
     * @param b
     */
    static void printBundleFormattedCompact ( PrintStream out, Bundle b, LazyBundleTracker lazyBundles ) {

        Ansi ansi = Ansi.ansi();

        ansi.bold().a(String.format("% 3d ", b.getBundleId())).boldOff(); //$NON-NLS-1$

        if ( b.getState() == BundleState.STARTING.getStateCode() && lazyBundles.isLazyBundle(b) ) {
            ansi.fg(BundleCommandUtil.stateToColor(BundleState.fromStateCode(b.getState()))).a(String.format("%8s ", LAZY)).fg(Ansi.Color.DEFAULT); //$NON-NLS-1$
        }
        else {
            ansi.fg(BundleCommandUtil.stateToColor(BundleState.fromStateCode(b.getState())))
                    .a(String.format("%8s ", BundleState.fromStateCode(b.getState()).toString())).fg(Ansi.Color.DEFAULT); //$NON-NLS-1$
        }
        ansi.bold().a(b.getSymbolicName()).boldOff().a("-").a(BundleCommandUtil.formatVersion(b.getVersion())); //$NON-NLS-1$

        BundleRevision revision = b.adapt(BundleRevision.class);
        BundleWiring wiring = b.adapt(BundleWiring.class);

        if ( revision != null && wiring != null && ( revision.getTypes() & BundleRevision.TYPE_FRAGMENT ) != 0 ) {

            printBundleHost(ansi, wiring);

        }

        out.println(ansi.toString());
    }


    /**
     * @param ansi
     * @param wiring
     */
    private static void printBundleHost ( Ansi ansi, BundleWiring wiring ) {
        List<BundleWire> wirings = wiring.getRequiredWires(BundleRevision.HOST_NAMESPACE);
        BundleWire r = wirings.get(0);

        if ( r != null ) {
            BundleRevision fragmentHost = r.getProvider();
            ansi.fg(Ansi.Color.BLUE).a(String.format(
                " (F -> %s [%d])", //$NON-NLS-1$
                fragmentHost.getSymbolicName(),
                fragmentHost.getBundle().getBundleId())).fg(Ansi.Color.DEFAULT);
        }
        else {
            ansi.fg(Ansi.Color.BLUE).a(" (F)").fg(Ansi.Color.DEFAULT); //$NON-NLS-1$
        }
    }


    /**
     * @param b
     */
    static void printBundleFormatted ( PrintStream out, Bundle b, boolean verbose, boolean sigs, LazyBundleTracker lazyBundles ) {
        if ( verbose ) {
            printBundleFormattedVerbose(out, b, lazyBundles);
        }
        else {
            printBundleFormattedCompact(out, b, lazyBundles);
        }

        if ( verbose || sigs ) {
            printBundleSignatures(out, b);
        }
    }


    /**
     * @param out
     * @param b
     */
    private static void printBundleSignatures ( PrintStream out, Bundle b ) {

        Ansi ansi = Ansi.ansi();

        Map<X509Certificate, List<X509Certificate>> signatures = b.getSignerCertificates(Bundle.SIGNERS_TRUSTED);

        if ( signatures == null || signatures.isEmpty() ) {
            ansi.bold().fg(Color.RED).a("      Not signed by trusted issuer").boldOff().fg(Color.DEFAULT).newline(); //$NON-NLS-1$
        }
        else {

            for ( Entry<X509Certificate, List<X509Certificate>> entry : signatures.entrySet() ) {
                ansi.bold().a("     signed by ").boldOff().a(entry.getKey().getSubjectX500Principal().getName()).newline(); //$NON-NLS-1$
            }
        }
        out.print(ansi.toString());
    }


    static String formatVersion ( Version v ) {
        return v.toString();
    }


    static Bundle findBundle ( BundleContext ctx, String bundleId ) throws BundleException {

        if ( bundleId.matches("\\d+") ) { //$NON-NLS-1$
            Bundle b = ctx.getBundle(Integer.parseInt(bundleId));

            if ( b == null ) {
                throw new BundleException(String.format("Cannot find bundle by ID \'%s\'", bundleId)); //$NON-NLS-1$
            }

            return b;
        }

        return searchForBundle(ctx, bundleId);
    }


    /**
     * @param ctx
     * @param bundleId
     * @return
     * @throws BundleException
     */
    private static Bundle searchForBundle ( BundleContext ctx, String bundleId ) throws BundleException {
        Bundle[] bundles = ctx.getBundles();

        for ( Bundle b : bundles ) {
            if ( b.getSymbolicName().equals(bundleId) ) {
                return b;
            }

            if ( b.getLocation().equals(bundleId) ) {
                return b;
            }

            String namePlusVersion = getBundleSpec(b);
            if ( namePlusVersion.equals(bundleId) ) {
                return b;
            }
        }

        throw new BundleException(String.format("Cannot find bundle by identifier \'%s\'", bundleId)); //$NON-NLS-1$
    }


    /**
     * @param b
     * @return
     */
    private static String getBundleSpec ( Bundle b ) {
        return String.format("%s-%s", b.getSymbolicName(), formatVersion(b.getVersion())); //$NON-NLS-1$
    }


    /**
     * @param out
     * @param exportingBundle
     */
    public static void printBundleReference ( Ansi out, Bundle exportingBundle ) {

        out.bold().fgBright(Ansi.Color.BLACK).a(exportingBundle.getSymbolicName()).boldOff().fg(Ansi.Color.DEFAULT)
                .a(String.format("-%s [%d]", exportingBundle.getVersion().toString(), exportingBundle.getBundleId()));//$NON-NLS-1$
    }


    /**
     * @param out
     * @param exportingBundle
     */
    public static void printBundleReference ( Ansi out, BundleDTO exportingBundle ) {

        out.bold().fgBright(Ansi.Color.BLACK).a(exportingBundle.symbolicName).boldOff().fg(Ansi.Color.DEFAULT)
                .a(String.format("-%s [%d]", exportingBundle.version.toString(), exportingBundle.id));//$NON-NLS-1$
    }


    /**
     * Dump a property value which either is a scalar or an array
     * 
     * On scalars toString() will be called for representation
     * 
     * @param out
     * @param value
     */
    public static void dumpPropertyValue ( Ansi out, Object value ) {
        if ( value instanceof Object[] ) {
            Object[] values = (Object[]) value;
            out.a("[ "); //$NON-NLS-1$
            boolean first = true;
            for ( Object val : values ) {

                if ( !first ) {
                    out.a(","); //$NON-NLS-1$
                }
                first = false;

                out.a(val.toString());
            }
            out.a(" ]"); //$NON-NLS-1$
        }
        else {
            out.a(value.toString());
        }
    }

}
