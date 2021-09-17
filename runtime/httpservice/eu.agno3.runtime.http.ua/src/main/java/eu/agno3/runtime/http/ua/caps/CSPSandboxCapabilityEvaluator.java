/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2015 by mbechler
 */
package eu.agno3.runtime.http.ua.caps;


import org.apache.log4j.Logger;

import eu.agno3.runtime.http.ua.CapabilityEvaluator;

import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.VersionNumber;


/**
 * @author mbechler
 *
 */
public class CSPSandboxCapabilityEvaluator implements CapabilityEvaluator {

    private static final Logger log = Logger.getLogger(CSPSandboxCapabilityEvaluator.class);


    /**
     * @param ua
     * @return whether CSP is supported by browser
     */
    @Override
    public boolean hasCapability ( ReadableUserAgent ua, String raw ) {
        VersionNumber versionNumber = ua.getVersionNumber();
        switch ( ua.getFamily() ) {
        case FIREFOX:
            return firefoxHasCapability(versionNumber);
        case IE:
        case IE_MOBILE:
        case IE_RSS_READER:
            return internetExplorerHasCapability(versionNumber);
        case CHROME:
        case CHROMIUM:
            return chromeHasCapability(versionNumber);
        case SAFARI:
            return safariHasCapability(versionNumber);
        case OPERA:
            return operaHasCapability(versionNumber);

        default:
            if ( log.isDebugEnabled() ) {
                log.debug("Unsupported user agent " + ua); //$NON-NLS-1$
            }
            return false;
        }
    }


    /**
     * @param versionNumber
     * @return
     */
    private static boolean operaHasCapability ( VersionNumber versionNumber ) {
        return Integer.parseInt(versionNumber.getMajor()) >= 15;
    }


    /**
     * @param versionNumber
     * @return
     */
    private static boolean safariHasCapability ( VersionNumber versionNumber ) {
        return Integer.parseInt(versionNumber.getMajor()) > 5
                || ( Integer.parseInt(versionNumber.getMajor()) == 5 && Integer.parseInt(versionNumber.getMinor()) >= 1 );
    }


    /**
     * @param versionNumber
     * @return
     */
    private static boolean chromeHasCapability ( VersionNumber versionNumber ) {
        return Integer.parseInt(versionNumber.getMajor()) >= 14;
    }


    /**
     * @param versionNumber
     * @return
     */
    private static boolean internetExplorerHasCapability ( VersionNumber versionNumber ) {
        return Integer.parseInt(versionNumber.getMajor()) >= 10;
    }


    /**
     * @param versionNumber
     * @return
     */
    private static boolean firefoxHasCapability ( VersionNumber versionNumber ) {
        return Integer.parseInt(versionNumber.getMajor()) >= 50;
    }
}
