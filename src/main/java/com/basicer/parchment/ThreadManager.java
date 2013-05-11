package com.basicer.parchment;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import com.basicer.parchment.EvaluationResult.BranchEvaluationResult;
import com.basicer.parchment.EvaluationResult.EvalCallback;

public class ThreadManager {
	
	final long SLEEP_STEP = 50;
	
	private static ThreadManager _instance;
	public static ThreadManager instance() {
		if ( _instance == null ) _instance = new ThreadManager();
		return _instance;
	}
	
	private static class WorkItem implements Comparable<WorkItem> {
		private long nextTimeSlice;
		private TCLEngine engine;
		
		public WorkItem(TCLEngine e) {
			this.engine = e;
			nextTimeSlice = System.currentTimeMillis();
		}
		
	
		public int compareTo(WorkItem o) {
			if ( o.nextTimeSlice == nextTimeSlice ) return 0;
			return o.nextTimeSlice > nextTimeSlice ? -1 : 1; 
		}
		
		public boolean doWork() {
			boolean out = true;
			int i = 0;
			while ( true ) {
				if ( ++i > 10 ) break;
				out = this.engine.step(true);
				EvaluationResult deepest = this.engine.getDeepestEvaluationResult();
				if ( deepest != null && deepest instanceof EvaluationResult.BranchEvaluationResult ) {
					Long when = ((EvaluationResult.BranchEvaluationResult)deepest).getScheduleAfter();
					if ( when != null ) nextTimeSlice = when;
					return true;
				}
				if ( out == false ) break;
			}
			nextTimeSlice = System.currentTimeMillis();
			if ( out == false ) {
				
			}
			
			return out;
		}
		
	}
	
	PriorityQueue<WorkItem> work;
	
	public ThreadManager() {
		work = new PriorityQueue<WorkItem>();
	}


	public void submitWork(BranchEvaluationResult br) {
		work.add(new WorkItem(new TCLEngine(br)));
	}
	
	public long doWork() {
		try {
			WorkItem i = work.peek();
			if ( i == null ) return System.currentTimeMillis() + SLEEP_STEP;
			if ( i.nextTimeSlice > System.currentTimeMillis() ) return i.nextTimeSlice;
			Debug.info("Doing work, Pool at %d items.", work.size());
			work.poll();
			if ( i.doWork() != false ) work.add(i); 
			else i = work.peek();
			if ( i != null ) return i.nextTimeSlice;
			return System.currentTimeMillis() + SLEEP_STEP;
		} catch ( Exception ex ) {
			ex.printStackTrace();
			return System.currentTimeMillis() + SLEEP_STEP;
		}
	}
	
}
