package edu.cs.ucla.preprocess;

import java.util.ArrayList;

import org.junit.Test;

public class Main {
	@Test
	public void mapGet() {
		// supplement names
		ArrayList<ArrayList<String>> argNameList = new ArrayList<ArrayList<String>>(); 
		ArrayList<String> argNames1 = new ArrayList<String>();
		argNames1.add("key");
		argNameList.add(argNames1);
		Preprocess.argNameList = argNameList;
		ArrayList<ArrayList<String>> argTypeList = new ArrayList<ArrayList<String>>();
		ArrayList<String> argTypes1 = new ArrayList<String>();
		argTypes1.add("Object");
		argTypeList.add(argTypes1);
		Preprocess.argTypeList = argTypeList;
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
		ArrayList<ArrayList<String>> argNameList = new ArrayList<ArrayList<String>>(); 
		ArrayList<String> argNames1 = new ArrayList<String>();
		argNames1.add("id");
		argNameList.add(argNames1);
		Preprocess.argNameList = argNameList;
		ArrayList<ArrayList<String>> argTypeList = new ArrayList<ArrayList<String>>();
		ArrayList<String> argTypes1 = new ArrayList<String>();
		argTypes1.add("int");
		argTypeList.add(argTypes1);
		Preprocess.argTypeList = argTypeList;
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
