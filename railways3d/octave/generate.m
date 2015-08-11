P = [-627 0; 627 0];
scale = 4;
gap = 80;
transition = 200;
stepCount = 9;
landLevel = 0.5;
mapLevelCount = 3;

HM0 = himapgen(stepCount, landLevel);
[n m] = size(HM0);
P1 = P / scale + [n m] / 2;
M = range(distance(P1, n, m),gap / scale, transition / scale);
HM = HM0 .* M;
surfc(HM);

savemap(HM, "himap.png");
HM1 = HM + (0.5 - rand(size(HM))) * 0.3 / (2 * mapLevelCount - 1);
A=alphaMap(HM1, mapLevelCount);
savemap(A, "alphamap.png");
