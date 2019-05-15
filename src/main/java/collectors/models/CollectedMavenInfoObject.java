package collectors.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectedMavenInfoObject extends InfoObject{
	
	private Map<String, List<String>> moduleDependencies;
	private List<ModuleInfoObject> modules;
	private List<ComponentInfoObject> components;
	
	public CollectedMavenInfoObject(String projectName) {
		super(projectName);
	}
	
	public Map<String, List<String>> getModuleDependencies() {
		return moduleDependencies;
	}

	public void setModuleDependencies(Map<String, List<String>> dependencyGraphEdges) {
		this.moduleDependencies = dependencyGraphEdges;
	}
	
	public void addModuleDependencies(String node, List<String> edges) {
		if (this.moduleDependencies == null) {
			this.moduleDependencies = new HashMap<>();
		}
		moduleDependencies.put(node, edges);
	}

	public List<ModuleInfoObject> getModules() {
		return modules;
	}

	public void setModules(List<ModuleInfoObject> modules) {
		this.modules = modules;
	}

	public List<ComponentInfoObject> getComponents() {
		return components;
	}

	public void setComponents(List<ComponentInfoObject> components) {
		this.components = components;
	}

}
