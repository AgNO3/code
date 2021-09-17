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
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.chart.MeterGaugeChartModel;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.system.info.SystemInformationException;
import eu.agno3.orchestrator.system.info.platform.CPUInformation;
import eu.agno3.orchestrator.system.info.platform.PlatformInformation;


/**
 * @author mbechler
 * 
 */
@Named ( "agentSysInfoLoadChartModel" )
public class LoadChartBean implements Serializable {

    private static final long serialVersionUID = -6424507510329117163L;

    @Inject
    private AgentSysInfoContextBean sysInfo;


    public String getLoad1BarStyle () {
        try {
            return getBarStyle(getCPUInformation().getLoad1());
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return StringUtils.EMPTY;
        }
    }


    public String getLoad1BarStyleClass () {
        try {
            return getBarStyleClass(getCPUInformation().getLoad1());
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return StringUtils.EMPTY;
        }
    }


    public String getLoad5BarStyle () {
        try {
            return getBarStyle(getCPUInformation().getLoad5());
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return StringUtils.EMPTY;
        }
    }


    public String getLoad5BarStyleClass () {
        try {
            return getBarStyleClass(getCPUInformation().getLoad5());
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return StringUtils.EMPTY;
        }
    }


    public String getLoad15BarStyle () {
        try {
            return getBarStyle(getCPUInformation().getLoad15());
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return StringUtils.EMPTY;
        }
    }


    public String getLoad15BarStyleClass () {
        try {
            return getBarStyleClass(getCPUInformation().getLoad15());
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return StringUtils.EMPTY;
        }
    }


    /**
     * @param load1
     * @return
     */
    private static String getBarStyleClass ( float load ) {
        if ( load > 1.0 ) {
            return "bg-color-failure"; //$NON-NLS-1$
        }
        else if ( load > 0.8 ) {
            return "bg-color-warning"; //$NON-NLS-1$
        }
        return "bg-color-ok"; //$NON-NLS-1$
    }


    /**
     * @param load1
     * @return
     */
    private static String getBarStyle ( float load ) {
        return String.format(Locale.ROOT, "width: %.2f%%", load * 50); //$NON-NLS-1$
    }


    /**
     * @return the model
     */
    public synchronized MeterGaugeChartModel getLoad1Model () {
        try {
            return makeChartModel(getCPUInformation().getLoad1());
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return new MeterGaugeChartModel();
        }
    }


    /**
     * @return the model
     */
    public synchronized MeterGaugeChartModel getLoad5Model () {
        try {
            return makeChartModel(getCPUInformation().getLoad5());
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return new MeterGaugeChartModel();
        }
    }


    /**
     * @return the model
     */
    public synchronized MeterGaugeChartModel getLoad15Model () {
        try {
            return makeChartModel(getCPUInformation().getLoad15());
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
            return new MeterGaugeChartModel();
        }
    }


    /**
     * @return
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    private static MeterGaugeChartModel makeChartModel ( float load )
            throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        List<Number> intervals = new ArrayList<>();
        intervals.add(0.8);
        intervals.add(1.0);
        intervals.add(2.0);

        MeterGaugeChartModel model = new MeterGaugeChartModel(load, intervals);
        setupGauge(model);
        model.setSeriesColors("57e964, ffa52f, cc6666"); //$NON-NLS-1$
        return model;

    }


    /**
     * @param model
     */
    private static void setupGauge ( MeterGaugeChartModel model ) {
        model.setShowTickLabels(false);
        model.setLabelHeightAdjust(110);
        model.setIntervalOuterRadius(50);
    }


    private CPUInformation getCPUInformation ()
            throws SystemInformationException, ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {
        PlatformInformation info = this.sysInfo.getPlatformInformation();
        if ( info == null ) {
            throw new SystemInformationException("Platform information unavailable"); //$NON-NLS-1$
        }

        CPUInformation memInfo = info.getCpuInformation();
        if ( memInfo == null ) {
            throw new SystemInformationException("CPU info unavailable"); //$NON-NLS-1$
        }
        return memInfo;

    }

}
