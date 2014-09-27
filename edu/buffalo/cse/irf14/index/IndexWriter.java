/**
 * 
 */
package edu.buffalo.cse.irf14.index;

import edu.buffalo.cse.irf14.analysis.*;
import edu.buffalo.cse.irf14.document.Document;
import edu.buffalo.cse.irf14.document.FieldNames;

/**
 * @author nikhillo
 * Class responsible for writing indexes to disk
 */
public class IndexWriter {
	/**
	 * Default constructor
	 * @param indexDir : The root directory to be sued for indexing
	 */
	public IndexWriter(String indexDir) {
		//TODO : YOU MUST IMPLEMENT THIS
	}

	/**
	 * Method to add the given Document to the index
	 * This method should take care of reading the filed values, passing
	 * them through corresponding analyzers and then indexing the results
	 * for each indexable field within the document. 
	 * @param d : The Document to be added
	 * @throws IndexerException : In case any error occurs
	 */

	public void addDocument(Document d) throws IndexerException {

		//TODO : YOU MUST IMPLEMENT THIS

		// Content tokenization

		Tokenizer tokenizer = new Tokenizer();
		try
		{
			String[] stringarr = d.getField(FieldNames.CONTENT);
			for(String s:stringarr)
			{
				TokenStream tStream = tokenizer.consume(s);
				AnalyzerFactory A =  AnalyzerFactory.getInstance();
				Analyzer contentAnalyser = A.getAnalyzerForField(FieldNames.CONTENT,tStream );
				//contentAnalyser.analyze();
				
				

			}
			
			
			
			// Title
			
			
			
			//AUthors
			
			
			
			
			
			
			
			
			
			
			
		}catch(Exception ex)
		{
			throw new IndexerException();
		}





	}

	/**
	 * Method that indicates that all open resources must be closed
	 * and cleaned and that the entire indexing operation has been completed.
	 * @throws IndexerException : In case any error occurs
	 */
	public void close() throws IndexerException {
		//TODO
	}
}
