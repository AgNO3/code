/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.06.2015 by mbechler
 */
package eu.agno3.runtime.webdav.server.impl;


import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.webdav.DavCompliance;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceIteratorImpl;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.bind.BindConstants;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.lock.ActiveLock;
import org.apache.jackrabbit.webdav.lock.LockDiscovery;
import org.apache.jackrabbit.webdav.lock.LockInfo;
import org.apache.jackrabbit.webdav.lock.LockManager;
import org.apache.jackrabbit.webdav.lock.Scope;
import org.apache.jackrabbit.webdav.lock.SupportedLock;
import org.apache.jackrabbit.webdav.lock.Type;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.property.HrefProperty;
import org.apache.jackrabbit.webdav.property.PropEntry;
import org.apache.jackrabbit.webdav.property.ResourceType;
import org.apache.jackrabbit.webdav.search.QueryGrammerSet;
import org.apache.jackrabbit.webdav.search.SearchInfo;
import org.apache.jackrabbit.webdav.search.SearchResource;
import org.apache.jackrabbit.webdav.security.AclProperty;
import org.apache.jackrabbit.webdav.util.HttpDateFormat;
import org.apache.jackrabbit.webdav.version.report.Report;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.agno3.runtime.webdav.server.DAVTreeNode;
import eu.agno3.runtime.webdav.server.ExistanceDAVNode;
import eu.agno3.runtime.webdav.server.ExtendedDavSession;
import eu.agno3.runtime.webdav.server.PatchableResource;
import eu.agno3.runtime.webdav.server.StreamingReportResource;
import eu.agno3.runtime.webdav.server.StreamingWebdavResponse;
import eu.agno3.runtime.webdav.server.WriteReplaceProperty;
import eu.agno3.runtime.webdav.server.acl.ReplacedAclProperty;
import eu.agno3.runtime.webdav.server.acl.ReplacedAclResource;


/**
 * @author mbechler
 * @param <T>
 *
 */
public class DavResourceWrapper <T> implements DavResource, ReplacedAclResource, SearchResource, PatchableResource, StreamingReportResource {

    private static final Logger log = Logger.getLogger(DavResourceWrapper.class);

    private static final String COMPLIANCE_CLASSES = DavCompliance.concatComplianceClasses(new String[] {
        DavCompliance._1_, DavCompliance._2_, DavCompliance.ACCESS_CONTROL
    });

    private LockManager lockManager;
    private DefaultDavResourceFactory<T> factory;
    private DavResourceLocator locator;
    private ExtendedDavSession session;

    private DavPropertySet properties = new DavPropertySet();
    private boolean propertiesInitialized = false;

    private DAVTreeNode<T> wrapped;
    private DavResource cachedParent;

    private String rfc4122Uri;

    private boolean overrideIsCollection;


    /**
     * @param wrapped
     * @param locator
     * @param factory
     * @param session
     * @param lockManager
     * @param overrideIsCollection
     * @param parent
     */
    public DavResourceWrapper ( DAVTreeNode<T> wrapped, DavResourceLocator locator, DefaultDavResourceFactory<T> factory, ExtendedDavSession session,
            LockManager lockManager, boolean overrideIsCollection, DavResource parent ) {
        this.wrapped = wrapped;
        this.locator = locator;
        this.factory = factory;
        this.session = session;
        this.lockManager = lockManager;
        this.overrideIsCollection = overrideIsCollection;
        this.cachedParent = parent;

        if ( wrapped != null && wrapped.getId() != null ) {
            this.rfc4122Uri = "urn:id:" + wrapped.getId(); //$NON-NLS-1$
        }

    }


    /**
     * @return the wrapped
     */
    public DAVTreeNode<T> getWrapped () {
        return this.wrapped;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#addLockManager(org.apache.jackrabbit.webdav.lock.LockManager)
     */
    @Override
    public void addLockManager ( LockManager lm ) {
        log.warn("addLockManager"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#addMember(org.apache.jackrabbit.webdav.DavResource,
     *      org.apache.jackrabbit.webdav.io.InputContext)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public void addMember ( DavResource member, InputContext context ) throws DavException {

        if ( ! ( member instanceof DavResourceWrapper ) ) {
            throw new DavException(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        }

        if ( !exists() ) {
            throw new DavException(HttpServletResponse.SC_NOT_FOUND);
        }

        if ( !isCollection() ) {
            throw new DavException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }

        if ( isLocked(this) || isLocked(member) ) {
            throw new DavException(DavServletResponse.SC_LOCKED);
        }

        if ( !member.exists() ) {
            this.factory.create(this, (DavResourceWrapper<T>) member, context);
        }
        else {
            this.factory.update((DavResourceWrapper<T>) member, context);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#removeMember(org.apache.jackrabbit.webdav.DavResource)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public void removeMember ( DavResource member ) throws DavException {
        if ( ! ( member instanceof DavResourceWrapper ) ) {
            throw new DavException(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        }

        if ( !exists() || !member.exists() ) {
            throw new DavException(HttpServletResponse.SC_NOT_FOUND);
        }

        if ( !isCollection() ) {
            throw new DavException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }

        if ( isLocked(this) || isLocked(member) ) {
            throw new DavException(DavServletResponse.SC_LOCKED);
        }

        this.factory.remove(this, (DavResourceWrapper<T>) member);

        // remove
        ActiveLock lock = getLock(Type.WRITE, Scope.EXCLUSIVE);
        if ( lock != null ) {
            this.lockManager.releaseLock(lock.getToken(), member);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#spool(org.apache.jackrabbit.webdav.io.OutputContext)
     */
    @Override
    public void spool ( OutputContext output ) throws IOException {
        this.factory.spoolContents(this, output);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getHref()
     */
    @Override
    public String getHref () {
        return this.locator.getHref(isCollection());
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getResourcePath()
     */
    @Override
    public String getResourcePath () {
        return this.locator.getResourcePath();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#isCollection()
     */
    @Override
    public boolean isCollection () {
        return this.overrideIsCollection || ( this.wrapped != null && this.wrapped.isCollection() );
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#exists()
     */
    @Override
    public boolean exists () {
        if ( this.wrapped == null ) {
            return false;
        }
        else if ( this.wrapped instanceof ExistanceDAVNode ) {
            return ( (ExistanceDAVNode<?>) this.wrapped ).exists();
        }
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getCollection()
     */
    @Override
    public DavResource getCollection () {
        if ( this.cachedParent == null ) {
            try {
                this.cachedParent = this.factory.fetchParent(this);
            }
            catch ( DavException e ) {
                log.warn("Failed to get parent", e); //$NON-NLS-1$
            }
        }

        return this.cachedParent;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getMembers()
     */
    @Override
    public DavResourceIterator getMembers () {
        try {
            if ( !isCollection() || !exists() ) {
                return new DavResourceIteratorImpl(Collections.EMPTY_LIST);
            }
            return this.factory.getChildren(this);
        }
        catch ( DavException e ) {
            log.warn("Failed to load members", e); //$NON-NLS-1$
            return new DavResourceIteratorImpl(Collections.EMPTY_LIST);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getSupportedMethods()
     */
    @Override
    public String getSupportedMethods () {
        return StringUtils.join(this.getWrapped().getSupportedMethods(), ',');
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getComplianceClass()
     */
    @Override
    public String getComplianceClass () {
        return COMPLIANCE_CLASSES;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getDisplayName()
     */
    @Override
    public String getDisplayName () {
        return this.wrapped.getDisplayName();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getModificationTime()
     */
    @Override
    public long getModificationTime () {
        DateTime mod = this.wrapped.getModificationTime();
        if ( mod == null ) {
            return -1;
        }

        return mod.getMillis();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getFactory()
     */
    @Override
    public DavResourceFactory getFactory () {
        return this.factory;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getLocator()
     */
    @Override
    public DavResourceLocator getLocator () {
        return this.locator;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getSession()
     */
    @Override
    public ExtendedDavSession getSession () {
        return this.session;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getProperties()
     */
    @Override
    public DavPropertySet getProperties () {
        ensurePropertiesInitialized();
        return this.properties;

    }


    /**
     * 
     */
    private void ensurePropertiesInitialized () {
        if ( !this.exists() || this.propertiesInitialized ) {
            return;
        }

        if ( !StringUtils.isBlank(this.getDisplayName()) ) {
            this.properties.add(new DefaultDavProperty<>(DavPropertyName.DISPLAYNAME, this.getDisplayName()));
        }

        if ( !this.wrapped.isOverrideResourceType() ) {
            if ( isCollection() ) {
                this.properties.add(new ResourceType(ResourceType.COLLECTION));
                this.properties.add(new DefaultDavProperty<>(DavPropertyName.ISCOLLECTION, String.valueOf(1)));
            }
            else {
                this.properties.add(new ResourceType(ResourceType.DEFAULT_RESOURCE));
                this.properties.add(new DefaultDavProperty<>(DavPropertyName.ISCOLLECTION, String.valueOf(0)));
            }
        }

        String lastMod;
        if ( this.wrapped.getModificationTime() != null ) {
            lastMod = HttpDateFormat.modificationDateFormat().format(this.wrapped.getModificationTime().toDate());
        }
        else {
            lastMod = HttpDateFormat.modificationDateFormat().format(DateTime.now().toDate());
        }

        if ( this.wrapped.getETag() != null ) {
            this.properties.add(new DefaultDavProperty<>(DavPropertyName.GETETAG, this.wrapped.getETag()));
        }

        if ( this.rfc4122Uri != null ) {
            this.properties.add(new HrefProperty(BindConstants.RESOURCEID, this.rfc4122Uri, true));
        }

        this.properties.add(new DefaultDavProperty<>(DavPropertyName.GETLASTMODIFIED, lastMod));

        if ( this.wrapped.getCreationTime() != null ) {
            this.properties.add(
                new DefaultDavProperty<>(
                    DavPropertyName.CREATIONDATE,
                    HttpDateFormat.creationDateFormat().format(this.wrapped.getCreationTime().toDate())));
        }

        if ( this.wrapped.getContentLength() != null ) {
            this.properties.add(new DefaultDavProperty<>(DavPropertyName.GETCONTENTLENGTH, this.wrapped.getContentLength()));
        }

        if ( !StringUtils.isBlank(this.wrapped.getContentType()) ) {
            this.properties.add(new DefaultDavProperty<>(DavPropertyName.GETCONTENTTYPE, this.wrapped.getContentType()));
        }

        this.properties.add(new LockDiscovery(getLock(Type.WRITE, Scope.EXCLUSIVE)));

        SupportedLock supportedLock = new SupportedLock();
        if ( this.session.isPersistent() ) {
            supportedLock.addEntry(Type.WRITE, Scope.EXCLUSIVE);
        }
        this.properties.add(supportedLock);

        for ( DavProperty<?> prop : this.wrapped.getExtraProperties(this.session.getLocale()) ) {
            if ( prop instanceof WriteReplaceProperty ) {
                prop = ( (WriteReplaceProperty) prop ).writeReplaceProperty(this.locator, this.locator.getFactory());
            }
            this.properties.add(prop);
        }
        this.propertiesInitialized = true;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getProperty(org.apache.jackrabbit.webdav.property.DavPropertyName)
     */
    @Override
    public DavProperty<?> getProperty ( DavPropertyName name ) {
        return this.factory.getProperty(this, name);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getPropertyNames()
     */
    @Override
    public DavPropertyName[] getPropertyNames () {
        return this.factory.getPropertyNames(this);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#alterProperties(java.util.List)
     */
    @Override
    public MultiStatusResponse alterProperties ( List<? extends PropEntry> changeList ) throws DavException {
        if ( !exists() ) {
            throw new DavException(HttpServletResponse.SC_NOT_FOUND);
        }

        if ( isLocked(this) ) {
            throw new DavException(DavServletResponse.SC_LOCKED);
        }

        return this.factory.alterProperties(this, changeList);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#move(org.apache.jackrabbit.webdav.DavResource)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public void move ( DavResource destination ) throws DavException {

        if ( ! ( destination instanceof DavResourceWrapper ) ) {
            throw new DavException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        if ( !exists() ) {
            throw new DavException(HttpServletResponse.SC_NOT_FOUND);
        }

        if ( isLocked(destination) ) {
            throw new DavException(DavServletResponse.SC_LOCKED);
        }

        this.factory.move(this, (DavResourceWrapper<T>) destination);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#copy(org.apache.jackrabbit.webdav.DavResource, boolean)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public void copy ( DavResource destination, boolean shallow ) throws DavException {

        if ( ! ( destination instanceof DavResourceWrapper ) ) {
            throw new DavException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        if ( !exists() ) {
            throw new DavException(HttpServletResponse.SC_NOT_FOUND);
        }

        if ( isLocked(destination) ) {
            throw new DavException(DavServletResponse.SC_LOCKED);
        }

        this.factory.copy(this, (DavResourceWrapper<T>) destination, shallow);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.security.AclResource#alterAcl(org.apache.jackrabbit.webdav.security.AclProperty)
     */
    @Override
    public void alterAcl ( ReplacedAclProperty acl ) throws DavException {
        if ( !exists() ) {
            throw new DavException(HttpServletResponse.SC_NOT_FOUND);
        }

        if ( isLocked(this) ) {
            throw new DavException(DavServletResponse.SC_LOCKED);
        }

        this.factory.alterAcl(this, acl);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.security.AclResource#alterAcl(org.apache.jackrabbit.webdav.security.AclProperty)
     */
    @Override
    public void alterAcl ( AclProperty ap ) throws DavException {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.security.AclResource#getReport(org.apache.jackrabbit.webdav.version.report.ReportInfo)
     */
    @Override
    public Report getReport ( ReportInfo report ) throws DavException {
        if ( report == null ) {
            throw new DavException(HttpServletResponse.SC_BAD_REQUEST);
        }

        if ( !exists() ) {
            throw new DavException(HttpServletResponse.SC_NOT_FOUND);
        }

        return this.factory.getReport(this, report);

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.StreamingReportResource#canStream(org.apache.jackrabbit.webdav.version.report.ReportInfo)
     */
    @Override
    public boolean canStream ( ReportInfo info ) {
        return this.factory.canStream(this, info);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.StreamingReportResource#streamReport(org.apache.jackrabbit.webdav.version.report.ReportInfo,
     *      eu.agno3.runtime.webdav.server.StreamingWebdavResponse)
     */
    @Override
    public void streamReport ( ReportInfo info, StreamingWebdavResponse response ) throws DavException, IOException {
        if ( info == null || StringUtils.isBlank(info.getReportName()) ) {
            throw new DavException(HttpServletResponse.SC_BAD_REQUEST);
        }

        if ( !exists() ) {
            throw new DavException(HttpServletResponse.SC_NOT_FOUND);
        }
        this.factory.streamReport(this, info, response);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.search.SearchResource#search(org.apache.jackrabbit.webdav.search.SearchInfo)
     */
    @Override
    public MultiStatus search ( SearchInfo search ) throws DavException {
        if ( !exists() ) {
            throw new DavException(HttpServletResponse.SC_NOT_FOUND);
        }

        return this.factory.search(this, search);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.search.SearchResource#getQueryGrammerSet()
     */
    @Override
    public QueryGrammerSet getQueryGrammerSet () {
        return this.factory.getQueryGrammarSet(this);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.PatchableResource#getAcceptablePatchFormats()
     */
    @Override
    public Collection<String> getAcceptablePatchFormats () {

        if ( !exists() ) {
            return Collections.EMPTY_LIST;
        }
        return this.factory.getAcceptablePatchFormats(this);
    }


    /**
     * 
     * @param inputContext
     * @throws DavException
     * @throws IOException
     */
    @Override
    public void patch ( InputContext inputContext ) throws DavException, IOException {
        if ( !exists() ) {
            throw new DavException(HttpServletResponse.SC_NOT_FOUND);
        }

        if ( isLocked(this) ) {
            throw new DavException(DavServletResponse.SC_LOCKED);
        }

        this.factory.patch(this, inputContext);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#removeProperty(org.apache.jackrabbit.webdav.property.DavPropertyName)
     */
    @Override
    public void removeProperty ( DavPropertyName prop ) throws DavException {
        alterProperties(Collections.singletonList(prop));
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#setProperty(org.apache.jackrabbit.webdav.property.DavProperty)
     */
    @Override
    public void setProperty ( DavProperty<?> property ) throws DavException {
        alterProperties(Collections.singletonList(property));
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getLock(org.apache.jackrabbit.webdav.lock.Type,
     *      org.apache.jackrabbit.webdav.lock.Scope)
     */
    @Override
    public ActiveLock getLock ( Type lockType, Scope lockScope ) {
        if ( exists() && Type.WRITE.equals(lockType) && Scope.EXCLUSIVE.equals(lockScope) ) {
            return this.lockManager.getLock(lockType, lockScope, this);
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#getLocks()
     */
    @Override
    public ActiveLock[] getLocks () {
        ActiveLock writeLock = getLock(Type.WRITE, Scope.EXCLUSIVE);
        return ( writeLock != null ) ? new ActiveLock[] {
            writeLock
        } : new ActiveLock[0];
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#hasLock(org.apache.jackrabbit.webdav.lock.Type,
     *      org.apache.jackrabbit.webdav.lock.Scope)
     */
    @Override
    public boolean hasLock ( Type lockType, Scope lockScope ) {
        return getLock(lockType, lockScope) != null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#isLockable(org.apache.jackrabbit.webdav.lock.Type,
     *      org.apache.jackrabbit.webdav.lock.Scope)
     */
    @Override
    public boolean isLockable ( Type lockType, Scope scope ) {
        return Type.WRITE.equals(lockType) && Scope.EXCLUSIVE.equals(scope);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#lock(org.apache.jackrabbit.webdav.lock.LockInfo)
     */
    @Override
    public ActiveLock lock ( LockInfo info ) throws DavException {
        log.debug("lock"); //$NON-NLS-1$
        if ( !isLockable(info.getType(), info.getScope()) ) {
            throw new DavException(HttpServletResponse.SC_PRECONDITION_FAILED, "Not lockable"); //$NON-NLS-1$
        }

        if ( !this.session.isPersistent() ) {
            throw new DavException(HttpServletResponse.SC_PRECONDITION_FAILED, "Cannot lock, no session support"); //$NON-NLS-1$
        }

        ActiveLock lock = this.lockManager.createLock(info, this);
        this.session.addLockToken(lock.getToken());
        return lock;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#refreshLock(org.apache.jackrabbit.webdav.lock.LockInfo,
     *      java.lang.String)
     */
    @Override
    public ActiveLock refreshLock ( LockInfo info, String token ) throws DavException {
        if ( !exists() ) {
            throw new DavException(HttpServletResponse.SC_NOT_FOUND);
        }

        log.debug("refresh"); //$NON-NLS-1$

        ActiveLock lock = getLock(info.getType(), info.getScope());
        if ( lock == null ) {
            throw new DavException(HttpServletResponse.SC_PRECONDITION_FAILED, "Lock does not exist"); //$NON-NLS-1$
        }

        this.lockManager.refreshLock(info, token, this);
        return lock;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.DavResource#unlock(java.lang.String)
     */
    @Override
    public void unlock ( String token ) throws DavException {
        log.debug("unlock"); //$NON-NLS-1$
        ActiveLock lock = getLock(Type.WRITE, Scope.EXCLUSIVE);
        if ( lock == null ) {
            throw new DavException(HttpServletResponse.SC_PRECONDITION_FAILED, "Lock does not exist"); //$NON-NLS-1$
        }
        else if ( lock.isLockedByToken(token) ) {
            this.lockManager.releaseLock(token, this);
        }
        else {
            throw new DavException(DavServletResponse.SC_LOCKED);
        }
    }


    private boolean isLocked ( DavResource res ) {
        ActiveLock lock = res.getLock(Type.WRITE, Scope.EXCLUSIVE);
        if ( lock == null ) {
            return false;

        }

        for ( String lockedBySession : this.session.getLockTokens() ) {
            if ( lockedBySession.equals(lock.getToken()) ) {
                return false;
            }
        }
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        if ( this.wrapped != null ) {
            return this.wrapped.hashCode();
        }

        return this.getHref().hashCode();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        if ( obj instanceof DavResourceWrapper ) {
            DavResourceWrapper<?> otherWrapper = (DavResourceWrapper<?>) obj;
            if ( this.exists() && otherWrapper.exists() ) {
                return this.wrapped.equals(otherWrapper.getWrapped());
            }
            else if ( !this.exists() && !otherWrapper.exists() ) {
                return this.getHref().equals(otherWrapper.getHref());
            }
            return false;
        }
        else if ( obj instanceof DavResource ) {
            return ( (DavResource) obj ).getHref().equals(this.getHref());
        }

        return super.equals(obj);
    }

}
