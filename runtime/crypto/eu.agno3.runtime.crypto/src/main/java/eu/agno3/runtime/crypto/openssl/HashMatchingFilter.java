/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.openssl;


import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * @author mbechler
 *
 */
public final class HashMatchingFilter implements FileFilter {

    private Set<String> hashes;


    /**
     * @param hashes
     */
    public HashMatchingFilter ( String... hashes ) {
        this.hashes = new HashSet<>(Arrays.asList(hashes));
    }


    /**
     * @param hashes
     */
    public HashMatchingFilter ( Set<String> hashes ) {
        this.hashes = new HashSet<>(hashes);
    }


    @Override
    public boolean accept ( File f ) {

        String name = f.getName();

        if ( !f.isFile() || !f.canRead() ) {
            return false;
        }

        int dotPos = name.indexOf('.');

        if ( dotPos < 0 || name.indexOf('.', dotPos + 1) > 0 ) {
            return false;
        }

        String hashVal = name.substring(0, dotPos);

        return this.hashes.contains(hashVal);
    }
}