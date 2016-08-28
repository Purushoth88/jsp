package ch.agilesolutions.jsp.views;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.service.prefs.Preferences;

import ch.agilesolutions.jsp.dialogs.ConfigurationDialog;
import ch.agilesolutions.jsp.dialogs.DataDialog;
import ch.agilesolutions.jsp.dialogs.DeployDialog;
import ch.agilesolutions.jsp.dialogs.EnvironmentDialog;
import ch.agilesolutions.jsp.dialogs.HouseKeepingDialog;
import ch.agilesolutions.jsp.dialogs.LoggingDialog;
import ch.agilesolutions.jsp.dialogs.ProfileDialog;
import ch.agilesolutions.jsp.dialogs.TailDialog;
import ch.agilesolutions.jsp.dialogs.UploadDialog;
import ch.agilesolutions.jsp.listeners.LogFileListener;
import ch.agilesolutions.jsp.model.ConfigItem;
import ch.agilesolutions.jsp.model.DataItem;
import ch.agilesolutions.jsp.model.Environment;
import ch.agilesolutions.jsp.utils.RemoteExecutor;
import ch.agilesolutions.jsp.watchers.ServerLogWatcher;

/**
 * 
 * IDS root view window
 *
 * @author agilesolutions
 * @version $Revision$, $Date$
 */
public class JSPView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "jsp.views.JSPView";

	public static final String VERSION = "1.0.0";

	private TextViewer viewer;
	private Action pullStandaloneXmlMenu;
	private Action stopServerMenu;
	private Action startServerMenu;
	private Action deployMenu;
	private Action switchEnvironmentMenu;
	private Action pullServerLogMenu;
	private Action pullStandaloneConfMenu;
	private Action launchAppMenu;
	private Action infoMenu;
	private Action launchConsoleMenu;
	private Action listConfigFilesMenu;
	private Action houseKeepingMenu;
	private Action openWikiPageMenu;
	private Action listLogFileMenu;
	private Action listDataFileMenu;
	private Action newDataFileMenu;
	private Action tailAllLogFilesMenu;
	private Action dockerMenu;
	private Action cliMenu;

	private FileDialog fd;

	private String selected;

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {

		viewer = new TextViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setEditable(true);
		final StyledText styledText = viewer.getTextWidget();
		styledText.setWordWrap(true);
		styledText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent event) {
				if (event.keyCode == 'p' && (event.stateMask & SWT.CTRL) != 0) {
					styledText.print();
				}
			}
		});

		final Document newDoc = new Document();
		viewer.setDocument(newDoc);
		viewer.getTextWidget().cut();
		viewer.getTextWidget().append("initializing log");
		viewer.setInput(getViewSite());

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "jsp.viewer");
		makeActions();
		hookContextMenu();

		final StyledText text = viewer.getTextWidget();

		ServerLogWatcher watcher = new ServerLogWatcher();

		final Display display = Display.getCurrent();
		watcher.addListener(new LogFileListener() {

			public void update(final StringBuilder in) {

				display.asyncExec(new Runnable() {
					public void run() {

						if (text.getCharCount() > 20 && in.length() > 20) {
							if (!text.getTextRange(text.getCharCount() - 20, 20).equals(
							                in.toString().substring(in.toString().length() - 20))) {
								text.replaceTextRange(0, text.getCharCount(), in.toString());
							}

						} else {
							text.replaceTextRange(0, text.getCharCount(), in.toString());
						}

						viewer.setTopIndex(newDoc.getNumberOfLines());

					}
				});

			}
		});

		watcher.start();

	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				JSPView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void fillContextMenu(IMenuManager manager) {
		// pull standalone.xml
		manager.add(pullStandaloneXmlMenu);
		manager.add(new Separator());
		// pull standalone.conf
		manager.add(pullStandaloneConfMenu);
		manager.add(new Separator());
		manager.add(stopServerMenu);
		manager.add(startServerMenu);
		manager.add(new Separator());
		manager.add(deployMenu);
		// switch environment
		manager.add(new Separator());
		manager.add(switchEnvironmentMenu);
		manager.add(houseKeepingMenu);
//		manager.add(new Separator());
//		manager.add(listConfigFilesMenu);
//		manager.add(listDataFileMenu);
//		manager.add(newDataFileMenu);
		manager.add(new Separator());
		manager.add(pullServerLogMenu);
//		manager.add(listLogFileMenu);
//		manager.add(tailAllLogFilesMenu);
		manager.add(new Separator());
		manager.add(infoMenu);
		manager.add(new Separator());
		manager.add(launchConsoleMenu);
		manager.add(launchAppMenu);
//		manager.add(openWikiPageMenu);
		manager.add(new Separator());
		manager.add(dockerMenu);

	}

	private void makeActions() {
		pullStandaloneXmlMenu = new Action() {
			public void run() {

				final ConfigItem item = RemoteExecutor.pullConfig("standalone.xml", viewer);

				File file = new File(item.getWindowsName());

				try {

					IFileStore fileStore = EFS.getLocalFileSystem().getStore(file.toURI());
					FileStoreEditorInput editorInput = new FileStoreEditorInput(fileStore);

					IWorkbenchPage page = getViewSite().getPage();

					IEditorPart openEditor = page.openEditor(editorInput, EditorsUI.DEFAULT_TEXT_EDITOR_ID);

					ITextEditor editor = (ITextEditor) openEditor;

					editor.addPropertyListener(new IPropertyListener() {
						@Override
						public void propertyChanged(Object source, int propId) {
							if (propId == IEditorPart.PROP_DIRTY && source instanceof EditorPart) {
								EditorPart editor = (EditorPart) source;
								if (editor.isDirty() == false) {
									RemoteExecutor.pushConfig("standalone.xml", viewer);
									showMessage(item.getFilename() + " pushed to " + item.getUnixName());
								}
							}
						}
					});
				} catch (Exception e) {
					showError("Error occurred : " + e.getMessage());
					e.printStackTrace();
				}

			}

		};
		pullStandaloneXmlMenu.setText("Pull standalone.xml");
		pullStandaloneXmlMenu.setToolTipText("Pull JBoss standalone.xml configuration from server");
		pullStandaloneXmlMenu.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_UP));
		
		// cli menu
		
		cliMenu = new Action() {
			public void run() {
				
				
				File file = new File(Platform.getLocation().toString() + "/.configuration/custom.cli");
				file.createNewFile();

				try {

					IFileStore fileStore = EFS.getLocalFileSystem().getStore(file.toURI());
					FileStoreEditorInput editorInput = new FileStoreEditorInput(fileStore);

					IWorkbenchPage page = getViewSite().getPage();

					IEditorPart openEditor = page.openEditor(editorInput, EditorsUI.DEFAULT_TEXT_EDITOR_ID);

					ITextEditor editor = (ITextEditor) openEditor;

					editor.addPropertyListener(new IPropertyListener() {
						@Override
						public void propertyChanged(Object source, int propId) {
							if (propId == IEditorPart.PROP_DIRTY && source instanceof EditorPart) {
								EditorPart editor = (EditorPart) source;
								if (editor.isDirty() == false) {
									RemoteExecutor.executeCLI("custom.cli", viewer);
									showMessage(item.getFilename() + " cli executed " + item.getUnixName());
								}
							}
						}
					});

					// addEditorSavedListener(editor);
				} catch (Exception e) {
					showError("Error occurred : " + e.getMessage());
					e.printStackTrace();
				}

			}

		};
		cliMenu.setText("Pull standalone.conf");
		cliMenu.setToolTipText("Execute CLI on Dockerized JBoss");
		cliMenu.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_UP));

		// standalone conf

		pullStandaloneConfMenu = new Action() {
			public void run() {

				final ConfigItem item = RemoteExecutor.pullConfig("standalone.conf", viewer);

				File file = new File(item.getWindowsName());

				try {

					IFileStore fileStore = EFS.getLocalFileSystem().getStore(file.toURI());
					FileStoreEditorInput editorInput = new FileStoreEditorInput(fileStore);

					IWorkbenchPage page = getViewSite().getPage();

					IEditorPart openEditor = page.openEditor(editorInput, EditorsUI.DEFAULT_TEXT_EDITOR_ID);

					ITextEditor editor = (ITextEditor) openEditor;

					editor.addPropertyListener(new IPropertyListener() {
						@Override
						public void propertyChanged(Object source, int propId) {
							if (propId == IEditorPart.PROP_DIRTY && source instanceof EditorPart) {
								EditorPart editor = (EditorPart) source;
								if (editor.isDirty() == false) {
									RemoteExecutor.pushConfig("standalone.conf", viewer);
									showMessage(item.getFilename() + " pushed to " + item.getUnixName());
								}
							}
						}
					});

					// addEditorSavedListener(editor);
				} catch (Exception e) {
					showError("Error occurred : " + e.getMessage());
					e.printStackTrace();
				}

			}

		};
		pullStandaloneConfMenu.setText("Pull standalone.conf");
		pullStandaloneConfMenu.setToolTipText("Pull JBoss standalone.conf configuration from server");
		pullStandaloneConfMenu.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_UP));

		stopServerMenu = new Action() {
			public void run() {

				StringBuilder builder = RemoteExecutor.stopBoss(viewer);
				// showMessage(builder.toString());

			}
		};

		stopServerMenu.setText("Stop Server");
		stopServerMenu.setToolTipText("Stop JBoss server through service script");
		stopServerMenu.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_STOP));

		startServerMenu = new Action() {
			public void run() {

				StringBuilder builder = RemoteExecutor.startJBoss(viewer);
				// showMessage(builder.toString());

			}
		};

		startServerMenu.setText("Start Server");
		startServerMenu.setToolTipText("Start JBoss server through service script");
		startServerMenu.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_SYNCED));

		deployMenu = new Action() {
			public void run() {

				try {

					if (ResourcesPlugin.getWorkspace().getRoot().getProjects().length > 0) {

						List<String> deployments = new ArrayList<String>();

						List<String> directories = new ArrayList<String>();

						int hitCount = 0;

						for (int i = 0; i < ResourcesPlugin.getWorkspace().getRoot().getProjects().length; i++) {
							IProject project = ResourcesPlugin.getWorkspace().getRoot().getProjects()[i];

							int type = project.getType();

							String pname = project.getName();

							URI path = ((IResource) project).getLocationURI();

							File dir = new File(path.getPath() + File.separator + "target");

							if (dir.isDirectory()) {

								File[] files = dir.listFiles();

								for (File file : files) {

									if (file.isFile()) {
										String extension;
										String fileName = file.getName();
										int y = fileName.lastIndexOf('.');
										if (y > 0) {
											if (fileName.substring(y + 1).equalsIgnoreCase("ear")) {
												hitCount++;
												deployments.add(fileName);
												directories.add(dir.toString());
											}
											if (fileName.substring(y + 1).equalsIgnoreCase("war")) {
												hitCount++;
												deployments.add(fileName);
												directories.add(dir.toString());
											}

										}
									}
								}

							}

						}

						if (hitCount == 0) {
							showWarning("No deployment archives found, run your Maven build process first!");

						}

						if (hitCount == 1) {
							// showMessage(String.format("Deploying %s", deployments.get(0)));
							RemoteExecutor.deploy(directories.get(0) + File.separator + deployments.get(0), deployments.get(0), viewer
							                .getControl().getShell());

						}

						if (hitCount > 1) {
							DeployDialog dialog = new DeployDialog(viewer.getControl().getShell(), deployments, directories);

							dialog.open();

						}

					}

				} catch (Exception e) {
					showError("Error occurred : " + e.getMessage());
					e.printStackTrace();
				}

			}
		};

		deployMenu.setText("Deploy");
		deployMenu.setToolTipText("Start JBoss server through service script");
		deployMenu.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD));

		switchEnvironmentMenu = new Action() {
			public void run() {

				switchEnvironment();

			}
		};

		switchEnvironmentMenu.setText("Switch Container");
		switchEnvironmentMenu.setToolTipText("Run a different Docker Container");
		switchEnvironmentMenu.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ETOOL_DEF_PERSPECTIVE));

		listDataFileMenu = new Action() {
			public void run() {

				listDataFiles();

			}
		};

		listConfigFilesMenu = new Action() {
			public void run() {

				listConfiguration();

			}
		};

		listConfigFilesMenu.setText("List Configuration Files");
		listConfigFilesMenu.setToolTipText("List Configuration Files");
		listConfigFilesMenu.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE));

		listDataFileMenu.setText("List Data Files");
		listDataFileMenu.setToolTipText("List Data Files");
		listDataFileMenu.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE));

		newDataFileMenu = new Action() {
			public void run() {

				openDataFile();

			}
		};

		newDataFileMenu.setText("New Data File");
		newDataFileMenu.setToolTipText("New Data File");
		newDataFileMenu.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE));

		pullServerLogMenu = new Action() {
			public void run() {

				String filePath = RemoteExecutor.pullLog(viewer);

				File file = new File(filePath);

				IFileStore fileStore;
				try {

					EFS.getLocalFileSystem().getStore(new Path(file.getAbsolutePath()));

					fileStore = EFS.getLocalFileSystem().getStore(new Path(file.getAbsolutePath()));
					IWorkbenchPage page = getViewSite().getPage();
					IEditorPart editor = IDE.openEditorOnFileStore(page, fileStore);
				} catch (Exception e) {
					showError("Error occurred : " + e.getMessage());
					e.printStackTrace();
				}

			}
		};

		pullServerLogMenu.setText("Pull server.log");
		pullServerLogMenu.setToolTipText("Download Server.log and open in editor");
		pullServerLogMenu.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_UP));

		listLogFileMenu = new Action() {
			public void run() {

				listLoggingFiles();

			}
		};

		listLogFileMenu.setText("List Log Files");
		listLogFileMenu.setToolTipText("List Log Files");
		listLogFileMenu.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE));

		houseKeepingMenu = new Action() {
			public void run() {

				HouseKeepingDialog dialog = new HouseKeepingDialog(viewer.getControl().getShell());

				dialog.open();

			}
		};

		houseKeepingMenu.setText("Remove Docker inactive Images");
		houseKeepingMenu.setToolTipText("Remove Docker Images");
		houseKeepingMenu.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_REMOVE));

		tailAllLogFilesMenu = new Action() {
			public void run() {

				switchTail();

			}
		};

		tailAllLogFilesMenu.setText("Tail All Log Files");
		tailAllLogFilesMenu.setToolTipText("Add view with tail to another log file");
		tailAllLogFilesMenu.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ETOOL_DEF_PERSPECTIVE));

		infoMenu = new Action() {
			public void run() {

				showInfo();
			}
		};

		infoMenu.setText("Info");
		infoMenu.setToolTipText("Show Space Information");
		infoMenu.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

		launchConsoleMenu = new Action() {
			public void run() {


				Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");
				
				String url = String.format("http://%s:%s/console", prefs.get("environment", null),prefs.get("adminPort", null));



				try {
					PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(url));
				} catch (PartInitException | MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		launchConsoleMenu.setText("Launch JBoss Console");
		launchConsoleMenu.setToolTipText("Launch JBoss Console");
		launchConsoleMenu.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_DEF_VIEW));

		launchAppMenu = new Action() {
			public void run() {

				Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");
				
				String url = String.format("http://%s:%s/jdo", prefs.get("environment", null),prefs.get("port", null));
				

				try {
					PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(url));
				} catch (PartInitException | MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		launchAppMenu.setText("Launch Application Page");
		launchAppMenu.setToolTipText("Launch Application Page");
		launchAppMenu.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ETOOL_HOME_NAV));

		openWikiPageMenu = new Action() {
			public void run() {

				try {

					PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser()
					                .openURL(new URL("https://github.com/agilesolutions/jdo"));
				} catch (PartInitException | MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		openWikiPageMenu.setText("Open WIKI page");
		openWikiPageMenu.setToolTipText("Open WIKI page");
		openWikiPageMenu.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_LCL_LINKTO_HELP));
		
		
		dockerMenu = new Action() {
			public void run() {

				ProfileDialog dialog = new ProfileDialog(viewer.getControl().getShell());

				dialog.open();

			}
		};

		dockerMenu.setText("Dockerize JCT Profile");
		dockerMenu.setToolTipText("Dockerize JCT Profile");
		dockerMenu.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_LCL_LINKTO_HELP));

	}

	private void showMessage(String message) {

		MessageDialog.openInformation(viewer.getControl().getShell(), "JSP View", message);
	}

	private void showWarning(String message) {

		MessageDialog.openWarning(viewer.getControl().getShell(), "JSP View", message);
	}

	private void showError(String message) {

		MessageDialog.openWarning(viewer.getControl().getShell(), "JSP View", message);
	}

	private void showInfo() {

		Environment env = Environment.getEnvironment();

		MessageDialog.openInformation(
		                viewer.getControl().getShell(),
		                "IDS Space Allocation Information",
		                "You are working currently on environment " 
			                			+ "\n\n" + "DOCKER server          : " + env.getServer()
			                			+ "\n\n" + "JCT profile                : " + env.getName()
			                			+ "\n" +   "JBAR id                    : " + env.getJbar()
			                			+ "\n" +   "Docker image            : " + env.getImage()
			                			+ "\n" +   "Docker container        : " + env.getContainer()
			                			+ "\n" +   "Port                     : " + env.getPort()
			                			+ "\n" +   "Admin Port            : " + env.getAdminPort()
			                			+ "\n" +   "Debug Port             : " + env.getDebugPort()
		                            + "\n" + "JSP Plugin Version " + VERSION);
	}

	private void switchEnvironment() {

		EnvironmentDialog dialog = new EnvironmentDialog(viewer.getControl().getShell());

		dialog.open();

	}

	private void listConfiguration() {

		ConfigurationDialog dialog = new ConfigurationDialog(getViewSite().getPage(), viewer.getControl().getShell());

		dialog.open();

	}

	private void listLoggingFiles() {

		LoggingDialog dialog = new LoggingDialog(getViewSite().getPage(), viewer.getControl().getShell());

		dialog.open();

	}

	private void listDataFiles() {

		DataDialog dialog = new DataDialog(getViewSite().getPage(), viewer.getControl().getShell());

		dialog.open();

	}

	private void switchTail() {

		TailDialog dialog = new TailDialog(getViewSite().getPage(), viewer.getControl().getShell());

		dialog.open();

	}

	private void openDataFile() {

		fd = new FileDialog(viewer.getControl().getShell(), SWT.OPEN);
		fd.setText("Open");
		fd.setFilterPath("C:/");
		String[] filterExt = { "*.*", "*.*" };
		fd.setFilterExtensions(filterExt);
		selected = fd.open();

		if (selected != null) {
			UploadDialog dialog = new UploadDialog(getViewSite().getPage(), viewer.getControl().getShell());
			dialog.setSelectedFile(selected);

			dialog.open();

		}

		// saveInput();

	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	protected void addEditorSavedListener(IWorkbenchPart editor) {
		if (editor != null) {

			editor.addPropertyListener(new IPropertyListener() {

				@Override
				public void propertyChanged(Object source, int propId) {
					if (propId == IEditorPart.PROP_DIRTY && source instanceof EditorPart) {
						EditorPart editor = (EditorPart) source;
						if (editor.isDirty() == true)
							RemoteExecutor.pushConfig("standalone.xml", viewer);
						showMessage(String.format("JBoss Config published to Linux"));
					}
				}
			});
		}
	}

	// save content of the Text fields because they get disposed
	// as soon as the Dialog closes
	// read http://www.massapi.com/class/org/eclipse/swt/events/MenuDetectListener.html
	private void saveInput() {

		Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

		String environment = prefs.get("environment", null);

		Environment env = Environment.getEnvironment();

		String directory = "/user/data/runuser/" + env.getName().substring(6, 9).toUpperCase().trim();

		String items[] = selected.toString().split("\\\\");

		String filename = items[items.length - 1];

		String unixName = directory + "/" + filename;

		final DataItem item = new DataItem(filename, unixName, selected);

		RemoteExecutor.pushData(item, viewer.getControl().getShell());

		File file = new File(item.getWindowsName());

		try {

			IFileStore fileStore = EFS.getLocalFileSystem().getStore(file.toURI());
			FileStoreEditorInput editorInput = new FileStoreEditorInput(fileStore);

			IWorkbenchPage page = getViewSite().getPage();

			IEditorPart openEditor = page.openEditor(editorInput, EditorsUI.DEFAULT_TEXT_EDITOR_ID);

			ITextEditor editor = (ITextEditor) openEditor;

			editor.addPropertyListener(new IPropertyListener() {
				@Override
				public void propertyChanged(Object source, int propId) {
					if (propId == IEditorPart.PROP_DIRTY && source instanceof EditorPart) {
						EditorPart editor = (EditorPart) source;
						if (editor.isDirty() == false) {
							RemoteExecutor.pushData(item, viewer.getControl().getShell());
							showMessage(item.getFilename() + " pushed to " + item.getUnixName());
						}
					}
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
