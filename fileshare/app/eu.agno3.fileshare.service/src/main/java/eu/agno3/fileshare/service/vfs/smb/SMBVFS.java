/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.01.2016 by mbechler
 */
package eu.agno3.fileshare.service.vfs.smb;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.smb.internal.SMBConfigUtil;
import eu.agno3.fileshare.service.vfs.AbstractVFS;
import eu.agno3.fileshare.service.vfs.FilesystemWatchListener;
import eu.agno3.fileshare.vfs.VFS;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;
import eu.agno3.runtime.net.ad.ADRealm;
import eu.agno3.runtime.net.ad.CIFSConfiguration;
import eu.agno3.runtime.net.krb5.KerberosRealm;
import eu.agno3.runtime.util.config.ConfigUtil;

import jcifs.CIFSContext;
import jcifs.CIFSException;
import jcifs.FileNotifyInformation;
import jcifs.SmbResource;
import jcifs.context.BaseContext;


/**
 * @author mbechler
 *
 */
@Component ( service = VFS.class, configurationPid = "vfs.smb", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class SMBVFS extends AbstractVFS implements VFS, FilesystemWatchListener<String, FileNotifyInformation> {

    private static final Logger log = Logger.getLogger(SMBVFS.class);
    private URL root;
    private CIFSContext context;

    private Map<URL, SmbResource> cache;
    private KerberosRealm realm;
    private URI rootUri;
    private CIFSContext ownedContext;
    private SMBWatcher watcher;


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL )
    protected synchronized void bindRealm ( KerberosRealm kr ) {
        this.realm = kr;
    }


    protected synchronized void unbindRealm ( KerberosRealm kr ) {
        if ( this.realm == kr ) {
            this.realm = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.vfs.AbstractVFS#setDefaultServiceContext(eu.agno3.fileshare.service.api.internal.DefaultServiceContext)
     */
    @Override
    @Reference
    protected synchronized void setDefaultServiceContext ( DefaultServiceContext dsc ) {
        super.setDefaultServiceContext(dsc);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.vfs.AbstractVFS#unsetDefaultServiceContext(eu.agno3.fileshare.service.api.internal.DefaultServiceContext)
     */
    @Override
    protected synchronized void unsetDefaultServiceContext ( DefaultServiceContext dsc ) {
        super.unsetDefaultServiceContext(dsc);
    }


    @Override
    @Activate
    @Modified
    protected synchronized void activate ( ComponentContext ctx ) {
        String rootString = ConfigUtil.parseString(ctx.getProperties(), "root", null); //$NON-NLS-1$
        if ( StringUtils.isBlank(rootString) ) {
            log.error("No root URL set"); //$NON-NLS-1$
            return;
        }

        if ( rootString.charAt(rootString.length() - 1) != '/' ) {
            rootString += '/';
        }

        try {
            this.rootUri = new URI(rootString);
            CIFSContext oldOwnedContext = this.ownedContext;
            CIFSContext configureBase = configureBase(ctx);
            URL newRoot = new URL(
                "smb", //$NON-NLS-1$
                this.rootUri.getHost(),
                this.rootUri.getPort() == -1 ? 0 : this.rootUri.getPort(),
                this.rootUri.getPath(),
                configureBase.getUrlHandler());

            try ( SmbResource f = (SmbResource) newRoot.openConnection() ) {
                f.exists();
            }
            catch ( IOException e ) {
                log.error("Failed to connect to share " + newRoot, e); //$NON-NLS-1$
            }

            this.context = configureBase;
            this.root = newRoot;
            if ( oldOwnedContext != null ) {
                try {
                    this.ownedContext.close();
                }
                catch ( CIFSException e ) {
                    log.error("Failed to dispose of context", e); //$NON-NLS-1$
                }
            }
        }
        catch (
            URISyntaxException |
            IOException e ) {
            log.error("Failed to parse root URL " + rootString, e); //$NON-NLS-1$
            return;
        }

        super.activate(ctx);

        if ( this.isInitialized() ) {
            try {
                if ( this.watcher == null ) {
                    this.watcher = new SMBWatcher(this.getId(), this.context, this.root, this);
                }
            }
            catch ( IOException e ) {
                log.error("Failed to create filesystem watch service", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.vfs.AbstractVFS#isInitialized()
     */
    @Override
    public boolean isInitialized () {
        return super.isInitialized() && this.context != null;
    }


    @Override
    @Deactivate
    protected void deactivate ( ComponentContext ctx ) {
        if ( this.watcher != null ) {
            this.watcher.close();
            this.watcher = null;
        }

        super.deactivate(ctx);
        if ( this.ownedContext != null ) {
            try {
                this.ownedContext.close();
            }
            catch ( CIFSException e ) {
                log.error("Failed to dispose of context", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param ctx
     * @param rootUri
     * @return
     * @throws CIFSException
     */
    @SuppressWarnings ( "unchecked" )
    private CIFSContext configureBase ( ComponentContext ctx ) throws CIFSException {
        this.cache = Collections.synchronizedMap(new LRUMap<>(ConfigUtil.parseInt(ctx.getProperties(), "cacheSize", 128))); //$NON-NLS-1$

        CIFSContext baseCtx;
        if ( this.realm instanceof ADRealm ) {
            baseCtx = ( (ADRealm) this.realm ).getCIFSContext();
        }
        else {
            String hostName = SMBConfigUtil.getNetbiosHostName();
            baseCtx = new BaseContext(new CIFSConfiguration(hostName, false, (Map<String, Object>) ctx.getProperties()));
            this.ownedContext = baseCtx;
        }
        return SMBConfigUtil.configureAuth(baseCtx, this.realm, ctx.getProperties());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.vfs.FilesystemWatchListener#fileChanged(java.lang.Object, java.lang.Object)
     */
    @Override
    public void fileChanged ( String p, FileNotifyInformation kind ) {
        // TODO Auto-generated method stub

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFS#begin(boolean)
     */
    @Override
    public VFSContext begin ( boolean readOnly ) throws FileshareException {
        try {
            return new SMBVFSContext(getServiceContext().getFileshareEntityTS(), readOnly, this.root, this);
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to start entity transaction", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFS#begin(eu.agno3.runtime.db.orm.EntityTransactionContext)
     */
    @Override
    public VFSContext begin ( EntityTransactionContext ctx ) throws FileshareException {
        return new SMBVFSContext(ctx, this, this.root);
    }


    /**
     * @return the cifs context to use
     */
    public CIFSContext getContext () {
        return this.context;
    }


    /**
     * @param url
     * @param f
     */
    public void putCache ( URL url, SmbResource f ) {
        if ( this.cache == null ) {
            return;
        }
        this.cache.put(url, f);
    }


    /**
     * @param url
     * @return cached entry
     */
    public SmbResource getCached ( URL url ) {
        if ( this.cache == null ) {
            return null;
        }
        return this.cache.get(url);
    }

}
