package ch.agilesolutions.jsp.watchers;

import java.io.BufferedReader;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import ch.agilesolutions.jsp.listeners.LogFileListener;
import ch.agilesolutions.jsp.utils.RemoteExecutor;

import jsp.Activator;

public class LogFileWatcher extends Thread {

	private boolean m_active = false;
	private BufferedReader m_reader = null;
	private LogFileListener listener;

	private static String user;
	
	private String logFile = "";


	
	public LogFileWatcher(String container) {
		
		Activator.getDefault().getLog().log(
		                new Status(IStatus.INFO, "JSP", String.format("Logfile watcher started for %s%s/%s/log/server.log",Activator.LOCAL_PATH,  user, container)));

		
		logFile = String.format("%s%s/%s/log/server.log",Activator.LOCAL_PATH,  user, container);
		
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

				StringBuilder in = RemoteExecutor.refreshApplicationLog(logFile);

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

	public static String getUser() {
		return user;
	}

	public static void setUser(String user) {
		LogFileWatcher.user = user;
	}

	
}
