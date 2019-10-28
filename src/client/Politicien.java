package client;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;

import javax.xml.bind.DatatypeConverter;

import Words.Ptrie;
import block.Block;
import block.Blockchain;
import data.Data;

public class Politicien implements Runnable{

	private static Integer idCpt = 0;
	private static final Object mutex = new Object();
	private static final Object waitround = new Object();

	private int cpt;
	private int myScore;
	private Blockchain myBlockChain;
	private String myId;
	private String myHashId;//SHA-256 of id
	private ArrayList<Data> lettersFromAuthors;//submitted unused valid letters
	
	private ArrayList<Auteur> authors;
	private ArrayList<Politicien> politicians;
	
	private int word_size;
	private Ptrie dico;
	
	
	

	@SuppressWarnings("unchecked")
	public Politicien(Blockchain bc, int score, ArrayList<Character> lettersFromAuthors, int word_size, Ptrie arbre) {
		this.cpt = 0;
		this.myScore = score;
		this.myBlockChain = new Blockchain(bc);//copy bc
		this.myId = getMyId();
		this.myHashId = hash_id(this.myId);
		this.lettersFromAuthors = (ArrayList<Data>) lettersFromAuthors.clone();
		this.authors = new ArrayList<Auteur>();
		this.politicians = new ArrayList<Politicien>();
		this.word_size = word_size;
		this.dico = arbre;
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

		for(Auteur a : authors) {
			a.receiveBlock(b, bc);
		}
		for(Politicien p : politicians) {
			p.receiveBlock(b, bc);
		}

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
	
	public synchronized void receiveLetter(Data d) {
		lettersFromAuthors.add(d);
	}
	
	
	public void cleanAuthorsLetters() {
		ArrayList<Data> toRemove = new ArrayList<Data>();
		
		for(Data d : lettersFromAuthors) {
			if(d.getLastBlockInChain().gethashId() != myBlockChain.getLastBlock().gethashId()) {
				toRemove.add(d);
			}
		}
		lettersFromAuthors.removeAll(toRemove);
	}
	
	public static Object getWaiter() {
		return Politicien.waitround;
	}
	
	

	@Override
	public void run() {
		while(myBlockChain.getBlocks().size()<20) {
			synchronized (waitround) {
				try {
					System.out.println("je m'endors");
					waitround.wait();
					System.out.println("je me reveille");
					ArrayList<Data> word = new ArrayList<Data>();
					for(int i = 0; i < 6; i++ ) {
						Collections.shuffle(lettersFromAuthors);
						word.add(lettersFromAuthors.get(0));
					}
					ArrayList<Data> mot = dico.findWord(word);
					String s = "";
					for(Data d : mot) {
						s+=d.getLetter();
					}
					Block block = new Block(myBlockChain.getLastBlock().gethashId(), mot, hash_id(s+myHashId+idCpt) , myHashId);	
					for(Auteur a : authors) {
						a.receiveBlock(block, myBlockChain);
					}
					for(Politicien p : politicians) {
						p.receiveBlock(block, myBlockChain);
					}
					myBlockChain.addBlock(block);
					
					synchronized(Auteur.getWaiter()) {
						Auteur.getWaiter().notifyAll();
					}
					
					
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			cleanAuthorsLetters();
			
		}
		
		
		
		
		
		
	}

}
