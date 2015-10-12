/** Simple Unigram analysis of data.
 * 
 * @author Scott Sanner
 */

package text;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import nlp.nicta.filters.StopWordChecker;
import util.DocUtils;
import util.FileFinder;

public class UnigramBuilder {

	public StopWordChecker _swc = new StopWordChecker();
	public TreeSet<WordCount> _wordCounts;
	public HashMap<String,Integer> _topWord2Index;
	
	//represent all documents in sparse representation:
	HashMap<Integer, HashMap<String, Double>> temp_document = new HashMap<Integer, HashMap<String, Double>>();
	public HashMap<Integer, String> docid2name = new HashMap<Integer, String>();//docid --> filename.
	public HashMap<Integer, HashMap<Integer, Double>> documents = new HashMap<Integer, HashMap<Integer, Double>>();
	
	
	public UnigramBuilder(String directory, int num_top_words, boolean ignore_stop_words) {
		ArrayList<File> files = FileFinder.GetAllFiles(directory, ".txt", true);
		HashMap<String,WordCount> word2count = new HashMap<String,WordCount>();
		//System.out.println("Found " + files.size() + " files.");
		
		int file_count = 0;
		for (File f : files) {
			String filename =  f.getName();
			//System.out.println("filename="+f.getName());		
			int docId = file_count;
            docid2name.put(docId,filename);
			HashMap<String, Double> singleFile = new HashMap<String, Double>();
			String file_content = DocUtils.ReadFile(f); 
			
			//word_counts:<Term，TermFrequent>存一篇文章的词，和这个词在这一片文章中的词频
			Map<Object,Double> word_counts = DocUtils.ConvertToFeatureMap(file_content); 
			
			//word2count:<Term，(Term，TermFrequent)>存所有文章的词(不重复)，和这个词在所有文章中的词频
			for (Map.Entry<Object, Double> me : word_counts.entrySet()) {
				String key = (String)me.getKey();
				WordCount wc = word2count.get(key);
				if (wc == null) {
					wc = new WordCount(key, (int)me.getValue().doubleValue());
					word2count.put(key, wc);
				} 
				else
					//wc._count++; //Wrong！应该为: 
					wc._count+=(int)me.getValue().doubleValue();
				//singleFile:<Term，TermFrequent>存一片文章的词和这个词在这篇文章中的词频
				singleFile.put(key, me.getValue().doubleValue());
			}			
			//temp_document:<DocumentId，hashMap<Term，TermFrequent>>存一片文章的ID,
			temp_document.put(docId, singleFile);			
			if (++file_count % 500 == 0){
				//System.out.println("Read " + file_count + " files.");		
			}
		}
		System.out.println("Extracted " + word2count.size() + " unique tokens.");

		//_wordCounts:(Term，TermFrequent)：存在所有文章中词和词频，按词频由高到低排序后的
		_wordCounts = new TreeSet<WordCount>(word2count.values()); 
		//_topWord2Index:<Term，TermIndex>：存在所有文章中词频排前num_top_word的词和这个词的编号(在_topWord2Index的编号)
		_topWord2Index = new HashMap<String,Integer>();
		int index = 0;
		for (WordCount wc : _wordCounts) {
			if (ignore_stop_words && _swc.isStopWord(wc._word))
				continue;
			//System.out.println("[index:" + index + "] " + wc);
			_topWord2Index.put(wc._word, index);
			if (++index >= num_top_words)
				break;
		}
		
		//deal with temp_document to compute the wanted document:
		//for each file, only keeps the terms that occurs in _topWord2Index:
		//document: <DocumentId，hashMap<TermIndex，TermFrequent>> 存所有文章的ID，和这片文章中的高频词的词号与词频，(在_topWord2Index的词)
		for(int key : temp_document.keySet())
		{
			HashMap<String, Double> single_file = temp_document.get(key);
			HashMap<Integer, Double> selected_single_file = new HashMap<Integer, Double>();
			for(String term : single_file.keySet())
			{
				if(_topWord2Index.containsKey(term))
					selected_single_file.put(_topWord2Index.get(term), single_file.get(term));
			}
			documents.put(key, selected_single_file);
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//UnigramBuilder UB = new UnigramBuilder("data/two_newsgroups/" /* data source */, 
		//		/* num top words */100, /* remove stopwords */true);
		
		UnigramBuilder UB = new UnigramBuilder("blog_data/",100,true);
		
		for(int docId : UB.documents.keySet())
		{
			System.out.println("docId = " + docId);
			HashMap<Integer, Double> m = UB.documents.get(docId);
			for(int j : m.keySet())
			{
				System.out.print("index:"+j+"frequency:"+m.get(j));
			}
			System.out.println("\n\n");
		}
	}

}
