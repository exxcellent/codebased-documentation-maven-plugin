package collectors.models.maven;

import java.util.ArrayList;
import java.util.List;

import collectors.models.InfoObject;

public class ModuleToComponentInfoObject extends InfoObject {
	
	private String moduleName;
	private List<ComponentInfoObject> components; 

	public ModuleToComponentInfoObject(String name) {
		this.setModuleName(name);
	}

	public List<ComponentInfoObject> getComponents() {
		return components;
	}

	public void setComponents(List<ComponentInfoObject> components) {
		this.components = components;
	}
	
	public void addComponent(ComponentInfoObject component) {
		if (this.components == null) {
			this.components = new ArrayList<>();
		}
		components.add(component);
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

}
