package block;

import java.util.ArrayList;

import data.Data;

public class Block {
	private ArrayList<Data> word;
	private String hashId; //block id
	private String politicianHashId;
	
	
	public Block(ArrayList<Data> word, String hashId, String politicianHashId) {
		super();
		this.word = word;
		this.hashId = hashId;
		this.politicianHashId = politicianHashId;
	}

	public boolean isEmptyWord() {
		return this.word.isEmpty();
	}

	public ArrayList<Data> getWord() {
		return word;
	}
	
	public boolean isBlockId(String hashId) {
		return hashId.equals(this.hashId);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == this) return true;
		
		if(! (o instanceof Block)) {
			return false;
		}
		
		Block b = (Block) o;
		
		return isBlockId(b.hashId); 
	}
	
	
}
