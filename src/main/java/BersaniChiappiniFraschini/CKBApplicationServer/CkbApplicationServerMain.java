package BersaniChiappiniFraschini.CKBApplicationServer;

import BersaniChiappiniFraschini.CKBApplicationServer.config.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CkbApplicationServerMain {
/*	@Autowired
	private JwtService jwtService;*/
	public static void main(String[] args) {
		SpringApplication.run(CkbApplicationServerMain.class, args);
	}

	/*@Bean
	public CommandLineRunner runner(){
		return args->{
			var token = jwtService.generateJWT("65ba712ad1ef8e08243ad34f");
			System.out.println("New token with id group: "+token);
		};
	}*/

}
