/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 9, 2016 by mbechler
 */
package eu.agno3.runtime.eventlog.internal;


import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.Map;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.runtime.eventlog.Anonymizer;
import eu.agno3.runtime.util.config.ConfigUtil;
import eu.agno3.runtime.util.ip.IpUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = Anonymizer.class, configurationPid = "event.anonymize.ip" )
public class IPAnonymizer implements Anonymizer {

    private static final Logger log = Logger.getLogger(IPAnonymizer.class);

    private int v4length;
    private int v6length;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        parseConfig(ctx.getProperties());
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        parseConfig(ctx.getProperties());
    }


    /**
     * @param properties
     */
    private void parseConfig ( Dictionary<String, Object> cfg ) {
        this.v4length = ConfigUtil.parseInt(cfg, "v4prefixBytes", 2); //$NON-NLS-1$
        this.v6length = ConfigUtil.parseInt(cfg, "v6prefixBytes", 6); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Anonymizer#anonymize(java.lang.String, java.util.Map)
     */
    @Override
    public String anonymize ( String val, Map<String, String> opts ) {
        try {
            return doAnonymize(IpUtil.parse(val));
        }
        catch ( IllegalArgumentException e ) {
            log.debug("Failed to parse ip address", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * @param data
     * @throws UnknownHostException
     */
    private String doAnonymize ( short[] data ) {
        int prefixLen = getPrefixLength(data);
        for ( int i = prefixLen; i < data.length; i++ ) {
            data[ i ] = 0;
        }
        return IpUtil.toString(data) + '/' + ( prefixLen * 8 );
    }


    /**
     * @param data
     * @return
     */
    int getPrefixLength ( short[] data ) {
        if ( data.length == 4 ) {
            // v4
            return this.v4length;
        }
        else if ( data.length == 16 ) {
            // v6
            return this.v6length;
        }
        // unknown
        return 0;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Anonymizer#getId()
     */
    @Override
    public String getId () {
        return "ip"; //$NON-NLS-1$
    }

}
