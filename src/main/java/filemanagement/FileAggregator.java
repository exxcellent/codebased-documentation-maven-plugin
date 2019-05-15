package filemanagement;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import collectors.ModuleInfoCollector;
import collectors.models.CollectedMavenInfoObject;
import collectors.models.InfoObject;
import collectors.models.ModuleInfoObject;

/**
 * Class for gathering and combining information files.
 * 
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
	 * @param folderPath     path to the folder, into which the new files shall be
	 *                       saved
	 * @param fileNameSuffix suffix to be appended to the file name of the topics
	 */
	public void aggregateFilesTo(File folderPath, String fileNameSuffix) {

		String projectName = findProjectName();

		log.info("    - MAVEN FILES - ");
		List<File> mavenInfoFiles = findFiles(ModuleInfoCollector.FOLDER_NAME, ModuleInfoCollector.FILE_NAME);
		List<ModuleInfoObject> mavenJsonObjects = createJSONObjects(mavenInfoFiles, ModuleInfoObject.class);
		// TODO process InfoObjects

		Map<String, List<String>> moduleDependencies = new HashMap<>();
		for (ModuleInfoObject currentObject : mavenJsonObjects) {
			moduleDependencies.put(currentObject.getTag(), currentObject.getDependsOn());
		}

		/* join information in one file */
		CollectedMavenInfoObject mavenCollection = new CollectedMavenInfoObject(projectName);
		mavenCollection.setModules(mavenJsonObjects);
		mavenCollection.setModuleDependencies(moduleDependencies);
		FileWriter.writeInfoToJSONFile(folderPath.getAbsolutePath(), ModuleInfoCollector.FILE_NAME + fileNameSuffix,
				mavenCollection, log);
		
	}

	/**
	 * Searches for the name of the project defined in the top level project. If
	 * there is no name defined, use groupId and artifactId concatenated.
	 * 
	 * @return Name of the project
	 */
	private String findProjectName() {
		MavenProject root = session.getTopLevelProject();
		return root.getName().isEmpty() ? (root.getGroupId() + ":" + root.getArtifactId()) : root.getName();

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

				File infoFile = Paths
						.get(currentProject.getBasedir().getAbsolutePath(), "target", folderName, fileName + ".json")
						.toFile();

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
	 * Creates InfoObjects (if possible) based on the files in the given list.
	 * 
	 * @param files list of files, that are to be turned into InfoObjects.
	 * @return list with the created InfoObjects.
	 */
	private <T extends InfoObject> List<T> createJSONObjects(List<File> files, Class<T> clazz) {
		List<T> objects = new ArrayList<>();
		Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

		for (File file : files) {
			try (JsonReader reader = new JsonReader(
					new InputStreamReader(new FileInputStream(file), FileWriter.CHARSET))) {
				objects.add(gson.fromJson(reader, clazz));
			} catch (IOException e) {
				log.error("Could not access file: " + file.getAbsolutePath());
				log.error(e.getMessage());
			} catch (IllegalStateException | JsonSyntaxException e) {
				log.error("Error reading JSON from file: " + file.getAbsolutePath());
				log.error(e.getMessage());
			}
		}

		return objects;
	}

}
