/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.04.2016 by mbechler
 */
package eu.agno3.runtime.webdav.server.acl;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.version.report.Report;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * @author mbechler
 *
 */
public class PrincipalPropertySearchPropertySetReport implements Report {

    /**
     * 
     */
    public static final String REPORT_NAME = "principal-search-property-set"; //$NON-NLS-1$

    /**
     * 
     */
    public static final ReportType REPORT_TYPE = ReportType
            .register(REPORT_NAME, DavConstants.NAMESPACE, PrincipalPropertySearchPropertySetReport.class);

    private Set<PrincipalSearchProperty> supportedProperties = new HashSet<>();


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.xml.XmlSerializable#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml ( Document doc ) {
        Element root = doc.createElementNS(DavConstants.NAMESPACE.getURI(), REPORT_NAME);
        for ( PrincipalSearchProperty prop : this.supportedProperties ) {
            root.appendChild(prop.toXml(doc));
        }
        return root;
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


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.version.report.Report#init(org.apache.jackrabbit.webdav.DavResource,
     *      org.apache.jackrabbit.webdav.version.report.ReportInfo)
     */
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
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.version.report.Report#isMultiStatusReport()
     */
    @Override
    public boolean isMultiStatusReport () {
        return false;
    }


    /**
     * @param props
     */
    public void setSupportedProperties ( Collection<PrincipalSearchProperty> props ) {
        this.supportedProperties = new HashSet<>(props);
    }

}
