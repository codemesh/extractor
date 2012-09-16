package com.tranplex.extractor;

import java.io.File;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * @author fengyu
 *
 */
public class ParallelPath {
	/**
	 * @param path The path to content. e.g.
	 * /html/body/div#page/div#main/div@2/div@2/div.articleContent* /div@1|div@2/p
	 * @return
	 */
	public PathNode parseFromString(String path){
		String[] nodes = path.split("/");
		PathNode root = new PathNode();
        PathNode curNode = root;
		for(String node : nodes){
			PathNode childNode= new PathNode();
            parseNode(node, curNode, childNode, curNode.getChild());
            curNode = childNode;
        }
        // this should recursive into siblings.
        curNode.setLeaf(true);
        return root;
	}
	public void parseNode(String node, PathNode curNode, PathNode childNode, PathNode.Child child){
		child.setChild(childNode);
        if(node.indexOf('#') >= 0){
            parseIdNode(node, curNode, child);
        }
        // classes-parsing should go before all-parsing, as classes-parsing can include all-parsing
        else if(node.indexOf('.') > 0){
            parseClassesNode(node, curNode, child);
        }
        else if(node.indexOf('*') > 0){
            parseAllNode(node, curNode, child);
        }
        else if(node.indexOf('|') > 0){
            String[] parallel = node.split("\\|");
            parseNode(parallel[0], curNode, childNode, child);
            parseNode(parallel[1], curNode, childNode, curNode.getParallelChild());
            curNode.getChild().setSibling(curNode.getParallelChild());
            curNode.getParallelChild().setChild(childNode);
            curNode.setParallel(true);
        }
        else if(node.indexOf('@') > 0){
            parseIndexNode(node, curNode, child);
        }
        else if(node.indexOf(':') > 0){
        	parseAttrNode(node, curNode, child);
        }
        else{
            parseTagNode(node, curNode, child);
        }
    }
	public void parseIdNode(String node, PathNode curNode, PathNode.Child child){
		String[] tagAndId = node.split("#|:");
		child.setTag(tagAndId[0]);
		child.setId(tagAndId[1]);
		if(tagAndId.length == 3){
			curNode.setAttr(tagAndId[2]);
		}
	}
	
	public void parseClassesNode(String node, PathNode curNode, PathNode.Child child){
		String[] tagAndClasses = node.split("\\.");
		child.setTag(tagAndClasses[0]);
		if(tagAndClasses[tagAndClasses.length - 1].indexOf('*') > 0){
			curNode.setAllChild(true);
			tagAndClasses[tagAndClasses.length - 1] =
					tagAndClasses[tagAndClasses.length - 1].substring(0, tagAndClasses[tagAndClasses.length - 1].length() - 1);
			
		}
		String[] classes = new String[tagAndClasses.length - 1];
		for(int i = 1; i < tagAndClasses.length; ++ i){
			classes[i - 1] = tagAndClasses[i];
		}
		child.setClasses(classes);
	}

    public void parseTagNode(String node, PathNode curNode, PathNode.Child child){
        child.setTag(node);
    }

    public void parseAllNode(String node, PathNode curNode, PathNode.Child child){
        String tag = node.substring(0, node.length() - 1);
        parseTagNode(tag, curNode, child);
        curNode.setAllChild(true);
    }

    public void parseIndexNode(String node, PathNode curNode, PathNode.Child child){
        String[] tagAndIndex = node.split("@|:");
        parseTagNode(tagAndIndex[0], curNode, child);
        child.setIndex(Integer.parseInt(tagAndIndex[1]));
        if(tagAndIndex.length == 3){
        	curNode.setAttr(tagAndIndex[2]);
        }
    }
    
    public void parseAttrNode(String node, PathNode curNode, PathNode.Child child){
    	String[] tagAndAttr = node.split(":");
    	parseTagNode(tagAndAttr[0], curNode, child);
    	child.setAttr(tagAndAttr[1]);
    }

    public static void main(String[] args){
    	ParallelPath p = new ParallelPath();
    	PathNode title = p.parseFromString("html/body/div#page/div#main/div@1/div@1/div.articleTitle/div|div@1/h2");
    	PathNode nytimes= p.parseFromString("html/body/div#page/div#main/div@1/div@1/div.articleContent*/div@0|div@1");
    	//PathNode google = p.parseFromString("html/body/div@1/div@1/div/div@3/div@1/div@1/div@1/div/ol/li.g*/div/h3/a");
    	PathNode google = p.parseFromString("html/body/div@3/div@1/div/div@3/div@1/div@1/div@1/div@1/div/ol/li.g*/div/h3/a:href");
    	//File input = new File("/home/fengyu/data/mt/times_google/business_2012_09_p2.html");
    	File input = new File("/home/fengyu/data/mt/parallel/NYT/fetched_2012-09-152349/url_2012091523490004");
    	Document doc = null;
    	try{
    		doc = Jsoup.parse(input, "UTF-8");
    		//doc = Jsoup.connect("http://http://cn.nytimes.com/article/world/2012/09/10/c10arableft/dual/").get();
    	}
    	catch (IOException e){
    		System.out.println(e.getMessage());
    		return;
    	}
    	PathNode.NodeWriter writer = google.new NodeWriter();
    	//google.dump();
    	//title.extractDoc(doc, writer);
    	nytimes.extractDoc(doc, writer);
    }
}
