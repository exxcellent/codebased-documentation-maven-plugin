package reader.interfaces;

import java.io.File;
import java.util.List;

import util.OfferDescription;

public interface APIReader {

	/**
	 * Form all REST-API annotation found in the given file into OfferDescriptions.
	 * 
	 * @param src
	 *            directory or file in which the annotations are searched for
	 * @return List of OfferDescriptions of the found annotations
	 */
	public List<OfferDescription> getPathsAndMethods(File src);

}
