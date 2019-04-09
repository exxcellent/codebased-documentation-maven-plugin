package collectors;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import creators.FileWriter;

/**
 * Collects info available from Maven (currently: Modules and their dependence
 * onto each other)
 * 
 * @author gmittmann
 *
 */
public class MavenInfoCollector implements InformationCollector {

	private MavenProject project;
	private Log log;
	private FileWriter out;

	static final String FILE_NAME = "mavenTest.txt";

	public MavenInfoCollector(MavenProject project, Log log) {
		this.project = project;
		this.log = log;
		this.out = new FileWriter(log);
	}

	public MavenInfoCollector(MavenProject project, Log log, FileWriter fileWriter) {
		this.project = project;
		this.log = log;
		this.out = fileWriter;
	}

	/**
	 * Collects the information about the current project and its dependencies and
	 * stores them into a file in the target directory
	 */
	public void collectInfo() {
		log.info("ExecProject: " + project.getExecutionProject().getArtifactId());

		if (out.createFile(project.getBasedir() + "\\target\\" + FILE_NAME)) {
			out.writeIntoFile("Dependencies to other modules: \n");

			for (Dependency dependency : project.getDependencies()) {
				// TODO check via artifact id against modules
				if (dependency.getGroupId().equals(project.getGroupId())) {
					out.writeIntoFile("- " + dependency.getGroupId() + ":" + dependency.getArtifactId() + "\n");
				}
			}
			out.finishFile();
		} else {
			log.error("meh");
		}

	}
}
