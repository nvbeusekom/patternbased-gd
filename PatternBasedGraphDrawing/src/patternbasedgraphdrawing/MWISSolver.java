/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patternbasedgraphdrawing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.stream.IntStream;

/**
 *
 * @author Nathan van Beusekom (n.a.c.v.beusekom@tue.nl)
 * 
 * This class takes PatternRectangles and their scores (i.e. 'nodes' and 'weight'), and gives the maximum independent set (there is an 'edge' if rectangles are overlapping)
 * 
 */



public class MWISSolver {
    ArrayList<PatternRectangle> allPatterns;
    
    // Only for exact solving
    PatternRectangle[] candidates;
    
    ArrayList<ArrayList<Integer>> adjacencyList;
    
    double bestScore = 0;
    ArrayList<PatternRectangle> bestSet;
    
    // For VND-ILS (see https://doi.org/10.1007/s11590-017-1128-7 for variable meaning)
    int randSeed;
    int c1 = 1;
    int c2 = 1;
    int c3 = 1;
    int c4 = 1;
    
    Random random;
    
    HashMap<Integer,Double> mu = new HashMap<>();
    
    // Todo: split into connected components! Important

    public MWISSolver(ArrayList<PatternRectangle> allPatterns, int randSeed) {
        this.allPatterns = allPatterns;
        this.randSeed = randSeed;
    }
    
    
    
    public ArrayList<PatternRectangle> solve(){
        this.candidates = new PatternRectangle[allPatterns.size()];
        allPatterns.toArray(candidates);;
        fillAdjacencyList();
        boolean[] independent = new boolean[candidates.length];
        Arrays.fill(independent, true);
        recursiveBacktrack(0, new ArrayList<>(), 0, independent);
        return bestSet;
    }
    
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
        if(bestSet == null){
            System.out.println("Set is empty");
            return;
        }
        System.out.println("Score: " + bestScore);
        System.out.println("In the best set:");
        for (int i = 0; i < bestSet.size(); i++) {
            PatternRectangle r = bestSet.get(i);
            System.out.println(r.i + "," + r.j + " to " + (r.i+r.h-1) + "," + (r.j+r.w-1) + " of type: " + r.pattern.name());
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
     * Runs in O(2^n) 💀
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
    
    // Below is a hybrid iterated local search heuristic
    // Nogueira, Bruno, Rian GS Pinheiro, and Anand Subramanian. 
    // "A hybrid iterated local search heuristic for the maximum weight independent set problem." 
    // Optimization Letters 12 (2018): 567-583.
    public void ILS_VND(int maxIter){
        random = new Random(randSeed);
        
        fillAdjacencyList();
        
        //Init mu
        for (int i = 0; i < allPatterns.size(); i++) {
            mu.put(i, allPatterns.get(i).score);
        }
        
        HashSet<Integer> s0 = initialize();
        
        HashSet<Integer> s = localSearch(s0);
        
    }
    
    public HashSet<Integer> localSearch(HashSet<Integer> currentSolution){
        int k = 1;
        while(k <= 2){
            HashSet<Integer> sPrime;
            ArrayList<Integer> notIn = notInSolution(currentSolution);
            
            // Shuffle all indices to check them in random order
            List<Integer> toImprove = IntStream.range(0, allPatterns.size()).boxed().toList();
            Collections.shuffle(toImprove, random);
            
            for (Integer i : notIn) {
                if(mu.get(i) > 0){
                    // Remove overlapping and add i???
                }
            }
        }
    }
    
    // We may assume currentSolution is sorted
    public ArrayList<Integer> notInSolution(HashSet<Integer> currentSolution){
        ArrayList<Integer> notIn = new ArrayList<>();
        for (int i = 0; i < allPatterns.size(); i++) {
            if(!currentSolution.contains(i)){
                notIn.add(i);
            }
            
        }
        return notIn;
    }
    
    public HashSet<Integer> perturb(int k, HashSet<Integer> currentSolution){
        // Add c1 random vertices not yet in there,
        Stack<Integer> notIn = new Stack<>();
        notIn.addAll(notInSolution(currentSolution));
        
        Collections.shuffle(notIn,random);
        
        ArrayList<Integer> toAdd = new ArrayList<>();
        
        // Decide on vertices to be added
        for (int i = 0; i < k; i++) {
            toAdd.add(notIn.pop());
        }
        
        // Remove overlapping
        for (Integer i : currentSolution) {
            for (Integer checkOverlapI : toAdd) {
                PatternRectangle checkOverlap = allPatterns.get(checkOverlapI);
                if(checkOverlap.overlaps(allPatterns.get(i))){
                    remove(currentSolution,i);
                    break;
                }    
            }
        }
        // Add perturbation
        insert(currentSolution, toAdd);
        // Add free vertices
        return addFreeVertices(currentSolution);
    }
    
    public void insert(HashSet<Integer> currentSolution, ArrayList<Integer> insertion){
        for (Integer i : insertion) {
            insert(currentSolution, i);
        }
    }
    
    // Insert, but keep it sorted
    public void insert(HashSet<Integer> currentSolution, int insertion){
        PatternRectangle rect = allPatterns.get(insertion);
        
        currentSolution.add(insertion);
        
        for (Integer neighbour : adjacencyList.get(insertion)) {
            mu.put(neighbour, mu.get(neighbour) - rect.score);
        }
    }
    
    public void remove(HashSet<Integer> currentSolution, ArrayList<Integer> removal){
        for (Integer i : removal) {
            insert(currentSolution, i);
        }
    }
    
    public void remove(HashSet<Integer> currentSolution, int removal){
        PatternRectangle rect = allPatterns.get(removal);
        currentSolution.remove(removal);
        for (Integer neighbour : adjacencyList.get(removal)) {
            mu.put(neighbour, mu.get(neighbour) + rect.score);
        }
    }
    
    public HashSet<Integer> initialize(){
        return addFreeVertices(new HashSet<>());
        
    }
    
    public HashSet<Integer> addFreeVertices(HashSet<Integer> currentSolution){
        ArrayList<Integer> availableIndices = new ArrayList<>();
        // Check for each pattern(index) whether it overlaps with one in the current solution. If not, add it to availableIndices.
        for (int i = 0; i < allPatterns.size(); i++) {
            boolean free = true;
            for(Integer toCheck : currentSolution){
                
                if(allPatterns.get(i).overlaps(allPatterns.get(toCheck))){
                    free = false;
                }
            }
            if(free){
                availableIndices.add(i);
            }
        }
        
        HashSet<Integer> result = new HashSet<>();
        result.addAll(currentSolution);
        
        // Randomly put in free vertices and update availableIndices
        while(availableIndices.size() > 0){
            ArrayList<Integer> newIndices = new ArrayList<>();
            int ind = availableIndices.get(random.nextInt(availableIndices.size()));
            insert(result,ind);
            for (Integer check : availableIndices) {
                if(!allPatterns.get(check).overlaps(allPatterns.get(ind))){
                    newIndices.add(check);
                }
            }
            availableIndices = newIndices;
            
        }
        return result;
        
    }
    
}
