package edu.cs.ucla.preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonReader {
	public static void main(String[] args) {
		String jsonPath = "/media/troy/Disk2/Boa/apis/Map.get/evis_550.txt";
		try(BufferedReader br = new BufferedReader(new FileReader(new File(jsonPath)))) {
			String line;
			while ((line = br.readLine()) != null) {
				if(line.startsWith("[")) line = line.substring(1);
				JSONObject obj = new JSONObject(line.substring(0, line.length() - 1));
				String n = obj.getString("url");
				int a = obj.getInt("exampleID");
				System.out.println(a + " " + n);
			}			
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
