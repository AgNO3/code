/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.internal;


import eu.agno3.orchestrator.system.info.SystemInformationException;


/**
 * @author mbechler
 * 
 */
public final class LVMUtil {

    private static final String DEV_MAPPER_PATH = "/dev/mapper/"; //$NON-NLS-1$
    private static final String DEV_PATH = "/dev/"; //$NON-NLS-1$
    static final String LVNAME_SEP = "-"; //$NON-NLS-1$
    static final String LVNAME_ESCAPED = "--"; //$NON-NLS-1$ 


    private LVMUtil () {}


    static String getVgNameFromDevice ( String devName ) throws SystemInformationException {
        if ( isDevMapperDeviceNode(devName) ) {
            return getVgNameFromDevMapperDevice(devName);
        }
        else if ( isDevSubdirDeviceNode(devName) ) {
            return getVgNameFromDevSubdirDevice(devName);
        }

        throw new SystemInformationException("Unknown device scheme, cannot extract vg name from " + devName); //$NON-NLS-1$
    }


    private static String getVgNameFromDevSubdirDevice ( String devName ) {
        return devName.substring(DEV_PATH.length(), devName.lastIndexOf('/'));
    }


    private static String getVgNameFromDevMapperDevice ( String devName ) throws SystemInformationException {
        String lvSpec = getDeviceFileName(devName);

        if ( lvSpec.indexOf('-') < 0 ) {
            throw new SystemInformationException("Cannot extract volume group name from " + devName); //$NON-NLS-1$
        }

        return lvSpec.substring(0, lvSpec.indexOf('-'));
    }


    static String getLvNameFromDevice ( String devName ) throws SystemInformationException {
        if ( isDevMapperDeviceNode(devName) ) {
            return getLvNameFromDevMapperDevice(devName);
        }
        else if ( isDevSubdirDeviceNode(devName) ) {
            return getLvNameFromDevSubdirDevice(devName);
        }

        throw new SystemInformationException("Unknown device scheme, cannot extract lv name from " + devName); //$NON-NLS-1$
    }


    private static String getLvNameFromDevSubdirDevice ( String devName ) {
        return getDeviceFileName(devName);
    }


    private static String getLvNameFromDevMapperDevice ( String devName ) throws SystemInformationException {
        String lvSpec = getDeviceFileName(devName);

        if ( lvSpec.indexOf('-') < 0 ) {
            throw new SystemInformationException("Cannot extract logical volume name from " + devName); //$NON-NLS-1$
        }

        return LVMUtil.unescapeLVName(lvSpec.substring(lvSpec.indexOf('-') + 1));
    }


    private static String getDeviceFileName ( String devName ) {
        return devName.substring(devName.lastIndexOf('/') + 1);
    }


    private static boolean isDevMapperDeviceNode ( String devName ) {
        return devName.startsWith(DEV_MAPPER_PATH);
    }


    private static boolean isDevSubdirDeviceNode ( String devName ) {
        return devName.startsWith(DEV_PATH) && devName.lastIndexOf('/') > 5;
    }


    /**
     * @param substring
     * @return
     */
    static String unescapeLVName ( String name ) {
        return name.replaceAll(LVNAME_ESCAPED, LVNAME_SEP);
    }
}
