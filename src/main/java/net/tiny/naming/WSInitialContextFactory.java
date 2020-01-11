package net.tiny.naming;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

public class WSInitialContextFactory implements InitialContextFactory {

    @Override
    public Context getInitialContext(Hashtable<?, ?> env) throws NamingException
    {
        if(env == null) {
            throw new NamingException("env is null");
        }
        if(env.get(Context.PROVIDER_URL) == null) {
            throw new NamingException("missing env-property " + Context.PROVIDER_URL);
        }
        //return new NameingContext("java:comp/env", env);
        return new NameingContext("/", env);
    }

}
