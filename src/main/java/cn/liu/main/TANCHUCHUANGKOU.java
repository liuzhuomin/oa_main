package cn.liu.main;

import org.eclipse.swt.*;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class TANCHUCHUANGKOU {

	static final int BROWSER_STYLE = SWT.NONE;

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("Main Window");
		shell.setLayout(new FillLayout());
		final Browser browser;
		try {
			browser = new Browser(shell, BROWSER_STYLE);
		} catch (SWTError e) {
			System.out.println("Could not instantiate Browser: " + e.getMessage());
			display.dispose();
			return;
		}
		initialize(display, browser);
		shell.open();
		browser.setUrl("http://www.w3schools.com/jsref/tryit.asp?filename=tryjsref_win_open");
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	/* register WindowEvent listeners */
	static void initialize(final Display display, Browser browser) {
		browser.addOpenWindowListener(event -> {
			if (!event.required)
				return; /* only do it if necessary */
			Shell shell = new Shell(display);
			shell.setText("New Window");
			shell.setLayout(new FillLayout());
			Browser browser1 = new Browser(shell, BROWSER_STYLE);
			initialize(display, browser1);
			event.browser = browser1;
		});
		browser.addVisibilityWindowListener(new VisibilityWindowListener() {
			@Override
			public void hide(WindowEvent event) {
				Browser browser = (Browser) event.widget;
				Shell shell = browser.getShell();
				shell.setVisible(false);
			}

			@Override
			public void show(WindowEvent event) {
				Browser browser = (Browser) event.widget;
				final Shell shell = browser.getShell();
				if (event.location != null)
					shell.setLocation(event.location);
				if (event.size != null) {
					Point size = event.size;
					shell.setSize(shell.computeSize(size.x, size.y));
				}
				shell.open();
			}
		});
		browser.addCloseWindowListener(event -> {
			Browser browser1 = (Browser) event.widget;
			Shell shell = browser1.getShell();
			shell.close();
		});
	}
}
