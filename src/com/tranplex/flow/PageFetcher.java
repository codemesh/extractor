package com.tranplex.flow;

import java.io.IOException;
import java.util.ArrayList;

public class PageFetcher{
	class FetchThread extends Thread{
		private FileWalker info;
		public FetchThread(int id, FileWalker info){
			this.info = info;
		}
		public void run(){
			String url = null;
			String outputDir = info.getOutputDir();
						while((url = info.getNextLine()) != null){
				int paramStart = url.indexOf('?');
				if(paramStart > 0){
					url = url.substring(0, paramStart);
				}
				String fileSuffix= info.genNextOutFileName();
				int retCode = 0;
				String cmd = "wget -O " + outputDir + "/url_" + fileSuffix + " " + url;
				try{
					Process p = Runtime.getRuntime().exec(cmd);
					retCode = p.waitFor();
				}catch(IOException e){
					System.err.println(e.getMessage());
				}catch(InterruptedException e){
					System.err.println(e.getMessage());
				}
				info.linesResult(url, retCode == 0);
			}
		}
	}
	private int concurrency;
	private ArrayList<FetchThread> threads = new ArrayList<FetchThread>();
	private FileWalker info = new FileWalker();
	public PageFetcher(int concurrency){
		this.concurrency = concurrency;
	}
	public void setOutputDir(String oDir){
		info.setOutputDir(oDir);
	}
	public void addInputFile(String iFile){
		info.addInputFile(iFile);
	}
	
	public void start(){
		for(int i = 0; i < concurrency; ++ i){
			FetchThread t = new FetchThread(i, info);
			threads.add(t);
			t.start();
		}
		for(FetchThread t : threads){
			try{
				t.join();
			}
			catch(InterruptedException e){
				System.err.println(e.getMessage());
			}
		}
	}
	public void stat(){
		System.out.println("Fetch stat:\nTotal url: " + info.getLineNum() +
				"\nFetch OK url: " + info.getLinesResult(true) +
				"\nFetch fail url: " + info.getLinesResult(false)
				+'\n');
		if(info.getLinesResult(false) > 0){
			System.out.println("List of failed url(s):");
			for(String url: info.getFailedLines()){
				System.out.println(url);
			}
		}
	}
	private static void help(){
		System.err.println("usage: java <class>" + " -o <outputdir> <inputfile>+");
	}
	
	public static void main(String[] args){
		if(args.length < 3){
			help();
			return;
		}
		if(!args[0].equals("-o")){
			help();
			return;
		}
		PageFetcher f = new PageFetcher(10);
		
		f.setOutputDir(args[1]);
		for(int i = 2; i < args.length; ++ i){
			f.addInputFile(args[i]);
		}
		f.start();
		f.stat();
	}
}
