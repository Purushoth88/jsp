package jsp;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import ch.agilesolutions.jsp.utils.RemoteExecutor;
import ch.agilesolutions.jsp.watchers.LogFileWatcher;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "jsp"; //$NON-NLS-1$
	
	public static final String DOCKER_HOST = "www.agile-solutions.ch";
	
	public static final String LOCAL_PATH = "/u01/data/jboss/";
	
	public static final String REMOTE_PATH = "/u01/data/jboss/";

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
		
		//RemoteExecutor.setUser(System.getProperty("user.name"));

		
		Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");
		
		if (prefs.get("user", null) == null) {
			RemoteExecutor.setUser(System.getProperty("user.name"));
			LogFileWatcher.setUser(System.getProperty("user.name"));
			prefs.put("user", System.getProperty("user.name"));
			try {
				prefs.flush();
			} catch (BackingStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			RemoteExecutor.setUser(prefs.get("user", null));
			LogFileWatcher.setUser(prefs.get("user", null));
		}

		
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

		prefs.put("environment", "srp07370lx");

		RemoteExecutor.startJBoss();

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		
		RemoteExecutor.stopBoss();
		
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
