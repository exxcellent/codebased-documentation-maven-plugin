package filemanagement;

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

		log.info("--AGGREGATING FILES--");

		FileWriter out = new FileWriter(log);
		log.info(folderPath + "\\" + MavenInfoCollector.FILE_NAME + fileNameSuffix);
		if (out.createFile(folderPath.getAbsolutePath(), MavenInfoCollector.FILE_NAME + fileNameSuffix)) {
			out.writeTextIntoFile("mmm");
			out.finishFile();
		} else {
			log.error("File already exists. Please delete the old file first"); //TODO: better handling
		}

		// TODO: check if all modules created a file -> warn if there is one, that did
		// not
		// TODO: put all information in one file

	}

}
