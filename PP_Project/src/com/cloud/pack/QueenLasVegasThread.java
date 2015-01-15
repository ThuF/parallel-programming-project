package com.cloud.pack;
/**
 * Probabilistic generation of ONE solution to the N-queens problem.
 *
 * Computations are done within threads functioning as compute engines.
 * The thread class is an inner class of QueenLasVegas, so that its
 * code has complete access to all private parts of the thread class.
 *
 * To insure that each thread uses a _different_ sequence of random
 * numbers, each has its own generator, and that generator is seeded
 * by the product of System.currentTimeMillis() and this.hashCode():
 * the first generates chance seeds on successive runs, the second
 * generates chance seeds within the same run.
 *
 * The ComputeEngine class has a static int[] solution which receives
 * the reference to the first successful solution discovered by one
 * of the threads.  When this static data member becomes non-null, all
 * threads terminate processing.
 *
 * Author:  Timothy Rolfe
 *
 * Specimen run:
 * Number of queens:   1000
 * Number of threads:  4
 *
 * Valid solution found in 14203 msec.
 * Trial solutions checked:
 * Thread 0:  25082
 * Thread 1:  23848
 * Thread 2:  22419
 * Thread 3:  22908
 *    Total:  94257
 */
import java.util.Scanner;
import java.util.Random;
import java.io.*;

public class QueenLasVegasThread
{
/**
 * Main program allows for either command-line arguments or user dialog
 * to determine the number of queens and the number of parallel threads
 * to work on the problem.
 */
   public static void main ( String[] args )
   {
      Scanner console = new Scanner(System.in);
      int nQueens,
          thread, nThreads;
      ComputeEngine[] compute;
      int[]  solution;
      long   start;
      double elapsed;
      int    nTrials = 0;
      PrintWriter outFile = null;

      System.out.print ("Number of queens:   ");
      if ( args.length < 1 )
         nQueens = console.nextInt();
      else
      {  nQueens = Integer.parseInt(args[0]);
         System.out.println(nQueens);
      }

      // CRITICAL:  Tell the compute engines how many queens
      ComputeEngine.nQueens = nQueens;

      System.out.print ("Number of threads:  ");
      if ( args.length < 2 )
      {  nThreads = console.nextInt();
         console.nextLine(); // Discard the trailing '\n'
      }
      else
      {  nThreads = Integer.parseInt(args[1]);
         System.out.println(nThreads);
      }
      System.out.println();

      // Timed computation of ONE solution:

      start = System.currentTimeMillis();
      compute = new ComputeEngine[nThreads];
      // Create the thread objects
      for ( thread = 0; thread < nThreads; thread++ )
         compute[thread] = new ComputeEngine();
      // Start all threads at NEARLY the same time
      for ( thread = 0; thread < nThreads; thread++ )
         compute[thread].start();
      // Wait for all threads to terminate
      try
      {  for ( thread = 0; thread < nThreads; thread++ )
            compute[thread].join();
      }
      catch ( Exception e )
      {  System.out.println(e);  System.exit(-1);  }

      elapsed = System.currentTimeMillis() - start;
      solution = ComputeEngine.solution;

      if ( valid(solution, nQueens) )
         System.out.println("Valid solution found in " +
            elapsed + " msec.");
      else
         System.out.println("ERROR --- invalid solution took " +
            (System.currentTimeMillis() - start) + " msec.");

      System.out.println("Trial solutions checked:  ");
      for ( thread = 0; thread < nThreads; thread++ )
      {  System.out.printf ("Thread %d:  %d\n",
            thread, compute[thread].nTrials);
         nTrials += compute[thread].nTrials;
      }
      System.out.printf ("   Total:  %d;  ratios:  %3.3f  %3.3f\n",
         nTrials, (nTrials/elapsed), (elapsed/nTrials) );

      try
      {
      // PrintWriter second argument:  autoflush
      //  FileWriter second argument:  append
         outFile = new PrintWriter(new FileWriter(
            new File("QLV.csv"), true), true);
      }
      catch (Exception e)
      {  System.err.println ("File open failed for QLV.csv\n" + e);
         System.exit(-1);
      }
//    nQueens,nThreads,nTrials,elapsed,ratio 1,ratio 2
      outFile.printf ("%d,%d,%d,%1.0f,%3.3f,%3.3f\n",
         nQueens,nThreads,nTrials,elapsed,
         (nTrials/elapsed),(elapsed/nTrials) );
   }

// Verify (or reject) the validity of the board of indicated size.
   static public boolean valid( int[] board, int size )
   {
      int row, nxt, k;
      // Take advantage of initialization to false
      boolean[] diagChk = new boolean[2*size-1],
                antiChk = new boolean[2*size-1];

//    mark (0, board[0], true);
      diagChk[0-board[0]+size-1] = true;
      antiChk[0+board[0]] = true;

      for ( row = 1; row < size; row++ )
         if ( (diagChk[row-board[row]+size-1]||antiChk[row+board[row]]) )
            return false;
         else
//          mark (row, board[row], true);
            diagChk[row-board[row]+size-1] =
               antiChk[row+board[row]] = true;
      return true;
   }

   /**
    * The class of compute engines to find a solution.
    */
   private static class ComputeEngine extends Thread
   {
      static int[] solution = null;
      static int   nQueens;       // This is set by the main
      Random generator = new Random
         ( this.hashCode() * System.currentTimeMillis() );
      int nTrials = 1;  // I.e., first trial is outside the while loop

   // The big kahuna --- all the work is done here
      public void run()
      {  int[] trial = new int[nQueens];
         int   k;

         for ( k = 0; k < nQueens; k++ )
            trial[k] = k;

         while ( ! ( solution != null || build(trial, nQueens) ) )
            nTrials++;

         if ( solution == null )
            solution = trial;
      }

   // Las Vegas algorithm to build a POSSIBLY valid board for the
   // N-queens problem:  random permutation, and then validation
   // or NOT (hence the boolean value returned --- true only if
   // the board discovered is a valid solution).
      boolean build( int[] board, int size )
      {
         int row, nxt, k;
         // Take advantage of initialization to false
         boolean[] diagChk = new boolean[2*size-1],
                   antiChk = new boolean[2*size-1];

         shuffleArray ( board, size );

   //    mark (0, board[0], true);
         diagChk[0-board[0]+size-1] = true;
         antiChk[0+board[0]] = true;

         for ( row = 1; row < size; row++ )
         {
            nxt = row + 1;
         // If the current row is invalid, try swapping with succeeding
         // rows until there is a valid one --- or NONE work.
            while ( (diagChk[row-board[row]+size-1] ||
                     antiChk[row+board[row]]) )
            {
               if (nxt == size)
                  return false;      // Failed to find a good replacement
               swap ( board, row, nxt++ );
            }
   //       mark (row, board[row], true);
            diagChk[row-board[row]+size-1] = true;
            antiChk[row+board[row]] = true;
         }
         return true;
      }

      static void swap ( int[] x, int p, int q )
      {  int temp = x[p];  x[p] = x[q];  x[q] = temp;  }

   /**
    * Shuffle the entire array, using the class scope generator
    *
    * See Rolfe, Timothy.  "Algorithm Alley:  Randomized Shuffling",
    * Dr. Dobb’s Journal, Vol. 25, No. 1 (January 2000), pp. 113-14.
    */
      void shuffleArray ( int [] x, int lim )    // NOT static; uses generator
      {  while ( lim > 1 )
         {  int item;
            int save = x[lim-1];
            item = generator.nextInt(lim);
            x[--lim] = x[item];             // Note predecrement on lim
            x[item] = save;
         } // end while
      } // end shuffleArray()
   } // end class ComputeEngine
} // end class QueenLasVegasThread