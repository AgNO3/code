/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.internal;


import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.freedesktop.UDisks2.Filesystem;
import org.freedesktop.UDisks2.Swapspace;
import org.freedesktop.dbus.types.Variant;

import eu.agno3.orchestrator.system.info.storage.fs.AbstractFileSystemImpl;
import eu.agno3.orchestrator.system.info.storage.fs.DataFileSystemImpl;
import eu.agno3.orchestrator.system.info.storage.fs.FileSystemType;
import eu.agno3.orchestrator.system.info.storage.fs.SwapFileSystemImpl;
import eu.agno3.orchestrator.system.info.storage.volume.AbstractVolumeImpl;


/**
 * @author mbechler
 * 
 */
public class FileSystemCollector {

    private static final String SWAP_ACTIVE_PROP = "Active"; //$NON-NLS-1$
    private static final String ID_LABEL_PROP = "IdLabel"; //$NON-NLS-1$
    private static final String ID_UUID_PROP = "IdUUID"; //$NON-NLS-1$
    private static final Object ID_TYPE_PROP = "IdType"; //$NON-NLS-1$
    private static final String FILESYSTEM_USAGE_PROP = "filesystem"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(FileSystemCollector.class);


    /**
     * 
     */
    public FileSystemCollector () {}


    protected void collectFileSystem ( AbstractVolumeImpl v, Map<String, Map<String, Variant<?>>> ifs, Map<String, Variant<?>> properties ) {
        AbstractFileSystemImpl fs = null;

        if ( isDataFileSystem(ifs, properties) ) {
            fs = handleDataFileSystem(v, ifs, properties);
        }

        if ( isSwapspace(ifs) ) {
            fs = handleSwapspace(ifs, properties);
        }

        if ( fs != null ) {
            setupFileSystem(v, properties, fs);
        }
    }


    @SuppressWarnings ( "unchecked" )
    protected AbstractFileSystemImpl handleDataFileSystem ( AbstractVolumeImpl v, Map<String, Map<String, Variant<?>>> ifs,
            Map<String, Variant<?>> properties ) {
        AbstractFileSystemImpl fs;
        log.trace(" contains filesystem"); //$NON-NLS-1$
        DataFileSystemImpl dataFs = new DataFileSystemImpl();
        dataFs.setFsType(FileSystemType.fromString(VolumeUtils.getVolumeType(properties)));

        Map<String, Variant<?>> fsProps = ifs.get(Filesystem.class.getName());
        Variant<List<List<Byte>>> mountPointsProp = (Variant<List<List<Byte>>>) fsProps.get("MountPoints"); //$NON-NLS-1$
        Set<String> mountPoints = new HashSet<>();

        for ( List<Byte> mp : mountPointsProp.getValue() ) {
            byte[] mountPoint = ArrayUtils.toPrimitive(mp.toArray(new Byte[] {}));
            mountPoints.add( ( new String(mountPoint, Charset.forName("UTF-8")) ).trim()); //$NON-NLS-1$
        }

        if ( !mountPoints.isEmpty() ) {
            Path fsPath = FileSystems.getDefault().getPath(mountPoints.iterator().next());
            try {
                FileStore store = Files.getFileStore(fsPath);
                dataFs.setTotalSpace(store.getTotalSpace());
                dataFs.setUsableSpace(store.getUsableSpace());
            }
            catch ( IOException e ) {
                log.warn("Failed to determine free space:", e); //$NON-NLS-1$
            }
        }

        dataFs.setMountPoints(mountPoints);
        fs = dataFs;
        return fs;
    }


    protected AbstractFileSystemImpl handleSwapspace ( Map<String, Map<String, Variant<?>>> ifs, Map<String, Variant<?>> properties ) {
        AbstractFileSystemImpl fs;
        log.trace(" containing swapspace"); //$NON-NLS-1$
        SwapFileSystemImpl swapFs = new SwapFileSystemImpl();
        swapFs.setActive((Boolean) ifs.get(Swapspace.class.getName()).get(SWAP_ACTIVE_PROP).getValue());
        fs = swapFs;
        return fs;
    }


    private static boolean isSwapspace ( Map<String, Map<String, Variant<?>>> ifs ) {
        return ifs.containsKey(Swapspace.class.getName());
    }


    private static boolean isDataFileSystem ( Map<String, Map<String, Variant<?>>> ifs, Map<String, Variant<?>> properties ) {
        return ifs.containsKey(Filesystem.class.getName()) && FILESYSTEM_USAGE_PROP.equals(VolumeUtils.getVolumeUsage(properties));
    }


    private static void setupFileSystem ( AbstractVolumeImpl v, Map<String, Variant<?>> properties, AbstractFileSystemImpl fs ) {
        @SuppressWarnings ( "unchecked" )
        Variant<String> uuidProp = (Variant<String>) properties.get(ID_UUID_PROP);
        @SuppressWarnings ( "unchecked" )
        Variant<String> labelProp = (Variant<String>) properties.get(ID_LABEL_PROP);
        @SuppressWarnings ( "unchecked" )
        Variant<String> idTypeProp = (Variant<String>) properties.get(ID_TYPE_PROP);

        if ( uuidProp != null && !uuidProp.getValue().isEmpty() ) {

            // exclude VFAT, these are not valid UUIDs
            if ( idTypeProp != null && "vfat".equals(idTypeProp.getValue()) ) { //$NON-NLS-1$
                log.debug("Skip filesystem UUID with id type " + idTypeProp.getValue()); //$NON-NLS-1$
            }
            else {
                try {
                    fs.setUuid(UUID.fromString(uuidProp.getValue()));
                }
                catch ( IllegalArgumentException e ) {
                    log.warn("Invalid UUID for " + v.getDevice(), e); //$NON-NLS-1$
                }
            }
        }

        if ( labelProp != null && !labelProp.getValue().isEmpty() ) {
            fs.setLabel(labelProp.getValue());
        }

        fs.setDevPath(VolumeUtils.getPreferredDeviceName(properties));

        v.setFileSystem(fs);
    }
}
