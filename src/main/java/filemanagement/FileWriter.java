package filemanagement;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.maven.plugin.logging.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

import collectors.models.InfoObject;

/**
 * Helper/Wrapper class for file creation and use.
 * 
 * @author gmittmann
 *
 */
public class FileWriter {

	public static final String CHARSET = "UTF-16";
	
	private final Log log;

	public FileWriter(Log log) {
		this.log = log;
	}

	/**
	 * Writes the given text into the file.
	 * 
	 * @param filePath path to the directory in which the file is to be created
	 * @param fileName name of the file, into which the text is to be written. If
	 *                 the file already exists, it will be overwritten.
	 * @param text     Text to be write to the file.
	 * @return true, if the text was written. False if there was an error or there
	 *         was no file created //TODO: errorhandling
	 */
	public boolean writeTextIntoFile(String filePath, String fileName, String text) {
		OutputStreamWriter out = createFile(filePath, fileName);
		if (out != null) {
			try {
				out.append(text);
				out.flush();
				out.close();
				return true;
			} catch (IOException e) {
				log.error("Could not write into file: " + filePath);
				log.error(e.getMessage());
			}
		}
		return false;
	}

	/**
	 * Takes the given InfoObject and serializes it to JSON into the given file.
	 * 
	 * @param <T> Type of InfoObject
	 * @param path path to the target directory
	 * @param fileName name of the file
	 * @param info InfoObject which contains the information to be written into the file
	 * @return true, if the file was created and written successfully.
	 */
	public <T extends InfoObject> boolean writeInfoToJSONFile(String path, String fileName, T info) {

		OutputStreamWriter out = createFile(path, fileName);

		if (out != null) {
			Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
			try {
				gson.toJson(info, out);
				out.flush();
				out.close();
			} catch (JsonIOException e) {
				log.error("Could not create JSON from object");
				log.error(e.getMessage());
			} catch (IOException e) {
				log.error("IOException: " + e.getMessage());
			}
			return true;
		}
		log.error("Could not write, as file couldn't be created");
		return false;
	}
	
	public void writeTestJSON(String path, String fileName, Object obj) {
		OutputStreamWriter out = createFile(path, fileName);

		if (out != null) {
			Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
			try {
				gson.toJson(obj, out);
				out.flush();
				out.close();
			} catch (JsonIOException e) {
				log.error("Could not create JSON from object");
				log.error(e.getMessage());
			} catch (IOException e) {
				log.error("IOException: " + e.getMessage());
			}
		}
	}
	
	public void writeTestJSON(String path, String fileName, List<String> list) {
		OutputStreamWriter out = createFile(path, fileName);

		if (out != null) {
			Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
			try {
				gson.toJson(list, out);
				out.flush();
				out.close();
			} catch (JsonIOException e) {
				log.error("Could not create JSON from object");
				log.error(e.getMessage());
			} catch (IOException e) {
				log.error("IOException: " + e.getMessage());
			}
		}
	}

	/**
	 * Tries to create the file with the given file name at the given path and an
	 * OutputStreamWriter to it.
	 * 
	 * @param path     path of the target directory.
	 * @param fileName name of the file to be created
	 * @return outputStreamWriter Writer to the created file. null if there was an
	 *         error.
	 */
	private OutputStreamWriter createFile(String path, String fileName) {

		try {
			createDir(path);

			Path logFile = Paths.get(path, fileName + ".json");

			if (!logFile.toFile().exists()) {
				Files.createFile(logFile);
			}
			FileOutputStream stream = new FileOutputStream(logFile.toFile(), false);
			OutputStreamWriter out = new OutputStreamWriter(stream, CHARSET); // TODO: set charset
			return out;

		} catch (NullPointerException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error("IO: " + e.getMessage());
		}
		return null;
	}

	/**
	 * Checks whether the given file path is null. If it isn't creates all
	 * directories of the path that do not exist already.
	 * 
	 * @param path Path of the target directory to create.
	 * @return Path Path object to the created directory.
	 * @throws NullPointerException if path is null
	 */
	private Path createDir(String path) throws NullPointerException {
		if (path != null) {
			try {
				return Files.createDirectories(Paths.get(path));
			} catch (IOException e1) {
				log.error("error creating directories at: " + Paths.get(path).toString());
				log.error(e1.getMessage());
			}
		} else {
			throw new NullPointerException("Path to directory is null!");
		}
		return null;
	}

}
