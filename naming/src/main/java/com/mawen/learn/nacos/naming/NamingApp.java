package com.mawen.learn.nacos.naming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/22
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
public class NamingApp {

	public static void main(String[] args) {
		SpringApplication.run(NamingApp.class, args);
	}
}
