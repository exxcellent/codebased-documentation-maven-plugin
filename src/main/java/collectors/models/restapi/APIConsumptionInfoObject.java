package collectors.models.restapi;

import java.util.ArrayList;
import java.util.List;


import collectors.models.InfoObject;
import util.ConsumeDescription;

public class APIConsumptionInfoObject extends InfoObject {
	
	private String microserviceName;
	private List<ConsumeDescription> consumes;
//	private Map<String, Map<String, Set<String>>> serviceToPathToMethod;
	
	public APIConsumptionInfoObject() {
		this.consumes = new ArrayList<>();
	}
	
	public String getMicroserviceName() {
		return microserviceName;
	}
	
	public void setMicroserviceName(String microserviceName) {
		this.microserviceName = microserviceName;
	}


	public List<ConsumeDescription> getConsumes() {
		return consumes;
	}

	public void setConsumes(List<ConsumeDescription> consumes) {
		this.consumes = consumes;
	}
	
	public void addConsumeDescriptionTriple(ConsumeDescription triple) {
		this.consumes.add(triple);
	}
	

}
