package net.tiny.ws;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;

import java.util.concurrent.ExecutorService;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;

public abstract class BaseEndpointService extends  AbstractWebService {

    // javax.xml.ws.Endpoint
    @Override
    public boolean isEndpoint() {
    	boolean ret = (this instanceof HttpHandler);
        if (!ret) {
        	ret = getClass().isAnnotationPresent(WebService.class);
        }
        return ret;
    }

    @Override
    public <T> T getBinding(Class<T> classType) {
    	System.out.println(classType.getName()); //TODO
        if(classType.isInstance(this)) {
            // Has HttpHandler interface
            return classType.cast(this);
        } else if (Endpoint.class.equals(classType) && isEndpoint()) {
            Endpoint endpoint = Endpoint.create(this);
            return classType.cast(endpoint);
        } else {
            throw new IllegalArgumentException(String.format("Can not cast '%s' interface from %s.",
                    classType.getSimpleName(), getClass().getSimpleName()));
        }
    }

    @Override
    public void publish(HttpContext serverContext) {
    	ExecutorService executor = (ExecutorService)serverContext.getAttributes().get(ExecutorService.class.getName());
        //Setup a endpoint
        Endpoint endpoint = getBinding(Endpoint.class);
        endpoint.setExecutor(executor);
        endpoint.publish(serverContext);
    }
}
