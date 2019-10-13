package data;

import block.Block;

public class Data {
	
	private char letter;
	private String authorHashId;//Author's id's hash (the one who send the letter)
	private Block lastBlockInChain;//Server always starts with a block
	
	public Data(char letter, String authorHashId, Block lastBlockInChain) {
		super();
		this.letter = letter;
		this.authorHashId = authorHashId;
		this.lastBlockInChain = lastBlockInChain;
	}

	public Block getLastBlockInChain() {
		return lastBlockInChain;
	}

	public String getAuthorHashId() {
		return authorHashId;
	}

	public char getLetter() {
		return letter;
	}
}
