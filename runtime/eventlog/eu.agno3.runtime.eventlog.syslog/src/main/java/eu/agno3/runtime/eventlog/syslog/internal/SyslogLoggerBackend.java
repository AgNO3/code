/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.06.2015 by mbechler
 */
package eu.agno3.runtime.eventlog.syslog.internal;


import java.nio.charset.Charset;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.graylog2.syslog4j.Syslog;
import org.graylog2.syslog4j.SyslogConstants;
import org.graylog2.syslog4j.SyslogIF;
import org.graylog2.syslog4j.SyslogPoolConfigIF;
import org.graylog2.syslog4j.impl.net.AbstractNetSyslogConfigIF;
import org.graylog2.syslog4j.impl.net.tcp.pool.PooledTCPNetSyslogConfig;
import org.graylog2.syslog4j.impl.net.udp.UDPNetSyslogConfig;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.runtime.eventlog.Event;
import eu.agno3.runtime.eventlog.EventLoggerBackend;
import eu.agno3.runtime.util.config.ConfigUtil;
import eu.agno3.runtime.util.net.LocalHostUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = SyslogLoggerBackend.class, configurationPolicy = ConfigurationPolicy.REQUIRE, configurationPid = "eventLog.syslog" )
public class SyslogLoggerBackend implements EventLoggerBackend {

    private static final Logger log = Logger.getLogger(SyslogLoggerBackend.class);

    private String logName;
    private SyslogIF syslog;
    private int logLevel;
    private Charset charset = Charset.forName("UTF-8"); //$NON-NLS-1$

    private Set<String> excludeStreams;
    private Set<String> includeStreams;

    private AbstractNetSyslogConfigIF config;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        parseConfig(ctx);
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        parseConfig(ctx);
    }


    /**
     * @param ctx
     */
    private void parseConfig ( ComponentContext ctx ) {
        this.logName = ConfigUtil.parseString(
            ctx.getProperties(),
            "logName", //$NON-NLS-1$
            "eventLog"); //$NON-NLS-1$

        this.includeStreams = ConfigUtil.parseStringSet(ctx.getProperties(), "includeStreams", null); //$NON-NLS-1$
        this.excludeStreams = ConfigUtil.parseStringSet(ctx.getProperties(), "excludeStreams", null); //$NON-NLS-1$

        this.logLevel = ConfigUtil.parseInt(ctx.getProperties(), "logLevel", SyslogConstants.LEVEL_NOTICE); //$NON-NLS-1$
        String charsetName = ConfigUtil.parseString(ctx.getProperties(), "logCharset", null); //$NON-NLS-1$
        if ( !StringUtils.isBlank(charsetName) ) {
            this.charset = Charset.forName(charsetName);
        }

        this.config = this.makeConfig(ctx.getProperties());
        if ( this.config != null ) {
            this.syslog = Syslog.createInstance(this.logName, this.config);
        }
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        if ( this.syslog != null ) {
            Syslog.destroyInstance(this.syslog);
            this.syslog = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventLoggerBackend#reset()
     */
    @Override
    public synchronized void reset () {
        if ( this.syslog != null ) {
            SyslogIF oldinstance = this.syslog;
            this.syslog = Syslog.createInstance(this.logName, this.config);
            Syslog.destroyInstance(oldinstance);
        }
    }


    /**
     * @param props
     * @return
     */
    private AbstractNetSyslogConfigIF makeConfig ( Dictionary<String, Object> props ) {

        String type = ConfigUtil.parseString(
            props,
            "type", //$NON-NLS-1$
            "udp"); //$NON-NLS-1$

        AbstractNetSyslogConfigIF cfg = makeConfig(type, props);

        if ( cfg == null ) {
            return null;
        }

        String localName = ConfigUtil.parseString(props, "localName", null); //$NON-NLS-1$
        if ( StringUtils.isBlank(localName) ) {
            localName = LocalHostUtil.guessPrimaryHostName();
        }

        cfg.setFacility(ConfigUtil.parseString(
            props,
            "facility", //$NON-NLS-1$
            "LOCAL0")); //$NON-NLS-1$
        cfg.setSendLocalName(true);
        cfg.setLocalName(localName);

        String host = ConfigUtil.parseString(props, "host", null); //$NON-NLS-1$
        int port = ConfigUtil.parseInt(props, "port", -1); //$NON-NLS-1$
        if ( StringUtils.isBlank(host) || port <= 0 ) {
            log.error("Host or port not given"); //$NON-NLS-1$
            return null;
        }

        return cfg;
    }


    /**
     * @param type
     * @param cfg
     * @return
     */
    protected AbstractNetSyslogConfigIF makeConfig ( String type, Dictionary<String, Object> cfg ) {
        switch ( type ) {
        case "udp": //$NON-NLS-1$
            return new UDPNetSyslogConfig();
        case "tcp": //$NON-NLS-1$
            PooledTCPNetSyslogConfig pcfg = new PooledTCPNetSyslogConfig();
            makePoolConfig(cfg, pcfg);
            return pcfg;
        default:
            return null;
        }
    }


    /**
     * @param cfg
     * @param pcfg
     */
    protected void makePoolConfig ( Dictionary<String, Object> cfg, SyslogPoolConfigIF pcfg ) {
        pcfg.setMaxIdle(ConfigUtil.parseInt(cfg, "poolMaxIdle", 4)); //$NON-NLS-1$
        pcfg.setMinIdle(ConfigUtil.parseInt(cfg, "poolMinIdle", 0)); //$NON-NLS-1$
        pcfg.setMaxActive(ConfigUtil.parseInt(cfg, "poolMaxActive", 5)); //$NON-NLS-1$
        pcfg.setMaxWait(ConfigUtil.parseInt(cfg, "poolMaxWait", 1000)); //$NON-NLS-1$
        pcfg.setWhenExhaustedAction((byte) 1);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventLoggerBackend#getPriority()
     */
    @Override
    public int getPriority () {
        return 0;
    }


    @Override
    public Future<?> log ( Event ev, byte[] bytes ) {
        SyslogIF sl = this.syslog;
        if ( sl == null ) {
            return null;
        }
        sl.log(this.logLevel, new String(bytes, this.charset));
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventLoggerBackend#bulkLog(java.util.List, java.util.Map)
     */
    @Override
    public Future<?> bulkLog ( List<Event> evs, Map<Event, byte[]> data ) {
        for ( Event ev : evs ) {
            this.log(ev, data.get(ev));
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventLoggerBackend#runMaintenance()
     */
    @Override
    public void runMaintenance () {

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventLoggerBackend#getExcludeStreams()
     */
    @Override
    public Set<String> getExcludeStreams () {
        return this.excludeStreams;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventLoggerBackend#getIncludeStreams()
     */
    @Override
    public Set<String> getIncludeStreams () {
        return this.includeStreams;
    }

}
