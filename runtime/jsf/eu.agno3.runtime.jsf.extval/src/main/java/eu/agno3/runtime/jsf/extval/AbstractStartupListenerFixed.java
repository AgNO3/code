/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package eu.agno3.runtime.jsf.extval;


import java.util.HashSet;
import java.util.Set;

import javax.faces.event.PhaseEvent;

import org.apache.log4j.Logger;
import org.apache.myfaces.extensions.validator.core.startup.AbstractStartupListener;
import org.apache.myfaces.extensions.validator.util.JsfUtils;


/**
 * The default implementation leaks references to the listeners
 * 
 * @author mbechler
 * 
 */
public abstract class AbstractStartupListenerFixed extends AbstractStartupListener {

    /**
     * 
     */
    private static final long serialVersionUID = -4568630371396663347L;

    private static final Logger log = Logger.getLogger(AbstractStartupListenerFixed.class);

    // don't remove - it's a fallback if there is a problem with deregistration
    // target: don't process init logic more than once
    private static Set<Class<?>> initializedListeners = new HashSet<>();


    /**
     * Is responsible for executing the one time only logic. Before the logic is performed (init method), the start-up
     * listener has the chance of putting a configuration object in place in the initModuleConfig method.
     * Startup listeners can be deactivated by an initialization parameter in the web.xml file and are deregistered from
     * the JSF system. If this fails, a fallback system is in place so that the logic can't be executed more then once.
     * 
     * @param event
     *            Jsf Phase Event info.
     */
    @Override
    public void beforePhase ( PhaseEvent event ) {
        synchronized ( AbstractStartupListener.class ) {
            if ( !initializedListeners.contains(getClass()) ) {
                try {
                    initializeListener();
                }
                catch ( Exception e ) {
                    log.warn(String.format("an exception occurred while deregistering the phase-listener %s", //$NON-NLS-1$
                        getClass().getName()), e);
                }
                finally {
                    initializedListeners.add(getClass());
                }
            }
        }
    }


    /**
     * 
     */
    private void initializeListener () {
        if ( log.isDebugEnabled() ) {
            log.debug("start init of " + getClass().getName()); //$NON-NLS-1$
        }

        try {
            if ( !isStartupListenerDeactivated() ) {
                initModuleConfig();

                initProjectStageResolver();

                init();
            }
            else {
                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("init of %s deactivated", getClass().getName())); //$NON-NLS-1$
                }
            }

            if ( log.isDebugEnabled() ) {
                log.debug(String.format("init of %s finished", getClass().getName())); //$NON-NLS-1$
            }
        }
        finally {
            JsfUtils.deregisterPhaseListener(this);
        }
    }


    /**
     * 
     */
    public static void destroy () {
        log.debug("Clearing listeners"); //$NON-NLS-1$
        initializedListeners.clear();
    }
}
