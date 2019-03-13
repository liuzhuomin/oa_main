package cn.liu.listener;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Widget;
import org.springframework.beans.factory.annotation.Autowired;

import cn.liu.service.LoginInterFace;
import cn.liu.statics.ComponentText;


public class MyMouseListener implements org.eclipse.swt.events.MouseListener{
		
		@Autowired
		LoginInterFace	loginInterFace;
	
		@Override
		public void mouseDoubleClick(MouseEvent arg0) {
			// TODO Auto-generated method stub
		}
		
		@Override
		public void mouseDown(MouseEvent e) {
			Widget widget = e.widget;
			if(widget instanceof org.eclipse.swt.widgets.Button) {		
				org.eclipse.swt.widgets.Button button =(Button) widget;
				if(ComponentText.LOGIN_BTN.equals(button.getText())) {	
					loginInterFace.login();
				}
			}
			
		}

		
		@Override
		public void mouseUp(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

}
