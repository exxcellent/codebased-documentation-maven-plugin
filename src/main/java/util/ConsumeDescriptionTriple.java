package util;

import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ConsumeDescriptionTriple {
	
	private String serviceName;
	private String path;
	private Set<String> methods;
	
	public String getServiceName() {
		return serviceName;
	}
	
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public Set<String> getMethods() {
		return methods;
	}
	
	public void setMethods(Set<String> methods) {
		this.methods = methods;
	}
	
	public void addMethod(String method) {
		if (this.methods == null) {
			methods = Sets.newHashSet(method);
		} else {
			methods.add(method);
		}
		
	}

}
