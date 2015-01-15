package tu.sofia.parallel.programming.project.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/matrix-generator")
public class MatrixFileGeneratorServlet extends HttpServlet {
	
	private static final long serialVersionUID = -7543646889733002232L;

	private static final int DEFAULT_VALUE = 100;
	private static final int DEFAULT_VALUE_RADIX = 100;
	private static final int DEFAULT_VALUE_SIZE = 100;

	private enum Parameters {
		SIZE, RADIX
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		int size = getParameter(request, Parameters.SIZE);
		int radix = getParameter(request, Parameters.RADIX);

		int[][] matrix = new int[size][size];
		fillMatrixWithRandomInts(matrix, radix);
		
		printMatrix(response, matrix);
	}
	
	private int getParameter(HttpServletRequest request, Parameters parameter) {
		int result = DEFAULT_VALUE;
		String parameterValue = request.getParameter(parameter.toString().toLowerCase());
		switch (parameter) {
		case SIZE:
			result = getValue(parameterValue, DEFAULT_VALUE_SIZE);
			break;
		case RADIX:
			result = getValue(parameterValue, DEFAULT_VALUE_RADIX);
			break;
		}
		return result;
	}

	private int getValue(String value, int defaultValue) {
		int result;
		try{
			if (value != null) {
				result = Integer.parseInt(value);
			} else {
				result = defaultValue;
			}
		} catch (Exception e) {
			result = defaultValue;
		}
		return result;
	}
	
	private static void printMatrix(HttpServletResponse response, int[][] matrix)
			throws IOException {
		response.setContentType("text/plain");
		response.setHeader("Content-Disposition", "attachment;filename=generated-matrix.txt");
		PrintWriter out = response.getWriter();

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				if (j == matrix[0].length - 1) {
					out.print(matrix[i][j]);
				} else {
					out.print(matrix[i][j] + " ");
				}
			}
			out.println();
		}
		out.close();
		out.flush();
	}

	private static void fillMatrixWithRandomInts(int[][] matrix, int radix) {
		Random rand = new Random();

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				matrix[i][j] = rand.nextInt(radix);
			}
		}

	}
}
