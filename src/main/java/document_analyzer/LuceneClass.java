package document_analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

public class LuceneClass {

	private IndexWriter indexWriter;
	private IndexReader indexReader;
	private Directory directory;
	private FileOperator fileOperator = new FileOperator();
	private Map vector;
	private ArrayList<Map> documentVectors;

	public LuceneClass(String indexDirectoryPath) throws IOException {

		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		Path path = FileSystems.getDefault().getPath(indexDirectoryPath, "index");
		directory = new SimpleFSDirectory(path);
		indexWriter = new IndexWriter(directory, indexWriterConfig);

	}

	public ArrayList<Map> getTermVectors() {

		try {
			indexReader = DirectoryReader.open(directory);
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			documentVectors = new ArrayList<Map>();
			for (int i = 0; i < indexReader.maxDoc(); ++i) {
				vector = new HashMap<String, Long>();
				final Terms terms = indexReader.getTermVector(i, "Contents");
				if (terms != null) {
					int numTerms = 0;
					// record term occurrences for corpus terms above threshold
					TermsEnum term = terms.iterator();
					while (term.next() != null) {
						vector.put(term.term().utf8ToString(), term.totalTermFreq());
						++numTerms;
					}
					documentVectors.add(vector);
					System.out.println("Document Vector " + (i + 1) + " :  " + vector.toString() + "\n");
				} else {
					System.err.println("Document " + i + " had a null terms vector for body");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return documentVectors;
	}

	public void close() throws CorruptIndexException, IOException {
		indexWriter.close();
	}

	public void indexDirectory(String directory) throws IOException {

		for (File file : new File(directory).listFiles()) {

			// System.out.println("Indexing " + file.getCanonicalPath());
			Document document = getDocument(file);
			indexWriter.addDocument(document);
		}
		indexWriter.flush();
	}

	private Document getDocument(File file) throws IOException {

		Document document = new Document();

		FieldType type = new FieldType();
		type.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
		type.setTokenized(true);
		type.setStored(true);
		type.setStoreTermVectors(true);

		// index file contents
		Field contentField = new Field("Contents", fileOperator.readFiles(file.getAbsolutePath()), type);
		Field fileNameField = new TextField("FileName", file.getName(), Store.YES);

		// // index file path
		Field filePathField = new StringField("Path", file.getAbsolutePath(), Field.Store.YES);
		document.add(contentField);
		document.add(fileNameField);
		document.add(filePathField);
		return document;
	}

	public ArrayList<ArrayList<Double>> getSimilarityMatrix(ArrayList<Map> vectors) {

		CosineSimilarity cosineSimilarity = new CosineSimilarity();

		ArrayList<ArrayList<Double>> similarityMatrix = new ArrayList<ArrayList<Double>>();

		for (Map vector1 : vectors) {

			ArrayList<Double> rowSimilarity = new ArrayList<Double>();

			for (Map vector2 : vectors) {
				rowSimilarity.add(cosineSimilarity.calculateCosineSimilarity(vector1, vector2));
			}
			similarityMatrix.add(rowSimilarity);
		}
		return similarityMatrix;

	}

	public ArrayList<String> getSimilarDocuments(ArrayList<ArrayList<Double>> similarityMatrix,
			double similarityMargin) {

		ArrayList<String> similarDocuments = new ArrayList<String>();

		for (ArrayList<Double> row : similarityMatrix) {
			for (int column = 0; column < similarityMatrix.indexOf(row); column++) {

				if (row.get(column) >= Double.valueOf(0.2) && row.get(column) < Double.valueOf(0.8)) {
					similarDocuments.add("Document " + (similarityMatrix.indexOf(row) + 1)
							+ " is similar with Document " + (column + 1) + ".");
				}
			}
		}
		return similarDocuments;

	}

	public ArrayList<ResultClass> getSimilarDocuments(ArrayList<ArrayList<Double>> similarityMatrix, double minValue,
			double maxValue) {

		ArrayList<ResultClass> similarDocuments = new ArrayList<ResultClass>();

		for (ArrayList<Double> row : similarityMatrix) {
			for (int column = 0; column < similarityMatrix.indexOf(row); column++) {

				if (row.get(column) >= Double.valueOf(minValue) && row.get(column) < Double.valueOf(maxValue)) {
					ResultClass result = new ResultClass();
					result.setDocument1(similarityMatrix.indexOf(row) + 1);
					result.setDocument2(column + 1);
					similarDocuments.add(result);
				}
			}
		}

		return similarDocuments;
	}

	public ArrayList<String> getCopyDocuments(ArrayList<ArrayList<Double>> similarityMatrix) {

		ArrayList<String> copyDocuments = new ArrayList<String>();

		for (ArrayList<Double> row : similarityMatrix) {
			for (int column = 0; column < similarityMatrix.indexOf(row); column++) {

				if (row.get(column) >= Double.valueOf(0.8)) {
					copyDocuments.add("Document " + (similarityMatrix.indexOf(row) + 1)
							+ " is almost same or same as Document " + (column + 1) + ".");
				}
			}
		}
		return copyDocuments;
	}

	public ArrayList<ResultClass> getOutlierDocuments(ArrayList<ArrayList<Double>> similarityMatrix, double minValue,
			double maxValue) {
		ArrayList<ResultClass> similarDocuments = this.getSimilarDocuments(similarityMatrix, minValue, maxValue);
		ArrayList<ResultClass> outliers = new ArrayList<ResultClass>();
		boolean isOutlier;
		for (int i = 1; i <= similarityMatrix.size(); i++) {
			isOutlier = true;
			for (ResultClass result : similarDocuments) {
				if (i == result.getDocument1() || i == result.getDocument2()) {
					isOutlier = false;
					break;
				}
			}
			if (isOutlier) {
				ResultClass result = new ResultClass();
				result.setDocument1(i);
				outliers.add(result);
			}
		}
		return outliers;
	}

	public ArrayList<String> getOutlierDocuments(ArrayList<ArrayList<Double>> similarityMatrix) {

		ArrayList<String> outlierDocuments = new ArrayList<String>();

		boolean isOutlier;

		for (ArrayList<Double> row : similarityMatrix) {

			isOutlier = true;

			for (Double CosineSimilarity : row) {

				if (CosineSimilarity >= Double.valueOf(0.2)
						&& similarityMatrix.indexOf(row) != row.indexOf(CosineSimilarity)) {
					isOutlier = false;
					break;
				}
			}

			if (isOutlier) {
				outlierDocuments.add("Document " + (similarityMatrix.indexOf(row) + 1) + " is an outlier");
			}
		}
		return outlierDocuments;
	}

	public ArrayList<String> getDocumentClusters(ArrayList<ArrayList<Double>> similarityMatrix) {

		ArrayList<String> documentsCluster = new ArrayList<String>();

		boolean subString;

		for (ArrayList<Double> row : similarityMatrix) {

			subString = false;
			String cluster;
			cluster = String.valueOf(similarityMatrix.indexOf(row) + 1);

			for (int column = similarityMatrix.indexOf(row) + 1; column < row.size(); column++) {
				if (row.get(column) >= Double.valueOf(0.2) && row.get(column) < Double.valueOf(0.8)) {
					cluster = cluster + "," + String.valueOf(column + 1);
				}
			}

			for (String string : documentsCluster) {
				if (string.contains(cluster)) {
					subString = true;
					break;
				}
			}

			if (!subString) {
				documentsCluster.add(cluster);
				subString = false;
			}

		}

		return documentsCluster;
	}

	private Double getMax(ArrayList<ArrayList<Double>> matrix) {
		Double maxValue = 0.0;
		for (ArrayList<Double> row : matrix) {
			for (Double value : row) {
				if (value >= maxValue && (row.indexOf(value) != matrix.indexOf(row)))
					maxValue = value;
			}
		}
		return maxValue;
	}

}
