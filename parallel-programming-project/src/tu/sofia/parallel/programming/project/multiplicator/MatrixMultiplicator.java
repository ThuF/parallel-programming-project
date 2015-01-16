package tu.sofia.parallel.programming.project.multiplicator;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MatrixMultiplicator {

	private static final Logger logger = Logger.getLogger(MatrixMultiplicator.class.getName());

	public static int[][] multiplyInParallel(int[][] matrixA, int[][] matrixB)
			throws IllegalArgumentException {
		int columnsInA = matrixA[0].length;
		int rowsInB = matrixB.length;
		if (columnsInA != rowsInB) {
			throw new IllegalArgumentException("Error: Columns of Matrix1: " + columnsInA
					+ " did not match to rows of Matrix2: " + rowsInB + ".");
		}
		int[][] matrixProduct = multMatrixWithThreadsSync(matrixA, matrixB);
		return matrixProduct;
	}

	public static int[][] multiply(int[][] matrixA, int[][] matrixB) throws NumberFormatException {

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
					matrixC[i][j] = matrixC[i][j] + matrixA[i][k] * matrixB[k][j];
				}
			}
		}
		return matrixC;
	}

	private static int[][] multMatrixWithThreadsSync(int[][] matrixA, int[][] matrixB) {

		int[][] matrixProduct = new int[matrixA.length][matrixB[0].length];

		ConcurrentMatrixMultiplyingTask.ConcurrencyContext context = new ConcurrentMatrixMultiplyingTask.ConcurrencyContext(
				matrixProduct.length);

		Runnable task = new ConcurrentMatrixMultiplyingTask(context, matrixA, matrixB,
				matrixProduct);
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
				logger.log(Level.SEVERE, null, ex);
			}
		}
		return matrixProduct;
	}
}