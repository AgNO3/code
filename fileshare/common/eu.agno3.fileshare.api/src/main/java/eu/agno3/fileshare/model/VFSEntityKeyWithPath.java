/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.10.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import java.io.Serializable;

import eu.agno3.fileshare.util.FilenameUtil;


/**
 * @author mbechler
 *
 */
public class VFSEntityKeyWithPath implements EntityKey, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3823845398177587783L;

    private final String vfsId;
    private final String path;

    private String toStringCache;


    /**
     * @param vfsId
     * @param path
     * 
     */
    public VFSEntityKeyWithPath ( String vfsId, String path ) {
        this.vfsId = vfsId;
        this.path = path;
    }


    /**
     * @return the groupId
     */
    public String getVFS () {
        return this.vfsId;
    }


    /**
     * @return the path
     */
    public String getPath () {
        return this.path;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        if ( this.toStringCache != null ) {
            return this.toStringCache;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("vgroupp:"); //$NON-NLS-1$
        if ( this.vfsId != null ) {
            sb.append(this.getVFS());
        }
        sb.append(':');
        if ( this.path != null ) {
            sb.append(FilenameUtil.encodeFileName(this.path));
        }
        this.toStringCache = sb.toString();
        return this.toStringCache;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo ( EntityKey o ) {
        if ( ! ( o instanceof VFSEntityKeyWithPath ) ) {
            return -1;
        }

        VFSEntityKeyWithPath k = (VFSEntityKeyWithPath) o;

        if ( this.vfsId != null && k.vfsId != null ) {
            int cmp = this.vfsId.compareTo(k.vfsId);
            if ( cmp != 0 ) {
                return cmp;
            }
        }
        else if ( this.vfsId == null ) {
            return -1;
        }
        else if ( k.vfsId == null ) {
            return 1;
        }

        if ( this.path != null && k.path != null ) {
            int cmp = this.path.compareTo(k.path);
            if ( cmp != 0 ) {
                return cmp;
            }
        }
        else if ( this.path == null ) {
            return -1;
        }
        else if ( k.path == null ) {
            return 1;
        }
        return 0;
    }


    // +GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.vfsId == null ) ? 0 : this.vfsId.hashCode() );
        result = prime * result + ( ( this.path == null ) ? 0 : this.path.hashCode() );
        return result;
    }


    // -GENERATED

    // +GENERATED
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        VFSEntityKeyWithPath other = (VFSEntityKeyWithPath) obj;
        if ( this.vfsId == null ) {
            if ( other.vfsId != null )
                return false;
        }
        else if ( !this.vfsId.equals(other.vfsId) )
            return false;
        if ( this.path == null ) {
            if ( other.path != null )
                return false;
        }
        else if ( !this.path.equals(other.path) )
            return false;
        return true;
    }
    // -GENERATED

}
