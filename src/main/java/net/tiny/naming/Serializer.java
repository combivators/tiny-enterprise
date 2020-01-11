package net.tiny.naming;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import javax.xml.bind.DatatypeConverter;

public final class Serializer {

    private Serializer() {}

    public static final String encode(Object obj) {
        if(obj == null) return null;
        String ret = null;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(out);
            oout.writeObject(obj);
            oout.flush();
            oout.close();
            ret = DatatypeConverter.printBase64Binary(out.toByteArray());
        } catch(Exception ex) {
            //Serializer is used on the client and on server side.
            //There is no log on the client side, so print the exception
            //to the console.
            throw new IllegalStateException(ex.getMessage(), ex);
        }
        return ret;
    }

    public static final Object decode(String s) {
        if(s == null || s.isEmpty()) return null;;
        Object obj = null;
        try {
            byte[] b = DatatypeConverter.parseBase64Binary(s);
            ByteArrayInputStream in = new ByteArrayInputStream(b);
            ObjectInputStream oin = new ObjectInputStream(in);
            obj = oin.readObject();
            oin.close();
        } catch(Exception ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
        return obj;
    }


    public static final InputStream encodeStream(Object obj) {
        if(obj == null) return null;;
        PipedInputStream pipeIn = new PipedInputStream();
        try {
            PipedOutputStream pipeOut = new PipedOutputStream(pipeIn);
            ObjectOutputStream oos = new ObjectOutputStream(pipeOut);
            oos.writeObject(obj);
            oos.close();
        } catch(Exception ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
        return pipeIn;
    }

    public static final Object decodeStream(InputStream in) {
        if(in == null) return null;;
        Object obj = null;
        try {
            ObjectInputStream oin = new ObjectInputStream(in);
            obj = oin.readObject();
            oin.close();
        } catch(Exception ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
        return obj;
    }

}
