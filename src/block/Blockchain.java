package block;

import java.util.ArrayList;

import data.Data;

public class Blockchain {
	
	ArrayList<Block> blockchain;
	
	public Blockchain() {
		super();
		this.blockchain = new ArrayList<>();
	}

	public Block getLastBlock() {
		
		assert (! blockchain.isEmpty());
		
		return blockchain.get(blockchain.size() -1);
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
				if(! dataLastBlock.equals(lastBlock)) {
					return false;
				}
				
				String authorHashId = data.getAuthorHashId();
				if(ids.contains(authorHashId)) {
					return false;
				}
				
				strB.append(data.getLetter());	
			}
			
			String word = strB.toString();
			
			//TODO 
				//word is in the dictionnary ??	
			//
			
			return true;
		}
	
}
