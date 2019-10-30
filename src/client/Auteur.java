package client;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import javax.xml.bind.DatatypeConverter;

import Words.Ptrie;
import block.Block;
import block.Blockchain;
import data.Data;
import round.Round;

public class Auteur implements Runnable {

	private static Integer idCpt = 0;
	private static final Object mutex = new Object();
	
	private int myScore;
	private Blockchain myBlockChain;
	private String myId;
	private String myHashId;//SHA-256 of id
	private ArrayList<Character> myLetters;
	private ArrayList<Data> mySubmittedLetters;//submitted unused valid letters
	private Round round;
	private int word_size;
	
	private ArrayList<Auteur> authors;
	private ArrayList<Politicien> politicians;
	
	
	private ArrayList<Blockchain> buffer;
	private final Object mutexBuffer = new Object();

	private Ptrie dico;
	
	private static int[] lettersPoint = {1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 10, 1, 2, 1, 1, 3, 8, 1, 1, 1, 1,
			4, 10, 10, 10, 10};
	
	@SuppressWarnings("unchecked")
	public Auteur(Blockchain bc, int score, ArrayList<Character> letters, int word_size, Ptrie dico) {
		this.myScore = score;
		this.myBlockChain = new Blockchain(bc);//copy bc
		this.myId = getMyId();
		this.myHashId = hash_id(this.myId);
		this.myLetters = (ArrayList<Character>) letters.clone();
		this.mySubmittedLetters = new ArrayList<>();
		this.authors = new ArrayList<Auteur>();
		this.politicians = new ArrayList<Politicien>();
		this.word_size = word_size;
		this.dico = dico;
		this.buffer = new ArrayList<Blockchain>();
	}
	
	//if block isValid then add it to the local blockchain,
	//take back all letters that have been submitted but not used in the new word
	//and return true
	//else return false
	public boolean validNewBlock(Block newBlock) {
		if(this.myBlockChain.isValidBlock(newBlock, word_size, dico)) {
			
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

	public synchronized void receiveBlock(Blockchain obc) {
		Blockchain tmp = new Blockchain(obc);
		synchronized (mutexBuffer) {
			buffer.add(tmp);
		}
	}

	@Override
	public void run() {
		while(myBlockChain.getBlocks().size()<21 && round.getNbAuthors() >= this.word_size) {
			
			
			cleanMySubmittedLetters();
			
			
			Collections.shuffle(this.myLetters);
			Data d = new Data(myLetters.get(0), myHashId, myBlockChain.getLastBlock());
			mySubmittedLetters.add(d);
			for(Politicien p : politicians) {
				synchronized(p) {
					p.receiveLetter(d);
				}
			}
			
			round.waitAuthorsRound();
			synchronized (mutexBuffer) {
				if(buffer.size()==0) {
					continue;
				}else {
					Blockchain best = new Blockchain(buffer.get(0));
					for(Blockchain buffered : buffer) {
						if(buffered.getBlockchainScore()> best.getBlockchainScore()) {
							best = new Blockchain(buffered);
						}
					}
					myBlockChain = best;
					buffer.clear();
				}
			}
		}
		round.unRegister(this);
		for(Block b : myBlockChain.getBlocks()) {
			for(Data d : b.getWord()) {
				if(d.getAuthorHashId() == myHashId) {
					myScore+=1; // toutes les lettres sont equiprobables donc 1
				}
			}
		}
		System.out.println("Score of Autor: " + myId + " = " +this.getScore());
	}
	
	public void setRound(Round round) {
		this.round = round;
	}
	
	public synchronized void authorLeft(Auteur a) {
		this.authors.remove(a);
	}
	
	public synchronized void PolLeft(Politicien a) {
		this.politicians.remove(a);
	}

	public Blockchain getMyBlockChain() {
		return myBlockChain;
	}

	public int getScore() {
		int score = 0;
		for(Block b : this.myBlockChain.getBlocks()) {
			for(Data d : b.getWord()) {
				if(d.getAuthorHashId() == this.myHashId)
					score += lettersPoint[d.getLetter() -'a'];
			}
		}
		return score;
	}
	
	public String getId() {
		return this.myId;
	}
	
	public int getDifficulty() {
		return this.word_size;
	}
	
	public void setDifficulty(int n) {
		this.word_size=n;
	}
}
