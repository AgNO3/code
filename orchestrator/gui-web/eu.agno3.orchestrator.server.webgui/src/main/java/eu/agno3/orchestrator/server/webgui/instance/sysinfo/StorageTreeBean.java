/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.03.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.instance.sysinfo;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.system.info.storage.drive.Drive;
import eu.agno3.orchestrator.system.info.storage.drive.RAIDDrive;
import eu.agno3.orchestrator.system.info.storage.drive.VolumeGroup;
import eu.agno3.orchestrator.system.info.storage.volume.LogicalVolume;
import eu.agno3.orchestrator.system.info.storage.volume.SystemVolume;
import eu.agno3.orchestrator.system.info.storage.volume.Volume;


/**
 * @author mbechler
 * 
 */
@Named ( "agentSysInfoStorageTree" )
@ViewScoped
public class StorageTreeBean implements Serializable {

    private static final String VOLUME_TYPE_SYSTEM = "system"; //$NON-NLS-1$
    private static final String VOLUME_TYPE_LV = "lv"; //$NON-NLS-1$
    private static final String VOLUME_TYPE_PARTITION = "partition"; //$NON-NLS-1$
    private static final String DRIVE_TYPE_RAID = "raid"; //$NON-NLS-1$
    private static final String DRIVE_TYPE_VG = "vg"; //$NON-NLS-1$
    private static final String DRIVE_TYPE_PHYS = "drive"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(StorageTreeBean.class);
    private static final long serialVersionUID = 1438773421459938335L;

    private TreeNode root;
    private TreeNode selectedNode;

    @Inject
    private AgentSysInfoContextBean sysInfo;


    /**
     * @return the root
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    public TreeNode getRoot () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        if ( this.root == null ) {
            this.root = makeTree();
        }
        return this.root;
    }


    /**
     * @return the selectedInterface
     */
    public TreeNode getSelectedNode () {
        return this.selectedNode;
    }


    public void setSelectedNode ( TreeNode node ) {
        if ( node != null && log.isDebugEnabled() ) {
            log.debug("Node selected: " + node.getData()); //$NON-NLS-1$
        }
        this.selectedNode = node;
    }


    public boolean isNodeSelected () {
        return this.selectedNode != null;
    }


    public String getPanelActive () {
        if ( this.isNodeSelected() ) {
            return "0,1"; //$NON-NLS-1$
        }
        return "0"; //$NON-NLS-1$
    }


    public void setPanelActive ( String active ) {
        // ignore
    }


    /**
     * @return
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    private TreeNode makeTree () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        DefaultTreeNode r = new DefaultTreeNode("root", null, null); //$NON-NLS-1$
        List<Drive> drives = new ArrayList<>(this.sysInfo.getStorageInformation().getDrives());
        Collections.sort(drives, new DriveDisplayComparator());
        for ( Drive drive : drives ) {
            makeDriveNode(drive, r);
        }
        return r;
    }


    /**
     * @param drive
     * @param parent
     */
    private static void makeDriveNode ( Drive drive, DefaultTreeNode parent ) {
        DefaultTreeNode t = new DefaultTreeNode(getDriveType(drive), drive, parent);
        t.setRowKey(drive.getId());
        List<Volume> vols = new ArrayList<>(drive.getVolumes());

        Collections.sort(vols, new VolumeDisplayComparator());

        int volIdx = 0;
        for ( Volume v : vols ) {
            makeVolumeNode(v, t, volIdx);
            volIdx++;
        }
    }


    private static String getDriveType ( Drive drive ) {
        if ( drive instanceof VolumeGroup ) {
            return DRIVE_TYPE_VG;
        }
        else if ( drive instanceof RAIDDrive ) {
            return DRIVE_TYPE_RAID;
        }
        return DRIVE_TYPE_PHYS;
    }


    /**
     * @param v
     * @param t
     * @param volIdx
     */
    private static void makeVolumeNode ( Volume v, DefaultTreeNode parent, int volIdx ) {
        DefaultTreeNode t = new DefaultTreeNode(getVolumeType(v), v, parent);
        t.setRowKey(t.getRowKey() + "-" + volIdx); //$NON-NLS-1$
    }


    /**
     * @param v
     * @return
     */
    private static String getVolumeType ( Volume v ) {
        String type = VOLUME_TYPE_PARTITION;

        if ( v instanceof LogicalVolume ) {
            type = VOLUME_TYPE_LV;
        }
        else if ( v instanceof SystemVolume ) {
            type = VOLUME_TYPE_SYSTEM;
        }

        return type;
    }

}
