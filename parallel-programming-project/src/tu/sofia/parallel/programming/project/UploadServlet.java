package tu.sofia.parallel.programming.project;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

@WebServlet("/uploads")
public class UploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private int[][][] matrices;
	private int[][] result;
	private int nextIndex;

	@Override
	public void init() throws ServletException {
		resetMatricesArray();
		super.init();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/plain");
		response.setHeader("Content-Disposition", "attachment;filename=result-of-multiplication.txt");
		PrintWriter out = response.getWriter();
		response.setStatus(200);
		try {
			writeResult(out);

			resetMatricesArray();
			matrices[1] = result;
		} catch (IllegalArgumentException iae) {
			out.print(iae.getMessage());
		}
		out.flush();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		List<String> filesAsStrings = new ArrayList<String>();
		try {
			ServletFileUpload servletFileUpload = new ServletFileUpload(new DiskFileItemFactory());
			List<FileItem> items = servletFileUpload.parseRequest(request);
			for (FileItem item : items) {
				if (!item.isFormField()) {
					filesAsStrings.add(IOUtils.toString(item.getInputStream(), "UTF-8"));
				}
			}
		} catch (FileUploadException e) {
			throw new ServletException("Cannot parse multipart request.", e);
		}
		String message = null;
		try {
			parseAndSaveMatrix(filesAsStrings);
			response.setStatus(200);
		} catch (NumberFormatException e) {
			// bad request
			response.setStatus(400);
			message = "There is no matrix in the file or is not properly formated!";
		}

		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		out.print("{\"error\": \"" + message + "\"}");
		out.flush();
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		resetMatricesArray();
		response.setStatus(200);
	}

	private void parseAndSaveMatrix(List<String> filesAsStrings) throws NumberFormatException {
		for (int m = 0; m < filesAsStrings.size(); m++) {
			String[] rows = filesAsStrings.get(m).split("\n");
			String[] cols = rows[0].split(" ");
			final int rowsN = rows.length;
			final int colsN = cols.length;
			int[][] matrix = new int[rowsN][colsN];

			for (int r = 0; r < rows.length; r++) {
				String[] colsPerRow = rows[r].split(" ");
				// matrix is not consistent
				if (colsPerRow.length != colsN) {
					throw new NumberFormatException();
				}

				for (int c = 0; c < colsPerRow.length; c++) {
					matrix[r][c] = Integer.parseInt(colsPerRow[c].trim());
				}
			}
			addInMatricesArray(matrix);
		}
	}

	private void writeResult(PrintWriter out) throws IllegalArgumentException {
		out.print("Multiplication of 2 matrices: " + matrices[0].length + "x"
				+ matrices[0][0].length + " and " + matrices[1].length + "x"
				+ matrices[1][0].length);
		out.println();
		out.println();
		long startTime = System.currentTimeMillis();
		MatrixMultiplicator.multiplyInParallel(matrices[0], matrices[1]);
		long endTime = System.currentTimeMillis();
		out.print("Time for being matrices multiplied in parallel (with " + matrices[0][0].length
				+ " threads): " + (endTime - startTime) + " milliseconds");
		out.println();
		out.println();
		startTime = System.currentTimeMillis();
		result = MatrixMultiplicator.multiply(matrices[0], matrices[1]);
		endTime = System.currentTimeMillis();
		out.print("Time for being matrices multiplied with loop (in the main thread): "
				+ (endTime - startTime) + " milliseconds");

		out.println();
		out.println();
		out.print("Result of multiplication: ");
		out.println();
		printMatrixInWriter(result, out);
	}

	private static void printMatrixInWriter(int[][] matrix, PrintWriter writer) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				writer.write(matrix[i][j] + " ");
			}
			writer.println();
		}
	}

	private void addInMatricesArray(int[][] matrix) {
		matrices[nextIndex] = matrix;
		++nextIndex;
	}

	private void resetMatricesArray() {
		matrices = new int[2][][];
		nextIndex = 0;
	}
}
