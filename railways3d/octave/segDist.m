function D = segDist(O, P)
# O = m x n ( m punti)
# P = p x n (p punti)
	 [ m n ] = size(O);
	 p = size(P, 1);
	DP = zeros(m, p - 1) ;
	for i = 1 : p -1
		A = P( i , : );
		B = P( i + 1 , : );
		BA = B  - A; # 1 x n
		OA =  O - A; # m  x n
		PS = OA * BA'; # m x 1
		AH = PS / (BA * BA'); # m x 1
		OA2 = sum(OA .^2 , 2); # m x 1
		OB = O - B; # m x n
		OB2 = sum(OB .^2, 2); # m x 1
		OH2 = OA2 - PS .^ 2 / (BA * BA'); # m x 1
		X1 = OB2 .* (AH > 1);
		M2 = (AH >= 0) & (AH <= 1) ;
		X2 = OH2 .* M2;
		X3 = OA2 .* (AH < 0);
		DP( : , i) = X1 + X2 + X3;
	endfor
	if (p > 2)
		D = sqrt(min(DP')');
	else
		D = sqrt(DP);		
	endif
endfunction