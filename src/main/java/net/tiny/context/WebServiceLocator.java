package net.tiny.context;

import java.net.MalformedURLException;
import java.net.URL;

import javax.activation.DataSource;
import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.http.HTTPBinding;

import net.tiny.naming.NamingService;
import net.tiny.naming.SerializableSource;

public class WebServiceLocator implements ServiceFeature {

    static final String WS_URL_FORMAT = "http://%s:%d" + NamingService.CONTEXT_PATH;
    static final QName NS_PORTNAME = new QName(NamingService.NAMESPACE_URI, NamingService.LOCAL_PART);

    private final NamingService namingService;

    public WebServiceLocator(NamingService ns) {
        this.namingService = ns;
    }

    @Override
    public ServiceType getType() {
        return ServiceType.WS;
    }

    @Override
    public <T>T lookup(String endpoint, Class<T> classType) {
        ServicePoint point = ServicePoint.valueOf(endpoint);
        if (!ServiceType.WS.equals(point.getServiceType())) {
            throw new IllegalArgumentException("'" + endpoint + "'");
        }
        try {
            NamingService ns = getNamingService(point.getHost(), point.getPort());
            //Attributes attr = ns.getAttributes(classType);
            //System.out.println(classType.getName() + " ### Attributes: " + attr.serviceName );
            WebServiceLocator locator = new WebServiceLocator(ns);
            return locator.lookup(classType);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public <T> T lookup(Class<T> interfaceClass) throws WebServiceException {
        NamingService.Attributes attr =
                namingService.getAttributes(ServicePoint.LOCALHOST_ADDRESS, interfaceClass);
        QName portName = new QName(attr.namespace, attr.serviceName);
        Service service = Service.create(attr.endpointAddress, portName);
        return service.getPort(interfaceClass);
    }

    public <T> T invoke(Class<?> interfaceClass, Class<T> typeClass, Service.Mode mode, T request) throws WebServiceException {
        NamingService.Attributes attr =
                namingService.getAttributes(ServicePoint.LOCALHOST_ADDRESS, typeClass);
        QName portName = new QName(attr.namespace, attr.serviceName);
        Service service = Service.create(portName);
        service.addPort(portName, HTTPBinding.HTTP_BINDING, attr.endpointAddress.toString());

        Dispatch<T> dispatch = service.createDispatch(portName, typeClass, mode);
        return dispatch.invoke(request);
    }

    public <T> T invoke(Class<?> interfaceClass, Object request, Class<T> typeClass) {
        DataSource requestSource = new SerializableSource<Object>(request);
        DataSource responseSource = invoke(interfaceClass, DataSource.class,
                Service.Mode.MESSAGE, requestSource);
        SerializableSource<T> ss = new SerializableSource<T>(responseSource);
        return ss.getObject();
    }

    public Object invoke(Class<?> interfaceClass, Object request) {
        return invoke(interfaceClass, request, Object.class);
    }


    public static NamingService getNamingService(String host, int port) throws MalformedURLException {
        URL endpointURL = new URL(String.format(WS_URL_FORMAT, host, port));
        Service service = Service.create(endpointURL, NS_PORTNAME);

//TODO
//		Map<String,	Object> requestContext = new	HashMap<String,	Object>();
//		requestContext.put(javax.xml.ws.BindingProvider.USERNAME_PROPERTY,	"username");
//		requestContext.put(javax.xml.ws.BindingProvider.PASSW0RD_PROPERTY,	"password");
//		requestContext.put("com.sun.xml.ws.connect, timeout", 15000L);
//		requestContext.put("com.sun.xml.ws.request, timeout", 15000L);
        return service.getPort(NamingService.class);
    }

}
