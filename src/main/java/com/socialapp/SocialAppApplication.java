package com.socialapp;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SocialAppApplication {

	public static void main(String[] args) {

		// Load .env
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing()
				.load();

		// Neo4j
		System.setProperty("NEO4J_URI", dotenv.get("NEO4J_URI"));
		System.setProperty("NEO4J_USERNAME", dotenv.get("NEO4J_USERNAME"));
		System.setProperty("NEO4J_PASSWORD", dotenv.get("NEO4J_PASSWORD"));

		// Mail
		System.setProperty("MAIL_HOST", dotenv.get("MAIL_HOST"));
		System.setProperty("MAIL_PORT", dotenv.get("MAIL_PORT"));
		System.setProperty("MAIL_USERNAME", dotenv.get("MAIL_USERNAME"));
		System.setProperty("MAIL_PASSWORD", dotenv.get("MAIL_PASSWORD"));

		// JWT
		System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
		// MinIO
		System.setProperty("MINIO_URL", dotenv.get("MINIO_URL"));
		System.setProperty("MINIO_ACCESS_KEY", dotenv.get("MINIO_ACCESS_KEY"));
		System.setProperty("MINIO_SECRET_KEY", dotenv.get("MINIO_SECRET_KEY"));
		System.setProperty("MINIO_BUCKET", dotenv.get("MINIO_BUCKET"));

		System.setProperty("STRINGEE_API_KEY_SID", dotenv.get("STRINGEE_API_KEY_SID"));
		System.setProperty("STRINGEE_API_KEY_SECRET", dotenv.get("STRINGEE_API_KEY_SECRET"));



		SpringApplication.run(SocialAppApplication.class, args);
	}
}