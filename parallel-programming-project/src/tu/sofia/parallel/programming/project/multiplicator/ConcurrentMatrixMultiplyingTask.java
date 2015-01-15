package tu.sofia.parallel.programming.project.multiplicator;

public class ConcurrentMatrixMultiplyingTask implements Runnable {

    private int[][] matrixA;
    private int[][] matrixB;
    private int[][] matrixProduct;
    //
    private final ConcurrencyContext context;

    public ConcurrentMatrixMultiplyingTask(ConcurrencyContext context, int[][] A, int[][] B, int[][] C) {
        if (context == null) {
            throw new IllegalArgumentException("context can not be null");
        }
        this.context = context;
        this.matrixA = A;
        this.matrixB = B;
        this.matrixProduct = C;
    }

    @Override
    public void run() {
        while (true) {
            int row;
            synchronized (context) {
                if (context.isFullyProcessed()) {
                    break;
                }
                row = context.nextRowNum();
            }
            System.out.println(Thread.currentThread().getName() + " is going to process row " + row);
            // i'm not really sure if this matrix algorithm here is right, idk..
            for (int j = 0; j < matrixB[0].length; j++) {
                for (int k = 0; k < matrixA[0].length; k++) {
                    matrixProduct[row][j] += matrixA[row][k] * matrixB[k][j];
                }
            }
        }
    }

    public static class ConcurrencyContext {

        private final int rowCount;
        private int nextRow = 0;

        public ConcurrencyContext(int rowCount) {
            this.rowCount = rowCount;
        }

        public synchronized int nextRowNum() {
            if (isFullyProcessed()) {
                throw new IllegalStateException("Already fully processed");
            }
            return nextRow++;
        }

        public synchronized boolean isFullyProcessed() {
            return nextRow == rowCount;
        }
    }
}
