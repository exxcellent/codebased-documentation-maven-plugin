package collectors;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import collectors.models.maven.ModuleInfoObject;
import collectors.models.maven.ProjectInfoObject;
import filemanagement.FileWriter;

/**
 * Collects info available from Maven about modules and their dependencies.
 * 
 * @author gmittmann
 *
 */
public class ModuleInfoCollector implements InformationCollector {

	private MavenProject project;
	private MavenSession session;
	private Log log;

	public static final String FILE_NAME = "moduleInformation";

	public ModuleInfoCollector(MavenProject project, MavenSession session, Log log) {
		this.project = project;
		this.session = session;
		this.log = log;
	}

	/**
	 * Collects the information about the current project and its dependencies and
	 * stores them into a file in the target directory
	 */
	public void collectInfo() {
		log.info("  -- COLLECTING MODULE INFO --");

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
		List<ProjectInfoObject> dependencies = turnToDependencyInfoObject(
				listNonTestDependencies(project.getDependencies()));
		
		ModuleInfoObject info = new ModuleInfoObject(project, dependencies, dependsOn);
		
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
	private List<ProjectInfoObject> turnToDependencyInfoObject(List<Dependency> dependencies) {

		List<ProjectInfoObject> objects = new ArrayList<>();
		for (Dependency dependency : dependencies) {
			ProjectInfoObject obj = new ProjectInfoObject(dependency);
			objects.add(obj);
		}

		return objects;
	}
}
