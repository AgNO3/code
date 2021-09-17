/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 12, 2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.graph;


import java.text.DecimalFormat;
import java.util.ResourceBundle;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.ChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.DateAxis;
import org.primefaces.model.chart.LegendPlacement;
import org.primefaces.model.chart.LineChartModel;

import eu.agno3.orchestrator.server.webgui.CoreServiceProvider;
import eu.agno3.orchestrator.server.webgui.util.i18n.I18NUtil;
import eu.agno3.runtime.i18n.ResourceBundleService;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class GraphBuilder {

    private static DecimalFormat NUMBER_FORMAT = new DecimalFormat("#.##"); //$NON-NLS-1$

    @Inject
    private CoreServiceProvider csp;


    public ChartModel makeChart ( GraphData gd ) {
        GraphDefinition def = gd.getDefinition();
        switch ( def.getType() ) {
        case TIMESERIES:
            return makeLineChart(gd);
        default:
            throw new IllegalArgumentException();
        }
    }


    public String mapType ( GraphDefinition def ) {
        switch ( def.getType() ) {
        case TIMESERIES:
            return "line"; //$NON-NLS-1$
        default:
            throw new IllegalArgumentException();
        }
    }


    /**
     * @param gd
     * @return chart model for the graph
     */
    public ChartModel makeLineChart ( GraphData gd ) {
        GraphDefinition def = gd.getDefinition();
        LineChartModel m = new LineChartModel();
        m.setTitle(formatString(def.getTitleString(), def.getTitleMsgId(), def.getMsgBundle()));

        for ( String dsName : def.getSeries() ) {
            m.addSeries(generateData(dsName, gd));
        }
        m.setZoom(true);
        m.setShowDatatip(false);
        m.setBreakOnNull(true);
        m.setShowPointLabels(false);
        m.setShadow(false);
        m.setExtender("chartExtender"); //$NON-NLS-1$
        m.setLegendPlacement(LegendPlacement.OUTSIDEGRID);
        m.setLegendPosition("e"); //$NON-NLS-1$

        Axis timeAxis = makeTimeAxis(gd);
        m.getAxes().put(AxisType.X, timeAxis);
        m.getAxis(AxisType.Y).setMin(0);
        return m;
    }


    /**
     * @param titleString
     * @param titleMsgId
     * @param msgBundle
     * @return
     */
    private String formatString ( String titleString, String titleMsgId, String msgBundle, Object... args ) {
        if ( !StringUtils.isBlank(titleMsgId) && !StringUtils.isBlank(msgBundle) ) {
            ResourceBundleService ls = this.csp.getLocalizationService();
            ResourceBundle b = ls.getBundle(msgBundle, FacesContext.getCurrentInstance().getViewRoot().getLocale(), this.getClass().getClassLoader());
            if ( args != null ) {
                return I18NUtil.format(b, titleMsgId, args);
            }
            return b.getString(titleMsgId);
        }
        return titleString;
    }


    /**
     * @param e
     * @param s
     * @return
     */
    Axis makeTimeAxis ( GraphData gd ) {
        Axis timeAxis = new DateAxis(); // $NON-NLS-1$
        timeAxis.setTickAngle(-50);
        timeAxis.setMin(gd.getStart());
        timeAxis.setMax(gd.getDataEnd());
        long minuteRange = ( gd.getEnd() - gd.getStart() ) / 60000;
        if ( minuteRange < 60 ) {
            timeAxis.setTickInterval("5 minute"); //$NON-NLS-1$
            timeAxis.setTickFormat("%d.%m.%Y %H:%M"); //$NON-NLS-1$
        }
        else if ( minuteRange <= 60 * 48 ) {
            timeAxis.setTickInterval("1 hour"); //$NON-NLS-1$
            timeAxis.setTickFormat("%d.%m.%Y %H:%M"); //$NON-NLS-1$
        }
        else if ( minuteRange <= 60 * 24 * 7 ) {
            timeAxis.setTickInterval("1 day"); //$NON-NLS-1$
            timeAxis.setTickFormat("%d.%m.%Y"); //$NON-NLS-1$
        }
        else {
            timeAxis.setTickInterval("1 week"); //$NON-NLS-1$
            timeAxis.setTickFormat("%d.%m.%Y"); //$NON-NLS-1$
        }
        timeAxis.setTickCount(10);
        return timeAxis;
    }


    /**
     * @param dproc
     * @return
     */
    private ChartSeries generateData ( String ds, GraphData data ) {
        SeriesData series = data.getSeries(ds);
        GraphSeriesDefinition sd = data.getDefinition().getSeries(ds);
        if ( series == null ) {
            throw new IllegalArgumentException();
        }
        ExtendedLineChartSeries c = new ExtendedLineChartSeries();
        c.setLabel(
            String.format(
                "<span class=\"series-title\">%s</span> " + //$NON-NLS-1$
                        "<span class=\"series-min\">%s</span> " + //$NON-NLS-1$
                        "<span class=\"series-max\">%s</span> " + //$NON-NLS-1$
                        "<span class=\"series-avg\">%s</span> " + //$NON-NLS-1$
                        "<span class=\"series-total\">%s</span> ", //$NON-NLS-1$
                formatString(sd.getTitleString(), sd.getTitleMsgId(), data.getDefinition().getMsgBundle()),
                NUMBER_FORMAT.format(series.getMinimum()),
                NUMBER_FORMAT.format(series.getMaximum()),
                NUMBER_FORMAT.format(series.getAverage()),
                NUMBER_FORMAT.format(series.getTotal())));

        c.setShowMarker(false);
        long[] ts = data.getTimestamps();
        double[] values = series.getValues();
        double[] upperBand = series.getUpperBand(); // $NON-NLS-1$
        double[] lowerBand = series.getLowerBand();
        if ( ( values != null && ts.length != values.length ) || ( upperBand != null && upperBand.length != ts.length )
                || ( lowerBand != null && lowerBand.length != ts.length ) ) {
            throw new IllegalArgumentException("Invalid graph data"); //$NON-NLS-1$
        }

        for ( int i = 0; i < ts.length; i++ ) {
            if ( values != null && lowerBand != null && upperBand != null && Double.isNaN(values[ i ]) ) {
                // lqplot does not handle band data with null values
                continue;
            }
            c.set(
                ts[ i ],
                values != null ? ( Double.isNaN(values[ i ]) ? null : values[ i ] ) : null,
                lowerBand != null ? ( Double.isNaN(lowerBand[ i ]) ? null : lowerBand[ i ] ) : null,
                upperBand != null ? ( Double.isNaN(upperBand[ i ]) ? null : upperBand[ i ] ) : null);
        }
        return c;
    }
}
