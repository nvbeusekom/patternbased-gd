/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patternbasedgraphdrawing;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class Matrix {

    String[] cols, rows;
    boolean[][] cells; // [col][row]

    int[] permutation = null;

    int n = 0;
    int m = 0;
    double moran = Double.NaN;
    double moran_diag = Double.NaN;

    double moran_perm = Double.NaN;
    double moran_perm_diag = Double.NaN;

    String col(int i, boolean perm) {
        if (perm && permutation != null) {
            return cols[permutation[i]];
        } else {
            return cols[i];
        }
    }

    String row(int i, boolean perm) {
        if (perm && permutation != null) {
            return rows[permutation[i]];
        } else {
            return rows[i];
        }
    }

    boolean cell(int col, int row, boolean perm) {
        if (perm && permutation != null) {
            return cells[permutation[col]][permutation[row]];
        } else {
            return cells[col][row];
        }
    }

    double moran(boolean perm) {
        if (perm && permutation != null) {
            if (Double.isNaN(moran_perm)) {
                moran_perm = compute(true, false);
            }
            return moran_perm;
        } else {
            if (Double.isNaN(moran)) {
                moran = compute(false, false);
            }
            return moran;
        }
    }

    double moran_diag(boolean perm) {
        if (perm && permutation != null) {
            if (Double.isNaN(moran_perm_diag)) {
                moran_perm_diag = compute(true, true);
            }
            return moran_perm_diag;
        } else {
            if (Double.isNaN(moran_diag)) {
                moran_diag = compute(false, true);
            }
            return moran_diag;
        }
    }

    double compute(boolean perm, boolean diags) {
        n = cols.length;
        m = 0;
        int B = 0;
        int W = 0;
        int B_diag = 0;
        int W_diag = 0;
        for (int c = 0; c < n; c++) {
            for (int r = 0; r < n; r++) {
                if (cell(c, r, perm)) {
                    m++;
                    if (c > 0) {
                        if (cell(c - 1, r, perm)) {
                            B++;
                        } else {
                        }
                    }
                    if (r > 0) {
                        if (cell(c, r - 1, perm)) {
                            B++;
                        } else {
                        }
                    }
                } else {
                    if (c > 0) {
                        if (cell(c - 1, r, perm)) {
                        } else {
                            W++;
                        }
                    }
                    if (r > 0) {
                        if (cell(c, r - 1, perm)) {
                        } else {
                            W++;
                        }
                    }
                }
                // diagonals                 
                if (c > 0 && r > 0) {
                    if (cell(c - 1, r - 1, perm) && cell(c, r, perm)) {
                        B_diag++;
                    } else if (!cell(c - 1, r - 1, perm) && !cell(c, r, perm)) {
                        W_diag++;
                    }

                    if (cell(c, r - 1, perm) && cell(c - 1, r, perm)) {
                        B_diag++;
                    } else if (!cell(c, r - 1, perm) && !cell(c - 1, r, perm)) {
                        W_diag++;
                    }
                }
            }
        }

        double dn = (double) n;
        double dm = (double) m;

        if (!diags) {
            double cB = dn / (2 * (dn - 1) * dm);
            double cW = dn / (2 * (dn - 1) * (dn * dn - dm));
            return (cB * B + cW * W - 1);
        } else {
            // total is 2n(n-1) + 2*2*(n-1)*(n-1)
            // 2n(n-1) + 4(n-1)(n-1)
            // 2n(n-1) + 4(n-1)(n-1)
            double cB_diag = dn * dn / (double) ((2 * dn * (dn - 1) + 4 * (dn - 1) * (dn - 1)) * dm);
            double cW_diag = dn * dn / (double) ((2 * dn * (dn - 1) + 4 * (dn - 1) * (dn - 1)) * (dn * dn - dm));
            return (cB_diag * (B + B_diag) + cW_diag * (W + W_diag) - 1);
        }
    }
    
}
