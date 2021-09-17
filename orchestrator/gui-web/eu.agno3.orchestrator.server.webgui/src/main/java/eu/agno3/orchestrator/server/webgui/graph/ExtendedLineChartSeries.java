/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 7, 2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.graph;


import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.LineChartSeries;


/**
 * @author mbechler
 *
 */
public class ExtendedLineChartSeries extends LineChartSeries {

    /**
     * 
     */
    private static final long serialVersionUID = 3945698523335191988L;

    private Map<Object, Number> lowerBand = new LinkedHashMap<>();
    private Map<Object, Number> upperBand = new LinkedHashMap<>();


    public void set ( Object x, Number y, Number lower, Number upper ) {
        super.set(x, y);

        if ( lower != null ) {
            this.lowerBand.put(x, lower);
        }

        if ( upper != null ) {
            this.upperBand.put(x, upper);
        }
    }


    @Override
    @SuppressWarnings ( "nls" )
    public void encode ( Writer writer ) throws IOException {

        String renderer = this.getRenderer();
        AxisType xaxis = this.getXaxis();
        AxisType yaxis = this.getYaxis();

        writer.write("{");
        writer.write("label:'" + this.getLabel() + "'");
        writer.write(",renderer: $.jqplot." + renderer);

        if ( xaxis != null )
            writer.write(",xaxis:\"" + xaxis + "\"");
        if ( yaxis != null )
            writer.write(",yaxis:\"" + yaxis + "\"");
        if ( this.isDisableStack() )
            writer.write(",disableStack:true");

        /*
         * if ( this.isFill() ) {
         * writer.write(",fill:true");
         * writer.write(",fillAlpha:" + this.getFillAlpha());
         * }
         */

        writer.write(",showLine:" + this.isShowLine());
        writer.write(",markerOptions:{show:" + this.isShowMarker() + ", style:'" + this.getMarkerStyle() + "'}");

        Map<Object, Number> lband = this.lowerBand;
        Map<Object, Number> hband = this.upperBand;
        if ( !lband.isEmpty() || !hband.isEmpty() ) {
            writer.write(",rendererOptions:{bandData:[");
            writeBandData(writer, lband);
            writer.write(",");
            writeBandData(writer, hband);
            writer.write("]}");
        }

        writer.write("}");
    }


    /**
     * @param writer
     * @param lband
     * @throws IOException
     */
    @SuppressWarnings ( "nls" )
    protected void writeBandData ( Writer writer, Map<Object, Number> lband ) throws IOException {
        writer.write("[");
        for ( Iterator<Object> x = getData().keySet().iterator(); x.hasNext(); ) {
            Object xValue = x.next();
            Number lValue = lband.get(xValue);
            String lValueAsString = ( lValue != null ) ? lValue.toString() : "null";

            writer.write(lValueAsString);

            if ( x.hasNext() ) {
                writer.write(",");
            }
        }
        writer.write("]");
    }
}
