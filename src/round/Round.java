package round;

import java.util.ArrayList;

import client.Auteur;
import client.Politicien;

public class Round implements Runnable{
	private ArrayList<Auteur> authors;
	private ArrayList<Auteur> authorsLeft;
	private int nbAuthors;
	private ArrayList<Politicien> politicians;
	private ArrayList<Politicien> politiciansLeft;
	private int nbPoliticians;
	
	private int max_size_word;
	
	private static final Object mutexAuthors = new Object();
	private static final Object mutexPoliticians = new Object();
	
	private static int nbAuthorsWaiting = 0;
	private static int nbPoliticiansWaiting = 0;
	
	private static final Object waitEndOfRoundOfAuthors = new Object();
	private static final Object waitEndOfRoundOfPoliticians = new Object();
	
	public Round(ArrayList<Auteur> authors, ArrayList<Politicien> politicians, int max_size_word) {
		super();
		this.authors = new ArrayList<>();
		this.authorsLeft = new ArrayList<>();
		this.authors.addAll(authors);
		this.nbAuthors = this.authors.size();
		this.politicians = new ArrayList<>();
		this.politiciansLeft = new ArrayList<>();
		this.politicians.addAll(politicians);
		this.nbPoliticians = this.politicians.size();
		this.max_size_word = max_size_word;
	}
	
	public void waitAuthorsRound() {
		synchronized (mutexAuthors) {
			nbAuthorsWaiting++;
			try {
				if(nbAuthorsWaiting != nbAuthors) {
					mutexAuthors.wait();
				}else{
					nbAuthorsWaiting=0;
					synchronized (waitEndOfRoundOfAuthors) {
						waitEndOfRoundOfAuthors.notifyAll();
					}
					mutexAuthors.wait();
					
					
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void waitPoliticiansRound() {
		synchronized (mutexPoliticians) {
			nbPoliticiansWaiting++;
			try {
				if(nbPoliticiansWaiting != nbPoliticians) {
					mutexPoliticians.wait();
				}else {
					nbPoliticiansWaiting=0;
					synchronized (waitEndOfRoundOfPoliticians) {
						waitEndOfRoundOfPoliticians.notifyAll();
					}
					mutexPoliticians.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void unRegister(Auteur a) {
		synchronized (mutexAuthors) {
			this.authors.remove(a);
			this.authorsLeft.add(a);
			this.nbAuthors--;
			for(Auteur auth : authors) {
				auth.authorLeft(a);
			}
			for(Politicien p: politicians) {
				p.authorLeft(a);
			}
			if(nbAuthorsWaiting == nbAuthors) {
				synchronized (waitEndOfRoundOfAuthors) {
					waitEndOfRoundOfAuthors.notifyAll();
				}
			}
			if(nbAuthors == 0) {
				synchronized (waitEndOfRoundOfAuthors) {
					waitEndOfRoundOfAuthors.notifyAll();
				}
			}
		}
	}

	public void unRegister(Politicien p) {
		synchronized (mutexPoliticians) {
			this.politicians.remove(p);
			this.politiciansLeft.add(p);
			this.nbPoliticians--;
			for(Auteur auth : authors) {
				auth.PolLeft(p);
			}
			for(Politicien pol: politicians) {
				pol.PolLeft(p);
			}
			if(nbPoliticiansWaiting == nbPoliticians) {
				synchronized (waitEndOfRoundOfPoliticians) {
					waitEndOfRoundOfPoliticians.notifyAll();
				}
			}
			if(nbPoliticians == 0) {
				synchronized (waitEndOfRoundOfPoliticians) {
					waitEndOfRoundOfPoliticians.notifyAll();
				}
			}
		}
	}
	
	public int getNbAuthors() {
		synchronized (mutexAuthors) {
			return nbAuthors;
		}
	}

	public int getNbPoliticians() {
		synchronized (mutexPoliticians) {
			return nbPoliticians;
		}
	}
	
	public int getScoreAuteur(Auteur a) {
		return a.getScore();
	}
	
	public int getScorePoliticien(Politicien p) {
			return p.getScore();
	}
	
	public void winners() {
		Auteur awin = null;
		Politicien pwin = null;
		
		int score = -1;
		for(Auteur a : this.authorsLeft) {
			int tmp = getScoreAuteur(a);
			if(tmp > score) {
				awin = a; 
				score = tmp;
			}
		}

		System.out.println(awin.getId()+" won with score : "+score);
		
		score = -1;
		for(Politicien p : this.politiciansLeft) {
			int tmp = getScorePoliticien(p);
			if(tmp > score) {
				pwin = p; 
				score = tmp;
			}
		}
		

		System.out.println(pwin.getId()+" won with score : "+score);
		
	}

	@Override
	public void run() {
		try {
			//int round = 1;
			while(getNbAuthors() != 0 || getNbPoliticians() != 0) {
				System.out.println("AUTHORS TURN");
				synchronized (waitEndOfRoundOfAuthors) {
					synchronized(mutexAuthors) {
						mutexAuthors.notifyAll();
						if(this.nbAuthors == 0) {
							break;
						}
					}
					waitEndOfRoundOfAuthors.wait();
				}
				System.out.println("POLITICIANS TURN");
				
				int[] size_of_bc = new int[nbPoliticians];
				for(int i = 0; i< politicians.size(); i++) {
					size_of_bc[i] = politicians.get(i).getMyBlockChain().getBlocks().size();
				}
				synchronized (waitEndOfRoundOfPoliticians) {
					synchronized(mutexPoliticians) {
						mutexPoliticians.notifyAll();
						if(this.nbPoliticians == 0) {
							break;
						}
					}
					waitEndOfRoundOfPoliticians.wait();
				}
				
				boolean word_found =false; 
				int i = 0;
				for(Politicien p : politicians){
					if(p.getMyBlockChain().getBlocks().size() != size_of_bc[i]) {
						word_found=true;
					}
					i++;
				}
				if(!word_found) {
					for(Politicien p : politicians) {
						p.setDifficulty(p.getDifficulty()-1);
					}
					for(Auteur p : authors) {
						p.setDifficulty(p.getDifficulty()-1);
					}
				}else {
					for(Politicien p : politicians) {
						p.setDifficulty(max_size_word);
					}
					for(Auteur p : authors) {
						p.setDifficulty(max_size_word);
					}
				}
				
				/*round++;
				boolean bcupdated = true;
				int shortest_bc = round;
				for(Politicien p : politicians) {
					if(p.getMyBlockChain().getBlocks().size() < round) {
						bcupdated=false;
						if(shortest_bc < p.getMyBlockChain().getBlocks().size()) {
							shortest_bc = p.getMyBlockChain().getBlocks().size();
						}
					}
				}
				for(Auteur a : authors) {
					if(a.getMyBlockChain().getBlocks().size() < round) {
						bcupdated=false;
						if(shortest_bc < a.getMyBlockChain().getBlocks().size()) {
							shortest_bc = a.getMyBlockChain().getBlocks().size();
						}
					}
				}*/
			}
			this.winners();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
	}
	

	
}
