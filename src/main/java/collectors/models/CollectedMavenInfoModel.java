package collectors.models;

import java.util.ArrayList;
import java.util.List;

public class CollectedMavenInfoModel extends InfoObject{
	
	private List<String> dependencyGraphEdges;
	private List<MavenInfoObject> modules;
	
	public CollectedMavenInfoModel(String projectName) {
		super(projectName);
	}
	
	public List<String> getDependencyGraphEdges() {
		return dependencyGraphEdges;
	}

	public void setDependencyGraphEdges(List<String> dependencyGraphEdges) {
		this.dependencyGraphEdges = dependencyGraphEdges;
	}
	
	public void addDependencyGraphEdge(String edge) {
		if (this.dependencyGraphEdges == null) {
			this.dependencyGraphEdges = new ArrayList<>();
		}
		dependencyGraphEdges.add(edge);
	}

	public List<MavenInfoObject> getModules() {
		return modules;
	}

	public void setModules(List<MavenInfoObject> modules) {
		this.modules = modules;
	}

}
