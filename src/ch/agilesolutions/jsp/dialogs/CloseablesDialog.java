package ch.agilesolutions.jsp.dialogs;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.preferences.InstanceScope;
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
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.osgi.service.prefs.Preferences;

import ch.agilesolutions.jsp.utils.RemoteExecutor;

import jsp.Activator;

public class CloseablesDialog extends TitleAreaDialog {

	private String environment;
	
	private IWorkbenchPage page;


	public CloseablesDialog(IWorkbenchPage page,Shell parentShell) {
		super(parentShell);
		this.page = page;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Stop Containers");

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

		lbtFirstName.setText("Environment");

		GridData dataFirstName = new GridData();
		dataFirstName.grabExcessHorizontalSpace = true;
		dataFirstName.horizontalAlignment = GridData.FILL;

		final Combo c1 = new Combo(container, SWT.READ_ONLY);

		c1.setBounds(50, 50, 150, 65);
		
		List<String> containers = RemoteExecutor.listPrivateActiveContainers();
		
		String items[] = containers.toArray(new String[containers.size()]);

		Arrays.sort(items);
		
		c1.setItems(items);

		if (containers.size()> 0) {
			c1.setText(containers.get(0));
			environment = containers.get(0);
		}

		Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

		if (prefs != null) {
			if (prefs.get("environment", null) != null) {
				c1.setText(prefs.get("environment", null));
			}
		}

		c1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

				environment = c1.getText();

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

		RemoteExecutor.spinDownContainer(environment);
		
		IViewPart part = Activator.parts.get(environment);
		
		page.hideView(part);

	}

	@Override
	protected void okPressed() {
		saveInput();
		super.okPressed();
	}

}
