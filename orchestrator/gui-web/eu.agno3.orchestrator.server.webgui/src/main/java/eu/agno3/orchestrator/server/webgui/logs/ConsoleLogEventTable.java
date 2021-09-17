/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.03.2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.logs;


import javax.faces.view.ViewScoped;
import javax.inject.Named;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "consoleLogEventTable" )
public class ConsoleLogEventTable extends LogEventTable {

    /**
     * 
     */
    private static final long serialVersionUID = 2803549832204613860L;


    /**
     * 
     */
    public ConsoleLogEventTable () {
        setPageSize(10);
        setFollow(true);
    }
}
