package collectors.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectedMavenInfoObject extends InfoObject{
	
	private Map<String, List<String>> moduleDependencies;
	private List<MavenInfoObject> modules;
	
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

	public List<MavenInfoObject> getModules() {
		return modules;
	}

	public void setModules(List<MavenInfoObject> modules) {
		this.modules = modules;
	}

}
