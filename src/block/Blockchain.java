package block;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Words.Ptrie;
import data.Data;

public class Blockchain {
	
	ArrayList<Block> blockchain;
	
	private Lock lock = new ReentrantLock();
	
	private static int[] lettersPoint = {1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 10, 1, 2, 1, 1, 3, 8, 1, 1, 1, 1,
			4, 10, 10, 10, 10};
	

	public Blockchain() {
		super();
		this.blockchain = new ArrayList<>();
	}
	
	//copy constructor
	public Blockchain(Blockchain toCopy) {
		this.blockchain = new ArrayList<Block>();
		@SuppressWarnings("unchecked")
		ArrayList<Block> bl = (ArrayList<Block>) toCopy.getBlocks().clone();
		for(Block b : bl) {
			this.blockchain.add(new Block(b));
		}
		
		
		
	}
	
	public Blockchain(ArrayList<Block> block_list) {
		this.blockchain = new ArrayList<Block>();
		for(Block b : block_list) {
			this.blockchain.add(new Block(b));
		}
	}
	
	
	
	public ArrayList<Block> getBlocks(){
		lock.lock();
		ArrayList<Block> b = blockchain;
		lock.unlock();
		return b;
	}
	
	

	public Block getLastBlock() {
		
		assert (!(blockchain.size()>0));
		
		return blockchain.get(blockchain.size() -1);
	}
	
	//add a copy of b in the blockchain
	public void addBlock(Block b) {
		lock.lock();
		this.blockchain.add(new Block(b));
		lock.unlock();
	}
	
	//Checks if the block can be added to the blockchain
	public boolean isValidBlock(Block block, int word_size, Ptrie dico) {
		if(block.isEmptyWord()) return false;
		if(block.getWord().size()< word_size) return false;
		
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
		if(!dico.isWord(word)) return false;
		
		return true;
	}
	
	public int getBlockchainScore() {
		int score = 0;
		for(Block b : getBlocks()) {
			for(Data d : b.getWord()) {
				score += lettersPoint[d.getLetter() -'a'];
			}
		}
		return score;
	}
	
	
}
