package round;

import java.util.ArrayList;

import block.Blockchain;
import client.Auteur;
import client.Politicien;

public class Round implements Runnable{
	private ArrayList<Auteur> authors;
	private ArrayList<Auteur> authorsLeft;
	private int nbAuthors;
	private ArrayList<Politicien> politicians;
	private ArrayList<Politicien> politiciansLeft;
	private int nbPoliticians;
	
	private static final Object mutexAuthors = new Object();
	private static final Object mutexPoliticians = new Object();
	
	private static int nbAuthorsWaiting = 0;
	private static int nbPoliticiansWaiting = 0;
	
	private static final Object waitEndOfRoundOfAuthors = new Object();
	private static final Object waitEndOfRoundOfPoliticians = new Object();
	
	private static int[] lettersPoint = {1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 10, 1, 2, 1, 1, 3, 8, 1, 1, 1, 1,
			4, 10, 10, 10, 10};
	
	
	public Round(ArrayList<Auteur> authors, ArrayList<Politicien> politicians) {
		super();
		this.authors = new ArrayList<>();
		this.authorsLeft = new ArrayList<>();
		this.authors.addAll(authors);
		this.nbAuthors = this.authors.size();
		this.politicians = new ArrayList<>();
		this.politiciansLeft = new ArrayList<>();
		this.politicians.addAll(politicians);
		this.nbPoliticians = this.politicians.size();
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
			System.out.println("ROUND "+nbAuthors+" left in course");
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
			while(getNbAuthors() != 0 || getNbPoliticians() != 0) {
				System.out.println("AUTHORS TURN");
				synchronized (waitEndOfRoundOfAuthors) {
					synchronized(mutexAuthors) {
						mutexAuthors.notifyAll();
					}
					waitEndOfRoundOfAuthors.wait();
				}
				System.out.println("POLITICIANS TURN");
				synchronized (waitEndOfRoundOfPoliticians) {
					synchronized(mutexPoliticians) {
						mutexPoliticians.notifyAll();
					}
					waitEndOfRoundOfPoliticians.wait();
				}
			}
			this.winners();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
	}
	

	
}
