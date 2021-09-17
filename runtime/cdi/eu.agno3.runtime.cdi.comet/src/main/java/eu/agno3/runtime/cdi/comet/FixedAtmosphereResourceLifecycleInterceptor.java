/*
 * Copyright 2015 Async-IO.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.agno3.runtime.cdi.comet;


import static org.atmosphere.cpr.ApplicationConfig.ATMOSPHERERESOURCE_INTERCEPTOR_METHOD;
import static org.atmosphere.cpr.ApplicationConfig.ATMOSPHERERESOURCE_INTERCEPTOR_TIMEOUT;
import static org.atmosphere.cpr.AtmosphereResource.TRANSPORT.POLLING;
import static org.atmosphere.cpr.AtmosphereResource.TRANSPORT.UNDEFINED;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceEventListenerAdapter.OnBroadcast;
import org.atmosphere.cpr.AtmosphereResourceEventListenerAdapter.OnClose;
import org.atmosphere.cpr.AtmosphereResourceImpl;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.atmosphere.util.Utils;


@SuppressWarnings ( "all" )
public class FixedAtmosphereResourceLifecycleInterceptor extends AtmosphereResourceLifecycleInterceptor {

    private String method = "GET";
    // For backward compat.
    private Integer timeoutInSeconds = -1;
    private static final Logger logger = Logger.getLogger(FixedAtmosphereResourceLifecycleInterceptor.class);
    private final boolean force;
    private long timeoutInMilli = -1;


    public FixedAtmosphereResourceLifecycleInterceptor () {
        this(false);
    }


    public FixedAtmosphereResourceLifecycleInterceptor ( boolean force ) {
        this.force = force;
    }


    @Override
    public void configure ( AtmosphereConfig config ) {
        String s = config.getInitParameter(ATMOSPHERERESOURCE_INTERCEPTOR_METHOD);
        if ( s != null ) {
            method = s;
        }

        s = config.getInitParameter(ATMOSPHERERESOURCE_INTERCEPTOR_TIMEOUT);
        if ( s != null ) {
            timeoutInSeconds = Integer.valueOf(s);
            timeoutInMilli = TimeUnit.MILLISECONDS.convert(timeoutInSeconds, TimeUnit.SECONDS);
        }
    }


    @Override
    public Action inspect ( AtmosphereResource r ) {

        switch ( r.transport() ) {
        case JSONP:
        case AJAX:
        case LONG_POLLING:
            r.resumeOnBroadcast(true);
            break;
        default:
            break;
        }
        return Action.CONTINUE;
    }


    @Override
    public void postInspect ( final AtmosphereResource r ) {

        if ( r.transport().equals(UNDEFINED) || Utils.webSocketMessage(r) || r.transport().equals(POLLING) )
            return;

        AtmosphereResourceImpl impl = AtmosphereResourceImpl.class.cast(r);
        if ( ( force || impl.getRequest(false).getMethod().equalsIgnoreCase(method) ) && !impl.action().equals(Action.CANCELLED)
                && impl.isInScope() ) {

            logger.trace("Marking AtmosphereResource {} for suspend operation", r.uuid());
            r.addEventListener(new OnBroadcast() {

                @Override
                public void onBroadcast ( AtmosphereResourceEvent event ) {
                    switch ( r.transport() ) {
                    case JSONP:
                    case AJAX:
                    case LONG_POLLING:
                    case CLOSE:
                        break;
                    default:
                        try {
                            r.getResponse().flushBuffer();
                        }
                        catch ( IOException e ) {
                            logger.trace("", e);
                        }
                        break;
                    }
                }
            }).addEventListener(new OnClose() {

                @Override
                public void onClose ( AtmosphereResourceEvent event ) {
                    logger.info("closed");
                    r.removeEventListeners();
                }

            }).suspend(timeoutInMilli);
        }
    }


    @Override
    public void destroy () {}

}
