package edu.cs.ucla.preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.cs.ucla.model.APICall;
import edu.cs.ucla.model.ControlConstruct;
import edu.cs.ucla.model.Item;

public class Preprocess {
	// Config this first!
	boolean isFirstRun = false;
	
	String path;
	String focal;
	HashMap<String, HashMap<String, String>> types;
	HashMap<String, ArrayList<Item>> seqs;

	public Preprocess(String input, String api) {
		this.path = input;
		this.focal = api;
		this.types = new HashMap<String, HashMap<String, String>>();
		this.seqs = new HashMap<String, ArrayList<Item>>();
	}

	public void process() {
		// first construct the symbol table
		constructSymbolTables();

		// then process the method call sequences
		constructMethodCallSequences();
	}

	private void constructSymbolTables() {
		File f = new File(path + File.separator + "1.txt");
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("vartypes[")) {
					String key = line.substring(line.indexOf("[") + 1,
							line.indexOf("] ="));
					String s = line.substring(line.indexOf("] =") + 3).trim();
					String[] ss = s.split("\\|");
					HashMap<String, String> map = new HashMap<String, String>();
					// skip the first element because it is empty string
					for (int i = 1; i < ss.length; i++) {
						String name = ss[i].split(":")[0];
						String type = ss[i].split(":")[1];
						map.put(name, type);
					}
					types.put(key, map);
				}
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void constructMethodCallSequences() {
		File f = new File(path + File.separator + "1-clean.txt");
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			String line = null;
			while ((line = br.readLine()) != null) {
				// process each line based on the strategy
				if (line.startsWith("results[")) {
					// set a threshold to avoid processing long methods
					if (line.length() < 3000) {
						String key = line.substring(line.indexOf("[") + 1,
								line.indexOf("][SEQ]"));
						System.out.println("processing " + key);
//						if(key.equals("https://github.com/apache/synapse!scratch/asankha/synapse_ws/modules/transports/src/main/java/org/apache/synapse/transport/amqp/AMQPSender.java!AMQPSender!processSyncResponse")) {
//							System.out.println("Hit");
//						}
						if (types.containsKey(key)) {
							HashMap<String, String> symbol_table = types
									.get(key);
							String seq = line
									.substring(line.indexOf("] =") + 3).trim();
							ArrayList<Item> sequence = new ArrayList<Item>();

							ArrayList<String> ss = ProcessUtils
									.splitByArrow(seq);
							for (String s : ss) {
								s = s.trim();
								int count1 = 0;
								if (s.endsWith("}")) {
									while (s.endsWith("}")) {
										s = s.substring(0, s.lastIndexOf("}"))
												.trim();
										count1++;
									}
								}

								ArrayList<String> rest = new ArrayList<String>();
								while (s.endsWith("} ELSE {")
										|| (s.contains("} CATCH(") && s
												.endsWith(") {"))
										|| s.endsWith("} FINALLY {")) {
									String s1 = s.substring(0,
											s.lastIndexOf('}') + 1).trim();
									String s2 = s.substring(
											s.lastIndexOf('}') + 1, s.length())
											.trim();

									if (!s2.isEmpty()) {
										rest.add(s2);
									}

									while (s1.endsWith("}")) {
										s1 = s1.substring(0,
												s1.lastIndexOf("}")).trim();
										rest.add("}");
									}

									s = s1;

								}

								if (!s.isEmpty()) {
									sequence.addAll(ProcessUtils.extractItems(
											s, symbol_table));
								}

								for (int j = rest.size() - 1; j >= 0; j--) {
									String r = rest.get(j);
									if (r.equals("IF {") || r.equals("ELSE {")
											|| r.equals("TRY {")
											|| r.equals("LOOP {")
											|| r.equals("FINALLY {")) {
										ControlConstruct cc = new ControlConstruct(
												r.trim(), null);
										sequence.add(cc);
									} else if (r.contains("CATCH(")
											&& r.endsWith(") {")) {
										String type = r.substring(
												r.lastIndexOf("CATCH(") + 6,
												r.lastIndexOf(") {"));
										ControlConstruct cc = new ControlConstruct(
												"CATCH", type);
										sequence.add(cc);
									}
								}

								while (count1 > 0) {
									sequence.add(new ControlConstruct("}", null));
									count1--;
								}
							}

							this.seqs.put(key, sequence);
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String synthesizeReableCode(ArrayList<APICall> calls, HashMap<String, String> map) {
		String code = "";
		HashMap<String, String> dict = new HashMap<String, String>();
		for(int i = 0; i < calls.size(); i++) {
			APICall call = calls.get(i);
			String apiName = call.name.substring(0, call.name.indexOf('('));
			if(!call.ret.contains(apiName + "(")) {
				// this call has a return value and its return value is assigned to another variable
				String retType = "";
				if(map.containsKey(call.ret)) {
					retType = map.get(call.ret);
					code += retType + " " + retType.toLowerCase() + " = ";
				} else {
					// cannot resolve the type of the lefthand side variable
					code += call.ret + " = ";
				}
			} else {
				// this call either does not have a return value or its return value is immediately consumed by another call
				// scan the succeeding API calls and check
				boolean isConsumed = false;
				for(int j = i+1; j < calls.size(); j++) {
					APICall next = calls.get(j);
					if(next.arguments.contains(call.ret) || (next.receiver != null && next.receiver.contains(call.ret))) {
						// yes
						isConsumed = true;
						break;
					}
				}
				if(isConsumed) {
					// introduce a temporary variable to store its value
					code += "value = ";
					// put this temporary variable name in the dictionary
					dict.put(call.ret, "value");
				}
			}
			
			if(call.receiver != null) {
				if(map.containsKey(call.receiver)) {
					String rcvType = map.get(call.receiver);
					code += rcvType.toLowerCase() + "." + apiName + "(";
				} else if (dict.containsKey(call.receiver)) {
					String temp = dict.get(call.receiver);
					code += temp + "." + apiName + "(";
				} else {
					code += call.receiver + "." + apiName + "(";
				}
			} else {
				code += apiName + "(";
			}
			
			if(!call.arguments.isEmpty()) {
				for(String argument : call.arguments) {
					if(map.containsKey(argument)) {
						String argType = map.get(argument);
						code += argType.toLowerCase() + ", ";
					} else if (dict.containsKey(argument)) {
						String temp = dict.get(argument);
						code += temp + ", ";
					} else {
						code += argument + ", ";
					}
				}
				code = code.substring(0, code.length() - 2);
			}
			
			code += ");" + System.lineSeparator();
		}
		
		return code;
	}

	public void dumpToJson(String output) {
		if(new File(output).exists()) {
			new File(output).delete();
		}
		
		int id = 0;
		int numOfUnreachableUrls = 0;
		for (String key : this.seqs.keySet()) {
			// get focal API call
			ArrayList<Item> seq = this.seqs.get(key);
			APICall theCall = null;
			ArrayList<APICall> prevCalls = new ArrayList<APICall>();
			ArrayList<APICall> postCalls = new ArrayList<APICall>();
			ControlConstruct prevCC = null;
			ControlConstruct postCC = null;
			for (int i = 0; i < seq.size(); i++) {
				Item item = seq.get(i);
				if (item instanceof APICall) {
					String signature = ((APICall) item).name;
					if (signature.contains(focal + "(")) {
						// okay this is the focal API call
						theCall = (APICall) item;
						
						HashSet<String> relVars = new HashSet<String>();
						if(theCall.receiver != null) {
							relVars.add(theCall.receiver);
						}
						
						if(!theCall.arguments.isEmpty()) {
							relVars.addAll(theCall.arguments);
						}
						
						// scanning backward to find the first preceding call
						for (int j = i - 1; j >= 0; j--) {
							Item prev = seq.get(j);
							if (prev instanceof APICall) {
								APICall call = (APICall)prev;
								if(relVars.contains(call.receiver) || relVars.contains(call.ret)) {
									prevCalls.add(0, call);
								}
							}
						}

						if(theCall.ret != null) {
							relVars.add(theCall.ret);
						}
						
						if(!theCall.arguments.isEmpty()) {
							// we don't care about those API calls that manipulate the arguments of the focal API call
							relVars.removeAll(theCall.arguments);
						}
						
						// scanning forward to find the first succeeding call
						for (int j = i + 1; j < seq.size(); j++) {
							Item post = seq.get(j);
							if (post instanceof APICall) {
								APICall call = (APICall)post;
								
								boolean isRelevant = false;
								if(relVars.contains(call.receiver)) {
									isRelevant = true;
								} else {
									for(String arg : call.arguments) {
										if(relVars.contains(arg)) {
											isRelevant = true;
											break;
										}
									}
								}
								
								if(isRelevant) {
									postCalls.add(call);
								}
							}
						}

						// scanning backward to find the first preceding control construct
						int closeBraceCount = 0;
						for (int j = i - 1; j >= 0; j--) {
							Item prev = seq.get(j);
							if (prev instanceof ControlConstruct) {
								String type = ((ControlConstruct) prev).type;
								if (type.equals("}")) {
									// there is likely to be a pre-check or
									// something
									closeBraceCount++;
								} else if (closeBraceCount == 0) {
									prevCC = (ControlConstruct) prev;
									if (prevCC.type.equals("IF {")
											|| prevCC.type.equals("LOOP {")) {
										// okay we need to fetch its predicate
										// from the enclosed calls
										prevCC.guard = theCall.normalizedGuard;
									}
									break;
								} else if (closeBraceCount > 0) {
									closeBraceCount --;
								}
							}
						}

						// scanning forward to find the first succeeding
						// construct
						for (int j = i + 1; j < seq.size(); j++) {
							Item post = seq.get(j);
							if (post instanceof ControlConstruct) {
								String type = ((ControlConstruct) post).type;

								if (type.equals("IF {")
										|| type.equals("LOOP {")) {
									postCC = (ControlConstruct) post;
									for (int k = j + 1; k < seq.size(); k++) {
										Item post2 = seq.get(k);
										if (post2 instanceof APICall) {
											String predicate = ((APICall) post2).originalGuard;
											// normalize the guard condition with respect to the receiver, return value, and the arguments of the focal API
											String normalize = ProcessUtils
													.getNormalizedPredicate(
															predicate,
															theCall.receiver,
															theCall.arguments,
															theCall.ret);
											postCC.guard = normalize;
											break;
										} else if (post2 instanceof ControlConstruct
												&& ((ControlConstruct) post2).type
														.equals("}")) {
											break;
										}
									}
								} else {
									postCC = (ControlConstruct) post;
								}
								break;
							}
						}
						
						break;
					}					
				}
			}
			
			if(theCall == null) {
				continue;
			}
			
			// download source code from GitHub if not cached already
			String[] ss = key.split("\\!");
			String projectURL = ss[0];
			String projectName = projectURL.substring(19);
			projectName = projectName.replaceAll("\\/", "-");
			String srcPath = ss[1];
			String className = ss[2];
			String methodName = ss[3];
			String url = projectURL + "/tree/master/" + srcPath;
			String dumpPath = path + File.separator + "dump" + File.separator + projectName + File.separator + className + ".java";
			File dumpFile = new File(dumpPath);
			String method = "empty";
			String code = null;
			if(isFirstRun) {
				// fetch by url
				System.out.println("Fetching code from " + url);
				code = FetchSourceCode.fetchCodeByUrl(url);
				if(code != null) {
					if(!dumpFile.getParentFile().exists()) {
						dumpFile.getParentFile().mkdirs();
					}
					
					FileUtils.writeStringToFile(code, dumpPath);	
				} else {
					numOfUnreachableUrls ++;
				}
			} else {
				// fetch from cached file
				if(new File(dumpPath).exists()) {
					code = FileUtils.readFileToString(dumpPath);
				}
			}
			
			if(code == null) {
				// only printing those examples that we can fetch raw code
				continue;
			}
			
			// extract the corresponding method from the source code
			ASTParser p = getASTParser(code);
			CompilationUnit cu = (CompilationUnit) p.createAST(null);
			ArrayList<String> methods = new ArrayList<String>();
			final String src = code;
			cu.accept(new ASTVisitor() {
				@Override
				public boolean visit(MethodDeclaration node) {
					if(node.getName().toString().equals(methodName)) {
						int startLine = cu.getLineNumber(node.getStartPosition()) - 1;
						int endLine = cu.getLineNumber(node.getStartPosition() + node.getLength()) - 1;
						String s = "";
						String[] ss = src.split(System.lineSeparator());
						for(int i = startLine; i <= endLine; i++) {
							s += ss[i] + System.lineSeparator();
						}
						methods.add(s);
					}
					
					return false;
				}
			});
			
			for(String m : methods) {
				if(m.contains(focal + "(")) {
					method = m;
					break;
				}
			}
			
			if (method.equals("empty")) {
				// only printing those code examples that we can find the matching method
				continue;
			}
			
			// construct the json string
			StringBuilder sb2 = new StringBuilder();
			sb2.append("{\"exampleID\": " + id + ", ");
			if(prevCC == null) {
				sb2.append("\"immediateControlStructure\": \"empty\", ");
				sb2.append("\"associatedPredicate\": \"empty\", ");
			} else {
				sb2.append("\"immediateControlStructure\": \"" + prevCC.type + "\", ");
				sb2.append("\"associatedPredicate\": \"" + (prevCC.guard == null ? "empty" : StringEscapeUtils.escapeJava(prevCC.guard)) + "\", ");
			}
			
			sb2.append("\"focalAPI\": \"" + theCall.name + "\", ");
			
			if(postCC == null) {
				sb2.append("\"followUpControlStructure\": \"empty\", ");
				sb2.append("\"followUpAssociatedPredicate\": \"empty\", ");
			} else {
				sb2.append("\"followUpControlStructure\": \"" + postCC.type + "\", ");
				sb2.append("\"followUpAssociatedPredicate\": \"" + (postCC.guard == null ? "empty" : StringEscapeUtils.escapeJava(postCC.guard)) + "\", ");
			}
			
			sb2.append("\"precedingAPICall\":[");
			if(!prevCalls.isEmpty()) {
				String s = "";
				for(APICall call : prevCalls) {
					s += "\"" + call.name + "\", ";
				}
				sb2.append(s.substring(0, s.length() - 2));
			}
			
			sb2.append("], ");
			
			sb2.append("\"postAPICall\": [");
			if(!postCalls.isEmpty()) {
				String s = "";
				for(APICall call : postCalls) {
					s += "\"" + call.name + "\", ";
				}
				sb2.append(s.substring(0, s.length() - 2));
			}
			
			sb2.append("], ");
			
			sb2.append("\"url\": \"" + url + "\", ");
			sb2.append("\"rawCode\": \"" + StringEscapeUtils.escapeJava(method) + "\"}");
			
			FileUtils.appendStringToFile(sb2.toString() + System.lineSeparator(),  output);
			
			String logFile = "/media/troy/Disk2/Boa/apis/Map.get/synthesis.txt";
			// print raw code
			FileUtils.appendStringToFile("Raw code: \n", logFile);
			FileUtils.appendStringToFile(method, logFile);
			FileUtils.appendStringToFile("Simplified code: \n", logFile);
			ArrayList<APICall> calls = new ArrayList<APICall>();
			calls.addAll(prevCalls);
			calls.add(theCall);
			calls.addAll(postCalls);
			if(types.containsKey(key)) {
				FileUtils.appendStringToFile(synthesizeReableCode(calls, types.get(key)) + "\n", logFile);
			} else {
				FileUtils.appendStringToFile(synthesizeReableCode(calls, new HashMap<String, String>()) + "\n", logFile);
			}
		
			id++;
		}
		
		// log the number of unreachable urls
		System.out.println(numOfUnreachableUrls);
	}
	
	private ASTParser getASTParser(String sourceCode) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setStatementsRecovery(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(sourceCode.toCharArray());
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		Map options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_5, options);
		parser.setCompilerOptions(options);
		return parser;
	}

	public static void main(String[] args) {
		String focal = "get";
		String input = "/media/troy/Disk2/Boa/apis/Map.get";
		Preprocess pp = new Preprocess(input, focal);
		pp.process();

		String output = "/media/troy/Disk2/Boa/apis/Map.get/evis.txt";
		pp.dumpToJson(output);
	}
}
