package launch;

import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;

import Words.Ptrie;
import block.Block;
import block.Blockchain;
import client.Auteur;
import client.Politicien;
import data.Data;
import round.Round;

public class Launcher {
	public final static int nb_authors = 16;
	public final static int nb_politicians = 5;
	
	public final static int nb_letters_per_pool = 200;
	
	public final static char[] alphabet = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'}; 
	
	public final static int word_size = 6;
	
	public final static Ptrie arbre = new Ptrie("dict_100000_1_10.txt");
	
	
	
	public static void main(String[] args) {
		//Blockchain initilization
		Blockchain blockchain = new Blockchain();
		
		
		
		//FIrst block initilization
		ArrayList<Data> word = new ArrayList<Data>();
		word.add(new Data('g', "start", null));
		word.add(new Data('e', "start", null));
		word.add(new Data('n', "start", null));
		word.add(new Data('e', "start", null));
		word.add(new Data('s', "start", null));
		word.add(new Data('e', "start", null));
		Block init = new Block("generalprevious",word,"123456789","987654321");
		blockchain.addBlock(init);
		
		
		
		
		ArrayList<Auteur> authors = new ArrayList<Auteur>();
		ArrayList<Politicien> politicians = new ArrayList<Politicien>();
		
		
		CyclicBarrier auteurs = new CyclicBarrier(nb_authors);
		CyclicBarrier politiciens = new CyclicBarrier(nb_politicians);
		
		Auteur.setSemaphore(nb_authors);
		
		for(int i=0; i<nb_authors; i++) {
			ArrayList<Character> letters = new ArrayList<Character>();
			for(int j=0; j< nb_letters_per_pool; j++) {
				letters.add(alphabet[(int)(Math.random()*26)]);
			}
			Auteur auteur = new Auteur(blockchain, 0, letters, auteurs);
			authors.add(auteur);
		}
		
		
		for(int i=0; i<nb_politicians; i++) {
			Politicien politicien = new Politicien(blockchain, 0, new ArrayList<Character>(), word_size, arbre, politiciens);
			politicians.add(politicien);
		}
		
		
		for(Auteur auteur : authors) {
			ArrayList<Auteur> authors_clone = (ArrayList<Auteur>) authors.clone();
			authors_clone.remove(authors_clone.indexOf(auteur));
			auteur.setAuthors(authors_clone);
			auteur.setPoliticians(politicians);
		}
		
		
		for(Politicien politicien: politicians) {
			ArrayList<Politicien> pol_clone = (ArrayList<Politicien>) politicians.clone();
			pol_clone.remove(pol_clone.indexOf(politicien));
			politicien.setPoliticians(pol_clone);
			politicien.setAuthors(authors);
			
			
		}
		
		Round round = new Round(authors, politicians);
		for(Politicien p : politicians) {
			p.setRound(round);
		}
		
		for(Auteur a : authors) {
			a.setRound(round);
		}
		
		
		new Thread(round).start();
		
		
		for(Politicien politicien: politicians) {
			
			new Thread(politicien).start();
			
		}
		
		for(Auteur auteur : authors) {
			new Thread(auteur).start();
		}
		
		
	}
	

}
