package com.example;

import java.util.*;

import sim.engine.*;
import sim.field.grid.ObjectGrid2D;

public class Agent implements Steppable {
    
    public ObjectGrid2D tempBoard = null;
    public GameBoard gameboard = null;
    public int stepcounter;
    public String strategy;
    public int backtrackingSteps = 0;
    public int checkForSolution = 0;
    public int fowardCheckPruning = 0;
    public Boolean ForwardChecking = true;
    public Boolean trivialPlacement = true;



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
        
        if(trivialPlacement){
            placeTrivialBulbs();
            placeTrivialBulbs();
        }
        
        setEmptyFieldsWithNumberedWalls();
        setCandidates();
        setLocationPlaceableNonTrivialBulbs();
        printGameboard();
        candidateBacktrackFowardSeachSolver(); 
        System.out.println("Number of Backtracking Steps: " + backtrackingSteps);
        System.out.println("Number of times Solution checked: " + checkForSolution);
        System.out.println("Number of forward Check Prunings: " + fowardCheckPruning);
        
    }

    public void printGameboard(){
        //Prints the gameboard
        //Emptyfield not illuminated not implaceable = _
        //NumberedWall = 0-4
        //Blank Wall = 6
        //Bulb = ?

        for(int y = 0; y<tempBoard.height; y++){
            for(int x = 0; x<tempBoard.width; x++){
                if(isEmptyField(x,y)){
                    if(isImplaceable(x,y)){System.out.print("x ");}
                    else if(!isImplaceable(x, y))System.out.print("_ ");
                    //if(isIlluminated(x, y)){System.out.print("─ ");}
                    
                }
                else if(isBulb(x, y)){System.out.print("? ");}
                else if(isWall(x, y)){
                    Wall tempWall = (Wall)tempBoard.get(x, y);
                    if(tempWall.blank){System.out.print("6 ");}
                    else{System.out.print(tempWall.numberAdjascentBulbs + " ");}
                }


            }
            System.out.println("");
        }
        System.out.println("");
        System.out.println("");

    }

    public void updateImplaceableOnRemove(int x, int y){
        //Called after every placement and removal of a bulb to ensure the correct marking as of placeable cells
        int tempX;
        int tempY;
        Wall tempWall;
        EmptyField tempField;


        if(!isOutOfBounds(x+1, y) && isNumberedWall(x+1, y)){
            tempWall = (Wall) tempBoard.get(x+1, y);
            tempWall.numberLeftoverBulbs++;
        }
        if(!isOutOfBounds(x-1, y) && isNumberedWall(x-1, y)){
            tempWall = (Wall) tempBoard.get(x-1, y);
            tempWall.numberLeftoverBulbs++;
        }
        if(!isOutOfBounds(x, y+1) && isNumberedWall(x, y+1)){
            tempWall = (Wall) tempBoard.get(x, y+1);
            tempWall.numberLeftoverBulbs++;
        }
        if(!isOutOfBounds(x, y-1) && isNumberedWall(x, y-1)){
            tempWall = (Wall) tempBoard.get(x, y-1);
            tempWall.numberLeftoverBulbs++;
        }


        for(int i = 0; i<gameboard.emptyFieldLocationswithWalls.size();i++){
            tempX = gameboard.emptyFieldLocationswithWalls.get(i)[0];
            tempY = gameboard.emptyFieldLocationswithWalls.get(i)[1];
            if(!isEmptyField(tempX, tempY)){continue;}
            tempField = (EmptyField) tempBoard.get(tempX, tempY);

            tempField.implaceable = false;
            
            if(!isOutOfBounds(tempX+1, tempY) && isNumberedWall(tempX+1, tempY)){
                tempWall = (Wall) tempBoard.get(tempX+1, tempY);
                if(tempWall.numberLeftoverBulbs == 0){tempField.implaceable = true;}
            }
            if(!isOutOfBounds(tempX-1, tempY) && isNumberedWall(tempX-1, tempY)){
                tempWall = (Wall) tempBoard.get(tempX-1, tempY);
                if(tempWall.numberLeftoverBulbs == 0){tempField.implaceable = true;}
            }
            if(!isOutOfBounds(tempX, tempY+1) && isNumberedWall(tempX, tempY+1)){
                tempWall = (Wall) tempBoard.get(tempX, tempY+1);
                if(tempWall.numberLeftoverBulbs == 0){tempField.implaceable = true;}
            }
            if(!isOutOfBounds(tempX, tempY-1) && isNumberedWall(tempX, tempY-1)){
                tempWall = (Wall) tempBoard.get(tempX, tempY-1);
                if(tempWall.numberLeftoverBulbs == 0){tempField.implaceable = true;}
            }
        }
    }

    public void setEmptyFieldsWithNumberedWalls(){
        //Bildet eine Liste von leeren Feldern die an nummerierten Mauern stehen
        int tempX;
        int tempY;
        for(int i = 0; i<gameboard.emptyFieldLocations.size(); i++){
            tempX = gameboard.emptyFieldLocations.get(i)[0];
            tempY = gameboard.emptyFieldLocations.get(i)[1];

            if(isNumberedWall(tempX+1, tempY)){
                Integer[] tempInt = {tempX, tempY};
                gameboard.emptyFieldLocationswithWalls.add(tempInt);
            }
            else if(isNumberedWall(tempX-1, tempY)){
                Integer[] tempInt = {tempX, tempY};
                gameboard.emptyFieldLocationswithWalls.add(tempInt);
            }
            else if(isNumberedWall(tempX, tempY+1)){
                Integer[] tempInt = {tempX, tempY};
                gameboard.emptyFieldLocationswithWalls.add(tempInt);
            }
            else if(isNumberedWall(tempX, tempY-1)){
                Integer[] tempInt = {tempX, tempY};
                gameboard.emptyFieldLocationswithWalls.add(tempInt);
            }
        }
    }

    public void setCandidates(){
        //Bildet die Liste von Kandidaten, jedoch werden hier redundante Einträge vermieden (Falls ein Feld an mehreren nummerierten Mauern steht)
        for(int i = 0; i<gameboard.emptyFieldLocationswithWalls.size();i++){
            int tempX = gameboard.emptyFieldLocationswithWalls.get(i)[0];
            int tempY = gameboard.emptyFieldLocationswithWalls.get(i)[1];
            if(!isEmptyField(tempX, tempY) || isIlluminated(tempX, tempY) || isImplaceable(tempX, tempY)){continue;}
            
            if(isNumberedWall(tempX+1,tempY)){
                Wall tempWall = (Wall) tempBoard.get(tempX+1, tempY);
                if(tempWall.numberLeftoverBulbs>0){
                    Integer[] tempInt = {tempX,tempY};
                    gameboard.numberedWallCandidates.add(tempInt);
                    continue;
                }
            }
            if(isNumberedWall(tempX-1,tempY)){
                Wall tempWall = (Wall) tempBoard.get(tempX-1, tempY);
                if(tempWall.numberLeftoverBulbs>0){
                    Integer[] tempInt = {tempX,tempY};
                    gameboard.numberedWallCandidates.add(tempInt);
                    continue;
                }
            }
            if(isNumberedWall(tempX,tempY+1)){
                Wall tempWall = (Wall) tempBoard.get(tempX, tempY+1);
                if(tempWall.numberLeftoverBulbs>0){
                    Integer[] tempInt = {tempX,tempY};
                    gameboard.numberedWallCandidates.add(tempInt);
                    continue;
                }
            }
            if(isNumberedWall(tempX,tempY-1)){
                Wall tempWall = (Wall) tempBoard.get(tempX, tempY-1);
                if(tempWall.numberLeftoverBulbs>0){
                    Integer[] tempInt = {tempX,tempY};
                    gameboard.numberedWallCandidates.add(tempInt);
                    continue;
                }
            }
        }
    }

    public boolean backtrackSolver2(){
    //Basic backtrack solver
        for(int i = 0; i<gameboard.locationPlaceableNonTrivialBulbs.size();i++){
            int x = gameboard.locationPlaceableNonTrivialBulbs.get(i)[0];
            int y = gameboard.locationPlaceableNonTrivialBulbs.get(i)[1];
                    if(ForwardChecking){
                        if(isIlluminationConstraintArchievable() == false || isWallConstraintArchievable() == false){
                            fowardCheckPruning++;
                            return false;
                        }
                    }
                    
                    if(setBulb(x,y)){
                        if(backtrackSolver2()){
                            return true;
                        }
                        else{
                            backtrackingSteps++;
                            removeBulb(x, y);
                        }
                    }
        }
        
        checkForSolution++;
        if(validateSolution()){
            System.out.println("FINAL SOLUTION");
            printGameboard();
            return true;}
        else {
            return false;}
    }

    public boolean candidateBacktrackFowardSeachSolver(){
        //Backtrack solver with forward checks
        int tempX;
        int tempY;
        
        for(int i = 0; i<gameboard.emptyFieldLocationswithWalls.size();i++){
            tempX = gameboard.emptyFieldLocationswithWalls.get(i)[0];
            tempY = gameboard.emptyFieldLocationswithWalls.get(i)[1];
            
            if(ForwardChecking){
                if(isWallConstraintArchievable() == false || isIlluminationConstraintArchievable() == false){
                    fowardCheckPruning++;
                    return false
                    ;}
            }
            if(setBulb(tempX, tempY)){   
                if(candidateBacktrackFowardSeachSolver()){
                    return true;
                }
                else{
                    removeBulb(tempX, tempY);
                    backtrackingSteps++;
                }
            }
        }
        checkForSolution++;
        if(validateNumBulbsOnWall()){
            System.out.println(gameboard.emptyFieldLocations.size() + " Empty Fields left");
            setLocationPlaceableNonTrivialBulbs();
            backtrackSolver2();
            return true;
        }
        else{

            return false;
        }
    }

    public boolean isWallConstraintArchievable(){
        // Checks if wall constraint is archievable
        int tempX;
        int tempY;
        for(int i = 0; i<gameboard.numberedWallLocations.size(); i++){
            int countPlaceable = 0;
            int bulbCounter = 0;
            tempX = gameboard.numberedWallLocations.get(i)[0];
            tempY = gameboard.numberedWallLocations.get(i)[1];
            Wall tempWall = (Wall) tempBoard.get(tempX, tempY);

            if(isBulb(tempX+1,tempY)){bulbCounter++;}
            if(isBulb(tempX-1,tempY)){bulbCounter++;}
            if(isBulb(tempX,tempY+1)){bulbCounter++;}
            if(isBulb(tempX,tempY-1)){bulbCounter++;}
            //Wenn
            if(bulbCounter == tempWall.numberAdjascentBulbs){continue;}
            
            //if placeable < numberLeftoverBulbs
            if(isEmptyField(tempX+1, tempY) && !isImplaceable(tempX+1, tempY) && !isIlluminated(tempX+1, tempY)){countPlaceable++;}
            if(isEmptyField(tempX-1, tempY) && !isImplaceable(tempX-1, tempY) && !isIlluminated(tempX-1, tempY)){countPlaceable++;}
            if(isEmptyField(tempX, tempY+1) && !isImplaceable(tempX, tempY+1) && !isIlluminated(tempX, tempY+1)){countPlaceable++;}
            if(isEmptyField(tempX, tempY-1) && !isImplaceable(tempX, tempY-1) && !isIlluminated(tempX, tempY-1)){countPlaceable++;}

            if(countPlaceable < tempWall.numberLeftoverBulbs){
                return false;
            }
        }

        return true;
    }

    public boolean isIlluminationConstraintArchievable(){
        //Wenn es ein leeres Feld gibt das nicht platzierbar und nicht beleuchtet ist muss es auf der y oder x achse ein Feld geben dass noch platzierbar ist, sonst kann keine Lösung gefunden werden
        int tempX;
        int tempY;
        int iteratorX;
        int iteratorY;
        int placeable = 0;
        
        
        for(int i = 0; i<gameboard.emptyFieldLocations.size();i++){
            placeable = 0;
            tempX = gameboard.emptyFieldLocations.get(i)[0];
            tempY = gameboard.emptyFieldLocations.get(i)[1];
            iteratorX = tempX;
            iteratorY = tempY;
            

            if(!isEmptyField(tempX, tempY)){continue;}
            if(isEmptyField(tempX,tempY) && !isIlluminated(tempX, tempY)){

                if(!isImplaceable(tempX, tempY) && !isIlluminated(tempX, tempY)){continue;}
                tempX = iteratorX;
                tempY = iteratorY;
                
                while(isEmptyField(tempX+1, tempY)){
                    if(!isImplaceable(tempX+1,tempY) && !isIlluminated(tempX+1, tempY)){placeable++;}
                    tempX++;
                }
        
                tempX = iteratorX;
                tempY = iteratorY;
                while(isEmptyField(tempX-1, tempY)){
                    if(!isImplaceable(tempX-1,tempY) && !isIlluminated(tempX-1, tempY)){placeable++;}
                    tempX--;
                }
        
                tempX = iteratorX;
                tempY = iteratorY;
                while(isEmptyField(tempX, tempY+1)){
                    if(!isImplaceable(tempX,tempY+1) && !isIlluminated(tempX, tempY+1)){placeable++;}
                    tempY++;
                }
        
                tempX = iteratorX;
                tempY = iteratorY;
                while(isEmptyField(tempX, tempY-1)){
                    if(!isImplaceable(tempX,tempY-1) && !isIlluminated(tempX, tempY-1)){placeable++;}
                    tempY--;
                }

                if(placeable==0){
                    return false;
                }
            }
        }
        //gameboard.
        return true;
    }

    public boolean setBulb(int x, int y){
        //Setzt eine Birne falls möglich, beleuchtet die entsprechenden Felder und dekrementiert den LeftoverBulb Counter von nahen Mauern
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
        }
        else{
            return false;
        }
    }

    public void removeBulb(int x, int y){
        //Entfernt eine Birne, entfernt die Beleuchtung der Felder und inkrementiert den LeftoverBulb Counter falls die Birne an einer nummerierten Mauer stand
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
            updateImplaceableOnRemove(x, y);
        }

    }
    
    public void markAsImplaceable(int x, int y){
        //Markiert eine Mauer als nicht platzierbar
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
                if(isEmptyField(x, y) && !isIlluminated(x, y)){
                    notIlluminated++;
                }
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

    public void createSolutionspaceArrayX(){
        //Bildet ein Array von leeren nicht beleuchteten und nicht getrennten Feldern
        List<Integer> tempListX = new ArrayList<>();
        List<Integer> tempListY = new ArrayList<>();
        EmptyField tempField;
        //Integer[] noBulb = {-1,-1};
        for(int y = 0; y<tempBoard.height;y++){
            for(int x = 0; x<tempBoard.width;x++){
                if(isEmptyField(x,y)){
                    tempField = (EmptyField) tempBoard.get(x, y);
                    if(tempField.illuminated>0 || tempField.implaceable){
                    //Wenn das Feld beleuchtetet oder als nicht platzierbar gekennzeichnet ist wird die Liste der Hauptliste angefügt
                        if(tempListX.isEmpty()){
                            continue;
                        }
                        else{
                        
                        tempListX.add(0, -1);
                        tempListY.add(0, -1);
                        gameboard.solutionspaceArrayX.add(tempListX);
                        gameboard.solutionspaceArrayY.add(tempListY);
                        tempListX = new ArrayList<>();
                        tempListY = new ArrayList<>();
                        
                        }
    
                    }
                    else{
                        
                        tempListX.add(x);
                        tempListY.add(y);
                        if(isWall(x+1,y)){
                            tempListX.add(0, -1);
                            tempListY.add(0, -1);
                            gameboard.solutionspaceArrayX.add(tempListX);
                            gameboard.solutionspaceArrayY.add(tempListY);
                            tempListX = new ArrayList<>();
                            tempListY = new ArrayList<>();;}
                        }
                    }
                }
            }
        }
    
    public void createSolutionspaceArrayY(){
        //Bildet ein Array von leeren nicht beleuchteten und nicht getrennten Feldern
        ArrayList<Integer[]> tempList = new ArrayList<Integer[]>();
        EmptyField tempField;
        Integer[] noBulb = {-1,-1};
        for(int x = 0; x<tempBoard.height;x++){
            for(int y = 0; y<tempBoard.width;y++){
                if(isEmptyField(x,y)){
                    tempField = (EmptyField) tempBoard.get(x, y);
                    if(tempField.illuminated>0 || tempField.implaceable){
                        if(tempList.isEmpty()){
                            continue;
                        }
                        else{
                        tempList.add(0, noBulb);
                        tempList = new ArrayList<Integer[]>();

                        }
    
                    }
                    else{
                        Integer[] tempInt = {x,y};
                        tempList.add(tempInt);
                        if(isWall(x,y+1)){
                            tempList.add(0, noBulb);
                            tempList = new ArrayList<Integer[]>();
                        }
                    }
                }
            }
        }
    }

    public boolean checkConstraintViolation(int x, int y){
        //Die Prüfung der numbered Wall Constraints ist bei Platzierung nicht notwendig da es durch die Art der Platzierung bereits behandelt wird
        if(!isWall(x, y) && !isBulb(x, y) && isEmptyField(x, y) && !isIlluminated(x, y) && !isImplaceable(x, y)){
            return false;
        }
        return true;
    }

   
    public boolean isWall(int x, int y){
        //Prüft ob das Feld eine Mauer ist
        if(x>tempBoard.width-1 || x<0 || y>tempBoard.height-1|| y<0){return true;}
        if(tempBoard.get(x, y).getClass() == Wall.class){return true;}
        else{return false;}
    }

    public boolean isNumberedWall(int x, int y){
        //Prüft ob das Feld eine nummerierte Mauer ist
        if(!isOutOfBounds(x,y) && tempBoard.get(x, y).getClass() == Wall.class){
            Wall tempWall = (Wall) tempBoard.get(x, y);
            if(!tempWall.blank){
                return true;
            }
        }
        return false;
        
    }

    public boolean isEmptyField(int x, int y){
        //Prüft ob das Feld ein leeres Feld ist
        if(x>tempBoard.width-1 || x<0 || y>tempBoard.height-1|| y<0){return false;}
        if(tempBoard.get(x, y).getClass() == EmptyField.class){return true;}
        else{return false;}
    }

    public boolean isImplaceable(int x, int y){
        //Prüft ob das Feld ein platzierbares Feld ist
        if(x==-1 || y==-1){return true;}
        EmptyField tempField = (EmptyField) tempBoard.get(x, y);
        if(tempField.implaceable){return true;}
        else{return false;}
    }

    public boolean isIlluminated(int x, int y){
        //Prüft ob das Feld beleuchtet ist
       EmptyField tempField = (EmptyField) tempBoard.get(x, y);
       if(tempField.illuminated>0) return true;
       else return false;
    }

    public boolean isOutOfBounds(int x, int y){
        //Prüft ob das Feld sich auf dem Spielbrett befindet
        if(x>tempBoard.width-1 || x<0 || y>tempBoard.height-1|| y<0){return true;}
        else{return false;}
    }

    public boolean isBulb(int x, int y){
        //Prüft ob das Feld eine Birne ist
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
        //Prüft die Gültigkeit einer Lösung
        if(numNotIlluminated() == 0 && validateNumBulbsOnWall()){return true;}
        else{return false;}
    }

    public void placeTrivialBulbs(){
        //Setzt triviale Birnen
        boolean repeatTrigger = false;
        int tempX;
        int tempY;
        int tempFreeNeighbors;

        for(int i = 0;i<gameboard.emptyFieldLocations.size();i++){
            //Wenn ein Feld nur von Mauern umgeben ist soll eine Birne gesetzt werden
            int wallCounter = 0;
            tempX = gameboard.emptyFieldLocations.get(i)[0];
            tempY = gameboard.emptyFieldLocations.get(i)[1];
            if(isWall(tempX+1,tempY)){
                wallCounter++;
            }
            if(isWall(tempX-1,tempY)){
                wallCounter++;
            }
            if(isWall(tempX,tempY+1)){
                wallCounter++;
            }
            if(isWall(tempX,tempY-1)){
                wallCounter++;
            }
            if(wallCounter == 4){setBulb(tempX, tempY);}
        }

            for(int i=0;i<gameboard.numberedWallLocations.size();i++){
                tempFreeNeighbors = 0;
                tempX = gameboard.numberedWallLocations.get(i)[0];
                tempY = gameboard.numberedWallLocations.get(i)[1];    
                Wall tempWall = (Wall) tempBoard.get(tempX, tempY);
    
    
                //Wenn die Mauer eine Null anzeigt wird die von neumann nachbarschaft als nicht platzierbar gekennezeichnet
                if(tempWall.numberLeftoverBulbs == 0){
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
                    //Zuerst werden alle Nullmauern als nicht platzierbar gekennzeichnet
                }
            }

            
        do{
            for(int i=0;i<gameboard.numberedWallLocations.size();i++){
                repeatTrigger = false;
                tempFreeNeighbors = 0;
                tempX = gameboard.numberedWallLocations.get(i)[0];
                tempY = gameboard.numberedWallLocations.get(i)[1];    
                Wall tempWall = (Wall) tempBoard.get(tempX, tempY);
                //Wenn das Feld in der von neumann nachbarschaft leer ist, nicht beleuchetet wird und nicht als nicht platzierbar gekennzeichnet ist wird es als freier Nachbar gezählt
                if(isEmptyField(tempX+1,tempY) && !isIlluminated(tempX+1, tempY) && !isImplaceable(tempX+1, tempY)){tempFreeNeighbors++;}
                if(isEmptyField(tempX-1,tempY) && !isIlluminated(tempX-1, tempY) && !isImplaceable(tempX-1, tempY)){tempFreeNeighbors++;}
                if(isEmptyField(tempX,tempY+1) && !isIlluminated(tempX, tempY+1) && !isImplaceable(tempX, tempY+1)){tempFreeNeighbors++;}
                if(isEmptyField(tempX,tempY-1) && !isIlluminated(tempX, tempY-1) && !isImplaceable(tempX, tempY-1)){tempFreeNeighbors++;}

                if(tempWall.numberLeftoverBulbs == tempFreeNeighbors){
                    if(isEmptyField(tempX+1,tempY) && !isIlluminated(tempX+1, tempY) && !isImplaceable(tempX+1, tempY)){
                        setBulb(tempX+1, tempY);
                        repeatTrigger = true;
                        
                    }
                    if(isEmptyField(tempX-1,tempY)  && !isIlluminated(tempX-1, tempY) && !isImplaceable(tempX-1, tempY)){
                        setBulb(tempX-1, tempY);
                        repeatTrigger = true;
                        
                    }
                    if(isEmptyField(tempX,tempY+1) && !isIlluminated(tempX, tempY+1) && !isImplaceable(tempX, tempY+1)){
                        setBulb(tempX, tempY+1);
                        repeatTrigger = true;
                        
                    }
                    if(isEmptyField(tempX,tempY-1) && !isIlluminated(tempX, tempY-1) && !isImplaceable(tempX, tempY-1)){
                        setBulb(tempX, tempY-1);
                        repeatTrigger = true;
                        
                    }
                }
            }
            
        } while(repeatTrigger == true);
    }

}
    
    



