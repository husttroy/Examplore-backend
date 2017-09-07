package edu.cs.ucla.preprocess;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.cs.ucla.model.APICall;
import edu.cs.ucla.model.ControlConstruct;
import edu.cs.ucla.model.Item;

public class ProcessUtils {
	// match < and > in API names to handle constructors of parameterized types
	static final Pattern METHOD_CALL = Pattern
				.compile("((new )?[a-zA-Z0-9_<>]+)\\(((.+),)*\\)");
		
	public static ArrayList<String> splitByArrow(String s) {
		ArrayList<String> ss = new ArrayList<String>();
		char[] chars = s.toCharArray();
		StringBuilder sb = new StringBuilder();
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		for(int i = 0; i < chars.length; i++) {
			char cur = chars[i];
			if(cur == '"' && i > 0 && chars[i-1] == '\\') {
				// count the number of backslashes
				int count = 0;
				while(i - count - 1 >= 0) {
					if(chars[i - count - 1] == '\\') {
						count ++;
					} else {
						break;
					}
				} 
				if(count % 2 == 0) {
					// escape one or more backslashes instead of this quote, end of quote
					// double quote ends
					inDoubleQuote = false;
					sb.append(cur);
				} else {
					// escape quote, not the end of the quote
					sb.append(cur);
				}
			} else if(cur == '"' && !inSingleQuote && !inDoubleQuote) {
				// double quote starts
				inDoubleQuote = true;
				sb.append(cur);
			} else if (cur == '\'' && i > 0 && chars[i-1] == '\\') {
				// count the number of backslashes
				int count = 0;
				while(i - count - 1 >= 0) {
					if(chars[i - count - 1] == '\\') {
						count ++;
					} else {
						break;
					}
				} 
				if(count % 2 == 0) {
					// escape one or more backslashes instead of this quote, end of quote
					// single quote ends
					inSingleQuote = false;
					sb.append(cur);
				} else {
					// escape single quote, not the end of the quote
					sb.append(cur);
				}
			} else if(cur == '\'' && !inDoubleQuote && !inSingleQuote) {
				// single quote starts
				inSingleQuote = true;
				sb.append(cur);
			} else if(cur == '"' && !inSingleQuote && inDoubleQuote) {
				// quote ends
				inDoubleQuote = false;
				sb.append(cur);
			} else if (cur == '\'' && inSingleQuote && !inDoubleQuote) {
				// single quote ends
				inSingleQuote = false;
				sb.append(cur);
			} else if (cur == '-' && i + 1 < chars.length && chars[i + 1] == '>' && !inSingleQuote && !inDoubleQuote) {
				i++;
				if(sb.length() > 0) {
					// push previous concatenated chars to the array
					ss.add(sb.toString());
					// clear the string builder
					sb.setLength(0);
				}
			} else {
				sb.append(cur);
			}
		}
		
		// push the last token if any
		if(sb.length() > 0) {
			ss.add(sb.toString());
		}
		
		return ss;
	}
	
	public static boolean isBalanced(String expr) {
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		int parentheses = 0;
		char[] chars = expr.toCharArray();
		for(int i = 0; i < chars.length; i++) {
			char cur = chars[i];
			if(cur == '"' && i > 0 && chars[i-1] == '\\') {
				// count the number of backslashes
				int count = 0;
				while(i - count - 1 >= 0) {
					if(chars[i - count - 1] == '\\') {
						count ++;
					} else {
						break;
					}
				} 
				if(count % 2 == 0) {
					// escape one or more backslashes instead of this quote, end of quote
					// double quote ends
					inDoubleQuote = false;
				} else {
					// escape quote, not the end of the quote
				}
			} else if(cur == '"' && !inSingleQuote && !inDoubleQuote) {
				// double quote starts
				inDoubleQuote = true;
			} else if(cur == '\'' && !inSingleQuote && !inDoubleQuote) {
				// single quote starts
				inSingleQuote = true;
			} else if (cur == '\'' && i > 0 && chars[i-1] == '\\') {
				// count the number of backslashes
				int count = 0;
				while(i - count - 1 >= 0) {
					if(chars[i - count - 1] == '\\') {
						count ++;
					} else {
						break;
					}
				} 
				if(count % 2 == 0) {
					// escape one or more backslashes instead of this quote, end of quote
					// single quote ends
					inSingleQuote = false;
				} else {
					// escape single quote, not the end of the quote
				}
			} else if(cur == '"' && !inSingleQuote && inDoubleQuote) {
				// double quote ends
				inDoubleQuote = false;
			} else if (cur == '\'' && inSingleQuote && !inDoubleQuote) {
				// single quote ends
				inSingleQuote = false;
			} else if (inSingleQuote || inDoubleQuote) {
				// ignore all parentheses in quote
			} else if (cur == '(') {
				parentheses ++;
			} else if (cur == ')') {
				parentheses --;
				if(parentheses < 0) {
					return false;
				}
			}
		}
		
		return parentheses == 0;
	}
	
	public static int findFirstUnbalancedCloseParenthesis(String expr) {
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		int parentheses = 0;
		char[] chars = expr.toCharArray();
		for(int i = 0; i < chars.length; i++) {
			char cur = chars[i];
			if(cur == '"' && i > 0 && chars[i-1] == '\\') {
				// count the number of backslashes
				int count = 0;
				while(i - count - 1 >= 0) {
					if(chars[i - count - 1] == '\\') {
						count ++;
					} else {
						break;
					}
				} 
				if(count % 2 == 0) {
					// escape one or more backslashes instead of this quote, end of quote
					// double quote ends
					inDoubleQuote = false;
				} else {
					// escape quote, not the end of the quote
				}
			} else if(cur == '"' && !inSingleQuote && !inDoubleQuote) {
				// double quote starts
				inDoubleQuote = true;
			} else if(cur == '\'' && !inSingleQuote && !inDoubleQuote) {
				// single quote starts
				inSingleQuote = true;
			} else if (cur == '\'' && i > 0 && chars[i-1] == '\\') {
				// count the number of backslashes
				int count = 0;
				while(i - count - 1 >= 0) {
					if(chars[i - count - 1] == '\\') {
						count ++;
					} else {
						break;
					}
				} 
				if(count % 2 == 0) {
					// escape one or more backslashes instead of this quote, end of quote
					// single quote ends
					inSingleQuote = false;
				} else {
					// escape single quote, not the end of the quote
				}
			} else if(cur == '"' && !inSingleQuote && inDoubleQuote) {
				// double quote ends
				inDoubleQuote = false;
			} else if (cur == '\'' && inSingleQuote && !inDoubleQuote) {
				// single quote ends
				inSingleQuote = false;
			} else if (inSingleQuote || inDoubleQuote) {
				// ignore all parentheses in quote
			} else if (cur == '(') {
				parentheses ++;
			} else if (cur == ')') {
				parentheses --;
				if(parentheses == -1) {
					return i;
				}
			}
		}
		
		// do not find the first unbalanced close parenthesis
		return -1;
	}
	
	public static int findFirstUnbalancedOpenParenthesis(String expr) {
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		Stack<Integer> stack = new Stack<Integer>();
		char[] chars = expr.toCharArray();
		for(int i = 0; i < chars.length; i++) {
			char cur = chars[i];
			if(cur == '"' && i > 0 && chars[i-1] == '\\') {
				// count the number of backslashes
				int count = 0;
				while(i - count - 1 >= 0) {
					if(chars[i - count - 1] == '\\') {
						count ++;
					} else {
						break;
					}
				} 
				if(count % 2 == 0) {
					// escape one or more backslashes instead of this quote, end of quote
					// double quote ends
					inDoubleQuote = false;
				} else {
					// escape quote, not the end of the quote
				}
			} else if(cur == '"' && !inSingleQuote && !inDoubleQuote) {
				// double quote starts
				inDoubleQuote = true;
			} else if(cur == '\'' && !inSingleQuote && !inDoubleQuote) {
				// single quote starts
				inSingleQuote = true;
			} else if (cur == '\'' && i > 0 && chars[i-1] == '\\') {
				// count the number of backslashes
				int count = 0;
				while(i - count - 1 >= 0) {
					if(chars[i - count - 1] == '\\') {
						count ++;
					} else {
						break;
					}
				} 
				if(count % 2 == 0) {
					// escape one or more backslashes instead of this quote, end of quote
					// single quote ends
					inSingleQuote = false;
				} else {
					// escape single quote, not the end of the quote
				}
			} else if(cur == '"' && !inSingleQuote && inDoubleQuote) {
				// double quote ends
				inDoubleQuote = false;
			} else if (cur == '\'' && inSingleQuote && !inDoubleQuote) {
				// single quote ends
				inSingleQuote = false;
			} else if (inSingleQuote || inDoubleQuote) {
				// ignore all parentheses in quote
			} else if (cur == '(') {
				stack.push(i);
			} else if (cur == ')') {
				stack.pop();
			}
		}
		
		// do not find the first unbalanced close parenthesis
		if(!stack.isEmpty()) {
			return stack.pop();
		} else {
			return -1;
		}
	}

	public static boolean isInQuote(String s, int index) {
		if(s.contains("\"") || s.contains("'")) {
			char[] chars = s.toCharArray();
			boolean inSingleQuote = false;
			boolean inDoubleQuote = false;
			for(int i = 0; i < chars.length; i++) {
				if(i == index) {
					return inSingleQuote || inDoubleQuote;
				}
				char cur = chars[i];
				if(cur == '"' && i > 0 && chars[i-1] == '\\') {
					// count the number of backslashes
					int count = 0;
					while(i - count - 1 >= 0) {
						if(chars[i - count - 1] == '\\') {
							count ++;
						} else {
							break;
						}
					} 
					if(count % 2 == 0) {
						// escape one or more backslashes instead of this quote, end of quote
						// double quote ends
						inDoubleQuote = false;
					} else {
						// escape quote, not the end of the quote
					}
				} else if(cur == '"' && !inSingleQuote && !inDoubleQuote) {
					// double q103uote starts
					inDoubleQuote = true;
				} else if (cur == '\'' && i > 0 && chars[i-1] == '\\') {
					// count the number of backslashes
					int count = 0;
					while(i - count - 1 >= 0) {
						if(chars[i - count - 1] == '\\') {
							count ++;
						} else {
							break;
						}
					} 
					if(count % 2 == 0) {
						// escape one or more backslashes instead of this quote, end of quote
						// single quote ends
						inSingleQuote = false;
					} else {
						// escape single quote, not the end of the quote
					}
				} else if(cur == '\'' && !inSingleQuote && !inDoubleQuote) {
					// single quote starts
					inSingleQuote = true; 
				} else if(cur == '"' && !inSingleQuote && inDoubleQuote) {
					// double quote ends
					inDoubleQuote = false;
				} else if (cur == '\'' && inSingleQuote && !inDoubleQuote) {
					// single quote ends
					inSingleQuote = false;
				}
			}
			
			return inSingleQuote;
		} else {
			return false;
		}
	}
	
	public static ArrayList<String> getArguments(String args) {
		ArrayList<String> list = new ArrayList<String>();
		boolean inQuote = false;
		int stack = 0;
		StringBuilder sb = new StringBuilder();
		char[] chars = args.toCharArray();
		for(int i = 0; i < chars.length; i++) {
			char cur = chars[i];
			if(cur == '"' && i > 0 && chars[i-1] == '\\') {
				// count the number of backslashes
				int count = 0;
				while(i - count - 1 >= 0) {
					if(chars[i - count - 1] == '\\') {
						count ++;
					} else {
						break;
					}
				} 
				if(count % 2 == 0) {
					// escape one or more backslashes instead of this quote, end of quote
					// quote ends
					inQuote = false;
					sb.append(cur);
				} else {
					// escape quote, not the end of the quote
					sb.append(cur);
				}
			} else if(cur == '"' && !inQuote) {
				// quote starts
				inQuote = true;
				sb.append(cur);
			} else if (cur == '\'' && i > 0 && chars[i-1] == '\\') {
				// count the number of backslashes
				int count = 0;
				while(i - count - 1 >= 0) {
					if(chars[i - count - 1] == '\\') {
						count ++;
					} else {
						break;
					}
				} 
				if(count % 2 == 0) {
					// escape one or more backslashes instead of this quote, end of quote
					// quote ends
					inQuote = false;
					sb.append(cur);
				} else {
					// escape single quote, not the end of the quote
					sb.append(cur);
				}
			} else if(cur == '\'' && !inQuote) {
				// single quote starts
				inQuote = true;
				sb.append(cur);
			} else if(cur == '"' && inQuote) {
				// quote ends
				inQuote = false;
				sb.append(cur);
			} else if (cur == '\'' && inQuote) {
				// single quote ends
				inQuote = false;
				sb.append(cur);
			} else if (cur == '(' && !inQuote) {
				// look behind to check if this is a method call
				sb.append(cur);
				stack ++;
			} else if (cur == ')' && !inQuote) {
				sb.append(cur);
				stack --;
			} else if (inQuote || stack != 0) {
				// ignore any separator in quote or in a method call
				sb.append(cur);
			} else if (cur == ',' && !inQuote && stack == 0){
				if(sb.length() > 0) {
					list.add(sb.toString());
					sb.setLength(0);
				} else {
					sb.append(cur);
				}
			} else {
				sb.append(cur);
			}
		}
		
		// push the last token if any
		if(sb.length() > 0) {
			list.add(sb.toString());
		}
		
		return list;
	}
	
	public static String readRawSequenceById(String id, String path) {
		String seq = null;
		try (BufferedReader br = new BufferedReader(new FileReader(new File(path)))){
			String line = null;
			while((line = br.readLine()) != null) {
				if(line.contains(id)) {
					seq = line.substring(line.indexOf("] =") + 3).trim();
					break;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return seq;
	}
	
	public static String[] splitByAt(String s) {
		ArrayList<String> ss = new ArrayList<String>();
		String[] arr = s.split("@");
		int index = 0;
		for(int i = 0; i < arr.length; i++) {
			String item = arr[i];
			if(!ProcessUtils.isInQuote(s, index)) {
				ss.add(item);
			} else {
				String last = ss.get(ss.size() - 1);
				ss.remove(ss.size() - 1);
				ss.add(last + "@" + item);
			}
			index += item.length() + 1;
		}
		
		if(ss.size() == 1) {
			ss.add("true");
		}
		
		String[] arr2 = new String[ss.size()];
		return ss.toArray(arr2);
	}
	
	public static boolean isAnnotatedWithType(String s) {
		if(!s.contains(":")) {
			return false;
		}
		
		int index = s.lastIndexOf(':');
		for(int i = index + 1; i < s.length(); i++) {
			char c = s.charAt(index);
			if((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '[' || c == ']' || c == '<' || c == '>') {
				continue;
			} else {
				return false;
			}
		}
		
		return true;
	}
	
	public static ArrayList<String> stripOffArguments(ArrayList<String> pattern) {
		ArrayList<String> patternWithoutArguments = new ArrayList<String>();
		for(String element : pattern) {
			if(element.contains("(") && element.contains(")")) {
				// this is an API call
				String apiName = element.substring(0, element.indexOf('('));
				patternWithoutArguments.add(apiName);
			} else {
				patternWithoutArguments.add(element);
			}
		}
		return patternWithoutArguments;
	}
	
	public static ArrayList<Item> extractItems(String expr, HashMap<String, String> symbol_table) {
		ArrayList<Item> items = new ArrayList<Item>();

		// check if it is a control-flow construct
		String s = expr.trim();
		if (s.equals("IF {") || s.equals("ELSE {") || s.equals("TRY {")
				|| s.equals("LOOP {")
				|| s.equals("FINALLY {")) {
			ControlConstruct cc = new ControlConstruct(s, null);
			items.add(cc);
			return items;
		} else if (s.contains("CATCH(") && s.endsWith(") {")) {
			String type = s.substring(s.lastIndexOf("CATCH(") + 6, s.lastIndexOf(") {"));
			ControlConstruct cc = new ControlConstruct("CATCH", type);
			items.add(cc);
			return items;
		}

		// split by @
		String[] ss = ProcessUtils.splitByAt(s);
		String predicate = null;
		String item = ss[0];
		if(ss[1].trim().isEmpty()) {
			predicate = "true";
		} else {
			predicate = ss[1];
		}

		// pre-check to avoid unnecessary pattern matching for the performance
		// purpose
		if (item.contains("(") && item.contains(")")) {
			// extract method calls
			return extractMethodCalls(item, item, symbol_table, predicate);
		} else {
			// no method call, skip
			return items;
		}
	}

	private static ArrayList<Item> extractMethodCalls(String expr, String original, HashMap<String, String> symbol_table, String predicate) {
		// check for return value
		int pos = hasReturnValue(expr);
		String retVal = null;
		if(pos != -1) {
			// the return value of this method call has been assigned to a variable
			retVal = expr.substring(0, pos);
		}
		
		ArrayList<Item> items = new ArrayList<Item>();
		Matcher m = METHOD_CALL.matcher(expr);
		while (m.find()) {
			String apiName = m.group(1);
			String args = m.group(3);
			String rest = null;
			ArrayList<String> arguments = new ArrayList<String>();
			if (args != null) {
				// check whether this is a chained method call by checking whether the argument is balanced
				if(!ProcessUtils.isBalanced(args)) {
					// this is a call chain
					// the regex cannot handle the method calls properly if one method call
					// after the first one in the chain contains arguments
					// the following method calls with arguments will be considered as the
					// argument of the first one
					int position = ProcessUtils.findFirstUnbalancedCloseParenthesis(args);
					if(position == -1) {
						// something goes wrong, return empty list
						return new ArrayList<Item>();
					} else {
						// adjust the string of the argument list
						String newArgs = args.substring(0, position);
						if(position + 2 <= args.length()) {
							rest = args.substring(position + 2) + ")";
						}
						args = newArgs;
					}
				}
				
				arguments = ProcessUtils.getArguments(args);
				
				// this api call has arguments
				ArrayList<Item> apis2 = extractMethodCalls(args, original, symbol_table, predicate);
				items.addAll(apis2);
				
				// then add this API call
				String signature = apiName + "(";
				for(String argument : arguments) {
					if(argument.contains(":")) {
						argument = argument.substring(0, argument.lastIndexOf(':'));
					}
					
					if(symbol_table.containsKey(argument)) {
						signature += symbol_table.get(argument);
					} else {
						String tmp = resolveAsPrimitiveType(argument);
						if (tmp != null) {
							signature += tmp;
						} else {
							signature += "*";
						}
					}
					signature += ",";
				}
				
				if(arguments.size() > 0) {
					signature = signature.substring(0, signature.length() - 1) + ")";
				} else {
					signature = signature + ")";
				}
				
				String rcv = getReceiver(original, apiName);
				String condition = getNormalizedPredicate(predicate, rcv, arguments, null);
				APICall apiCall;
				if(retVal != null) {
					apiCall = new APICall(signature, predicate, condition, rcv, arguments, retVal); 
				} else {
					// if there is no return value, use the call expression itself as a return value
					apiCall = new APICall(signature, predicate, condition, rcv, arguments, (rcv==null?"":(rcv+"."))+apiName+"("+args+")");
				}
				
				items.add(apiCall);
				
				// then process the rest of the API calls in the chain (if any)
				if(rest != null) {
					ArrayList<Item> apis3 = extractMethodCalls(rest, original, symbol_table, predicate);
					items.addAll(apis3);
				}
			} else {
				// a single method call with no argument or chained method call
				String rcv = getReceiver(original, apiName);
				
				// get the predicate of this method call
				String condition = getNormalizedPredicate(predicate, rcv, new ArrayList<String>(), null);
				APICall apiCall;
				if(retVal != null) {
					apiCall = new APICall(apiName + "()", predicate, condition, rcv, new ArrayList<String>(), retVal); 
				} else {
					// if there is no return value, use the call expression itself as a return value
					apiCall = new APICall(apiName + "()", predicate, condition, rcv, new ArrayList<String>(), (rcv==null?"":(rcv+"."))+apiName+"()");
				}
				 
				items.add(apiCall);
			}
		}
		return items;
	}

	private static String resolveAsPrimitiveType(String argument) {
		argument = argument.trim();
		if(argument.startsWith("\"") || argument.endsWith("\"")) {
			return "String";
		} else if (isInteger(argument)){
			return "int";
		} else if (argument.equals("true") || argument.equals("false")) {
			return "boolean";
		} else if (isDouble(argument)) {
			return "double";
		} else if (argument.startsWith("'") || argument.endsWith("'")) {
			return "char";
		}
 		return null;
	}

	private static boolean isInteger(String str) {
	    if (str == null) {
	        return false; 
	    } 
	    int length = str.length();
	    if (length == 0) {
	        return false; 
	    } 
	    int i = 0;
	    if (str.charAt(0) == '-') {
	        if (length == 1) {
	            return false; 
	        } 
	        i = 1;
	    } 
	    for (; i < length; i++) {
	        char c = str.charAt(i);
	        if (c < '0' || c > '9') {
	            return false; 
	        } 
	    } 
	    return true; 
	}
	
	private static boolean isDouble(String str) {
	    if (str == null) {
	        return false; 
	    } 
	    int length = str.length();
	    if (length == 0) {
	        return false; 
	    } 
	    int i = 0;
	    if (str.charAt(0) == '-') {
	        if (length == 1) {
	            return false; 
	        } 
	        i = 1;
	    } 
	    for (; i < length; i++) {
	        char c = str.charAt(i);
	        if ((c < '0' || c > '9') && c != '.') {
	            return false; 
	        } 
	    }
	    
	    return str.contains("."); 
	}
	
	public static int hasReturnValue(String expr) {
		char[] cs = expr.toCharArray();
		for(int i = 0; i < cs.length; i++) {
			char c = cs[i];
			if((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
				// continue
				continue;
			} else if (c == '=' && cs[i+1] != '=') {
				// stop 
				return i;
			} else {
				return -1;
			}
		}
		
		return -1;
	}
	
	
	
	/**
	 * 
	 * Note!!! Should always pass the original expression to this method (not the truncated one). 
	 * 
	 * @param expr
	 * @param apiName
	 * @return
	 */
	public static String getReceiver(String expr, String apiName) {
		String receiver = null;
		if (!apiName.startsWith("new ")) {
			// make sure this is not a call to class constructor since class
			// constructors do not have receivers
			int index = expr.indexOf(apiName);
			String sub = expr.substring(0, index);
			if (sub.endsWith(".")) {
				// make sure it is not a call to local method since local method
				// calls also do not have receivers
				if (sub.startsWith("return ")) {
					// return statement
					receiver = sub.substring(7, sub.length() - 1);
				} else if (sub.matches("[a-zA-Z0-9_]+=.+")) {
					// assignment statement
					receiver = sub.substring(sub.indexOf("=") + 1,
							sub.length() - 1);
				} else {
					// regular method call
					receiver = sub.substring(0, sub.length() - 1);
				}

				// strip off any type casting of the return value before the receiver
				// but be careful and do not strip off the type casting in the receiver
				receiver = receiver.trim();
				if (receiver.matches("^\\([a-zA-Z0-9_\\.<>\\?\\s]+\\).+$")) {
					receiver = receiver.substring(receiver.indexOf(')') + 1);
					receiver = receiver.trim();
				}
				
				if(!ProcessUtils.isBalanced(receiver)) {
					// truncate the expression starting from the first unbalanced open parenthesis
					int pos = ProcessUtils.findFirstUnbalancedOpenParenthesis(receiver);
					if(pos != -1) {
						receiver = receiver.substring(pos + 1);
					}
				}
				receiver = SAT.stripUnbalancedParentheses(receiver);
				
				if(receiver.contains(",") && !(receiver.contains("(") && receiver.contains(")"))) {
					// I saw a receive like 'a,b' in an expression 'bar(a,b.foo())' when extracting the
					// receiver of foo
					receiver = receiver.substring(receiver.lastIndexOf(",") + 1);
				}
			}
		}
		return receiver;
	}
	
	public static String getNormalizedPredicate(String predicate, String rcv, ArrayList<String> args, String ret) {
		HashSet<String> relevant_elements = new HashSet<String>();
		if (rcv != null && !rcv.isEmpty()) {
			relevant_elements.add(rcv);
		}
		relevant_elements.addAll(args);
		if (ret != null && !ret.isEmpty()) {
			relevant_elements.add(ret);
		}

		// remove irrelevant clauses
		String conditioned_predicate;
		if(!predicate.equals("true")) {
			conditioned_predicate = condition(relevant_elements,predicate);
		} else {
			conditioned_predicate = "true";
		}
				
		// normalize names
		// declare temporary variables to fit the API
		ArrayList<String> temp1 = new ArrayList<String>();
		if (rcv != null && !rcv.isEmpty()) {
			temp1.add(rcv);
		}
		ArrayList<ArrayList<String>> temp2 = new ArrayList<ArrayList<String>>();
		temp2.add(args);
		ArrayList<String> temp3 = new ArrayList<String>();
		if (ret != null && !ret.isEmpty()) {
			temp3.add(ret);
		}

		String normalized_predicate;
		if(!conditioned_predicate.equals("true")) {
			normalized_predicate = normalize(conditioned_predicate,
					temp1, temp2, temp3);
		} else {
			normalized_predicate = "true";
		}
		
		normalized_predicate = normalized_predicate.replaceAll("true\\s*&&", "");
		normalized_predicate = normalized_predicate.replaceAll("&&\\s*true", "");
		normalized_predicate = normalized_predicate.trim();
		
		
		if(normalized_predicate.isEmpty()) {
			normalized_predicate = "true";
		}
		
		if(normalized_predicate.startsWith("&&")) {
			normalized_predicate = normalized_predicate.substring(2).trim();
		}
		
		return normalized_predicate;
	}
	
	public static String condition(Set<String> vars, String predicate) {
		if(predicate.contains("?")) {
			return "conditional";
		}
		
		if(predicate.contains(">>") || predicate.contains("<<")) {
			return "bitshift";
		}
		
		if(predicate.matches("^.*(?<!\\|)\\|(?!\\|).*$") || predicate.matches("^.*(?<!&)&(?!&).*$")) {
			return "bitwise";
		}
		
		// normalize the use of assignment in the middle of a predicate as the assigned variable
		predicate = replaceAssignment(predicate);
		
		String[] arr = splitOutOfQuote(predicate);
		String res = predicate;
		for (String c : arr) {
			c = c.trim();
			if (c.isEmpty() || c.equals("(") || c.equals(")")) {
				continue;
			} else {
				c = SAT.stripUnbalancedParentheses(c);

				if(c.isEmpty()) continue;
				
				boolean flag = false;
				for (String var : vars) {
					if (containsVar(var, c, 0)) {
						flag = true;
					}
				}

				if (!flag) {
					// this clause is irrelevant
					res = conditionClause(c, res);
				}
			}
		}
		
		// a && !b | a ==> a && !true, which is always evaluated to false
		// Such conditioning  is incomplete because !b should be replaced with true instead of b 
		// So we add the following replacement statement to replace !true with true
		// we also need to handle cases such as !(true && true), !(true || true), !(true || true && true), etc.
		while(res.matches("^.*true(\\s)*&&(\\s)*true.*$") || res.matches("^.*true(\\s)*\\|\\|(\\s)*true.*$") || res.matches("^.*\\!true.*$") || res.matches("^.*\\!\\(true\\).*$")) {
			if(res.matches("^.*true(\\s)*&&(\\s)*true.*$")) {
				res = res.replaceAll("true(\\s)*&&(\\s)*true", "true");
			} else if (res.matches("^.*true(\\s)*\\|\\|(\\s)*true.*$")) {
				res = res.replaceAll("true(\\s)*\\|\\|(\\s)*true", "true");
			} else if(res.matches("^.*\\!true.*$")) {
				res = res.replaceAll("\\!true", "true");
			} else {
				res = res.replaceAll("\\!\\(true\\)", "true");
			}
		}
		
		return res;
	}
	
	public static String replaceAssignment(String predicate) {
		if(predicate.matches("^.+(?<!(=|\\!|>|<))=(?!=).+$")){
			// this algorithm is based on one observation that an assignment sub-expression must be wrapped with parentheses in a boolean expression
			char[] chars = predicate.toCharArray();
			Stack<Integer> stack = new Stack<Integer>();
			int snapshot = -1;
			int assignment_index = -1;
			boolean inSingleQuote = false;
			boolean inDoubleQuote = false;
			ArrayList<Point> ranges = new ArrayList<Point>();
			for(int i = 0; i < chars.length; i++) {
				char cur = chars[i];
				if(cur == '"' && i > 0 && chars[i-1] == '\\') {
					// count the number of backslashes
					int count = 0;
					while(i - count - 1 >= 0) {
						if(chars[i - count - 1] == '\\') {
							count ++;
						} else {
							break;
						}
					} 
					if(count % 2 == 0) {
						// escape one or more backslashes instead of this quote, end of quote
						// double quote ends
						inDoubleQuote = false;
					} else {
						// escape quote, not the end of the quote
					}
				} else if(cur == '"' && !inSingleQuote && !inDoubleQuote) {
					// double quote starts
					inDoubleQuote = true;
				} else if (cur == '\'' && i > 0 && chars[i-1] == '\\') {
					// count the number of backslashes
					int count = 0;
					while(i - count - 1 >= 0) {
						if(chars[i - count - 1] == '\\') {
							count ++;
						} else {
							break;
						}
					} 
					if(count % 2 == 0) {
						// escape one or more backslashes instead of this quote, end of quote
						// single quote ends
						inSingleQuote = false;
					} else {
						// escape single quote, not the end of the quote
					}
				} else if(cur == '\'' && !inSingleQuote && !inDoubleQuote) {
					// single quote starts
					inSingleQuote = true; 
				} else if(cur == '"' && !inSingleQuote && inDoubleQuote) {
					// double quote ends
					inDoubleQuote = false;
				} else if (cur == '\'' && inSingleQuote && !inDoubleQuote) {
					// single quote ends
					inSingleQuote = false;
				} else if (inSingleQuote || inDoubleQuote) {
					// ignore any separator in quote
				} else if (cur == '=') {
					if(i + 1 < chars.length && chars[i+1] == '=') {
						// equal operator, ignore
						i++;
					} else if (i -1 >= 0 && (chars[i-1] == '!' || chars[i-1] == '<' || chars[i-1] == '>')) {
						// not equal operator, ignore
					} else {
						// assignment operator, stack size must be at least 1
						snapshot = stack.size();
						assignment_index = i;
					}
				} else if (cur == '(') {
					stack.push(i);
				} else if (cur == ')') {
					stack.pop();
					if(stack.size() == snapshot - 1) {
						ranges.add(new Point(assignment_index, i));
						// reset
						snapshot = -1;
						assignment_index = -1;
					}
				}
			}
			
			// remove whatever in the range list
			String rel = "";
			int cur = 0;
			for(Point p : ranges) {
				rel += predicate.substring(cur, p.x);
				cur = p.y;
			}
			
			if(cur <= predicate.length()) {
				rel += predicate.substring(cur);
			}
			
			return rel;
		} else {
			return predicate;
		}
	}

	public static String[] splitOutOfQuote(String s) {
		ArrayList<String> tokens = new ArrayList<String>();
		char[] chars = s.toCharArray();
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		int inArgList = 0;
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < chars.length; i++) {
			char cur = chars[i];
			if(cur == '"' && i > 0 && chars[i-1] == '\\') {
				// count the number of backslashes
				int count = 0;
				while(i - count - 1 >= 0) {
					if(chars[i - count - 1] == '\\') {
						count ++;
					} else {
						break;
					}
				} 
				if(count % 2 == 0) {
					// escape one or more backslashes instead of this quote, end of quote
					// double quote ends
					inDoubleQuote = false;
					sb.append(cur);
				} else {
					// escape quote, not the end of the quote
					sb.append(cur);
				}
			} else if(cur == '"' && !inSingleQuote && !inDoubleQuote) {
				// double quote starts
				inDoubleQuote = true;
				sb.append(cur);
			} else if (cur == '\'' && i > 0 && chars[i-1] == '\\') {
				// count the number of backslashes
				int count = 0;
				while(i - count - 1 >= 0) {
					if(chars[i - count - 1] == '\\') {
						count ++;
					} else {
						break;
					}
				} 
				if(count % 2 == 0) {
					// escape one or more backslashes instead of this quote, end of quote
					// single quote ends
					inSingleQuote = false;
					sb.append(cur);
				} else {
					// escape single quote, not the end of the quote
					sb.append(cur);
				}
			} else if(cur == '\'' && !inDoubleQuote && !inSingleQuote) {
				// single quote starts
				inSingleQuote = true;
				sb.append(cur);
			} else if(cur == '"' && !inSingleQuote && inDoubleQuote) {
				// quote ends
				inDoubleQuote = false;
				sb.append(cur);
			} else if (cur == '\'' && inSingleQuote && !inDoubleQuote) {
				// single quote ends
				inSingleQuote = false;
				sb.append(cur);
			} else if (inArgList == 0 && cur == '(' && !inSingleQuote && !inDoubleQuote) {
				// look behind to check if this is a method call
				int behind = i - 1;
				while(behind >= 0) {
					if(chars[behind] == ' ') {
						// continue to look behind
						behind = behind - 1;
					} else if ((chars[behind] >= 'a' && chars[behind] <= 'z') || 
							(chars[behind] >= 'A' && chars[behind] <= 'Z') ||
							(chars[behind] >= '0' && chars[behind] <= '9') ||
							chars[behind] == '_') {
						// this is a method call
						inArgList++;
						break;
					} else {
						// not a method call
						break;
					}
				}
				sb.append(cur);
			} else if (inArgList > 0 && cur == '(' && !inSingleQuote && !inDoubleQuote) {
				// already in an argument list. Since we cannot easily identify the end of argument list 
				// due to the formatting inconsistency between partial program analysis and BOA query, I have
				// to count every parenthesis in the argument list until it is 0
				inArgList ++;
				sb.append(cur);
			} else if (inArgList > 0 && cur == ')' && !inSingleQuote && !inDoubleQuote) {
				inArgList --;
				sb.append(cur);
			} else if (inSingleQuote || inArgList > 0 || inDoubleQuote) {
				// ignore any separator in quote or in a method call
				sb.append(cur);
			} else if (cur == '&' || cur == '|'){
				// look ahead
				if (i + 1 < chars.length && chars[i+1] == cur) {
					// step forward if it is logic operator, otherwise it is a bitwise operator
					i++;
					if(sb.length() > 0) {
						// push previous concatenated chars to the array
						tokens.add(sb.toString());
						// clear the string builder
						sb.setLength(0);
					}
				} else {
					// bitwise separator
					sb.append(cur);
				}
			} else if (cur == '!') {
				// look ahead
				if (i + 1 < chars.length && chars[i+1] == '=') {
					// != operator instead of logic negation operator
					sb.append(cur);
				} else {
					if(sb.length() > 0) {
						// push previous concatenated chars to the array
						tokens.add(sb.toString());
						// clear the string builder
						sb.setLength(0);
					}
				}
			} else {
				sb.append(cur);
			}
		}
		
		// push the last token if any
		if(sb.length() > 0) {
			tokens.add(sb.toString());
		}
		
		String[] arr = new String[tokens.size()];
		for(int i = 0; i < tokens.size(); i++) {
			arr[i] = tokens.get(i);
		}
		return arr;
	}
	
	public static String normalize(String predicate, ArrayList<String> rcv_candidates,
			ArrayList<ArrayList<String>> args_candidates, ArrayList<String> ret_candidates) {
		String norm = predicate;
		for (String rcv : rcv_candidates) {
			if (norm.contains(rcv)) {
				// cannot simply call replaceAll since some name be appear as part of other names
				//norm = norm.replaceAll(Pattern.quote(rcv), "rcv");
				norm = replaceVar(rcv, norm, 0, "rcv");
			}
		}

		for (ArrayList<String> args : args_candidates) {
			for (int i = 0; i < args.size(); i++) {
				if (norm.contains(args.get(i))) {
					// cannot simply call replaceAll since some name be appear as part of other names
					//norm = norm.replaceAll(Pattern.quote(args.get(i)), "arg" + i);
					norm = replaceVar(args.get(i), norm, 0, "arg" + i);
				}
			}
		}
		
		for (String ret : ret_candidates) {
			if (norm.contains(ret)) {
				// cannot simply call replaceAll since some name be appear as part of other names
				//norm = norm.replaceAll(Pattern.quote(rcv), "rcv");
				norm = replaceVar(ret, norm, 0, "ret");
			}
		}

		return norm.trim();
	}
	
	public static boolean containsVar(String var, String clause, int start) {
		if(clause.substring(start).contains(var)) {
			boolean flag1 = false;
			boolean flag2 = false;
			// a small trick to avoid the case where a condition variable name is part of a variable name in the clause
			int index = clause.indexOf(var, start);
			int ahead =  index - 1;
			int behind = index + var.length();
			if (ahead >= 0 && ahead < clause.length()) {
				char c = clause.charAt(ahead);
				if((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_') {
					// something contains the variable name as part of it
					flag1 = false;
				} else {
					flag1 = true;
				}
			} else {
				flag1 = true;
			}
			
			if (behind >= 0 && behind < clause.length()) {
				char c = clause.charAt(behind);
				if((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_') {
					// something contains the variable name as part of it
					flag2 = false;
				} else {
					flag2 = true;
				}
			} else {
				flag2 = true;
			}
			
			if(flag1 && flag2 && !ProcessUtils.isInQuote(clause, index)) {
				return true;
			} else {
				// keep looking forward
				if(behind < clause.length()) {
					try {
						return containsVar(var, clause, behind);
					} catch (StackOverflowError err) {
						err.printStackTrace();
					}
					
					return false;
				} else {
					return false;
				}
			}
		} else {
			return false;
		}
	}
	
	public static String conditionClause(String clause, String predicate) {
		String res = predicate;
		String pre = "";
		// a small trick to check whether the part that matches the clause is a stand-alone clause
		while(true) {
			if(res.indexOf(clause) == -1) {
				// the clause does not exist
				break;
			} else {
				boolean flag1 = false;
				boolean flag2 = false;
				
				// look ahead and see if it reaches a clause separator, e.g., &(&), |(|), !(!=)
				int ahead = res.indexOf(clause) - 1;
				while (ahead >= 0 && ahead < res.length()) {
					char c = res.charAt(ahead);
					if(c == ' ' || c == '(') {
						// okay lets keep looking back
						ahead --;
					} else if (c == '&' || c == '!' || c == '|'){
						flag1 = true;
						break;
					} else {
						break;
					}
				}
				
				if(ahead == -1) {
					// this clause appear in the beginning
					flag1 = true;
				}
				
				int behind = res.indexOf(clause) + clause.length();
				while (behind >= 0 && behind < res.length()) {
					char c = res.charAt(behind);
					if(c == ' ' || c == ')') {
						// okay lets keep looking behind
						behind ++;
					} else if (c == '&' || c == '|'){
						flag2 = true;
						break;
					} else if (c == '!' && behind + 1 < res.length() && res.charAt(behind + 1) != '=') {
						flag2 = true;
						break;
					} else {
						break;
					}
				}
				
				if(behind == res.length()) {
					// this clause appears in the end
					flag2 = true;
				}
				
				if(flag1 && flag2) {
					// stand-alone clause
					String sub1 = res.substring(0, res.indexOf(clause));
					String sub2 = res.substring(res.indexOf(clause) + clause.length());
					return pre + sub1 + "true" + sub2;
				} else {
					// keep searching
					pre = res.substring(0, behind);
					res = res.substring(behind);
				}
			}
		}
		
		return predicate;
	}
	
	public static String replaceVar(String var, String predicate, int start, String substitute) {
		if(!containsVar(var, predicate, start)) {
			return predicate;
		}
		
		boolean flag1 = false;
		boolean flag2 = false;
		// a small trick to avoid the case where a condition variable name is part of a variable name in the clause
		int index = predicate.indexOf(var, start);
		int ahead =  index - 1;
		int behind = index + var.length();
		
		if (ahead >= 0 && ahead < predicate.length()) {
			char c = predicate.charAt(ahead);
			if((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_') {
				// something contains the variable name as part of it
				flag1 = false;
			} else {
				flag1 = true;
			}
		} else {
			flag1 = true;
		}
		
		if (behind >= 0 && behind < predicate.length()) {
			char c = predicate.charAt(behind);
			if((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_') {
				// something contains the variable name as part of it
				flag2 = false;
			} else {
				flag2 = true;
			}
		} else {
			flag2 = true;
		}
		

		String sub1 = predicate.substring(0, ahead + 1);
		String sub2 = predicate.substring(behind);
		if(flag1 && flag2 && !ProcessUtils.isInQuote(predicate, index)) {
			// replace it
			String predicate2 = sub1 + substitute + sub2;
			// recalculate behind index after substitution
			behind = behind + substitute.length() - var.length();
			return replaceVar(var, predicate2, behind, substitute);
		} else {
			// keep looking forward
			return replaceVar(var, predicate, behind, substitute);
		}
	}
	
	public static void main(String[] args) {
		String m = "  /**\n   * Process the blog entries\n   *\n   * @param httpServletRequest Request\n   * @param httpServletResponse Response\n   * @param user {@link org.blojsom.blog.BlogUser} instance\n   * @param context Context\n   * @param entries Blog entries retrieved for the particular request\n   * @return Modified set of blog entries\n   * @throws org.blojsom.plugin.BlojsomPluginException If there is an error processing the blog\n   *     entries\n   */\n  public org.blojsom.blog.BlogEntry[] process(\n      HttpServletRequest httpServletRequest,\n      HttpServletResponse httpServletResponse,\n      BlogUser user,\n      Map context,\n      org.blojsom.blog.BlogEntry[] entries)\n      throws org.blojsom.plugin.BlojsomPluginException {\n    if (!_emoticonsMap.containsKey(user.getId())) {\n      return entries;\n    }\n\n    String blogBaseUrl = user.getBlog().getBlogBaseURL();\n    Map emoticonsForUser = (Map) _emoticonsMap.get(user.getId());\n    for (int i = 0; i < entries.length; i++) {\n      BlogEntry entry = entries[i];\n      String updatedDescription = entry.getDescription();\n      updatedDescription =\n          replaceEmoticon(emoticonsForUser, updatedDescription, HAPPY, HAPPY_PARAM, blogBaseUrl);\n      updatedDescription =\n          replaceEmoticon(emoticonsForUser, updatedDescription, SAD, SAD_PARAM, blogBaseUrl);\n      updatedDescription =\n          replaceEmoticon(emoticonsForUser, updatedDescription, GRIN, GRIN_PARAM, blogBaseUrl);\n      updatedDescription =\n          replaceEmoticon(emoticonsForUser, updatedDescription, LOVE, LOVE_PARAM, blogBaseUrl);\n      updatedDescription =\n          replaceEmoticon(\n              emoticonsForUser, updatedDescription, MISCHIEF, MISCHIEF_PARAM, blogBaseUrl);\n      updatedDescription =\n          replaceEmoticon(emoticonsForUser, updatedDescription, COOL, COOL_PARAM, blogBaseUrl);\n      updatedDescription =\n          replaceEmoticon(emoticonsForUser, updatedDescription, DEVIL, DEVIL_PARAM, blogBaseUrl);\n      updatedDescription =\n          replaceEmoticon(emoticonsForUser, updatedDescription, SILLY, SILLY_PARAM, blogBaseUrl);\n      updatedDescription =\n          replaceEmoticon(emoticonsForUser, updatedDescription, ANGRY, ANGRY_PARAM, blogBaseUrl);\n      updatedDescription =\n          replaceEmoticon(emoticonsForUser, updatedDescription, LAUGH, LAUGH_PARAM, blogBaseUrl);\n      updatedDescription =\n          replaceEmoticon(emoticonsForUser, updatedDescription, WINK, WINK_PARAM, blogBaseUrl);\n      updatedDescription =\n          replaceEmoticon(emoticonsForUser, updatedDescription, BLUSH, BLUSH_PARAM, blogBaseUrl);\n      updatedDescription =\n          replaceEmoticon(emoticonsForUser, updatedDescription, CRY, CRY_PARAM, blogBaseUrl);\n      updatedDescription =\n          replaceEmoticon(\n              emoticonsForUser, updatedDescription, CONFUSED, CONFUSED_PARAM, blogBaseUrl);\n      updatedDescription =\n          replaceEmoticon(\n              emoticonsForUser, updatedDescription, SHOCKED, SHOCKED_PARAM, blogBaseUrl);\n      updatedDescription =\n          replaceEmoticon(emoticonsForUser, updatedDescription, PLAIN, PLAIN_PARAM, blogBaseUrl);\n      entry.setDescription(updatedDescription);\n    }\n    return entries;\n  }\n";
		System.out.println(m.substring(669, 718));
	}
}
