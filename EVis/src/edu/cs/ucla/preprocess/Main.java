package edu.cs.ucla.preprocess;

import org.junit.Test;

public class Main {
	@Test
	public void mapGet() {
		// supplement names
		Preprocess.argName = "key";
		Preprocess.argType = "Object";
		Preprocess.rcvName = "map";
		Preprocess.rcvType = "Map";
		Preprocess.retName = "value";
		Preprocess.retType = "Object";
		
		String focal = "get";
		String input = "/media/troy/Disk2/Boa/apis/Map.get";
		Preprocess pp = new Preprocess(input, focal);
		pp.process();
		String output = "/media/troy/Disk2/Boa/apis/Map.get/evis.txt";
		pp.dumpToJsonNewSchema(output);
	}
	
	@Test
	public void activityFindViewById() {
		Preprocess.argName = "id";
		Preprocess.argType = "int";
		Preprocess.rcvName = "activity";
		Preprocess.rcvType = "Activity";
		Preprocess.retName = "view";
		Preprocess.retType = "View";
		
		String focal = "findViewById";
		String input = "/media/troy/Disk2/Boa/apis/Activity.findViewById";
		Preprocess pp = new Preprocess(input, focal);
		pp.process();

		String output = "/media/troy/Disk2/Boa/apis/Activity.findViewById/evis.txt";
		pp.dumpToJsonNewSchema(output);
	}
}
