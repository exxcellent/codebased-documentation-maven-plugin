package collectors;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import collectors.models.maven.ModuleInfoObject;
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
		
		ModuleInfoObject info = new ModuleInfoObject(project, dependsOn);
		
		FileWriter.writeInfoToJSONFile(dirPath, FILE_NAME, info, log);

	}
}
