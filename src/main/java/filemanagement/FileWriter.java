package filemanagement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.apache.maven.plugin.logging.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Helper/Wrapper class for file creation and use.
 * 
 * @author gmittmann
 *
 */
public class FileWriter {

	private Log log;
	OutputStreamWriter out;
	private String filePath;

	public FileWriter(Log log) {
		this.log = log;
	}

	/**
	 * Tries to create the file with the given file name at the given path and an
	 * OutputStreamWriter to it.
	 * 
	 * @param path     Path to the file.
	 * @param fileName name of the file to be created (WITH type)
	 * @return true, if there was no error and the file was created.
	 */
	public boolean createFile(String path, String fileName) {
		try {
			FileOutputStream stream;

			File dir = new File(path);
			dir.mkdirs();

			File logFile = new File(path + "\\" + fileName + ".json");

			filePath = logFile.getAbsolutePath();
			if (logFile.createNewFile() || logFile.exists()) {
				stream = new FileOutputStream(logFile, false);
				out = new OutputStreamWriter(stream, "UTF-8"); // TODO: set charset
				return true;
			} else {
				log.error("could not create file");
			}
		} catch (FileNotFoundException e) {
			log.error("FnF: " + e.getMessage());
		} catch (IOException e) {
			log.error("IO: " + e.getMessage());
		}
		return false;
	}

	/**
	 * Appends the given text onto the file.
	 * 
	 * @param text Text to be appended to the file.
	 * @return true, if the text was written. False if there was an error or there
	 *         was no file created beforehand. //TODO: errorhandling
	 */
	public boolean writeTextIntoFile(String text) {
		if (out != null) {
			try {
				out.append(text);
				out.flush();
				return true;
			} catch (IOException e) {
				log.error("Could not write into file: " + filePath);
				log.error(e.getMessage());
			}
		}
		return false;
	}

	/**
	 * Writes the given JSON Object into the file.
	 * 
	 * @param json JSONObject that contains the information that is to be written
	 *             into the file.
	 * @return true, if write was successful
	 */
	public boolean writeJSONObjectIntoFile(JSONObject json) {
		if (out != null) {
			try {
				out.write(json.toString(1));
				return true;
			} catch (JSONException e) {
				log.error("JSON file could not be created: ");
				log.error(e.getMessage());
			} catch (IOException e) {
				log.error("Could not write into file: " + filePath);
				log.error(e.getMessage());
			}
		}
		return false;
	}

	/**
	 * Flushes and tries to close the StreamWriter.
	 * 
	 * @return true, if there was no error and StreamWriter was closed.
	 */
	public boolean finishFile() {
		if (out == null) {
			return false;
		}
		try {
			out.flush();
			out.close();
		} catch (IOException e) {
			log.error("could not close OutputStreamWriter");
			return false;
		}
		return true;
	}

}
