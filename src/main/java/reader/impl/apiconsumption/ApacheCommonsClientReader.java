package reader.impl.apiconsumption;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.impl.client.CloseableHttpClient;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;

import reader.interfaces.ConsumesAPIReader;
import util.ConsumeDescriptionTriple;

public class ApacheCommonsClientReader implements ConsumesAPIReader {

	@Override
	public List<ConsumeDescriptionTriple> getAPIConsumption(File src) {
		List<ConsumeDescriptionTriple> returnList = new ArrayList<>();

		
		
		return returnList;
	}

}
