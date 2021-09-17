/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.04.2016 by mbechler
 */
package eu.agno3.runtime.webdav.server.colsync;


import java.io.IOException;
import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.Status;
import org.apache.jackrabbit.webdav.version.report.Report;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.agno3.runtime.webdav.server.DAVTreeNode;
import eu.agno3.runtime.webdav.server.DavWrapperFactory;
import eu.agno3.runtime.webdav.server.StreamingContext;
import eu.agno3.runtime.webdav.server.StreamingReport;
import eu.agno3.runtime.webdav.server.StreamingWebdavResponse;
import eu.agno3.runtime.webdav.server.impl.DavResourceWrapper;
import eu.agno3.runtime.webdav.server.properties.StreamableMultiStatusResponse;


/**
 * @author mbechler
 * @param <T>
 *
 */
public class ColSyncReport <T> implements Report, StreamingReport {

    /**
     * 
     */
    private static final QName SYNC_TRAVERSAL_SUPPORTED = new QName(DavConstants.NAMESPACE.getURI(), "sync-traversal-supported"); //$NON-NLS-1$
    /**
     * 
     */
    private static final String SYNC_TOKEN = "sync-token"; //$NON-NLS-1$
    private static final String SYNC_LEVEL = "sync-level"; //$NON-NLS-1$

    /**
     * 
     */
    private static final String LIMIT = "limit"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String REPORT_NAME = "sync-collection"; //$NON-NLS-1$

    /**
     * 
     */
    public static final ReportType REPORT_TYPE = ReportType.register(REPORT_NAME, DavConstants.NAMESPACE, ColSyncReport.class);

    private MultiStatusResponse[] responses;
    private String syncToken;
    private String responseSyncToken;
    private boolean oneLevel = true;
    private long limit = -1;

    private DavWrapperFactory<T> factory;
    private DavResourceWrapper<T> context;
    private StreamingWebdavResponse response;
    private StreamingContext stream;

    private int state = -1;
    private ReportInfo report;
    private boolean failed;


    /**
     * 
     */
    public ColSyncReport () {}


    /**
     * @param item
     * @param response
     * @param factory
     */
    public ColSyncReport ( DavResourceWrapper<T> item, StreamingWebdavResponse response, DavWrapperFactory<T> factory ) {
        this.context = item;
        this.response = response;
        this.factory = factory;
        this.state = 0;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.version.report.Report#getType()
     */
    @Override
    public ReportType getType () {
        return REPORT_TYPE;
    }


    @Override
    public void init ( DavResource resource, ReportInfo info ) throws DavException {
        if ( resource == null || info == null ) {
            throw new DavException(HttpServletResponse.SC_BAD_REQUEST, "Unable to run report: WebDAV Resource and ReportInfo must not be null."); //$NON-NLS-1$
        }
        if ( !getType().isRequestedReportType(info) ) {
            throw new DavException(
                HttpServletResponse.SC_BAD_REQUEST,
                String.format("Expected report type: '%s', found '%s'", getType().getReportName(), info.getReportName())); //$NON-NLS-1$
        }
        if ( info.getDepth() > DavConstants.DEPTH_0 ) {
            throw new DavException(HttpServletResponse.SC_BAD_REQUEST, "Invalid Depth header: " + info.getDepth()); //$NON-NLS-1$
        }

        if ( !info.containsContentElement(SYNC_TOKEN, DavConstants.NAMESPACE) ) { // $NON-NLS-1$
            throw new DavException(HttpServletResponse.SC_BAD_REQUEST, "Missing sync-token element"); //$NON-NLS-1$
        }
        this.syncToken = info.getContentElement(SYNC_TOKEN, DavConstants.NAMESPACE).getTextContent();

        if ( !info.containsContentElement(SYNC_LEVEL, DavConstants.NAMESPACE) ) { // $NON-NLS-1$
            throw new DavException(HttpServletResponse.SC_BAD_REQUEST, "Missing sync-level element"); //$NON-NLS-1$
        }
        String syncLevelStr = info.getContentElement(SYNC_LEVEL, DavConstants.NAMESPACE).getTextContent();
        if ( "infinity".equals(syncLevelStr.trim()) ) { //$NON-NLS-1$
            this.oneLevel = false;
        }
        else if ( "1".equals(syncLevelStr.trim()) ) { //$NON-NLS-1$
            this.oneLevel = true;
        }
        else {
            throw new DavException(HttpServletResponse.SC_BAD_REQUEST, "Invalid sync-level element"); //$NON-NLS-1$
        }

        if ( info.containsContentElement(LIMIT, DavConstants.NAMESPACE) ) {
            Element lim = info.getContentElement(LIMIT, DavConstants.NAMESPACE);
            NodeList nresults = lim.getElementsByTagNameNS(DavConstants.NAMESPACE.getURI(), "nresults"); //$NON-NLS-1$
            if ( nresults.getLength() > 0 ) {
                Node n = nresults.item(0);
                if ( n instanceof Element ) {
                    this.limit = Long.parseLong(n.getTextContent());
                }
            }
        }
        else {
            this.limit = -1;
        }

        this.report = info;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.StreamingReport#close()
     */
    @Override
    public void close () throws IOException {
        if ( !this.failed ) {
            ensureStarted();
            if ( this.stream != null ) {
                int st = this.state;
                this.state = -1;
                if ( st >= 2 ) {
                    writeEnd();
                }
                this.stream.close();
                this.stream = null;
            }
        }
    }


    /**
     * @throws IOException
     * 
     */
    private void writeEnd () throws IOException {
        this.state = 1;
        XMLStreamWriter sw = this.stream.getStreamWriter();
        try {
            sw.writeStartElement(DavConstants.NAMESPACE.getURI(), "sync-token"); //$NON-NLS-1$
            if ( !StringUtils.isBlank(this.responseSyncToken) ) {
                sw.writeCharacters(this.responseSyncToken);
            }
            sw.writeEndElement();
        }
        catch ( XMLStreamException e ) {
            throw new IOException("Failed to write colsync trailer", e); //$NON-NLS-1$
        }
    }


    /**
     * Ensure that writing the response has started
     * 
     * @throws IOException
     */
    public synchronized void ensureStarted () throws IOException {
        if ( this.context == null || this.response == null || this.factory == null || this.report == null ) {
            throw new IllegalStateException("Not set up for streaming"); //$NON-NLS-1$
        }

        if ( this.state < 0 ) {
            throw new IOException("Error occured writing stream response"); //$NON-NLS-1$
        }

        if ( this.state == 0 ) {
            this.state = 1;
            this.stream = this.response.getStreamingContext();
            this.stream.startMultiStatus();
            this.state = 2;
        }
    }


    /**
     * @param node
     * @param i
     * @throws IOException
     */
    private void emit ( DAVTreeNode<T> node, String absPath, int status ) throws IOException {
        ensureStarted();
        DavResourceLocator locator = this.context.getLocator().getFactory()
                .createResourceLocator(this.context.getLocator().getPrefix(), this.context.getLocator().getWorkspacePath(), absPath);
        StreamableMultiStatusResponse msr = new StreamableMultiStatusResponse(locator.getHref(node.isCollection()), new Status(status), null);
        this.stream.write(msr);
    }


    /**
     * Emit a 200 response
     * 
     * @param node
     * @throws IOException
     */
    public void emit200 ( DAVTreeNode<T> node ) throws IOException {
        ensureStarted();
        DavResource resource = this.factory.getWrapperFor(this.context, node);
        StreamableMultiStatusResponse msr = new StreamableMultiStatusResponse(resource, this.report.getPropertyNameSet());
        this.stream.write(msr);
    }


    /**
     * Emit a 404 response
     * 
     * @param node
     * @param absPath
     * @throws IOException
     */
    public void emit404 ( DAVTreeNode<T> node, String absPath ) throws IOException {
        emit(node, absPath, HttpServletResponse.SC_NOT_FOUND);
    }


    /**
     * Emit a 403 response
     * 
     * @param node
     * @param absPath
     * @param allowSubtree
     * @throws IOException
     */
    public void emit403 ( DAVTreeNode<T> node, String absPath, boolean allowSubtree ) throws IOException {
        if ( allowSubtree ) {
            ensureStarted();
            DavResourceLocator locator = this.context.getLocator().getFactory()
                    .createResourceLocator(this.context.getLocator().getPrefix(), this.context.getLocator().getWorkspacePath(), absPath);
            StreamableMultiStatusResponse msr = new StreamableMultiStatusResponse(
                locator.getHref(node.isCollection()),
                new Status(403),
                null,
                Arrays.asList(SYNC_TRAVERSAL_SUPPORTED));
            this.stream.write(msr);
        }
        emit(node, absPath, HttpServletResponse.SC_FORBIDDEN);
    }


    /**
     * @return the syncToken
     */
    public String getSyncToken () {
        return this.syncToken;
    }


    /**
     * @return the responseSyncToken
     */
    public String getResponseSyncToken () {
        return this.responseSyncToken;
    }


    /**
     * @param responseSyncToken
     *            the responseSyncToken to set
     */
    public void setResponseSyncToken ( String responseSyncToken ) {
        this.responseSyncToken = responseSyncToken;
    }


    /**
     * @return the oneLevel
     */
    public boolean isOneLevel () {
        return this.oneLevel;
    }


    /**
     * @return the limit
     */
    public long getLimit () {
        return this.limit;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.version.report.Report#isMultiStatusReport()
     */
    @Override
    public boolean isMultiStatusReport () {
        return true;
    }


    /**
     * @param resp
     */
    public void setResponses ( MultiStatusResponse[] resp ) {
        this.responses = resp;
    }


    @Override
    public Element toXml ( Document document ) {
        MultiStatus ms = new MultiStatus();
        if ( this.responses != null ) {
            for ( MultiStatusResponse r : this.responses ) {
                ms.addResponse(r);
            }
        }
        Element xml = ms.toXml(document);
        Element tokenElem = document.createElementNS(DavConstants.NAMESPACE.getURI(), "sync-token"); //$NON-NLS-1$
        tokenElem.setTextContent(this.responseSyncToken);
        xml.appendChild(tokenElem);
        return xml;
    }


    /**
     * 
     */
    @Override
    public void setFailed () {
        this.failed = true;
    }

}
