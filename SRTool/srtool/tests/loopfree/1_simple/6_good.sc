void main(int i, int j, int p)
{
	i=64;
	j=3<<1;
	p = (i >> j);
	assert(p);
}
