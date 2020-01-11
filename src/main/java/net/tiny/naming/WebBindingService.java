package net.tiny.naming;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.jws.WebService;

import net.tiny.service.ServiceContext;


@WebService(serviceName = "BindingService",
    portName = "BindingServicePort",
    endpointInterface = "net.tiny.naming.BindingService")
public class WebBindingService implements BindingContext, BindingService {

    private static final Logger LOGGER = Logger.getLogger(WebBindingService.class.getName());

    private static final String ROOT = "/";

    private Map<String, Map<String, String>>  bindings;
    private Map<String, JNDIContextHandler>   handlers;
    private ServiceContext serviceContext;
    private Listener listener;

    public WebBindingService(ServiceContext context) {
        serviceContext = context;
        bindings = Collections.synchronizedMap(new HashMap<String, Map<String, String>>());
        handlers = new HashMap<String, JNDIContextHandler>();
        //Create root path
        bindings.put(ROOT, Collections.synchronizedMap(new HashMap<String, String>()));
    }

    ///////////////////////////////////////////////////////////////////////
    // BindService interface
    @Override
    public BindingObject query(BindingObject binding) throws RemoteException {
        try {
            switch(binding.getActionType()) {
            case BIND:
                bind(binding.getPath(), binding.getName(), binding.getValue());
                break;
            case UNBIND:
                unbind(binding.getPath(), binding.getName());
                break;
            case REBIND:
                unbind(binding.getPath(), binding.getName());
                bind(binding.getPath(), binding.getName(), binding.getValue());
                break;
            case LOOKUP:
                Object obj = lookup(binding.getPath(), binding.getName());
                binding.setValue(obj);
                break;
            case LIST:
                Map<String, Object> map = list(binding.getPath(), binding.getName());
                binding.setValue(map);
                break;
            case RENAME:
                rename(binding.getPath(), binding.getName(), (String)binding.getValue());
                break;
            case CREATE_CONTEXT:
                String newPath = createSubcontext(binding.getPath(), binding.getName());
                binding.setValue(newPath);
                break;
            case DESTROY_CONTEXT:
                destroySubcontext(binding.getPath(), binding.getName());
                break;
            }
            return binding;
        } catch(RemoteException ex) {
            throw ex;
        } catch(Exception ex) {
            throw new RemoteException(ex.getMessage(), ex);

        }
    }

    ///////////////////////////////////////////////////////////////////////
    // BindContext interface
    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void setSubcontext(List<String> paths) {
        for(String path : paths) {
            if(bindings.containsKey(path)) {
                throw new IllegalArgumentException("'" + path + "' is existed.");
            }
            bindings.put(path, Collections.synchronizedMap(new HashMap<String, String>()));
            if(listener != null) {
                listener.createSubcontext(path);
            }
        }
    }

    public void setBounds(List<String> list) {
        for(String value : list) {
            String path = ROOT;
            String name = value;
            int pos = value.lastIndexOf("/");
            if(pos >= 0) {
                path = value.substring(0, pos+1);
                name = value.substring(pos + 1);
            }
            if(!bindings.containsKey(path)) {
                bindings.put(path, Collections.synchronizedMap(new HashMap<String, String>()));
            }
            Map<String, String> map = bindings.get(path);
            String ref = path + name;
            if(path.equals(ROOT)) {
                ref = name;
            }
            map.put(name, ref);
            if(listener != null) {
                listener.bind(ref);
            }
        }
    }

    private void assertPath(String path) throws IllegalArgumentException
    {
        if(path == null)
            throw new IllegalArgumentException("The path must not be null!");
        if(!path.endsWith("/"))
            throw new IllegalArgumentException("Illegal path '" + path + "'. The end without '/'.");
    }

    private void assertName(String name) throws IllegalArgumentException
    {
        if(name == null)
            throw new IllegalArgumentException("The name must not be null!");
        if(name.endsWith("/"))
            throw new IllegalArgumentException("Illegal name '" + name + "'. The end must not be '/'.");
    }

    @Override
    public String createSubcontext(String path, String name) {
        assertPath(path);
        assertName(name);
        String newPath =  path + name + "/";
        if(bindings.containsKey(newPath)) {
            //new path NULL ist returned, client will throw exception
            throw new IllegalArgumentException("'" + newPath + "' is existed.");
        }
        if(listener != null) {
            listener.createSubcontext(newPath);
        }
        bindings.put(newPath, Collections.synchronizedMap(new HashMap<String, String>()));
        return newPath;
    }

    @Override
    public void destroySubcontext(String path, String name) {
        assertPath(path);
        assertName(name);

        String jndiPath = path + name + "/";
        Map<String, String> map = bindings.get(jndiPath);
        if(map == null) {
             throw new IllegalArgumentException("Invalid subcontext path '" + jndiPath + "'");
        }
        String[] names = map.keySet().toArray(new String[map.size()]);
        for(String n : names) {
            map.remove(n);
            JNDIContextHandler handler = handlers.get(jndiPath);
            if(handler != null) {
                handler.unbind(n);
            }
        }
        bindings.remove(jndiPath);
        handlers.remove(jndiPath);
        if(listener != null) {
            listener.destroySubcontext(jndiPath);
        }
    }

    @Override
    public void bind(String path, String name, Object obj)  throws Exception {
        assertPath(path);
        assertName(name);
        if(obj == null)
            throw new IllegalArgumentException("The obj must not be null!");

        Map<String, String> map = bindings.get(path);
        if(map == null) {
            throw new IllegalArgumentException("Invalid path '" + path + "'");
        }
        String ref = path + name;
        if(path.equals(ROOT)) {
            ref = name;
        }
        map.put(name, ref);
        JNDIContextHandler handler = handlers.get(path);
        if(handler != null) {
            handler.bind(name, obj);
        }

        serviceContext.bind(ref, obj, true);
        if(listener != null) {
            listener.bind(path + name);
        }
    }

    @Override
    public void unbind(String path, String name) throws Exception {
        assertPath(path);
        assertName(name);

        Map<String, String> map = bindings.get(path);
        if(map == null) {
            throw new IllegalArgumentException("Invalid path '" + path + "'");
        }
        String ref = map.remove(name);
        JNDIContextHandler handler = handlers.get(path);
        if(handler != null) {
            handler.unbind(name);
        }

        serviceContext.unbind(ref);
        if(listener != null) {
            listener.unbind(path + name);
        }
    }


    @Override
    public Object lookup(String path, String name) {
        assertPath(path);
        assertName(name);

        String subpath = path + name + "/";
        Map<String, String> map = bindings.get(subpath);
        if(map != null) {
            //ok, name is a context ...
            return new CreateContext(subpath);
        }

        map = bindings.get(path);
        if(map == null) {
            throw new IllegalArgumentException("Invalid path '" + path + "'");
        }
        String ref = map.get(name);
        Object obj = serviceContext.lookup(ref);
        if(listener != null) {
            listener.lookup(path + name, obj);
        }
        return obj;
    }


    @Override
    public Map<String, Object> list(String path, String name) {
        assertPath(path);
        assertName(name);

        Map<String, String> map = bindings.get(path);
        if(map == null) {
            throw new IllegalArgumentException("Invalid path '" + path + "'");
        }
        Map<String, Object> ret = new HashMap<String, Object>();
        String[] names = map.keySet().toArray(new String[map.size()]);
        for(String key : names) {
            String ref = map.get(key);
            Object obj = serviceContext.lookup(ref);
            ret.put(ref, obj);
        }
        if(listener != null) {
            listener.list(path + name , ret.size());
        }
        return ret;
    }

    @Override
    public void rename(String path, String oldName, String newName) throws Exception {
        assertPath(path);
        assertName(oldName);
        assertName(newName);

        if(oldName.equals(newName)) {
            throw new IllegalArgumentException("The new name must not be same as the old name '" + oldName + "'");
        }

        Map<String, String> map = bindings.get(path);
        if(map == null) {
            throw new IllegalArgumentException("Invalid path '" + path + "'");
        }
        if(!map.containsKey(oldName)) {
            throw new IllegalArgumentException("Invalid name '" + oldName + "' on '" + path + "'");
        }

        if(map.containsKey(newName)) {
            throw new IllegalArgumentException("'" + newName + "' is existed name on '" + path + "'");
        }

        String ref = map.remove(oldName);
        Object obj = serviceContext.lookup(ref);
        JNDIContextHandler handler = handlers.get(path);
        if(handler != null) {
            handler.unbind(oldName);
        }
        serviceContext.unbind(ref);

        ref = path + newName;
        map.put(newName, ref);

        if(handler != null) {
            handler.bind(newName, obj);
        }
        serviceContext.bind(ref, obj, true);
        if(listener != null) {
            listener.rename(path + oldName, path + newName);
        }
    }

    @Override
    public void destroy() {
        String[] keys = bindings.keySet().toArray(new String[bindings.size()]);
        for(String path: keys) {
            Map<String, String> map = bindings.get(path);
            String[] names = map.keySet().toArray(new String[map.size()]);
            for(String name : names) {
                map.remove(name);
                JNDIContextHandler handler = handlers.get(path);
                if(handler != null) {
                    handler.unbind(name);
                }
            }
            bindings.remove(path);
            handlers.remove(path);
        }
        bindings = null;
        handlers = null;
    }


    static class Monitor implements Listener {
        @Override
        public void createSubcontext(String path) {
            LOGGER.info("[binding] createSubcontext('" + path + "')");
        }

        @Override
        public void destroySubcontext(String path) {
            LOGGER.info("[binding] createSubcontext('" + path + "')");
        }

        @Override
        public void bind(String path) {
            LOGGER.info("[binding] bind('" + path + "')");
        }

        @Override
        public void unbind(String path) {
            LOGGER.info("[binding] unbind('" + path + "')");
        }

        @Override
        public void rebind(String path) {
            LOGGER.info("[binding] rebind('" + path + "')");
        }

        @Override
        public void rename(String path, String oldPath) {
            LOGGER.info("[binding] rename('" + path + "', '"+ oldPath + "')");
        }

        @Override
        public void lookup(String path, Object obj) {
            LOGGER.info("[binding] lookup('" + path + "') : " + obj);
        }

        @Override
        public void list(String path, int size) {
            LOGGER.info("[binding] list('" + path + "') : " + size);
        }
    }
}
