package ch.agilesolutions.jsp.dialogs;

import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.prefs.Preferences;

import ch.agilesolutions.jsp.utils.RemoteExecutor;

public class ProfileDialog extends TitleAreaDialog {

	private String baseImage;

	private List<String> baseImages;

	private Text progress;
	private Text imageName;

	public ProfileDialog(Shell parentShell) {
		super(parentShell);

		this.baseImages = RemoteExecutor.listBaseImages();

	}

	@Override
	protected Point getInitialSize() {

		final Point size = super.getInitialSize();

		size.x = convertWidthInCharsToPixels(155);

		size.y += convertHeightInCharsToPixels(30);

		return size;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Select a base image to creating your own image and container");

	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);

		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout(2, false);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setLayout(layout);

		createBaseImage(container);

		createProfile(container);

		createProgress(container);

		return area;
	}

	private void createBaseImage(Composite container) {
		Label lbtBaseImage = new Label(container, SWT.NONE);

		// list of base images
		lbtBaseImage.setText("Base Image");

		final Combo images = new Combo(container, SWT.READ_ONLY);

		images.setBounds(50, 50, 450, 1400);

		String imageItems[] = new String[baseImages.size()];

		imageItems = baseImages.toArray(imageItems);

		images.setItems(imageItems);

		if (baseImages.size()>0) {
			images.setText(baseImages.get(0));
			baseImage = baseImages.get(0);
			
		} else {
			images.setText("no base images");
			baseImage = "empty";
			
		}


		images.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				baseImage = images.getText();

			}
		});

	}

	private void createProfile(Composite container) {
		Label lbtProfile = new Label(container, SWT.NONE);

		// List of JCT profiles
		lbtProfile.setText("Image name to create ");

		GridData profileGrid = new GridData();
		profileGrid.grabExcessHorizontalSpace = true;
		profileGrid.horizontalAlignment = GridData.FILL;

		imageName = new Text(container, SWT.BORDER);
		imageName.setLayoutData(profileGrid);

	}

	private void createProgress(Composite container) {

		Label lbtProgress = new Label(container, SWT.NONE);
		lbtProgress.setText("Progress");

		GridData progressGrid = new GridData();
		progressGrid.grabExcessHorizontalSpace = true;
		progressGrid.horizontalAlignment = GridData.FILL;

		progress = new Text(container, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.READ_ONLY);
		progress.setLayoutData(progressGrid);

	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	// save content of the Text fields because they get disposed
	// as soon as the Dialog closes
	private void submitBuild() {

		Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

		//showMessage(String.format("Dockerizing %s", imageName.getText()));

		prefs.put("name", imageName.getText());
		prefs.put("port", "8080");
		prefs.put("adminPort", "9999");
		prefs.put("debugPort", "8787");

		// REST get CLI script from JCT and create docker image
		String cli = retrieveInitialCli(imageName.getText(), this.baseImage);

	}

	@Override
	protected void okPressed() {

		getButton(OK).setEnabled(false);
		
		submitBuild();
		super.okPressed();
	}

	private void showMessage(String message) {

		MessageDialog.openInformation(getShell(), "JSP View", message);
	}

	private String retrieveInitialCli(String imageName, String baseImage) {

		String cli = null;

		String path = "none";

		path = Platform.getLocation().toString() + "/.configuration/docker/customization";

		try {

//			FileWriter fw = new FileWriter(new File(path + File.separator + "commands.cli"));
//			fw.write(cli);
//			fw.close();

			RemoteExecutor.stopBoss();
			RemoteExecutor.listDockerPorts(imageName);
			Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

			String container = prefs.get("container", null);
			StringBuilder status = new StringBuilder();
			RemoteExecutor.pushDockerArtefacts(status, progress, this.getShell(), container, baseImage + ":latest");
			RemoteExecutor.createDockerImage(status, progress, this.getShell(), container);
			RemoteExecutor.runDockerImage(status, progress, this.getShell(), container);

		} catch (Exception se) {
			showMessage(se.getMessage());

			se.printStackTrace();
		}

		return cli;
	}

}
