/*
 * @author gautham
 */
package system;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import shared.Shared;
import utils.Constants;
import api.Space;
import api.Task;

/**
 * This class enables different tasks to be executed by the Compute Space using its remote reference (proxy)
 * These tasks are run using the task's implementation of the execute method and the results are returned to the Compute Space.
 * Each task can either be executed right away (atomic) or be decomposed into multiple tasks. In the earlier case, the result is stored in the space while in the latter case, the subtasks are stored.
 */
public final class ComputerImpl extends UnicastRemoteObject implements Computer{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The remote reference to Space (proxy). */
	private Computer2Space space;	
	
	
	/** The shared object. */
	private Shared shared;
	
	/** The computer id. */
	private int computerId;
	
	/**
	 * Instantiates a new implementation object for the Computer Interface.
	 *
	 * @throws RemoteException the remote exception
	 */
	public ComputerImpl() throws RemoteException{		
	}

	/**
	 * Different tasks can be submitted to this method.
	 * The function checks to see if the task can be executed.
	 * If the task is atomic or if it's a successor task, then it's executed and the intermediate results are stored in Space. 
	 * Otherwise, the task is split into multiple subtasks and are put in Space.
	 * 
	 * @param <T> the generic type
	 * @param t the Task object
	 * @return Result the return value of the Task object's execute method
	 * @throws RemoteException the remote exception
	 */	
	@Override
	public <T> void execute(final Task<T> t) throws RemoteException {
		//System.out.println("Task: " + t);
		// If it's a regular task and if the base condition is not set (the task can be split into sub-tasks)
		
		t.setComputer(this);
		//System.out.println("Computer: Elapsed time for task " + (result.getTaskId() + 1) + ": " + elapsedTime + " ms");
		
		long elapsedTime = 0;
		if (t.isAtomic() || t.getTaskType() == Constants.SUCCESSOR_TASK) {
			// Execute the task and store the result on the Space
			long startTime = System.nanoTime();
			t.execute();
			long endTime = System.nanoTime();
			elapsedTime = endTime - startTime;
			t.setTaskRunTime(elapsedTime);
			try {
				space.storeResult(t);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		else{
			// Split the task into 'n' sub-tasks and 1 successor task and put
			// them all in Space.
			long startTime = System.nanoTime();
			List<Task<T>> tasks = t.splitTask();
			
			
			Task<T> successorTask = t.createSuccessorTask();
			Task[] inputList = null;
			if(tasks != null && tasks.size() > 0){ // if all the children have NOT been pruned
				inputList = new Task[tasks.size()];
				successorTask.setJoinCounter(inputList.length);
			}
			
			successorTask.setInputList(inputList);
			// Successor's successor should be the current task's successor.
			successorTask.setSuccessorTaskId((Object) t.getSuccessorTaskId());
			
			long endTime = System.nanoTime();
			elapsedTime = endTime - startTime;
			t.setTaskRunTime(elapsedTime);
			try {
				space.storeTasks(t, tasks, successorTask);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} 
			
	}
	
	

	/* (non-Javadoc)
	 * @see system.Computer#stop()
	 */
	@Override
	public void exit() throws RemoteException {
		System.out.println("Received command to stop.");
		System.exit(0);		
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {		
		String spaceDomainName = args[0];
		
		String spaceURL = "//" + spaceDomainName + "/" + Space.SERVICE_NAME;		
		Computer2Space remoteSpace = (Computer2Space) Naming.lookup(spaceURL);
		
		Computer computer = new ComputerImpl();
		remoteSpace.register(computer);
		computer.setSpace(remoteSpace);
		System.out.println("Computer ready.");
	}
	

	/* (non-Javadoc)
	 * @see system.Computer#setSpace(system.Computer2Space)
	 */
	@Override
	public void setSpace(Computer2Space space) throws Exception {
		this.space = space;		
	}

	/* (non-Javadoc)
	 * @see system.Computer#setShared(api.Shared)
	 */
	@Override
	public synchronized void setShared(final Shared<?> proposedShared, boolean canPropagate) throws RemoteException {
		if(this.shared == null || proposedShared.isNewerThan(this.shared)){
			//System.out.println("New cost received.");
			this.shared = proposedShared;
			if(canPropagate){
				Thread thread = new Thread(){
					public void run(){
						//System.out.println("Propagating to Space.");
						
						try {
							space.setShared(proposedShared, computerId);
						} catch (RemoteException e) {
							e.printStackTrace();							
						}						
					}
				};
				thread.start();
				
			}
		}
		else{
			//System.out.println("Old cost received from task.");
		}
	}

	/* (non-Javadoc)
	 * @see system.Computer#getShared()
	 */
	@Override
	public Shared getShared() throws RemoteException {
		return this.shared;
	}

	/* (non-Javadoc)
	 * @see system.Computer#setComputerId(int)
	 */
	@Override
	public void setComputerId(int computerId) throws RemoteException {
		this.computerId = computerId;		
	}

	/* (non-Javadoc)
	 * @see system.Computer#getComputerId()
	 */
	@Override
	public int getComputerId() throws RemoteException {
		return this.computerId;
	}
	
	
}