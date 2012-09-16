package com.tranplex.flow;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Properties;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.tranplex.extractor.IParallelNodeWriter;
import com.tranplex.extractor.ParallelPath;
import com.tranplex.extractor.PathNode;

public class NYTimesExtractor {
	class ParallelNodeWriter implements IParallelNodeWriter{
		public static final String FLAW = "flaw";
		private String oDir;
		private String oFlawDir;
		private String fileName;
		private String fullFileName1;
		private String fullFileName2;
		private String parallelDir1;
		private String parallelDir2;
		private String flawParallelDir1;
		private String flawParallelDir2;
		private Writer writer;
		private Writer parallelWriter1;
		private Writer parallelWriter2;
		
		public ParallelNodeWriter(String oDir){
			this.oDir = oDir;
			this.oFlawDir = oDir + '_' + FLAW;
		}

		public void mkdirParallel(String dir1, String dir2) throws IOException{
			parallelDir1 = oDir + File.separatorChar + dir1;
			File f = new File(parallelDir1);
			f.mkdirs();
			
			parallelDir2 = oDir+ File.separatorChar + dir2;
			f = new File(parallelDir2);
			f.mkdirs();
			
			flawParallelDir1 = oFlawDir + File.separatorChar + dir1;
			f = new File(flawParallelDir1);
			f.mkdirs();
			
			flawParallelDir2 = oFlawDir + File.separatorChar + dir2;
			f = new File(flawParallelDir2);
			f.mkdirs();
		}
		
		public void openParallel(String file) throws IOException {
			fileName = file;
			fullFileName1 = parallelDir1 + File.separatorChar + file;
			fullFileName2 = parallelDir2 + File.separatorChar + file;
			parallelWriter1 = new FileWriter(fullFileName1);
			parallelWriter2 = new FileWriter(fullFileName2);
			writer = parallelWriter1;
		}
		
		public void markAsFlaw(){
			close();
			File f = new File(fullFileName1);
//			Files.move(new UnixPath(fullFileName1), new Path(parallelDir1 + File.separatorChar + FLAW + fileName);
			f.renameTo(new File(flawParallelDir1 + File.separatorChar + FLAW + fileName));
			f = new File(fullFileName2);
			f.renameTo(new File(flawParallelDir2 + File.separatorChar + FLAW + fileName));
		}
		
		@Override
		public void switchBack() {
			writer = parallelWriter1;
			
		}

		@Override
		public void switchToParallel() {
			writer = parallelWriter2;
		}

		@Override
		public void write(String content) { 
			try{
				writer.write(content);
				writer.append('\n');
			}catch(IOException e){
				System.err.println(e.getMessage());
			}
		}

		@Override
		public void close() {
			try{
				if(parallelWriter1 != null){
					parallelWriter1.close();
					parallelWriter1 = null;
				}
				if(parallelWriter2 != null){
					parallelWriter2.close();
					parallelWriter2 = null;
				}
			}catch(Exception e){
				System.err.println(e.getMessage());
			}
		}
		
	}
	
	private String language1;
	private String language2;
	private String parallelPattern;
	private String titleParallelPattern;
	private String oDir;
	private String iDir;
	private Properties prop;
	private PathNode parser;
	private PathNode titleParser;
	private ArrayList<String> failedDocList = new ArrayList<String>();
	private int statTotalDoc;
	private int statFailedDoc;
	private int statOKDoc;
	
	public String getLanguage1() {
		return language1;
	}

	public void setLanguage1(String language1) {
		this.language1 = language1;
	}

	public String getLanguage2() {
		return language2;
	}

	public void setLanguage2(String language2) {
		this.language2 = language2;
	}

	public String getParallelPattern() {
		return parallelPattern;
	}

	public void setParallelPattern(String parallelPattern) {
		this.parallelPattern = parallelPattern;
	}

	public String getTitleParallelPattern() {
		return titleParallelPattern;
	}

	public void setTitleParallelPattern(String titleParallelPattern) {
		this.titleParallelPattern = titleParallelPattern;
	}

	public void setOutput(String oDir){
		this.oDir = oDir;
	}
	
	public void setInput(String iDir){
		this.iDir = iDir;
	}
	
	public void setProp(Properties prop){
		this.prop = prop;
	}
	
	public int getStatTotalDoc() {
		return statTotalDoc;
	}

	public int getStatFailedDoc() {
		return statFailedDoc;
	}

	public int getStatOKDoc() {
		return statOKDoc;
	}
	
	public void incrFailedDoc(File f){
		++ statFailedDoc;
		failedDocList.add(f.getAbsolutePath());
	}
	
	public void incrOKDoc(File f){
		++ statOKDoc;
	}
	
	public void incrTotalDoc(File f){
		++ statTotalDoc;
		//System.out.println("To extract: " + f.getAbsolutePath());
	}

	public void extract(){
		ParallelPath parserBuilder = new ParallelPath();
		parser = parserBuilder.parseFromString(prop.getProperty(this.parallelPattern));
		titleParser = parserBuilder.parseFromString(prop.getProperty(this.titleParallelPattern));
		ParallelNodeWriter w = new ParallelNodeWriter(oDir);
		try{
			w.mkdirParallel(prop.getProperty(this.language1), prop.getProperty(this.language2));
		}catch(IOException e){
			System.err.println(e.getMessage());
			return;
		}
		File iDirFile = new File(iDir);
		for(File f : iDirFile.listFiles()){
			if(f.isDirectory()){
				continue;
			}
			try {
				w.openParallel(f.getName());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			incrTotalDoc(f);
			if(extractFile(f, w)){
				w.close();
			}
			else{
				w.markAsFlaw();
			}
		}
	}
	public boolean extractFile(File f, IParallelNodeWriter w){
		Document doc = null;
		try{
			doc = Jsoup.parse(f, "UTF-8");
		}catch(IOException e){
			System.err.println(e.getMessage());
			return false;
		}
		boolean OK = this.titleParser.extractDoc(doc,w);
		if(!OK){
			incrFailedDoc(f);
			return false;
		}
		OK = this.parser.extractDoc(doc, w);
		if(!OK){
			incrFailedDoc(f);
			return false;
		}
		incrOKDoc(f);
		return true;
	}
	
	public void stat(){
		System.out.println("Parse Stat:\nTotal doc: " + statTotalDoc +
				"\nOK Doc: " + statOKDoc +
				"\nFailed Doc: " + statFailedDoc);
		if(statFailedDoc > 0){
			System.out.println("List of failed doc:");
			for(String d : failedDocList){
				System.out.println(d);
			}
		}
	}
	
	public static void help(){
		// java class -o outputdir
		System.err.println("java <class> -c <conffile> -o <outputdir> <inputdir>");
	}
	public static void main(String[] args){
		if(args.length < 5 || !args[0].equals("-c") || !args[2].equals("-o")){
			help();
			return;
		}
		String confFile = args[1];
		String oDir= args[3];
		
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
		NYTimesExtractor e = new NYTimesExtractor();
		e.setLanguage1("en");
		e.setLanguage2("cn");
		e.setParallelPattern("NYTParallelPattern");
		e.setTitleParallelPattern("NYTTitleParallelPattern");
		e.setOutput(oDir);
		e.setProp(prop);
		e.setInput(args[4]);
		e.extract();
		e.stat();
	}
}
