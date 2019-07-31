package filemanagement;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import collectors.ModuleInfoCollector;
import collectors.APIInfoCollector;
import collectors.ComponentInfoCollector;
import collectors.models.InfoObject;
import collectors.models.maven.CollectedMavenInfoObject;
import collectors.models.maven.ModuleToComponentInfoObject;
import collectors.models.maven.ModuleInfoObject;
import collectors.models.restapi.APIConsumptionInfoObject;
import collectors.models.restapi.APIInfoObject;
import collectors.models.restapi.CollectedAPIInfoObject;
import mojos.DocumentationMojo;

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
	 * Collects all component and module information files of the projects and
	 * combines them to one joined file with all information.
	 * 
	 * @param folderPath
	 *            path to the folder, into which the new files is to be saved
	 * @param fileNameSuffix
	 *            suffix to be appended to the file name
	 */
	public void aggregateMavenFilesTo(File folderPath, String fileNameSuffix) {

		String projectName = findProjectName();

		log.info("    - MODULE FILES - ");
		List<File> moduleInfoFiles = findFiles(ModuleInfoCollector.FOLDER_NAME, ModuleInfoCollector.FILE_NAME);
		List<ModuleInfoObject> mavenJsonObjects = createJSONObjects(moduleInfoFiles, ModuleInfoObject.class);

		log.info("    - COMPONENT FILES - ");
		List<File> componentInfoFiles = findFiles(ComponentInfoCollector.FOLDER_NAME, ComponentInfoCollector.FILE_NAME);
		List<ModuleToComponentInfoObject> packageJsonObjects = createJSONObjects(componentInfoFiles,
				ModuleToComponentInfoObject.class);

		log.info("    - AGGREGATE - ");
		/* join information in one file */
		CollectedMavenInfoObject mavenCollection = new CollectedMavenInfoObject(projectName, findProjectTag(),
				findSystem(), findSubsystem());
		mavenCollection.setModules(mavenJsonObjects);
		mavenCollection.setComponents(packageJsonObjects);
		log.info("    - WRITE - ");
		FileWriter.writeInfoToJSONFile(folderPath.getAbsolutePath(),
				DocumentationMojo.MAVEN_AGGREGATE_NAME + fileNameSuffix, mavenCollection, log);
		log.info("completed aggregating maven info");

	}

	/**
	 * Collects all information about API of the projects into one joined file at
	 * the given path.
	 * 
	 * @param folderPath
	 *            path to the folder, into which the new files is to be saved
	 * @param fileNameSuffix
	 *            suffix to be appended to the file name of the topics
	 */
	public void aggregateAPIFilesTo(File folderPath, String fileNameSuffix) {
		log.info("    - REST OFFER FILE - ");
		List<File> interfaceInfoFiles = findFiles(APIInfoCollector.FOLDER_NAME, APIInfoCollector.FILE_NAME);
		List<APIInfoObject> apiInfoObjects = createJSONObjects(interfaceInfoFiles, APIInfoObject.class);

		/* merge APIINfoObjects */
		APIInfoObject apiInfoObject = new APIInfoObject(findProjectTag(), findProjectName());
		for (APIInfoObject info : apiInfoObjects) {
			apiInfoObject.addOffers(info.getApi());
		}

		System.out.println("---");

		log.info("    - REST CONSUME FILE - ");
		/* get APIConsumptionInfoObjects */
		List<File> consumeInfoFiles = findFiles(APIInfoCollector.FOLDER_NAME, APIInfoCollector.FILE_NAME_CONSUME);
		List<APIConsumptionInfoObject> apiConsumeInfoObjects = createJSONObjects(consumeInfoFiles,
				APIConsumptionInfoObject.class);

		log.info("    - AGGREGATE - ");
		CollectedAPIInfoObject collectedInfo = new CollectedAPIInfoObject(apiInfoObject.getMicroserviceName());
		collectedInfo.setServiceTag(apiInfoObject.getMicroserviceTag());
		collectedInfo.setProvide(apiInfoObject);

		for (APIConsumptionInfoObject consumption : apiConsumeInfoObjects) {
			collectedInfo.addConsumeDescriptionTriples(consumption.getConsumes());
		}

		log.info("    - WRITE - ");
		FileWriter.writeInfoToJSONFile(folderPath.getAbsolutePath(),
				DocumentationMojo.API_AGGREGATE_NAME + fileNameSuffix, collectedInfo, log);
		log.info("completed aggregating rest api info");
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
	 * Searches for groupId, artifactId and Version of the root project and
	 * concatenates the found values.
	 * 
	 * @return concatenation of groupId, artifactId and version of root project
	 */
	private String findProjectTag() {
		MavenProject root = session.getTopLevelProject();
		return (root.getGroupId() + ":" + root.getArtifactId() + ":" + root.getVersion());
	}

	/**
	 * Reads system configuration from the root project.
	 * 
	 * @return the found system name
	 */
	private String findSystem() {
		MavenProject root = session.getTopLevelProject();
		for (MavenProject prj : session.getAllProjects()) {
			if (prj.isExecutionRoot()) {
				root = prj;
			}
		}
		String system = extractFromConfigurationDOM(
				root.getPlugin("codebased-documentation:cd-maven-plugin").getConfiguration(), "system");

		if (system == null) {
			system = "default_system";
		}

		return system;
	}

	/**
	 * Read subsystem configuration of the root project.
	 * 
	 * @return found subsystem name
	 */
	private String findSubsystem() {
		MavenProject root = session.getTopLevelProject();
		for (MavenProject prj : session.getAllProjects()) {
			if (prj.isExecutionRoot()) {
				root = prj;
			}
		}
		String subsystem = extractFromConfigurationDOM(
				root.getPlugin("codebased-documentation:cd-maven-plugin").getConfiguration(), "subsystem");

		if (subsystem == null) {
			subsystem = "default_subsystem";
		}

		return subsystem;
	}

	/**
	 * Searches for a certain file in the target folder of each project of this
	 * session and puts them in a list. If the file is in the target folder itself,
	 * set folderName to a dot [.].
	 * 
	 * @param folderName
	 *            name of the folder, in which the file is located. Path relative to
	 *            the target folder of the projects.
	 * @param fileName
	 *            name of the file (without document type).
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
	 * @param files
	 *            list of files, that are to be turned into InfoObjects.
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

	/**
	 * Tries to read the given parameter in the given object. If the object is not a
	 * Xpp3Dom object or the parameter doesn't exist, returns null.
	 * 
	 * @param domObject
	 *            object in which the parameter is searched for.
	 * @param parameterName
	 *            name of the parameter
	 * @return value of the parameter or null, if object doesn't exist.
	 */
	private String extractFromConfigurationDOM(Object domObject, String parameterName) {
		if (domObject instanceof Xpp3Dom) {
			Xpp3Dom docLocationChild = ((Xpp3Dom) domObject).getChild(parameterName);
			if (docLocationChild != null) {
				return docLocationChild.getValue();
			}
		}
		return null;
	}

}
