package com.example;

import java.util.*;
import java.io.OutputStream;
import com.google.common.collect.Lists;

import sim.engine.*;
import sim.field.grid.Grid2D;
import sim.field.grid.ObjectGrid2D;
import java.util.concurrent.TimeUnit;

import java.util.Arrays;

public class Agent implements Steppable {
    
//TODO
//Wann wird geprüft ob die Constraints erfüllt werden?
//Wie entscheidet der Spieler wo was platziert wird? -> aus Expertenwissen bedienen



    public ObjectGrid2D tempBoard = null;
    public GameBoard gameboard = null;
    public int stepcounter;
    public String strategy;
    public int backtrackingSteps = 0;


    public Agent(){
        super();
        this.stepcounter = 0;
    }

    public Agent(String strategy){
        super();
        this.stepcounter = 0;
        this.strategy = strategy;
    }

    public void step(SimState state){
        this.gameboard = (GameBoard)state;
        this.tempBoard = this.gameboard.field;
        

        //TODO
        //Die Kandidaten sortieren, die mit weniger Möglichkeiten zuerst, dafür brauche ich noch eine Liste um nach größe zu sortieren
        //In dem KandidatenBacktracking alle Möglichkeiten der restlichen Birnen iterieren, dafür brauche ich einen Abbruch wenn alle Möglichkeiten durch sind
        //Bei der greedybacktrack soll eine validnumbumbsonwall platzierung als Lösung gespeichert werden inklusive trivialer Birnen, die Lösung wird in form von Bulblocations gespeichert. Durch diese Lösungen wird mit einer anderen rekursiven funktion iteriert

        //createSolutionspaceArrayX();
        //createSolutionspaceArrayY();
        //System.out.println(gameboard.solutionspaceArrayX.size());
        //System.out.println(gameboard.solutionspaceArrayY.size());

        //TODO
        //Die Kandidatenliste aufteilen
        //Jedes mal wenn die Möglichen Kandidaten einer Mauer platziert werden wird geprüft ob diese Mauer die Constraints erfüllt, wenn nicht wird die iteration gebrochen

        //Bugs
        //Wenn eine Birne entfernt wird und die Felder als platzierbar markiert werden wird nicht geprüft ob andere Mauern betroffen sind
        //

        placeTrivialBulbs(2);
        setCandidates();
        printGameboard(); 
        //greedyBacktrackSolver();
        backtrackSolver();
        
        
        
        
        
        
    }

    public void printGameboard(){
        //Emptyfield not illuminated not implaceable = _
        //NumberedWall = 0-4
        //Bulb = ?

        for(int y = 0; y<tempBoard.height; y++){
            for(int x = 0; x<tempBoard.width; x++){
                if(isEmptyField(x,y)){
                    if(isImplaceable(x,y)){System.out.print("x  ");}
                    else if(!isImplaceable(x, y))System.out.print("_  ");
                    
                }
                else if(isBulb(x, y)){System.out.print("?  ");}
                else if(isWall(x, y)){
                    Wall tempWall = (Wall)tempBoard.get(x, y);
                    if(tempWall.blank){System.out.print("-1");}
                    else{System.out.print(tempWall.numberAdjascentBulbs + "  ");}
                }


            }
            System.out.println("");
        }
        System.out.println("");
        System.out.println("");

    }

    public void setCandidates(){
        for(int i = 0; i<gameboard.numberedWallLocations.size();i++){
            int tempX = gameboard.numberedWallLocations.get(i)[0];
            int tempY = gameboard.numberedWallLocations.get(i)[1];
            int bulbCount = 0;

            if(isEmptyField(tempX+1, tempY) && isBulb(tempX+1, tempY))bulbCount++;
            if(isEmptyField(tempX-1, tempY) && isBulb(tempX-1, tempY))bulbCount++;
            if(isEmptyField(tempX, tempY+1) && isBulb(tempX, tempY+1))bulbCount++;
            if(isEmptyField(tempX, tempY-1) && isBulb(tempX, tempY-1))bulbCount++;

            Wall tempWall = (Wall)tempBoard.get(tempX, tempY);
            if(tempWall.numberAdjascentBulbs != bulbCount){
                if(isEmptyField(tempX+1, tempY) && !isIlluminated(tempX+1, tempY)){
                    Integer[] location = {tempX+1, tempY};
                    gameboard.numberedWallCandidates.add(location);
                }
                if(isEmptyField(tempX-1, tempY) && !isIlluminated(tempX-1, tempY)){
                    Integer[] location = {tempX-1, tempY};
                    gameboard.numberedWallCandidates.add(location);
                }
                if(isEmptyField(tempX, tempY+1) && !isIlluminated(tempX, tempY+1)){
                    Integer[] location = {tempX, tempY+1};
                    gameboard.numberedWallCandidates.add(location);
                }
                if(isEmptyField(tempX, tempY-1) && !isIlluminated(tempX, tempY-1)){
                    Integer[] location = {tempX, tempY-1};
                    gameboard.numberedWallCandidates.add(location);
                }
                


            }

        }
    }

    public void smartSetter(){
        //Experiment
        int tempX;
        int tempY;
        while(!validateSolution()){

        for (int i = 0; i<gameboard.solutionspaceArrayX.size(); i++){
            for( int z = 0; z<gameboard.solutionspaceArrayX.get(i).size(); z++){
                //tempX = gameboard.solutionspaceArrayX.get(i).get(z)[0];
                //tempY = gameboard.solutionspaceArrayX.get(i).get(z)[1];
                //setBulb(tempX, tempY);
            }
        }
        
    }

        //[Alle waagerechten leeren felder][Array der Locations][Location]
        //Durchlaufen
        while(numPlaceableBulbs()!=0){
        //Setbulbs

        if(validateSolution()){
            System.out.println("Solution found");
            break;
            }
        }           
    }

    public boolean backtrackSolver(){
    
        for(int y = 0; y<tempBoard.height;y++){
            for(int x = 0; x<tempBoard.width;x++){
                //if(isEmptyField(x, y) && !isImplaceable(x, y)){
                    //System.out.println(".");
                    if(setBulb(x,y)){
                        printGameboard();
                        if(backtrackSolver()){
                            return true;
                        }
                        else{
                            backtrackingSteps++;
                            removeBulb(x, y);
                        }
                    }
                    
                //}
                //return false;
            }
        }
        if(validateSolution()){
            System.out.println("Solution found");
            return true;}
        else return false;
    }

    public boolean backtrackSolver2(){
    
        for(int i = 0; i<gameboard.locationPlaceableNonTrivialBulbs.size();i++){
            int x = gameboard.locationPlaceableNonTrivialBulbs.get(i)[0];
            int y = gameboard.locationPlaceableNonTrivialBulbs.get(i)[1];
                //if(isEmptyField(x, y) && !isImplaceable(x, y)){
                    //System.out.println(".");
                    if(setBulb(x,y)){
                        if(backtrackSolver2()){
                            return true;
                        }
                        else{
                            backtrackingSteps++;
                            removeBulb(x, y);
                        }
                    }
                    
                //}
                //return false;
         
        }
        if(validateSolution()){
            System.out.println("Solution found");
            return true;}
        else return false;
    }

    public boolean backtrackSolverArray(){
        if(validateSolution()){
            System.out.println("Solution found");
            return true;
        }
        int tempX;
        int tempY;

        for(int i = 0; i<gameboard.solutionspaceArrayX.size();i++){
            for(int j = 0; j<gameboard.solutionspaceArrayX.get(i).size();j++){
                tempX = gameboard.solutionspaceArrayX.get(i).get(j);
                tempY = gameboard.solutionspaceArrayY.get(i).get(j);
                //System.out.println(tempX + " und " + tempY);
                    if(setBulb(tempX,tempY)){
                        if(backtrackSolverArray()){
                            return true;
                        }
                        else{
                            removeBulb(tempX, tempY);
                        }
                    }

            }
        }
        if(validateSolution()){
            System.out.println("Solution found");
            return true;}
        else return false;

        //rekursiv die Liste durchgehen, in der ersten Liste das Element platzieren, dann rekursiv in die Zweite und das erste Element, wenn am ende die Lösung nicht valide ist soll die Birne entfernt werden und das Zweite element aus der Liste genommen werden
    }

    public boolean greedyBacktrackSolver(){
        int tempX;
        int tempY;
        for(int i = 0; i<gameboard.numberedWallCandidates.size();i++){
            tempX = gameboard.numberedWallCandidates.get(i)[0];
            tempY = gameboard.numberedWallCandidates.get(i)[1];
            //Wenn die numbered Wall constraints nicht erfüllt sind soll direkt die nächste Iteration genommen werden (Wird das schon?)
            if(setBulb(tempX, tempY)){
                printGameboard();
                if(greedyBacktrackSolver()){
                    return true;
                }
                else{
                    //Ab hier können keine Kandidaten mehr gesetzt werden
                    //setLocationPlaceableNonTrivialBulbs();
                    //System.out.println(validateNumBulbsOnWall());
                    backtrackingSteps++;
                    if(validateNumBulbsOnWall()){
                        //Wenn die numbered Wall constraints erfüllt sind sollen die restlichen platzierungen probiert werden,
                        //Wenn die nicht möglich sind soll die nächste Kandidatenkombination geprüft werden.
                        
                        setLocationPlaceableNonTrivialBulbs();
                        System.out.println("Locationsize:"+gameboard.locationPlaceableNonTrivialBulbs.size());
                        backtrackSolver2();
                        System.out.println("Possible Solution found");
                        
                    }
                    else removeBulb(tempX, tempY);
                    
                }

            }

        }
        if(validateSolution()){
            System.out.println("Solution found");
            return true;
        }
        else{
            return false;
        }
    }

    public boolean setBulb(int x, int y){
        if(x==-1 || y==-1){return true;}
        //Setzt Birnen
        if(!checkConstraintViolation(x, y)){
        tempBoard.set(x, y, new Bulb());
        illuminate(x, y);

        if(!isOutOfBounds(x+1, y) && tempBoard.get(x+1, y).getClass() == Wall.class){
            Wall tempWall = (Wall) tempBoard.get(x+1, y);
            tempWall.numberLeftoverBulbs--;
            if(tempWall.numberLeftoverBulbs==0){
                markAsImplaceable(x+1, y);
            }
        }
        if(!isOutOfBounds(x-1, y) && tempBoard.get(x-1, y).getClass() == Wall.class){
            Wall tempWall = (Wall) tempBoard.get(x-1, y);
            tempWall.numberLeftoverBulbs--;
            if(tempWall.numberLeftoverBulbs==0){
                markAsImplaceable(x-1, y);
            }
            
        }
        if(!isOutOfBounds(x, y+1) && tempBoard.get(x, y+1).getClass() == Wall.class){
            Wall tempWall = (Wall) tempBoard.get(x, y+1);
            tempWall.numberLeftoverBulbs--;
            if(tempWall.numberLeftoverBulbs==0){
                markAsImplaceable(x, y+1);
            }
            
        }
        if(!isOutOfBounds(x, y-1) && tempBoard.get(x, y-1).getClass() == Wall.class){
            Wall tempWall = (Wall) tempBoard.get(x, y-1);
            tempWall.numberLeftoverBulbs--;
            if(tempWall.numberLeftoverBulbs==0){
                markAsImplaceable(x, y-1);
            }
            
        }
        return true;
        //Wenn bulb platiert wird muss Leftoverbulbs von den Wall neighbors dekrementiert werden, in der Hauptmethode unten wird nähmlich sonst nur eine Mauer dekrementiert
        }
        else{
            return false;
        }
    }

    public void removeBulb(int x, int y){
        if(isBulb(x, y)){
            tempBoard.set(x, y, new EmptyField());
            int tempX = x;
            int tempY = y;
            while(isEmptyField(tempX+1, tempY)){
                EmptyField tempField = (EmptyField) tempBoard.get(tempX+1, tempY);
                tempField.illuminated -= 1;
                tempX++;
            }

            tempX = x;
            tempY = y;
            while(isEmptyField(tempX-1, tempY)){
                EmptyField tempField = (EmptyField) tempBoard.get(tempX-1, tempY);
                tempField.illuminated -= 1;
                tempX--;
            }

            tempX = x;
            tempY = y;
            while(isEmptyField(tempX, tempY+1)){
                EmptyField tempField = (EmptyField) tempBoard.get(tempX, tempY+1);
                tempField.illuminated -= 1;
                tempY++;
            }

            tempX = x;
            tempY = y;
            while(isEmptyField(tempX, tempY-1)){
                EmptyField tempField = (EmptyField) tempBoard.get(tempX, tempY-1);
                tempField.illuminated -= 1;
                tempY--;
            }

            if(!isOutOfBounds(x+1, y) && tempBoard.get(x+1, y).getClass() == Wall.class){
                Wall tempWall = (Wall) tempBoard.get(x+1, y);
                tempWall.numberLeftoverBulbs++;
                markAsPlaceable(x+1, y);
    
            }
            if(!isOutOfBounds(x-1, y) && tempBoard.get(x-1, y).getClass() == Wall.class){
                Wall tempWall = (Wall) tempBoard.get(x-1, y);
                tempWall.numberLeftoverBulbs++;
                markAsPlaceable(x-1, y);
                
            }
            if(!isOutOfBounds(x, y+1) && tempBoard.get(x, y+1).getClass() == Wall.class){
                Wall tempWall = (Wall) tempBoard.get(x, y+1);
                tempWall.numberLeftoverBulbs++;
                markAsPlaceable(x, y+1);
                
            }
            if(!isOutOfBounds(x, y-1) && tempBoard.get(x, y-1).getClass() == Wall.class){
                Wall tempWall = (Wall) tempBoard.get(x, y-1);
                tempWall.numberLeftoverBulbs++;
                markAsPlaceable(x, y-1);
                
            }
        }
        //Prüfen ob es eine Birne ist
        //Wenn ja, Empty field erzeugen
        //x und y durchgehen und illuminated-1
        //Falls eine Mauer in der Umgebung ist numberleftoverbulbs+1
        //  Dann gucken ob die Felder wieder als platzierbar markiert werden

    }

    public void markAsImplaceable(int x, int y){
            int tempX = x;
            int tempY = y;
            if(isEmptyField(tempX+1, tempY)){
                EmptyField tempField = (EmptyField) tempBoard.get(tempX+1, tempY);
                tempField.implaceable = true;
            }
            if(isEmptyField(tempX-1, tempY)){
                EmptyField tempField = (EmptyField) tempBoard.get(tempX-1, tempY);
                tempField.implaceable = true;
            }
            if(isEmptyField(tempX, tempY+1)){
                EmptyField tempField = (EmptyField) tempBoard.get(tempX, tempY+1);
                tempField.implaceable = true;
            }
            if(isEmptyField(tempX, tempY-1)){
                EmptyField tempField = (EmptyField) tempBoard.get(tempX, tempY-1);
                tempField.implaceable = true;
            }
    }

    public void markAsPlaceable(int x, int y){
        //Remarks the von neumann neighborhood of a wall as placeable when a bulb is removed
            int tempX = x;
            int tempY = y;
            if(isEmptyField(tempX+1, tempY)){
                EmptyField tempField = (EmptyField) tempBoard.get(tempX+1, tempY);
                tempField.implaceable = false;
            }
            if(isEmptyField(tempX-1, tempY)){
                EmptyField tempField = (EmptyField) tempBoard.get(tempX-1, tempY);
                tempField.implaceable = false;
            }
            if(isEmptyField(tempX, tempY+1)){
                EmptyField tempField = (EmptyField) tempBoard.get(tempX, tempY+1);
                tempField.implaceable = false;
            }
            if(isEmptyField(tempX, tempY-1)){
                EmptyField tempField = (EmptyField) tempBoard.get(tempX, tempY-1);
                tempField.implaceable = false;
            }
    }

    public int numPlaceableBulbs(){
        //Gibt die Zahl der Felder zurück die nicht als nicht platzierbar gekennzeichnet sind
        int placeable= 0;
        for(int x = 0; x<tempBoard.width;x++){
            for(int y = 0; y<tempBoard.height;y++){
                if(isEmptyField(x, y)){
                    EmptyField tempField = (EmptyField) tempBoard.get(x, y);
                    if (!tempField.implaceable && tempField.illuminated==0){placeable++;}
                }
            }
        }
        return placeable;

    }

    public int numNotIlluminated(){
        //Gibt die Zahl der nicht beleuchteten Felder zurück
        int notIlluminated = 0;
        for(int x = 0; x<tempBoard.width;x++){
            for(int y = 0; y<tempBoard.height;y++){
                if(isEmptyField(x, y) && !isIlluminated(x, y)){notIlluminated++;}
            }
        }
        return notIlluminated;
    }

    public void setLocationPlaceableNonTrivialBulbs(){
        //Füllt das generische Array locationPlaceableNonTrivialBulbs, gibt die Zahl der möglichen nicht trivialen platzierbaren Birnen an
        gameboard.locationPlaceableNonTrivialBulbs = new ArrayList<>();
        for(int x = 0; x<tempBoard.width;x++){
            for(int y = 0; y<tempBoard.height;y++){
                if(isEmptyField(x, y)){
                    EmptyField tempField = (EmptyField) tempBoard.get(x, y);
                    if (!tempField.implaceable && tempField.illuminated==0){
                        Integer[] temp = {x,y};
                        gameboard.locationPlaceableNonTrivialBulbs.add(temp);
                    }
                }
            }
        }
    }

    public void setLocationNumberedWalls(){
        int x,y;
        for(int i = 0; i<gameboard.numberedWallLocations.size(); i++){
            x = gameboard.numberedWallLocations.get(i)[0];
            y = gameboard.numberedWallLocations.get(i)[1];
            Wall tempWall = (Wall) tempBoard.get(x, y);
            if(tempWall.numberLeftoverBulbs==0 || tempWall.numberAdjascentBulbs==tempWall.numberBulbs)continue;
            if(isEmptyField(x+1,y)){

            }
            if(isEmptyField(x-1,y)){
                    
            }
            if(isEmptyField(x,y+1)){
                    
            }
            if(isEmptyField(x,y+1)){
                    
            }
            


        }   
    }

    public void createSolutionspaceArrayX(){
        //ArrayList<Integer[]> tempList = new ArrayList<Integer[]>();
        List<Integer> tempListX = new ArrayList<>();
        List<Integer> tempListY = new ArrayList<>();
        EmptyField tempField;
        Integer[] noBulb = {-1,-1};
        for(int y = 0; y<tempBoard.height;y++){
            for(int x = 0; x<tempBoard.width;x++){
                //Create Array of connected X empty fields and add it to the ArrayList, when there are no connectable fields add to the first dimension ArrayList
                if(isEmptyField(x,y)){
                    tempField = (EmptyField) tempBoard.get(x, y);
                    if(tempField.illuminated>0 || tempField.implaceable){
                    //Wenn das Feld beleuchtetet oder als nicht platzierbar gekennzeichnet ist wird die Liste der Hauptliste angefügt
                        if(tempListX.isEmpty()){
                            continue;
                        }
                        else{
                        //tempList.add(0, noBulb);
                        tempListX.add(0, -1);
                        tempListY.add(0, -1);
                        gameboard.solutionspaceArrayX.add(tempListX);
                        gameboard.solutionspaceArrayY.add(tempListY);
                        //tempList = new ArrayList<Integer[]>();
                        tempListX = new ArrayList<>();
                        tempListY = new ArrayList<>();
                        //if(gameboard.solutionspaceArrayX.size() > 9){return;}
                        }
    
                    }
                    else{
                        Integer[] tempInt = {x,y};
                        //tempList.add(tempInt);
                        tempListX.add(x);
                        tempListY.add(y);
                        //System.out.println(Arrays.toString(tempInt));
                        if(isWall(x+1,y)){
                            //tempList.add(0, noBulb);
                            //gameboard.solutionspaceArrayX.add(tempList);
                            tempListX.add(0, -1);
                            tempListY.add(0, -1);
                            gameboard.solutionspaceArrayX.add(tempListX);
                            gameboard.solutionspaceArrayY.add(tempListY);
                            //tempList = new ArrayList<Integer[]>();
                            tempListX = new ArrayList<>();
                            tempListY = new ArrayList<>();

                            //if(gameboard.solutionspaceArrayX.size() > 9){return;}
                        }
                    }
                }
            }
        }
    }

    public void createSolutionspaceArrayY(){
        ArrayList<Integer[]> tempList = new ArrayList<Integer[]>();
        EmptyField tempField;
        Integer[] noBulb = {-1,-1};
        for(int x = 0; x<tempBoard.height;x++){
            for(int y = 0; y<tempBoard.width;y++){
                //Create Array of connected X empty fields and add it to the ArrayList, when there are no connectable fields add to the first dimension ArrayList
                if(isEmptyField(x,y)){
                    tempField = (EmptyField) tempBoard.get(x, y);
                    if(tempField.illuminated>0 || tempField.implaceable){
                    //Wenn das Feld beleuchtetet oder als nicht platzierbar gekennzeichnet ist wird die Liste der Hauptliste angefügt
                        if(tempList.isEmpty()){
                            continue;
                        }
                        else{
                        tempList.add(0, noBulb);
                        //gameboard.solutionspaceArrayY.add(tempList);
                        tempList = new ArrayList<Integer[]>();

                        }
    
                    }
                    else{
                        Integer[] tempInt = {x,y};
                        tempList.add(tempInt);
                        //System.out.println(Arrays.toString(tempInt));
                        if(isWall(x,y+1)){
                            tempList.add(0, noBulb);
                            //gameboard.solutionspaceArrayY.add(tempList);
                            tempList = new ArrayList<Integer[]>();
                        }
                    }
                }
            }
        }
    }

    public boolean checkConstraintViolation(int x, int y){
        //Die Prüfung der numbered Wall Constraints ist nicht notwendig da es durch die Art der Platzierung bereits behandelt wird
        //if(!isWall(x, y) && !isBulb(x, y) && isEmptyField(x, y) && !isIlluminated(x, y) && !isImplaceable(x, y)){
        if(!isWall(x, y) && !isBulb(x, y) && isEmptyField(x, y) && !isIlluminated(x, y) && !isImplaceable(x, y)){
            return false;
        }
        return true;
    }

    //Die nächsten Prüfungen sind selbsterklärend
    public boolean isWall(int x, int y){
        if(x>tempBoard.width-1 || x<0 || y>tempBoard.height-1|| y<0){return true;}
        if(tempBoard.get(x, y).getClass() == Wall.class){return true;}
        else{return false;}

    }

    public boolean isEmptyField(int x, int y){
        if(x>tempBoard.width-1 || x<0 || y>tempBoard.height-1|| y<0){return false;}
        if(tempBoard.get(x, y).getClass() == EmptyField.class){return true;}
        else{return false;}
    }

    public boolean isImplaceable(int x, int y){
        if(x==-1 || y==-1){return true;}
        EmptyField tempField = (EmptyField) tempBoard.get(x, y);
        return tempField.implaceable;
    }

    public boolean isIlluminated(int x, int y){
       EmptyField tempField = (EmptyField) tempBoard.get(x, y);
       if(tempField.illuminated>0) return true;
       else return false;
    }

    public boolean isOutOfBounds(int x, int y){
        if(x>tempBoard.width-1 || x<0 || y>tempBoard.height-1|| y<0){return true;}
        else{return false;}
    }

    public boolean isBulb(int x, int y){
        if(isOutOfBounds(x, y)){return false;}
        if(tempBoard.get(x, y).getClass() == Bulb.class){return true;}
        else{return false;}
    }

    public int numBulbs(){
        //Gibt die Zahl der Birnen auf dem Spielbrett zurück
        int numBulbs = 0;
        for(int x = 0; x<tempBoard.width;x++){
            for(int y = 0; y<tempBoard.height;y++){
                if(isBulb(x, y)){numBulbs++;}
            }
        }
        return numBulbs;
    }

    public void illuminate(int x, int y){
        //Beleuchtet die Felder in alle 4 Richtungen bis eine Mauer erreicht ist
        //Alternative: Ein Beleuchtungscounter ermöglicht das entfernen einer Birne, also eventuell ToDo
        int tempX = x;
        int tempY = y;
        while(isEmptyField(tempX+1, tempY)){
            EmptyField tempField = (EmptyField) tempBoard.get(tempX+1, tempY);
            tempField.illuminated += 1;
            tempX++;
        }

        tempX = x;
        tempY = y;
        while(isEmptyField(tempX-1, tempY)){
            EmptyField tempField = (EmptyField) tempBoard.get(tempX-1, tempY);
            tempField.illuminated += 1;
            tempX--;
        }

        tempX = x;
        tempY = y;
        while(isEmptyField(tempX, tempY+1)){
            EmptyField tempField = (EmptyField) tempBoard.get(tempX, tempY+1);
            tempField.illuminated += 1;
            tempY++;
        }

        tempX = x;
        tempY = y;
        while(isEmptyField(tempX, tempY-1)){
            EmptyField tempField = (EmptyField) tempBoard.get(tempX, tempY-1);
            tempField.illuminated += 1;
            tempY--;
        }
    }

    public boolean validateNumBulbsOnWall(){
        //Prüft ob die Zahl der Birnen an einer Mauer mit der Constraint übereinstimmt
        int tempX;
        int tempY;
        int bulbCounter;
        boolean validation;

        //if(gameboard.numberedWallLocations.size()==0){return true;}
        for(int i = 0; i<gameboard.numberedWallLocations.size();i++){
            bulbCounter = 0;
            tempX = gameboard.numberedWallLocations.get(i)[0];
            tempY = gameboard.numberedWallLocations.get(i)[1];
            Wall tempWall = (Wall) tempBoard.get(tempX, tempY);

            if(isBulb(tempX+1,tempY)){bulbCounter++;}
            if(isBulb(tempX-1,tempY)){bulbCounter++;}
            if(isBulb(tempX,tempY+1)){bulbCounter++;}
            if(isBulb(tempX,tempY-1)){bulbCounter++;}

            if(bulbCounter != tempWall.numberAdjascentBulbs){return false;}
        }
        return true;
    }

    public boolean validateSolution(){
        //Gibt zurück ob die Lösung validie ist
        //Test
        //if(numNotIlluminated() == 0){return true;}
        //Original
        if(numNotIlluminated() == 0 && validateNumBulbsOnWall()){return true;}
        else{return false;}
    }

    public void placeTrivialBulbs(int degreeOfSmartmode){
        //first degree smartmode
        //place trivial bulbs (including surrounding walls)
        //only one iteration

        //second degree smartmode
        //place trivial bulbs iteratively (including surrounding walls)


        //check for implaceable fields in a x++ and y++ loop
        int smartmode = degreeOfSmartmode;
        boolean trivialTrigger;
        int tempX;
        int tempY;
        int tempFreeNeighbors;

        for(int i = 0;i<gameboard.emptyFieldLocations.size();i++){
            int wallCounter = 0;
            tempX = gameboard.emptyFieldLocations.get(i)[0];
            tempY = gameboard.emptyFieldLocations.get(i)[1];
            if(!isOutOfBounds(tempX+1, tempY) && isWall(tempX+1,tempY)){
                Wall tempwWall = (Wall)tempBoard.get(tempX+1, tempY);
                if (tempwWall.numberAdjascentBulbs==0)continue;
                wallCounter++;
            }
            if(!isOutOfBounds(tempX-1, tempY) && isWall(tempX-1,tempY)){
                Wall tempwWall = (Wall)tempBoard.get(tempX-1, tempY);
                if (tempwWall.numberAdjascentBulbs==0)continue;
                wallCounter++;
            }
            if(!isOutOfBounds(tempX, tempY+1) && isWall(tempX,tempY+1)){
                Wall tempwWall = (Wall)tempBoard.get(tempX, tempY+1);
                if (tempwWall.numberAdjascentBulbs==0)continue;
                wallCounter++;
            }
            if(!isOutOfBounds(tempX, tempY-1) && isWall(tempX,tempY-1)){
                Wall tempwWall = (Wall)tempBoard.get(tempX, tempY-1);
                if (tempwWall.numberAdjascentBulbs==0)continue;
                wallCounter++;
            }
            if(wallCounter == 4){setBulb(tempX, tempY);}
        }

        do{
            trivialTrigger = false;
            for(int i=0;i<gameboard.numberedWallLocations.size();i++){
                tempFreeNeighbors = 0;
                tempX = gameboard.numberedWallLocations.get(i)[0];
                tempY = gameboard.numberedWallLocations.get(i)[1];    
                Wall tempWall = (Wall) tempBoard.get(tempX, tempY);
    
    
                //Wenn die Mauer eine Null anzeigt wird die von neumann nachbarschaft als nicht platzierbar gekennezeichnet
                if(tempWall.numberLeftoverBulbs == 0){
                    if(isEmptyField(tempX+1, tempY)){
                        EmptyField tempField = (EmptyField) tempBoard.get(tempX+1, tempY);
                        if(!tempField.implaceable && smartmode==2){trivialTrigger=true;}
                        tempField.implaceable = true;
                        
                    }
                    if(isEmptyField(tempX-1, tempY)){
                        EmptyField tempField = (EmptyField) tempBoard.get(tempX-1, tempY);
                        if(!tempField.implaceable && smartmode==2){trivialTrigger=true;}
                        tempField.implaceable = true;
                        
                    }
                    if(isEmptyField(tempX, tempY+1)){
                        EmptyField tempField = (EmptyField) tempBoard.get(tempX, tempY+1);
                        if(!tempField.implaceable && smartmode==2){trivialTrigger=true;}
                        tempField.implaceable = true;
                        
                    }
                    if(isEmptyField(tempX, tempY-1)){
                        EmptyField tempField = (EmptyField) tempBoard.get(tempX, tempY-1);
                        if(!tempField.implaceable && smartmode==2){trivialTrigger=true;}
                        tempField.implaceable = true;
                    }
                    continue;
                }
    
                //Wenn das Feld in der von neumann nachbarschaft leer ist, nicht beleuchetet wird und nicht als nicht platzierbar gekennzeichnet ist wird es als freier Nachbar gezählt
                if(isEmptyField(tempX+1,tempY) && !isIlluminated(tempX+1, tempY) && !isImplaceable(tempX+1, tempY)){tempFreeNeighbors++;}
                if(isEmptyField(tempX-1,tempY) && !isIlluminated(tempX-1, tempY) && !isImplaceable(tempX-1, tempY)){tempFreeNeighbors++;}
                if(isEmptyField(tempX,tempY+1) && !isIlluminated(tempX, tempY+1) && !isImplaceable(tempX, tempY+1)){tempFreeNeighbors++;}
                if(isEmptyField(tempX,tempY-1) && !isIlluminated(tempX, tempY-1) && !isImplaceable(tempX, tempY-1)){tempFreeNeighbors++;}

                if(tempWall.numberLeftoverBulbs == tempFreeNeighbors){
                    if(isEmptyField(tempX+1,tempY) && !isIlluminated(tempX+1, tempY) && !isImplaceable(tempX+1, tempY)){
                        setBulb(tempX+1, tempY);
                        if(smartmode==2){trivialTrigger = true;}
                    }
                    if(isEmptyField(tempX-1,tempY)  && !isIlluminated(tempX-1, tempY) && !isImplaceable(tempX-1, tempY)){
                        setBulb(tempX-1, tempY);
                        if(smartmode==2){trivialTrigger = true;}
                    }
                    if(isEmptyField(tempX,tempY+1) && !isIlluminated(tempX, tempY+1) && !isImplaceable(tempX, tempY+1)){
                        setBulb(tempX, tempY+1);
                        if(smartmode==2){trivialTrigger = true;}
                    }
                    if(isEmptyField(tempX,tempY-1) && !isIlluminated(tempX, tempY-1) && !isImplaceable(tempX, tempY-1)){
                        setBulb(tempX, tempY-1);
                        if(smartmode==2){trivialTrigger = true;}
                    }
                }
            }
        } while(trivialTrigger == true);
    }   

}

