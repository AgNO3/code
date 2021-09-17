/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.02.2015 by mbechler
 */
package eu.agno3.runtime.configloader.file;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;

import eu.agno3.runtime.util.osgi.ResourceUtil;


/**
 * @author mbechler
 *
 */
public class ConfigFileLoaderImpl implements ConfigFileLoader {

    private static final Logger log = Logger.getLogger(ConfigFileLoaderImpl.class);

    private static final String BUNDLE_PREFIX = "/cfgfiles/"; //$NON-NLS-1$

    private List<File> searchDirs;
    private Bundle fallbackBundle;


    /**
     * 
     */
    protected ConfigFileLoaderImpl () {}


    /**
     * @param searchDirs
     * @param fallbackBundle
     * 
     */
    public ConfigFileLoaderImpl ( List<File> searchDirs, Bundle fallbackBundle ) {
        this.searchDirs = searchDirs;
        this.fallbackBundle = fallbackBundle;
    }


    /**
     * @param searchDirs
     *            the searchDirs to set
     */
    @Override
    public void setSearchDirs ( List<File> searchDirs ) {
        this.searchDirs = searchDirs;
    }


    /**
     * @param fallbackBundle
     *            the fallbackBundle to set
     */
    protected void setFallbackBundle ( Bundle fallbackBundle ) {
        this.fallbackBundle = fallbackBundle;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.configloader.file.ConfigFileLoader#exists(java.lang.String)
     */
    @Override
    public boolean exists ( String path ) throws IOException {
        for ( File searchDir : this.searchDirs ) {
            if ( !searchDir.isDirectory() && searchDir.canRead() ) {
                continue;
            }

            String canonDir = searchDir.getCanonicalPath();

            File f = new File(searchDir, path);
            if ( f.exists() && f.canRead() ) {
                checkCanonicalBelow(canonDir, f.getCanonicalPath());
                return true;
            }
        }

        if ( this.fallbackBundle == null ) {
            return false;
        }

        URL u = getFallbackUrl(path);

        if ( u == null ) {
            return false;
        }

        try ( InputStream is = u.openStream() ) {
            return true;
        }
        catch ( FileNotFoundException e ) {
            log.trace("File not found in bundle", e); //$NON-NLS-1$
            return false;
        }
    }


    /**
     * @param path
     * @return
     */
    private URL getFallbackUrl ( String path ) {
        return ResourceUtil.safeFindEntry(this.fallbackBundle, BUNDLE_PREFIX, path);
    }


    /**
     * @param canonDir
     * @param filePath
     * @throws IOException
     */
    private static void checkCanonicalBelow ( String canonDir, String filePath ) throws IOException {
        if ( !filePath.startsWith(canonDir) ) {
            throw new IOException("Path traversal"); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.configloader.file.ConfigFileLoader#getInputStream(java.lang.String)
     */
    @Override
    public InputStream getInputStream ( String path ) throws IOException {
        if ( log.isDebugEnabled() ) {
            log.debug("Looking up " + path); //$NON-NLS-1$
        }
        for ( File searchDir : this.searchDirs ) {
            if ( !searchDir.isDirectory() && searchDir.canRead() ) {
                continue;
            }

            String canonDir = searchDir.getCanonicalPath();

            File f = new File(searchDir, path);
            if ( f.exists() && f.canRead() ) {
                checkCanonicalBelow(canonDir, f.getCanonicalPath());
                return new FileInputStream(f);
            }

            if ( log.isTraceEnabled() ) {
                log.trace("Not found  " + f); //$NON-NLS-1$
            }
        }

        if ( this.fallbackBundle == null ) {
            throw new FileNotFoundException("Not found in search path: " + path); //$NON-NLS-1$
        }

        return getFallbackUrl(path).openStream();
    }


    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     *
     * @see eu.agno3.runtime.configloader.file.ConfigFileLoader#getURL(java.lang.String)
     */
    @Override
    public URL getURL ( String path ) throws IOException {

        if ( log.isDebugEnabled() ) {
            log.debug("Looking up " + path); //$NON-NLS-1$
        }

        for ( File searchDir : this.searchDirs ) {
            if ( !searchDir.isDirectory() && searchDir.canRead() ) {
                continue;
            }

            String canonDir = searchDir.getCanonicalPath();

            File f = new File(searchDir, path);
            if ( f.exists() && f.canRead() ) {
                checkCanonicalBelow(canonDir, f.getCanonicalPath());
                return f.toURI().toURL();
            }

            if ( log.isTraceEnabled() ) {
                log.trace("Not found  " + f); //$NON-NLS-1$
            }

        }

        if ( this.fallbackBundle == null ) {
            return null;
        }

        return getFallbackUrl(path);
    }
}
