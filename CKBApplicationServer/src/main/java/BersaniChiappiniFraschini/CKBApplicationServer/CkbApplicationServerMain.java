package BersaniChiappiniFraschini.CKBApplicationServer;

import BersaniChiappiniFraschini.CKBApplicationServer.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CkbApplicationServerMain {

	public static void main(String[] args) {
		SpringApplication.run(CkbApplicationServerMain.class, args);
	}

	@Bean
	CommandLineRunner runner(UserRepository repository){
		return args ->{
/*			User marco = User.builder()
					.username("MarkolinoXx")
					.accountType(AccountType.EDUCATOR)
					.email("marco.sgrodoli@gmail.com")
					.build();

			User giovanna = User.builder()
					.username("GiovannonaCosciaLunga88")
					.accountType(AccountType.STUDENT)
					.email("concetta.esposito@gmail.com")
					.build();
			repository.insert(List.of(marco, giovanna));*/
			System.out.println(repository.findUserByEmail("marco.sgrodoli@gmail.com"));
		};
	}
}
