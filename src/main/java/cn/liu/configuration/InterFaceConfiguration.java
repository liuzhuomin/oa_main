package cn.liu.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.liu.service.LoginInterFace;
import cn.liu.service.impl.LoginInterFaceImpl;

@Configuration
public class InterFaceConfiguration {
	
	@Bean
	public LoginInterFace loginInterFace() {
		return new LoginInterFaceImpl();
	}
}
