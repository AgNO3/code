/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.internal;


import java.io.Serializable;
import java.net.URL;
import java.util.Comparator;


/**
 * Comparator for ordering schema changes
 * 
 * Version format is YYYYMMDD(.INDEX)
 * 
 * @author mbechler
 * 
 */
public class URLSchemaVersionComperator implements Comparator<URL>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7042347734797547115L;
    /**
     * 
     */
    private static final String VERSION_SEPARATOR = "."; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( URL a, URL b ) {

        String versionA = SchemaBundleTracker.extractChangeVersion(a);
        String versionB = SchemaBundleTracker.extractChangeVersion(b);

        if ( versionA == null && versionB == null ) {
            return 0;
        }
        else if ( versionA == null ) {
            return 1;
        }
        else if ( versionB == null ) {
            return -1;
        }

        int res = compareNullSafe(versionA, versionB);

        if ( res != 0 ) {
            return res;
        }

        return a.toString().compareTo(b.toString());
    }


    /**
     * @param versionA
     * @param versionB
     * @return
     */
    private static int compareNullSafe ( String versionA, String versionB ) {
        String majorVersionA = null;
        int minorVersionA = 0;
        String majorVersionB = null;
        int minorVersionB = 0;

        if ( versionA.contains(VERSION_SEPARATOR) ) {
            majorVersionA = versionA.substring(0, versionA.indexOf(VERSION_SEPARATOR));
            minorVersionA = Integer.parseInt(versionA.substring(versionA.indexOf(VERSION_SEPARATOR) + 1));
        }
        else {
            majorVersionA = versionA;
        }

        if ( versionB.contains(VERSION_SEPARATOR) ) {
            majorVersionB = versionB.substring(0, versionB.indexOf(VERSION_SEPARATOR));
            minorVersionB = Integer.parseInt(versionB.substring(versionB.indexOf(VERSION_SEPARATOR) + 1));
        }
        else {
            majorVersionB = versionB;
        }

        if ( majorVersionA.equals(majorVersionB) ) {
            return Integer.compare(minorVersionA, minorVersionB);
        }

        return majorVersionA.compareTo(majorVersionB);

    }
}
