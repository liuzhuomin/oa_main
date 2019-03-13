package cn.liu.listener;

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.springframework.beans.factory.annotation.Autowired;

import cn.liu.service.LoginInterFace;
import cn.liu.statics.KeyCode;

public class MyKeyListener implements KeyListener {

	@Autowired
	LoginInterFace	loginInterFace;
	
	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.keyCode;
		if(keyCode==KeyCode.ENTER) {
			loginInterFace.login();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

}
