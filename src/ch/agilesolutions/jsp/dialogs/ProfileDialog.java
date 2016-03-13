package ch.agilesolutions.jsp.dialogs;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
import org.glassfish.jersey.SslConfigurator;
import org.osgi.service.prefs.Preferences;

import ch.agilesolutions.jsp.model.Profile;
import ch.agilesolutions.jsp.utils.GsonMessageBodyHandler;
import ch.agilesolutions.jsp.utils.RemoteExecutor;

public class ProfileDialog extends TitleAreaDialog {

	private String profile;

	private List<Profile> profiles;
	
	private Text progress;

	private Map<String, Profile> profileMap = new HashMap<>();

	public ProfileDialog(Shell parentShell) {
		super(parentShell);

		this.profiles = retrieveJCTProfiles();

		for (Profile profile : profiles) {
			profileMap.put(String.format("%s@%s", profile.getName(),profile.getHostName()), profile);
		}

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
		setTitle("Select JCT Profile to be dockerized");

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

		c1.setBounds(50, 50, 450, 1400);

		List<String> names = new ArrayList<String>();

		for (String name : profileMap.keySet()) {
			names.add(name);
		}

		String items[] = new String[names.size()];

		items = names.toArray(items);

		c1.setItems(items);

		c1.setText(names.get(0));

		profile = names.get(0);

		c1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				profile = c1.getText();

			}
		});

		Label lbtProgress = new Label(container, SWT.NONE);
		lbtProgress.setText("Progress");

		GridData progressGrid = new GridData();
		progressGrid.grabExcessHorizontalSpace = true;
		progressGrid.horizontalAlignment = GridData.FILL;

		progress = new Text(container, SWT.MULTI | SWT.BORDER | 
						SWT.WRAP | SWT.READ_ONLY); 
		progress.setLayoutData(progressGrid);

	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	// save content of the Text fields because they get disposed
	// as soon as the Dialog closes
	private void saveInput() {

		Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");
		
		Profile pr = profileMap.get(profile);

		showMessage(String.format("Dockerizing %s", pr.getDescription()));
		
		prefs.put("id", Integer.toString(pr.getId()));
		prefs.put("name", pr.getName());
		prefs.put("port", "8080");
		prefs.put("adminPort", "9999");
		prefs.put("debugPort", "8787");
		prefs.put("jbar", pr.getJbarName());
		prefs.put("image", System.getProperty("user.name") + "/" +  profileMap.get(profile).getName() + ":" + "8080");
		

		// REST get CLI script from JCT and create docker image
		String cli = retrieveInitialCli(profileMap.get(profile).getId(), profileMap.get(profile).getName());
		

	}

	@Override
	protected void okPressed() {
		saveInput();
		super.okPressed();
	}

	private void showMessage(String message) {

		MessageDialog.openInformation(getShell(), "JSP View", message);
	}

	private List<Profile> retrieveJCTProfiles() {

		List<Profile> profiles = Collections.emptyList();

		List<Profile> filteredProfiles = new ArrayList<>();

		Client client;

		SslConfigurator sslConfig = SslConfigurator.newInstance()
		                .trustStoreFile(Platform.getLocation().toString() + "/.configuration/keystore.jks");

		SSLContext sslContext = sslConfig.createSSLContext();

		client = ClientBuilder.newBuilder().sslContext(sslContext).register(GsonMessageBodyHandler.class).build();

		Response response = client.target("https://jct-uat/rest/ara/allProfiles").request().accept(MediaType.APPLICATION_JSON).get();

		if (response.getStatus() == 200) {
			profiles = response.readEntity(new GenericType<List<Profile>>() {
			});
		}

		for (Profile profile : profiles) {
			if (profile.getEnvironment().equalsIgnoreCase("SIT")) {
				filteredProfiles.add(profile);
			}

		}

		return filteredProfiles;

	}

	private String retrieveInitialCli(int id, String name) {

		String cli = null;

		Client client;

		SslConfigurator sslConfig = SslConfigurator.newInstance()
		                .trustStoreFile(Platform.getLocation().toString() + "/.configuration/keystore.jks");

		SSLContext sslContext = sslConfig.createSSLContext();

		client = ClientBuilder.newBuilder().sslContext(sslContext).register(GsonMessageBodyHandler.class).build();

		Response response = client.target(String.format("https://jct-uat/rest/profile/cli/%s", id)).request().accept(MediaType.TEXT_PLAIN)
		                .get();

		if (response.getStatus() == 200) {
			cli = response.readEntity(String.class);

			String path = "none";

			path = Platform.getLocation().toString() + "/.configuration/docker/customization";

			try {

				FileWriter fw = new FileWriter(new File(path + File.separator + "commands.cli"));
				fw.write(cli);
				fw.close();
				
				RemoteExecutor.listDockerPorts();
				
				StringBuilder status = new StringBuilder();
				RemoteExecutor.stopBoss();
				RemoteExecutor.pushDockerArtefacts(status, progress,this.getShell(), name);
				RemoteExecutor.createDockerImage(status, progress, this.getShell(), name);
				RemoteExecutor.runDockerImage(status, progress, this.getShell(), name);

			} catch (Exception se) {
				showMessage(se.getMessage());

				se.printStackTrace();
			}

		}

		return cli;

	}

}
