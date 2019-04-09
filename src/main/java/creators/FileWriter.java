package creators;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

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
	 * Tries to create the file at the given path and an OutputStreamWriter to it.
	 * 
	 * @param path Path to the file.
	 * @return true, if there was no error and the file was created.
	 */
	public boolean createFile(String path) {
		try {
			FileOutputStream stream;
			log.info("creating file");
			File logFile;
			logFile = new File(path);

			filePath = logFile.getAbsolutePath();
			if (logFile.createNewFile()) {
				log.info("created file");
				stream = new FileOutputStream(logFile);
				out = new OutputStreamWriter(stream, Charset.defaultCharset()); // TODO: set charset
			} else {
				log.error("could not create file");
				return false;
			}
		} catch (FileNotFoundException e) {
			log.error("FnF: " + e.getMessage());
			return false;
		} catch (IOException e) {
			log.error("IO: " + e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Appends the given text onto the file.
	 * 
	 * @param text Text to be appended to the file.
	 * @return true, if the text was written. False if there was an error or there
	 *         was no file created beforehand. //TODO: errorhandling
	 */
	public boolean writeIntoFile(String text) {
		if (out == null) {
			return false;
		}
		try {
			out.append(text);
			out.flush();
		} catch (IOException e) {
			log.error("Could not write into file: " + filePath);
			log.error(e.getMessage());
		}
		return true;
	}

	/**
	 * Flushes and tries to close the StreamWriter.
	 * @return true, if there was no error.
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
