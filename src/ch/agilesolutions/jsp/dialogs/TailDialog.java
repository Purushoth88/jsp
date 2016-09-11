package ch.agilesolutions.jsp.dialogs;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.osgi.service.prefs.Preferences;

import ch.agilesolutions.jsp.model.LoggingItem;
import ch.agilesolutions.jsp.utils.RemoteExecutor;
import ch.agilesolutions.jsp.watchers.LogFileWatcher;

public class TailDialog extends TitleAreaDialog {

	private String configItem;

	private Shell shell;

	private IWorkbenchPage page;

	private String directory;

	public TailDialog(IWorkbenchPage page, Shell parentShell) {
		super(parentShell);
		this.page = page;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Select the Log file to switch Tail");

	}

	@Override
	protected Control createDialogArea(Composite parent) {
		this.shell = parent.getShell();

		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout(2, false);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setLayout(layout);

		createFirstName(container);

		return area;
	}

	private void createFirstName(Composite container) {
		Label lbtFirstName = new Label(container, SWT.NONE);

		lbtFirstName.setText("Environment");

		GridData dataFirstName = new GridData();
		dataFirstName.grabExcessHorizontalSpace = true;
		dataFirstName.horizontalAlignment = GridData.FILL;

		final Combo c1 = new Combo(container, SWT.READ_ONLY);

		c1.setBounds(50, 50, 150, 65);

		StringBuilder sb = RemoteExecutor.listLogFiles();

		String items[] = sb.toString().split("\\n");

		String values[] = new String[items.length];

		for (int i = 0; i < items.length; i++) {
			values[i] = items[i].substring(items[i].indexOf("/") + 1);

		}

		c1.setItems(values);

		if (values.length > 0) {
			c1.setText(values[0]);
			configItem = values[0];
		}

		c1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

				configItem = c1.getText();

			}
		});

	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	// save content of the Text fields because they get disposed
	// as soon as the Dialog closes
	// read http://www.massapi.com/class/org/eclipse/swt/events/MenuDetectListener.html
	private void saveInput() {
		
		Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

		String prefix = prefs.get("prefix", null);
		
		directory = "/u01/log/admrun/" + prefix.toUpperCase();

		final LoggingItem item = RemoteExecutor.pullLoggingArtefact(directory, configItem, shell);

		//LogFileWatcher.logFile.setUnixName(item.getUnixName());

		prefs.put("tailfile", item.getFilename());

		try {

	        
	        IViewPart part = page.findView("ch.agilesolutions.jsp.views.LogFileView");
	        
	        if (part != null) {
	        	page.hideView(page.findView("ch.agilesolutions.jsp.views.LogFileView"));
	        }
			
			page.showView("ch.agilesolutions.jsp.views.LogFileView");
	        
	        
        } catch (PartInitException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }

		
	}

	@Override
	protected void okPressed() {
		saveInput();
		super.okPressed();
	}

	private static void showMessage(Shell shell, String message) {

		MessageDialog.openError(shell, "JSP Exception Occurred", message);
	}

}
