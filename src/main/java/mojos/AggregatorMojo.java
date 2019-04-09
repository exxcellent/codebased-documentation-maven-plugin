package mojos;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Mojo to test aggregator param. 
 * DOES NOT WORK AS INTENDED
 * @author gmittmann
 *
 */
@Mojo(name = "test", aggregator = true)
public class AggregatorMojo extends AbstractMojo {
	
	@Parameter( property = "project", defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;
	
	@Parameter( property = "session", defaultValue = "${session}")
	private MavenSession session;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().error("-------AGGREGATING------");
		
		
		for (MavenProject pr : session.getProjectDependencyGraph().getSortedProjects()) {
			getLog().info(pr.getArtifactId());
		}
		
		getLog().info("aggregator: " + project.getArtifactId());
		getLog().info("lok: " + project.getBasedir().getAbsolutePath());
		getLog().info("exe: " + (project.isExecutionRoot() ? "root" : "notRoot"));
		
		if (project.isExecutionRoot()) {
			// check if all submodules have a file
			// if they don't - exit, else continue
			List<String> modules = new ArrayList<>();
			for (Object obj : project.getModules()) {
				modules.add(obj.toString());
			}
			for (String module : modules) {
				File testFile = new File(module + "\\target\\mavenTest.txt");
				if (!testFile.exists()) {
					getLog().error("not all files available");
					return;
				}
				getLog().info("file available");
			}
			
		} 
		

		// write all files in one big file
	}

}
