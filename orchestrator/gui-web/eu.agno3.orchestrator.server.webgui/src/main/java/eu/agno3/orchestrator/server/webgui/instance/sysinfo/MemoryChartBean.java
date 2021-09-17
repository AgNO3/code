/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.03.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.instance.sysinfo;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.primefaces.model.chart.MeterGaugeChartModel;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.system.info.SystemInformationException;
import eu.agno3.orchestrator.system.info.platform.MemoryInformation;
import eu.agno3.orchestrator.system.info.platform.PlatformInformation;


/**
 * @author mbechler
 * 
 */
@Named ( "agentSysInfoMemoryChartModel" )
public class MemoryChartBean implements Serializable {

    private static final Logger log = Logger.getLogger(MemoryChartBean.class);
    private static final long serialVersionUID = -6424507510329117163L;

    @Inject
    private AgentSysInfoContextBean sysInfo;


    /**
     * @return the model
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    public synchronized MeterGaugeChartModel getPhysicalModel () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return this.makePhysicalChartModel();
    }


    /**
     * @return the swapModel
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    public MeterGaugeChartModel getSwapModel () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        return this.makeSwapChartModel();
    }


    /**
     * @return
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    private MeterGaugeChartModel makeSwapChartModel () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        List<Number> intervals = new ArrayList<>();
        try {
            MemoryInformation memInfo = getMemoryInformation();
            intervals.add(memInfo.getCurrentSwapMemoryUsed());
            intervals.add(memInfo.getTotalSwapMemory());

            MeterGaugeChartModel model = new MeterGaugeChartModel(memInfo.getCurrentSwapMemoryUsed(), intervals);
            setupGauge(model);
            model.setSeriesColors("E55533, 19A13D"); //$NON-NLS-1$
            return model;
        }
        catch ( SystemInformationException e ) {
            log.warn("Failed to get swap information:", e); //$NON-NLS-1$
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage()));
            return new MeterGaugeChartModel();
        }
    }


    /**
     * @return
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    private MeterGaugeChartModel makePhysicalChartModel () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        List<Number> intervals = new ArrayList<>();
        try {
            MemoryInformation memInfo = getMemoryInformation();
            long memoryUsed = memInfo.getCurrentPhysicalMemoryUsedTotal() - memInfo.getCurrentPhysicalMemoryUsedBuffers()
                    - memInfo.getCurrentPhysicalMemoryUsedCache();
            intervals.add(memoryUsed);
            intervals.add(memInfo.getCurrentPhysicalMemoryUsedTotal() - memInfo.getCurrentPhysicalMemoryUsedBuffers());
            intervals.add(memInfo.getCurrentPhysicalMemoryUsedTotal());
            intervals.add(memInfo.getTotalPhysicalMemory());

            MeterGaugeChartModel model = new MeterGaugeChartModel(memInfo.getCurrentPhysicalMemoryUsedTotal(), intervals);
            setupGauge(model);
            model.setSeriesColors("E55533, E5A333, ffd801, 19A13D"); //$NON-NLS-1$
            return model;
        }
        catch ( SystemInformationException e ) {
            log.warn("Failed to get system information:", e); //$NON-NLS-1$
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage()));
            return new MeterGaugeChartModel();
        }
    }


    /**
     * @param model
     */
    private static void setupGauge ( MeterGaugeChartModel model ) {
        model.setShowTickLabels(false);
        model.setLabelHeightAdjust(110);
        model.setIntervalOuterRadius(50);
    }


    private MemoryInformation getMemoryInformation ()
            throws SystemInformationException, ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        PlatformInformation info = this.sysInfo.getPlatformInformation();
        if ( info == null ) {
            throw new SystemInformationException("Platform information unavailable"); //$NON-NLS-1$
        }

        MemoryInformation memInfo = info.getMemoryInformation();
        if ( memInfo == null ) {
            throw new SystemInformationException("Memory info unavailable"); //$NON-NLS-1$
        }
        return memInfo;
    }
}
