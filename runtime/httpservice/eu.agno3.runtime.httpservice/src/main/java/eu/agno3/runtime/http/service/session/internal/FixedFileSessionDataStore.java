/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.01.2017 by mbechler
 */
package eu.agno3.runtime.http.service.session.internal;


import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.session.FileSessionDataStore;


/**
 * 
 * Copy of jetty upstream 9.4.0, but more graceful when it comes to session files with old format
 * 
 * @author mbechler
 *
 */
public class FixedFileSessionDataStore extends FileSessionDataStore {

    private static final Logger log = Logger.getLogger(FixedFileSessionDataStore.class);


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.jetty.server.session.FileSessionDataStore#doGetExpired(java.util.Set)
     */
    @Override
    public Set<String> doGetExpired ( Set<String> candidates ) {
        final long now = System.currentTimeMillis();
        HashSet<String> expired = new HashSet<>();

        File[] files = getStoreDir().listFiles(new FilenameFilter() {

            @Override
            public boolean accept ( File dir, String name ) {
                if ( dir != getStoreDir() )
                    return false;

                int sep = name.indexOf('_');

                if ( sep < 0 ) {
                    // old format, expire
                    if ( getLog().isDebugEnabled() ) {
                        getLog().debug("Expiring old format session file " + name); //$NON-NLS-1$
                    }
                    return true;
                }

                String s = name.substring(0, sep);
                long expiry = ( s == null ? 0 : Long.parseLong(s) );

                if ( expiry > 0 && expiry < now ) {
                    return true;
                }

                return false;
            }
        });

        if ( files != null ) {
            for ( File f : files ) {
                expired.add(getIdFromFile(f));
            }
        }

        // check candidates that were not found to be expired, perhaps they no
        // longer exist and they should be expired
        for ( String c : candidates ) {
            if ( !expired.contains(c) ) {
                // check if the file exists
                File f = getFile(getStoreDir(), c);
                if ( f == null || !f.exists() )
                    expired.add(c);
            }
        }

        return expired;
    }


    private static String getIdFromFile ( File file ) {
        if ( file == null )
            return null;
        String name = file.getName();

        int sep = name.lastIndexOf('_');
        if ( sep < 0 ) {
            return null;
        }
        return name.substring(sep + 1);
    }


    private File getFile ( final File storeDir, final String id ) {
        File[] files = storeDir.listFiles(new FilenameFilter() {

            /**
             * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
             */
            @Override
            public boolean accept ( File dir, String name ) {
                if ( dir != storeDir )
                    return false;
                return ( name.contains(getFileName(id)) );
            }

        });

        if ( files == null || files.length < 1 )
            return null;
        return files[ 0 ];
    }


    String getFileName ( String id ) {
        return this._context.getCanonicalContextPath() + '_' + this._context.getVhost() + '_' + id;
    }

}
