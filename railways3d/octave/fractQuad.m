function Z = fractQuad(Z, i, j, st, f)

	mstep = st / 2;

	z00 = Z( i , j );
	z02 = Z( i , j + st );
	z20 = Z( i + st, j );
	z22 = Z( i + st, j + st);
	
	z01 = (z00 + z02) / 2 + f();
	z10 = (z00 + z20) / 2 + f();
	z12 = (z02 + z22) / 2 + f();
	z21 = (z20 + z22) / 2 + f();
	z11 = (z01 + z10 + z12 + z21) / 4 + f();

	Z(i, j + mstep) = z01;
	Z(i + mstep, j) = z10;
	Z(i + mstep, j + st) = z12;
	Z(i + st, j + mstep) = z21;
	Z(i + mstep, j + mstep) = z11;
	
endfunction