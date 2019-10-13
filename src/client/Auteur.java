package client;

import java.security.MessageDigest;
//import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;

import block.Block;
import block.Blockchain;
import data.Data;

public class Auteur {

	private static Integer idCpt = 0;
	private static final Object mutex = new Object();
	
	private int myScore;
	private Blockchain myBlockChain;
	private String myId;
	private String myHashId;//SHA-256 of id
	private ArrayList<Character> myLetters;
	private ArrayList<Data> mySubmittedLetters;//submitted unused valid letters
	
	@SuppressWarnings("unchecked")
	public Auteur(Blockchain bc, int score, ArrayList<Character> letters) {
		this.myScore = score;
		this.myBlockChain = new Blockchain(bc);//copy bc
		this.myId = getMyId();
		this.myHashId = hash_id(this.myId);
		this.myLetters = (ArrayList<Character>) letters.clone();
		this.mySubmittedLetters = new ArrayList<>();
	}
	
	private String getMyId() {
		int id;
		synchronized (mutex) {
			id = Auteur.idCpt;
			Auteur.idCpt++;
		}
		String res = "Auteur_"+id;
		return res;
	}
	
	
	public static String hash_id(String id) {
		
		String result = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(id.getBytes("UTF-8"));
                    
            //decommenter la ligne suivante
            //result = DatatypeConverter.printHexBinary(hash); 
                        
            return result.substring(0,16);
        }catch(Exception ex) {
            ex.printStackTrace();
        }
        return result;
	}
	
	//if block isValid then add it to the local blockchain,
	//take back all letters that have been submitted but not used in the new word
	//and return true
	//else return false
	public boolean validNewBlock(Block newBlock) {
		if(this.myBlockChain.isValidBlock(newBlock)) {
			
			//TODO update this.myScore
			
			myBlockChain.addBlock(newBlock);//add the new block
			
			//take back unused letters 
			ArrayList<Data> word = newBlock.getWord();
			ArrayList<Character> usedInNewBlock = new ArrayList<>();
			for(Data d : word) {
				if(d.getAuthorHashId() == this.myHashId) {
					usedInNewBlock.add(d.getLetter());
				}
			}
			for(Data d : this.mySubmittedLetters) {
				if(usedInNewBlock.contains(d.getLetter())) {
					usedInNewBlock.remove(d.getLetter());
				}else {
					this.myLetters.add(d.getLetter());
				}
			}
			this.mySubmittedLetters.clear();
			
			return true;
		}else {
			return false;
		}
	}
	
	//TODO method : submit letter
	
	
}
