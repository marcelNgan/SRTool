void main()
{
	int i;
	i=0;
	// with default unwind depth of 5, unwinding assertion should fail
	while(i < 6)
	{
	 i = i + 1;
	}
	
}

