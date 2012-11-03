/*
 * @author gautham
 */
package solution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import shared.Shared;

/**
 * The EuclideanTSPSolution represents the partial tour (from the root to the current node) in the search tree of a Traveling Salesman Problem (TSP).
 */
public class EuclideanTSPSolution extends Solution<Double>{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The cities in 2D Euclidean plane that are part of the TSP. */
	protected double[][] cities;
	
	/** The taskPermutation denotes the permutation of cities that are yet to be explored from this node.*/
	private List<Integer> taskPermutation;
	
	/**
	 * Instantiates a new euclidean tsp solution.
	 *
	 * @param cities the cities
	 * @param pathFromRoot the path from root
	 * @param taskPermutation the task permutation
	 */
	public EuclideanTSPSolution(double[][] cities, int[] pathFromRoot, List<Integer> taskPermutation){
		this.cities = cities;
		this.pathFromRoot = pathFromRoot;
		this.taskPermutation = taskPermutation;
	}
	
	/* (non-Javadoc)
	 * @see solution.Solution#isComplete()
	 */
	@Override
	public boolean isComplete() {
		return pathFromRoot.length == cities.length;
	}

	/* (non-Javadoc)
	 * @see solution.Solution#getChildren(shared.Shared)
	 */
	@Override
	public Queue<Solution<Double>> getChildren(Shared shared) {
		Queue<Solution<Double>> children = new LinkedList<Solution<Double>>();
		//System.out.println("Task permutation size " + taskPermutation.size());
		if(taskPermutation != null){
			for(int i = 0, n = taskPermutation.size(); i < n; i++){
				int[] pathFromRoot = Arrays.copyOfRange(this.pathFromRoot, 0, this.pathFromRoot.length + 1);
				pathFromRoot[pathFromRoot.length - 1] = taskPermutation.get(i);
				
				List<Integer> childTaskPermutation = new ArrayList<Integer>(taskPermutation);
				
				childTaskPermutation.remove(i);
				
				Solution<Double> solution = new EuclideanTSPSolution(cities, pathFromRoot, childTaskPermutation);
				solution.computeLowerBound();
				
				double upperBound = (Double) shared.get();
				if(solution.lowerBound <= upperBound){
					children.add(solution);
				}
			}
		}
		return children;
	}

	/* (non-Javadoc)
	 * @see solution.Solution#getLowerBound()
	 */
	@Override
	public Double getLowerBound() {
		return lowerBound;
	}

	/* (non-Javadoc)
	 * @see solution.Solution#getPathFromRoot()
	 */
	@Override
	public int[] getPathFromRoot() {
		return pathFromRoot;
	}

	/** 
	 * The lower bound is computed by adding up the euclidean distance between the nodes in the path from root.
	 * @see solution.Solution#computeLowerBound()
	 */
	@Override
	public void computeLowerBound(){
		lowerBound = calculateDistance(pathFromRoot);		
		lowerBound += getEuclideanDistance(cities[pathFromRoot[pathFromRoot.length - 1]], cities[0]);		
	}
	
	/**
	 * Calculates distance between the cities passed in the given order 
	 *
	 * @param cityPermutation the city permutation
	 * @return double
	 */
	private double calculateDistance(int[] cityPermutation){
		double distance = 0;
		for(int j = 0; j < cityPermutation.length - 1; j++){
			distance += getEuclideanDistance(this.cities[cityPermutation[j]], this.cities[cityPermutation[j + 1]]);				
		}
		return distance;
	}
	
	/**
	 * Calculate the Euclidean distance.
	 *
	 * @param pointA the starting point
	 * @param pointB the ending point
	 * @return distance the distance between the points
	 */
	private double getEuclideanDistance(double[] pointA, double[] pointB){		
		double temp1 = Math.pow((pointA[0] - pointB[0]), 2);
		double temp2 = Math.pow((pointA[1] - pointB[1]), 2);
		double distance = Math.sqrt(temp1 + temp2);
		return distance;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("pathFromRoot " + Arrays.toString(pathFromRoot));
		sb.append("taskPermutation " + Arrays.toString(taskPermutation.toArray()));
		return sb.toString();
	}

	/** Compares the lower bound of this partial solution to the given solution.	 * 
	 * @see solution.Solution#compareTo(solution.Solution)
	 */
	@Override
	public boolean compareTo(Solution<?> solution) {
		return (Double)this.getLowerBound() <= (Double)solution.getLowerBound();
	}
	
}
