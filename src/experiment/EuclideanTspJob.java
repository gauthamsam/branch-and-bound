/*
 * @author gautham
 */
package experiment;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Map;

import shared.DoubleShared;
import solution.EuclideanTSPSolution;
import solution.Solution;
import tasks.BranchAndBound;
import api.Result;
import api.Task;


/**
 * This class represents the entire work involved in solving a Traveling Salesman Problem (TSP), where the cities are points in the 2D Euclidean plane.
 * The job is split into multiple tasks by the clients and then passed to the Compute Space for computation whose results are later obtained and composed to form the solution to the original problem.
 */
public class EuclideanTspJob implements Job<int[]>{

	/** The cities in 2D Euclidean plane that are part of the TSP. */
	private double[][] cities;
	
	/**
	 * Instantiates a new Euclidean TSP task.
	 *
	 * @param cities the cities in 2D Euclidean plane that are part of the TSP; it codes the x and y coordinates of city[i]: cities[i][0] is the x-coordinate of city[i] and cities[i][1] is the y-coordinate of city[i]
	 */
	public EuclideanTspJob(double[][] cities){
		this.cities = cities;		
	}	
	
	private long startTime;
	/**
	 * Generates multiple tasks from this job. The client decomposes the problem (job), constructing a set of Task objects
	 * The EuclideanTsp job is split into n-1 tasks (n corresponds to the number of cities), representing the (n-1)! factorial permutations that are needed to find the minimal tour
	 * 
	 */
	@Override
	public void generateTasks(Space space) {
		// prefix represents the path taken (permutation) from the root task up to the current task 
		int[] prefix = {0};
		// permutation represents the array that needs to be permuted for computing the minimal tour
		Integer[] permutation = new Integer[cities.length - 1];
		for(int i = 0; i < permutation.length; i++){
			permutation[i] = i + 1;
		}
		DoubleShared shared = new DoubleShared(Double.MAX_VALUE);
		// The first task has level = 0
		Solution<Double> solution = new EuclideanTSPSolution(cities, prefix, Arrays.asList(permutation));
		
		
		Task task = new BranchAndBound(solution, 0);
		
		try{
			//task.setShared(new DoubleShared(task.getLowerBound(permutation)));
			this.startTime = System.nanoTime();
			space.put(task);
		}
		catch(RemoteException e){
			e.printStackTrace();
		}
		catch(NullPointerException e){
			e.printStackTrace();
		}
	}

	/**
	 * Collects results from the Space, composing them into a solution to the original problem.
	 * Each result in the EuclideanTSP job is a map of the minimal tour among the permutations computed by this task and the cost involved for that tour. 	
	 */
	@Override
	public int[] collectResults(Space space) {
		System.out.println("Collect Results");
		int[] minTour = null;		
		Map<int[], Double> minTourMap = null;
		try {
			Result<Map<int[], Double>> result = space.take();
			long elapsedTime = System.nanoTime() - this.startTime;
			System.out.println("Elapsed Time for the task: " + elapsedTime + " ns");
			minTourMap = result.getTaskReturnValue();
			minTour = minTourMap.keySet().iterator().next();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}catch(NullPointerException e){
			e.printStackTrace();
		}
		System.out.println("Min Tour: " + Arrays.toString(minTour));
		return minTour;
	}
	
}
