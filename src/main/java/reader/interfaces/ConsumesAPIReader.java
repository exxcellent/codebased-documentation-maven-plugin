package reader.interfaces;

import java.io.File;
import java.util.List;

import util.ConsumeDescription;

public interface ConsumesAPIReader {
	
	public List<ConsumeDescription> getAPIConsumption(File src);

}
