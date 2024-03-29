package edu.buffalo.cse.irf14.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import edu.buffalo.cse.util.TrieNode;

/**
 * @author nikhillo Class that emulates reading data back from a written index
 */

/**
 * @author kaush
 *
 */
public class IndexReader {
	/**
	 * Default constructor
	 * 
	 * @param indexDir
	 *            : The root directory from which the index is to be read. This
	 *            will be exactly the same directory as passed on IndexWriter.
	 *            In case you make sub directories etc., you will have to handle
	 *            it accordingly.
	 * @param type
	 *            The {@link IndexType} to read from
	 */

	IndexType type = null;
	String indexDirectory = null;
	IndicesDTO indices = null;

	// Constructors

	public IndexReader() {
	}

	public IndexReader(String indexDir) {
		this.indexDirectory = indexDir;
		loadIndexFromfile();
	}

	public IndexReader(String indexDir, IndexType type) {
		this.type = type;
		this.indexDirectory = indexDir;
		loadIndexFromfile();
	}

	private void loadIndexFromfile() {
		try {
			indices = new IndicesDTO();
			readIndex();
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	/**
	 * Get total number of terms from the "key" dictionary associated with this
	 * index. A postings list is always created against the "key" dictionary
	 * 
	 * @return The total number of terms
	 */
	public int getTotalKeyTerms() {
		int size = 0;
		switch (type) {
		case TERM:
			size = indices.termIndex.size();
			break;
		case CATEGORY:
			size = indices.categoryIndex.size();
			break;
		case AUTHOR:
			size = indices.authorIndex.size();
			break;
		case PLACE:
			size = indices.placeIndex.size();
			break;
		default:
			break;
		}
		return size;
	}

	/**
	 * Get total number of terms from the "value" dictionary associated with
	 * this index. A postings list is always created with the "value" dictionary
	 * 
	 * @return The total number of terms
	 */
	public int getTotalValueTerms() {
		return indices.docIDLookup.size();
	}

	/**
	 * Method to get the postings for a given term. You can assume that the raw
	 * string that is used to query would be passed through the same Analyzer as
	 * the original field would have been.
	 * 
	 * @param term
	 *            : The "analyzed" term to get postings for
	 * @return A Map containing the corresponding fileid as the key and the
	 *         number of occurrences as values if the given term was found, null
	 *         otherwise.
	 * @throws IndexerException
	 */
	public Map<String, Integer> getPostings(String term) {

		HashMap<String, Integer> indexPostings = null;

		if (null != term) {
			switch (type) {
			case TERM:
				if (null != indices.termIndex.get(term))
					indexPostings = generatePostings(indices.termIndex
							.get(term));// .getTermFreqPositionIndexDTO());
				break;
			case CATEGORY:
				if (null != indices.categoryIndex.get(term))
					indexPostings = generatePostings(indices.categoryIndex
							.get(term));// .getTermFreqPositionIndexDTO());
				break;
			case AUTHOR:
				if (null != indices.authorIndex.get(term))
					indexPostings = generatePostings(indices.authorIndex
							.get(term));// .getTermFreqPositionIndexDTO());
				break;
			case PLACE:
				if (null != indices.placeIndex.get(term))
					indexPostings = generatePostings(indices.placeIndex
							.get(term));// .getTermFreqPositionIndexDTO());
				break;
			default:
				break;
			}
		}
		return indexPostings;
	}

	private HashMap<String, Integer> generatePostings(
			HashMap<Integer, String> postings) {

		HashMap<String, Integer> indexPostings = new HashMap<String, Integer>();

		for (Entry<Integer, String> eItr : postings.entrySet()) {
			String docID = indices.docIDLookup.get(eItr.getKey());
			String[] str = eItr.getValue().split(":");
			int termFreq = Integer.parseInt(str[0]);
			indexPostings.put(docID, termFreq);
		}
		return indexPostings;
	}

	/**
	 * Method to get the top k terms from the index in terms of the total number
	 * of occurrences.
	 * 
	 * @param k
	 *            : The number of terms to fetch
	 * @return : An ordered list of results. Must be <=k fr valid k values null
	 *         for invalid k values
	 */
	public List<String> getTopK(int k) {

		if (k <= 0)
			return null;
		List<TrieNode> topk_nodes = new ArrayList<TrieNode>();
		List<String> topKwords = new ArrayList<String>();

		int distinct_word_count = 0;
		int total_word_count = 0;

		for (int p = 0; p < k; p++) {
			topk_nodes.add(indices.root);
		}

		indices.root.GetTopCounts(topk_nodes, distinct_word_count,
				total_word_count);
		Collections.sort(topk_nodes);

		for (TrieNode t : topk_nodes) {
			topKwords.add(t.toString());
		}

		Collections.reverse(topKwords);
		return topKwords;
	}

	/**
	 * Method to implement a simple boolean AND query on the given index
	 * 
	 * @param terms
	 *            The ordered set of terms to AND, similar to getPostings() the
	 *            terms would be passed through the necessary Analyzer.
	 * @return A Map (if all terms are found) containing FileId as the key and
	 *         number of occurrences as the value, the number of occurrences
	 *         would be the sum of occurrences for each participating term.
	 *         return null if the given term list returns no results BONUS ONLY
	 * @throws IndexerException
	 */
	public Map<String, Integer> query(String... terms) {

		List<Map<String, Integer>> hashMaps = new ArrayList<Map<String, Integer>>();
		for (String term : terms) {
			Map<String, Integer> map = getPostings(term);
			if (map != null) {
				hashMaps.add(getPostings(term));
			}

		}
		HashMap<String, Integer> containter = new HashMap<String, Integer>(
				hashMaps.get(0));

		int size = hashMaps.size();
		String key;
		int value;
		for (int i = 1; i < size; i++) {
			containter.keySet().retainAll(hashMaps.get(i).keySet());
		}

		for (int i = 1; i < size; i++) {
			for (Entry<String, Integer> indexEntry : hashMaps.get(i).entrySet()) {
				key = indexEntry.getKey();
				value = indexEntry.getValue();

				if (containter.containsKey(key)) {
					containter.put(key, containter.get(key) + value);
				}
			}
		}

		if (containter.isEmpty()) {
			return null;
		}
		// for (Entry<String, Integer> entry : containter.entrySet())
		// System.out.println(entry.getKey() + " " + entry.getValue());
		return containter;
	}

	private void readIndex() throws IndexerException {
		try {
			String termIndexFilepath = this.indexDirectory + File.separator
					+ IndexType.TERM.toString() + ".txt";
			indices.termIndex = fileToIndex(termIndexFilepath);
			String categoryIndexFilepath = this.indexDirectory + File.separator
					+ IndexType.CATEGORY.toString() + ".txt";
			indices.categoryIndex = fileToIndex(categoryIndexFilepath);
			String authorIndexFilepath = this.indexDirectory + File.separator
					+ IndexType.AUTHOR.toString() + ".txt";
			indices.authorIndex = fileToIndex(authorIndexFilepath);
			String placeIndexFilepath = this.indexDirectory + File.separator
					+ IndexType.PLACE.toString() + ".txt";
			indices.placeIndex = fileToIndex(placeIndexFilepath);
			String docIDLookupFilepath = this.indexDirectory + File.separator
					+ "FILEID" + ".txt";
			indices.docIDLookup = fileToLookup(docIDLookupFilepath);
			String docIDLengthFilepath = this.indexDirectory + File.separator
					+ "DOCLENGTH" + ".txt";
			indices.docLength = fileToDocLength(docIDLengthFilepath);

//			System.out.println("Reader");
//			System.out.println("Term Size : " + indices.termIndex.size());
//			System.out.println("Cate Size : " + indices.categoryIndex.size());
//			System.out.println("Auth Size : " + indices.authorIndex.size());
//			System.out.println("Plac Size : " + indices.placeIndex.size());
//			System.out.println("Dcid Size : " + indices.docIDLookup.size());
//			System.out.println("dcLe Size : " + indices.docLength.size());

		} catch (Exception e) {
			e.printStackTrace();
			throw new IndexerException();
		}
	}

	@SuppressWarnings("unchecked")
	private static HashMap<String, HashMap<Integer, String>> fileToIndex(
			String path) throws IndexerException {

		HashMap<String, HashMap<Integer, String>> map = new HashMap<String, HashMap<Integer, String>>();

		try {
			BufferedReader br = null;

			try {
				br = new BufferedReader(new FileReader(path));
				// System.out.println(path);
				String line;
				line = br.readLine();
				while (line != null) {

					// System.out.println(line);
					if (!line.trim().isEmpty()) {
						String[] hasher = line.split(Pattern.quote("#$%!@*("));
						String key = hasher[0];
						HashMap<Integer, String> posting = new HashMap<Integer, String>();
						// System.out.println(hasher[1]);
						String[] postings = hasher[1]
								.split(Pattern.quote("||"));

						for (String postingStr : postings) {
							String[] freqPosIndex = postingStr.split(Pattern
									.quote("|"));
							posting.put(Integer.parseInt(freqPosIndex[0]),
									freqPosIndex[1]);
						}
						map.put(key, posting);
						// System.out.println(map.size());
					}
					line = br.readLine();
				}

			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					br.close();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new IndexerException();
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	public static HashMap<Integer, String> fileToLookup(String path)
			throws IndexerException {

		HashMap<Integer, String> map = new HashMap<Integer, String>();

		try {
			BufferedReader br = null;

			try {
				br = new BufferedReader(new FileReader(path));
				String line;
				line = br.readLine();
				while (line != null) {
					if (!line.trim().isEmpty()) {

						String[] hasher = line.split(":");
						if (hasher.length < 2) {
							System.out.println("Error buddy: " + hasher[0]);
							System.out.println(path);
						}
						String key = hasher[0];
						String value = hasher[1];
						map.put(Integer.parseInt(key), value);
					}
					line = br.readLine();
				}

			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			} finally {
				if (br != null)
					br.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new IndexerException();
		}
		return map;
	}

	public static HashMap<Integer, Integer> fileToDocLength(String path)
			throws IndexerException {

		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();

		try {
			BufferedReader br = null;

			try {
				br = new BufferedReader(new FileReader(path));
				String line;
				line = br.readLine();
				while (line != null) {
					if (!line.trim().isEmpty()) {
						String[] hasher = line.split(":");
						if (hasher.length < 2) {
							System.out.println("Error buddy: " + hasher[0]);
							System.out.println(path);
						}

						String key = hasher[0];
						String value = hasher[1];
						map.put(Integer.parseInt(key), Integer.parseInt(value));
					}
					line = br.readLine();
				}
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			} finally {
				if (br != null)
					br.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new IndexerException();
		}
		return map;
	}

	public IndicesDTO getIndexDTO() {
		return this.indices;
	}

}
