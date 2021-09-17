/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2015 by mbechler
 */
package eu.agno3.runtime.webdav.server;


import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.joda.time.DateTime;


/**
 * @author mbechler
 * @param <T>
 *
 */
public interface DAVTreeNode <T> {

    /**
     * @return an node id if known
     */
    T getId ();


    /**
     * @return whether this is a collection
     */
    boolean isCollection ();


    /**
     * @return creation time
     */
    DateTime getCreationTime ();


    /**
     * @return modification time
     */
    DateTime getModificationTime ();


    /**
     * @return the display name
     */
    String getDisplayName ();


    /**
     * 
     * @return the local path name
     */
    String getPathName ();


    /**
     * @return the content type
     */
    String getContentType ();


    /**
     * @return the content length
     */
    Long getContentLength ();


    /**
     * @return the etag, if there is one
     */
    String getETag ();


    /**
     * @return whether this node provides a custom resource type via extraProperties
     */
    boolean isOverrideResourceType ();


    /**
     * @param l
     *            user locale
     * @return extra properties
     */
    Collection<DavProperty<?>> getExtraProperties ( Locale l );


    /**
     * 
     * @return the reports supported on this node
     */
    Set<ReportType> getSupportedReports ();


    /**
     * @return custom headers to set
     */
    Map<String, String> getCustomHeaders ();


    /**
     * 
     * @return absolute path to this node, null if not applicable
     */
    String getAbsolutePath ();


    /**
     * @return the methods supported by this resource
     */
    Collection<String> getSupportedMethods ();

}
