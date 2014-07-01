package pw.swordfish.cossim;

import pw.swordfish.util.HashMultiSet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 *
 * @author brandonkoepke
 */
public class App {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		/*
		 * if the number of arguments is not 6 then
		 * the arguments can't possibly be valid
		 */
		if (args.length < 4)
			usage();

		/*
		 * initialize the argument variables
		 */
		String queryFileString = null;
		Integer numberOfDocuments = 10;
		String repositoryDirectoryString = null;
		String stopWordsString = "stopwords.txt";
		Document stopWordsDocument = null;
		Document queryDocument = null;
		Vector<Document> repositoryDocuments = new Vector<Document>();
		HashMultiSet<String> documentHashMultiSet = new HashMultiSet<String>();


		/*
		 * get the arguments in no particular order
		 */
		for (int argsIndex = 0; argsIndex < args.length; argsIndex++)
			if (args[argsIndex].equals("-f")) {
				argsIndex++;
				queryFileString = args[argsIndex];
			} else if (args[argsIndex].equals("-k")) {
				argsIndex++;
				numberOfDocuments = Integer.parseInt(args[argsIndex]);
			} else if (args[argsIndex].equals("-d")) {
                argsIndex++;
                repositoryDirectoryString = args[argsIndex];
            } else if (args[argsIndex].equals("-s")) {
				argsIndex++;
				stopWordsString = args[argsIndex];
            } else
				usage("Argument <" + args[argsIndex] + "> is not a valid parameter\n");

        assert (queryFileString != null && repositoryDirectoryString != null);
		/*
		 * initialize the queryFile and the repositoryDirectory
		 */
		File queryFile = new File(queryFileString);
		File repositoryDirectory = new File(repositoryDirectoryString);
		File stopWordsFile = new File(stopWordsString);

		/*
		 * now we check if all of the arguments were initialized correctly
		 */
		if (!queryFile.isFile())
			usage("The query file " + queryFileString + " does not exist\n");
		if (!repositoryDirectory.isDirectory())
			usage("The repository directory " + repositoryDirectoryString + " does not exist\n");
		if (!stopWordsFile.isFile())
			usage("The stop words file " + stopWordsString + " does not exist\n");

		/*
		 * now we try to create the query document based on the query file
		 */
		try {
			queryDocument = new Document(queryFile);
		} catch (FileNotFoundException ex) {
			usage(ex.getMessage());
		} catch (IOException ex) {
			usage(ex.getMessage());
		}

		/*
		 * get a list of all the files in the directory
		 */
		File[] files = repositoryDirectory.listFiles();
        assert (files != null);

		/*
		 * catch a number format exception
		 */
		try {
			Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    int fileNameNumberOne = Integer.parseInt(o1.getName().replaceAll("\\D", ""));
                    int fileNameNumberTwo = Integer.parseInt(o2.getName().replaceAll("\\D", ""));

                    return fileNameNumberOne - fileNameNumberTwo;
                }
            });
		} catch (NumberFormatException ex) {
			usage("Error formatting file: " + ex.getMessage() + "\n");
		}

		/**
		 * try to create a stopWordsDocument
		 */
		try {
			stopWordsDocument = new Document(stopWordsFile);
		} catch (FileNotFoundException ex) {
			usage(ex.getMessage());
		} catch (IOException ex) {
			usage(ex.getMessage());
		}

		/*
		 * check if the names of the files in the directory are
		 * formatted correctly and add them to the vector
		 */
		for (File file : files) {
            if ((file != null) && (!file.isFile()))
				usage(file.getName() + " is not a file\n");

            assert (stopWordsDocument != null);
			/*
			 * now we want to try to create a repository document based on the 
			 * current file and then we want to add that to our list
			 */
			Document repositoryDocument = null;
			try {
				repositoryDocument = new Document(file, stopWordsDocument.toHashMultiset());
			} catch (FileNotFoundException ex) {
				usage(ex.getMessage());
			} catch (IOException ex) {
				usage(ex.getMessage());
			}
			repositoryDocuments.add(repositoryDocument);
            assert (repositoryDocument != null);

			/*
			 * now we add every term to a document hash multi set so we can 
			 * get the number of documents that contain the term t easily 
			 * later
			 */
			for(String term : repositoryDocument.toHashMultiset().getKeySet())
				documentHashMultiSet.add(term);
		}

		/*
		 * Now for every document in the document repository, we want to 
		 * set the cosine similarity
		 */
		for(Document document : repositoryDocuments)
			document.setCosineSimilarity(queryDocument, documentHashMultiSet);

		/*
		 * now we sort the repository documents according to their cosine
		 * similarity
		 */
		Collections.sort(repositoryDocuments);

		/*
		 * create an iterator so we can individually grab each of the documents 
		 * from the repositoryDocuments
		 */
		Iterator<Document> repositoryDocumentsIterator = repositoryDocuments.iterator();

		if(repositoryDocuments.size() < numberOfDocuments)
			numberOfDocuments = repositoryDocuments.size(); 

		/*
		 * iterate through the repository documents iterator and 
		 * print off the documents name and cosine similarity
		 */
		for(int index = 0; index < numberOfDocuments; index++) {
			Document document = repositoryDocumentsIterator.next();
			System.out.println((index + 1) + ". " + document.getName() + "\t Similarity: " + document.getCosineSimilarity());
		}

	}

	/**
	 * print the usage of the program with a message and then exit
	 * @param message a message to print before printing the usage
	 */
	private static void usage(String message) {
		System.err.println(message);
		usage();
	}

	/**
	 * print the usage of the program and exit
	 */
	private static void usage() {
		System.err.println("Usage: java -jar RankCosSim.jar -f <queryFile> -k <numberOfDocuments> -d <repositoryDirectory> -s <stopWordsFile>\n" +
				"	<queryFile>:			The file representing the query\n" +
				"	<numberOfDocuments>:	The maximum number of documents to return (default is 10)\n" +
				"	<repositoryDirectory>:	The directory containing \n" +
				"	<stopWordsFile>:		<optional> The stop words file (default is stopwords.txt)");
		System.exit(-1);
	}
}
