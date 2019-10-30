package launch;

import java.util.ArrayList;

import Words.Ptrie;
import block.Block;
import block.Blockchain;
import client.Auteur;
import client.Politicien;
import data.Data;
import round.Round;

public class Launcher {
	public final static int nb_authors = 75;
	public final static int nb_politicians = 5;
	
	public final static int nb_letters_per_pool = 500;
	
	public final static char[] alphabet = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'}; 
	
	public final static int word_size = 10;
	
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
				
		for(int i=0; i<nb_authors; i++) {
			ArrayList<Character> letters = new ArrayList<Character>();
			for(int j=0; j< nb_letters_per_pool; j++) {
				letters.add(alphabet[(int)(Math.random()*26)]);
			}
			Auteur auteur = new Auteur(blockchain, 0, letters, word_size, arbre);
			authors.add(auteur);
		}
		
		for(int i=0; i<nb_politicians; i++) {
			Politicien politicien = new Politicien(blockchain, 0, new ArrayList<Character>(), word_size, arbre);
			politicians.add(politicien);
		}
		
		
		for(Auteur auteur : authors) {
			@SuppressWarnings("unchecked")
			ArrayList<Auteur> authors_clone = (ArrayList<Auteur>) authors.clone();
			authors_clone.remove(authors_clone.indexOf(auteur));
			auteur.setAuthors(authors_clone);
			auteur.setPoliticians(politicians);
		}
		
		
		for(Politicien politicien: politicians) {
			@SuppressWarnings("unchecked")
			ArrayList<Politicien> pol_clone = (ArrayList<Politicien>) politicians.clone();
			pol_clone.remove(pol_clone.indexOf(politicien));
			politicien.setPoliticians(pol_clone);
			politicien.setAuthors(authors);
			
			
		}
		
		Round round = new Round(authors, politicians, word_size);
		for(Politicien p : politicians) {
			p.setRound(round);
		}
		
		for(Auteur a : authors) {
			a.setRound(round);
		}
		
		
		Thread t_start  = new Thread(round);
		t_start.start();
		Thread[] t_authors = new Thread[nb_authors];
		Thread[] t_politicians = new Thread[nb_politicians];
		
		int i = 0;
		for(Politicien politicien: politicians) {
			t_politicians[i]= new Thread(politicien);
			t_politicians[i].start();
			i++;
			
		}
		i = 0;
		for(Auteur auteur : authors) {
			t_authors[i] = new Thread(auteur);
			t_authors[i].start();
			i++;
		}
		
		try {
			t_start.join();
			for(Thread t : t_authors) {
				t.join();
			}
			for(Thread t : t_politicians) {
				t.join();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
}
