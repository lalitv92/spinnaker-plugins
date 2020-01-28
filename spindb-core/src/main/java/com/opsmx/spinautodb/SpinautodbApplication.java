package com.opsmx.spinautodb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SpinautodbApplication {

	public static void main(String[] args) {

		ConfigurableApplicationContext ctx = SpringApplication.run(SpinautodbApplication.class, args); 
		ctx.close();
		//int exitValue = SpringApplication.exit(ctx);
		//System.out.println("exitValue ... " + exitValue);
		//System.exit(exitValue);

		/*
		 * try { SpringApplication.run(SpinautodbApplication.class, args); } catch
		 * (Exception e) { System.out.println("execptions ... 11212" + e);
		 * e.printStackTrace(); }
		 */

		 //System.exit(SpringApplication.exit(SpringApplication.run(SpinautodbApplication.class, args)));
	}

}
