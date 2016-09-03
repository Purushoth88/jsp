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
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.service.prefs.Preferences;

import ch.agilesolutions.jsp.model.DataItem;
import ch.agilesolutions.jsp.model.Environment;
import ch.agilesolutions.jsp.utils.RemoteExecutor;

public class UploadDialog extends TitleAreaDialog {

	private String configItem;

	private Shell shell;

	private IWorkbenchPage page;
	
	private String selectedFile;

	public UploadDialog(IWorkbenchPage page, Shell parentShell) {
		super(parentShell);
		this.page = page;
	}

	public void setSelectedFile(String selectedFile) {
		this.selectedFile = selectedFile;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Select remote directory you wish to upload to");

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

		createComponents(container);

		return area;
	}

	private void createComponents(Composite container) {
		Label lbtFirstName = new Label(container, SWT.NONE);

		lbtFirstName.setText("Directories");

		GridData dataFirstName = new GridData();
		dataFirstName.grabExcessHorizontalSpace = true;
		dataFirstName.horizontalAlignment = GridData.FILL;

		final Combo c1 = new Combo(container, SWT.READ_ONLY);

		c1.setBounds(50, 50, 150, 65);

		StringBuilder sb = RemoteExecutor.listDataDirectories();

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

		Environment env = Environment.getEnvironment();
		
		String directory = "/u01/data/admrun/" + env.getContainer();

		String items[] = selectedFile.toString().split("\\\\");
		
		String filename = items[items.length-1];
		
		String unixName = directory + "/" + configItem + "/" + filename;
		
		final DataItem item = new DataItem(filename, unixName, selectedFile);
		
		RemoteExecutor.pushData(item, shell);

		File file = new File(item.getWindowsName());

		try {

			IFileStore fileStore = EFS.getLocalFileSystem().getStore(file.toURI());
			FileStoreEditorInput editorInput = new FileStoreEditorInput(fileStore);

			IEditorPart openEditor = page.openEditor(editorInput, EditorsUI.DEFAULT_TEXT_EDITOR_ID);
			
			ITextEditor editor = (ITextEditor) openEditor;

			editor.addPropertyListener(new IPropertyListener() {
				@Override
				public void propertyChanged(Object source, int propId) {
					if (propId == IEditorPart.PROP_DIRTY && source instanceof EditorPart) {
						EditorPart editor = (EditorPart) source;
						if (editor.isDirty() == false) {
							RemoteExecutor.pushData(item, shell);
							showMessage(shell,item.getFilename() + " pushed to " + item.getUnixName());
						}
					}
				}
			});
			

		} catch (Exception e) {
			e.printStackTrace();
		}

		showMessage(shell,item.getFilename() + " pushed to " + item.getUnixName());
		

	}

	@Override
	protected void okPressed() {
		saveInput();
		super.okPressed();
	}

	private static void showMessage(Shell shell, String message) {

		MessageDialog.openInformation(shell, "JSP Confirmation", message);
	}

}
