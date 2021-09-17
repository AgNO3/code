/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 29, 2016 by mbechler
 */
package eu.agno3.runtime.http.service.session.internal;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.EnumSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.runtime.http.service.session.SessionBindingGenerator;
import eu.agno3.runtime.util.config.ConfigUtil;
import eu.agno3.runtime.util.ip.IpUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = SessionBindingGenerator.class, configurationPid = "session.binding" )
public class SessionBindingGeneratorImpl implements SessionBindingGenerator {

    private static final Logger log = Logger.getLogger(SessionBindingGeneratorImpl.class);

    enum IncludeData {
        NONE, TLS_SESSION, HOST
    }

    private Set<IncludeData> include = EnumSet.noneOf(IncludeData.class);
    private int v4prefix;
    private int v6prefix;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        parseConfig(ctx.getProperties());
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        parseConfig(ctx.getProperties());
    }


    /**
     * @param cfg
     */
    private void parseConfig ( Dictionary<String, Object> cfg ) {
        Set<String> typesStr = ConfigUtil.parseStringSet(cfg, "includeData", Collections.EMPTY_SET); //$NON-NLS-1$
        Set<IncludeData> types = EnumSet.noneOf(IncludeData.class);
        for ( String type : typesStr ) {
            try {
                types.add(IncludeData.valueOf(type.trim()));
            }
            catch ( IllegalArgumentException e ) {
                log.error("Invalid data type " + type, e); //$NON-NLS-1$
            }
        }
        this.include = types;
        this.v4prefix = ConfigUtil.parseInt(cfg, "v4prefix", 24); //$NON-NLS-1$
        this.v6prefix = ConfigUtil.parseInt(cfg, "v6prefix", 64); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.http.service.session.SessionBindingGenerator#generateHash(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public byte[] generateHash ( HttpServletRequest httpReq ) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            if ( this.include.contains(IncludeData.TLS_SESSION) ) {
                byte[] tlsSessionId = (byte[]) httpReq.getAttribute("agno3.tlsSessionIdHash"); //$NON-NLS-1$
                if ( tlsSessionId != null ) {
                    bos.write(tlsSessionId);
                }
            }

            if ( this.include.contains(IncludeData.HOST) ) {
                String remoteAddr = httpReq.getRemoteAddr();
                try {
                    short[] addr = IpUtil.parse(remoteAddr);
                    bos.write(truncateAddress(addr));
                }
                catch ( IllegalArgumentException e ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug("Failed to parse ip address " + remoteAddr, e); //$NON-NLS-1$
                    }
                }
            }
            return bos.toByteArray();
        }
        catch ( IOException e ) {
            log.error("Failed to produce session hash", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * @param addr
     * @return
     */
    private byte[] truncateAddress ( short[] addr ) {
        int len;
        switch ( addr.length ) {
        case 16:
            len = this.v6prefix;
        case 4:
            len = this.v4prefix;
        default:
            len = 0;
        }
        byte[] truncated = new byte[len];
        for ( int i = 0; i < len; i++ ) {
            truncated[ i ] = (byte) addr[ i ];
        }
        return truncated;
    }

}
