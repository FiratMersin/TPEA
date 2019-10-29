package round;

import java.util.ArrayList;

import client.Auteur;
import client.Politicien;

public class Round implements Runnable{
	private ArrayList<Auteur> authors;
	private int nbAuthors;
	private ArrayList<Politicien> politicians;
	private int nbPoliticians;
	
	private static final Object mutexAuthors = new Object();
	private static final Object mutexPoliticians = new Object();
	
	private static int nbAuthorsWaiting = 0;
	private static int nbPoliticiansWaiting = 0;
	
	private static final Object waitEndOfRoundOfAuthors = new Object();
	private static final Object waitEndOfRoundOfPoliticians = new Object();
	
	
	public Round(ArrayList<Auteur> authors, ArrayList<Politicien> politicians) {
		super();
		this.authors = new ArrayList<>();
		this.authors.addAll(authors);
		this.nbAuthors = this.authors.size();
		this.politicians = new ArrayList<>();
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
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	
}
