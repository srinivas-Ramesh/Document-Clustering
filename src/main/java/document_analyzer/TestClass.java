package document_analyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class TestClass {

	private static String indexDirectoryPath = "C:\\Users\\i322373\\Desktop";
	private static String dataDirectoryPath = "C:\\Users\\i322373\\Desktop\\textFiles1";

	public static void main(String args[]) {

		ArrayList<Map> termVectors;
		ArrayList<ArrayList<Double>> similarityMatrix;
		ArrayList<String> similarDocuments;
		ArrayList<String> copyDocuments;
		ArrayList<String> outlierDocuments;
		ArrayList<String> documentsClusters;

		try {

			LuceneClass luceneClass = new LuceneClass(indexDirectoryPath);

			luceneClass.indexDirectory(dataDirectoryPath);

			luceneClass.close();

			System.out.println("--------------------DOCUMENT VECTORS--------------------\n");
			termVectors = luceneClass.getTermVectors();
			
			System.out.println("\n");

			System.out.println("--------------------SIMILARITY MATRIX--------------------\n");
			
			similarityMatrix = luceneClass.getSimilarityMatrix(termVectors);

			for (ArrayList<Double> row : similarityMatrix) {
				for (Double CosineSimilarity : row) {
					System.out.print(CosineSimilarity + "\t");
				}
				System.out.println("\n");
			}
			System.out.println("\n");
			
			System.out.println("--------------------SIMILAR DOCUMENTS--------------------\n");
			
			similarDocuments = luceneClass.getSimilarDocuments(similarityMatrix, 0.2);
			for (String string : similarDocuments) {
				System.out.println(string+"\n");
			}
			System.out.println("\n");
			
			System.out.println("--------------------COPY DOCUMENTS--------------------\n");
			copyDocuments = luceneClass.getCopyDocuments(similarityMatrix);
			for (String string : copyDocuments) {
				System.out.println(string+"\n");
			}
			System.out.println("\n");

			System.out.println("--------------------OUTLIER DOCUMENTS--------------------\n");
			outlierDocuments = luceneClass.getOutlierDocuments(similarityMatrix);
			for (String string : outlierDocuments) {
				System.out.println(string+"\n");
			}
			System.out.println("\n");
			
			System.out.println("--------------------DOCUMENTS CLUSTER--------------------\n");
			documentsClusters = luceneClass.getDocumentClusters(similarityMatrix);
			for (String string : documentsClusters) {
				System.out.println("Cluster "+(documentsClusters.indexOf(string)+1)+": "+string+"\n");
			}
			System.out.println("\n");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
