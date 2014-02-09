package com.basicer.parchment;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Callable;

import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.EvaluationResult.EvalCallback;
import com.basicer.parchment.bukkit.ParchmentPluginLite;
import com.basicer.parchment.tcl.Eval;
import com.google.common.base.Function;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitWorker;

import javax.annotation.Nullable;

public class ThreadManager {
	
	final long SLEEP_STEP = 50;
	
	private static ThreadManager _instance;
	public static ThreadManager instance() {
		if ( _instance == null ) _instance = new ThreadManager();
		return _instance;
	}

	public static boolean amInSyncThread() {
		return Thread.currentThread().getName().equals("Server thread");
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
			long timeout = System.currentTimeMillis() + 5;
			while ( true ) {
				if ( System.currentTimeMillis() > timeout ) break;
				out = this.engine.step(true);
				EvaluationResult deepest = this.engine.getDeepestEvaluationResult();
				if ( deepest != null && deepest instanceof BranchEvaluationResult ) {
					Long when = ((BranchEvaluationResult)deepest).getScheduleAfter();
					if ( when != null ) {
						nextTimeSlice = when;
						return true;
					}

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

	Function<Callable<EvaluationResult>, EvaluationResult> workerCommandGuard;

	public ThreadManager() {
		work = new PriorityQueue<WorkItem>();
		workerCommandGuard = new Function<Callable<EvaluationResult>, EvaluationResult>() {

			@Override
			public EvaluationResult apply(@Nullable Callable<EvaluationResult> evaluationResultCallable) {
				try {
					if ( amInSyncThread() ) {
						//System.out.println("Am in Thread " + Thread.currentThread().getName());
						return evaluationResultCallable.call();
					}
					else {
						//System.out.println("Deferring to main thread..."  + Thread.currentThread().getName());
						return Bukkit.getScheduler().callSyncMethod(ParchmentPluginLite.instance(), evaluationResultCallable).get();
					}
				} catch ( Exception ex ) {
					throw new RuntimeException(ex);
				}
			}
		};
	}


	public void submitWork(BranchEvaluationResult br) {
		TCLEngine engine = new TCLEngine(br);
		engine.commandGuard = workerCommandGuard;
		work.add(new WorkItem(engine));
	}
	
	public long doWork() {
		try {
			WorkItem i = work.peek();
			if ( i == null ) return System.currentTimeMillis() + SLEEP_STEP;
			if ( i.nextTimeSlice > System.currentTimeMillis() ) return i.nextTimeSlice;
			Debug.trace("Doing work, Pool at %d items.", work.size());
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
