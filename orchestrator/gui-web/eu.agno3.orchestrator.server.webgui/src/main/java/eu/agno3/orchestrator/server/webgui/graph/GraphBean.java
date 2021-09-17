/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 5, 2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.graph;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.primefaces.context.RequestContext;
import org.primefaces.model.chart.ChartModel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;


/**
 * @author mbechler
 *
 */
@Named ( "graphBean" )
@ViewScoped
public class GraphBean implements Serializable {

    private static final ObjectMapper OM = new ObjectMapper();


    static {
        OM.registerModule(new JodaModule());
    }

    /**
     * 
     */
    private static final long serialVersionUID = -1206379557840188093L;

    private static final Logger log = Logger.getLogger(GraphBean.class);

    @Inject
    private GraphBuilder graphBuilder;

    @Inject
    private FakeGraphService graphService;

    @Inject
    private StructureViewContextBean structureContext;

    private boolean graphsLoaded;
    private List<Graph> graphs;

    private DateTime startTime;
    private DateTime endTime;

    private boolean categoriesLoaded;
    private List<GraphCategory> categories;
    private GraphCategory defaultCategory;
    private GraphCategory selectedCategory;


    /**
     * @return the categories
     */
    public List<GraphCategory> getCategories () {
        if ( !this.categoriesLoaded ) {
            try {
                this.categories = this.graphService.getCategories(this.structureContext.getSelectedObject());
                if ( this.categories != null ) {
                    for ( GraphCategory c : this.categories ) {
                        if ( c.isDefault() ) {
                            this.defaultCategory = c;
                        }
                    }
                }
            }
            catch ( Exception e ) {
                ExceptionHandler.handle(e);
                this.categories = Collections.EMPTY_LIST;
            }
        }
        return this.categories;
    }


    /**
     * @return the selectedCategory
     */
    public GraphCategory getSelectedCategory () {
        if ( this.selectedCategory == null ) {
            getCategories();
            return this.defaultCategory;
        }
        return this.selectedCategory;
    }


    /**
     * @param selectedCategory
     *            the selectedCategory to set
     */
    public void setSelectedCategory ( GraphCategory selectedCategory ) {
        this.selectedCategory = selectedCategory;
    }


    public Collection<Graph> getGraphs () {
        if ( !this.graphsLoaded ) {
            try {
                this.graphs = initGraphs();
            }
            catch ( Exception e ) {
                ExceptionHandler.handle(e);
                this.graphs = Collections.EMPTY_LIST;
            }
        }
        return this.graphs;
    }


    /**
     * @return
     */
    private List<Graph> initGraphs () {
        List<Graph> gs = new ArrayList<>();

        GraphCategory selected = getSelectedCategory();
        if ( selected == null ) {
            return gs;
        }

        DateTime now = DateTime.now();
        DateTime s = this.startTime != null ? this.startTime : now.minusDays(1);
        DateTime e = this.endTime != null ? this.endTime : now;

        List<String> instances = selected.getInstances();
        List<GraphData> gds = this.graphService.getGraphs(this.structureContext.getSelectedObjectId(), instances, s, e);
        int i = 0;
        for ( GraphData gd : gds ) {
            try {
                ChartModel model = this.graphBuilder.makeChart(gd);
                String type = this.graphBuilder.mapType(gd.getDefinition());
                Graph g = new Graph(String.format("graph%d", i), gd, model, type); //$NON-NLS-1$
                g.setLastUpdate(new DateTime(gd.getDataEnd()));
                gs.add(g);
                i++;
            }
            catch ( Exception ex ) {
                ExceptionHandler.handle(ex);
            }
        }
        return gs;
    }


    public void fetchGraphData () {
        RequestContext currentInstance = RequestContext.getCurrentInstance();

        List<String> instances = this.graphs.stream().map(x -> x.getData().getInstanceId()).collect(Collectors.toList());
        Optional<Integer> resolution = this.graphs.stream().map(x -> x.getData().getResolution()).min(Comparator.naturalOrder());
        Optional<DateTime> start = this.graphs.stream().map(x -> x.getLastUpdate()).min(Comparator.naturalOrder());

        if ( !resolution.isPresent() || !start.isPresent() ) {
            return;
        }

        Map<String, Graph> byId = new HashMap<>();

        for ( Graph graph : this.graphs ) {
            byId.put(graph.getData().getInstanceId(), graph);
        }

        Map<String, Map<Integer, Object[][]>> data = new HashMap<>();
        for ( GraphData gd : this.graphService
                .getIncrementals(this.structureContext.getSelectedObjectId(), instances, start.get(), resolution.get()) ) {

            Graph graph = byId.get(gd.getInstanceId());
            if ( graph == null ) {
                continue;
            }
            makeIncrementalGraphData(data, graph, gd);
            graph.update(gd);
        }

        try {
            currentInstance.addCallbackParam("graphData", OM.writeValueAsString(data)); //$NON-NLS-1$
        }
        catch ( JsonProcessingException e ) {
            log.warn("Failed to create JSON graph data", e); //$NON-NLS-1$
        }
    }


    /**
     * @param data
     * @param graph
     * @param incData
     * @param gd
     */
    void makeIncrementalGraphData ( Map<String, Map<Integer, Object[][]>> data, Graph graph, GraphData gd ) {
        Map<Integer, Object[][]> incData = new HashMap<>();
        int idx = 0;
        for ( String ser : graph.getData().getDefinition().getSeries() ) {
            SeriesData series = gd.getSeries(ser);
            incData.put(idx, makeIncrementalSeriesData(gd, series));
            idx++;
        }

        data.put(graph.getWidgetVar(), incData);
    }


    /**
     * @param gd
     * @param series
     * @return
     */
    private static Object[][] makeIncrementalSeriesData ( GraphData gd, SeriesData series ) {
        long[] ts = gd.getTimestamps();
        double[] values = series.getValues();
        double[] upperBand = series.getUpperBand(); // $NON-NLS-1$
        double[] lowerBand = series.getLowerBand();
        if ( ts.length != values.length || ( upperBand != null && upperBand.length != ts.length )
                || ( lowerBand != null && lowerBand.length != ts.length ) ) {
            throw new IllegalArgumentException("Invalid graph data"); //$NON-NLS-1$
        }

        Object serData[][] = new Object[ts.length][];

        for ( int i = 0; i < ts.length; i++ ) {
            if ( upperBand != null && lowerBand != null ) {
                serData[ i ] = new Object[] {
                    ts[ i ], values[ i ], lowerBand[ i ], upperBand[ i ]
                };
            }
            else {
                serData[ i ] = new Object[] {
                    ts[ i ], values[ i ]
                };
            }
        }
        return serData;
    }

}
