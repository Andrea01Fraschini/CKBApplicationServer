package BersaniChiappiniFraschini.CKBApplicationServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;


@SpringBootApplication
public class CkbApplicationServerMain{

	public static void main(String[] args) {
		SpringApplication.run(CkbApplicationServerMain.class, args);
	}

}
