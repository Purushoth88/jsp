package ch.agilesolutions.jsp.dialogs;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
import org.osgi.service.prefs.Preferences;

import ch.agilesolutions.jsp.utils.RemoteExecutor;

import jsp.Activator;

public class EnvironmentDialog extends TitleAreaDialog {

	private String environment;

	public EnvironmentDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	public void create() {
		super.create();
		setTitle("Switch Docker Container");

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

		lbtFirstName.setText("Container");

		GridData dataFirstName = new GridData();
		dataFirstName.grabExcessHorizontalSpace = true;
		dataFirstName.horizontalAlignment = GridData.FILL;

		final Combo c1 = new Combo(container, SWT.READ_ONLY);

		c1.setBounds(50, 50, 150, 65);
		
		List<String> containers = RemoteExecutor.listPrivateContainers();
		
		String items[] = containers.toArray(new String[containers.size()]);

		Arrays.sort(items);
		
		c1.setItems(items);

		if (containers.size()> 0) {
			c1.setText(containers.get(0));
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

		// https://wiki.eclipse.org/FAQ_How_do_I_load_and_save_plug-in_preferences%3F

		RemoteExecutor.stopBoss();
		
		
		Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

		prefs.put("container", environment);
		prefs.put("image", String.format("%s/%s:%s", environment.substring(0, 6),environment.substring(6,environment.length()-4),environment.substring(environment.length()-4)));
		int availablePort = Integer.valueOf(environment.substring(environment.length()-4));
		prefs.put("port",Integer.toString(availablePort) );
		// shift them all up with the new offset
		prefs.put("adminPort", Integer.toString(9999 + (availablePort - 8080)));
		prefs.put("debugPort", Integer.toString(8787 + (availablePort - 8080)));


		try {
			prefs.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Activator.getDefault().getLog().log(new Status(IStatus.INFO, "JSP", "Switch to container " + environment));
		RemoteExecutor.startJBoss();
		MessageDialog.openInformation(getShell(), "JSP View", String.format("Switched Docker Container %s!", environment));


	}

	@Override
	protected void okPressed() {
		saveInput();
		super.okPressed();
	}

}
