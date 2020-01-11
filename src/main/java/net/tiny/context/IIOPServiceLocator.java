package net.tiny.context;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
//import javax.rmi.PortableRemoteObject;

public class IIOPServiceLocator implements ServiceFeature {

    @Override
    public ServiceType getType() {
        return ServiceType.IIOP;
    }

    @Override
    public <T> T lookup(String endpoint, Class<T> classType) {
        ServicePoint point = ServicePoint.valueOf(endpoint);
        if (!ServiceType.IIOP.equals(point.getServiceType())) {
            throw new IllegalArgumentException("'" + endpoint + "'");
        }
        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.cosnaming.CNCtxFactory");
        env.put(Context.PROVIDER_URL, String.format("iiop://%s:%d", point.getHost(), point.getPort()));
        try {
            final Context context =  new InitialContext(env);
            Object objref = context.lookup(point.getName());
            //return (T)PortableRemoteObject.narrow(objref, classType);
            return classType.cast(objref);
        } catch (NamingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
