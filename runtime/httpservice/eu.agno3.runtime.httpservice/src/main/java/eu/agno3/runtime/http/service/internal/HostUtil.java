/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 18, 2016 by mbechler
 */
package eu.agno3.runtime.http.service.internal;


import java.util.regex.Pattern;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public final class HostUtil {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9\\-\\.]+$", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
    private static final Pattern PORT_PATTERN = Pattern.compile("^[0-9]+$"); //$NON-NLS-1$

    /**
     * These do not really have to make sure to only match valid addresses, we only care that
     * no strange characters are present.
     */
    private static final Pattern V4_PATTERN = Pattern.compile("^([0-9]{1,3}\\.){3}[0-9]{1,3}$"); //$NON-NLS-1$
    private static final Pattern V6_PATTERN = Pattern.compile("^([0-9a-f]{0,4}:){1,7}[0-9a-f]{1,4}$"); //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(HostUtil.class);


    /**
     * 
     */
    private HostUtil () {}


    /**
     * @param serverName
     * @return whether validation was successful
     */
    public static boolean validateHostHeader ( String serverName ) {
        return validateHostHeader(serverName, false);
    }


    /**
     * @param serverName
     * @param allowPort
     *            allow a port specification to be present
     * @return whether validation was successful
     */
    public static boolean validateHostHeader ( String serverName, boolean allowPort ) {
        if ( serverName == null || serverName.isEmpty() ) {
            return true;
        }

        if ( serverName.length() >= 2 && serverName.charAt(0) == '[' ) {
            // v6 address
            if ( !allowPort || serverName.charAt(serverName.length() - 1) == ']' ) {
                // no port
                return validateV6(serverName.substring(1, serverName.length() - 1));
            }

            int portSep = serverName.lastIndexOf(':');
            if ( portSep < 1 || serverName.charAt(portSep - 1) != ']' ) {
                return false;
            }

            if ( !validatePort(serverName.substring(portSep + 1)) ) {
                return false;
            }
            return validateV6(serverName.substring(1, portSep - 1));
        }

        int portSep = serverName.lastIndexOf(':');
        String serverNameWithoutPort;
        if ( allowPort && portSep > 0 ) {
            if ( !validatePort(serverName.substring(portSep + 1)) ) {
                return false;
            }
            serverNameWithoutPort = serverName.substring(0, portSep - 1);
        }
        else {
            serverNameWithoutPort = serverName;
        }

        if ( Character.isDigit(serverNameWithoutPort.charAt(serverNameWithoutPort.length() - 1)) ) {
            // v4 address
            return validateV4(serverNameWithoutPort);
        }

        // dns name
        return validateDNS(serverNameWithoutPort);
    }


    /**
     * @param port
     * @return
     */
    private static boolean validatePort ( String port ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Checking port " + port); //$NON-NLS-1$
        }
        return PORT_PATTERN.matcher(port).matches();
    }


    /**
     * @param addr
     */
    private static boolean validateV4 ( String addr ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Checking V4 " + addr); //$NON-NLS-1$
        }
        return V4_PATTERN.matcher(addr).matches();
    }


    /**
     * @param serverName
     * @return
     * @throws ServletException
     */
    private static boolean validateDNS ( String serverName ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Checking name " + serverName); //$NON-NLS-1$
        }
        return NAME_PATTERN.matcher(serverName).matches();
    }


    /**
     * @param addr
     */
    private static boolean validateV6 ( String addr ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Checking V6 " + addr); //$NON-NLS-1$
        }
        return V6_PATTERN.matcher(addr).matches();
    }
}
