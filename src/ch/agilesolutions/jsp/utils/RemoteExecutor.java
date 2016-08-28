package ch.agilesolutions.jsp.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jgit.api.Git;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.prefs.Preferences;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import ch.agilesolutions.jsp.model.ConfigItem;
import ch.agilesolutions.jsp.model.DataItem;
import ch.agilesolutions.jsp.model.LoggingItem;
import ch.agilesolutions.jsp.watchers.LogFileWatcher;

public class RemoteExecutor {

	private static final String REMOTE_URL = "https://github.com/agilesolutions/jsp/jspstore.git";

	private static final String PUBLIC_KEY_FILE = "/ids_openssh";

	private static final int MAX_RETRIES = 2;

	// private static Session session = null;

	/**
	 * push the WAR or EAR deployment to the JBoss deployment directory where it is going to be detected by the JBoss deployment scanner.
	 * 
	 * @param fullFileName
	 *            full qualified filename.
	 * @param fileName
	 *            short name to be deployed.
	 * @param shell
	 *            shell id.
	 * @return full qualified
	 */
	public static void deploy(String fullFileName, String fileName, Shell shell) {

		try {

			Session session = openSession(shell);

			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;

			sftpChannel.put(fullFileName, "/user/data/jboss/" + System.getProperty("user.name") + "/deployments/" + fileName);

			sftpChannel.exit();

			session.disconnect();

		} catch (Exception e) {

			MessageDialog.openInformation(shell, "JSP Exception Occurred", e.getMessage());
		}

	}

	/**
	 * push standalone.xml or standalone.conf files to /JBx/configuration location.
	 * 
	 * @param file
	 * @param viewer
	 */
	public static void pushConfig(String file, TextViewer viewer) {

		String path = "none";

		try {

			Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

			String prefix = prefs.get("prefix", null);

			Session session = openSession(viewer.getControl().getShell());

			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;

			path = Platform.getLocation().toString() + "/.configuration";

			sftpChannel.put(path + "/" + file, "/user/app/runuser/" + prefix.toUpperCase() + "/configuration/" + file);

			sftpChannel.exit();

			session.disconnect();

		} catch (Exception e) {
			showError(viewer.getControl().getShell(), e.getMessage());
		}

	}

	/**
	 * push standalone.xml or standalone.conf files to /JBx/configuration location.
	 * 
	 * @param file
	 * @param viewer
	 */
	public static void executeCLI(String file, TextViewer viewer) {

		String path = "none";

		try {

			Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

			String prefix = prefs.get("prefix", null);

			Session session = openSession(viewer.getControl().getShell());

			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;

			path = Platform.getLocation().toString() + "/.configuration";

			sftpChannel.put(path + "/" + file, "/user/app/runuser/" + prefix.toUpperCase() + "/configuration/" + file);

			sftpChannel.exit();

			session.disconnect();

		} catch (Exception e) {
			showError(viewer.getControl().getShell(), e.getMessage());
		}

	}

	/**
	 * 
	 * Push data artefact to /user/data/JBx location.
	 * 
	 * @param item
	 *            holder object which holds all data file coordinates.
	 * @param shell
	 */
	public static void pushData(DataItem item, Shell shell) {

		try {

			Session session = openSession(shell);

			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;

			sftpChannel.put(item.getWindowsName(), item.getUnixName());

			sftpChannel.exit();

			session.disconnect();

		} catch (Exception e) {
			showError(shell, e.getMessage());
		}

	}

	/**
	 * push application specific configuration file to /user/app/JBx/configuration/{application artefact} location.
	 * 
	 * @param item
	 * @param shell
	 * @return
	 */
	public static String pushConfigItem(ConfigItem item, Shell shell) {

		try {

			Session session = openSession(shell);

			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;

			sftpChannel.put(item.getWindowsName(), item.getUnixName());

			sftpChannel.exit();

			session.disconnect();

		} catch (Exception e) {
			showError(shell, e.getMessage());
		}
		return item.getFilename();

	}

	/**
	 * push directory with docker artefacts to server.
	 * 
	 * @param item
	 * @param shell
	 * @return
	 */
	public static String pushDockerArtefacts(StringBuilder status, Text progress, Shell shell, String name) {

		StringBuilder result = new StringBuilder();

		status.append("1. Start build preperation....\n");

		progress.setText(status.toString());

		progress.update();
		progress.redraw();

		progress.getParent().update();
		progress.getParent().redraw();
		progress.getParent().layout(true);

		try {

			Session session = openSession(shell);

			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;

			SftpATTRS attrs;
			try {
				attrs = sftpChannel.stat("/user/data/jboss/" + System.getProperty("user.name"));
			} catch (Exception e) {
				sftpChannel.mkdir("/user/data/jboss/" + System.getProperty("user.name"));

				sftpChannel.mkdir("/user/data/jboss/" + System.getProperty("user.name") + "/docker");

				sftpChannel.mkdir("/user/data/jboss/" + System.getProperty("user.name") + "/docker/customization");

				sftpChannel.mkdir("/user/data/jboss/" + System.getProperty("user.name") + "/deployments");
			}

			removeCRLF();

			sftpChannel.put(Platform.getLocation().toString() + "/.configuration/docker/customization/commands.cli",
			                "/user/data/jboss/" + System.getProperty("user.name") + "/docker/customization/commands.cli");

			sftpChannel.put(Platform.getLocation().toString() + "/.configuration/docker/customization/execute.stripped",
			                "/user/data/jboss/" + System.getProperty("user.name") + "/docker/customization/execute.sh");

			sftpChannel.put(Platform.getLocation().toString() + "/.configuration/docker/Dockerfile",
			                "/user/data/jboss/" + System.getProperty("user.name") + "/docker/Dockerfile");

			sftpChannel.exit();

			session.disconnect();

			status.append("2. Build artefacts copied to Docker server....\n");

			progress.setText(status.toString());

			progress.update();
			progress.redraw();

			progress.getParent().update();
			progress.getParent().redraw();
			progress.getParent().layout(true);

		} catch (Exception e) {
			showError(shell, e.getMessage());
		}

		return result.toString();

	}

	/**
	 * push directory with docker artefacts to server.
	 * 
	 * @param item
	 * @param shell
	 * @return
	 */
	public static String createDockerImage(StringBuilder status, Text progress, Shell shell, String name) {

		InputStream in = null;

		status.append("3. Start building Docker image....\n");

		progress.setText(status.toString());

		progress.update();
		progress.redraw();

		progress.getParent().update();
		progress.getParent().redraw();
		progress.getParent().layout(true);

		try {

			Session session = openSession();

			Channel channel = session.openChannel("exec");

			((ChannelExec) channel).setPty(true);

			String user = System.getProperty("user.name");

			Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

			String port = prefs.get("port", null);

			prefs.put("image", user + "/" + name + ":" + port);
			prefs.put("container", user + name + port);
			prefs.flush();

			((ChannelExec) channel).setCommand("cd /user/data/jboss/" + user + "/docker;chmod -R 777 /user/data/jboss/" + user
			                + "/docker/customization/execute.sh;sudo /usr/bin/docker build --rm -t " + user + "/" + name + ":" + port
			                + " .");

			channel.setInputStream(null);

			((ChannelExec) channel).setErrStream(System.err);

			in = channel.getInputStream();

			channel.connect();

			byte[] tmp = new byte[1024];

			boolean firsttime = true;

			int retries = 0;

			try {
				while (true) {
					while (in.available() > 0) {
						int i = in.read(tmp, 0, 1024);
						if (i < 0)
							break;
						firsttime = false;

						String message = new String(tmp, 0, i);

						status.append(message);

						progress.setText(status.toString());

						progress.update();
						progress.redraw();
						progress.forceFocus();

						progress.getParent().update();
						progress.getParent().redraw();
						progress.getParent().layout(true);

					}
					if (retries > 20) {
						break;
					}
					try {
						Thread.sleep(1000);
						retries++;

					} catch (Exception ee) {
						System.out.println(ee.getMessage());
					}
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());

			}

			channel.disconnect();
			session.disconnect();

		} catch (Exception e) {
			System.out.println(e.getMessage());

		}

		return status.toString();

	}

	/**
	 * push directory with docker artefacts to server.
	 * 
	 * @param item
	 * @param shell
	 * @return
	 */
	public static String runDockerImage(StringBuilder status, Text progress, Shell shell, String name) {

		InputStream in = null;

		status.append("4. Running Docker image....\n");
		

		progress.setText(status.toString());

		progress.update();
		progress.redraw();

		progress.getParent().update();
		progress.getParent().redraw();
		progress.getParent().layout(true);

		try {

			Session session = openSession();

			Channel channel = session.openChannel("exec");

			((ChannelExec) channel).setPty(true);

			Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

			String container = prefs.get("container", null);
			String port = prefs.get("port", null);
			String adminPort = prefs.get("adminPort", null);
			String debugPort = prefs.get("debugPort", null);

			String user = System.getProperty("user.name");
			
			StringBuilder commands = new StringBuilder();
			
			commands.append("sudo /usr/bin/docker run -d --name " + container + " -p " + port + ":8080 -p " + adminPort
			                + ":9990  -p " + debugPort + ":8787 -v /user/data/jboss/" + user
			                + "/deployments:/opt/jboss/wildfly/standalone/deployments/:rw -v /user/log/jboss/" + user
			                + ":/opt/jboss/wildfly/standalone/log/:rw " + user + "/" + name + ":" + port);
			commands.append(";sudo /usr/bin/docker cp " + container
			                + ":/opt/jboss/wildfly/standalone/configuration/standalone.xml /user/data/jboss/" + user + "/configuration");
			commands.append(";sudo /usr/bin/docker cp " + container
			                + ":/opt/jboss/wildfly/bin/standalone.conf /user/data/jboss/" + user + "/configuration");


			((ChannelExec) channel).setCommand(commands.toString() );
			
			channel.setInputStream(null);

			((ChannelExec) channel).setErrStream(System.err);

			in = channel.getInputStream();

			channel.connect();

			byte[] tmp = new byte[1024];

			boolean firsttime = true;

			int retries = 0;

			try {
				while (true) {
					while (in.available() > 0) {
						int i = in.read(tmp, 0, 1024);
						if (i < 0)
							break;
						firsttime = false;

						String message = new String(tmp, 0, i);

						status.append(message);

						progress.setText(status.toString());

						progress.update();
						progress.redraw();
						progress.forceFocus();

						progress.getParent().update();
						progress.getParent().redraw();
						progress.getParent().layout(true);

					}
					if (retries > 5) {
						break;
					}
					try {
						Thread.sleep(1000);
						retries++;

					} catch (Exception ee) {
						System.out.println(ee.getMessage());
					}
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());

			}

			channel.disconnect();
			session.disconnect();

		} catch (Exception e) {
			System.out.println(e.getMessage());

		}

		return status.toString();

	}

	/**
	 * 
	 * pull JBoss standalone configuration files.
	 * 
	 * @param file
	 * @param viewer
	 * @return
	 */
	public static ConfigItem pullConfig(String file, TextViewer viewer) {

		String path = "none";

		ConfigItem configItem = null;

		String user = System.getProperty("user.name");

		try {

			Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

			String prefix = prefs.get("prefix", null);

			Session session = openSession(viewer.getControl().getShell());

			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;

			path = Platform.getLocation().toString() + "/.configuration";

			File theDir = new File(path);

			// if the directory does not exist, create it
			if (!theDir.exists()) {

				try {
					theDir.mkdir();

					String branchName = "";

					if (prefs != null) {
						if (prefs.get("environment", null) != null) {
							branchName = prefs.get("environment", null);
						}
					}

					configItem = new ConfigItem(file, "/user/data/jboss/" + user + "/configuration/" + file, path + "/" + file);

					sftpChannel.get("/user/data/jboss/" + user + "/configuration/" + file, path + "/" + file);

					sftpChannel.exit();

				} catch (Exception se) {
					showError(viewer.getControl().getShell(), se.getMessage());
				}
			} else {

				configItem = new ConfigItem(file, "/user/data/jboss/" + user + "/configuration/" + file, path + "/" + file);

				sftpChannel.get("/user/data/jboss/" + user + "/configuration/" + file, path + "/" + file);

				sftpChannel.exit();

			}

			session.disconnect();

		} catch (Exception e) {
			showError(viewer.getControl().getShell(), e.getMessage());

		}
		return configItem;

	}

	/**
	 * 
	 * pull application specific configuration artefact from /user/app/JBx/configuration/{application artefact} location.
	 * 
	 * @param directory
	 *            application specific directory location derived from the artefact id of the deployment.
	 * @param file
	 *            file to be pulled.
	 * @param shell
	 * @return
	 */
	public static ConfigItem pullConfigArtefact(String directory, String file, Shell shell) {

		String path = "none";

		ConfigItem configItem = null;

		try {

			Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

			String prefix = prefs.get("prefix", null);

			Session session = openSession(shell);

			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;

			path = Platform.getLocation().toString() + "/.configuration";

			File theDir = new File(path);

			// if the directory does not exist, create it
			if (!theDir.exists()) {

				try {
					theDir.mkdir();

				} catch (Exception se) {
					showError(shell, se.getMessage());
				}
			}

			String strippedFilename = file.substring(file.lastIndexOf("/") + 1);

			configItem = new ConfigItem(strippedFilename,
			                "/user/app/runuser/" + prefix.toUpperCase() + "/configuration/" + directory + "/" + file,
			                path + "/" + strippedFilename);

			sftpChannel.get("/user/app/runuser/" + prefix.toUpperCase() + "/configuration/" + directory + "/" + file,
			                path + "/" + strippedFilename);

			sftpChannel.exit();

			session.disconnect();

		} catch (Exception e) {
			showError(shell, e.getMessage());

		}
		return configItem;

	}

	/**
	 * 
	 * Pull specified log file into a Eclipse editor instance for view only purpose.
	 * 
	 * @param directory
	 * @param file
	 * @param shell
	 * @return
	 */
	public static LoggingItem pullLoggingArtefact(String directory, String file, Shell shell) {

		String path = "none";

		LoggingItem loggingItem = null;

		try {

			Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

			String prefix = prefs.get("prefix", null);

			Session session = openSession(shell);

			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;

			path = Platform.getLocation().toString() + "/.logging";

			File theDir = new File(path);

			// if the directory does not exist, create it
			if (!theDir.exists()) {

				try {
					theDir.mkdir();

				} catch (Exception se) {
					showError(shell, se.getMessage());
				}
			}

			String strippedFilename = file.substring(file.lastIndexOf("/") + 1);

			loggingItem = new LoggingItem(strippedFilename, "/user/log/runuser/" + prefix.toUpperCase() + "/" + file,
			                path + "/" + strippedFilename);

			sftpChannel.get("/user/log/runuser/" + prefix.toUpperCase() + "/" + file, path + "/" + strippedFilename);

			sftpChannel.exit();

			session.disconnect();

		} catch (Exception e) {
			showError(shell, e.getMessage());

		}
		return loggingItem;

	}

	/**
	 * 
	 * Pull application specific data file to Eclipse editor or download location workspace-dir/.data.
	 * 
	 * @param directory
	 * @param file
	 * @param shell
	 * @return
	 */
	public static DataItem pullDataArtefact(String directory, String file, Shell shell) {

		String path = "none";

		DataItem dataItem = null;

		try {

			Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

			String environment = prefs.get("environment", null);

			Session session = openSession(shell);

			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;

			path = Platform.getLocation().toString() + "/.data";

			File theDir = new File(path);

			// if the directory does not exist, create it
			if (!theDir.exists()) {

				try {
					theDir.mkdir();

				} catch (Exception se) {
					showError(shell, se.getMessage());
				}
			}

			String strippedFilename = file.substring(file.lastIndexOf("/") + 1);

			dataItem = new DataItem(strippedFilename, "/user/data/runuser/" + System.getProperty("user.name") + "/" + file,
			                path + "/" + strippedFilename);

			sftpChannel.get("/user/data/runuser/" + System.getProperty("user.name") + "/" + file, path + "/" + strippedFilename);

			sftpChannel.exit();

			session.disconnect();

		} catch (Exception e) {
			showError(shell, e.getMessage());

		}
		return dataItem;

	}

	/**
	 * 
	 * Down
	 * 
	 * @param destination
	 * @param file
	 * @param shell
	 * @return
	 */
	public static String downloadDataArtefact(String destination, String file, Shell shell) {

		try {

			Session session = openSession(shell);

			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;

			sftpChannel.get("/user/data/runuser/" + System.getProperty("user.name") + "/" + file, destination);

			sftpChannel.exit();

			session.disconnect();

		} catch (Exception e) {
			showError(shell, e.getMessage());

		}
		return destination;

	}

	/**
	 * 
	 * Pull server log to an Eclipse editor instance or view only purpose.
	 * 
	 * @param viewer
	 * @return
	 */
	public static String pullLog(TextViewer viewer) {

		String path = "none";

		try {

			Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

			String prefix = prefs.get("prefix", null);

			Session session = openSession(viewer.getControl().getShell());

			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;

			path = Platform.getLocation().toString() + "/.logs";

			File theDir = new File(path);

			String user = System.getProperty("user.name");

			// if the directory does not exist, create it
			if (!theDir.exists()) {

				try {
					theDir.mkdir();

					sftpChannel.get("/user/log/jboss/" + user + "/server.log", path + "/server.log");

					sftpChannel.exit();

				} catch (Exception se) {
					showError(viewer.getControl().getShell(), se.getMessage());

					se.printStackTrace();
				}
			} else {

				sftpChannel.get("/user/log/jboss/" + user + "/server.log", path + "/server.log");

				sftpChannel.exit();

			}

			session.disconnect();

		} catch (Exception e) {
			showError(viewer.getControl().getShell(), e.getMessage());

		}
		return path + "/server.log";

	}

	/**
	 * Flush the server log before restart of the server.
	 * 
	 * @param viewer
	 * @return
	 */
	public static StringBuilder removeLog(TextViewer viewer) {

		InputStream in = null;

		StringBuilder builder = new StringBuilder();

		try {

			Session session = openSession(null);

			Channel channel = session.openChannel("exec");

			((ChannelExec) channel).setCommand("rm /user/log/jboss/server.log");

			channel.setInputStream(null);

			((ChannelExec) channel).setErrStream(System.err);

			in = channel.getInputStream();

			channel.connect();

			byte[] tmp = new byte[1024];

			boolean firsttime = true;

			int loopCount = 0;

			try {
				while (true) {
					while (in.available() > 0) {
						int i = in.read(tmp, 0, 1024);
						if (i < 0)
							break;
						// System.out.print(new String(tmp, 0, i));
						firsttime = false;
						builder.append(new String(tmp, 0, i));
					}
					if (loopCount > 1) {
						builder.append("Server log has been removed, please restart the JBoss Server again!");
						break;
					}
					if (!firsttime) {
						break;
					}
					try {
						Thread.sleep(1000);
						loopCount++;
					} catch (Exception ee) {
						showError(viewer.getControl().getShell(), ee.getMessage());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			channel.disconnect();
			session.disconnect();

		} catch (Exception e) {
			e.printStackTrace();

		}

		return builder;

	}

	/**
	 * 
	 * refresh the tail on the JBoss main server.log file.
	 * 
	 * @param viewer
	 * @return
	 */
	public static StringBuilder refreshServerLog(TextViewer viewer) {

		InputStream in = null;

		StringBuilder builder = new StringBuilder();

		try {

			Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

			String prefix = prefs.get("prefix", null);

			Session session = openSession(null);

			Channel channel = session.openChannel("exec");

			String user = System.getProperty("user.name");

			((ChannelExec) channel).setCommand("tail -n 100 /user/log/jboss/" + user + "/server.log");

			channel.setInputStream(null);

			((ChannelExec) channel).setErrStream(System.err);

			in = channel.getInputStream();

			channel.connect();

			int loopCount = 0;

			try {
				while (true) {

					if (in.available() > 0) {
						java.util.Scanner s = new java.util.Scanner(in).useDelimiter("\\A");

						builder.append(s.hasNext() ? s.next() : "");

						in.close();

						break;

					}

					if (loopCount > 3) {
						break;
					}
					try {
						Thread.sleep(1000);
						loopCount++;
					} catch (Exception ee) {
						showError(viewer.getControl().getShell(), ee.getMessage());
					}
				}
				// capture the last line

			} catch (Exception e) {
				e.printStackTrace();
			}
			channel.disconnect();
			session.disconnect();

		} catch (Exception e) {
			e.printStackTrace();

		}

		return builder;

	}

	/**
	 * Refresh the tail on any of the selected log files.
	 * 
	 * @param viewer
	 * @return
	 */
	public static StringBuilder refreshApplicationLog(TextViewer viewer) {

		InputStream in = null;

		StringBuilder builder = new StringBuilder();

		try {

			Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

			String prefix = prefs.get("prefix", null);

			Session session = openSession(null);

			Channel channel = session.openChannel("exec");

			((ChannelExec) channel).setCommand("tail -n 100 " + LogFileWatcher.logFile.getUnixName());

			channel.setInputStream(null);

			((ChannelExec) channel).setErrStream(System.err);

			in = channel.getInputStream();

			channel.connect();

			int loopCount = 0;

			try {
				while (true) {

					if (in.available() > 0) {
						java.util.Scanner s = new java.util.Scanner(in).useDelimiter("\\A");

						builder.append(s.hasNext() ? s.next() : "");

						in.close();

						break;

					}

					if (loopCount > 3) {
						break;
					}
					try {
						Thread.sleep(1000);
						loopCount++;
					} catch (Exception ee) {
						showError(viewer.getControl().getShell(), ee.getMessage());
					}
				}
				// capture the last line

			} catch (Exception e) {
				e.printStackTrace();
			}
			channel.disconnect();
			session.disconnect();

		} catch (Exception e) {
			e.printStackTrace();

		}

		return builder;

	}

	/**
	 * List all configuration files for a specific application.
	 * 
	 * @return
	 */
	public static StringBuilder listConfiguration(String directoryName) {

		InputStream in = null;

		StringBuilder builder = new StringBuilder();

		try {

			Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

			String prefix = prefs.get("prefix", null);

			Session session = openSession(null);

			Channel channel = session.openChannel("exec");

			channel = session.openChannel("exec");

			((ChannelExec) channel).setCommand(
			                "cd /user/app/runuser/" + prefix.toUpperCase() + "/configuration/" + directoryName + ";find . -type f");

			channel.setInputStream(null);

			((ChannelExec) channel).setErrStream(System.err);

			in = channel.getInputStream();

			channel.connect();

			byte[] tmp = new byte[1024];

			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0)
						break;
					builder.append(new String(tmp, 0, i));
				}
				if (channel.isClosed()) {
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
					ee.printStackTrace();
				}
			}

			session.disconnect();

		} catch (Exception e) {
			e.printStackTrace();

		}
		return builder;

	}

	/**
	 * List all log files available to a combobox to be selected.
	 * 
	 * @return
	 */
	public static StringBuilder listLogFiles() {

		InputStream in = null;

		StringBuilder builder = new StringBuilder();

		try {

			Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

			String prefix = prefs.get("prefix", null);

			Session session = openSession(null);

			Channel channel = session.openChannel("exec");

			channel = session.openChannel("exec");

			((ChannelExec) channel).setCommand("cd /user/log/" + System.getProperty("user.name") + "/jboss;find . -type f");

			channel.setInputStream(null);

			((ChannelExec) channel).setErrStream(System.err);

			in = channel.getInputStream();

			channel.connect();

			byte[] tmp = new byte[1024];

			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0)
						break;
					builder.append(new String(tmp, 0, i));
				}
				if (channel.isClosed()) {
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
					ee.printStackTrace();
				}
			}

			session.disconnect();

		} catch (Exception e) {
			e.printStackTrace();

		}
		return builder;

	}

	/**
	 * List all log files available to a combobox to be selected.
	 * 
	 * @return
	 */
	public static StringBuilder listDockerPorts() {

		InputStream in = null;

		StringBuilder builder = new StringBuilder();

		try {

			Session session = openSession();

			Channel channel = session.openChannel("exec");

			((ChannelExec) channel).setPty(true);

			((ChannelExec) channel).setCommand("sudo /usr/bin/docker images | awk '$1 ~ /u/ { print $2}'");

			channel.setInputStream(null);

			((ChannelExec) channel).setErrStream(System.err);

			in = channel.getInputStream();

			channel.connect();

			byte[] tmp = new byte[1024];

			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0)
						break;
					builder.append(new String(tmp, 0, i));
				}
				if (channel.isClosed()) {
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
					ee.printStackTrace();
				}
			}

			// match first available port
			Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");
			Map<String, String> ports = new HashMap<>();

			Scanner scan = new Scanner(builder.toString());
			while (scan.hasNextLine()) {
				ports.put(scan.nextLine(), "value");
			}

			for (int availablePort = 8080; availablePort < 9090; availablePort++) {
				if (!ports.containsKey(Integer.toString(availablePort))) {
					prefs.put("port", Integer.toString(availablePort));
					// shift them all up with the new offset
					prefs.put("adminPort", Integer.toString(9999 + (availablePort - 8080)));
					prefs.put("debugPort", Integer.toString(8787 + (availablePort - 8080)));
					prefs.flush();
					break;
				}

			}

			session.disconnect();

		} catch (Exception e) {
			e.printStackTrace();

		}
		return builder;

	}

	/**
	 * List all log files available to a combobox to be selected.
	 * 
	 * @return
	 */
	public static List<String> listPrivateContainers() {

		InputStream in = null;

		StringBuilder builder = new StringBuilder();

		List<String> images = new ArrayList<>();

		try {

			Session session = openSession();

			Channel channel = session.openChannel("exec");

			((ChannelExec) channel).setPty(true);

			((ChannelExec) channel).setCommand("sudo /usr/bin/docker ps -a  --filter \"name=user\" --format \"{{.Names}}\"");

			channel.setInputStream(null);

			((ChannelExec) channel).setErrStream(System.err);

			in = channel.getInputStream();

			channel.connect();

			byte[] tmp = new byte[1024];

			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0)
						break;
					builder.append(new String(tmp, 0, i));
				}
				if (channel.isClosed()) {
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
					ee.printStackTrace();
				}
			}

			// match first available port
			Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

			Scanner scan = new Scanner(builder.toString());
			String userName = System.getProperty("user.name");
			while (scan.hasNextLine()) {
				String name = scan.nextLine();
				if (name.startsWith(userName)) {
					images.add(name);
				}
			}

			session.disconnect();

		} catch (Exception e) {
			e.printStackTrace();

		}
		return images;

	}


	/**
	 * List all log files available to a combobox to be selected.
	 * 
	 * @return
	 */
	public static List<String> listPrivateInActiveContainers() {

		InputStream in = null;

		StringBuilder builder = new StringBuilder();

		List<String> images = new ArrayList<>();

		try {

			Session session = openSession();

			Channel channel = session.openChannel("exec");

			((ChannelExec) channel).setPty(true);

			((ChannelExec) channel).setCommand("sudo /usr/bin/docker ps -a  --filter name=user --filter status=exited --format \"{{.Names}}\"");

			channel.setInputStream(null);

			((ChannelExec) channel).setErrStream(System.err);

			in = channel.getInputStream();

			channel.connect();

			byte[] tmp = new byte[1024];

			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0)
						break;
					builder.append(new String(tmp, 0, i));
				}
				if (channel.isClosed()) {
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
					ee.printStackTrace();
				}
			}

			// match first available port
			Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

			Scanner scan = new Scanner(builder.toString());
			String userName = System.getProperty("user.name");
			while (scan.hasNextLine()) {
				String name = scan.nextLine();
				if (name.startsWith(userName)) {
					images.add(name);
				}
			}

			session.disconnect();

		} catch (Exception e) {
			e.printStackTrace();

		}
		return images;

	}

	
	/**
	 * List all available data directory for a specific JB jboss instance.
	 * 
	 * @return
	 */
	public static StringBuilder listDataDirectories() {

		InputStream in = null;

		StringBuilder builder = new StringBuilder();

		try {

			Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

			Session session = openSession(null);

			Channel channel = session.openChannel("exec");

			channel = session.openChannel("exec");

			((ChannelExec) channel).setCommand("cd /user/data/runuser/" + System.getProperty("user.name") + ";find . -type d");

			channel.setInputStream(null);

			((ChannelExec) channel).setErrStream(System.err);

			in = channel.getInputStream();

			channel.connect();

			byte[] tmp = new byte[1024];

			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0)
						break;
					builder.append(new String(tmp, 0, i));
				}
				if (channel.isClosed()) {
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
					ee.printStackTrace();
				}
			}

			session.disconnect();

		} catch (Exception e) {
			e.printStackTrace();

		}
		return builder;

	}

	/**
	 * recursively list all datafiles for a specific JBoss instance.
	 * 
	 * @return
	 */
	public static StringBuilder listDataFiles() {

		InputStream in = null;

		StringBuilder builder = new StringBuilder();

		try {

			Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

			String prefix = prefs.get("prefix", null);

			String environment = prefs.get("environment", null);

			Session session = openSession(null);

			Channel channel = session.openChannel("exec");

			channel = session.openChannel("exec");

			((ChannelExec) channel).setCommand("cd /user/data/runuser/" + System.getProperty("user.name") + ";find . -type f");

			channel.setInputStream(null);

			((ChannelExec) channel).setErrStream(System.err);

			in = channel.getInputStream();

			channel.connect();

			byte[] tmp = new byte[1024];

			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0)
						break;
					builder.append(new String(tmp, 0, i));
				}
				if (channel.isClosed()) {
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
					ee.printStackTrace();
				}
			}

			session.disconnect();

		} catch (Exception e) {
			e.printStackTrace();

		}
		return builder;

	}
	

	public static StringBuilder removeImageAndContainer(String name) {

		InputStream in = null;

		StringBuilder builder = new StringBuilder();

		try {

			String image = String.format("%s/%s:%s", name.substring(0,6),name.substring(6, name.length()-4),name.substring(name.length()-4));
			
			Session session = openSession();

			Channel channel = session.openChannel("exec");

			((ChannelExec) channel).setPty(true);

			((ChannelExec) channel).setCommand("sudo /usr/bin/docker rm " + name + ";sudo /usr/bin/docker rmi " + image);

			channel.setInputStream(null);

			((ChannelExec) channel).setErrStream(System.err);

			in = channel.getInputStream();

			channel.connect();

			byte[] tmp = new byte[1024];

			boolean firsttime = true;

			int retries = 0;

			try {
				while (true) {
					while (in.available() > 0) {
						int i = in.read(tmp, 0, 1024);
						if (i < 0)
							break;
						firsttime = false;
						builder.append(new String(tmp, 0, i));
					}
					if (!firsttime) {
						break;
					}
					if (retries > MAX_RETRIES) {
						break;
					}
					try {
						Thread.sleep(1000);
						retries++;
					} catch (Exception ee) {
						System.out.println(ee.getMessage());
					}
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}

			channel.disconnect();
			session.disconnect();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return builder;

	}


	/**
	 * Get the configuration directory for a specific deployment.
	 * 
	 * @return
	 */
	public static String getArtefactDir() {

		InputStream in = null;

		String directoryName = null;

		try {

			Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

			String prefix = prefs.get("prefix", null);

			Session session = openSession(null);

			Channel channel = session.openChannel("exec");

			((ChannelExec) channel).setCommand(
			                "cd /user/app/runuser/" + prefix.toUpperCase() + "/configuration;find -name configuration.properties");

			channel.setInputStream(null);

			((ChannelExec) channel).setErrStream(System.err);

			in = channel.getInputStream();

			channel.connect();

			byte[] tmp = new byte[1024];
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0)
						break;
					directoryName = new String(tmp, 0, i).split("/")[1];
				}
				if (channel.isClosed()) {
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
					ee.printStackTrace();
				}
			}

			channel.disconnect();

			session.disconnect();

		} catch (Exception e) {
			e.printStackTrace();

		}
		return directoryName;

	}

	public static StringBuilder stopBoss(TextViewer viewer) {

		InputStream in = null;

		StringBuilder builder = new StringBuilder();

		try {

			Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

			String container = prefs.get("container", null);

			Session session = openSession(viewer.getControl().getShell());

			Channel channel = session.openChannel("exec");

			((ChannelExec) channel).setPty(true);

			((ChannelExec) channel).setCommand("sudo /usr/bin/docker stop " + container);

			channel.setInputStream(null);

			((ChannelExec) channel).setErrStream(System.err);

			in = channel.getInputStream();

			channel.connect();

			byte[] tmp = new byte[1024];

			boolean firsttime = true;

			int retries = 0;

			try {
				while (true) {
					while (in.available() > 0) {
						int i = in.read(tmp, 0, 1024);
						if (i < 0)
							break;
						firsttime = false;
						builder.append(new String(tmp, 0, i));
					}
					if (!firsttime) {
						break;
					}
					if (retries > MAX_RETRIES) {
						showMessage(viewer.getControl().getShell(), "Unable to stop JBoss instance");
						break;
					}
					try {
						Thread.sleep(1000);
						retries++;
					} catch (Exception ee) {
						showError(viewer.getControl().getShell(), ee.getMessage());
					}
				}
			} catch (Exception e) {

				showError(viewer.getControl().getShell(), e.getMessage());

			}

			channel.disconnect();
			session.disconnect();

		} catch (Exception e) {
			showError(viewer.getControl().getShell(), e.getMessage());

		}
		return builder;

	}

	public static StringBuilder stopBoss() {

		InputStream in = null;

		StringBuilder builder = new StringBuilder();

		try {

			Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

			String container = prefs.get("container", null);

			Session session = openSession();

			Channel channel = session.openChannel("exec");

			((ChannelExec) channel).setPty(true);

			((ChannelExec) channel).setCommand("sudo /usr/bin/docker stop " + container);

			channel.setInputStream(null);

			((ChannelExec) channel).setErrStream(System.err);

			in = channel.getInputStream();

			channel.connect();

			byte[] tmp = new byte[1024];

			boolean firsttime = true;

			int retries = 0;

			try {
				while (true) {
					while (in.available() > 0) {
						int i = in.read(tmp, 0, 1024);
						if (i < 0)
							break;
						firsttime = false;
						builder.append(new String(tmp, 0, i));
					}
					if (!firsttime) {
						break;
					}
					if (retries > MAX_RETRIES) {
						break;
					}
					try {
						Thread.sleep(1000);
						retries++;
					} catch (Exception ee) {
						System.out.println(ee.getMessage());
					}
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}

			channel.disconnect();
			session.disconnect();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return builder;

	}

	public static StringBuilder startJBoss(TextViewer viewer) {

		InputStream in = null;
		
		StringBuilder commands = new StringBuilder();

		String user = System.getProperty("user.name");

		Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

		String container = prefs.get("container", null);
		
		StringBuilder builder = new StringBuilder();
		
		commands.append("sudo /usr/bin/docker start " + container);
		commands.append(";sudo /usr/bin/docker cp " + container
		                + ":/opt/jboss/wildfly/standalone/configuration/standalone.xml /user/data/jboss/" + user + "/configuration");
		commands.append(";sudo /usr/bin/docker cp " + container
		                + ":/opt/jboss/wildfly/bin/standalone.conf /user/data/jboss/" + user + "/configuration");

		try {

			Session session = openSession(viewer.getControl().getShell());

			Channel channel = session.openChannel("exec");

			((ChannelExec) channel).setPty(true);

			((ChannelExec) channel).setCommand(commands.toString() );

			channel.setInputStream(null);

			((ChannelExec) channel).setErrStream(System.err);

			in = channel.getInputStream();

			channel.connect();

			byte[] tmp = new byte[1024];

			boolean firsttime = true;

			int retries = 0;

			try {
				while (true) {
					while (in.available() > 0) {
						int i = in.read(tmp, 0, 1024);
						if (i < 0)
							break;
						// System.out.print(new String(tmp, 0, i));
						firsttime = false;
						builder.append(new String(tmp, 0, i));
					}
					if (!firsttime) {
						break;
					}
					if (retries > MAX_RETRIES) {
						showMessage(viewer.getControl().getShell(), "Unable to start JBoss instance");
						break;
					}
					try {
						Thread.sleep(1000);
						retries++;

					} catch (Exception ee) {
						showError(viewer.getControl().getShell(), ee.getMessage());
					}
				}
			} catch (Exception e) {

				showError(viewer.getControl().getShell(), e.getMessage());

			}

			channel.disconnect();
			session.disconnect();

		} catch (Exception e) {
			showError(viewer.getControl().getShell(), e.getMessage());

		}
		return builder;

	}

	public static StringBuilder startJBoss() {

		InputStream in = null;

		StringBuilder builder = new StringBuilder();

		String user = System.getProperty("user.name");

		Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

		String container = prefs.get("container", null);

		StringBuilder commands = new StringBuilder();

		commands.append("sudo /usr/bin/docker start " + container);
		commands.append(";sudo /usr/bin/docker cp " + container
		                + ":/opt/jboss/wildfly/standalone/configuration/standalone.xml /user/data/jboss/" + user + "/configuration");
		commands.append(";sudo /usr/bin/docker cp " + container
		                + ":/opt/jboss/wildfly/bin/standalone.conf /user/data/jboss/" + user + "/configuration");
		

		try {

			Session session = openSession();

			Channel channel = session.openChannel("exec");

			((ChannelExec) channel).setPty(true);

			((ChannelExec) channel).setCommand(commands.toString());

			channel.setInputStream(null);

			((ChannelExec) channel).setErrStream(System.err);

			in = channel.getInputStream();

			channel.connect();

			byte[] tmp = new byte[1024];

			boolean firsttime = true;

			int retries = 0;

			try {
				while (true) {
					while (in.available() > 0) {
						int i = in.read(tmp, 0, 1024);
						if (i < 0)
							break;
						// System.out.print(new String(tmp, 0, i));
						firsttime = false;
						builder.append(new String(tmp, 0, i));
					}
					if (!firsttime) {
						break;
					}
					if (retries > MAX_RETRIES) {
						break;
					}
					try {
						Thread.sleep(1000);
						retries++;

					} catch (Exception ee) {
						System.out.println(ee.getMessage());
					}
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());

			}

			channel.disconnect();
			session.disconnect();

		} catch (Exception e) {
			System.out.println(e.getMessage());

		}
		return builder;

	}

	/**
	 * Open
	 * 
	 * @return
	 */
	private static Session openSession(Shell shell) {

		String path = Platform.getLocation().toString() + "/.configuration";

		File theDir = new File(path);

		// if the directory does not exist, create it
		if (!theDir.exists()) {

			try {
				theDir.mkdir();

				Git repos = Git.cloneRepository().setURI(REMOTE_URL).setDirectory(theDir).call();

				repos.checkout().setCreateBranch(true).setName("master").call();

			} catch (Exception se) {
				showError(shell, se.getMessage());
			}
		}

		Session session = null;

		if (session == null) {

			JSch jsch = new JSch();

			String privateKey = path + PUBLIC_KEY_FILE;
			try {

				jsch.addIdentity(privateKey, "");

				Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

				String environment = prefs.get("environment", null);

				session = jsch.getSession("runuser", environment, 22);

				session.setUserInfo(new IdsUserInfo());

				session.setConfig("StrictHostKeyChecking", "no");
				session.setTimeout(15000);

				session.connect();

			} catch (Exception e) {
				showError(shell, e.getMessage());
			}

		}

		return session;

	}

	/**
	 * Open
	 * 
	 * @return
	 */
	private static Session openSession() {

		String path = Platform.getLocation().toString() + "/.configuration";

		File theDir = new File(path);

		// if the directory does not exist, create it
		if (!theDir.exists()) {

			try {
				theDir.mkdir();

				Git repos = Git.cloneRepository().setURI(REMOTE_URL).setDirectory(theDir).call();

				repos.getRepository().getConfig().setBoolean("core", "", "core.safecrlf", false);

				repos.checkout().setCreateBranch(true).setName("master").call();

				repos.pull().call();

			} catch (Exception se) {
				System.out.println(se.getMessage());
			}
		}

		Session session = null;

		if (session == null) {

			JSch jsch = new JSch();

			String privateKey = path + PUBLIC_KEY_FILE;
			try {

				jsch.addIdentity(privateKey, "");

				Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

				String environment = prefs.get("environment", null);

				session = jsch.getSession("runuser", environment, 22);

				session.setUserInfo(new IdsUserInfo());

				session.setConfig("StrictHostKeyChecking", "no");
				session.setTimeout(15000);

				session.connect();

			} catch (Exception e) {
				System.out.println(e.getMessage());
			}

		}

		return session;

	}

	private static void showError(Shell shell, String message) {

		MessageDialog.openError(shell, "JSP Exception Occurred", message);
	}

	private static void showMessage(Shell shell, String message) {

		MessageDialog.openInformation(shell, "JSP Exception Occurred", message);
	}

	private static void removeCRLF() {

		String resultName = Platform.getLocation().toString() + "/.configuration/docker/customization/execute.stripped";

		Path path = Paths.get(Platform.getLocation().toString() + "/.configuration/docker/customization", "execute.sh");

		List<String> lines;
		try {
			lines = Files.readAllLines(path, StandardCharsets.UTF_8);

			PrintWriter pw = new PrintWriter(resultName);
			for (String line : lines) {
				pw.write(line + "\n");
			}

			pw.close();
			// String fileString = builder.toString().replaceAll("\\r", "\n");
			// fileString = fileString.replaceAll("\\r", "\n");
			// ..
			// write to file in binary mode.. something like:

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
