package ch.agilesolutions.jsp.dialogs;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import ch.agilesolutions.jsp.utils.RemoteExecutor;

public class DeployDialog extends TitleAreaDialog {

	private String deployment;

	private List<String> deployments;

	private Map<String, String> mapDirectories = new HashMap<String, String>();

	public DeployDialog(Shell parentShell, List<String> deployments, List<String> directories) {
		super(parentShell);
		this.deployments = deployments;

		int i = 0;

		for (String deployment : deployments) {
			mapDirectories.put(deployment, directories.get(i));
			i++;
		}

	}

	@Override
	public void create() {
		super.create();
		setTitle("Select archive to be deployed");

	}

	@Override
	protected Control createDialogArea(Composite parent) {
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

		lbtFirstName.setText("Archive");

		GridData dataFirstName = new GridData();
		dataFirstName.grabExcessHorizontalSpace = true;
		dataFirstName.horizontalAlignment = GridData.FILL;

		final Combo c1 = new Combo(container, SWT.READ_ONLY);

		c1.setBounds(50, 50, 150, 65);

		String items[] = new String[deployments.size()];

		items = deployments.toArray(items);

		c1.setItems(items);

		c1.setText(deployments.get(0));
		
		deployment = deployments.get(0);

		c1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				deployment = c1.getText();

			}
		});

	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	// save content of the Text fields because they get disposed
	// as soon as the Dialog closes
	private void saveInput() {

		// showMessage(String.format("Deploying %s", deployment));
		RemoteExecutor.deploy(mapDirectories.get(deployment) + File.separator + deployment, deployment, getShell());

	}

	@Override
	protected void okPressed() {
		saveInput();
		super.okPressed();
	}

	private void showMessage(String message) {

		MessageDialog.openInformation(getShell(), "JSP View", message);
	}

}
