package creators;

import java.io.File;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

public class FileAggregator {

	private MavenProject project;
	private Log log;

	public FileAggregator(MavenProject project, Log log) {
		this.project = project;
		this.log = log;
	}

	public void aggregateFilesTo(File path, String fileName) {

		log.info("WILL DO SOMETHING, SOON");

		FileWriter out = new FileWriter(log);
		log.info(path + "\\" + fileName);
		if (out.createFile(path + "\\" + fileName)) {
			out.writeIntoFile("tada");
			out.finishFile();
		} else {
			log.error("File already exists. Please delete the old file first"); //TODO: better handling
		}

		// TODO: positioning of the file into target of main pom or plugin defined
		// location
//		String filePath = path;
//		if(!project.isExecutionRoot()) {
//			file
//		} 
//		File file = new File(path);
//		if ( !file.isAbsolute() )
//		{
//		    file = new File( project.getBasedir(), path );
//		}
//		
//		log.info("put file to: " + file.getAbsolutePath());

		// TODO: check if all modules created a file -> warn if there is one, that did
		// not
		// TODO: put all information in one file

	}

}
