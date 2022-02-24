package com.example;

import sim.engine.*;
import sim.field.grid.ObjectGrid2D;

import java.util.*;
import java.io.*;

public class GameBoard extends SimState {

    static String filePath = new File("").getAbsolutePath() + "\\CSV\\";


//Spielfeld / Environment / Simulation
    public GameBoard(long seed, int csv) {
		super(seed);
        this.csv = csv;
        
        
	}

    public GameBoard(long seed, GameBoard original){
        super(seed);
        this.csv = csv;
        this.field = field;
        this.numberedWallLocations = numberedWallLocations;
        this.emptyFieldLocations = emptyFieldLocations;
        this.locationPlaceableNonTrivialBulbs = locationPlaceableNonTrivialBulbs;
        this.solutionspaceArrayX = solutionspaceArrayX;
        this.solutionspaceArrayY = solutionspaceArrayY;
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
    protected ArrayList<Integer[]> emptyFieldLocationswithWalls = new ArrayList<Integer[]>();
    protected ArrayList<Integer[]> locationPlaceableNonTrivialBulbs = new ArrayList<Integer[]>();

    protected List<List<Integer>> solutionspaceArrayX = new ArrayList<>();
    protected List<List<Integer>> solutionspaceArrayY = new ArrayList<>();

    protected List<Integer[]> numberedWallCandidates = new ArrayList<Integer[]>();
    

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
        for(int i=0; i<1;i++){ //4 zum testen
        GameBoard board = new GameBoard(System.currentTimeMillis(),1);
        board.setFilepaths();
        board.start();
        board.finish();
        //Kopiert das?
        
        //System.out.println(board.files[0]);


        //System.out.println(cartesianProduct(Arrays.asList(Arrays.asList("Apple", "Banana"), Arrays.asList("Red", "Green", "Blue"))));

        //System.out.println(getCartesian2(board.solutionspaceArrayX));

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
        System.out.println(files[csv]);
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

    public static List<List<Integer>> getCartesian2(List<List<Integer>> lists) {
        long size = 1;
        final List<List<Integer>> copy = new ArrayList<List<Integer>>();
        for (List<Integer> list : lists) {
            size *= list.size();
            if (size > Integer.MAX_VALUE)
                throw new IllegalArgumentException();
            copy.add(new ArrayList<Integer>(list));
        }
        final int fSize = (int) size;
        return new AbstractList<List<Integer>>() {
            @Override
            public int size() {
                return fSize;
            }
            @Override
            public List<Integer> get(int i) {
                if (i < 0 || i >= fSize)
                    throw new IndexOutOfBoundsException();
                Integer[] arr = new Integer[copy.size()];
                for (int j = copy.size() - 1; j >= 0; j--)  {
                    List<Integer> list = copy.get(j);
                    arr[j] = list.get(i % list.size());
                    i /= list.size();
                }
                return Arrays.asList(arr);
            }
        };
    }

}
