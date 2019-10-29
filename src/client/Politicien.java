package client;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

import javax.xml.bind.DatatypeConverter;

import Words.Ptrie;
import block.Block;
import block.Blockchain;
import data.Data;
import round.Round;

public class Politicien implements Runnable{

	private static Integer idCpt = 0;
	private static final Object mutex = new Object();
	private static final Object waitround = new Object();
	private static final Object politicians_sleeping = new Object();
	public static int nb_asleep = 0;
	private static Semaphore sem_politiciens = new Semaphore(0);
	private boolean canTakeSemaphore;
	private static final Object wait_on_CanTakeSem = new Object();
	private static final Object mutex_read_CTS = new Object();
	private static int nb_politicians_asleep=0;
	

	private int cpt;
	private int myScore;
	private Blockchain myBlockChain;
	private String myId;
	private String myHashId;//SHA-256 of id
	private ArrayList<Data> lettersFromAuthors;//submitted unused valid letters
	private Round round;
	
	private ArrayList<Auteur> authors;
	private ArrayList<Politicien> politicians;
	
	private int word_size;
	private Ptrie dico;
	
	private CyclicBarrier barrier;
	
	private static boolean isFirstAwaken = true;
	
	private static int[] lettersPoint = {1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 10, 1, 2, 1, 1, 3, 8, 1, 1, 1, 1,
			4, 10, 10, 10, 10};
	
	@SuppressWarnings("unchecked")
	public Politicien(Blockchain bc, int score, ArrayList<Character> lettersFromAuthors, int word_size, Ptrie arbre, CyclicBarrier barrier) {
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
		this.barrier = barrier;
		this.canTakeSemaphore = true;
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



	public synchronized void sendBlock(Block b,Blockchain obc) {
		Blockchain bc = new Blockchain(obc);
		for(Auteur a : authors) {
			a.receiveBlock(b, (new Blockchain(bc)));
		}
		for(Politicien p : politicians) {
			p.receiveBlock(b, (new Blockchain(bc)));
		}

	}

	public synchronized void receiveBlock(Block b,Blockchain obc) {
		Blockchain bc = new Blockchain();
		for(Block bl : obc.getBlocks()) {
			bc.addBlock(bl);
		}
		if(myBlockChain.isValidBlock(b)) {
			myBlockChain.addBlock(b);
		}
		else {
			
			//CONSENSUS ALGORITHM
			
			Block tested_B = b;
			ArrayList<Block> myblockList = (ArrayList<Block>) myBlockChain.getBlocks().clone();
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
								System.out.println(myId +" politician finished his consensus bis chain size before:"+myblockList.size());
								
								if(new_bc.size() > myblockList.size()) {
									this.myBlockChain = new Blockchain(new_bc);
								}

								System.out.println(myId +" politician finished his consensus bis chain size:"+ myBlockChain.getBlocks().size());
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
		System.out.println(myId +" politician finished his consensus chain size:"+ myBlockChain.getBlocks().size());
	}

	/*public synchronized void createBlock() {

		ArrayList<Data> word = getWord();
		String BlockHashId = hash_id( myHashId+myBlockChain.getLastBlock().gethashId()+ cpt);

		Block newB = new Block(myBlockChain.getLastBlock().gethashId(),
				word,
				BlockHashId,
				myHashId);

		if(!myBlockChain.isValidBlock(newB))return;


		sendBlock(newB,myBlockChain);

		myBlockChain.addBlock(newB);
	}*/
	
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
	
	public static Object getWaiter() {
		return Politicien.waitround;
	}

	
	public static int getNbAsleep() {
		return nb_asleep;
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
		
		for(int i = 0; i < 1000; i++) {
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
		int j =1;
		while(myBlockChain.getBlocks().size()<21 && round.getNbAuthors() >= this.word_size) {
			
			round.waitPoliticiansRound();
			cleanAuthorsLetters();
			
			if(round.getNbAuthors() >= this.word_size) {
				
				/*for(int i = 0; i < Math.min(lettersFromAuthors.size(), 1)6; i++ ) {
					Collections.shuffle(lettersFromAuthors);
					word.add(lettersFromAuthors.get(0));
					lettersFromAuthors.remove(0);
				}*/
				
				ArrayList<Data> letters = getWordLetters();
				ArrayList<Data> mot = getWord(letters);
				
				if(mot.size() >= this.word_size) {
					String s = "";
					for(Data d : mot) {
						s+=d.getLetter();
					}
					System.out.println(this.myId+" propose le mot "+s);
					Block block = new Block(myBlockChain.getLastBlock().gethashId(), mot, hash_id(s+myHashId+idCpt) , myHashId);
					for(Auteur a : authors) {
						a.receiveBlock(block, (new Blockchain(myBlockChain)));
					}
					
		
					for(Politicien p : politicians) {
						p.receiveBlock(block, (new Blockchain(myBlockChain)));
					}
					myBlockChain.addBlock(block);
				}
				j++;
				//}
				
			}
				

		}
		round.unRegister(this);
		for(Block b : myBlockChain.getBlocks()) {
			if(b.getPoliticianHashId() == myHashId) {
				myScore+=1; // toutes les lettres sont equiprobables donc 1
			}
		}
		
		System.out.println("Score of Politician: " + myId + " = " +getScore());
		
	}


	public static Object getPolSleep() {
		return politicians_sleeping;
	}
	
	
	public static Semaphore getSem_politiciens() {
		return sem_politiciens;
	}
	
	public void setRound(Round round) {
		this.round = round;
	}
	
	
	public static int getsleep() {
		return nb_asleep;
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
		
}

