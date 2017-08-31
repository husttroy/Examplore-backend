package edu.cs.ucla.preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang3.StringEscapeUtils;

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
			APICall prevCall = null;
			APICall postCall = null;
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
									prevCall = call;
									break;
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
									postCall = call;
									break;
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
			
			if(code != null) {
				String[] lines = code.split(System.lineSeparator());
				ArrayList<Integer> startLines = new ArrayList<Integer>();
				for(int i = 0; i < lines.length; i++) {
					String line = lines[i];
					if(line.contains(methodName + "(") && (line.trim().endsWith("{") || line.trim().endsWith("(") || line.trim().endsWith(","))) {
						startLines.add(i);
					}
				}
				
				int start = 0;
				int end = 0;
				for(int startLine : startLines) {
					StringBuilder sb = new StringBuilder();
					for(int i = startLine; i < lines.length; i++) {
						String line = lines[i];
						sb.append(lines[i] + System.lineSeparator());
						if (line.startsWith("  }")) {
							start = startLine;
							end = i;
							break;
						}
					}
					
					if(sb.toString().contains(focal + "(")) {
						method = sb.toString();
						break;
					} else {
						// overloading method
						continue;
					}
				}
				
				if (start != 0 && end != 0 && !method.equals("empty")) {
					// rewrite the url by appending line numbers
					url += "#L" + (start+1) + "-L" + (end+1);
				}
			} else {
				url = "empty";
			}
			
			// construct the json string
			StringBuilder sb2 = new StringBuilder();
			sb2.append("{\"example_id\": " + id + ", ");
			if(prevCC == null) {
				sb2.append("\"immediate-control-structure\": \"empty\", ");
				sb2.append("\"associated-predicate\": \"empty\", ");
			} else {
				sb2.append("\"immediate-control-structure\": \"" + prevCC.type + "\", ");
				sb2.append("\"associated-predicate\": \"" + (prevCC.guard == null ? "empty" : StringEscapeUtils.escapeJava(prevCC.guard)) + "\", ");
			}
			
			sb2.append("\"focal-API\": \"" + theCall.name + "\", ");
			
			if(postCC == null) {
				sb2.append("\"follow-up-control-structure\": \"empty\", ");
				sb2.append("\"follow-up-associated-predicate\": \"empty\", ");
			} else {
				sb2.append("\"follow-up-control-structure\": \"" + postCC.type + "\", ");
				sb2.append("\"follow-up-associated-predicate\": \"" + (postCC.guard == null ? "empty" : StringEscapeUtils.escapeJava(postCC.guard)) + "\", ");
			}
			
			if(prevCall == null) {
				sb2.append("\"preceding-API-call\": \"empty\", ");				
			} else {
				sb2.append("\"preceding-API-call\": \"" + prevCall.name + "\", ");
			}
			
			if(postCall == null) {
				sb2.append("\"post-API-call\": \"empty\", ");				
			} else {
				sb2.append("\"post-API-call\": \"" + postCall.name + "\", ");
			}
			
			sb2.append("\"url\": \"" + url + "\", ");
			sb2.append("\"raw-code\": \"" + StringEscapeUtils.escapeJava(method) + "\"}");
			
			FileUtils.appendStringToFile(sb2.toString() + System.lineSeparator(),  output);
			id++;
		}
		
		// log the number of unreachable urls
		System.out.println(numOfUnreachableUrls);
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
