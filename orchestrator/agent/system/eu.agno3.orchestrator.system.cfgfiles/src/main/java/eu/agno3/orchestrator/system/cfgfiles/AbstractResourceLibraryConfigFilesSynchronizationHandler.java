/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2015 by mbechler
 */
package eu.agno3.orchestrator.system.cfgfiles;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.realm.StructuralObjectReference;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibraryEntry;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibraryException;
import eu.agno3.orchestrator.system.file.util.FileHashUtil;
import eu.agno3.orchestrator.system.file.util.FileTemporaryUtils;


/**
 * @author mbechler
 *
 */
public abstract class AbstractResourceLibraryConfigFilesSynchronizationHandler implements ResourceLibrarySynchronizationHandler {

    private static final Logger log = Logger.getLogger(AbstractResourceLibraryConfigFilesSynchronizationHandler.class);


    /**
     * 
     * @return the config files manager
     * @throws ResourceLibraryException
     */
    protected abstract ConfigFileManager getManager ( StructuralObjectReference service ) throws ResourceLibraryException;


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.cfgfiles.ResourceLibrarySynchronizationHandler#list(eu.agno3.orchestrator.config.model.realm.StructuralObjectReference,
     *      java.lang.String)
     */
    @Override
    public List<ResourceLibraryEntry> list ( StructuralObjectReference serviceTarget, String hint ) throws ResourceLibraryException {
        ConfigFileManager cfm = getManager(serviceTarget);
        if ( log.isDebugEnabled() ) {
            log.debug("Listing for " + serviceTarget); //$NON-NLS-1$
        }
        return list(cfm, hint);
    }


    /**
     * 
     * @param cfm
     * @param hint
     * @return
     * @throws ResourceLibraryException
     */
    private static List<ResourceLibraryEntry> list ( ConfigFileManager cfm, String hint ) throws ResourceLibraryException {
        List<String> files = cfm.getFiles();
        List<ResourceLibraryEntry> entries = new LinkedList<>();
        try {
            for ( String path : files ) {
                ResourceLibraryEntry e = new ResourceLibraryEntry();
                e.setPath(path);
                e.setHash(Hex.encodeHexString(FileHashUtil.sha256(cfm.getPath(path))));
                if ( log.isTraceEnabled() ) {
                    log.trace(String.format("%s : %s", e.getPath(), e.getHash())); //$NON-NLS-1$
                }
                entries.add(e);
            }
        }
        catch (
            NoSuchAlgorithmException |
            IOException e ) {
            throw new ResourceLibraryException("Failed to list resource library", e); //$NON-NLS-1$
        }

        log.debug("Done listing"); //$NON-NLS-1$
        return entries;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.cfgfiles.ResourceLibrarySynchronizationHandler#synchronize(eu.agno3.orchestrator.config.model.realm.StructuralObjectReference,
     *      java.lang.String, java.util.Set, java.util.Set, java.util.Set)
     */
    @Override
    public List<ResourceLibraryEntry> synchronize ( StructuralObjectReference serviceTarget, String hint, Set<ResourceLibraryEntry> update,
            Set<ResourceLibraryEntry> add, Set<ResourceLibraryEntry> delete ) throws ResourceLibraryException {
        ConfigFileManager cfm = getManager(serviceTarget);
        log.debug("Synchronizing"); //$NON-NLS-1$
        try {
            for ( ResourceLibraryEntry a : add ) {
                writeContents(cfm, serviceTarget, a);
            }

            for ( ResourceLibraryEntry u : update ) {
                writeContents(cfm, serviceTarget, u);
            }

            for ( ResourceLibraryEntry d : delete ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Removing " + d.getPath()); //$NON-NLS-1$
                }
                cfm.remove(d.getPath());
            }

            if ( !add.isEmpty() || !update.isEmpty() || !delete.isEmpty() ) {
                this.updated(serviceTarget, hint);
            }
        }
        catch ( IOException e ) {
            throw new ResourceLibraryException("Failed to synchronize", e); //$NON-NLS-1$
        }

        return list(cfm, hint);
    }


    /**
     * @param hint
     * 
     */
    protected void updated ( StructuralObjectReference service, String hint ) {

    }


    /**
     * @param cfm
     * @param a
     * @throws IOException
     */
    private static void writeContents ( ConfigFileManager cfm, StructuralObjectReference serviceTarget, ResourceLibraryEntry a ) throws IOException {
        Path path = cfm.getPath(a.getPath());
        if ( log.isDebugEnabled() ) {
            log.debug("Writing " + path); //$NON-NLS-1$
        }

        Path tmpFile = FileTemporaryUtils.createRelatedTemporaryFile(path);
        try {
            Files.write(tmpFile, a.getContent(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            cfm.createOrReplace(a.getPath(), tmpFile);
        }
        finally {
            Files.deleteIfExists(tmpFile);
        }
    }
}
