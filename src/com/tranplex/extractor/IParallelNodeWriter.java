package com.tranplex.extractor;

public interface IParallelNodeWriter {
	public void switchBack();
	public void switchToParallel();
	public void write(String content);
	public void close();
}
