package net.tiny.naming;

import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.CompoundName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

public class NameingContext implements Context {

    private static final String MALFORMED_URL =
            "Malformed PROVIDER_URL: use 'http(s)://<hostname>:<port>/<path>'";

    private String            path;
    private Hashtable<String, Object>   environment;
    private BindingService    client;

    /** Creates new ContextImpl */
    public NameingContext(String path, Hashtable<?, ?> env) throws NamingException {
        if(path == null) {
            throw new IllegalArgumentException("path is null!");
        }
        try {
            URL url = new URL((String)env.get(Context.PROVIDER_URL));
            QName portName = new QName("http://naming.eac.com/", "BindingService");
            Service service = Service.create(url, portName);
            this.client = service.getPort(BindingService.class);
        } catch(Exception ex) {
            NamingException nex = new NamingException(MALFORMED_URL);
            nex.setRootCause(ex);
            throw nex;
        }
        this.path        = path;
        this.environment = new Hashtable<String, Object>();
        Set<?> keys = env.keySet();
        for(Object key : keys) {
            environment.put(key.toString(), env.get(key));
        }
    }

    @Override
    public Object addToEnvironment(String name, Object value)  throws NamingException {
        assertNotNull("name", name);
        assertNotNull("value", value);
        return environment.put(name, value);
    }

    @Override
    public void bind(String name, Object obj) throws NamingException {
        assertNotNull("name", name);
        assertNotNull("object", obj);

        bind(new CompositeName(name), obj);
    }

    @Override
    public void bind(Name name, Object obj) throws NamingException {
        assertNotNull("name", name);
        assertNotNull("object", obj);

        BindingObject binding = new BindingObject(this.path, name.toString(), BindingObject.ActionType.BIND, obj);
        try {
            binding = client.query(binding);
        } catch(Exception ex) {
            NamingException nex = new NamingException(ex.getMessage());
            nex.setRootCause(ex);
            throw nex;
        }
    }

    @Override
    public void close() throws NamingException {
        //Do nothing
    }

    @Override
    public String composeName(String name, String prefix)  throws NamingException {
        assertNotNull("name", name);
        assertNotNull("prefix", prefix);

        Name result = composeName(new CompositeName(name),
                                  new CompositeName(prefix));
        return result.toString();
    }

    @Override
    public Name composeName(Name name, Name prefix) throws NamingException {
        assertNotNull("name", name);
        assertNotNull("prefix", prefix);

        Name result = (Name)(prefix.clone());
        result.addAll(name);
        return result;
    }

    @Override
    public Context createSubcontext(String name) throws NamingException
    {
        assertNotNull("name", name);
        BindingObject binding = new BindingObject(this.path, name, BindingObject.ActionType.CREATE_CONTEXT);

        try {
            binding = client.query(binding);
        } catch(Exception ex) {
            ex.printStackTrace();
            NamingException nex = new NamingException(ex.getMessage());
            nex.setRootCause(ex);
            throw nex;
        }
        String newPath = (String)binding.getValue();
        if(newPath == null) {
            throw new NamingException(name + " already exists");
        }
        return new NameingContext(newPath, this.environment);
    }

    @Override
    public Context createSubcontext(Name name) throws NamingException
    {
        assertNotNull("name", name);

        if(name == null) {
            throw new IllegalArgumentException("name is null!");
        }
        return createSubcontext(name.toString());
    }

    @Override
    public void destroySubcontext(String name) throws NamingException
    {
        assertNotNull("name", name);
        BindingObject binding = new BindingObject(this.path, name.toString(), BindingObject.ActionType.DESTROY_CONTEXT);
        try {
            binding = client.query(binding);
        } catch(Exception ex) {
            NamingException nex = new NamingException(ex.getMessage());
            nex.setRootCause(ex);
            throw nex;
        }
    }

    @Override
    public void destroySubcontext(Name name) throws NamingException
    {
        assertNotNull("name", name);
        destroySubcontext(name.toString());
    }

    @Override
    public Hashtable<?, ?> getEnvironment() throws NamingException
    {
        return (Hashtable<?, ?>)environment.clone();
    }

    @Override
    public String getNameInNamespace() throws NamingException
    {
        throw new OperationNotSupportedException("getNameInNamespace");
    }

    @Override
    public NameParser getNameParser(String name) throws NamingException
    {
        return new NameParserImpl();
    }

    @Override
    public NameParser getNameParser(Name name) throws NamingException
    {
        return new NameParserImpl();
    }

    @Override
    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        assertNotNull("name", name);
        return list(new CompositeName(name));
    }

    @SuppressWarnings("unchecked")
    @Override
    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        assertNotNull("name", name);
        BindingObject binding =
                new BindingObject(path, name.toString(), BindingObject.ActionType.LIST);
        try {
            binding = client.query(binding);
        } catch(Exception ex) {
            NamingException nex = new NamingException(ex.getMessage());
            nex.setRootCause(ex);
            throw nex;
        }
        Map<String, Object> map = (Map<String, Object>)binding.getValue();
        return new NamingEnumerationImpl<NameClassPair>(map);
    }

    @Override
    public NamingEnumeration<Binding> listBindings(String name)  throws NamingException {
        assertNotNull("name", name);
        return listBindings(new CompositeName(name));
    }

    @SuppressWarnings("unchecked")
    @Override
    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        assertNotNull("name", name);
        BindingObject binding =
                new BindingObject(path, name.toString(), BindingObject.ActionType.LIST);
        try {
            binding = client.query(binding);
        } catch(Exception ex) {
            NamingException nex = new NamingException(ex.getMessage());
            nex.setRootCause(ex);
            throw nex;
        }
        Map<String, Object> map = (Map<String, Object>)binding.getValue();
        return new NamingEnumerationImpl<Binding>(map);
    }

    @Override
    public Object lookup(String name) throws NamingException {
        assertNotNull("name", name);
        return lookup(new CompositeName(name));
    }

    @Override
    public Object lookup(Name name) throws NamingException {
        assertNotNull("name", name);
        BindingObject binding =
                new BindingObject(path, name.toString(), BindingObject.ActionType.LOOKUP);
        try {
            binding = client.query(binding);
        } catch(Exception ex) {
            NamingException nex = new NamingException(ex.getMessage());
            nex.setRootCause(ex);
            throw nex;
        }
        Object obj = binding.getValue();
        if(obj == null) {
            throw new NameNotFoundException(name.toString());
        }
        if(obj instanceof CreateContext) {
            return new NameingContext(((CreateContext)obj).getPath(), this.environment);
        }
        return obj;
    }

    @Override
    public Object lookupLink(String name)  throws NamingException {
        assertNotNull("name", name);
        return lookupLink(new CompositeName(name));
    }

    @Override
    public Object lookupLink(Name name) throws NamingException {
        assertNotNull("name", name);
        return lookup(name);
    }

    @Override
    public void rebind(String name, Object obj) throws NamingException {
        assertNotNull("name", name);
        assertNotNull("object", obj);
        rebind(new CompositeName(name), obj);
    }

    @Override
    public void rebind(Name name, Object obj)  throws NamingException {
        assertNotNull("name", name);
        assertNotNull("object", obj);
        BindingObject binding =
                new BindingObject(path, name.toString(), BindingObject.ActionType.REBIND, obj);
        try {
            binding = client.query(binding);
        } catch(Exception ex) {
            NamingException nex = new NamingException(ex.getMessage());
            nex.setRootCause(ex);
            throw nex;
        }
    }

    @Override
    public Object removeFromEnvironment(String key)  throws NamingException {
        assertNotNull("key", key);
        return environment.remove(key);
    }

    @Override
    public void rename(String oldName, String newName) throws NamingException {
        assertNotNull("oldName", oldName);
        assertNotNull("newName", newName);
        BindingObject binding =
                new BindingObject(path, oldName, BindingObject.ActionType.RENAME);
        binding.setValue(newName);
        try {
            binding = client.query(binding);
        } catch(Exception ex) {
            NamingException nex = new NamingException(ex.getMessage());
            nex.setRootCause(ex);
            throw nex;
        }
    }

    @Override
    public void rename(Name oldName, Name newName) throws NamingException {
        assertNotNull("oldName", oldName);
        assertNotNull("newName", newName);
        rename(oldName.toString(), newName.toString());
    }

    @Override
    public void unbind(String name) throws NamingException {
        unbind(new CompositeName(name));
    }

    @Override
    public void unbind(Name name) throws NamingException {
        BindingObject binding =
                new BindingObject(path, name.toString(), BindingObject.ActionType.UNBIND);
        try {
            binding = client.query(binding);
        } catch(Exception ex) {
            NamingException nex = new NamingException(ex.getMessage());
            nex.setRootCause(ex);
            throw nex;
        }
    }


    private void assertNotNull(String name, Object obj) throws NamingException {
        if(obj == null) {
            throw new NamingException(name + " must not be null!");
        }
    }

    public static class NameParserImpl implements NameParser {

        private static Properties syntax = new Properties();

        static {
            syntax.put("jndi.syntax.direction", "flat");
            syntax.put("jndi.syntax.ignorecase", "false");
        }

        public Name parse(String name) throws NamingException {
            return new CompoundName(name, syntax);
        }
    }

    class NamingEnumerationImpl<T> implements NamingEnumeration<T> {

        private Map<String, Object>  bindings;
        private Iterator<String> iter;

        NamingEnumerationImpl (Map<String, Object>  bindings) {
            this.bindings = bindings;
            this.iter     = this.bindings.keySet().iterator();
        }

        public boolean hasMoreElements() {
            return iter.hasNext();
        }

        public boolean hasMore() throws NamingException {
            return iter.hasNext();
        }

        @SuppressWarnings("unchecked")
        public T nextElement() {
            String name = iter.next();
            return (T)new Binding(name, bindings.get(name));
        }

        public T next() throws NamingException {
            return nextElement();
        }

        public void close() {
        }

    }

}
