package ch.agilesolutions.jsp.views;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.prefs.Preferences;

import ch.agilesolutions.jsp.listeners.LogFileListener;
import ch.agilesolutions.jsp.watchers.LogFileWatcher;

/**
 * 
 * http://www.vogella.com/tutorials/EclipsePlugIn/article.html
 *
 * @author u24279
 * @version $Revision$, $Date$
 */
public class LogFileView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "jsp.views.JSPView";

	public static final String VERSION = "1.0.0";

	private TextViewer viewer;
	
	/**
	 * The constructor.
	 */

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


	
	

	@Override
    public void init(IViewSite site) throws PartInitException {
	    // TODO Auto-generated method stub
	    super.init(site);
	    
	    Preferences prefs = InstanceScope.INSTANCE.getNode("jsp");

		String prefix = prefs.get("tailfile", null);
		
		setPartName(prefix);

	}



	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {

		viewer = new TextViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		// viewer.setDocument(newDoc);
		viewer.setEditable(true);
		final StyledText styledText = viewer.getTextWidget();
		styledText.setWordWrap(true);
		styledText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent event) {
				if (event.keyCode == 'p' && (event.stateMask & SWT.CTRL) != 0) {
					styledText.print();
				}
			}
		});

		final Document newDoc = new Document();
		viewer.setDocument(newDoc);
		viewer.getTextWidget().cut();
		viewer.getTextWidget().append("initializing log");

		viewer.setInput(getViewSite());

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "logfile.viewer");
		final StyledText text = viewer.getTextWidget();

		LogFileWatcher watcher = new LogFileWatcher();

		final Display display = Display.getCurrent();
		watcher.addListener(new LogFileListener() {

			public void update(final StringBuilder in) {

				display.asyncExec(new Runnable() {
					public void run() {

						// text.selectAll();
						// text.cut();

						if (text.getCharCount() > 20 && in.length() > 20) {
							if (!text.getTextRange(text.getCharCount() - 20, 20).equals(
							                in.toString().substring(in.toString().length() - 20))) {
								text.replaceTextRange(0, text.getCharCount(), in.toString());
							}

						} else {
							text.replaceTextRange(0, text.getCharCount(), in.toString());
						}

						// text.append(in.toString());

						viewer.setTopIndex(newDoc.getNumberOfLines());

					}
				});

			}
		});

		watcher.start();

	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}



}
