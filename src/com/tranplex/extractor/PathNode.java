package com.tranplex.extractor;

import java.util.Arrays;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PathNode {
	class Child{
		String tag;
		public String getTag() {
			return tag;
		}
		public void setTag(String tag) {
			this.tag = tag;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public int getIndex() {
			return index;
		}
		public void setIndex(int index) {
			this.index = index;
		}
		String id;
		String[] classes;

		int index;
		PathNode child;
		Child sibling;
		public void setChild(PathNode childNode) {
			this.child = childNode;
		}
		public void setSibling(Child sibling){
			this.sibling = sibling;
		}
		@Override
		public String toString() {
			return "Child [tag=" + tag + ", id=" + id + ", classes="
					+ Arrays.toString(classes) + ", index=" + index
					+ ", child=" + child +
					//", sibling=" + sibling +
					"]";
		}
		public void setClasses(String[] classes) {
			this.classes = classes;
		}
		public void setAttr(String attr) {
			this.child.setAttr(attr);
		}
	}
	
	class NodeWriter implements IParallelNodeWriter{
		public void write(String content){
			System.out.println(content);
		}

		@Override
		public void switchBack() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void switchToParallel() {
			// TODO Auto-generated method stub
			
		}
	}
	String name;
	Element element;
	String attr;
	Child child = new Child();
	Child parallelChild;
	boolean allChild;
	boolean parallel;
	boolean leaf;
	public boolean isAllChild() {
		return allChild;
	}
	public void setAllChild(boolean allChild) {
		this.allChild = allChild;
	}
	public boolean isParallel() {
		return parallel;
	}
	public void setParallel(boolean parallel) {
		this.parallel = parallel;
	}
	public boolean isLeaf() {
		return leaf;
	}
	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}
	
	public Element getTagChildAt(Element e, String tag, int index){
		int nTag = 0;
		int nEle = e.children().size();
		for(int i = 0; i < nEle; ++ i){
			if(e.child(i).tagName().equals(tag)){
				if(nTag == index){
					return e.child(i);
				}
				++ nTag;
			}
		}
		return null;
	}
	
	public void extractDoc(Document doc, IParallelNodeWriter writer){
		extract(doc, writer);
	}
	
	public void extract(Element element, IParallelNodeWriter writer){
		if(this.leaf){
			writeElement(element, writer);
			return;
		}
		else if(this.parallel){
			PathNode sourceNode = getChildNode(child, element);
            writer.switchBack();
			sourceNode.extract(writer);
			PathNode targetNode = getChildNode(parallelChild, element);
            writer.switchToParallel();
			targetNode.extract(writer);
			return;
		}
		else if(this.allChild){
			Elements children = null;
			children = element.children();
			int nChildren = children.size();
			for(int i = 0; i < nChildren; ++ i){
				if(child.tag != null){
					if(!children.get(i).tagName().equals(child.tag)){
						continue;
					}
				}
				boolean testPassed = true;
				if(child.classes != null){
					for(String classValue : child.classes){
						if(!children.get(i).hasClass(classValue)){
							testPassed = false;
							break;
						}
					}
					if(!testPassed){
						continue;
					}
				}
				child.child.extract(children.get(i), writer);
			}
			return;
		}
		PathNode node = this;
		node.element = element;
		while(true){
			node = node.getChildNode(node.child, node.element);
			if(node.element == null){
				return;
			}
			if(node.leaf || node.parallel || node.allChild){
				node.extract(node.element, writer);
				return;
			}
		}
			
	}
	
	public void extract(IParallelNodeWriter writer){
		extract(this.element, writer);
	}
	
	public PathNode getChildNode(Child child, Element element){
//		System.out.println("Ready to get child of: " + child);
		if(child.id != null){
			child.child.element = element.getElementById(child.id);
		}
		else{
//			System.out.println("element is: " + element);
			child.child.element = getTagChildAt(element, child.tag, child.index);// element.getElementsByTag(child.tag).get(child.index);
		}
		return child.child;
	}
	public void writeElement(Element element, IParallelNodeWriter writer){
		if(this.attr != null){
			writer.write(element.attr(this.attr));
		}
		else{
			writer.write(element.text());
		}
	}
	public Child getChild() {
		return child;
	}
	public synchronized Child getParallelChild() {
		if(this.parallelChild == null){
			this.parallelChild = new Child();
		}
		return this.parallelChild;
	}
	
	@Override
	public String toString() {
		return "PathNode [name=" + name + ", attr=" + attr +
				//", element=" + element + 
				//", parallelChild=" + parallelChild +
				", allChild="
				+ allChild + ", parallel=" + parallel + ", leaf=" + leaf + "]\nchild=" + child;
	}
	public void dump(){
		System.out.println(this);
	}
	public void setAttr(String attr) {
		this.attr = attr;
	}

}
