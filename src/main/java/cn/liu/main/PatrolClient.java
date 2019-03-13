package cn.liu.main;

import java.io.File;

import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.springframework.beans.factory.annotation.Autowired;

public class PatrolClient {

	@Autowired
	Shell shell;

	@Autowired
	Display loginDisplay;
	

	/**
	 * Open the window.
	 * 
	 * @wbp.parser.entryPoint
	 */
	public void open() {
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!loginDisplay.readAndDispatch()) {
				loginDisplay.sleep();
			}
		}
		loginDisplay.dispose();
	}


	/**
	 * <li>添加监听器
	 */
	protected void addListener( ) {

//		GraphicsEnvironment localGraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
//		Rectangle maximumWindowBounds = localGraphicsEnvironment.getMaximumWindowBounds();
//		int x=(int)((maximumWindowBounds.getWidth()-WindowStatics.LOGIN_WINDOW_WIDTH)/2);
//		int y=(int)((maximumWindowBounds.getHeight()-WindowStatics.LOGIN_WINDOW_HEIGHT)/2);
//		shell = new Shell();
//		shell.setMaximized(true);
//		shell.setImage(SWTResourceManager.getImage("C:\\Users\\liuliuliu\\Pictures\\\u9ED1\u77F3\u5C71.jpg"));
//		shell.setText("OA");
//		shell.setLayout(new FillLayout());
//		shell.setBounds(x, y, WindowStatics.LOGIN_WINDOW_WIDTH, WindowStatics.LOGIN_WINDOW_HEIGHT);

//		loginComposite = new Composite(shell, SWT.NONE);
//		loginComposite.addKeyListener(keyListener);

//		combo = new Combo(loginComposite, SWT.NONE);
//		loginCombo.setBounds(68, 28,  204, 25);

//		passWordText = new Text(loginComposite, SWT.BORDER);
//		passWordText.setBounds(68, 59, 204, 25);
//		passWordText.setEchoChar('*');

//		loginBtn = new Button(loginComposite, SWT.TOGGLE);
//		loginBtn.setBounds(33, 113, 239, 25);
//		loginBtn.setText(ComponentText.LOGIN_BTN);
//		loginBtn.addMouseListener(mouseListener);
//		loginBtn.addSelectionListener(widgetSelectedAdapter(e-> {
//			Runnable longJob = new Runnable() {
//				boolean done = false;
//				@Override
//				public void run() {
//					Thread thread = new Thread(() -> {
//						done = true;
//						display.wake();
//					});
//					thread.start();
//					while (!done && !shell.isDisposed()) {
//						if (!display.readAndDispatch())
//							display.sleep();
//					}
//				}
//			};
//			BusyIndicator.showWhile(display, longJob);
//		}));

//		Label loginLabel = new Label(loginComposite, SWT.NONE);
//		loginLabel.setBounds(33, 31, 27, 25);
//		loginLabel.setText("账号:");
//		
//		Label pwdLabel = new Label(loginComposite, SWT.NONE);
//		pwdLabel.setText("密码:");
//		pwdLabel.setBounds(33, 62, 27,17);

//		remenberPwdCK = new Button(loginComposite, SWT.CHECK);
//		remenberPwdCK.setText("记住密码");
//		remenberPwdCK.setBounds(68, 90, 98, 17);

//		autoLoginCk = new Button(loginComposite, SWT.CHECK);
//		autoLoginCk.setText("自动登陆");
//		autoLoginCk.setBounds(68+98, 90, 98, 17);

	}

	/**
	 * 获取文件的真实路径
	 * 
	 * @param rootPath
	 * @return
	 */
	public final String getRealPath(String rootPath) {
		String path = this.getClass().getClassLoader().getResource("").getPath().replace("target/classes/",
				"src/main/resources/") + rootPath;
		return new File(path).getAbsolutePath();
	}

}
