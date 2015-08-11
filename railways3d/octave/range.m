function Y=range(x, dist = 0, gap = 1)
	Y = min(max((x-dist)/gap, 0),1);
endfunction