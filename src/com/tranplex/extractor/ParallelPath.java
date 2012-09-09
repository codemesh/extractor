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
        if(node.indexOf('#') >= 0){
            parseIdNode(node, curNode, child);
            child.setChild(childNode);
        }
        // classes-parsing should go before all-parsing, as classes-parsing can include all-parsing
        else if(node.indexOf('.') > 0){
            parseClassesNode(node, curNode, child);
            child.setChild(childNode);
        }
        else if(node.indexOf('*') > 0){
            parseAllNode(node, curNode, child);
            child.setChild(childNode);
        }
        else if(node.indexOf('|') > 0){
            String[] parallel = node.split("\\|");
            parseNode(parallel[0], curNode, childNode, child);
            parseNode(parallel[1], curNode, childNode, curNode.getParallelChild());
            curNode.getChild().setSibling(curNode.getParallelChild());
            child.setChild(childNode);
            curNode.getParallelChild().setChild(childNode);
            curNode.setParallel(true);
        }
        else if(node.indexOf('@') > 0){
            parseIndexNode(node, curNode, child);
            child.setChild(childNode);
        }
        else{
            parseTagNode(node, curNode, child);
            child.setChild(childNode);
        }
    }
	public void parseIdNode(String node, PathNode curNode, PathNode.Child child){
		String[] tagAndId = node.split("#");
		child.setTag(tagAndId[0]);
		child.setId(tagAndId[1]);
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
        String[] tagAndIndex = node.split("@");
        parseTagNode(tagAndIndex[0], curNode, child);
        child.setIndex(Integer.parseInt(tagAndIndex[1]));
    }

    public static void main(String[] args){
    	ParallelPath p = new ParallelPath();
    	PathNode title = p.parseFromString("html/body/div#page/div#main/div@1/div@1/div.articleTitle/div|div@1/h2");
    	PathNode content= p.parseFromString("html/body/div#page/div#main/div@1/div@1/div.articleContent*/div@0|div@1/p");
    	//root.dump();
    	File input = new File("/home/fengyu/Downloads/nytimes3.html");
    	Document doc = null;
    	try{
    		doc = Jsoup.parse(input, "UTF-8");
    	}
    	catch (IOException e){
    		System.out.println(e.getMessage());
    		return;
    	}
    	PathNode.NodeWriter writer = content.new NodeWriter();
    	
    	title.extractDoc(doc, writer);
    	content.extractDoc(doc, writer);
    }
}
