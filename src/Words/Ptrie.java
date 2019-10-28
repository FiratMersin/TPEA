package Words;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Ptrie {

	private ArrayList<Node> roots;
	
	public Ptrie(String filename) {
		this.roots = new ArrayList<>();
		
		File file = new File(filename); 
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
		
			String word; 
			while ((word = br.readLine()) != null) {
			  this.addNewWord(word);	
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public void addNewWord(String word) {
		int size = word.length();
		char first = word.charAt(0);
		for(Node n : roots) {
			if(n.getLetter() == first) {
				if(size == 1) {
					n.setEndOfWord(true);
					return;
				}else {
					n.addWord(word.substring(1));
					return;
				}
			}
		}
		Node newRoot = new Node(first, false);
		if(size == 1){
			newRoot.setEndOfWord(true);
		}else {
			newRoot.addWord(word.substring(1));
		}
		roots.add(newRoot);
	}
	
	public boolean isWord(String word) {
		int size = word.length();
		int first = word.charAt(0);
		if(size == 1) {
			for(Node n : roots) {
				if(n.getLetter() == first) {
					return true;
				}
			}
		}else {
			for(Node n : roots) {
				if(n.getLetter() == first) {
					return n.isWord(word.substring(1));
				}
			}
		}
		return false;
		
	}	
}
