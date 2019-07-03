package reader.interfaces;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import util.HttpMethods;
import util.OfferDescription;
import util.Pair;

public interface APIReader {
	
//	public static final List<String> HTTP_METHOD_TYPE = Arrays.asList("GET", "PUT", "POST", "DELETE", "HEAD", "OPTIONS", "PATCH");
	
	public List<OfferDescription> getPathsAndMethods(File src);

}
