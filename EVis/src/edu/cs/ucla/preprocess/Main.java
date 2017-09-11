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

	@Test
	public void sqlitedatabaseQuery() {
		// query(String table, String[] columns, String selection, String[]
		// selectionArgs, String groupBy, String having, String orderBy)
		ArrayList<ArrayList<String>> argNameList = new ArrayList<ArrayList<String>>();
		ArrayList<String> argNames1 = new ArrayList<String>();
		argNames1.add("table");
		argNames1.add("columns");
		argNames1.add("selection");
		argNames1.add("selectionArgs");
		argNames1.add("groupBy");
		argNames1.add("having");
		argNames1.add("orderBy");
		ArrayList<String> argNames2 = new ArrayList<String>();
		argNames2.add("table");
		argNames2.add("columns");
		argNames2.add("selection");
		argNames2.add("selectionArgs");
		argNames2.add("groupBy");
		argNames2.add("having");
		argNames2.add("orderBy");
		argNames2.add("limit");
		ArrayList<String> argNames3 = new ArrayList<String>();
		argNames3.add("distinct");
		argNames3.add("table");
		argNames3.add("columns");
		argNames3.add("selection");
		argNames3.add("selectionArgs");
		argNames3.add("groupBy");
		argNames3.add("having");
		argNames3.add("orderBy");
		argNames3.add("limit");
		ArrayList<String> argNames4 = new ArrayList<String>();
		argNames4.add("distinct");
		argNames4.add("table");
		argNames4.add("columns");
		argNames4.add("selection");
		argNames4.add("selectionArgs");
		argNames4.add("groupBy");
		argNames4.add("having");
		argNames4.add("orderBy");
		argNames4.add("limit");
		argNames4.add("cancellationSignal");
		argNameList.add(argNames1);
		argNameList.add(argNames2);
		argNameList.add(argNames3);
		argNameList.add(argNames4);
		Preprocess.argNameList = argNameList;
		ArrayList<ArrayList<String>> argTypeList = new ArrayList<ArrayList<String>>();
		ArrayList<String> argTypes1 = new ArrayList<String>();
		argTypes1.add("String");
		argTypes1.add("String[]");
		argTypes1.add("String");
		argTypes1.add("String[]");
		argTypes1.add("String");
		argTypes1.add("String");
		argTypes1.add("String");
		ArrayList<String> argTypes2 = new ArrayList<String>();
		argTypes2.add("String");
		argTypes2.add("String[]");
		argTypes2.add("String");
		argTypes2.add("String[]");
		argTypes2.add("String");
		argTypes2.add("String");
		argTypes2.add("String");
		argTypes2.add("String");
		ArrayList<String> argTypes3 = new ArrayList<String>();
		argTypes3.add("boolean");
		argTypes3.add("String");
		argTypes3.add("String[]");
		argTypes3.add("String");
		argTypes3.add("String[]");
		argTypes3.add("String");
		argTypes3.add("String");
		argTypes3.add("String");
		argTypes3.add("String");
		ArrayList<String> argTypes4 = new ArrayList<String>();
		argTypes4.add("boolean");
		argTypes4.add("String");
		argTypes4.add("String[]");
		argTypes4.add("String");
		argTypes4.add("String[]");
		argTypes4.add("String");
		argTypes4.add("String");
		argTypes4.add("String");
		argTypes4.add("String");
		argTypes4.add("CancellationSignal");
		argTypeList.add(argTypes1);
		argTypeList.add(argTypes2);
		argTypeList.add(argTypes3);
		argTypeList.add(argTypes4);
		Preprocess.argTypeList = argTypeList;
		Preprocess.rcvName = "database";
		Preprocess.rcvType = "SQLiteDatabase";
		Preprocess.retName = "cursor";
		Preprocess.retType = "Cursor";

		String focal = "query";
		String input = "/media/troy/Disk2/Boa/apis/SQLiteDatabase.query";
		Preprocess pp = new Preprocess(input, focal);
		pp.process();

		String output = "/media/troy/Disk2/Boa/apis/SQLiteDatabase.query/evis.txt";
		pp.dumpToJsonNewSchema(output);
	}
}
