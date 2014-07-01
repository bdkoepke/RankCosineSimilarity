package pw.swordfish.cossim;

import com.sun.istack.internal.NotNull;
import pw.swordfish.util.HashMultiSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author brandonkoepke
 */
public class Document implements Comparable<Document> {

	private HashMultiSet<String> documentHashMultiSet;
	private boolean isCosineSimilaritySet = false;
	private float cosineSimilarity = 0;
	private String fileName;

	/**
	 * Construct a document object based on a file
	 * @param file the file to construct the object based on
	 * @throws FileNotFoundException thrown if the file cannot be found
	 * @throws IOException thrown if an IOException occurs while reading the file
	 */
	Document(File file) throws IOException {
		documentHashMultiSet = new HashMultiSet<String>();
		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		fileName = file.getName();

		/*
		 * We want to read through the entire file and add its
		 * contents and we want to read it into the document
		 */
		while (bufferedReader.ready()) {
			for(String currentWord : bufferedReader.readLine().toLowerCase().replaceAll("\\p{Punct}", "").split(" ")) {
				documentHashMultiSet.add(currentWord);
			}
		}
	}

	/**
	 * Construct a document object based on a file and remove the stopWords
	 * @param file the file to base the document on
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	Document(File file, HashMultiSet<String> stopWordHashMultiSet) throws IOException {
		documentHashMultiSet = new HashMultiSet<String>();
		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		fileName = file.getName();

		/*
		 * we want to loop through the entire file and add its
		 * contents and we want to read it into the document, we
		 * also want to make sure that we clean out any "stopwords"
		 * that we find
		 */
		while (bufferedReader.ready()) {
			/*
			 * now for every word in the lien that we read, we want to make sure
			 * that the word is not a stopWord as specified by the stopWordHashMultiset
			 */
			for (String currentWord : bufferedReader.readLine().toLowerCase().replaceAll("\\p{Punct}", "").split(" ")) {
				if (! currentWord.equals("") && (! stopWordHashMultiSet.contains(currentWord))) {
					documentHashMultiSet.add(currentWord);
				}
			}
		}

	}

	/**
	 * Set the cosine similarity of two documents
	 * @param queryDocument the document to set the cosine similarity on
	 * @param multipleDocumentHashMultiSet the document multiset
	 */
	public void setCosineSimilarity(Document queryDocument, HashMultiSet<String> multipleDocumentHashMultiSet) {
		/*
		 * initialize variables for the calculation
		 */
		float documentR, queryR, dividend = 0, documentSquaredSum = 0, querySquaredSum = 0;
		cosineSimilarity = 0;

		HashMultiSet<String> terms = new HashMultiSet<String>();
		terms.add(documentHashMultiSet);
		terms.add(queryDocument.toHashMultiset());


		/*
		 * for every term in the query document we want to increment our cosine 
		 * similarity by the value calculated by using the cosine similarity
		 */
		for (String term : terms.getKeySet()) {
			int numberOfDocuments = multipleDocumentHashMultiSet.count(term);

			if(numberOfDocuments != 0) {

				/*
				 * the reason I am doing this here is simply for performance
				 */
				documentR = r(term, numberOfDocuments);
				queryR = queryDocument.r(term, numberOfDocuments);

				dividend += documentR * queryR;
				documentSquaredSum += documentR * documentR;
				querySquaredSum += queryR * queryR;
			}
		}

		float divisor = (new Double(Math.sqrt(documentSquaredSum)).floatValue()) * (new Double(Math.sqrt(querySquaredSum)).floatValue());

        cosineSimilarity = documentSquaredSum == 0 ? 0 : dividend / divisor;
		/*
		 * now we save the getCosineSimilarity method some CPU time by letting it 
		 * know that we don't have to calculate this again
		 */
		isCosineSimilaritySet = true;
	}

	/**
	 * Get the cosine similarity that has been set already
	 * @return the cosine similarity
	 */
	public float getCosineSimilarity() {
		/*
		 * check to see if some nice person has already set the cosine 
		 * similarity for us
		 */
		if(! isCosineSimilaritySet)
			throw new DocumentException("The cosine similarity has not been set");
		return cosineSimilarity;
	}

	/**
	 * Calculate r, I don't really know what this does
	 * @param term the term to calculate the frequency based on
	 * @param numberOfDocuments the number of documents containing the term t
	 * @return the value of r
	 */
	public float r(String term, int numberOfDocuments) {
		return termFrequency(term) * inverseDocumentFrequency(numberOfDocuments);
	}

	/**
	 * Calculate the term frequency
	 * @param term the term to calculate the frequency based on
	 * @return the term frequency
	 */
	private float termFrequency(String term) {
		return new Double(Math.log(1 + (((double) documentHashMultiSet.count(term)) / ((double)documentHashMultiSet.countAll())))).floatValue();
	}

	/**
	 * Get the inverse document frequency from the number of documents
	 * @param numberOfDocuments the number of documents
	 * @return the inverse document frequency as a double
	 */
	private float inverseDocumentFrequency(int numberOfDocuments) {
		if(numberOfDocuments <= 0) { 
			return 0; 
		}
		return  (1 / new Integer(numberOfDocuments).floatValue());
	}

	/**
	 * Get the hashMultiset representing this document
	 * @return the hashMultiset representing this document
	 */
	public HashMultiSet<String> toHashMultiset() {
		return documentHashMultiSet;
	}

	/**
	 * We override the toString method here so we can easily check 
	 * how our document is doing.  
	 * @return the string representing the document object
	 */
	@Override
	public String toString() {
		return documentHashMultiSet.toString();
	}

	/**
	 * Get the name of the file
	 * @return the name of the file
	 */
	public String getName() {
		return fileName;
	}

	/**
	 * We override the compareTo method so we can easily sort the documents
	 * based on their cosine similarity
	 * @param o the document to compare to 
	 * @return the value of the comparison
	 */
	@Override
	@SuppressWarnings("unchecked")
	public int compareTo(Document o) {
		if(! isCosineSimilaritySet)
			throw new DocumentException("The cosine similarity has not been set");
		/*
		 * now we create a float representing the differences between the cosine similarities
		 */
		float difference = getCosineSimilarity() - o.getCosineSimilarity();

		/*
		 * now we define our return value based on the difference value,
		 * the reason we have to do this is the fact that get cosine similarity
		 * is a float and not an int
		 */
		return difference > 0 ? -1 : (difference == 0 ? 0 : 1);
	}
}
