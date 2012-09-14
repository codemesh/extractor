package com.tranplex.flow;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Properties;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.tranplex.extractor.ParallelPath;
import com.tranplex.extractor.PathNode;

public class UrlLister {
	private Properties prop;
	private PathNode parser;
	void list(){
		String urlPattern = prop.getProperty("UrlPattern");
		if(urlPattern == null){
			System.err.println("No UrlPattern found in conf");
			return;
		}
		ParallelPath parserBuilder = new ParallelPath();
		this.parser = parserBuilder.parseFromString(urlPattern);
	}
	protected void listFile(String f){
    	File input = new File(f);
    	//File input = new File("/home/fengyu/Downloads/nytimes.html");
    	Document doc = null;
    	try{
    		doc = Jsoup.parse(input, "UTF-8");
    		//doc = Jsoup.connect("http://http://cn.nytimes.com/article/world/2012/09/10/c10arableft/dual/").get();
    	}
    	catch (IOException e){
    		System.out.println(e.getMessage());
    		return;
    	}

	}
	public static void help(){
		System.out.println("usage: java <class> -c <conffile> -o <outputfile> <inputfile>+");
	}
	public static void main(String[] args){
		if(args.length < 5 || !args[0].equals("-c") || !args[2].equals("-o")){
			help();
			return;
		}
		String confFile = args[1];
		String oFile = args[3];
		Properties prop = new Properties();
		try{
			prop.load(new FileReader(confFile));
		}catch(NoSuchFileException e){
			System.err.println(e.getMessage());
			return;
		}catch(IOException e){
			System.err.println(e.getMessage());
			return;
		}
		UrlLister l = new UrlLister();
		l.setOutputFile(oFile);
		l.setProp(prop);
		for(int i = 4; i < args.length; ++ i ){
			l.addInputFile(args[i]);
		}
		l.list();
		
	}
}
