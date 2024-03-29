package Words;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import data.Data;

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
	
	public ArrayList<Data> findWord(ArrayList<Data> datas) {
		if(datas.size()==0) {
			return datas;
		}
		ArrayList<Data> word = new ArrayList<>();
		ArrayList<Data> letters = new ArrayList<>();
		letters.addAll(datas);
		String validWord = "";
		StringBuilder sb = new StringBuilder();
		Node actual = null;
		while(true) {
			validWord = "";
			sb = new StringBuilder();
			actual = null;
			Collections.shuffle(letters);
			Data next = letters.get(0);
			for(Node n : roots) {
				if(n.getLetter() == next.getLetter()) {
					actual = n;
					sb.append(next.getLetter());
					word.add(next);
					if(n.isEndOfWord()) {
						validWord = sb.toString();
					}
					letters.remove(next);
					break;
				}
			}
			if(actual == null) continue;
			
			boolean foundOne = true;
			while(letters.isEmpty() == false && foundOne == true) {
			
				Collections.shuffle(letters);
				next = letters.get(0);
				foundOne = false;
				for(Node n : actual.getChilds()) {
					if(n.getLetter() == next.getLetter()) {
						actual = n;
						sb.append(next.getLetter());
						word.add(next);
						if(n.isEndOfWord()) {
							validWord = sb.toString();
						}
						letters.remove(next);
						foundOne = true;
						break;
					}
				}
			}
			if(validWord.length() != 0) break;
		}
		while(word.size() > validWord.length()) {
			word.remove(word.size()-1);
		}
		return word;
	}
}
