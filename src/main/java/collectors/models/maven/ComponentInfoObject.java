package collectors.models.maven;

import java.util.ArrayList;
import java.util.List;

import collectors.models.InfoObject;

public class ComponentInfoObject extends InfoObject {
	
	private List<PackageInfoObject> components; 

	public ComponentInfoObject(String name) {
		super(name);
	}

	public List<PackageInfoObject> getComponents() {
		return components;
	}

	public void setComponents(List<PackageInfoObject> components) {
		this.components = components;
	}
	
	public void addComponent(PackageInfoObject component) {
		if (this.components == null) {
			this.components = new ArrayList<>();
		}
		components.add(component);
	}

}
