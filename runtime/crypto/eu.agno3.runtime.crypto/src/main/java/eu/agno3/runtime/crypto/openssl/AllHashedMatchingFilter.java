/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.openssl;


import java.io.File;
import java.io.FileFilter;

import org.apache.commons.lang3.StringUtils;


/**
 * @author mbechler
 *
 */
public final class AllHashedMatchingFilter implements FileFilter {

    /**
     * {@inheritDoc}
     *
     * @see java.io.FileFilter#accept(java.io.File)
     */
    @Override
    public boolean accept ( File f ) {
        return isValidHashedFile(f);
    }


    /**
     * @param candFile
     * @return
     */
    private static boolean isValidHashedFile ( File candFile ) {
        if ( !candFile.isFile() || !candFile.canRead() ) {
            return false;
        }

        return isValidHashFilename(candFile.getName());
    }


    /**
     * @param fname
     * @return
     */
    protected static boolean isValidHashFilename ( String fname ) {
        int dot = fname.indexOf('.');
        if ( dot < 0 || fname.indexOf('.', dot + 1) >= 0 ) {
            return false;
        }

        String hash = fname.substring(0, dot);
        String index = fname.substring(dot + 1);

        if ( invalidHash(hash) || invalidIndex(index) ) {
            return false;
        }

        return true;
    }


    /**
     * @param hash
     * @param index
     * @return
     */
    private static boolean invalidIndex ( String index ) {
        return index.isEmpty() || !StringUtils.isNumeric(index);
    }


    private static boolean invalidHash ( String hash ) {
        return hash.isEmpty() || !isHex(hash);
    }


    private static boolean isHex ( String hash ) {
        return StringUtils.containsOnly(hash, "0123456789abcdef"); //$NON-NLS-1$
    }

}