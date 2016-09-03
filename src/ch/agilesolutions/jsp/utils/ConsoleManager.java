package ch.agilesolutions.jsp.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;

public class ConsoleManager {

	public static void createConsole() {

		// https://wiki.eclipse.org/FAQ_How_do_I_write_to_the_console_from_a_plug-in%3F

		String name = "linux";

		IOConsole myConsole = null;

		ConsolePlugin plugin = ConsolePlugin.getDefault();

		IConsoleManager conMan = plugin.getConsoleManager();

		// no console found, so create a new one
		myConsole = new IOConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		String id = IConsoleConstants.ID_CONSOLE_VIEW;
		IConsoleView view = null;
		try {
			view = (IConsoleView) page.showView(id);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		view.display(myConsole);

		IOConsoleOutputStream out = myConsole.newOutputStream();

		String result = "";

		try {

			out.write("Hello from Generic console sample action");

			IOConsoleInputStream inputStream = myConsole.getInputStream();

			inputStream.setColor(new Color(null, 200, 30, 240));

			BufferedReader bufferedReader = null;

			if (bufferedReader == null) {

				bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

				result = bufferedReader.readLine();

			}
			out.write(result);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
