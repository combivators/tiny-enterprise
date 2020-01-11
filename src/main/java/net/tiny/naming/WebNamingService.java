package net.tiny.naming;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebService;

import com.sun.net.httpserver.HttpHandler;

import net.tiny.service.ClassHelper;
import net.tiny.service.Container;
import net.tiny.service.ServiceContext;
import net.tiny.ws.AbstractWebService;
import net.tiny.ws.BaseEndpointService;


@WebService(serviceName = "NamingService",
        portName = "NamingServicePort",
        endpointInterface = "net.tiny.naming.NamingService")
public class WebNamingService extends BaseEndpointService implements NamingService {

    private Map<String, Attributes> services =
            Collections.synchronizedMap(new HashMap<String, Attributes>());

    private ServiceContext serviceContext;
    private int serverPort = 80;

    // Getter and Setter
    public ServiceContext getServiceContext() {
        return serviceContext;
    }

    public void setServiceContext(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int port) {
        serverPort = port;
    }
    /**
     * @see AbstractWebService#path()
     */
    @Override
    public String path() {
        if (null == path) {
            path = CONTEXT_PATH;
        }
        return path;
    }

    ///////////////////////////////////////////////////
    // NamingService implemented methods
    @Override
    public Attributes[] getAllAttributes(String client) {
        Attributes[] array = services.values().toArray(
                new Attributes[services.size()]);
        for (int i = 0; i < array.length; i++) {
            array[i] = translate(client, array[i]);
        }
        return array;
    }

    @Override
    public boolean isWebservice(String client, Class<?> classType) {
        return (getAttributes(client, classType) != null);
    }

    @Override
    public Attributes getAttributes(String client, Class<?> classType) {
        String key = classType.getName();
        if (services.containsKey(key)) {
            return translate(client, services.get(key));
        }
        key = classType.getSimpleName();
        Collection<Attributes> list = services.values();
        for (Attributes attr : list) {
            if (key.equals(attr.serviceName)) {
                return translate(client, attr);
            }
        }
        LOGGER.warning(String.format("Can not found a web service end point '%s'.", classType.getName()));
        return null;
    }

    @Override
    public String getNamespace(String client, Class<?> classType) {
        return getAttributes(client, classType).namespace;
    }

    @Override
    public String getEndpointAddress(String client, Class<?> classType) {
        return getAttributes(client, classType).endpointAddress.toString();
    }

    ///////////////////////////////////////////////////
    // WebserviceCache implemented methods
    /////////////////////////////////////////////////////
    //@Override
    public void sync() {
        synchronized (services) {
            services.clear();
            final String[] keys = serviceContext.lookup(Container.class).getAllKeyNames();
            final List<Object> endpoints = new ArrayList<>();
            for (String key : keys) {
                Object bean = serviceContext.lookup(key);
                if (isEndpoint(bean) && !endpoints.contains(bean)) {
                    endpoints.add(bean);
                    Attributes attr = getEndpointAttributes(bean, serverPort, true);
                    services.put(attr.interfaceName, attr);
                    LOGGER.info(String.format("[NAMING] Register a endpoint '%s' : %s", key, attr.toString()));
                }
                /*
                if (bean instanceof WebServiceContext) {
                    WebServiceContext endpoint = (WebServiceContext)bean;
                    String interfaceName = endpoint.getClassType().getName();
                    if (null == endpoint.getEndpointURL() || services.containsKey(interfaceName)) {
                        continue;
                    }

                    Attributes attr = new Attributes();
                    attr.endpointAddress = endpoint.getEndpointURL();
                    attr.namespace = endpoint.getNamespace();
                    attr.serviceName = endpoint.getServiceName();
                    attr.portName = endpoint.getPortName();
                    services.put(interfaceName, attr);
                }
                */
            }
        }
    }

    Attributes translate(String address, Attributes source) {
        NetworkAddressTranslater nat = serviceContext.lookup(NetworkAddressTranslater.class);
        URL url = nat.translate(address, source.endpointAddress);
        if (url.equals(source.endpointAddress)) {
            return source;
        } else {
            Attributes target = new Attributes();
            target.endpointAddress = url;
            target.namespace = source.namespace;
            target.serviceName = source.serviceName;
            target.portName = source.portName;
            return target;
        }
    }

    public static boolean isEndpoint(Object bean) {
        if (null == bean || bean instanceof HttpHandler) {
            return false;
        }
        return bean.getClass().isAnnotationPresent(WebService.class);
    }

    static Attributes getEndpointAttributes(Object endpoint, int port, boolean inner) {
        Class<?> endpointType = endpoint.getClass();
        WebService ws = endpointType.getAnnotation(WebService.class);
        final Attributes attr = new Attributes();
        attr.namespace = ws.targetNamespace();
        attr.serviceName = ws.serviceName();
        attr.portName = ws.portName();
        attr.interfaceName = ws.endpointInterface();

        if(attr.isValid())
            return attr;

        // Set WebService Annotation attributes of interface class
        ws = null;
        //String interfaceName = "";
        List<Class<?>> ifs = ClassHelper.getInterfaces(endpointType, inner, false, false);
        for (Class<?> c : ifs) {
            if (c.isAnnotationPresent(WebService.class)) {
                ws = c.getAnnotation(WebService.class);
                //interfaceName = c.getSimpleName();
                break;
            }
        }
        if (ws != null) {
            if (Attributes.isEmpty(attr.interfaceName))
                attr.interfaceName = ws.endpointInterface();
            if (Attributes.isEmpty(attr.namespace))
                attr.namespace = ws.targetNamespace();
            if (Attributes.isEmpty(attr.serviceName))
                attr.serviceName = ws.serviceName();
            if (Attributes.isEmpty(attr.portName))
                attr.portName = ws.portName();
        }
        try {
            String path = "/";
            if (endpoint instanceof AbstractWebService) {
                path = ((AbstractWebService)endpoint).path();
            }
            attr.endpointAddress = new URL(String.format("http://localhost:%d%s", port, path)); // address & port
        } catch (MalformedURLException e) {
            // TODO
            e.printStackTrace();
        }
        return attr;
    }


}
