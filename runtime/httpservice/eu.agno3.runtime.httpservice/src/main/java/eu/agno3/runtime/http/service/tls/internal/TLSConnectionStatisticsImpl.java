/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Mar 2, 2017 by mbechler
 */
package eu.agno3.runtime.http.service.tls.internal;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;

import org.apache.log4j.Logger;

import eu.agno3.runtime.http.service.tls.TLSFailureReason;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( "nls" )
public class TLSConnectionStatisticsImpl implements TLSConnectionStatisticsInternal {

    /**
     * 
     */
    private static final String RECEIVED_FATAL_ALERT = "Received fatal alert: ";
    /**
     * 
     */
    private static final String PROTOCOL_DISABLED = "Client requested protocol ";

    private static final Logger log = Logger.getLogger(TLSConnectionStatisticsImpl.class);

    private final AtomicLong total = new AtomicLong();
    private final AtomicLong successful = new AtomicLong();
    private final AtomicLong failed = new AtomicLong();

    private static final Map<String, TLSFailureReason> ERROR_MAP = new HashMap<>();
    private static final Map<String, TLSFailureReason> ALERT_MAP = new HashMap<>();

    private final ConcurrentMap<TLSFailureReason, AtomicLong> failureReasons = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AtomicLong> disabledProtocols = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AtomicLong> successfulProtocols = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AtomicLong> successfulCiphers = new ConcurrentHashMap<>();


    static {
        ERROR_MAP.put("Insecure renegotiation is not allowed", TLSFailureReason.UNKNOWN);
        ERROR_MAP.put("No appropriate protocol (protocol is disabled or cipher suites are inappropriate)", TLSFailureReason.PROTOCOL_MISMATCH);
        ERROR_MAP.put("no cipher suites in common", TLSFailureReason.CIPHER_MISMATCH);

        ERROR_MAP.put("certificate verify format error", TLSFailureReason.BAD_PEER_CERT);
        ERROR_MAP.put("certificate verify message signature error", TLSFailureReason.BAD_PEER_CERT);

        ERROR_MAP.put("null cert chain", TLSFailureReason.NO_PEER_CERT);
        ERROR_MAP.put("client did not send certificate verify message", TLSFailureReason.NO_PEER_CERT);

        ERROR_MAP.put("Client initiated renegotiation is not allowed", TLSFailureReason.INSECURE_RENEGOTIATION);
        ERROR_MAP.put("The SCSV is present in a secure renegotiation", TLSFailureReason.INSECURE_RENEGOTIATION);
        ERROR_MAP.put("The SCSV is present in a insecure renegotiation", TLSFailureReason.INSECURE_RENEGOTIATION);
        ERROR_MAP.put("The renegotiation_info field is not empty", TLSFailureReason.INSECURE_RENEGOTIATION);
        ERROR_MAP.put("The renegotiation_info is present in a insecure renegotiation", TLSFailureReason.INSECURE_RENEGOTIATION);
        ERROR_MAP.put("Incorrect verify data in ClientHello renegotiation_info message", TLSFailureReason.INSECURE_RENEGOTIATION);
        ERROR_MAP.put("Inconsistent secure renegotiation indication", TLSFailureReason.INSECURE_RENEGOTIATION);
        ERROR_MAP.put("Renegotiation is not allowed", TLSFailureReason.INSECURE_RENEGOTIATION);

        // this indicates the use of a legacy hello
        ERROR_MAP.put("Failed to negotiate the use of secure renegotiation", TLSFailureReason.PROTOCOL_MISMATCH);

        ERROR_MAP.put("Unrecognized server name indication", TLSFailureReason.SNI_INVALID);
    }


    @Override
    public long getNumTotal () {
        return this.total.get();
    }


    @Override
    public long getNumSuccessful () {
        return this.successful.get();
    }


    @Override
    public long getNumFailed () {
        return this.failed.get();
    }


    @Override
    public Map<String, Long> getSuccessfulProtocols () {
        return makeMap(this.successfulProtocols);
    }


    @Override
    public Map<String, Long> getSuccessfulCiphers () {
        return makeMap(this.successfulCiphers);
    }


    @Override
    public Map<String, Long> getDisabledProtocols () {
        return makeMap(this.disabledProtocols);
    }


    @Override
    public Map<String, Long> getFailureReasons () {
        Map<String, Long> res = new HashMap<>();
        for ( TLSFailureReason r : TLSFailureReason.values() ) {
            AtomicLong val = this.failureReasons.get(r);
            if ( val != null ) {
                res.put(r.name(), val.get());
            }
            else {
                res.put(r.name(), 0L);
            }
        }
        return res;
    }


    /**
     * 
     */
    @Override
    public void trackSuccessful ( SSLSession engine ) {

        this.total.incrementAndGet();
        this.successful.incrementAndGet();

        String proto = engine.getProtocol();
        String cipher = engine.getCipherSuite();

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Have successful TLS connection (proto: %s cipher: %s)", proto, cipher)); //$NON-NLS-1$
        }

        incCounter(this.successfulCiphers, cipher);
        incCounter(this.successfulProtocols, proto);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.http.service.tls.internal.TLSConnectionStatisticsInternal#trackFailure(javax.net.ssl.SSLHandshakeException)
     */
    @Override
    public void trackFailure ( SSLHandshakeException e ) {
        this.total.incrementAndGet();
        this.failed.incrementAndGet();

        // unfortunately we don't get error details in a nice way
        TLSFailureReason r = ERROR_MAP.get(e.getMessage());

        if ( e.getMessage().startsWith(RECEIVED_FATAL_ALERT) ) {
            // in the case the client sends us an alert that tell's us what it does not like
            r = ALERT_MAP.get(e.getMessage().substring(RECEIVED_FATAL_ALERT.length()));
        }
        else if ( e.getMessage().startsWith(PROTOCOL_DISABLED) ) {
            int sep = e.getMessage().indexOf(' ', PROTOCOL_DISABLED.length() + 1);

            if ( sep > 0 ) {
                String proto = e.getMessage().substring(PROTOCOL_DISABLED.length(), sep);
                if ( log.isDebugEnabled() ) {
                    log.debug("Disabled protocol " + proto);
                }
                incCounter(this.disabledProtocols, proto);
                r = TLSFailureReason.PROTOCOL_MISMATCH;
            }
        }

        if ( r == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Don't have a mapping for " + e.getMessage());
            }
            r = TLSFailureReason.UNKNOWN;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Have failure with reason " + r); //$NON-NLS-1$
        }
        incCounter(this.failureReasons, r);
    }


    private static <T> long incCounter ( Map<T, AtomicLong> m, T key ) {
        AtomicLong l = m.get(key);
        if ( l != null ) {
            return l.incrementAndGet();
        }

        synchronized ( m ) {
            l = m.get(key);
            if ( l != null ) {
                return l.incrementAndGet();
            }
            m.put(key, new AtomicLong(1));
            return 1;
        }
    }


    private static <K> Map<K, Long> makeMap ( ConcurrentMap<K, AtomicLong> m ) {
        Map<K, Long> res = new HashMap<>();

        for ( K k : m.keySet() ) {
            AtomicLong l = m.get(k);
            if ( l != null ) {
                res.put(k, l.get());
            }
        }
        return res;
    }
}
