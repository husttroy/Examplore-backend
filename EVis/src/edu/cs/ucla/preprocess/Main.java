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
}
