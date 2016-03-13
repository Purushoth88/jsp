package ch.agilesolutions.jsp.watchers;

import java.io.BufferedReader;

import ch.agilesolutions.jsp.listeners.LogFileListener;
import ch.agilesolutions.jsp.model.LoggingItem;
import ch.agilesolutions.jsp.utils.RemoteExecutor;

public class ServerLogWatcher extends Thread {

	private boolean m_active = false;
	private BufferedReader m_reader = null;
	private LogFileListener listener;
	
	public static LoggingItem logFile = new LoggingItem();

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

				StringBuilder in = RemoteExecutor.refreshServerLog(null);

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
