/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config;


import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeTreeNode;
import eu.agno3.orchestrator.config.model.realm.service.ConfigurationService;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;


/**
 * @author mbechler
 * 
 */
@Named ( "objectTypeProvider" )
@ApplicationScoped
public class ObjectTypeProvider {

    private static final Logger log = Logger.getLogger(ObjectTypeProvider.class);

    @Inject
    private ServerServiceProvider ssp;

    private List<String> objectTypeCache;

    private TreeNode objectTypeTreeCache;


    /**
     * 
     * @return the known object types
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public List<String> getObjectTypes () throws ModelServiceException, GuiWebServiceException {
        if ( this.objectTypeCache == null ) {
            this.objectTypeCache = new ArrayList<>(this.ssp.getService(ConfigurationService.class).getObjectTypes());

        }

        return this.objectTypeCache;
    }


    /**
     * 
     * @return a tree of known object types (using their parent relation)
     * @throws ModelServiceException
     * @throws GuiWebServiceException
     */
    public TreeNode getObjectTypeTree () throws ModelServiceException, GuiWebServiceException {
        if ( this.objectTypeTreeCache == null ) {
            this.objectTypeTreeCache = loadObjectTypeTree();
        }

        return this.objectTypeTreeCache;
    }


    /**
     * @return
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     */
    private TreeNode loadObjectTypeTree () throws ModelServiceException, GuiWebServiceException {
        List<ObjectTypeTreeNode> objectTypes = this.ssp.getService(ConfigurationService.class).getObjectTypeTrees();
        DefaultTreeNode root = new DefaultTreeNode();

        if ( objectTypes != null ) {
            for ( ObjectTypeTreeNode rootType : objectTypes ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Loading subtree " + rootType); //$NON-NLS-1$
                }
                buildTree(root, rootType);
            }
        }

        return root;
    }


    /**
     * @param parent
     * @param type
     */
    private void buildTree ( TreeNode parent, ObjectTypeTreeNode type ) {

        TreeNode n = new DefaultTreeNode(type.getObjectType(), parent);

        if ( type.getChildren() != null ) {
            for ( ObjectTypeTreeNode childNode : type.getChildren() ) {
                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("%s -> %s", type.getObjectType(), childNode.getObjectType())); //$NON-NLS-1$
                }
                buildTree(n, childNode);
            }
        }
    }
}
