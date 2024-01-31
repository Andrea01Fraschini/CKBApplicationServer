package BersaniChiappiniFraschini.CKBApplicationServer;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;


@SpringBootApplication
@RequiredArgsConstructor
public class CkbApplicationServerMain{

	final ApplicationContext context;

	public static void main(String[] args) {
		SpringApplication.run(CkbApplicationServerMain.class, args);
	}

}
