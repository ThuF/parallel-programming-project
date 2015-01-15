package com.cloud.pack;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MatrixMultiplicator {

	public static int[][] multiplyInParallel(int[][] matrixA, int[][] matrixB)
			throws IllegalArgumentException {
		int columnsInA = matrixA[0].length;
		int rowsInB = matrixB.length;
		if (columnsInA != rowsInB) {
			throw new IllegalArgumentException("Error: Columns of Matrix1: "
					+ columnsInA + " did not match to rows of Matrix2: "
					+ rowsInB + ".");
		}
		int[][] matrixProduct = multMatrixWithThreadsSync(matrixA, matrixB);
		return matrixProduct;
	}

	public static int[][] multiply(int[][] matrixA, int[][] matrixB)
			throws NumberFormatException {

		int rowsInA = matrixA.length;
		int columnsInA = matrixA[0].length;
		int rowsInB = matrixB.length;
		int columnsInB = matrixB[0].length;

		if (columnsInA != rowsInB) {
			throw new IllegalArgumentException("A:Rows: " + columnsInA
					+ " did not match B:Columns " + rowsInB + ".");
		}

		int[][] matrixC = new int[rowsInA][columnsInB];
		for (int i = 0; i < rowsInA; i++) {
			for (int j = 0; j < columnsInB; j++) {
				for (int k = 0; k < columnsInA; k++) {
					matrixC[i][j] = matrixC[i][j] + matrixA[i][k]
							* matrixB[k][j];
				}
			}
		}
		return matrixC;
	}

	private static int[][] multMatrixWithThreadsSync(int[][] matrixA,
			int[][] matrixB) {

		int[][] matrixProduct = new int[matrixA.length][matrixB[0].length];
		int[] matrixProductColumn = new int[matrixA.length];

		ConcurrentMatrixMultiplyingTask.ConcurrencyContext context = new ConcurrentMatrixMultiplyingTask.ConcurrencyContext(
				matrixProduct.length);

		Runnable task = new ConcurrentMatrixMultiplyingTask(context, matrixA,
				matrixB, matrixProduct);
		Thread[] workers = new Thread[5];
		for (int i = 0; i < workers.length; i++) {
			workers[i] = new Thread(task, "Worker-" + i);
		}
		for (int i = 0; i < workers.length; i++) {
			Thread worker = workers[i];
			worker.start();
		}
		for (int i = 0; i < workers.length; i++) {
			Thread worker = workers[i];
			try {
				worker.join();
			} catch (InterruptedException ex) {
				Logger.getLogger(MatrixMultiplicator.class.getName()).log(
						Level.SEVERE, null, ex);
			}
		}
		return matrixProduct;
	}

	public static boolean validDimension(int dim) {
		if (dim <= 0 || dim > 1000) {
			System.err.println("Dimension value entered is not valid");
			return false;
		}
		return true;

	}
}