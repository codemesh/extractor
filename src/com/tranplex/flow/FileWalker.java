package com.tranplex.flow;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class FileWalker {
	private String outputDir;
	private ArrayList<String> inputFiles = new ArrayList<String>();
	private Iterator<String> inputFileIterator;
	BufferedReader lineReader;
	private int nLines;
	private int successLines;
	private int failedLines;
	
	public synchronized String getNextLine(){
		String line = null;
		do{
			if(lineReader== null){
				if(!openNextFile()){
					return null;
				}
			}
			try{
				line = lineReader.readLine();
				if(line == null){
					lineReader.close();
					lineReader = null;
					continue;
				}
				break;
			}
			catch(IOException e){
				System.err.println(e.getMessage());
				return null;
			}
		}while(true);
		++ nLines;
		return line;
	}
	
	private boolean openNextFile(){
		String file = null;
		if(inputFileIterator == null){
			inputFileIterator = inputFiles.iterator();
		}
		try{
			file = inputFileIterator.next();
		}
		catch(NoSuchElementException e){
			return false;
		}
		try{
			lineReader = new BufferedReader(new FileReader(file));
		}
		catch(FileNotFoundException e){
			System.err.println(e.getMessage());
			return false;
		}
		return true;
	}
	
	public synchronized void addInputFile(String file){
		this.inputFiles.add(file);
	}

	public String getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}
	
	public int getLineNum(){
		return nLines;
	}
	
	public synchronized void linesResult(boolean OK){
		if(OK){
			successLines++;
		}
		else{
			failedLines++;
		}
	}
	
	public int getLinesResult(boolean OK){
		if(OK){
			return successLines;
		}
		else{
			return failedLines;
		}
	}
}
