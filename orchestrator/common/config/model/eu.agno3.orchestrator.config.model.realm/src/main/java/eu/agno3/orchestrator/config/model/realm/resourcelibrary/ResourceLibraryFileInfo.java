/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 29, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.resourcelibrary;


import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;


/**
 * @author mbechler
 *
 */
public class ResourceLibraryFileInfo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1957692492401847608L;

    private String path;
    private UUID anchorId;
    private String libraryName;
    private boolean inherited;
    private boolean globalDefault;

    private UUID libraryId;


    /**
     * 
     */
    public ResourceLibraryFileInfo () {}


    /**
     * 
     * @param path
     * @param libraryId
     */
    public ResourceLibraryFileInfo ( String path, UUID libraryId ) {
        this.path = path;
        this.libraryId = libraryId;
    }


    /**
     * 
     * @param path
     * @param globalDefault
     */
    public ResourceLibraryFileInfo ( String path, boolean globalDefault ) {
        this.path = path;
        this.globalDefault = globalDefault;
        this.inherited = true;
    }


    /**
     * 
     * @param path
     * @param libraryId
     * @param anchor
     * @param libraryName
     * @param inherited
     */
    public ResourceLibraryFileInfo ( String path, UUID libraryId, UUID anchor, String libraryName, boolean inherited ) {
        this.path = path;
        this.libraryId = libraryId;
        this.anchorId = anchor;
        this.libraryName = libraryName;
        this.inherited = inherited;
    }


    /**
     * @return the path
     */
    public String getPath () {
        return this.path;
    }


    /**
     * @param path
     *            the path to set
     */
    public void setPath ( String path ) {
        this.path = path;
    }


    /**
     * @return the anchorId
     */
    public UUID getAnchorId () {
        return this.anchorId;
    }


    /**
     * @param anchorId
     *            the anchorId to set
     */
    public void setAnchorId ( UUID anchorId ) {
        this.anchorId = anchorId;
    }


    /**
     * @return the libraryName
     */
    public String getLibraryName () {
        return this.libraryName;
    }


    /**
     * @param libraryName
     *            the libraryName to set
     */
    public void setLibraryName ( String libraryName ) {
        this.libraryName = libraryName;
    }


    /**
     * @return the libraryId
     */
    public UUID getLibraryId () {
        return this.libraryId;
    }


    /**
     * @param libraryId
     *            the libraryId to set
     */
    public void setLibraryId ( UUID libraryId ) {
        this.libraryId = libraryId;
    }


    /**
     * @return the globalDefault
     */
    public boolean getGlobalDefault () {
        return this.globalDefault;
    }


    /**
     * @param globalDefault
     *            the globalDefault to set
     */
    public void setGlobalDefault ( boolean globalDefault ) {
        this.globalDefault = globalDefault;
    }


    /**
     * @return the inherited
     */
    public boolean getInherited () {
        return this.inherited;
    }


    /**
     * @param inherited
     *            the inherited to set
     */
    public void setInherited ( boolean inherited ) {
        this.inherited = inherited;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        return this.path != null ? this.path.hashCode() : 0;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        if ( ! ( obj instanceof ResourceLibraryFileInfo ) ) {
            return false;
        }
        ResourceLibraryFileInfo o = (ResourceLibraryFileInfo) obj;
        return Objects.equals(this.path, o.path);
    }
}
