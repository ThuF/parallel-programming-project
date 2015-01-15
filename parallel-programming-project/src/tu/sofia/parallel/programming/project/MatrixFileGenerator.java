package tu.sofia.parallel.programming.project;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class MatrixFileGenerator {
	
	public static void main(String[] args) throws IOException {
		int mn = 1000;
		int radix = 1000;
		int[][] a = new int[mn][mn];
		int[][] b = new int[mn][mn];
		fillMatrixWithRandomInts(a, radix);
		fillMatrixWithRandomInts(b, radix);
		printMatrixToFile(a, "a" + mn +"r"+ radix + ".txt");
		printMatrixToFile(b, "b" + mn +"r"+ radix + ".txt");
	}

	private static void printMatrixToFile(int[][] matrix, String fileName)
			throws IOException {
		PrintWriter userOutput = new PrintWriter(new FileWriter(fileName));
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				if (j == matrix[0].length -1){
					userOutput.print(matrix[i][j]);
				} else {
					userOutput.print(matrix[i][j] + " ");
				}
			}
			userOutput.println();
		}
		userOutput.close();
	}

	private static void fillMatrixWithRandomInts(int[][] matrix, int radix) {
		Random rand = new Random();

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				matrix[i][j] = rand.nextInt(radix) + 1;
			}
		}

	}
}
