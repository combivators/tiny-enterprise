package net.tiny.naming;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;


@WebService(name = "Binding", targetNamespace = "http://naming.eac.com/")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface BindingService extends Remote {

	@WebMethod
	@WebResult(partName = "return")
	BindingObject query(@WebParam(partName = "binding")BindingObject binding) throws RemoteException;
}