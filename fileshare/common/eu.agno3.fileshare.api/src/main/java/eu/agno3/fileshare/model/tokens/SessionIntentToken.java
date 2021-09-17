/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.02.2015 by mbechler
 */
package eu.agno3.fileshare.model.tokens;


import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;


/**
 * @author mbechler
 *
 */
public class SessionIntentToken implements AccessToken {

    /**
     * 
     */
    private static final long serialVersionUID = 6887426698660142189L;
    private Serializable withIntentSessionId;


    /**
     * @param withIntentSessionId
     * 
     */
    public SessionIntentToken ( Serializable withIntentSessionId ) {
        this.withIntentSessionId = withIntentSessionId;
    }


    /**
     * 
     */
    public SessionIntentToken () {}


    /**
     * @return the withIntentSessionId
     */
    @Override
    public Serializable getWithIntentSessionId () {
        return this.withIntentSessionId;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.model.tokens.AccessToken#isWithIntent()
     */
    @Override
    public boolean isWithIntent () {
        return this.withIntentSessionId != null;
    }


    @Override
    public void writeExternal ( ObjectOutput oos ) throws IOException {
        if ( this.withIntentSessionId instanceof byte[] ) {
            oos.writeByte(0);
            oos.writeByte( ( (byte[]) this.withIntentSessionId ).length);
            oos.write((byte[]) this.withIntentSessionId);
        }
        else if ( this.withIntentSessionId instanceof String ) {
            oos.writeByte(1);
            oos.writeUTF((String) this.withIntentSessionId);
        }
        else {
            oos.writeByte(2);
            oos.writeObject(this.withIntentSessionId);
        }
    }


    @Override
    public void readExternal ( ObjectInput ois ) throws ClassNotFoundException, IOException {
        byte type = ois.readByte();
        if ( type == 0 ) {
            byte len = ois.readByte();
            if ( len > 0 ) {
                byte sessId[] = new byte[len];
                ois.readFully(sessId);
                this.withIntentSessionId = sessId;
            }
        }
        else if ( type == 1 ) {
            this.withIntentSessionId = ois.readUTF();
        }
        else if ( type == 2 ) {
            this.withIntentSessionId = (Serializable) ois.readObject();
        }
    }
}
