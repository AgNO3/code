/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.08.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.instance.sysinfo;


import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.system.info.network.InterfaceStatus;
import eu.agno3.orchestrator.system.info.network.InterfaceType;
import eu.agno3.orchestrator.system.info.platform.PlatformType;
import eu.agno3.orchestrator.system.info.storage.drive.Drive;
import eu.agno3.orchestrator.system.info.storage.fs.DataFileSystem;
import eu.agno3.orchestrator.system.info.storage.fs.FileSystem;
import eu.agno3.orchestrator.system.info.storage.fs.FileSystemType;
import eu.agno3.orchestrator.system.info.storage.volume.Volume;
import eu.agno3.runtime.jsf.view.stacking.DialogContext;


/**
 * @author mbechler
 * 
 */
@ApplicationScoped
@Named ( "sysInfoUtil" )
public class SysInfoUtil {

    /**
     * 
     */
    private static final String TRADEMARK_SYMBOL = "\u2122"; //$NON-NLS-1$
    private static final String[] CPUMODEL_REPLACE = new String[] {
        " CPU", //$NON-NLS-1$
        "(R)", //$NON-NLS-1$
        "(TM)", //$NON-NLS-1$
        "(tm)" //$NON-NLS-1$
    };
    private static final String[] CPUMODEL_REPLACEMENT = new String[] {
        StringUtils.EMPTY, "\u00AE", //$NON-NLS-1$
        TRADEMARK_SYMBOL, TRADEMARK_SYMBOL
    };


    public String pick ( Object o ) {
        if ( ! ( o instanceof Serializable ) ) {
            return null;
        }

        if ( o instanceof Drive ) {
            return pickDrive((Drive) o);
        }
        else if ( o instanceof Volume ) {
            return pickVolume((Volume) o);
        }
        else {
            return null;
        }
    }


    public boolean canPick ( Object o ) {
        if ( ! ( o instanceof Serializable ) ) {
            return false;
        }
        if ( o instanceof Drive ) {
            return canPickDrive((Drive) o);
        }
        else if ( o instanceof Volume ) {
            return canPickVolume((Volume) o);
        }
        else if ( o instanceof DataFileSystem ) {
            return canPickFileSystem((FileSystem) o);
        }
        else {
            return false;
        }
    }


    /**
     * @param o
     * @return
     */
    private static boolean canPickFileSystem ( FileSystem o ) {
        return o instanceof DataFileSystem && o.getFsType() != FileSystemType.UNKNOWN;
    }


    /**
     * @param o
     * @return
     */
    private static boolean canPickVolume ( Volume o ) {
        return o.getFileSystem() != null && canPickFileSystem(o.getFileSystem());
    }


    /**
     * @param o
     * @return
     */
    private static boolean canPickDrive ( Drive o ) {
        return o.getVolumes() != null && o.getVolumes().size() == 1 && canPickVolume(o.getVolumes().get(0));
    }


    /**
     * @param o
     * @return
     */
    private static String pickDrive ( Drive o ) {
        if ( !canPickDrive(o) ) {
            return null;
        }
        return pickVolume(o.getVolumes().get(0));
    }


    /**
     * @param o
     * @return
     */
    private static String pickVolume ( Volume o ) {
        if ( !canPickVolume(o) ) {
            return null;
        }
        return DialogContext.closeDialog(o);
    }


    /**
     * 
     * @param cpuModel
     * @return a readable transformation of the cpu model
     */
    public String cleanCpuModel ( String cpuModel ) {
        String model = cpuModel;
        int atIndex = model.indexOf('@');

        if ( atIndex >= 0 ) {
            model = model.substring(0, atIndex);
        }

        model = StringUtils.replaceEach(model, CPUMODEL_REPLACE, CPUMODEL_REPLACEMENT);
        return model.replaceAll(
            "\\s", //$NON-NLS-1$
            " "); //$NON-NLS-1$
    }


    /**
     * 
     * @param t
     * @return translated platform type
     */
    public String translatePlatformType ( PlatformType t ) {
        return translateEnumValue(t != null ? t : PlatformType.UNKNOWN, "PlatformType."); //$NON-NLS-1$
    }


    /**
     * 
     * @param t
     * @return translated interface type
     */
    public String translateInterfaceType ( InterfaceType t ) {
        return translateEnumValue(t != null ? t : InterfaceType.UNKNOWN, "InterfaceType."); //$NON-NLS-1$
    }


    /**
     * 
     * @param t
     * @return translate interface status
     */
    public String translateInterfaceStatus ( InterfaceStatus t ) {
        return translateEnumValue(t != null ? t : InterfaceStatus.UNKNOWN, "InterfaceStatus."); //$NON-NLS-1$
    }


    private static String translateEnumValue ( Enum<?> t, String prefix ) {
        return GuiMessages.get(prefix + t.name());
    }

}
