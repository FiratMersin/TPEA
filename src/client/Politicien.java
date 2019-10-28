package client;

import java.security.MessageDigest;
import java.util.ArrayList;

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
				
				//
				
				
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
