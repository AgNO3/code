/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.02.2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.hostconfig.storage;


import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;

import eu.agno3.orchestrator.config.hostconfig.storage.LocalMountEntryMutable;
import eu.agno3.orchestrator.server.webgui.components.OuterWrapper;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.system.info.storage.fs.DataFileSystem;
import eu.agno3.orchestrator.system.info.storage.volume.Volume;


/**
 * @author mbechler
 *
 */
public class LocalDiskReturnListener {

    private static final Logger log = Logger.getLogger(LocalDiskReturnListener.class);
    private OuterWrapper<?> wrapper;


    /**
     * @param wrapper
     */
    public LocalDiskReturnListener ( OuterWrapper<?> wrapper ) {
        this.wrapper = wrapper;
    }


    public void picked ( SelectEvent ev ) {
        if ( this.wrapper != null && ev != null && ev.getObject() instanceof Volume ) {
            Volume v = (Volume) ev.getObject();

            if ( ! ( v.getFileSystem() instanceof DataFileSystem ) ) {
                return;
            }
            DataFileSystem fs = (DataFileSystem) v.getFileSystem();

            OuterWrapper<?> outerWrapper = this.wrapper.get("urn:agno3:objects:1.0:hostconfig:storage:mount:local"); //$NON-NLS-1$
            if ( outerWrapper == null || outerWrapper.getEditor() == null ) {
                return;
            }

            try {

                LocalMountEntryMutable current = (LocalMountEntryMutable) outerWrapper.getEditor().getCurrent();

                if ( log.isDebugEnabled() ) {
                    log.debug("Have picked " + v.getDevice()); //$NON-NLS-1$
                    log.debug("Label is " + fs.getLabel()); //$NON-NLS-1$
                    log.debug("UUID is " + fs.getUuid()); //$NON-NLS-1$
                }

                if ( StringUtils.isBlank(current.getAlias()) ) {
                    String alias = v.getDevice();
                    int lastSlash = alias.lastIndexOf('/');
                    if ( lastSlash >= 0 && lastSlash < alias.length() ) {
                        alias = alias.substring(lastSlash + 1);
                    }
                    current.setAlias(alias);
                }

                if ( !StringUtils.isBlank(fs.getLabel()) ) {
                    current.setMatchLabel(fs.getLabel());
                    current.setMatchUuid(null);
                }
                else {
                    current.setMatchLabel(null);
                    current.setMatchUuid(fs.getUuid());
                }
            }
            catch ( Exception e ) {
                ExceptionHandler.handle(e);
                return;
            }

        }
    }

}
