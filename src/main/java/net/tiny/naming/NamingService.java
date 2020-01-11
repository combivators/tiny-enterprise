package net.tiny.naming;

import java.net.URL;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlAttribute;

@WebService(name = "NS", targetNamespace = "http://naming.tiny.net/")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface NamingService {


    String VER = "v1";
    String CONTEXT_PATH = String.format("/%s/ns", VER);
    String NAMESPACE_URI = "http://naming.tiny.net/";
    String LOCAL_PART    = NamingService.class.getSimpleName();

    class Attributes {

        @XmlAttribute
        public URL endpointAddress;

        @XmlAttribute
        public String namespace;

        @XmlAttribute
        public String serviceName;

        @XmlAttribute
        public String portName;

        public String interfaceName;

        @Override
        public String toString() {
            return endpointAddress + "," + namespace + "," + serviceName + "," + portName;
        }

        public boolean isValid() {
            return !isEmpty(namespace)
                    && !isEmpty(serviceName)
                    && !isEmpty(portName)
                    && !isEmpty(interfaceName)
                    && endpointAddress != null;
        }

        public static boolean isEmpty(String s) {
            return (s == null || s.isEmpty());
        }
    }

    @WebMethod
    @WebResult(partName = "return")
    Attributes[] getAllAttributes(@WebParam(partName = "client")String client);

    @WebMethod
    @WebResult(partName = "return")
    boolean isWebservice(
            @WebParam(partName = "client")String client,
            @WebParam( partName = "classType") Class<?> classType);

    @WebMethod
    @WebResult(partName = "return")
    Attributes getAttributes(
            @WebParam(partName = "client")String client,
            @WebParam(partName = "classType") Class<?> classType);

    @WebMethod
    @WebResult(partName = "return")
    String getEndpointAddress(
            @WebParam(partName = "client")String client,
            @WebParam(partName = "classType") Class<?> classType);

    @WebMethod
    @WebResult(partName = "return")
    String getNamespace(
            @WebParam(partName = "client")String client,
            @WebParam(partName = "classType") Class<?> classType);

}
