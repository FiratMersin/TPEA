package client;

import java.security.MessageDigest;
import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import block.Block;
import block.Blockchain;
import data.Data;

public class Auteur implements Runnable {

	private static Integer idCpt = 0;
	private static final Object mutex = new Object();
	private static final Object waitround = new Object();
	private static int awaiting_authors = 0; 
	private static boolean isFirstAwaken = true;
	
	private int myScore;
	private Blockchain myBlockChain;
	private String myId;
	private String myHashId;//SHA-256 of id
	private ArrayList<Character> myLetters;
	private ArrayList<Data> mySubmittedLetters;//submitted unused valid letters
	
	private ArrayList<Auteur> authors;
	private ArrayList<Politicien> politicians;
	
	private CyclicBarrier barrier;
	
	
	@SuppressWarnings("unchecked")
	public Auteur(Blockchain bc, int score, ArrayList<Character> letters, CyclicBarrier barrier) {
		this.myScore = score;
		this.myBlockChain = new Blockchain(bc);//copy bc
		this.myId = getMyId();
		this.myHashId = hash_id(this.myId);
		this.myLetters = (ArrayList<Character>) letters.clone();
		this.mySubmittedLetters = new ArrayList<>();
		this.authors = new ArrayList<Auteur>();
		this.politicians = new ArrayList<Politicien>();
		this.barrier = barrier;
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
	
	
	private String getMyId() {
		int id;
		synchronized (mutex) {
			id = Auteur.idCpt;
			Auteur.idCpt++;
		}
		String res = "Auteur_"+id;
		return res;
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
	
	public void setAuthors(ArrayList<Auteur> authors) {
		this.authors = authors;
	}
	
	public void setPoliticians(ArrayList<Politicien> politicians) {
		this.politicians = politicians;
	}
	
	public void cleanMySubmittedLetters() {
		ArrayList<Data> toRemove = new ArrayList<Data>();
		
		for(Data d : mySubmittedLetters) {
			if(d.getLastBlockInChain().gethashId() != myBlockChain.getLastBlock().gethashId()) {
				toRemove.add(d);
			}
		}
		mySubmittedLetters.removeAll(toRemove);
		
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
	
	
	
	public static Object getWaiter() {
		return Auteur.waitround;
	}
	
	

	@Override
	public void run() {
		while(myBlockChain.getBlocks().size()<20) {
			try {
				System.out.println("je suis la "+ myHashId);
				cleanMySubmittedLetters();
				Collections.shuffle(this.myLetters);
				Data d = new Data(myLetters.get(0), myHashId, myBlockChain.getLastBlock());
				mySubmittedLetters.add(d);
				for(Politicien p : politicians) {
					p.receiveLetter(d);
				}
				System.out.println(barrier.getNumberWaiting());
				barrier.await();
				System.out.println("release the kraken");
				synchronized (Politicien.getWaiter()) {
					System.out.println("J'arrive dans le bordel " + myHashId);
					if(isFirstAwaken) {
						System.out.println("premier arrve premier servi " + myHashId);
						isFirstAwaken=false;
						Politicien.getWaiter().notifyAll();
						System.out.println("j'ai notifie ta race ");
					}
					synchronized(waitround) {
						System.out.println("maintenant j'attend");
						waitround.wait();
						isFirstAwaken = true;
					}
					
				}
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BrokenBarrierException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(myBlockChain.getBlocks().size());
		}
		for(Block b : myBlockChain.getBlocks()) {
			for(Data d : b.getWord()) {
				if(d.getAuthorHashId() == myHashId) {
					myScore+=1; // toutes les lettres sont equiprobables donc 1
				}
			}
		}
		System.out.println("Score of " + myHashId + " = " +myScore);
		
		
	}
}
