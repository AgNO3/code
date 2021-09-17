/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.fileshare.exceptions.EntityNotFoundException;
import eu.agno3.fileshare.exceptions.MultiVFSException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.NativeEntityKey;
import eu.agno3.fileshare.model.VFSEntityKeyWithPath;
import eu.agno3.fileshare.service.VFSService;
import eu.agno3.fileshare.service.api.internal.VFSServiceInternal;
import eu.agno3.fileshare.service.vfs.NativeVFS;
import eu.agno3.fileshare.util.FilenameUtil;
import eu.agno3.fileshare.vfs.VFS;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    VFSService.class, VFSServiceInternal.class
} )
public class VFSServiceImpl implements VFSServiceInternal {

    private NativeVFS nativeVFS;

    private Map<String, VFS> impls = new HashMap<>();


    @Reference
    protected synchronized void setNativeVFS ( NativeVFS nv ) {
        this.nativeVFS = nv;
    }


    protected synchronized void unsetNativeVFS ( NativeVFS nv ) {
        if ( this.nativeVFS == nv ) {
            this.nativeVFS = null;
        }
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindVFS ( VFS v ) {
        this.impls.put(v.getId(), v);
    }


    protected synchronized void unbindVFS ( VFS v ) {
        this.impls.remove(v.getId(), v);
    }


    @Override
    public EntityKey parseEntityKey ( String id ) {
        if ( StringUtils.isBlank(id) ) {
            throw new IllegalArgumentException();
        }

        if ( id.startsWith("vgroupp:") ) { //$NON-NLS-1$
            String[] parts = StringUtils.splitPreserveAllTokens(id, ':');
            if ( parts.length != 3 ) {
                throw new IllegalArgumentException();
            }

            VFSEntityKeyWithPath k = new VFSEntityKeyWithPath(parts[ 1 ], FilenameUtil.decodeFileName(parts[ 2 ]));
            return k;
        }

        return new NativeEntityKey(UUID.fromString(id));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.VFSServiceInternal#getNative()
     */
    @Override
    public VFS getNative () {
        return this.nativeVFS;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws EntityNotFoundException
     *
     * @see eu.agno3.fileshare.service.api.internal.VFSServiceInternal#getVFS(java.lang.String)
     */
    @Override
    public VFS getVFS ( String id ) throws EntityNotFoundException {
        VFS v = this.impls.get(id);
        if ( v == null ) {
            throw new EntityNotFoundException();
        }
        return v;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws EntityNotFoundException
     *
     * @see eu.agno3.fileshare.service.api.internal.VFSServiceInternal#getVFS(eu.agno3.fileshare.model.EntityKey)
     */
    @Override
    public VFS getVFS ( EntityKey key ) throws EntityNotFoundException {
        if ( key instanceof NativeEntityKey ) {
            return this.nativeVFS;
        }
        else if ( key instanceof VFSEntityKeyWithPath ) {
            return getVFS( ( (VFSEntityKeyWithPath) key ).getVFS());
        }
        throw new IllegalArgumentException("Unsupported VFS " + key); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @throws EntityNotFoundException
     * @throws MultiVFSException
     *
     * @see eu.agno3.fileshare.service.api.internal.VFSServiceInternal#getVFS(java.util.Collection)
     */
    @Override
    public VFS getVFS ( Collection<EntityKey> ids ) throws EntityNotFoundException, MultiVFSException {

        VFS found = null;
        for ( EntityKey k : ids ) {
            VFS v = getVFS(k);
            if ( found != null && !found.getId().equals(v.getId()) ) {
                throw new MultiVFSException();
            }
            found = v;
        }

        if ( found == null ) {
            throw new EntityNotFoundException("Unsupported VFS"); //$NON-NLS-1$
        }
        return found;
    }
}
