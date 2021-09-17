/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.03.2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.logs;


import javax.faces.view.ViewScoped;
import javax.inject.Named;


/**
 * @author mbechler
 *
 */

@Named ( "dashboardLogEventTable" )
@ViewScoped
public class DashboardLogEventTable extends LogEventTable {

    /**
     * 
     */
    private static final long serialVersionUID = 8049390268823885359L;


    /**
     * 
     */
    public DashboardLogEventTable () {
        setFollow(true);
        setPageSize(10);
        setRelativeTime(true);
    }
}
