package torusworld.math;

public class MatrixMath
{
	//private static final int MAX_VAR = 20;   // This must be set to be the same for both this program and
	// the calling program.  If both programs are placed in one file,
	// a common global variable can be used.

	public boolean GaussianElim( double a[][],     // Left-hand-side matrix
	double b[],                // Right-hand-side column matrix
		double x[],                // Column matrix for solution
			int N_eqn )
	{
		int       N_rhs = 1;     // Number of right hand side vectors

		try {
			//   Start of executable statements
			double max_in_matrix = GetMax( a, N_eqn );   // Determine maximum absolute value in matrix

			AugmentMatrix( a, b, N_eqn, N_rhs );

			//   Reduction to upper triangular matrix starts here
			boolean singular;   // flag to detect singular matrix (has no solution)
			singular = false;
			for ( int pivot = 0; pivot <= N_eqn-1; pivot++ ) {
			    singular = swaprows( a, pivot, N_eqn, N_rhs, max_in_matrix);
				if ( singular )
					break;
				for ( int row = pivot + 1; row < N_eqn; row++ )
					for ( int column = pivot+1; column < N_eqn + N_rhs; column++ )
						a[row][column] -= a[row][pivot] * a[pivot][column]
							/ a[pivot][pivot];
			}

			//   Now do back substitution (unless a singluar matrix is found)
			if (singular == false) {
				for ( int rhs = N_eqn; rhs < N_eqn + N_rhs; rhs++ )
					for ( int row = N_eqn-1; row >=0; row-- ) {
						for ( int column = row+1; column < N_eqn; column++ )
							a[row][rhs] -= a[row][column] * a[column][rhs];

						a[row][rhs] /= a[row][row];
					}

				for ( int row = 0; row < N_eqn; row++ )
					x[row] = a[row][N_eqn];

			}


		return (singular? false : true);
  		}	  catch (Exception e) {
			return false;
		}
	}

	public void AugmentMatrix( double a[][], double b[], int N_eqn, int N_rhs ){
		//   Add right-hand side vectors as extra columns
		//   in origina a matrix

		for (int row=0; row<N_eqn; row++)
			for (int column=N_eqn; column<N_eqn+N_rhs; column++)
				a[row][column] = b[row];
	}

	public boolean swaprows( double a[][], int pivot, int N_eqn, int N_rhs, double max_in_matrix ){
		// Subroutine to swap pivot rows
		/*
		 Find row with maximum element in the pivot column and swap this row
		 with the current pivot row.  If the resulting pivot element is
		 smaller than some desired minimum, the matrix is assumed to be singular.
		 In this case the routine returns the value true as an error flag.  If there is
		 no error (the calculation can proceed), the function returns the value false.
		 */
		final double small = 1e-9;  // The matrix is considered to be a singular matrix if the
		// maximum element in the pivot column is less than "small"
		// times the maximum element in the array

		// Search for maximum element in pivot column starting with pivot row and
		// checking all rows from pivot row to last row.

		double max_element = Math.abs( a[pivot][pivot] );
		int max_row = pivot;
		for (int row = pivot+1; row < N_eqn; row++ )
			if (  Math.abs( a[row][pivot] ) > max_element)
			{
				max_row = row;
				max_element = Math.abs( a[row][pivot] );
			}

		// Maximum element found.  Check to see if this is essentially zero to within
		// specified tolerance for roundoff error.  If it is, return a true error flag.
		// If it is not, swap rows if required and return a error flag value of false.

		if ( max_element < small * max_in_matrix )
			return true;
		else
		{
			if ( max_row != pivot)
				for ( int column = pivot; column < N_eqn + N_rhs; column++)
				{
					double temp = a[max_row][column];
					a[max_row][column] = a[pivot][column];
					a[pivot][column] = temp;
				}
			return false;
		}
	}

	public double GetMax( double a[][], int N_eqn ) // function to get maximum array element
	{
		// Find matrix element with maximum absolute value
		double max_in_matrix = 0.0;
		for ( int row = 0; row < N_eqn; row++ )
			for ( int column = 0; column < N_eqn; column++ )
				if ( max_in_matrix < Math.abs( a[row][column] ) )
					max_in_matrix = Math.abs( a[row][column] );
		return max_in_matrix;
	}	

}