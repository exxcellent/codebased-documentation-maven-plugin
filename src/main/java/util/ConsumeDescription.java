package util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ConsumeDescription {
	
	private String serviceName;
	private String packageName;
	private Map<String, Set<String>> pathToMethods;
	
	public String getServiceName() {
		return serviceName;
	}
	
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	
//	public String getPath() {
//		return path;
//	}
//	
//	public void setPath(String path) {
//		this.path = path;
//	}
//	
//	public Set<String> getMethods() {
//		return methods;
//	}
//	
//	public void setMethods(Set<String> methods) {
//		this.methods = methods;
//	}
//	
//	public void addMethod(String method) {
//		if (this.methods == null) {
//			methods = Sets.newHashSet(method);
//		} else {
//			methods.add(method);
//		}
//		
//	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public Map<String, Set<String>> getPathToMethods() {
		return pathToMethods;
	}

	public void setPathToMethods(Map<String, Set<String>> pathToMethods) {
		this.pathToMethods = pathToMethods;
	}

	public void addPathToMethod(String path, Set<String> methods) {
		if (pathToMethods == null || pathToMethods.isEmpty()) {
			this.pathToMethods = new HashMap<>();
			this.pathToMethods.put(path, methods);
		} else {
			if (this.pathToMethods.containsKey(path)) {
				this.pathToMethods.get(path).addAll(methods);
			} else {
				this.pathToMethods.put(path, methods);
			}
		}
	}
}
