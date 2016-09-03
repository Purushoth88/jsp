package ch.agilesolutions.jsp.utils;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public class TestGIT {

	private static final String REMOTE_URL = "http://github.agile-solutions.ch/scm/jct/idsstore.git";

	public static void main(String[] args) {
		new TestGIT().connect();

	}

	private void connect() {

		Git git = null;

		File theDir = new File("Y:/jbossdev/workspaces/test");

		try {
			git = Git.cloneRepository().setURI(REMOTE_URL)
			                .setDirectory(theDir)
			                .call();

			// set starting point of new branch to tag name.
			git.branchCreate().setName("test").call();

			// checkout new local branch.
			git.checkout().setName("test").call();

			git.add().addFilepattern(".").call();

			git.commit().setCommitter("info", "info@agile-solutions.ch").setMessage("Initial create JBoss configuration").call();

			git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider("info", "info")).call();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
