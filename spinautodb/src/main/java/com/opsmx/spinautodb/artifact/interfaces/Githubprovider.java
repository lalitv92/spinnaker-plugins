package com.opsmx.spinautodb.artifact.interfaces;

import java.io.File;
import java.nio.file.Paths;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Githubprovider {

	// File scriptDirFile = new File(opsmxDirFile.getPath() + separator + "dbscript");
	private static final Logger log = LoggerFactory.getLogger(Githubprovider.class);

	public static File getdbscriptpath(String username, String password, String uri) {

		String githubrepo = uri.split("//")[0];
		String githubreposcriptpath = uri.split("//")[1];

		// String repoUrl = "https://github.com/lalitv92/TerraformPlansModule.git";
		String repoUrl = "https://github.com/" + username + "/" + githubrepo;
		String cloneDirectoryPath = "/home/spinautodb/opsmx/dbmigration/script/";
		String sqlscriptPath = cloneDirectoryPath + githubreposcriptpath;

		try {
			log.info("Cloning " + repoUrl + " into " + repoUrl);
			Git.cloneRepository().setURI(repoUrl).setDirectory(Paths.get(cloneDirectoryPath).toFile())
					.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password)).call();
			log.info("Completed Cloning");
		} catch (GitAPIException e) {
			log.info("Exception occurred while cloning repo");
			e.printStackTrace();
		}

		return new File(sqlscriptPath);
	}

	public static void main(String[] args) {

		String abc = "TerraformPlansModule.git//Namespace/sdbc.sql";

		System.out.println("first part :: " + abc.split("//")[0] + " and sceond part :: " + abc.split("//")[1]);
	}

}
