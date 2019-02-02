package com.luacraft.classes;

import java.util.ArrayList;
import java.util.List;

public class CommandLineParser {
	public static List<String> parse(String data) {
	    List<String> tokens = new ArrayList<String>();
	    StringBuilder sb = new StringBuilder();
	    boolean insideQuote = false;
	    char previous = '\0';

	    for (char ch : data.toCharArray()) {
	        if (ch == ' ' && !insideQuote) {
	            if (sb.length() > 0 && previous != '"')
	                addTokenAndResetBuilder(sb, tokens);
	        } else if (ch == '"') {
	            if (insideQuote) {
	                sb.append(ch);
	                addTokenAndResetBuilder(sb, tokens);
	            } else {
	                addTokenAndResetBuilder(sb, tokens);
	                sb.append(ch);
	            }
	            insideQuote = !insideQuote;
	        } else {
	            sb.append(ch);
	        }
	        previous = ch;
	    }
	    addTokenAndResetBuilder(sb, tokens);

	    return tokens;
	}

	private static void addTokenAndResetBuilder(StringBuilder sb, List<String> list) {
	    if (sb.length() > 0) {
	        list.add(sb.toString());
	        sb.delete(0, sb.length());
	    }
	}
}
