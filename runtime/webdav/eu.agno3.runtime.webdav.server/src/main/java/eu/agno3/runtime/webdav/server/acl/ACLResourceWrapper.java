/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.04.2016 by mbechler
 */
package eu.agno3.runtime.webdav.server.acl;


import java.io.IOException;
import java.util.List;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.lock.ActiveLock;
import org.apache.jackrabbit.webdav.lock.LockInfo;
import org.apache.jackrabbit.webdav.lock.LockManager;
import org.apache.jackrabbit.webdav.lock.Scope;
import org.apache.jackrabbit.webdav.lock.Type;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.PropEntry;
import org.apache.jackrabbit.webdav.security.AclProperty;
import org.apache.jackrabbit.webdav.security.AclResource;
import org.apache.jackrabbit.webdav.version.report.Report;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;


/**
 * 
 * ACE requires a ACL resource to be passed
 * 
 * The only method called is getHref(). This would be totally unnecessary if the API was not that stupid.
 * 
 * @author mbechler
 *
 */
public class ACLResourceWrapper implements AclResource {

    private String href;


    /**
     * @param href
     * 
     */
    public ACLResourceWrapper ( String href ) {
        this.href = href;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#addLockManager(org.apache.jackrabbit.webdav.lock.LockManager)
     */
    @Override
    public void addLockManager ( LockManager arg0 ) {}


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#addMember(org.apache.jackrabbit.webdav.DavResource,
     *      org.apache.jackrabbit.webdav.io.InputContext)
     */
    @Override
    public void addMember ( DavResource arg0, InputContext arg1 ) throws DavException {}


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#alterProperties(java.util.List)
     */
    @Override
    public MultiStatusResponse alterProperties ( List<? extends PropEntry> arg0 ) throws DavException {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#copy(org.apache.jackrabbit.webdav.DavResource, boolean)
     */
    @Override
    public void copy ( DavResource arg0, boolean arg1 ) throws DavException {}


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#exists()
     */
    @Override
    public boolean exists () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getCollection()
     */
    @Override
    public DavResource getCollection () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getComplianceClass()
     */
    @Override
    public String getComplianceClass () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getDisplayName()
     */
    @Override
    public String getDisplayName () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getFactory()
     */
    @Override
    public DavResourceFactory getFactory () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getHref()
     */
    @Override
    public String getHref () {
        return this.href;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getLocator()
     */
    @Override
    public DavResourceLocator getLocator () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getLock(org.apache.jackrabbit.webdav.lock.Type,
     *      org.apache.jackrabbit.webdav.lock.Scope)
     */
    @Override
    public ActiveLock getLock ( Type arg0, Scope arg1 ) {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getLocks()
     */
    @Override
    public ActiveLock[] getLocks () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getMembers()
     */
    @Override
    public DavResourceIterator getMembers () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getModificationTime()
     */
    @Override
    public long getModificationTime () {
        return 0;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getProperties()
     */
    @Override
    public DavPropertySet getProperties () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getProperty(org.apache.jackrabbit.webdav.property.DavPropertyName)
     */
    @Override
    public DavProperty<?> getProperty ( DavPropertyName arg0 ) {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getPropertyNames()
     */
    @Override
    public DavPropertyName[] getPropertyNames () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getResourcePath()
     */
    @Override
    public String getResourcePath () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getSession()
     */
    @Override
    public DavSession getSession () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getSupportedMethods()
     */
    @Override
    public String getSupportedMethods () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#hasLock(org.apache.jackrabbit.webdav.lock.Type,
     *      org.apache.jackrabbit.webdav.lock.Scope)
     */
    @Override
    public boolean hasLock ( Type arg0, Scope arg1 ) {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#isCollection()
     */
    @Override
    public boolean isCollection () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#isLockable(org.apache.jackrabbit.webdav.lock.Type,
     *      org.apache.jackrabbit.webdav.lock.Scope)
     */
    @Override
    public boolean isLockable ( Type arg0, Scope arg1 ) {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#lock(org.apache.jackrabbit.webdav.lock.LockInfo)
     */
    @Override
    public ActiveLock lock ( LockInfo arg0 ) throws DavException {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#move(org.apache.jackrabbit.webdav.DavResource)
     */
    @Override
    public void move ( DavResource arg0 ) throws DavException {}


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#refreshLock(org.apache.jackrabbit.webdav.lock.LockInfo,
     *      java.lang.String)
     */
    @Override
    public ActiveLock refreshLock ( LockInfo arg0, String arg1 ) throws DavException {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#removeMember(org.apache.jackrabbit.webdav.DavResource)
     */
    @Override
    public void removeMember ( DavResource arg0 ) throws DavException {}


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#removeProperty(org.apache.jackrabbit.webdav.property.DavPropertyName)
     */
    @Override
    public void removeProperty ( DavPropertyName arg0 ) throws DavException {}


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#setProperty(org.apache.jackrabbit.webdav.property.DavProperty)
     */
    @Override
    public void setProperty ( DavProperty<?> arg0 ) throws DavException {}


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#spool(org.apache.jackrabbit.webdav.io.OutputContext)
     */
    @Override
    public void spool ( OutputContext arg0 ) throws IOException {}


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#unlock(java.lang.String)
     */
    @Override
    public void unlock ( String arg0 ) throws DavException {}


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.security.AclResource#alterAcl(org.apache.jackrabbit.webdav.security.AclProperty)
     */
    @Override
    public void alterAcl ( AclProperty arg0 ) throws DavException {}


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.security.AclResource#getReport(org.apache.jackrabbit.webdav.version.report.ReportInfo)
     */
    @Override
    public Report getReport ( ReportInfo arg0 ) throws DavException {
        return null;
    }

}
