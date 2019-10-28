package block;

import java.util.ArrayList;

import data.Data;

public class Blockchain {
	
	ArrayList<Block> blockchain;
	
	//TODO when the game starts, the blockchain contains one block

	public Blockchain() {
		super();
		this.blockchain = new ArrayList<>();
	}
	
	//copy constructor
	public Blockchain(Blockchain toCopy) {
		this.blockchain = new ArrayList<>();
		for(Block b : blockchain) {
			this.blockchain.add(new Block(b));
		}
	}
	
	public Blockchain(ArrayList<Block> block_list) {
		this.blockchain = new ArrayList<>();
		for(Block b : block_list) {
			this.blockchain.add(new Block(b));
		}
	}
	
	
	
	public ArrayList<Block> getBlocks(){
		return blockchain;
	}
	
	

	public Block getLastBlock() {
		
		assert (! blockchain.isEmpty());
		
		return blockchain.get(blockchain.size() -1);
	}
	
	//add a copy of b in the blockchain
	public void addBlock(Block b) {
		this.blockchain.add(new Block(b));
	}
	
	//Checks if the block can be added to the blockchain
	public boolean isValidBlock(Block block) {
		if(block.isEmptyWord()) return false;
		
		StringBuilder strB = new StringBuilder();
		
		Block lastBlock = this.getLastBlock();//last block 
		ArrayList<String> ids= new ArrayList<>();
		//checking if all the letters in the new blocks' word are signed with the last block
		//and if all the letters are from different authors
		
		for(Data data : block.getWord()) {
			Block dataLastBlock = data.getLastBlockInChain();
			if(! dataLastBlock.equals(lastBlock)) {//wrong signature
				return false;
			}
			
			String authorHashId = data.getAuthorHashId();
			if(ids.contains(authorHashId)) {//2 letters from the same author
				return false;
			}
			
			strB.append(data.getLetter());	
		}
		
		String word = strB.toString();//the word of the new block
		
		//TODO 
			//word is in the dictionnary ??	
		//
		
		return true;
	}
}
