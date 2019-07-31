package collectors.models.maven;

import java.util.List;

import collectors.models.InfoObject;

/**
 * Class containing all collected data concerning internal structure of the projects.
 * 
 * @author gmittmann
 *
 */
public class CollectedMavenInfoObject extends InfoObject{
	
	private String projectName;
	private String tag;
	private String system;
	private String subsystem;
	private List<ModuleInfoObject> modules;
	private List<ModuleToComponentInfoObject> components;
	
	public CollectedMavenInfoObject(String projectName, String tag, String system, String subsystem) {
		this.setProjectName(projectName);
		this.tag = tag;
		this.system = system;
		this.subsystem = subsystem;
	}

	public List<ModuleInfoObject> getModules() {
		return modules;
	}

	public void setModules(List<ModuleInfoObject> modules) {
		this.modules = modules;
	}

	public List<ModuleToComponentInfoObject> getComponents() {
		return components;
	}

	public void setComponents(List<ModuleToComponentInfoObject> components) {
		this.components = components;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getSystem() {
		return system;
	}

	public void setSystem(String system) {
		this.system = system;
	}

	public String getSubsystem() {
		return subsystem;
	}

	public void setSubsystem(String subsystem) {
		this.subsystem = subsystem;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

}
