package org.kisst.cordys.sbf;


public class SbfMain {

	public static void main(String[] args) {
		Sbf.init("user.properties");
		int idx=0;
		String command=args[idx++];
		String type=args[idx++];
		if ("create".equals(command)) {
			if ("ms".equals(type) || "methodset".equals(type)) {
				String conntype=args[idx++];
				String name=args[idx++];
				String namespace=args[idx++];
				if ("http".equals(conntype))
					conntype="org.kisst.cordys.http.HttpConnector";
				if ("relay".equals(conntype))
					conntype="org.kisst.cordys.relay.RelayConnector";
				Sbf.createMethodSet(conntype, name, namespace);
			}
			else if ("method".equals(type)) {
				String methodset=args[idx++];
				String name=args[idx++];
				String impl=args[idx++];
				String wsdl=args[idx++];
				Sbf.createMethod(methodset, name, impl, wsdl);
			}
		}
		if ("delete".equals(command)) {
			if ("ms".equals(type) || "methodset".equals(type)) {
				String conntype=args[idx++];
				String name=args[idx++];
				String namespace=args[idx++];
				if ("http".equals(conntype))
					conntype="org.kisst.cordys.http.HttpConnector";
				if ("relay".equals(conntype))
					conntype="org.kisst.cordys.relay.RelayConnector";
				Sbf.deleteMethodSet(conntype, name, namespace);
			}			
		}
	}

}
