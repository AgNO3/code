/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.08.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.components;


import eu.agno3.orchestrator.server.webgui.config.ConfigContext;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public class OuterWrapper <T> {

    private OuterWrapper<?> outer;
    private String type;
    private AbstractObjectEditor<T> editor;


    /**
     * @param editor
     * @param outer
     * @param type
     */
    public OuterWrapper ( AbstractObjectEditor<T> editor, OuterWrapper<?> outer, String type ) {
        super();
        this.editor = editor;
        this.outer = outer;
        this.type = type;
    }


    /**
     * 
     * @param objectType
     * @return the first outer object that matches the given type
     */
    public OuterWrapper<?> get ( String objectType ) {
        if ( this.type.equals(objectType) ) {
            return this;
        }

        if ( this.outer == null ) {
            return null;
        }

        return this.outer.get(objectType);
    }


    /**
     * 
     * @param objectType
     * @param path
     * @return the object editor for the given path, starting at the closest object of objectType
     */
    public AbstractObjectEditor<?> resolve ( String objectType, String path ) {
        OuterWrapper<?> outerWrapper = this.get(objectType);
        if ( outerWrapper == null ) {
            return null;
        }

        if ( path == null || path.isEmpty() ) {
            return outerWrapper.getEditor();
        }
        return outerWrapper.getEditor().findChildByPath(path);
    }


    /**
     * @return the local object editor
     */
    public AbstractObjectEditor<T> getEditor () {
        return this.editor;
    }


    /**
     * 
     * @return whether the current context is simplified
     */
    public boolean getSimplified () {
        return getEditor().isSimplified();
    }


    /**
     * 
     * @return the config context
     */
    public ConfigContext<?, ?> getContext () {
        return getEditor().internalGetContext();
    }


    /**
     * 
     * @param param
     * @return the parameter value
     */
    public Object getParameter ( String param ) {
        return this.editor.getParameter(param);
    }


    /**
     * 
     * @param param
     * @param def
     * @return the parameter value, or default if null/unset
     */
    public Object getParameterOr ( String param, Object def ) {
        Object val = this.editor.getParameter(param);
        if ( val == null ) {
            return def;
        }
        return val;
    }


    /**
     * @return the local object type
     */
    public String getType () {
        return this.type;
    }
}
