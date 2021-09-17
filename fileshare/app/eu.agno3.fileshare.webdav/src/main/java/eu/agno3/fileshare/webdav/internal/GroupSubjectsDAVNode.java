/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.04.2016 by mbechler
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
public class GroupSubjectsDAVNode extends SubjectBaseNode {

    /**
     * 
     */
    public static final String SUBJECTS_GROUPS = SubjectsDAVNode.SUBJECTS_PATH + "/groups"; //$NON-NLS-1$


    /**
     * @param l
     * 
     */
    public GroupSubjectsDAVNode ( DAVLayout l ) {
        super("groups", l); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getAbsolutePath()
     */
    @Override
    public String getAbsolutePath () {
        return SUBJECTS_GROUPS; // $NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getSupportedReports()
     */
    @Override
    public Set<ReportType> getSupportedReports () {
        return new HashSet<>(
            Arrays.asList(PrincipalMatchReport.REPORT_TYPE, PrincipalPropertySearchPropertySetReport.REPORT_TYPE, PrincipalSearchReport.REPORT_TYPE));
    }

}
