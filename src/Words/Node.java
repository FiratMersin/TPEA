package Words;

import java.util.ArrayList;

public class Node {
	private char letter;
	private boolean isEndOfWord;
	private ArrayList<Node> childs;
	
	public Node(char letter, boolean isEndOfWord) {
		super();
		this.letter = letter;
		this.isEndOfWord = isEndOfWord;
		this.childs = new ArrayList<>();
	}

	public char getLetter() {
		return letter;
	}

	public boolean isEndOfWord() {
		return isEndOfWord;
	}

	public ArrayList<Node> getChilds() {
		return childs;
	}
	
	public void setEndOfWord(boolean isEndOfWord) {
		this.isEndOfWord = isEndOfWord;
	}
	
	public void addWord(String word) {
		int size = word.length();
		char first = word.charAt(0);
		for(Node n : childs) {
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
		Node newChild = new Node(first, false);
		if(size == 1){
			newChild.setEndOfWord(true);
		}else {
			newChild.addWord(word.substring(1));
		}
		childs.add(newChild);
	}
	
	public boolean isWord(String word) {
		int size = word.length();
		int first = word.charAt(0);
		if(size == 1) {
			for(Node n : childs) {
				if(n.getLetter() == first) {
					return true;
				}
			}
		}else {
			for(Node n : childs) {
				if(n.getLetter() == first) {
					return n.isWord(word.substring(1));
				}
			}
		}
		return false;
	}
}
