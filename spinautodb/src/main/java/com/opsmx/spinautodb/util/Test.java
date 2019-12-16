package com.opsmx.spinautodb.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.AbortedByHookException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public class Test {

	public static void main(String[] args) throws NoHeadException, NoMessageException, UnmergedPathsException, ConcurrentRefUpdateException, WrongRepositoryStateException, AbortedByHookException, GitAPIException, IOException, URISyntaxException {
		
		
//		// Creation of a temp folder that will contain the Git repository
//		File workingDirectory = File.createTempFile("nuxeo-git-test", "");
//		//workingDirectory.delete();
//		workingDirectory.mkdirs();

		// Create a Repository object
//		Repository repo = FileRepositoryBuilder.create(new File(workingDirectory, ".git"));
//		repo.create();
//		Git git = new Git(repo);
//		
//
//		// Create a new file and add it to the index
//		File newFile = new File(workingDirectory, "myNewFile");
//		newFile.createNewFile();
//		git.add().addFilepattern("myNewFile").call();
//
//		// Now, we do the commit with a message
//		RevCommit rev = git.commit().setAuthor("gildas", "gildas@example.com").setMessage("My first commit").call();
//		 TODO Auto-generated method stu
		
		String httpUrl =  "https://github.com/lalitv92/configureAWScli.git";
		Repository localRepo = new FileRepository("/home/opsmx/Desktop/check/configureAWScli_check3");

		Git git = new Git(localRepo); 
		Git.cloneRepository()
                .setURI(httpUrl)
                .setDirectory(new File("/home/opsmx/Desktop/check/configureAWScli_check3"))
                .call();
	
		
	    
//	    Repository localRepo = new FileRepository(localPath);
//	    this.git = new Git(localRepo);        
//	    localRepo.create();  
//		File checkFile = new File("/home/opsmx/Desktop/check/configureAWScli_check1/");
//		Git repo = Git.open(checkFile);
        File myfile = new File(localRepo.getDirectory()/*.getParent()*/, "testfile");
        if (!myfile.createNewFile()) {
            throw new IOException("Could not create file " + myfile);
        }
//        log.info("file created at{}", myfile.getPath());
//        Git git = Git.init().setDirectory( directory ).call();
        git.add().addFilepattern(myfile.getName()).call();
       
	    git.commit().setMessage("test message").call();
	    

	    // add remote repo:
	    RemoteAddCommand remoteAddCommand = git.remoteAdd();
	    remoteAddCommand.setName("origin");
	    remoteAddCommand.setUri(new URIish(httpUrl));
	    // you can add more settings here if needed
	    remoteAddCommand.call();

	    // push to remote:
	    PushCommand pushCommand = git.push();
	    pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider("lalitv92", "4a0396c02249c6487e898db6bf82e6f0d56fca5c"));
	    // you can add more settings here if needed
	    pushCommand.call();
		

	}
	
	public static Repository openRepository() throws IOException {
	    FileRepositoryBuilder builder = new FileRepositoryBuilder();

	    Repository repository = builder.setGitDir(new File("/home/opsmx/Desktop/check/"))
	            .readEnvironment() // scan environment GIT_* variables
	            .findGitDir() // scan up the file system tree
	            .build();
//	     Object log;
//		((Object) log).info("Repository directory is {}", repository.getDirectory());

	    return repository;
	}

}
