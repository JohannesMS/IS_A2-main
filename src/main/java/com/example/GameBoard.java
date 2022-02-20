package com.example;

import sim.engine.*;
import sim.field.grid.ObjectGrid2D;

import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;

public class GameBoard extends SimState {

    static String filePath = new File("").getAbsolutePath() + "\\CSV\\";


//Spielfeld / Environment / Simulation
    public GameBoard(long seed, int csv) {
		super(seed);
        this.csv = csv;
	}
    
    protected String[] strategies = {"bruteForce","smart"};

    public int test = 1;

    //Folder enthält die CSV Dateien
    private File folder = new File(filePath);
    //Files ist ein Array der Pfade der CSV Dateien, hier wird zunächst nur die Länge des Arrays (Die Zahl der CSV Dateien) deklariert
    private File[] files = new File[folder.listFiles().length];
    //Wenn die Klasse Gamboard gemacht wird wird mit csv bestimmt welche CSV genommen wird. Es soll damit die Erstellung des Gameboards mit jeder CSV geloopt werden
    //Temporär
    private int csv;

    //Liste der Locations der nummerierten Mauern
    protected ArrayList<Integer[]> numberedWallLocations = new ArrayList<Integer[]>();
    protected ArrayList<Integer[]> emptyFieldLocations = new ArrayList<Integer[]>();
    protected ArrayList<Integer[]> locationPlaceableNonTrivialBulbs = new ArrayList<Integer[]>();

    protected ArrayList<ArrayList<Integer[]>> solutionspaceArrayX = new ArrayList<ArrayList<Integer[]>>();
    protected ArrayList<ArrayList<Integer[]>> solutionspaceArrayY = new ArrayList<ArrayList<Integer[]>>();
    

    //Spielbrett als ObjectGrid2D
    protected ObjectGrid2D field = null;



    //Das Spielfeld soll wie ein einfaches Array sein, die Spieler spawnen die Figuren an fixen stellen. Falls ein Spieler am Ursprungspunkt-2 ist kommt er auf ein neues kleines array dass die Ziellinie abbildet
    

    public void finish(){
        super.finish();
    }

    public void start(){
        
        super.start();
        this.setField();
    
        Agent agent = new Agent();
        schedule.scheduleOnce(agent);
        schedule.step(this);
    
    }

    public static void main(String[] args){
        
        //Hier würde dann mithilfe des files Array geloopt werden und die Simulation mindestens einmal pro CSV ausgeführt werden
        for(int i=0; i<1;i++){
        GameBoard board = new GameBoard(System.currentTimeMillis(), 0);
        board.setFilepaths();
        board.start();
        board.finish();
        //Kopiert das?
        
        //System.out.println(board.files[0]);
        System.out.println("test");
        System.out.println(board.solutionspaceArrayX.size());
        }
        
    }

    public static void print(Object o){
        System.out.println(o.toString());
    }

    public void setFilepaths(){
        //files ist ein Array der CSV Dateien
        this.files = folder.listFiles();
    }

    public int[] returnCSVDimension(String pathToCsv){
        //Gibt die Spielfeldgröße aus, man könnte es auch statisch machen aber es ist elegant die Größe des Spielbretts dynamisch zu machen
        int rowCount = 0;
        int columnCount = 0;
        try{
            Scanner sc = new Scanner(new File(pathToCsv));
            while(sc.hasNext()){
               sc.nextLine();
               rowCount++;
            }
            sc = new Scanner(new File(pathToCsv));
            sc.next();
            //regex Split weil die normale Methode keine trailing delimiters erkennt
            String[] rowArray = sc.next().split("\\;",-1);
            columnCount = rowArray.length;
        } catch (FileNotFoundException e){
            System.out.println("File not found");
        }
        int[] lenghts = {rowCount, columnCount};
        return lenghts;
    }

    
    public void setField(){
        //Füllt das Sparse2DGrid mit Objekten, je nach CSV
        int[] size = returnCSVDimension(files[csv].toString());
        this.field = new ObjectGrid2D(size[1], size[0]);
        String[] rows;
        
        try{
            Scanner sc = new Scanner(new File(files[csv].toString())); 
            for(int row = 0; row<size[0] ;row++){
                rows = sc.nextLine().split("\\;", -1);
                for (int column = 0; column<size[1]; column++){
                    if(rows[column].equals("x")){
                        field.set(column, row, new Wall(true));
                    }
                    else if(rows[column].equals("")){
                        field.set(column, row, new EmptyField());
                        Integer[] temp = {column,row};
                        emptyFieldLocations.add(temp);
                    }
                    else{
                        field.set(column, row, new Wall(Integer.parseInt(rows[column])));
                        Integer[] temp = {column,row};
                        numberedWallLocations.add(temp);
                        
                    }
                }   
            }

        } catch (FileNotFoundException e){
            System.out.println("File probably not found");
        }   
    }

}
