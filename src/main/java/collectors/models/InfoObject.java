package collectors.models;

public abstract class InfoObject {
	
	private String name;
	
	public InfoObject(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

}
