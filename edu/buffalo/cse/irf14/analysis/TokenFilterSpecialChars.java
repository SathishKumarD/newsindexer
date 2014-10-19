package edu.buffalo.cse.irf14.analysis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TokenFilterSpecialChars extends TokenFilter {

	public TokenFilterSpecialChars(TokenStream stream) {
		super(stream);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean increment() throws TokenizerException {
		try {
			if (!tStream.hasNext())
				return false;
			Token token = tStream.next();
			String str = token.getTermText();
			if(str == null || str.isEmpty())
				return true;
			
			// Pattern p = Pattern.compile("[$&+,:;=?@#|`~()]+");
			//str = str.replaceAll("[^\\sa-zA-Z0-9.-]+", "");
			str = str.replaceAll("[\\p{Punct}&&[^\\.\\-]]", "");
			// Pattern p = Pattern.compile("[a-zA-Z]*-+[a-zA-Z]*");
			Pattern p = Pattern.compile("[^0-9]+-+[^0-9]*");
			Matcher m = p.matcher(str);
			if (m.find()) {
				str = str.replaceAll("[-]+", "");
			}
			str = str.replaceAll("^[.-]+$", "");
			
			//System.out.println(str);
			token.setTermText(str);
		} catch (Exception e) {
			throw new TokenizerException();
		}
		return true;
	}

	@Override
	public TokenStream getStream() {
		// TODO Auto-generated method stub
		return tStream;
	}
}
