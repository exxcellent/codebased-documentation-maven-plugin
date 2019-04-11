package filemanagement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.json.JSONException;
import org.json.JSONObject;

import collectors.MavenInfoCollector;

/**
 * Class for gathering and combining information files.
 * @author gmittmann
 *
 */
public class FileAggregator {

	private MavenSession session;
	private Log log;

	public FileAggregator(MavenSession session, Log log) {
		this.session = session;
		this.log = log;
	}

	/**
	 * Collects all information files of the projects and combines them to topic
	 * related files and one joined file with all information.
	 * 
	 * @param folderPath path to the folder, into which the new files shall be saved
	 * @param fileNameSuffix suffix to be appended to the file name of the topics
	 */
	public void aggregateFilesTo(File folderPath, String fileNameSuffix) {

		log.info("-- AGGREGATING FILES --");

		FileWriter out = new FileWriter(log);

		String mavenInfoPath = folderPath + "\\" + MavenInfoCollector.FILE_NAME + fileNameSuffix + ".json";
		log.info("MavenInfo in: " + mavenInfoPath);
		if (out.createFile(folderPath.getAbsolutePath(), MavenInfoCollector.FILE_NAME + fileNameSuffix)) {

			List<File> mavenInfoFiles = findFiles(MavenInfoCollector.FOLDER_NAME, MavenInfoCollector.FILE_NAME);
			List<JSONObject> jsonObjects = createJSONObjects(mavenInfoFiles);
			//TODO process JSONObjects

			out.finishFile();
		} else {
			log.error("There was an error creating the file: " + mavenInfoPath); // TODO: better handling
			out.finishFile();
		}

		// TODO: join information in one file
		
		

	}

	/**
	 * Searches for a certain file in the target folder of each project of this
	 * session and puts them in a list. If the file is in the target folder itself,
	 * set folderName to a dot [.].
	 * 
	 * @param folderName name of the folder, in which the file is located. Path
	 *                   relative to the target folder of the projects.
	 * @param fileName   name of the file (without document type).
	 * @return List of all files with the given name and folder location.
	 */
	private List<File> findFiles(String folderName, String fileName) {
		List<File> files = new ArrayList<>();

		for (MavenProject currentProject : session.getProjects()) {
			if (!currentProject.getPackaging().equalsIgnoreCase("pom")) {
				File infoFile = new File(
						currentProject.getBasedir() + "\\target\\" + folderName + "\\" + fileName + ".json");

				if (infoFile.exists() && infoFile.canRead()) {
					log.info("found file for: " + currentProject.getArtifactId());
					files.add(infoFile);

				} else {
					log.error("Module " + currentProject.getName() + " did not create a file named: " + fileName);
					log.error("Please check whether the PlugIn was run for this module.");
				}
			}
		}
		return files;
	}
	
	/**
	 * Creates JSONObjects (if possible) based on the files in the given list.
	 * @param files list of files, that are to be turned into JSONObjects.
	 * @return list with the created JSONObjects.
	 */
	private List<JSONObject> createJSONObjects(List<File> files) {
		List<JSONObject> objects = new ArrayList<>();
		
		for (File file : files) {
			try {
				InputStream in = new FileInputStream(file);
				String content = IOUtils.toString(in, "UTF-8");
				objects.add(new JSONObject(content));
			} catch (FileNotFoundException e) {
				log.error("FnF - Could not read file: " + file.getAbsolutePath());
			} catch (IOException e) {
				log.error("IO - Could not read file: " + file.getAbsolutePath());
			} catch (JSONException e) {
				log.error("JSON - file is not correct: " + file.getAbsolutePath());
			}
		}
		
		return objects;
	}

}
