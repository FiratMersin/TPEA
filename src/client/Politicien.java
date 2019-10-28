package client;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;

import javax.xml.bind.DatatypeConverter;

import block.Block;
import block.Blockchain;
import data.Data;

public class Politicien implements Runnable{

	private static Integer idCpt = 0;
	private static final Object mutex = new Object();

	private int cpt;
	private int myScore;
	private Blockchain myBlockChain;
	private String myId;
	private String myHashId;//SHA-256 of id
	private ArrayList<Data> lettersFromAuthors;//submitted unused valid letters

	@SuppressWarnings("unchecked")
	public Politicien(Blockchain bc, int score, ArrayList<Character> lettersFromAuthors) {
		this.cpt = 0;
		this.myScore = score;
		this.myBlockChain = new Blockchain(bc);//copy bc
		this.myId = getMyId();
		this.myHashId = hash_id(this.myId);
		this.lettersFromAuthors = (ArrayList<Data>) lettersFromAuthors.clone();
	}

	//hash the id using SHA-256
	public static String hash_id(String id) {
		String result = null;
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(id.getBytes("UTF-8")); 
			result = DatatypeConverter.printHexBinary(hash);                         
			return result;
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

	public int getMyScore() {
		return myScore;
	}

	private String getMyId() {
		int id;
		synchronized (mutex) {
			id = Politicien.idCpt;
			Politicien.idCpt++;
		}
		String res = "Politicien_"+id;
		return res;
	}

	public ArrayList<Data> getWord(){

		//return the best word which can be created

		return null;

	}

	public void sendBlock(Block b,Blockchain bc) {

		//broadcast the new block to others


	}

	public void receiveBlock(Block b,Blockchain bc) {

		if(myBlockChain.isValidBlock(b)) {
			myBlockChain.addBlock(b);
		}else {
			
			//CONSENSUS ALGORITHM
			
			Block tested_B = b;
			ArrayList<Block> myblockList = myBlockChain.getBlocks();
			ArrayList<Block> new_bc = new ArrayList<Block>();
			ArrayList<Block> next_bc = new ArrayList<Block>();

			while(true) {
				
				for(int i = myblockList.size()-1 ;i>=0; i++) {
					if(myblockList.get(i).gethashId() == tested_B.getprevioushashId()) {
						for(Block bl: myblockList) {
							
							if(bl.gethashId() == tested_B.getprevioushashId()) { // block in the blockchain
								
								new_bc.add(bl);//build the new blockchain
								
								//adding blocks after the block tested if their exist
								Collections.reverse(next_bc);
								for(Block nextB: next_bc) {
									new_bc.add(nextB);
								}
								
								this.myBlockChain = new Blockchain(new_bc);
								return;
								
							}else {
								new_bc.add(bl);//build the new blockchain
							}
						}
					}else {
						continue;
					}
				}
				
				//the blockchain after the block received
				next_bc.add(tested_B);
				
				//getting the precedent block
				for(Block newB: bc.getBlocks()) {
					if(newB.gethashId() == tested_B.getprevioushashId()) {
						tested_B = newB;
					}
				}
			}
		}
	}

	public void createBlock() {

		ArrayList<Data> word = getWord();
		String BlockHashId = hash_id( myHashId+myBlockChain.getLastBlock().gethashId()+ cpt);

		Block newB = new Block(myBlockChain.getLastBlock().gethashId(),
				word,
				BlockHashId,
				myHashId);

		if(!myBlockChain.isValidBlock(newB))return;


		sendBlock(newB,myBlockChain);

		myBlockChain.addBlock(newB);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

}
