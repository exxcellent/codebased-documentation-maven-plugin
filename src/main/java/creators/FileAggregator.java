package creators;

import java.io.File;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import collectors.MavenInfoCollector;

public class FileAggregator {

	private MavenProject project;
	private Log log;

	public FileAggregator(MavenProject project, Log log) {
		this.project = project;
		this.log = log;
	}

	public void aggregateFilesTo(File folderPath, String fileNameSuffix) {

		log.info("WILL DO SOMETHING, SOON");

		FileWriter out = new FileWriter(log);
		log.info(folderPath + "\\" + MavenInfoCollector.FILE_NAME.substring(0, MavenInfoCollector.FILE_NAME.length() - 4) + fileNameSuffix);
		if (out.createFile(folderPath.getAbsolutePath(), MavenInfoCollector.FILE_NAME.substring(0, MavenInfoCollector.FILE_NAME.length() - 4) + fileNameSuffix)) {
			out.writeIntoFile("tada");
			out.finishFile();
		} else {
			log.error("File already exists. Please delete the old file first"); //TODO: better handling
		}

		// TODO: check if all modules created a file -> warn if there is one, that did
		// not
		// TODO: put all information in one file

	}

}
