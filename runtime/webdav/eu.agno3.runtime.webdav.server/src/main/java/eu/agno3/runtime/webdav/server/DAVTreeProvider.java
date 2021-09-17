/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2015 by mbechler
 */
package eu.agno3.runtime.webdav.server;


import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.Status;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.PropEntry;
import org.apache.jackrabbit.webdav.search.QueryGrammerSet;
import org.apache.jackrabbit.webdav.search.SearchInfo;
import org.apache.jackrabbit.webdav.security.report.PrincipalMatchReport;
import org.apache.jackrabbit.webdav.version.report.Report;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;

import eu.agno3.runtime.webdav.server.acl.FixedPrincipalSearchReport;
import eu.agno3.runtime.webdav.server.acl.PrincipalSearchProperty;
import eu.agno3.runtime.webdav.server.acl.ReplacedAclProperty;
import eu.agno3.runtime.webdav.server.colsync.ColSyncReport;


/**
 * @author mbechler
 * @param <T>
 *            node identifier type
 *
 */
public interface DAVTreeProvider <T> {

    /**
     * @param n
     * @param outputStream
     * @throws IOException
     */
    void writeOutput ( DAVTreeNode<T> n, OutputStream outputStream ) throws IOException;


    /**
     * @return the root node
     * @throws DavException
     */
    DAVTreeNode<T> getRootNode () throws DavException;


    /**
     * @param repositoryPath
     * @return the resource with the given path
     * @throws DavException
     */
    DAVTreeNode<T> getResourceWithPath ( String repositoryPath ) throws DavException;


    /**
     * @param node
     * @return the node's children
     * @throws DavException
     */
    Collection<DAVTreeNode<T>> getNodeChildren ( DAVTreeNode<T> node ) throws DavException;


    /**
     * @param parent
     * @param toCreate
     * @param name
     * @param input
     * @return the created node
     * @throws DavException
     */
    DAVTreeNode<T> create ( DAVTreeNode<T> parent, DAVTreeNode<T> toCreate, String name, InputContext input ) throws DavException;


    /**
     * @param parent
     * @param name
     * @param context
     * @return the created node
     * @throws DavException
     */
    DAVTreeNode<T> createCollection ( DAVTreeNode<T> parent, String name, InputContext context ) throws DavException;


    /**
     * @param node
     * @throws DavException
     */
    void delete ( DAVTreeNode<T> node ) throws DavException;


    /**
     * @param node
     * @param context
     * @return the updated node
     * @throws DavException
     */
    DAVTreeNode<T> update ( DAVTreeNode<T> node, InputContext context ) throws DavException;


    /**
     * @param node
     * @param targetName
     * @return the renamed entity
     * @throws DavException
     */
    DAVTreeNode<T> rename ( DAVTreeNode<T> node, String targetName ) throws DavException;


    /**
     * @param wrapped
     * @param changeList
     * @return status codes for the property changes
     * @throws DavException
     */
    Map<? extends PropEntry, ExtendedStatus> alterProperties ( DAVTreeNode<T> wrapped, List<? extends PropEntry> changeList ) throws DavException;


    /**
     * @param item
     * @param toParent
     * @param targetName
     * @return the moved entity
     * @throws DavException
     */
    DAVTreeNode<T> move ( DAVTreeNode<T> item, DAVTreeNode<T> toParent, String targetName ) throws DavException;


    /**
     * @param wrapped
     * @param acl
     * @throws DavException
     */
    void alterAcl ( DAVTreeNode<T> wrapped, ReplacedAclProperty acl ) throws DavException;


    /**
     * @param wrapped
     * @param report
     * @param res
     * @return acl report
     * @throws DavException
     */
    Report getReport ( DAVTreeNode<T> wrapped, ReportInfo report, Report res ) throws DavException;


    /**
     * @param root
     * @return supported query grammars
     */
    QueryGrammerSet getQueryGrammarSet ( DAVTreeNode<T> root );


    /**
     * @param root
     * @param search
     * @return search result multi status
     * @throws DavException
     */
    MultiStatus search ( DAVTreeNode<T> root, SearchInfo search ) throws DavException;


    /**
     * @param wrapped
     * @param inputContext
     * @throws IOException
     * @throws DavException
     */
    void patch ( DAVTreeNode<T> wrapped, InputContext inputContext ) throws DavException, IOException;


    /**
     * @param wrapped
     * @return the acceptable patch formats
     */
    Collection<String> getAcceptablePatchFormats ( DAVTreeNode<T> wrapped );


    /**
     * @param wrapped
     * @param name
     * @return the property value, or null if not available
     */
    DavProperty<?> getDynamicProperty ( DAVTreeNode<T> wrapped, DavPropertyName name );


    /**
     * @param n
     * @param output
     */
    void addDynamicHeaders ( DAVTreeNode<T> n, OutputContext output );


    /**
     * @param wrapped
     * @return the supported dynamic properties
     */
    Set<DavPropertyName> getSupportedDynamicProperties ( DAVTreeNode<T> wrapped );


    /**
     * @param wrapped
     * @param pm
     * @return tree nodes matching the request
     * @throws DavException
     */
    Map<DAVTreeNode<T>, Status> findPrincipalMatch ( DAVTreeNode<T> wrapped, PrincipalMatchReport pm ) throws DavException;


    /**
     * @param wrapped
     * @param ps
     * @return tree nodes matching the request
     * @throws DavException
     */
    Map<DAVTreeNode<T>, Status> findPrincipalSearch ( DAVTreeNode<T> wrapped, FixedPrincipalSearchReport ps ) throws DavException;


    /**
     * @param wrapped
     * @return the properties supported in principal search
     */
    Collection<PrincipalSearchProperty> getSupportedPrincipalSearchProperties ( DAVTreeNode<T> wrapped );


    /**
     * @param wrapped
     * @param cs
     * @throws DavException
     * @throws IOException
     */
    void syncCollectionStream ( DAVTreeNode<T> wrapped, ColSyncReport<T> cs ) throws DavException, IOException;


    /**
     * @param child
     * @return canonical path to the node
     * @throws DavException
     */
    String getAbsolutePath ( DAVTreeNode<T> child ) throws DavException;

}
