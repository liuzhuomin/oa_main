package cn.liu.service.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jarvis.base.util.HttpUtils;
import com.jarvis.base.util.MD5Util;

import cn.liu.service.LoginInterFace;
import cn.liu.statics.WindowStatics;

@Service
public class LoginInterFaceImpl implements LoginInterFace{
	
	@Autowired
	Combo loginCombo;
	
	@Autowired
	MessageBox loginDialog;
	
	@Autowired
	Text passWordText;
	
	@Autowired
	Button remenberPwdCK;

	@Override
	public void login() {
		String username = loginCombo.getText();
		String pwd = passWordText.getText();
		if("".equals(username)) {
			loginDialog.setMessage("账号不能为空!");
			loginDialog.open();
		    return;
		}
		if("".equals(pwd)) {
			loginDialog.setMessage("密码不能为空!");
			loginDialog.open();
		     return;
		}
		Map<String,String> m=new HashMap<>();
		m.put("userName",username);
		m.put("password", pwd);
		String sendGet = HttpUtils.sendPost("http://47.92.126.239:8080/user/checkLogin", m);
		System.out.println(sendGet);
		if("登录成功".equals(sendGet)) {
			String filePath=System.getProperty("user.home");
			File file =new File(filePath+File.separatorChar+WindowStatics.ROOT_DIR+File.separatorChar+ loginCombo.getText());
			if(!file.exists()) {
				file.mkdirs();
			}
			if(file!=null) {
				File propertiesFile = new File(file.getAbsolutePath()+File.separator+"user.properties");
				if(propertiesFile!=null) {
					if(!propertiesFile.exists()) {
						try {
							propertiesFile.createNewFile();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					Properties properties = new Properties();
					try {
						InputStream s=	new BufferedInputStream(new FileInputStream(propertiesFile.getAbsolutePath()));
						properties.load(s);
						properties.put("user.un", MD5Util.convertMD5(loginCombo.getText()));
						boolean isCheck =  remenberPwdCK.getSelection();
						if(isCheck) {
							properties.put("user.pd",MD5Util.md5(passWordText.getText()));
						}
						OutputStream out = new FileOutputStream(propertiesFile.getAbsolutePath());
						properties.store(out, "add user list");
						out.flush();
						out.close();
						s.close();
						Properties properties2 = new Properties();
						properties2.load(new BufferedInputStream(new FileInputStream(propertiesFile.getAbsolutePath())));
					} catch (Exception e1) {
						
					}
				}
				
			}
		}
		
	}
}
