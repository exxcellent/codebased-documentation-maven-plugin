package collectors.models.restapi;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import collectors.models.InfoObject;

public class APIInfoObject extends InfoObject {

	private String address;
	private Map<String, Set<String>> pathToMethod;

	public APIInfoObject(String name) {
		super(name);
		pathToMethod = new HashMap<>();
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Map<String, Set<String>> getPathToMethod() {
		return pathToMethod;
	}

	public void setPathToMethod(Map<String, Set<String>> pathToMethod) {
		this.pathToMethod = pathToMethod;
	}

	public void addMethod(String path, String method) {
		if (pathToMethod.containsKey(path)) {
			pathToMethod.get(path).add(method);
			return;
		}
		Set<String> methods = new HashSet<>();
		methods.add(method);
		pathToMethod.put(path, methods);
	}
	
	public void addMethod(String path, Collection<String> method) {
		if (pathToMethod.containsKey(path)) {
			pathToMethod.get(path).addAll(method);
			return;
		}
		Set<String> methods = new HashSet<>();
		methods.addAll(method);
		pathToMethod.put(path, methods);
	}
	
	public void addPath(String path) {
		if (pathToMethod.containsKey(path)) {
			return;
		}
		pathToMethod.put(path, new HashSet<>());
	}

}
