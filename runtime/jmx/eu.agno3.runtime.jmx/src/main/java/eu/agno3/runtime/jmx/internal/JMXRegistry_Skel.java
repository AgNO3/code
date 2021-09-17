/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2017 by mbechler
 */
package eu.agno3.runtime.jmx.internal;


import java.rmi.Remote;
import java.rmi.UnmarshalException;
import java.rmi.registry.Registry;
import java.rmi.server.Operation;
import java.rmi.server.RemoteCall;
import java.rmi.server.SkeletonMismatchException;

import sun.rmi.server.MarshalInputStream;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( {
    "deprecation", "nls"
} )
public class JMXRegistry_Skel implements java.rmi.server.Skeleton {

    private static final Operation[] OPERATIONS;
    private static final long interfaceHash = 4905912898345647071L;


    static {
        OPERATIONS = new Operation[] {
            new Operation("void bind(java.lang.String, java.rmi.Remote)"), new Operation("java.lang.String list()[]"),
            new Operation("java.rmi.Remote lookup(java.lang.String)"), new Operation("void rebind(java.lang.String, java.rmi.Remote)"),
            new Operation("void unbind(java.lang.String)")
        };

    }


    @Override
    @SuppressWarnings ( "resource" )
    public void dispatch ( Remote obj, RemoteCall theCall, int opnum, long hash ) throws Exception {
        if ( hash != interfaceHash ) {
            throw new SkeletonMismatchException("interface hash mismatch");
        }
        Registry reg = (Registry) obj;
        String name;
        Remote remobj = null;
        MarshalInputStream inputStream = (MarshalInputStream) theCall.getInputStream();
        try {
            switch ( opnum ) {
            case 0: // bind
                name = readString(inputStream);
                theCall.releaseInputStream();
                reg.bind(name, remobj);
                break;
            case 1: // list
                theCall.releaseInputStream();
                theCall.getResultStream(true).writeObject(reg.list());
                return;
            case 2: // lookup
                name = readString(inputStream);
                theCall.releaseInputStream();
                theCall.getResultStream(true).writeObject(reg.lookup(name));
                break;
            case 3: // rebind
                name = readString(inputStream);
                theCall.releaseInputStream();
                reg.rebind(name, remobj);
                break;
            case 4: // unbind
                name = readString(inputStream);
                theCall.releaseInputStream();
                reg.unbind(name);
                break;
            default:
                throw new UnmarshalException("invalid method number");
            }
        }
        finally {
            theCall.releaseInputStream();
        }
    }


    /**
     * @param name
     * @param inputStream
     * @return
     * @throws UnmarshalException
     */
    private static String readString ( MarshalInputStream inputStream ) throws UnmarshalException {
        try {
            return (String) inputStream.readObject();
        }
        catch ( Exception e ) {
            throw new UnmarshalException("error unmarshalling arguments", e);
        }
    }


    @Override
    public Operation[] getOperations () {
        return OPERATIONS.clone();
    }

}
