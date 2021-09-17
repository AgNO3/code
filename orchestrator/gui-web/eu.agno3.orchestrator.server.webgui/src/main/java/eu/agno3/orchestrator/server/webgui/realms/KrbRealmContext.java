/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.04.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.realms;


import java.io.Serializable;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.realms.KeyData;
import eu.agno3.orchestrator.realms.RealmType;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.runtime.jsf.view.stacking.DialogContext;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "krbRealmContext" )
public class KrbRealmContext implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 7699899931140733374L;

    private String realm;
    private RealmType type;
    private String keytab;

    private Set<KeyData> keys = new TreeSet<>();

    @Inject
    private InstanceRealmManager irm;


    /**
     * @return the realm
     */
    public String getRealm () {
        return this.realm;
    }


    /**
     * @param realm
     *            the realm to set
     */
    public void setRealm ( String realm ) {
        this.realm = realm;
    }


    /**
     * @return the type
     */
    public RealmType getType () {
        return this.type;
    }


    /**
     * @param type
     *            the type to set
     */
    public void setType ( RealmType type ) {
        this.type = type;
    }


    public String getTypeString () {
        if ( this.type == null ) {
            return null;
        }

        return this.type.name();
    }


    public void setTypeString ( String type ) {
        setType(RealmType.valueOf(type));
    }


    /**
     * @return the keytab
     */
    public String getKeytab () {
        return this.keytab;
    }


    /**
     * @param keytab
     *            the keytab to set
     */
    public void setKeytab ( String keytab ) {
        this.keytab = keytab;
    }


    /**
     * @return the keytabKeys
     */
    public Set<KeyData> getKeys () {
        return this.keys;
    }


    /**
     * @param keys
     *            the keys to set
     */
    public void setKeys ( Set<KeyData> keys ) {
        this.keys = keys;
    }


    /**
     * 
     * @return dialog close on success
     */
    public String deleteKeytab () {
        try {
            this.irm.deleteKeytab(this.realm, this.type, this.keytab);
            return DialogContext.closeDialog(true);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    /**
     * 
     * @return dialog close on success
     */
    public String createKeytab () {

        try {
            this.irm.createKeytab(this.realm, this.type, this.keytab, new LinkedList<>(getKeys()));
            this.keys.clear();
            return DialogContext.closeDialog(true);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }

        return null;
    }


    /**
     * 
     * @return dialog close on success
     */
    public String addKeys () {
        try {
            this.irm.addKeys(this.realm, this.type, this.keytab, new LinkedList<>(getKeys()));
            this.keys.clear();
            return DialogContext.closeDialog(true);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);;
        }

        return null;
    }

}
