/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.04.2016 by mbechler
 */
package eu.agno3.runtime.webdav.server.acl;


import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.HrefProperty;
import org.apache.jackrabbit.webdav.security.SecurityConstants;
import org.apache.jackrabbit.webdav.security.report.AbstractSecurityReport;
import org.apache.jackrabbit.webdav.security.report.PrincipalSearchReport;
import org.apache.jackrabbit.webdav.version.report.Report;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.w3c.dom.Element;


/**
 * @author mbechler
 *
 */
public class FixedPrincipalSearchReport extends AbstractSecurityReport {

    private String[] searchRoots;
    private SearchArgument[] searchArguments;


    /**
     * @see Report#init(DavResource, ReportInfo)
     */
    @Override
    public void init ( DavResource resource, ReportInfo info ) throws DavException {
        super.init(resource, info);
        // make sure the request body contains all mandator elements
        if ( !info.containsContentElement(PrincipalSearchReport.XML_PROPERTY_SEARCH, SecurityConstants.NAMESPACE) ) {
            throw new DavException(HttpServletResponse.SC_BAD_REQUEST, "Request body must contain at least a single DAV:property-search element."); //$NON-NLS-1$
        }
        List<Element> psElements = info.getContentElements(PrincipalSearchReport.XML_PROPERTY_SEARCH, SecurityConstants.NAMESPACE);
        this.searchArguments = new SearchArgument[psElements.size()];
        Iterator<Element> it = psElements.iterator();
        int i = 0;
        while ( it.hasNext() ) {
            this.searchArguments[ i++ ] = new SearchArgument(it.next());
        }

        if ( info.containsContentElement(PrincipalSearchReport.XML_APPLY_TO_PRINCIPAL_COLLECTION_SET, SecurityConstants.NAMESPACE) ) {
            HrefProperty p = new HrefProperty(resource.getProperty(SecurityConstants.PRINCIPAL_COLLECTION_SET));
            this.searchRoots = p.getHrefs().toArray(new String[0]);
        }
        else {
            this.searchRoots = new String[] {
                resource.getHref()
            };
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.jackrabbit.webdav.version.report.Report#getType()
     */
    @Override
    public ReportType getType () {
        return PrincipalSearchReport.REPORT_TYPE;
    }


    // ------------------------------------< implementation specific methods >---
    /**
     * Retrieve the the locations where the search should be performed.<br>
     * Note, that the search result must be converted to {@link MultiStatusResponse}s
     * that must be returned back to this report.
     *
     * @return href of collections that act as start for the search.
     * @see #setResponses(MultiStatusResponse[])
     */
    public String[] getSearchRoots () {
        return this.searchRoots;
    }


    /**
     * Retrive the search arguments used to run the search for principals.<br>
     * Note, that the search result must be converted to {@link MultiStatusResponse}s
     * that must be returned back to this report.
     *
     * @return array of <code>SearchArgument</code> used to run the principal
     *         search.
     * @see #setResponses(MultiStatusResponse[])
     */
    public SearchArgument[] getSearchArguments () {
        return this.searchArguments;
    }


    /**
     * Write the search result back to the report.
     *
     * @param responses
     */
    public void setResponses ( MultiStatusResponse[] responses ) {
        this.responses = responses;
    }

    // --------------------------------< implementation specific inner class >---
    /**
     * Inner utility class preparing the query arguments present in the
     * DAV:property-search element(s).
     */
    public class SearchArgument {

        private final DavPropertyNameSet searchProps;
        private final String searchString;


        SearchArgument ( Element propSearch ) {
            this.searchProps = new DavPropertyNameSet(DomUtil.getChildElement(propSearch, DavConstants.XML_PROP, DavConstants.NAMESPACE));
            this.searchString = DomUtil.getChildText(propSearch, PrincipalSearchReport.XML_MATCH, SecurityConstants.NAMESPACE);
        }


        /**
         * @return property name set used to restrict the search to a limited
         *         amount of properties.
         */
        public DavPropertyNameSet getSearchProperties () {
            return this.searchProps;
        }


        /**
         * @return query string as present in the DAV:match element.
         */
        public String getSearchString () {
            return this.searchString;
        }
    }

}
