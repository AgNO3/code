/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.04.2016 by mbechler
 */
package eu.agno3.fileshare.webdav.sync;


import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.agno3.fileshare.exceptions.InvalidSyncTokenException;
import eu.agno3.fileshare.exceptions.SyncException;


/**
 * @author mbechler
 *
 */
public class SyncTokenData {

    private static JsonFactory JF = new JsonFactory();
    private static ObjectMapper OM = new ObjectMapper(JF);

    private static final String URL_BASE = "http://agno3.eu/ns/dav/sync/1/"; //$NON-NLS-1$
    private static final String UTF8 = "UTF-8"; //$NON-NLS-1$


    /**
     * 
     */
    public SyncTokenData () {}

    private Long lastModified;
    private Long lastSync;

    private boolean haveUserRoot;
    private boolean haveGroups;
    private boolean haveShares;

    private int version = 1;

    private UUID rootId;

    private Set<UUID> visibleGroups = new HashSet<>();
    private Set<UUID> visibleGrants = new HashSet<>();


    /**
     * @return the lastModified
     */
    public Long getLastModified () {
        return this.lastModified;
    }


    /**
     * @param lastModified
     *            the lastModified to set
     */
    public void setLastModified ( long lastModified ) {
        this.lastModified = lastModified;
    }


    /**
     * @return the version
     */
    public int getVersion () {
        return this.version;
    }


    /**
     * @param version
     *            the version to set
     */
    public void setVersion ( int version ) {
        this.version = version;
    }


    /**
     * @return the rootId
     */
    public UUID getRootId () {
        return this.rootId;
    }


    /**
     * @param rootId
     *            the rootId to set
     */
    public void setRootId ( UUID rootId ) {
        this.rootId = rootId;
    }


    /**
     * @return the lastSync
     */
    public Long getLastSync () {
        return this.lastSync;
    }


    /**
     * @param lastSync
     *            the lastSync to set
     */
    public void setLastSync ( long lastSync ) {
        this.lastSync = lastSync;
    }


    /**
     * @param lm
     */
    public void updateLastModified ( long lm ) {
        if ( this.lastModified == null ) {
            this.lastModified = lm;
        }
        else {
            this.lastModified = Math.max(this.lastModified, lm);
        }
    }


    /**
     * 
     * @param dt
     */
    public void updateLastModified ( DateTime dt ) {
        if ( dt == null ) {
            return;
        }
        updateLastModified(dt.getMillis());
    }


    /**
     * @return the haveGroups
     */
    public boolean isHaveGroups () {
        return this.haveGroups;
    }


    /**
     * @param haveGroups
     *            the haveGroups to set
     */
    public void setHaveGroups ( boolean haveGroups ) {
        this.haveGroups = haveGroups;
    }


    /**
     * @return the haveUserRoot
     */
    public boolean isHaveUserRoot () {
        return this.haveUserRoot;
    }


    /**
     * @param haveUserRoot
     *            the haveUserRoot to set
     */
    public void setHaveUserRoot ( boolean haveUserRoot ) {
        this.haveUserRoot = haveUserRoot;
    }


    /**
     * @return the haveShares
     */
    public boolean isHaveShares () {
        return this.haveShares;
    }


    /**
     * @param haveShares
     *            the haveShares to set
     */
    public void setHaveShares ( boolean haveShares ) {
        this.haveShares = haveShares;
    }


    /**
     * @return the visibleGroups
     */
    public Set<UUID> getVisibleGroups () {
        return this.visibleGroups;
    }


    /**
     * @return the visibleShares
     */
    public Set<UUID> getVisibleGrants () {
        return this.visibleGrants;
    }


    /**
     * @param data
     * @return the parsed token
     * @throws InvalidSyncTokenException
     */
    public static SyncTokenData parse ( String data ) throws InvalidSyncTokenException {
        try {
            if ( !data.startsWith(URL_BASE) ) {
                throw new InvalidSyncTokenException("Not a valid token"); //$NON-NLS-1$
            }
            return OM.readValue(URLDecoder.decode(data.substring(URL_BASE.length()), UTF8), SyncTokenData.class);
        }
        catch ( IOException e ) {
            throw new InvalidSyncTokenException("Failed to read token", e); //$NON-NLS-1$
        }
    }


    /**
     * @return a string representation of the data
     * @throws SyncException
     */
    public String marshall () throws SyncException {
        try {
            return URL_BASE + URLEncoder.encode(OM.writeValueAsString(this), UTF8);
        }
        catch ( IOException e ) {
            throw new SyncException("Failed to generate sync token"); //$NON-NLS-1$
        }
    }

}
