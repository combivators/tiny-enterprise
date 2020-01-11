package net.tiny.naming;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URL;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.junit.jupiter.api.Test;

import net.tiny.service.ClassHelper;
import net.tiny.ws.BaseEndpointService;


public class WebNamingServiceTest {

    @Test
    public void testEndpointWebService() throws Exception {
        CalculatorServer cs = new CalculatorServer();
        cs.path("/cal");
        assertTrue(WebNamingService.isEndpoint(cs));
        assertTrue(WebNamingService.isEndpoint(new WebNamingService()));
        assertFalse(WebNamingService.isEndpoint(new Object()));

        WebService ws = cs.getClass().getAnnotation(WebService.class);
        assertEquals("net.tiny.naming.WebNamingServiceTest$CalculatorService", ws.endpointInterface());
        assertEquals("CalculatorService", ws.serviceName());
        assertEquals("CalculatorServicePort", ws.portName());
        assertEquals("", ws.wsdlLocation());
        assertEquals("", ws.targetNamespace());


        List<Class<?>> ifs = ClassHelper.getInterfaces(cs.getClass());
        assertEquals(2, ifs.size());
        for (Class<?> c : ifs) {
            if (c.isAnnotationPresent(WebService.class)) {
                ws = c.getAnnotation(WebService.class);
            }
        }
        assertEquals("http://naming.tiny.net/", ws.targetNamespace());
        assertEquals("Calculator", ws.name());
        assertEquals("", ws.wsdlLocation());

        assertEquals(CalculatorServer.class, cs.getClass());
        NamingService.Attributes attr = WebNamingService.getEndpointAttributes(cs, 8080, true);
        assertEquals("http://naming.tiny.net/", attr.namespace);
        assertEquals("CalculatorService", attr.serviceName);
        assertEquals("CalculatorServicePort", attr.portName);
        assertEquals("net.tiny.naming.WebNamingServiceTest$CalculatorService", attr.interfaceName);
        assertEquals(new URL("http://localhost:8080/cal"), attr.endpointAddress);
    }

    @WebService(name = "Calculator",
            targetNamespace = "http://naming.tiny.net/")
    @SOAPBinding(style = SOAPBinding.Style.RPC)
    public static interface CalculatorService {
        @WebMethod
        int sum(int a, int b);

        @WebMethod
        int diff(int a, int b);

        @WebMethod
        int multiply(int a, int b);

        @WebMethod
        int divide(int a, int b);
    }

    @WebService(serviceName = "CalculatorService",
            portName = "CalculatorServicePort",
            endpointInterface = "net.tiny.naming.WebNamingServiceTest$CalculatorService")
    public static class CalculatorServer extends BaseEndpointService implements CalculatorService {
        @Override
        public int sum(int a, int b) {
            return a+b;
        }

        @Override
        public int diff(int a, int b) {
            return a-b;
        }

        @Override
        public int multiply(int a, int b) {
            return a*b;
        }

        @Override
        public int divide(int a, int b) {
            return a/b;
        }
    }

}
