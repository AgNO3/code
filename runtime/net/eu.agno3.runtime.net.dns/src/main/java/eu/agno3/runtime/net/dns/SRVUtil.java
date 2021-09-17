/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.03.2015 by mbechler
 */
package eu.agno3.runtime.net.dns;


import java.net.IDN;
import java.util.Hashtable;

import javax.naming.InvalidNameException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.spi.NamingManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public final class SRVUtil {

    private static final Logger log = Logger.getLogger(SRVUtil.class);
    private static final String SRV_REC = "SRV"; //$NON-NLS-1$
    private static final String SOA_REC = "SOA"; //$NON-NLS-1$
    private static final int DEFAULT_TTL = 600;


    /**
     * 
     */
    private SRVUtil () {}


    /**
     * @param domain
     * @param srvType
     * @return SRV entries
     * @throws NamingException
     */
    public static SRVEntries lookup ( String domain, String srvType ) throws NamingException {
        return lookup(domain, srvType, -1);
    }


    /**
     * @param domain
     * @param srvType
     * @param ttl
     * @return SRV entries
     * @throws NamingException
     */
    public static SRVEntries lookup ( String domain, String srvType, int ttl ) throws NamingException {
        if ( StringUtils.isBlank(domain) ) {
            throw new InvalidNameException("Name must not be empty"); //$NON-NLS-1$
        }

        String realDomain;
        try {
            realDomain = IDN.toASCII(domain);
        }
        catch ( IllegalArgumentException e ) {
            log.debug("Failed to create IDN representation", e); //$NON-NLS-1$
            throw new InvalidNameException("IDN encoding failed"); //$NON-NLS-1$
        }

        // cannot obtain the record TTLs through java API :(
        // use the zone SOA TTL instead
        int zoneTTL = ttl;
        if ( zoneTTL <= 0 ) {
            zoneTTL = getTTL(realDomain);
        }
        Attribute srv = getSRVAttributeValues(srvType + "." + realDomain); //$NON-NLS-1$
        NamingEnumeration<?> values = srv.getAll();
        SRVEntries entries = new SRVEntries();
        while ( values.hasMore() ) {
            Object val = values.next();
            if ( val == null ) {
                continue;
            }

            String[] parts = StringUtils.split((String) val, ' ');

            if ( parts == null || parts.length != 4 ) {
                log.warn("Unsupported record format " + val); //$NON-NLS-1$
                continue;
            }

            try {
                int prio = Integer.parseInt(parts[ 0 ]);
                int weight = Integer.parseInt(parts[ 1 ]);
                int port = Integer.parseInt(parts[ 2 ]);
                String serverName = parts[ 3 ];

                entries.add(new SRVEntry(serverName, prio, weight, port, zoneTTL));
            }
            catch ( IllegalArgumentException e ) {
                log.warn("Failed to parse record " + val, e); //$NON-NLS-1$
                continue;
            }

        }

        return entries;

    }


    private static Attributes getAttributes ( DirContext ctx, String name, String... attrs ) throws NamingException {
        if ( name.indexOf(':') >= 0 || name.indexOf('/') >= 0 ) {
            // make sure we never pass something that looks like an URL
            throw new InvalidNameException();
        }
        return ctx.getAttributes("dns:///" + name, attrs); //$NON-NLS-1$
    }


    /**
     * @param domain
     * @return
     * @throws NamingException
     */
    private static int getTTL ( String domain ) throws NamingException {
        if ( log.isDebugEnabled() ) {
            log.debug("Getting TTL for " + domain); //$NON-NLS-1$
        }

        DirContext ictx = getDirContext();
        Attributes attrs = getAttributes(ictx, domain, SOA_REC);

        Attribute attribute = attrs.get(SOA_REC);
        if ( attribute == null ) {
            throw new NameNotFoundException("No SOA record found"); //$NON-NLS-1$
        }

        String[] parts = StringUtils.split((String) attribute.get(), ' ');

        if ( parts == null || parts.length != 7 ) {
            log.warn("Unsupported SOA format " + attribute.get()); //$NON-NLS-1$
            return DEFAULT_TTL;
        }

        int ttl = Integer.parseInt(parts[ 6 ]);
        if ( ttl > 0 ) {
            return ttl;
        }

        return DEFAULT_TTL;
    }


    /**
     * @param name
     * @return
     * @throws NamingException
     * @throws NameNotFoundException
     */
    private static Attribute getSRVAttributeValues ( String name ) throws NamingException {

        if ( log.isDebugEnabled() ) {
            log.debug("Resolving SRV for " + name); //$NON-NLS-1$
        }

        DirContext ictx = getDirContext();
        Attributes attrs = getAttributes(ictx, name, SRV_REC);
        Attribute attribute = attrs.get(SRV_REC);
        if ( attribute == null ) {
            throw new NameNotFoundException("No SRV record found"); //$NON-NLS-1$
        }

        return attribute;
    }


    /**
     * @return
     * @throws NamingException
     */
    private static DirContext getDirContext () throws NamingException {
        return (DirContext) NamingManager.getURLContext("dns", new Hashtable<>(0)); //$NON-NLS-1$
    }

}
