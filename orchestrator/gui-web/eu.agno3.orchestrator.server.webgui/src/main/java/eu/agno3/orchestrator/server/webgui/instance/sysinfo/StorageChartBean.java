/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.06.2017 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.instance.sysinfo;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.webbeans.util.StringUtil;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.system.info.storage.StorageInformation;
import eu.agno3.orchestrator.system.info.storage.drive.Drive;
import eu.agno3.orchestrator.system.info.storage.fs.DataFileSystem;
import eu.agno3.orchestrator.system.info.storage.fs.FileSystem;
import eu.agno3.orchestrator.system.info.storage.volume.Volume;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "agentSysInfoStorageChartModel" )
public class StorageChartBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2090313471909334023L;
    private List<FilesystemEntry> entries;

    @Inject
    private AgentSysInfoContextBean sysInfo;


    /**
     * @return the entries
     */
    public List<FilesystemEntry> getEntries () {
        if ( this.entries == null ) {
            try {
                this.entries = makeFilesystemEntries();
            }
            catch ( Exception e ) {
                ExceptionHandler.handle(e);
                return Collections.EMPTY_LIST;
            }
        }
        return this.entries;
    }


    /**
     * @return
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    private List<FilesystemEntry> makeFilesystemEntries () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        List<FilesystemEntry> fsentries = new ArrayList<>();
        StorageInformation storage = this.sysInfo.getStorageInformation();
        for ( Drive d : storage.getDrives() ) {
            for ( Volume v : d.getVolumes() ) {
                FileSystem fs = v.getFileSystem();
                if ( ! ( fs instanceof DataFileSystem ) ) {
                    continue;
                }

                DataFileSystem dfs = (DataFileSystem) fs;

                Long usableSpace = dfs.getUsableSpace();
                Long totalSpace = dfs.getTotalSpace();
                if ( usableSpace == null || totalSpace == null ) {
                    continue;
                }

                String name = d.getAssignedAlias();
                if ( StringUtil.isBlank(name) ) {
                    int lastsep = v.getDevice().lastIndexOf('/');
                    if ( lastsep > 0 ) {
                        name = v.getDevice().substring(lastsep + 1);
                    }
                    else {
                        name = v.getDevice();
                    }
                }

                if ( "system".equals(name) && !StringUtil.isBlank(v.getLabel()) ) { //$NON-NLS-1$
                    name = name + '-' + v.getLabel();
                }

                float free = (float) ( usableSpace.doubleValue() / totalSpace.doubleValue() );
                fsentries.add(new FilesystemEntry(name, free, usableSpace, totalSpace));
            }
        }

        Collections.sort(fsentries, new Comparator<FilesystemEntry>() {

            @Override
            public int compare ( FilesystemEntry o1, FilesystemEntry o2 ) {
                return o1.getName().compareTo(o2.getName());
            }

        });

        return fsentries;
    }

    public static class FilesystemEntry implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = -7372711444848179394L;

        private String name;
        private float freePercent;
        private long usableSpace;
        private long totalSpace;
        private float usedPercent;

        private String barStyle;

        private String barStyleClass;


        /**
         * @param name
         * @param free
         * @param usableSpace
         * @param totalSpace
         */
        public FilesystemEntry ( String name, float free, long usableSpace, long totalSpace ) {
            this.name = name;
            this.freePercent = free;
            this.usedPercent = 1 - free;
            this.usableSpace = usableSpace;
            this.totalSpace = totalSpace;

            String stclass = "bg-color-ok"; //$NON-NLS-1$
            if ( this.usedPercent > 0.9 ) {
                stclass = "bg-color-failure"; //$NON-NLS-1$
            }
            else if ( this.usedPercent > 0.7 ) {
                stclass = "bg-color-warning"; //$NON-NLS-1$
            }
            else if ( this.usedPercent > 0.5 ) {
                stclass = "bg-color-warning-low"; //$NON-NLS-1$
            }
            this.barStyleClass = stclass;
            this.barStyle = String.format(Locale.ROOT, "width: %.2f%%;", this.usedPercent * 100); //$NON-NLS-1$ ;
        }


        /**
         * @return the name
         */
        public String getName () {
            return this.name;
        }


        public String getBarStyle () {
            return this.barStyle;
        }


        public String getBarStyleClass () {
            return this.barStyleClass;
        }


        /**
         * @return the freePercent
         */
        public float getFreePercent () {
            return this.freePercent;
        }


        public float getUsedPercent () {
            return this.usedPercent;
        }


        /**
         * @return the usableSpace
         */
        public long getUsableSpace () {
            return this.usableSpace;
        }


        /**
         * @return the totalSpace
         */
        public long getTotalSpace () {
            return this.totalSpace;
        }

    }
}
