/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patternbasedgraphdrawing;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Nathan van Beusekom (n.a.c.v.beusekom@tue.nl)
 * 
 * This class takes PatternRectangles and their scores (i.e. 'nodes' and 'weight'), and gives the maximum independent set (there is an 'edge' if rectangles are overlapping)
 * 
 */



public class MWISSolver {
    
    PatternRectangle[] candidates;
    
    ArrayList<ArrayList<Integer>> adjacencyList;
    
    double bestScore = 0;
    ArrayList<PatternRectangle> bestSet;
    
    
    // Todo: split into connected components! Important
    
    
    public void basicTest(){
        candidates = new PatternRectangle[5];
        candidates[0] = new PatternRectangle(0,0,2,2,5);
        candidates[1] = new PatternRectangle(0,2,2,2,5);
        candidates[2] = new PatternRectangle(1,1,2,2,19);
        candidates[3] = new PatternRectangle(2,0,2,2,5);
        candidates[4] = new PatternRectangle(2,2,2,2,5);
        fillAdjacencyList();
        boolean[] independent = new boolean[candidates.length];
        Arrays.fill(independent, true);
        recursiveBacktrack(0, new ArrayList<>(), 0, independent);
        printResults();
    }
    
    public void printResults(){
        System.out.println("Score: " + bestScore);
        System.out.println("In the best set:");
        for (int i = 0; i < bestSet.size(); i++) {
            System.out.println(bestSet.get(i).i + "," + bestSet.get(i).j);
        }
    }
    
    public void fillAdjacencyList(){
        adjacencyList = new ArrayList<>();
        for (int i = 0; i < candidates.length; i++) {
            ArrayList<Integer> adjacencies = new ArrayList<>();
            for (int j = 0; j < candidates.length; j++) {
                candidates[i].overlaps(candidates[j]);
                if(i!=j && candidates[i].overlaps(candidates[j])){
                    adjacencies.add(j);
                }
            }
            adjacencyList.add(adjacencies);
        }
    }
    
    /**
     * Exactly solves the MWIS problem with recursive backtracking
     * @param index: index from which we consider rectangles
     * @param currSet: current set
     * @param currScore: current score
     * @param independent: rectangles that may still be considered
     */
    
    public void recursiveBacktrack(int index, ArrayList<PatternRectangle> currSet, double currScore, boolean[] independent){
        for (int i = index; i < candidates.length; i++) {
            // Check if the ith candidate is not conflicting with currSet
            if(independent[i]){
                // Add it
                currSet.add(candidates[i]);
                boolean[] newIndependent = Arrays.copyOf(independent, independent.length);
                
                
                for (int j = 0; j < adjacencyList.get(i).size(); j++) {
                    newIndependent[adjacencyList.get(i).get(j)] = false;
                }
                newIndependent[i] = false;
                boolean sometrue = false;
                for (int j = 0; j < independent.length; j++) {
                    sometrue = sometrue || newIndependent[j];
                }
                double newScore = currScore + candidates[i].score;
                
                // Recurse if possible
                if(sometrue){
                    recursiveBacktrack(i+1, currSet, newScore, newIndependent);
                }
                // Else check if better
                else{
                    if(newScore > bestScore){
                        bestScore = newScore;
                        bestSet = new ArrayList<>(currSet);
                    }
                }
                    
                // Remove again
                currSet.remove(currSet.size()-1);
                
            }
        }
    }
    
}
