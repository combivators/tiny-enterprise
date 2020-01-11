package net.tiny.naming;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import javax.activation.DataSource;
import javax.xml.ws.WebServiceException;

public class SerializableSource<E> implements DataSource, Externalizable {

    public SerializableSource() {}

    public static byte[] encode(Object object) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            baos.close();
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new WebServiceException(ex.getMessage(), ex);
        }
    }

    public static Object decode(byte[] data) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bais);
            Object object = ois.readObject();
            bais.close();
            return object;
        } catch (Exception ex) {
            throw new WebServiceException(ex.getMessage(), ex);
        }
    }

    private byte[] resource = new byte[0];
    private ByteArrayOutputStream cache = null;

    public SerializableSource(Object object) {
        if (object instanceof DataSource) {
            try {
                InputStream is = ((DataSource) object).getInputStream();
                ObjectInput ois = new ObjectInputStream(is);
                readExternal(ois);
                is.close();
            } catch (Exception ex) {
                throw new WebServiceException(ex.getMessage(), ex);
            }
        } else {
            this.resource = encode(object);

        }
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(resource);
    }

    @Override
    public OutputStream getOutputStream() {
        if (cache == null) {
            cache = new ByteArrayOutputStream();
        }
        return cache;
    }

    @Override
    public String getContentType() {
        return "application/octet-stream";
    }

    @Override
    public String getName() {
        return SerializableSource.class.getSimpleName();
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.resource = encode(in.readObject());
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        if (isExist()) {
            out.writeObject(getSource());
        }
    }

    public boolean isExist() {
        if (resource.length == 0 && cache == null) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public E getObject() {
        return (E) getSource();
    }

    private Object getSource() {
        if (resource.length > 0) {
            return decode(resource);
        } else if (cache != null) {
            resource = cache.toByteArray();
            return decode(resource);
        } else {
            return null;
        }
    }

}
