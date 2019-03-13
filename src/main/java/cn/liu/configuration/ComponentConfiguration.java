package cn.liu.configuration;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.liu.annotions.ListenerAnnotion;
import cn.liu.listener.MyMouseListener;
import cn.liu.statics.ComponentText;
import cn.liu.statics.WindowMessage;
import cn.liu.statics.WindowStatics;

@Configuration
@ListenerAnnotion()
public class ComponentConfiguration {

	@Autowired
	Shell shell;

	@Autowired
	KeyListener keyListener;

	@Autowired
	Composite loginComposite;

	@Autowired
	MouseListener mouseListener;

	@Autowired
	Display loginDisplay;

	/**
	 * create shell bean;
	 * 
	 * @return
	 */
	@Bean
	public org.eclipse.swt.widgets.Shell shell() {
		GraphicsEnvironment localGraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle maximumWindowBounds = localGraphicsEnvironment.getMaximumWindowBounds();
		int x = (int) ((maximumWindowBounds.getWidth() - WindowStatics.LOGIN_WINDOW_WIDTH) / 2);
		int y = (int) ((maximumWindowBounds.getHeight() - WindowStatics.LOGIN_WINDOW_HEIGHT) / 2);
		Shell shell = new Shell();
		shell.setMaximized(true);
		shell.setImage(SWTResourceManager.getImage("C:\\Users\\liuliuliu\\Pictures\\\u9ED1\u77F3\u5C71.jpg"));
		shell.setText(WindowStatics.PROJECT_NAME);
		shell.setLayout(new FillLayout());
		shell.setBounds(x, y, WindowStatics.LOGIN_WINDOW_WIDTH, WindowStatics.LOGIN_WINDOW_HEIGHT);
		return shell;
	}

	@Bean
	public Display loginDisplay() {
		return Display.getDefault();
	}

	/**
	 * <li>登录页面的组件框
	 * @return
	 */
	@Bean
	public Composite loginComposite() {
		Composite loginComposite = new Composite(shell, SWT.NONE);
		return loginComposite;
	}

	@Bean
	public org.eclipse.swt.widgets.Combo loginCombo() {
		Combo combo = new Combo(loginComposite, SWT.NONE);
		combo.setBounds(68, 28, 204, 25);
		return combo;
	}

	@Bean
	public org.eclipse.swt.widgets.Combo lo2() {
		Combo combo = new Combo(loginComposite, SWT.NONE);
		combo.setBounds(68, 28, 204, 25);
		return combo;
	}

	@Bean
	public MessageBox loginDialog() {
		MessageBox loginDialog = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
		loginDialog.setText(WindowMessage.WARNNING);
		return loginDialog;
	}

	@Bean
	public Text passWordText() {
		Text passWordText = new Text(loginComposite, SWT.BORDER);
		passWordText.setBounds(68, 59, 204, 25);
		passWordText.setEchoChar('*');
		return passWordText;
	}

	@Bean
	public Button remenberPwdCK() {
		Button remenberPwdCK = new Button(loginComposite, SWT.CHECK);
		remenberPwdCK.setText(WindowStatics.remenberPwdCK);
		remenberPwdCK.setBounds(68, 90, 98, 17);
		return remenberPwdCK;
	}

	@Bean
	public Button autoLoginCk() {
		Button autoLoginCk = new Button(loginComposite, SWT.CHECK);
		autoLoginCk.setText(WindowStatics.autoLoginCk);
		autoLoginCk.setBounds(68 + 98, 90, 98, 17);
		return autoLoginCk;
	}

	
	@Bean
	@ListenerAnnotion(MouseListener.class)
	public Button loginBtn() {
		Button loginBtn = new Button(loginComposite, SWT.TOGGLE);
		loginBtn.setBounds(33, 113, 239, 25);
		loginBtn.setText(ComponentText.LOGIN_BTN);
//		loginBtn.addMouseListener(mouseListener);
		loginBtn.addSelectionListener(widgetSelectedAdapter(e -> {
			Runnable longJob = new Runnable() {
				boolean done = false;

				@Override
				public void run() {
					Thread thread = new Thread(() -> {
						done = true;
						loginDisplay.wake();
					});
					thread.start();
					while (!done && !shell.isDisposed()) {
						if (!loginDisplay.readAndDispatch())
							loginDisplay.sleep();
					}
				}
			};
			BusyIndicator.showWhile(loginDisplay, longJob);
		}));
		return loginBtn;
	}

	@Bean
	public Label loginLabel() {
		Label loginLabel = new Label(loginComposite, SWT.NONE);
		loginLabel.setBounds(33, 31, 27, 25);
		loginLabel.setText(WindowStatics.loginLabel);
		return loginLabel;
	}
	
	@Bean
	public Label pwdLabel() {
		Label pwdLabel = new Label(loginComposite, SWT.NONE);
		pwdLabel.setText(WindowStatics.pwdLabel);
		pwdLabel.setBounds(33, 62, 27, 17);
		return pwdLabel;
	}
	
	
}
