package com.example;

import java.util.*;
import java.io.OutputStream;
import com.google.common.collect.Lists;

import sim.engine.*;
import sim.field.grid.Grid2D;
import sim.field.grid.ObjectGrid2D;
import java.util.concurrent.TimeUnit;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

public class Agent implements Steppable {
    
//TODO
//Wann wird geprüft ob die Constraints erfüllt werden?
//Wie entscheidet der Spieler wo was platziert wird? -> aus Expertenwissen bedienen



    public ObjectGrid2D tempBoard = null;
    public GameBoard gameboard = null;
    public int stepcounter;
    public String strategy;
    public int backtrackingSteps = 0;
    public int counter = 0;

    int steps = 0;


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
        
        placeTrivialBulbs();
        printGameboard();
        System.out.println("Illumination Constraint archievable:" + isIlluminationConstraintArchievable());
        System.out.println("Wall Constraint archievable:" + isWallConstraintArchievable()); 
        
        setEmptyFieldsWithNumberedWalls();
        setCandidates();
        System.out.println(gameboard.numberedWallCandidates.size() + " Candidates, 2^" + gameboard.numberedWallCandidates.size() + " Possibilities");

        candidateBacktrackFowardSeachSolver();   
    }

    public void printGameboard(){
        //Emptyfield not illuminated not implaceable = _
        //NumberedWall = 0-4
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
        int tempX;
        int tempY;
        Wall tempWall;
        EmptyField tempField;
        //Called after every placement and removal of a bulb

        //Nach jeder Platzierung oder Entfernung werden die LeftoverBulbs aktualisiert
        //Auf Basis der LeftoverBulbs wird dann implaceable gesetzt


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


        //Zuerst Leftoverbulbs aktualisieren
        //
        //

        //Lösung
        //Liste von leeren Feldern bilden die an einer Mauer sind
        //Prüfen ob in der von neumann nachbarschaft eine Mauer ist (Auch Nullmauern)
        //Falls ja, den Wert leftoverbulbs speichern
        //Falls einer der Werte 0 ist muss das Feld als implaceable markiert werden
        //
        //Falls eine Birne entfernt wird werden auch alle werte gespeichert
        //Falls alle Werte nicht 0 sind wird das Feld als placeable markiert

        //TODO
        //Emptyfieldlocations mit mindestens einer nahen Mauer erstellen
        //Leftoverbulbs aktualisieren
        //

        
        
            //Wenn hier jetzt eine Birne steht, muss leftoverbulbs bei jeder nahen Mauer dekrementiert werden
        

        //UpdateOnSet
        //Nimmt die location der Bulb, schaut sich die Mauern in der Nachbarschaft an und dekrementiert lefoverbulbs

        //UpdateOnRemove
        //Nimmt die location der Bulb, schaut sich das jetzt leere Feld an, inkrementiert leftoverbalbs und schaut dann ob eine nahe Mauer noch leftoverbulbs 0 ist, falls ja wird es als implaceable markiert, sonst als placeable
    }

    public void setEmptyFieldsWithNumberedWalls(){
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

    public void setCandidatesOld(){
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
                if(isEmptyField(tempX+1, tempY) && !isIlluminated(tempX+1, tempY) && !isImplaceable(tempX+1, tempY)){
                    Integer[] location = {tempX+1, tempY};
                    gameboard.numberedWallCandidates.add(location);
                }
                if(isEmptyField(tempX-1, tempY) && !isIlluminated(tempX-1, tempY) && !isImplaceable(tempX-1, tempY)){
                    Integer[] location = {tempX-1, tempY};
                    gameboard.numberedWallCandidates.add(location);
                }
                if(isEmptyField(tempX, tempY+1) && !isIlluminated(tempX, tempY+1) && !isImplaceable(tempX, tempY+1)){
                    Integer[] location = {tempX, tempY+1};
                    gameboard.numberedWallCandidates.add(location);
                }
                if(isEmptyField(tempX, tempY-1) && !isIlluminated(tempX, tempY-1) && !isImplaceable(tempX, tempY-1)){
                    Integer[] location = {tempX, tempY-1};
                    gameboard.numberedWallCandidates.add(location);
                }
            }
        }
    }

    public void setCandidates(){
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

    public boolean backtrackSolver(){
    
        for(int y = 0; y<tempBoard.height;y++){
            for(int x = 0; x<tempBoard.width;x++){
                //if(isEmptyField(x, y) && !isImplaceable(x, y)){
                    //System.out.println(".");
                    if(setBulb(x,y)){
                        //printGameboard();
                        //printGameboard();
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
                    //if(isWallConstraintArchievable() == false){return false;}
                    if(isIlluminationConstraintArchievable() && setBulb(x,y)){
                        //printGameboard();                        
                        //printGameboard();
                        if(backtrackSolver2()){
                            return true;
                        }
                        else{
                            //backtrackingSteps++;
                            removeBulb(x, y);
                        }
                    }
                    
                //}
                //return false;
         
        }
        //printGameboard();
        //System.out.println("Number not illuminated: " + numNotIlluminated());
        //printGameboard();
        if(validateSolution()){
            System.out.println("FINAL SOLUTION");
            printGameboard();
            return true;}
        else {
            //System.out.println("No Solution found");
            return false;}
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
                        //Wenn tempX == -1 soll ein blocker für die Liste gesetzt werden, der Blocker würde wenn vorhanden in Zeile 220 gehoben werden 
                        //printGameboard();
                        if(tempX == -1){continue;}
                        if(backtrackSolverArray()){
                            return true;
                        }
                        else{
                            //RemoveBlocker
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
            //Prüfung ob die constraints überhaupt noch erfüllbar sind, wenn nicht return false; FOWARD CHECKING!!!
            if(setBulb(tempX, tempY)){
                //FUNKTIONIERT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! fast
                //printGameboard();
                //if(isWallConstraintArchievable() == false && isIlluminationConstraintArchievable() == false){
                    //removeBulb(tempX, tempY);
                    //continue;
                //}

                if(greedyBacktrackSolver()){
                    printGameboard();
                    if(isIlluminationConstraintArchievable() == false){return false;}
                    System.out.println("Illumination Constraint archievable:" + isIlluminationConstraintArchievable());
                    System.out.println("Wall constraint archievable: " + isWallConstraintArchievable());
                    //HIER!
                    return true;
                }
                else{
                    removeBulb(tempX, tempY);
                    /*

                    if(validateNumBulbsOnWall()){
                        //printGameboard();
                        setLocationPlaceableNonTrivialBulbs();
                        System.out.println("Illumination Constraint archievable:" + isIlluminationConstraintArchievable());
                        //printGameboard();
                        System.out.println("Possible Solution found");
                        //backtrackSolver2();
                        //Wenn es ein leeres Feld gibt das nicht beleuchtet ist, als nicht platzierbar markiert ist und auf der X und Y Achse kein freies leeres Feld hat das nicht beleuchtet und als nicht platzierbar gekennzeichnet ist
                        //if so, false

                        return false;
                        //return true;
                        
                    }
                    */
                    
                    //Ab hier können keine Kandidaten mehr gesetzt werden
                    //setLocationPlaceableNonTrivialBulbs();
                    //System.out.println(validateNumBulbsOnWall());
                    //printGameboard();
                    //printGameboard();
                    //if(validateNumBulbsOnWall()){
                        //Wenn die numbered Wall constraints erfüllt sind sollen die restlichen platzierungen probiert werden,
                        //Wenn die nicht möglich sind soll die nächste Kandidatenkombination geprüft werden.
                        //createSolutionspaceArrayX();
                        //setLocationPlaceableNonTrivialBulbs();
                        //System.out.println("Locationsize:"+gameboard.locationPlaceableNonTrivialBulbs.size());
                        //printGameboard();
                        //System.out.println("Possible Solution found");
                        //backtrackSolverArray();
                        //backtrackSolver2();
                        
                        
                    //}
                    //else removeBulb(tempX, tempY);
                    
                }

            }

        }
        //printGameboard();
        if(validateNumBulbsOnWall()){
            System.out.println("Possible Solution found");
            /*
            //Falls eine Lösung gefunden wird soll hier der andere Solver genutzt werden, dieser soll false returnen wenn keine Lösung gefunden wird
            //System.out.println("Possible Solution found");
            System.out.println("Illumination Constraint archievable:" + isIlluminationConstraintArchievable());
            System.out.println("Wall constraint archievable: " + isWallConstraintArchievable());
            //printGameboard();
            setLocationPlaceableNonTrivialBulbs();
            printGameboard();
            backtrackSolver2();
            */
            return true;
        }
        else{
            return false;
        }
    }

    public boolean candidateBacktrackFowardSeachSolver(){
        int tempX;
        int tempY;
        //List<Integer[]> fowardCheckingCandidates = new ArrayList<>();
        
        //System.out.println(iterationCounter);
        for(int i = 0; i<gameboard.numberedWallCandidates.size();i++){
            tempX = gameboard.numberedWallCandidates.get(i)[0];
            tempY = gameboard.numberedWallCandidates.get(i)[1];
            //Wenn die numbered Wall constraints nicht erfüllt sind soll direkt die nächste Iteration genommen werden (Wird das schon?)
            //Prüfung ob die constraints überhaupt noch erfüllbar sind, wenn nicht return false; FOWARD CHECKING!!!
            
            if(isWallConstraintArchievable() && isIlluminationConstraintArchievable() && setBulb(tempX, tempY)){   


                //FOWARD CHECKING
                //Wenn ein Kandidat gesetzt wurde wird geprüft ob es Mauern gibt wo number placeable == numberleftoverbulbs
                //Falls ja wird platziert und neu gestartet, so lange bis nichts platziert wird
                //Jede platzierte Birne wird in einer Kandidatenliste gespeichert
                //Falls eine Verletzung auftritt werden die Kandidaten aus der Liste entfernt

                //printGameboard();
                //if(isWallConstraintArchievable() == false){
                    //Es loopt wenn keine Lösung beim ersten Versuch möglich ist
                    //removeBulb(tempX, tempY);
                    //continue;
                //}
                //FUNKTIONIERT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                //if(iterationCounter >= 1 && isWallConstraintArchievable() == false){
                    //removeBulb(tempX, tempY);
                    //return false;
                //}
                //printGameboard();
                if(candidateBacktrackFowardSeachSolver()){
                    return true;
                }
                else{removeBulb(tempX, tempY);}
            }
        }
        //printGameboard();
        //counter++;
        //if(counter%1000000 == 1){System.out.println("1 Million tries");}
        //if(!isWallConstraintArchievable() || !isIlluminationConstraintArchievable()){return false;}
        if(validateNumBulbsOnWall()){
            //printGameboard();
            //System.out.println("Possible Solution found");
            //System.out.println("Illumination Constraint archievable:" + isIlluminationConstraintArchievable());
            if(isIlluminationConstraintArchievable() == false){return false;}
            setLocationPlaceableNonTrivialBulbs();
            backtrackSolver2();
            return true;
        }
        else{
            return false;
        }
    }

    public boolean isWallConstraintArchievable(){
        int tempX;
        int tempY;
        //Zum validieren Rückwärts laufen lassen, verletzungen hinten sind wahrscheinlicher?
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
            updateImplaceableOnRemove(x, y);
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

    public void setLocationNumberedWalls(){
        //Veraltet
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

    public boolean isNumberedWall(int x, int y){
        if(!isOutOfBounds(x,y) && tempBoard.get(x, y).getClass() == Wall.class){
            Wall tempWall = (Wall) tempBoard.get(x, y);
            if(!tempWall.blank){return true;}
        }
        return false;
        
    }

    public boolean isEmptyField(int x, int y){
        if(x>tempBoard.width-1 || x<0 || y>tempBoard.height-1|| y<0){return false;}
        if(tempBoard.get(x, y).getClass() == EmptyField.class){return true;}
        else{return false;}
    }

    public boolean isImplaceable(int x, int y){
        if(x==-1 || y==-1){return true;}
        EmptyField tempField = (EmptyField) tempBoard.get(x, y);
        if(tempField.implaceable){return true;}
        else{return false;}
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

    public void placeTrivialBulbs(){
        //first degree smartmode
        //place trivial bulbs (including surrounding walls)
        //only one iteration

        //second degree smartmode
        //place trivial bulbs iteratively (including surrounding walls)


        //check for implaceable fields in a x++ and y++ loop
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

    public void setTrivialBulbs(){
        List<Integer[]> tempList = new ArrayList<>();
        boolean repeatTrigger = false;
        int tempFreeNeighbors = 0;
        int tempX;
        int tempY;
        Integer[] tempInt = new Integer[2];
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
                        tempInt[0] = tempX+1;
                        tempInt[1] = tempY;
                        tempList.add(tempInt);
                        repeatTrigger = true;
                        
                    }
                    if(isEmptyField(tempX-1,tempY)  && !isIlluminated(tempX-1, tempY) && !isImplaceable(tempX-1, tempY)){
                        setBulb(tempX-1, tempY);
                        tempInt[0] = tempX-1;
                        tempInt[1] = tempY;
                        tempList.add(tempInt);
                        repeatTrigger = true;
                        
                    }
                    if(isEmptyField(tempX,tempY+1) && !isIlluminated(tempX, tempY+1) && !isImplaceable(tempX, tempY+1)){
                        setBulb(tempX, tempY+1);
                        tempInt[0] = tempX;
                        tempInt[1] = tempY+1;
                        tempList.add(tempInt);
                        repeatTrigger = true;
                        
                    }
                    if(isEmptyField(tempX,tempY-1) && !isIlluminated(tempX, tempY-1) && !isImplaceable(tempX, tempY-1)){
                        setBulb(tempX, tempY-1);
                        tempInt[0] = tempX;
                        tempInt[1] = tempY-1;
                        tempList.add(tempInt);
                        repeatTrigger = true;
                        
                    }
                }
            }
            
        }while(repeatTrigger == true);
    }
}
    
    



