package ch.agilesolutions.jsp.watchers;

import java.io.BufferedReader;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.Preferences;

import ch.agilesolutions.jsp.listeners.LogFileListener;
import ch.agilesolutions.jsp.model.LoggingItem;
import ch.agilesolutions.jsp.utils.RemoteExecutor;

public class LogFileWatcher extends Thread {

	private boolean m_active = false;
	private BufferedReader m_reader = null;
	private LogFileListener listener;
	
	public static LoggingItem logFile = new LoggingItem();

	static {
		
		Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

		String prefix = prefs.get("prefix", null);
		
		logFile.setUnixName("/user/log/runuser/" + prefix.toUpperCase() + "/infra/server.log");
		
		
	}
	
	

	public void addListener(LogFileListener lst) {
		this.listener = lst;
	}

	/**
	 * Runs the thread that watches for changes to the file.
	 */
	public void run() {
		m_active = true;

		while (m_active) {

			try {

				StringBuilder in = RemoteExecutor.refreshApplicationLog(null);

				this.listener.update(in);
				
				
				sleep(2 * 1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			m_reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
