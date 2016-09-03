package ch.agilesolutions.jsp.model;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.Preferences;

public class Environment {

	private static final Environment environment = new Environment();

	
	private String server;
	
	private String id;

	private String name;

	private String image;

	private String container;

	private String port;

	private String adminPort;

	private String debugPort;

	private String jbar;

	public Environment(String id, String name, String image, String port, String adminPort, String debugPort, String jbar) {
		this.id = id;
		this.name = name;
		this.image = image;
		this.port = port;
		this.adminPort = adminPort;
		this.debugPort = debugPort;
		this.jbar = jbar;
	}

	public Environment() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getJbar() {
		return jbar;
	}

	public void setJbar(String jbar) {
		this.jbar = jbar;
	}

	public String getContainer() {
		return container;
	}

	public void setContainer(String container) {
		this.container = container;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getAdminPort() {
		return adminPort;
	}

	public void setAdminPort(String adminPort) {
		this.adminPort = adminPort;
	}

	public String getDebugPort() {
		return debugPort;
	}

	public void setDebugPort(String debugPort) {
		this.debugPort = debugPort;
	}

	public static Environment getEnvironment() {
		
		
		Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

		environment.setServer((prefs.get("environment", null) == null) ? "NA" : (prefs.get("environment", null)));
		environment.setId((prefs.get("id", null) == null) ? "NA" : (prefs.get("id", null)));
		environment.setName((prefs.get("name", null) == null) ? "NA" : (prefs.get("name", null)));
		environment.setContainer((prefs.get("container", null) == null) ? "NA" : (prefs.get("container", null)));
		environment.setImage((prefs.get("image", null) == null) ? "NA" : (prefs.get("image", null)));
		environment.setPort((prefs.get("port", null) == null) ? "NA" : (prefs.get("port", null)));
		environment.setAdminPort((prefs.get("adminPort", null) == null) ? "NA" : (prefs.get("adminPort", null)));
		environment.setDebugPort((prefs.get("debugPort", null) == null) ? "NA" : (prefs.get("debugPort", null)));
		environment.setJbar((prefs.get("jbar", null) == null) ? "NA" : (prefs.get("jbar", null)));
		
		return environment;
	}

	private static void setTrustAllCerts() throws Exception {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}
		} };

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

				public boolean verify(String urlHostName, SSLSession session) {
					return true;
				}
			});
		} catch (

		Exception e) {
			// We can not recover from this exception.
			e.printStackTrace();
		}
	}

}
