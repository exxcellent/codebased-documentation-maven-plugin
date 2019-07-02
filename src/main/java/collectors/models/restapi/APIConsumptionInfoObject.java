package collectors.models.restapi;

import java.util.ArrayList;
import java.util.List;


import collectors.models.InfoObject;
import util.ConsumeDescriptionTriple;

public class APIConsumptionInfoObject extends InfoObject {
	
	private String microserviceName;
	private List<ConsumeDescriptionTriple> consumes;
//	private Map<String, Map<String, Set<String>>> serviceToPathToMethod;
	
	public APIConsumptionInfoObject() {
//		this.serviceToPathToMethod = new HashMap<>();
		this.consumes = new ArrayList<>();
	}
	
	public String getMicroserviceName() {
		return microserviceName;
	}
	
	public void setMicroserviceName(String microserviceName) {
		this.microserviceName = microserviceName;
	}

//	public Map<String, Map<String, Set<String>>> getServiceToPathToMethod() {
//		return serviceToPathToMethod;
//	}
//
//	public void setServiceToPathToMethod(Map<String, Map<String, Set<String>>> serviceToPathToMethod) {
//		this.serviceToPathToMethod = serviceToPathToMethod;
//	}
//	
//	public void addServiceToPathToMethod(ConsumeDescriptionTriple triple) {
//		if (this.serviceToPathToMethod == null) {
//			serviceToPathToMethod = new HashMap<>();			
//		}
//		
//		if (serviceToPathToMethod.get(triple.getServiceName()) == null) {
//			Map<String, Set<String>> pathToMethod = new HashMap<>();
//			pathToMethod.put(triple.getPath(), triple.getMethods());
//			serviceToPathToMethod.put(triple.getServiceName(), pathToMethod);
//		} else {
//			Map<String, Set<String>> pathToMethod = serviceToPathToMethod.get(triple.getServiceName());
//			if (pathToMethod.get(triple.getPath()) == null) {
//				pathToMethod.put(triple.getPath(), triple.getMethods());
//			} else {
//				pathToMethod.get(triple.getPath()).addAll(triple.getMethods());
//			}
//			
//		}
//		
//	}

	public List<ConsumeDescriptionTriple> getConsumes() {
		return consumes;
	}

	public void setConsumes(List<ConsumeDescriptionTriple> consumes) {
		this.consumes = consumes;
	}
	
	public void addConsumeDescriptionTriple(ConsumeDescriptionTriple triple) {
		this.consumes.add(triple);
	}
	

}
