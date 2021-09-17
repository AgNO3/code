/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 12, 2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.graph;


import java.io.Serializable;

import org.joda.time.DateTime;
import org.primefaces.model.chart.ChartModel;


/**
 * @author mbechler
 *
 */
public class Graph implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6942729153584712254L;

    private ChartModel model;
    private GraphData data;
    private String widgetVar;

    private DateTime lastUpdate;

    private String type;


    /**
     * @param widgetVar
     * @param gd
     * @param m
     * @param type
     */
    public Graph ( String widgetVar, GraphData gd, ChartModel m, String type ) {
        this.widgetVar = widgetVar;
        this.data = gd;
        this.model = m;
        this.type = type;
    }


    public String getWidgetVar () {
        return this.widgetVar;
    }


    /**
     * @return the type
     */
    public String getType () {
        return this.type;
    }


    /**
     * @return the model
     */
    public ChartModel getModel () {
        return this.model;
    }


    /**
     * @return the data
     */
    public GraphData getData () {
        return this.data;
    }


    /**
     * @return the last incremental update time
     */
    public DateTime getLastUpdate () {
        return this.lastUpdate;
    }


    /**
     * @param lastUpdate
     *            the lastUpdate to set
     */
    public void setLastUpdate ( DateTime lastUpdate ) {
        this.lastUpdate = lastUpdate;
    }


    /**
     * @param gd
     */
    public void update ( GraphData gd ) {
        setLastUpdate(new DateTime(gd.getDataEnd()));
    }

}
