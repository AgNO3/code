/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.02.2014 by mbechler
 */
package eu.agno3.runtime.jsf.view.stacking.overlay;


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.primefaces.context.RequestContext;


/**
 * @author mbechler
 * 
 */
public final class OverlayDialog {

    /**
     * 
     */
    private static final String OPTION_CLOSABLE = "closable"; //$NON-NLS-1$
    private static final String OPTION_DRAGGABLE = "draggable"; //$NON-NLS-1$
    private static final String OPTION_RESIZABLE = "resizable"; //$NON-NLS-1$
    private static final String OPTION_MODAL = "modal"; //$NON-NLS-1$
    private static final String OPTION_MAX_WIDTH = "maxContentWidth"; //$NON-NLS-1$


    private OverlayDialog () {}


    /**
     * Open a overlay dialog with default options
     * 
     * @param id
     * @param url
     * @param sourceComponentId
     * @param sourceWidget
     * @param closable
     */
    public static void openDialogOverlay ( String id, String url, String sourceComponentId, String sourceWidget, boolean closable ) {
        openDialogOverlay(id, url, sourceComponentId, sourceWidget, makeDialogOptions(closable));
    }


    /**
     * Open a overlay dialog
     * 
     * @param id
     * @param url
     * @param sourceComponentId
     * @param sourceWidget
     * @param options
     */
    public static void openDialogOverlay ( String id, String url, String sourceComponentId, String sourceWidget, Map<String, Object> options ) {
        String dialog = buildPrimefacesDialogJS(id, url, sourceComponentId, sourceWidget, options);
        RequestContext.getCurrentInstance().execute(dialog);
    }


    /**
     * @param id
     * @param url
     * @param sourceComponentId
     * @param sourceWidget
     * @param options
     * @return
     */
    private static String buildPrimefacesDialogJS ( String id, String url, String sourceComponentId, String sourceWidget,
            Map<String, Object> options ) {
        StringBuilder sb = new StringBuilder();
        sb.append("AgNO3DialogOverlay.openDialog({"); //$NON-NLS-1$
        sb.append(String.format("url:'%s'", url)); //$NON-NLS-1$
        sb.append(String.format(",pfdlgcid:'%s'", id)); //$NON-NLS-1$
        sb.append(String.format(",sourceComponentId:'%s'", sourceComponentId)); //$NON-NLS-1$
        appendSourceWidget(sourceWidget, sb);
        if ( options != null ) {
            appendOptions(options, sb);
        }
        sb.append("});"); //$NON-NLS-1$
        return sb.toString();
    }


    /**
     * @param options
     * @param sb
     */
    private static void appendOptions ( Map<String, Object> options, StringBuilder sb ) {
        boolean first = true;
        sb.append(",options:{"); //$NON-NLS-1$
        for ( Entry<String, Object> e : options.entrySet() ) {
            if ( !first ) {
                sb.append(',');
            }
            first = false;
            sb.append(e.getKey()).append(':').append(e.getValue());
        }
        sb.append('}');
    }


    /**
     * @param sourceWidget
     * @param sb
     */
    private static void appendSourceWidget ( String sourceWidget, StringBuilder sb ) {
        if ( sourceWidget != null ) {
            sb.append(String.format(",sourceWidget:PF('%s')", sourceWidget)); //$NON-NLS-1$
        }
    }


    /**
     * @param closable
     * @param closable
     * @return
     */
    private static Map<String, Object> makeDialogOptions ( boolean closable ) {
        Map<String, Object> options = new HashMap<>();
        options.put(OPTION_MODAL, true);
        options.put(OPTION_RESIZABLE, false);
        options.put(OPTION_DRAGGABLE, false);
        options.put(OPTION_CLOSABLE, closable);
        options.put(OPTION_MAX_WIDTH, 1600);
        return options;
    }

}
