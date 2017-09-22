package document_analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class FileOperator {

	private File file;
	private FileReader fileReader;

	/**
	 * This method reads the files present in a particular dire
	 * 
	 * @param filePath
	 *            absolute path of the directory
	 * @return String  which contains the text 
	 * @throws IOException
	 */
	public String readFiles(String filePath) throws IOException {

		file = new File(filePath);

		String fileText = "";
		String line;

		fileReader = new FileReader(file.getAbsolutePath());
		BufferedReader bufferedReader = new BufferedReader(fileReader);

		while ((line = bufferedReader.readLine()) != null) {
			fileText = fileText + line;
		}

		bufferedReader.close();
		fileReader.close();

		return fileText;
	}

}
