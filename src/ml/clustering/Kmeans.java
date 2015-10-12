package ml.clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeSet;
import text.UnigramBuilder;

public class Kmeans {
	int K;
	long randomSeed;
	int interations;
	HashMap<Integer, HashMap<Integer, Double>> documents;
	HashMap<Integer, String> docid2name; // docid2name: <documentId, documentName>
	HashMap<Integer, TreeSet<DocId_Sim>> clusters = new HashMap<Integer, TreeSet<DocId_Sim>>();//clusters: <clusterId, HashMap<documentId, similarity>>
	HashMap<Integer, HashMap<Integer, Double>> centroids = new HashMap<Integer, HashMap<Integer, Double>>();
	
	public Kmeans(HashMap<Integer, HashMap<Integer, Double>> documents, HashMap<Integer, String> docid2name, int K, long randomSeed, int interations){
		this.K = K;
		this.documents = documents;
		this.docid2name = docid2name;
		this.randomSeed=randomSeed;		
		this.interations=interations;
	}
	
	private void initializeCluster(){
		Random random = new Random(randomSeed);
		for(int i = 1; i <= K; i++){
			int docId = Math.abs(random.nextInt())%documents.size();
			centroids.put(i, documents.get(docId));
			clusters.put(i, new TreeSet<DocId_Sim>());
		}		
	}
	
	private double computeSimilarity(HashMap<Integer, Double> document, HashMap<Integer, Double> centroid){
		int documentSize = document.size();
		int centroidSize = centroid.size();
		double dotProduct = 0;
		double norm1 = 0, norm2 = 0;

		//compute dot product and norm of two documents: 
		if(documentSize < centroidSize){		
			for(int index : document.keySet()){
				norm1 += Math.pow(document.get(index),2);
				if(centroid.containsKey(index)){
					dotProduct += document.get(index)*centroid.get(index);
				}
				for(double value : centroid.values())
					norm2 += Math.pow(value, 2);			
			}
		}
		else{
			for(int index : centroid.keySet()){
				norm2 += Math.pow(centroid.get(index),2);
				if(document.containsKey(index)){
					dotProduct += document.get(index)*centroid.get(index);
				}
			}
			for(double value : document.values())
				norm1 += Math.pow(value, 2);
		}		
		
		// Compute cosine similarity:
		return dotProduct/(Math.sqrt(norm1*norm2));
	}
	
	private ArrayList<Object>  getNearestCentroid(HashMap<Integer, Double> document, HashMap<Integer, HashMap<Integer, Double>> centroids){
		int nearestCentroidId = 0;		
		double maxSimilarity = 0;		
		ArrayList<Object> docId2sim = new ArrayList<Object>();
		for(int i=1; i<=K; i++){
			double similarity = computeSimilarity(document, centroids.get(i));
			if(similarity > maxSimilarity){
				maxSimilarity = similarity;
				nearestCentroidId = i;
			}
		}	
		docId2sim.add(nearestCentroidId);
		docId2sim.add(maxSimilarity);
		return docId2sim;
	}
	
	private void assignDocuments(){
			for(int docId : documents.keySet()){
			int assignedClusterId = (int)getNearestCentroid(documents.get(docId), centroids).get(0);
			double similarity = (double)getNearestCentroid(documents.get(docId), centroids).get(1);
			DocId_Sim clusteredDocument = new DocId_Sim(docId,similarity);
			clusters.get(assignedClusterId).add(clusteredDocument);
		}		
	}
	
	private void updateCentroids(){
		for(int i = 1; i <= K; i++){
			HashMap<Integer, Double> centroid = new HashMap<Integer, Double>();
			for(DocId_Sim doc2sim : clusters.get(i)){
				HashMap<Integer, Double> document = documents.get(doc2sim.docId);
				for(int index : document.keySet()){
					if(centroid.containsKey(index))
						centroid.put(index,centroid.get(index) + document.get(index));
					else
						centroid.put(index, (double)document.get(index));
				}
			}
			for(int j : centroid.keySet()){					
				centroid.put(j, centroid.get(j)/K);
			}
			centroids.put(i, centroid);
		}
	}
	
	private boolean checkCentroidsFixed(HashMap<Integer, HashMap<Integer, Double>> oldCentroids){
		HashMap<Integer, Double> centroid = new HashMap<Integer, Double>();
		HashMap<Integer, Double> oldCentroid = new HashMap<Integer, Double>();
		for(int i = 1; i <= K; i++){
			centroid = centroids.get(i);
			oldCentroid = oldCentroids.get(i);
			for(int index : centroid.keySet()){
				if (!oldCentroid.containsKey(index) || !oldCentroid.get(index).equals(centroid.get(index)))
					return false;			
			}
		}	
		return true;
	}

	private void printClusters(){
		for(int i=1; i <= K; i++){
			System.out.println("Cluster " +i+":");
			int top5 = 0;
			for(DocId_Sim ds : clusters.get(i)){
				if (top5++ >= 5)
					break;
				System.out.println(docid2name.get(ds.docId)+" : "+ds.similarity);
			}
			System.out.println("");
		}
	}
	
	@SuppressWarnings("unchecked")
	public void run()
	{
		boolean centroidsFixed = false;
		// Step1:Initialize Cluster
		initializeCluster();
		
		HashMap<Integer, HashMap<Integer, Double>> oldCentroids = new HashMap<Integer, HashMap<Integer, Double>>();
		
		for(int i=0; i<interations; i++){// The loop will stop if it runs up to the set iterations or the centorids no longer change within the iterations.
			if (centroidsFixed){
				System.out.println("Centroids are fixed after "+i+" interations");
				System.out.println("");
				break;
			}
			
			for(int j=1; j<=K; j++){
				clusters.get(j).clear();
			}
			
			// Step2: Assign documents
			assignDocuments();
			
			// Save old centroids
			oldCentroids = (HashMap<Integer, HashMap<Integer, Double>>) centroids.clone();
			
			// Step3: Update centroids
			updateCentroids();
			
			// Step4: Check whether centroids are fixed
			centroidsFixed = checkCentroidsFixed(oldCentroids);
		}

		// Step5: Print results
		printClusters();
		System.out.println("Centroids fixed: "+centroidsFixed);
	}
		
	public static void main(String[] args) {
		UnigramBuilder UB = new UnigramBuilder("data/blog_data_test/",1000,true);
		Kmeans kmeans = new Kmeans(UB.documents, UB.docid2name, 3, System.currentTimeMillis(), 20);		
		kmeans.run();
	}
}