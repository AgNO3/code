/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.02.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.net.URI;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.UnsupportedOperationException;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.service.ArchiveType;
import eu.agno3.fileshare.service.LinkService;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.util.FilenameUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = LinkService.class )
public class LinkServiceImpl implements LinkService {

    /**
     * 
     */
    private static final String DIR = "dir"; //$NON-NLS-1$
    private static final String UNKNOWN = "unknwown"; //$NON-NLS-1$

    private DefaultServiceContext ctx;


    @Reference
    protected synchronized void setServiceContext ( DefaultServiceContext sctx ) {
        this.ctx = sctx;
    }


    protected synchronized void unsetServiceContext ( DefaultServiceContext sctx ) {
        if ( this.ctx == sctx ) {
            this.ctx = null;
        }
    }


    private String getBaseURL ( String overrideBase ) {
        String base = overrideBase;
        URI frontend = this.ctx.getConfigurationProvider().getFrontendConfiguration().getWebFrontendURI();
        if ( StringUtils.isBlank(base) && frontend != null ) {
            base = frontend.toString();
        }

        if ( base.charAt(base.length() - 1) == '/' ) {
            return base.substring(0, base.length() - 1);
        }

        return base;
    }


    /**
     * 
     * @param string
     * @param overrideBase
     * @return a generic absolute link
     */
    @Override
    public String makeGenericLink ( String string, String overrideBase ) {
        return this.getBaseURL(overrideBase).concat(string);
    }


    @Override
    public String makeDownloadLink ( VFSFileEntity file, String query, String overrideBase, boolean anon ) throws FileshareException {
        if ( file == null ) {
            return null;
        }
        return String.format(
            "%s/files/download/%s/%s%s", //$NON-NLS-1$
            getBaseURL(overrideBase),
            file.getEntityKey(),
            anon ? "file" : FilenameUtil.encodeFileName(file.getLocalName()), //$NON-NLS-1$
            query);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.LinkService#makeDownloadAllLink(java.util.Set,
     *      eu.agno3.fileshare.service.ArchiveType, java.lang.String, java.lang.String)
     */
    @Override
    public String makeDownloadAllLink ( Set<VFSEntity> entities, ArchiveType type, String query, String overrideBase ) throws FileshareException {
        if ( entities == null || entities.isEmpty() ) {
            return null;
        }
        String res = String.format(
            "%s/files/download-all%s%s&type=%s", //$NON-NLS-1$
            this.getBaseURL(overrideBase),
            StringUtils.isBlank(query) ? "?" //$NON-NLS-1$
                    : query + "&", //$NON-NLS-1$
            makeEntitiesArg(entities),
            type.name());

        if ( res.length() > 8000 ) {
            throw new UnsupportedOperationException("Too many files"); //$NON-NLS-1$
        }
        return res;
    }


    @Override
    public String makeDirectoryArchiveLink ( VFSContainerEntity file, ArchiveType archiveType, String query, String overrideBase, boolean anon )
            throws FileshareException {
        if ( archiveType == null || file == null ) {
            return null;
        }

        return String.format(
            "%s/files/archive-%s/%s/%s.%s%s", //$NON-NLS-1$
            this.getBaseURL(overrideBase),
            archiveType.name(),
            file.getEntityKey(),
            getTargetFileName(file, anon),
            archiveType.getExtension(),
            query);
    }


    /**
     * @param entities
     * @return
     */
    private static String makeEntitiesArg ( Set<VFSEntity> entities ) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for ( VFSEntity e : entities ) {
            if ( !first ) {
                sb.append('&');
            }
            else {
                first = false;
            }
            sb.append("entity=" + e.getEntityKey()); //$NON-NLS-1$
        }

        return sb.toString();
    }


    @Override
    public String makeBackendViewLink ( VFSFileEntity file, String query, String overrideBase ) throws FileshareException {
        if ( file == null ) {
            return null;
        }

        String encfname = FilenameUtil.encodeFileName(file.getLocalName());
        return String.format(
            "%s/files/view/%s/%s%s", //$NON-NLS-1$
            getBaseURL(overrideBase),
            file.getEntityKey(),
            encfname,
            query);
    }


    /**
     * 
     * @param file
     * @param extraQuery
     * @param overrideBase
     * @return a frontend view link
     * @throws FileshareException
     */
    @Override
    public String makeFrontendViewLink ( VFSFileEntity file, String extraQuery, String overrideBase ) throws FileshareException {
        if ( file == null ) {
            return null;
        }

        return String.format("%s/actions/viewFile.xhtml?file=%s%s", getBaseURL(overrideBase), file.getEntityKey(), extraQuery); //$NON-NLS-1$
    }


    @Override
    public String makeDirectoryViewLink ( VFSContainerEntity dir, String extraQuery, String overrideBase ) throws FileshareException {
        if ( dir == null ) {
            return null;
        }

        return String.format("%s/index.xhtml?root=%s%s", getBaseURL(overrideBase), dir.getEntityKey(), extraQuery); //$NON-NLS-1$
    }


    /**
     * @param file
     * @param anon
     * @return
     */
    private static String getTargetFileName ( VFSContainerEntity file, boolean anon ) {
        return anon ? DIR : file.getLocalName() != null ? FilenameUtil.encodeFileName(file.getLocalName()) : UNKNOWN;
    }

}
