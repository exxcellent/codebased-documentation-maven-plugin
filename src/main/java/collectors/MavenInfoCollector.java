package collectors;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import collectors.models.DependencyInfoObject;
import collectors.models.MavenInfoObject;
import filemanagement.FileWriter;

/**
 * Collects info available from Maven (currently: Modules and their dependence
 * onto each other)
 * 
 * @author gmittmann
 *
 */
public class MavenInfoCollector implements InformationCollector {

	private MavenProject project;
	private MavenSession session;
	private Log log;

	public static final String FILE_NAME = "mavenInformation";

	public MavenInfoCollector(MavenProject project, MavenSession session, Log log) {
		this.project = project;
		this.session = session;
		this.log = log;
	}

	/**
	 * Collects the information about the current project and its dependencies and
	 * stores them into a file in the target directory
	 */
	public void collectInfo() {
		log.info("  -- COLLECTING MAVEN INFO --");

		String dirPath = Paths.get(project.getBasedir().getAbsolutePath(), "target", FOLDER_NAME).toString();
		log.info("target folder: " + dirPath);
		log.info("target file: " + FILE_NAME);

		/*Get all projects the current project directly depends on. Projects with packaging type pom are irrelevant*/
		List<String> dependsOn = new ArrayList<>();
		for (MavenProject prj : session.getProjectDependencyGraph().getUpstreamProjects(project, false)) {
			if (!prj.getPackaging().equalsIgnoreCase("pom")) {
				dependsOn.add((prj.getGroupId() + ":" + prj.getArtifactId() + ":" + prj.getVersion()));
			}
		}
		
		/*Create DependencyInfoObjects for the non test dependencies of the current project*/
		List<DependencyInfoObject> dependencies = turnToDependencyInfoObject(
				listNonTestDependencies(project.getDependencies()));
		
		MavenInfoObject info = new MavenInfoObject(project, dependencies, dependsOn);
		
		FileWriter.writeInfoToJSONFile(dirPath, FILE_NAME, info, log);

	}

	/**
	 * FIlters all dependencies that are optional or have their scope set to test.
	 * 
	 * @param dependencies List of dependencies to filter.
	 * @return List of the filtered dependencies.
	 */
	private List<Dependency> listNonTestDependencies(List<Dependency> dependencies) {
		List<Dependency> nonTestDependencies = new ArrayList<>();

		for (Dependency dependency : dependencies) {
			if (!dependency.isOptional() && !dependency.getScope().equalsIgnoreCase("test")) {
				nonTestDependencies.add(dependency);
			}
		}

		return nonTestDependencies;
	}

	/**
	 * Turns the given list of Dependency objects into a list of
	 * DependencyInfoObjects.
	 * 
	 * @param dependencies List of Dependency objects, whose information should be
	 *                     turned into DependencyInfoObjects.
	 * @return List of DependencyInfoObjects
	 */
	private List<DependencyInfoObject> turnToDependencyInfoObject(List<Dependency> dependencies) {

		List<DependencyInfoObject> objects = new ArrayList<>();
		for (Dependency dependency : dependencies) {
			DependencyInfoObject obj = new DependencyInfoObject(dependency);
			objects.add(obj);
		}

		return objects;
	}
}
