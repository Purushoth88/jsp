package ch.agilesolutions.jsp.views;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
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
import org.eclipse.ui.part.ViewPart;

public class ConsoleView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "jsp.views.ConsoleView";

	private TextViewer viewer;
	private Action action1;
	private Action action2;
	private Action action3;
	private Action action4;
	private Action action5;
	private Action action6;
	private Action action7;
	private Action action8;

	/*
	 * The content provider class is responsible for providing objects to the view. It can wrap existing objects in adapters or simply
	 * return objects as-is. These objects may be sensitive to the current input of the view, or ignore it and always show the same content
	 * (like Task List, for example).
	 */

	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			return new String[] { "One", "Two", "Three" };
		}
	}

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}

		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		public Image getImage(Object obj) {
			
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public ConsoleView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		

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

	@Override
    public void setFocus() {
	    // TODO Auto-generated method stub
	    
    }

}
