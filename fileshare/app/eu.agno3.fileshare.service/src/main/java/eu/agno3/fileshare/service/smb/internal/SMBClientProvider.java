/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 4, 2017 by mbechler
 */
package eu.agno3.fileshare.service.smb.internal;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;

import eu.agno3.runtime.net.ad.CIFSConfiguration;
import eu.agno3.runtime.util.config.ConfigUtil;

import jcifs.CIFSContext;
import jcifs.CIFSException;
import jcifs.RuntimeCIFSException;
import jcifs.SmbResource;
import jcifs.context.BaseContext;


/**
 * @author mbechler
 *
 */
@Component ( service = SMBClientProvider.class, configurationPid = "store.smb", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class SMBClientProvider {

    private static final Logger log = Logger.getLogger(SMBClientProvider.class);

    private URI rootUri;
    private CIFSContext context;
    private SmbResource root;


    @Activate
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
            String hostName = SMBConfigUtil.getNetbiosHostName();
            @SuppressWarnings ( "unchecked" )
            CIFSContext cctx = new BaseContext(new CIFSConfiguration(hostName, false, (Map<String, Object>) ctx.getProperties()));
            cctx = SMBConfigUtil.configureAuth(cctx, null, ctx.getProperties());

            URL r = new URL(
                "smb", //$NON-NLS-1$
                this.rootUri.getHost(),
                this.rootUri.getPort() == -1 ? 0 : this.rootUri.getPort(),
                this.rootUri.getPath(),
                cctx.getUrlHandler());

            SmbResource f = (SmbResource) r.openConnection();
            try {
                f.exists();
            }
            catch (
                IOException |
                RuntimeCIFSException e ) {
                log.error("Failed to connect to share " + r, e); //$NON-NLS-1$
            }

            this.context = cctx;
            this.root = f;
        }
        catch (
            URISyntaxException |
            IOException e ) {
            log.error("Failed to parse root URL " + rootString, e); //$NON-NLS-1$
            return;
        }
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        SmbResource r = this.root;
        this.root = null;
        if ( r != null ) {
            r.close();
        }

        CIFSContext cctx = this.context;
        this.context = null;
        if ( cctx != null ) {
            try {
                cctx.close();
            }
            catch ( CIFSException e ) {
                log.warn("Failed to close context", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @return the context
     */
    public CIFSContext getContext () {
        return this.context;
    }


    /**
     * @return the root
     */
    public SmbResource getRoot () {
        return this.root;
    }
}
