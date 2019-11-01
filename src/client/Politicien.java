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

public class Politicien implements Runnable{

	private static Integer idCpt = 0;
	private static final Object mutex = new Object();
	private static final Object mutex_cpt = new Object();
	
	private int myScore;
	private Blockchain myBlockChain;
	private String myId;
	private String myHashId;//SHA-256 of id
	private ArrayList<Data> lettersFromAuthors;//submitted unused valid letters
	private Round round;
	
	private ArrayList<Auteur> authors;
	private ArrayList<Politicien> politicians;
	
	private ArrayList<Blockchain> buffer;
	private final Object mutexBuffer = new Object();
	
	private int word_size;
	private Ptrie dico;
	
	
	
	private static int[] lettersPoint = {1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 10, 1, 2, 1, 1, 3, 8, 1, 1, 1, 1,
			4, 10, 10, 10, 10};
	
	@SuppressWarnings("unchecked")
	public Politicien(Blockchain bc, int score, ArrayList<Character> lettersFromAuthors, int word_size, Ptrie arbre	) {
		this.myScore = score;
		this.myBlockChain = new Blockchain(bc);//copy bc
		this.myId = getMyId();
		this.myHashId = hash_id(this.myId);
		this.lettersFromAuthors = (ArrayList<Data>) lettersFromAuthors.clone();
		this.authors = new ArrayList<Auteur>();
		this.politicians = new ArrayList<Politicien>();
		this.word_size = word_size;
		this.dico = arbre;
		this.buffer = new ArrayList<Blockchain>();
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



	public synchronized void sendBlock(Blockchain obc) {
		Blockchain bc = new Blockchain(obc);
		for(Auteur a : authors) {
			a.receiveBlock((new Blockchain(bc)));
		}
		for(Politicien p : politicians) {
			p.receiveBlock((new Blockchain(bc)));
		}

	}

	public synchronized void receiveBlock(Blockchain obc) {
		Blockchain tmp = new Blockchain(obc);
		synchronized (mutexBuffer) {
			Block b = tmp.getLastBlock();
			Block before = tmp.getBlocks().get(tmp.getBlocks().size()-2);
			boolean is_Ok = true;
			if(b.getprevioushashId() != before.gethashId()) {
				is_Ok=false;
			}
			if(b.getWord().size() < getDifficulty()) {
				is_Ok = false;
			}
			ArrayList<String> already_in = new ArrayList<String>();
			for(Data d : b.getWord()) {
				if(already_in.contains(d.getAuthorHashId())) {
					is_Ok = false;
					break;
				}else {
					already_in.add(d.getAuthorHashId());
				}
			}
			if(is_Ok) {
				buffer.add(tmp);
			}
			
		}
	}

	
	public synchronized void receiveLetter(Data d) {
		lettersFromAuthors.add(d);
	}
	
	public void setAuthors(ArrayList<Auteur> authors) {
		this.authors = authors;
	}
	
	public void setPoliticians(ArrayList<Politicien> politicians) {
		this.politicians = politicians;
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
	

	

	
	public ArrayList<Data> getWordLetters(){
		ArrayList<Data> word = new ArrayList<>();
		ArrayList<String> hashIds = new ArrayList<>();
		
		ArrayList<Data> tmpLetters = new ArrayList<>();
		tmpLetters.addAll(lettersFromAuthors);
		
		while(tmpLetters.isEmpty() == false) {
			Collections.shuffle(tmpLetters);
			Data first = tmpLetters.get(0);
			if(hashIds.contains(first.getAuthorHashId()) == false) {
				hashIds.add(first.getAuthorHashId());
				word.add(first);
			}
			tmpLetters.remove(first);
		}
		return word;
	}
	
	public ArrayList<Data> getWord(ArrayList<Data> letters){ 
		ArrayList<Data> mot = dico.findWord(letters);
		
		for(int i = 0; i < 10000; i++) {
			ArrayList<Data> tmp = dico.findWord(letters);
			if(tmp.size() > mot.size()) {
				mot.clear();
				mot.addAll(tmp);
			}
		}
		return mot;
	}

	@Override
	public void run() {
		while(myBlockChain.getBlocks().size()<21 && round.getNbAuthors() >= this.word_size) {
			
			round.waitPoliticiansRound();
			cleanAuthorsLetters();
			
			if(round.getNbAuthors() >= this.word_size) {

				ArrayList<Data> letters = getWordLetters();
				ArrayList<Data> mot = getWord(letters);
				
				
				if(mot.size() >= this.word_size) {
					String s = "";
					for(Data d : mot) {
						s+=d.getLetter();
					}
					System.out.println(this.myId+" propose le mot "+s);
					int idCpt_tmp;
					synchronized (mutex_cpt) {
						idCpt_tmp = idCpt;
						idCpt++;
					}
					Block block = new Block(myBlockChain.getLastBlock().gethashId(), mot, hash_id(s+myHashId+idCpt_tmp) , myHashId);
					myBlockChain.addBlock(block);
					for(Auteur a : authors) {
						a.receiveBlock((new Blockchain(myBlockChain)));
					}
					
					for(Politicien p : politicians) {
						p.receiveBlock((new Blockchain(myBlockChain)));
					}
				}	
			}
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
			if(b.getPoliticianHashId() == myHashId) {
				myScore+=1; // toutes les lettres sont equiprobables donc 1
			}
		}
		System.out.println("blocks of Politician: " + myId + " = " + myBlockChain.getBlocks());
		System.out.println("Score of Politician: " + myId + " = " +getScore());
		
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
			if(b.getPoliticianHashId() == this.myHashId)
				for(Data d : b.getWord()) {
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

