package client;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

import javax.xml.bind.DatatypeConverter;

import block.Block;
import block.Blockchain;
import data.Data;
import round.Round;

public class Auteur implements Runnable {

	private static Integer idCpt = 0;
	private static final Object mutex = new Object();
	private static final Object waitround = new Object();
	private static final Object nb_auths_asleep = new Object();
	private static int awaiting_authors = 0; 
	private static boolean isFirstAwaken = true;
	private static Semaphore sem_auteurs;
	private boolean canTakeSemaphore;
	private static final Object wait_on_CanTakeSem = new Object();
	private static final Object mutex_read_CTS = new Object();
	
	private static int nb_asleep = 0;
	
	private int myScore;
	private Blockchain myBlockChain;
	private String myId;
	private String myHashId;//SHA-256 of id
	private ArrayList<Character> myLetters;
	private ArrayList<Data> mySubmittedLetters;//submitted unused valid letters
	private Round round;
	
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
		this.canTakeSemaphore = true;
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

	public synchronized void receiveBlock(Block b,Blockchain obc) {
		Blockchain bc = new Blockchain();
		for(Block bl : obc.getBlocks()) {
			bc.addBlock(bl);
		}
		
		//if(myBlockChain.isValidBlock(b)) {
			myBlockChain.addBlock(b);
		/*}else {
			
			//CONSENSUS ALGORITHM
			
			Block tested_B = b;
			ArrayList<Block> myblockList = myBlockChain.getBlocks();
			ArrayList<Block> new_bc = new ArrayList<Block>();
			ArrayList<Block> next_bc = new ArrayList<Block>();

			while(true) {
				
				for(int i = myblockList.size()-1 ;i>=0; i--) {
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
		}*/
//		System.out.println(myId +" author finished his consensus nique ta race");
	}
	
	
	
	public static Object getWaiter() {
		return Auteur.waitround;
	}
	
	

	@Override
	public void run() {
		int i = 1;
		while(myBlockChain.getBlocks().size()<21 && round.getNbAuthors() >=6) {
			cleanMySubmittedLetters();
			
			
			Collections.shuffle(this.myLetters);
			Data d = new Data(myLetters.get(0), myHashId, myBlockChain.getLastBlock());
			mySubmittedLetters.add(d);
			for(Politicien p : politicians) {
				synchronized(p) {
					p.receiveLetter(d);
				}
			}
//				System.out.println(this.myId+" ieme passage : "+i );
//				System.out.println(this.myId+" passed la barriere");
//				System.out.println("authors size = "+authors.size());
				
			//System.out.println("UN AUTEUR REVEIL TOUS LES POLITICIENS AU NOMBRE DE "+this.politicians.size());
			System.out.println(myBlockChain.getBlocks().size());
			i++;
			round.waitAuthorsRound();
		}
		round.unRegister(this);
		for(Block b : myBlockChain.getBlocks()) {
			for(Data d : b.getWord()) {
				if(d.getAuthorHashId() == myHashId) {
					myScore+=1; // toutes les lettres sont equiprobables donc 1
				}
			}
		}
		System.out.println("Score of Autor: " + myHashId + " = " +myScore);
		
		
	}
	
	public static Object getAuthsAsleep() {
		return nb_auths_asleep;
	}
	
	public void setRound(Round round) {
		this.round = round;
	}
	
	
	public static void setSemaphore(int permits) {
		Auteur.sem_auteurs = new Semaphore(permits);
	}
	
	public synchronized void authorLeft(Auteur a) {
		this.authors.remove(a);
	}
	
	public synchronized void PolLeft(Politicien a) {
		this.politicians.remove(a);
	}

	
	
	
}
