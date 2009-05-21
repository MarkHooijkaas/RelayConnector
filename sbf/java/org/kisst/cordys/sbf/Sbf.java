package org.kisst.cordys.sbf;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

public class Sbf {
	private static HttpClient client = new HttpClient();
	private static String organization;
	private static String url;
	private static String username;
	private static String password;
	private static Properties properties=new Properties();

	public static String cordysCall(String data) {
		return post(url, data, username, password);
	}

	public static String post(String url, String data, String username, String password) {
		PostMethod method=new PostMethod(url);
		method.setDoAuthentication(true);
		int statusCode;
		try {
			//HttpState state=new HttpState();
			//state.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
			method.setRequestEntity(new StringRequestEntity(data, "text/xml", "UTF-8"));
			statusCode = client.executeMethod(method);
		}
		catch (HttpException e) { throw new RuntimeException(e);}
		catch (IOException e) { throw new RuntimeException(e);}
		if (statusCode != HttpStatus.SC_OK) {
			throw new RuntimeException("Method failed: " + method.getStatusLine());
		}
		try {
			return method.getResponseBodyAsString();
		} catch (IOException e) { throw new RuntimeException(e);}
	}

	private static String loadTemplate(String name) {
		InputStream instream=Sbf.class.getClassLoader().getResourceAsStream("org/kisst/cordys/sbf/templates/"+name);
		BufferedReader inp = new BufferedReader(new InputStreamReader(instream));
		String result="";
		String line;
		try {
			while ((line=inp.readLine()) != null)
				result+=line+"\n";
		} catch (IOException e) { throw new RuntimeException(e); }
		return result;
	}
	public static void createMethodSet(String conntype, String name, String namespace) {
		String template=loadTemplate("CreateMethodSet.template");
		template = template.replaceAll("\\$\\{org}",organization);
		template = template.replaceAll("\\$\\{name}",name);
		template = template.replaceAll("\\$\\{namespace\\}",namespace);
		template = template.replaceAll("\\$\\{type\\}",conntype);
		System.out.println(cordysCall(template));
	}
	public static void deleteMethodSet(String conntype, String name, String namespace) {
		String template=loadTemplate("DeleteMethodSet.template");
		template = template.replaceAll("\\$\\{org}",organization);
		template = template.replaceAll("\\$\\{name}",name);
		template = template.replaceAll("\\$\\{namespace\\}",namespace);
		template = template.replaceAll("\\$\\{type\\}",conntype);
		System.out.println(template);
		System.out.println(cordysCall(template));
	}	
	public static void createMethod(String methodset, String name, String impl, String wsdl) {
		String template=loadTemplate("CreateMethod.template");
		template = template.replaceAll("\\$\\{org}",organization);
		template = template.replaceAll("\\$\\{methodset}",methodset);
		template = template.replaceAll("\\$\\{name\\}",name);
		template = template.replaceAll("\\$\\{impl\\}",xmlEscape(impl));
		template = template.replaceAll("\\$\\{wsdl\\}",xmlEscape(wsdl));
		System.out.println(template);
		System.out.println(cordysCall(template));
	}
	private static String xmlEscape(String str) {
		return str.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
	}

	public static void init(String filename)
	{
		properties.clear();
		FileInputStream inp = null;
		try {
			inp =new FileInputStream(filename);
			properties.load(inp);
		} 
		catch (java.io.IOException e) { throw new RuntimeException(e);  }
		finally {
			try {
				if (inp!=null) 
					inp.close();
			}
			catch (java.io.IOException e) { throw new RuntimeException(e);  }
		}
		url     =(String) properties.get("cordys.gateway.url");
		String host     =(String) properties.get("cordys.gateway.host");
		String realm    =(String) properties.get("cordys.gateway.realm");
		int port     =80;
		username=(String) properties.get("cordys.gateway.username");
		password=(String) properties.get("cordys.gateway.password");
		organization =(String) properties.get("cordys.organization");
		if (url.indexOf("Gateway.wcp")<=0) {
			if (url.endsWith("cordys"))
				url+="/";
			url+="com.eibus.web.soap.Gateway.wcp?organization="+organization;
		}
		client.getState().setCredentials(new AuthScope(host,port,realm),new UsernamePasswordCredentials(username, password));

	}
}
