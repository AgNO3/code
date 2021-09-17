//
//  ========================================================================
//  Copyright (c) 1995-2017 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//
package eu.agno3.runtime.redis.session.internal;


import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.session.AbstractSessionDataStore;
import org.eclipse.jetty.server.session.SessionContext;
import org.eclipse.jetty.server.session.SessionData;
import org.eclipse.jetty.server.session.SessionDataStore;
import org.redisson.api.MapOptions;
import org.redisson.api.RFuture;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.CompositeCodec;


/**
 * 
 * Mostly stolen from jetty's HazelcastSessionDataStore
 * 
 * @author mbechler
 *
 */
public class RedisSessionDataStore extends AbstractSessionDataStore implements SessionDataStore {

    private static final Logger log = Logger.getLogger(RedisSessionDataStore.class);
    private final RedissonClient client;
    private final String mapName = "session"; //$NON-NLS-1$
    private boolean diff = true;
    private RMap<String, SessionData> map;


    /**
     * @param client
     */
    public RedisSessionDataStore ( RedissonClient client ) {
        this.client = client;
        setSavePeriodSec(0);
        setGracePeriodSec(3600);
    }


    RMap<String, SessionData> getSessionDataMap () {
        return this.map;
    }


    @Override
    public SessionData load ( String id ) throws Exception {
        if ( log.isDebugEnabled() ) {
            log.debug("Loading session " + id); //$NON-NLS-1$
        }

        return getSessionDataMap().get(getCacheKey(id));
    }


    @Override
    public boolean exists ( String id ) throws Exception {
        return getSessionDataMap().containsKey(getCacheKey(id));
    }


    @Override
    public void doStore ( String id, SessionData data, long lastSaveTime ) throws Exception {
        if ( log.isDebugEnabled() ) {
            log.debug("Storing session " + id); //$NON-NLS-1$
        }
        String key = getCacheKey(id);
        RMap<String, SessionData> m = getSessionDataMap();

        if ( this.diff ) {
            SessionData old = m.get(key);

            if ( old != null ) {
                SessionDiffUtil.diff(data, old);
            }
        }

        long start = System.currentTimeMillis();

        log.debug("Put session data"); //$NON-NLS-1$
        RFuture<SessionData> put = m.putAsync(key, data);
        put.thenAccept( ( s ) -> {
            log.debug(String.format("Completed PUT in %d ms", ( System.currentTimeMillis() - start ))); //$NON-NLS-1$
        }).exceptionally( ( Throwable e ) -> {
            log.warn("Failed PUT", e); //$NON-NLS-1$
            return null;
        });
        log.debug(String.format("Put session data end %d ms", ( System.currentTimeMillis() - start ))); //$NON-NLS-1$
    }


    @Override
    public boolean delete ( String id ) throws Exception {
        if ( log.isDebugEnabled() ) {
            log.debug("Deleting session " + id); //$NON-NLS-1$
        }
        return getSessionDataMap().remove(getCacheKey(id)) != null;
    }


    @Override
    public void initialize ( SessionContext context ) throws Exception {
        this._context = context;
        MapOptions<String, SessionData> opts = MapOptions.defaults();
        Codec ser = new CompositeCodec(new StringCodec(), new SerializationCodec(this._context));
        this.map = this.client.getMap(this.mapName, ser, opts);
    }


    @Override
    public boolean isPassivating () {
        return true;
    }


    @Override
    public Set<String> doGetExpired ( Set<String> candidates ) {
        if ( candidates == null || candidates.isEmpty() ) {
            return Collections.emptySet();
        }
        long now = System.currentTimeMillis();
        return candidates.stream().filter(candidate -> {
            if ( log.isDebugEnabled() ) {
                log.debug("Checking expiry for candidate " + candidate); //$NON-NLS-1$
            }
            try {
                SessionData sd = load(candidate);

                // if the session no longer exists
                if ( sd == null ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug("Session does not exist in Redis " + candidate); //$NON-NLS-1$
                    }
                    return true;
                }
                if ( this._context.getWorkerName().equals(sd.getLastNode()) ) {
                    // we are its manager, add it to the expired set if it is expired now
                    if ( ( sd.getExpiry() > 0 ) && sd.getExpiry() <= now ) {
                        if ( log.isDebugEnabled() ) {
                            log.debug(String.format(
                                "Session %s managed by %s is expired", //$NON-NLS-1$
                                candidate,
                                this._context.getWorkerName()));
                        }
                        return true;
                    }
                }
                else {
                    // if we are not the session's manager, only expire it iff:
                    // this is our first expiryCheck and the session expired a long time ago
                    // or
                    // the session expired at least one graceperiod ago
                    if ( this._lastExpiryCheckTime <= 0 ) {
                        if ( ( sd.getExpiry() > 0 ) && sd.getExpiry() < ( now - ( 1000L * ( 3 * this._gracePeriodSec ) ) ) ) {
                            return true;
                        }
                    }
                    else {
                        if ( ( sd.getExpiry() > 0 ) && sd.getExpiry() < ( now - ( 1000L * this._gracePeriodSec ) ) ) {
                            return true;
                        }
                    }
                }
            }
            catch ( Exception e ) {
                log.warn(String.format("Error checking if candidate %s is expired so expire it", candidate), e); //$NON-NLS-1$
                return true;
            }
            return false;
        }).collect(Collectors.toSet());
    }


    private String getCacheKey ( String id ) {
        return "sess_" + this._context.getCanonicalContextPath() + '_' + this._context.getVhost() + '_' + id; //$NON-NLS-1$
    }

}
