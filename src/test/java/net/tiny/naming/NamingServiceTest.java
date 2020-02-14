package net.tiny.naming;

import static org.junit.jupiter.api.Assertions.*;


import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.logging.LogManager;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.tiny.context.WebServiceLocator;
import net.tiny.service.ServiceContext;
import net.tiny.service.ServiceLocator;
import net.tiny.ws.AccessLogger;
import net.tiny.ws.BaseEndpointService;
import net.tiny.ws.EmbeddedServer;
import net.tiny.ws.WebServiceHandler;
import net.tiny.ws.client.SimpleClient;


public class NamingServiceTest {

    static int port;
    static EmbeddedServer server;

    @BeforeAll
    public static void setUp() throws Exception {
        LogManager.getLogManager()
            .readConfiguration(Thread.currentThread().getContextClassLoader().getResourceAsStream("logging.properties"));

        AccessLogger logger = new AccessLogger();

        final Integer pid = 100;

        final ServiceContext.Monitor monitor = new ServiceContext.Monitor();
        final ServiceLocator serviceContext = new ServiceLocator();
        serviceContext.setListener(monitor);
        final Map<String, Object> collection = new HashMap<>();

        final CalculatorServer s = new CalculatorServer();
        final WebServiceHandler calculator =
                s.path("/cal")
                .filters(Arrays.asList(logger));
        collection.put("CalculatorService", s);
        NetworkAddressTranslaterService nat = new NetworkAddressTranslaterService();

        nat.setLan(InetAddress.getLocalHost().getHostAddress());
        nat.setWan("8.8.8.8");

        collection.put("nat", nat);

        final Callable<Properties> callable = new Callable<Properties>() {
            @Override
            public Properties call() {
                // Setup service locator properties;
                Properties services = new Properties();
                services.put("PID", pid);
                for (String key : collection.keySet()) {
                    services.put(key, collection.get(key));
                }
                return services;
            }
        };
        serviceContext.accept(callable);

        final WebNamingService endpoint = new WebNamingService();
        endpoint.setServiceContext(serviceContext);


        final WebServiceHandler naming = endpoint
                .filters(Arrays.asList(logger));

        server = new EmbeddedServer.Builder()
                .random()
                .handlers(Arrays.asList(naming, calculator))
                .build();
        port = server.port();
        endpoint.setServerPort(port);
        endpoint.sync();
        server.listen(callback -> {
            if(callback.success()) {
                System.out.println("Server listen on port: " + port);
            } else {
                callback.cause().printStackTrace();
            }
        });
    }

    @AfterAll
    public static void tearDown() throws Exception {
        server.close();
    }

    @Test
    public void testNamingServiceEndpoint() throws Exception {
        SimpleClient client = new SimpleClient.Builder()
                .keepAlive(true)
                .build();

        client.doGet(new URL("http://localhost:" + port +"/v1/ns?wsdl"), callback -> {
            if(callback.success()) {
                assertEquals(client.getStatus(), HttpURLConnection.HTTP_OK);
                assertEquals("text/xml;charset=utf-8", client.getHeader("Content-Type"));
                assertTrue(client.getContents().length > 1024);
                //System.out.println(new String(client.getContents()));
             } else {
                Throwable err = callback.cause();
                fail(err.getMessage());
            }
        });

        client.close();

        NamingService ns = WebServiceLocator.getNamingService("localhost", port);
        assertNotNull(ns);
        NamingService.Attributes attributes = ns.getAttributes("127.0.0.1", CalculatorService.class);
        assertNotNull(attributes);
        System.out.println(attributes.toString());

        QName portName = new QName(attributes.namespace, attributes.serviceName);
        Service service = Service.create(attributes.endpointAddress, portName);
        CalculatorService calculator = service.getPort(CalculatorService.class);
        assertNotNull(calculator);
        assertEquals(579, calculator.sum(123, 456));
        assertEquals(333, calculator.diff(456, 123));
    }


    @Test
    public void testWebServiceLocatorRemoteAccess() throws Exception {
        NamingService ns = WebServiceLocator.getNamingService("localhost", port);
        WebServiceLocator locator = new WebServiceLocator(ns);
        CalculatorService calculator = locator.lookup(CalculatorService.class);
        assertNotNull(calculator);
        assertEquals(579, calculator.sum(123, 456));
        assertEquals(333, calculator.diff(456, 123));
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
            endpointInterface = "net.tiny.naming.NamingServiceTest$CalculatorService")
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

    public static class CalculatorClient implements Supplier<CalculatorService> {

        final CalculatorService calculator;

        CalculatorClient(URL url) {
            QName qname = new QName("http://naming.tiny.net/", "CalculatorService");
            Service service = Service.create(url, qname);
            calculator = service.getPort(CalculatorService.class);
        }

        @Override
        public CalculatorService get() {
            return calculator;
        }
    }
}
