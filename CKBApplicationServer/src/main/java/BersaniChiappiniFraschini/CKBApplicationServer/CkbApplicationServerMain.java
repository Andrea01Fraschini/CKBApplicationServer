package BersaniChiappiniFraschini.CKBApplicationServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CkbApplicationServerMain {

	public static void main(String[] args) {
		SpringApplication.run(CkbApplicationServerMain.class, args);
	}

	/*@Bean
	CommandLineRunner runner(UserRepository repository){
		return args ->{
*//*			User marco = User.builder()
					.username("MarkolinoXx")
					.accountType(AccountType.EDUCATOR)
					.email("marco.sgrodoli@gmail.com")
					.build();

			User giovanna = User.builder()
					.username("GiovannonaCosciaLunga88")
					.accountType(AccountType.STUDENT)
					.email("concetta.esposito@gmail.com")
					.build();
			repository.insert(List.of(marco, giovanna));*//*
			System.out.println(repository.findUserByEmail("marco.sgrodoli@gmail.com"));
		};
	}*/
}
