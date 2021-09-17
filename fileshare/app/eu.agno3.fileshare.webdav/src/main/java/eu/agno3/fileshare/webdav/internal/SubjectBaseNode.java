/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.04.2016 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.jackrabbit.webdav.security.report.PrincipalMatchReport;
import org.apache.jackrabbit.webdav.security.report.PrincipalSearchReport;
import org.apache.jackrabbit.webdav.version.report.ReportType;

import eu.agno3.runtime.webdav.server.acl.PrincipalPropertySearchPropertySetReport;


/**
 * @author mbechler
 *
 */
public abstract class SubjectBaseNode extends AbstractVirtualDAVNode {

    /**
     * @param name
     * @param layout
     */
    public SubjectBaseNode ( String name, DAVLayout layout ) {
        super(name, null, null, layout);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getInode()
     */
    @Override
    protected byte[] getInode () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getSupportedReports()
     */
    @Override
    public Set<ReportType> getSupportedReports () {
        return new HashSet<>(
            Arrays.asList(PrincipalMatchReport.REPORT_TYPE, PrincipalSearchReport.REPORT_TYPE, PrincipalPropertySearchPropertySetReport.REPORT_TYPE));
    }

}
