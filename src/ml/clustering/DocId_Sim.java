package ml.clustering;

public class DocId_Sim implements Comparable{
	int docId;
	double similarity;
	public DocId_Sim(int docId, double similarity) {
		this.docId=docId;
		this.similarity=similarity;
	}
	public String toString() {
		return ""+docId + " : " + similarity;
	}
	
	public int compareTo(Object o) {
		if (o instanceof DocId_Sim) {
			DocId_Sim ds = (DocId_Sim)o;
			if (similarity > ds.similarity) 
				return -1;
			else 
				return 1;	
		}
		return 0; // Incomparable, just say equal
	}
}
