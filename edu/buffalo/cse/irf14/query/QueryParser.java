/**
 * 
 */
package edu.buffalo.cse.irf14.query;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author nikhillo
 * Static parser that converts raw text to Query objects
 */
public class QueryParser {
	/**
	 * MEthod to parse the given user query into a Query object
	 * @param userQuery : The query to parse
	 * @param defaultOperator : The default operator to use, one amongst (AND|OR)
	 * @return Query object if successfully parsed, null otherwise
	 */
	public static Query parse(String userQuery, String defaultOperator) {

		//TODO: YOU MUST IMPLEMENT THIS METHOD
		Operator defaultOper = getDefaultOperator(defaultOperator);

		if(!ValidateQuery(userQuery))
			return null;


		return null;
	}

	private static boolean ValidateQuery(String userQuery )
	{
		//TODO: 4 types of validation

		// validate brackets (( 

		// validate quotes """

		// validate operators AND OR

		// validate operator followed by closing bracket

		return true;
	}

	private static String GetFormatedQueryString(String userQuery, Operator defaultOper)
	{

		HashMap<String,String> quotedSearchTerms = GetHashMapForQuotedSearchTerms(userQuery);
		userQuery = ReplaceQuotedStringWithHashValues(userQuery,quotedSearchTerms);
		userQuery = AddCategoryToIndividualTermsInsideParanthesis(userQuery);
		userQuery = AddParanthesis(userQuery);
		userQuery = AddDefaultOperators(userQuery,defaultOper);
		userQuery = AddDefaultIndex(userQuery);
		userQuery = ReplaceParanthesisWithSquareBrackets(userQuery);
		userQuery = EncloseWithCurlyBrackets(userQuery);
		userQuery = ReplaceHashValuesWithQuotedString(userQuery,quotedSearchTerms);


		return userQuery;
	}

	private static String ReplaceHashValuesWithQuotedString(String userQuery,
			HashMap<String, String> quotedSearchTerms) {

		for (Map.Entry<String, String> entry : quotedSearchTerms.entrySet())
		{
			userQuery = userQuery.replace(entry.getKey(), entry.getValue() );

		}

		return userQuery;
	}

	private static String ReplaceQuotedStringWithHashValues(String userQuery,
			HashMap<String, String> quotedSearchTerms) {

		for (Map.Entry<String, String> entry : quotedSearchTerms.entrySet())
		{
			userQuery = userQuery.replace(entry.getValue(), entry.getKey());

		}

		return userQuery;
	}

	private static HashMap<String,String> GetHashMapForQuotedSearchTerms(String userQuery)
	{
		// #1 GUID replacement for quoted strings 
		// replace "hi quote" with a random UUID like 38400000-8cf0-11bd-b23e-10b96e4ef00d


		HashMap<String,String> quotedSearchTerms = new HashMap<String,String>(); 

		String RegexQuotesString = "([\"'])(?:\\\\\\1|.)*?\\1";


		Pattern p = Pattern.compile(RegexQuotesString);
		Matcher m = p.matcher(userQuery);
		while(m.find()) {

			String quotedSearchTem = m.group(0);
			String guid = UUID.randomUUID().toString();
			quotedSearchTerms.put(guid, quotedSearchTem);
			//userQuery = userQuery.replaceFirst(RegexQuotesString, guid);

		}

		return quotedSearchTerms;

	}

	private static String EncloseWithCurlyBrackets(String userQuery) {
		userQuery = "{"+userQuery+"}";
		return null;
	}

	private static String ReplaceParanthesisWithSquareBrackets(String userQuery) {
		// TODO Auto-generated method stub
		return null;
	}

	private static String AddDefaultIndex(String userQuery) {
		// TODO Auto-generated method stub

		return userQuery;
	}

	private static String AddParanthesis(String userQuery)
	{
		// AND prisoners detainees rebels
		// will be converted to AND (prisoners detainees rebels)
		String[] tokens = userQuery.split("[AND|OR|NOT]");
		if(tokens.length >1)
		{
			for(String word:tokens) 
			{
				if(word.split(" ").length >1
						&& !word.contains("(")
						&& !word.contains(")")
						)
				{
					String paranthesisEnclosedWords = "("+ word + ")";
					userQuery = userQuery.replace(word, paranthesisEnclosedWords);

				}
			}
		}
		return userQuery;

	}

	private static String AddDefaultOperators(String userQuery, Operator defaultOperator)
	{
		String[] tokens = userQuery.split("[AND|OR|NOT]");
		for(String word:tokens) 
		{
			String queryWithDefaultOperator = String.join(defaultOperator.toString(), word.split(" "));
			userQuery = userQuery.replace(word, queryWithDefaultOperator);
		}

		return userQuery;

	}

	private static String AddCategoryToIndividualTermsInsideParanthesis(String userQuery)
	{
		// #2 Category Normalization
		// if :( is found in the user query we need to replace
		// the content inside brackets with categories
		// Category:(movies AND crime) will be changed to (Category:movies AND Category:crime)


		//String termsToInsertIndexRegex = ":\\((.*?)\\)";
		String extractIndexTypeRegex = "(Author|Category|Term|Place):\\(((?:[^\\)])+)";
		Pattern p = Pattern.compile(extractIndexTypeRegex);
		Matcher m = p.matcher(userQuery);


		while(m.find()) {

			String searchTerms = m.group(2); // movies AND crime
			String indexType = m.group(1);


			String QueryTermExtractorRegex = "\\s(?!AND|OR|NOT)(\\w+)";
			Pattern p2 =  Pattern.compile(QueryTermExtractorRegex);
			Matcher m2 = p2.matcher(searchTerms);

			while(m2.find())
			{ 
				String queryterm = m2.group(1);
				searchTerms = searchTerms.replace(searchTerms, indexType+":"+queryterm);
				//m2 = p2.matcher(searchTerms);
			}


			userQuery = userQuery.replaceFirst(m.group(0), "(" + searchTerms+")");
			//m = p.matcher(userQuery);
		}

		return userQuery;


	}

	private static Operator getDefaultOperator(String defaultOperator)
	{
		Operator defaultOper = Operator.OR;

		if(defaultOperator!=null && defaultOperator.toLowerCase()== "and")
		{
			defaultOper = Operator.AND;
		}

		return defaultOper;
	}
}
