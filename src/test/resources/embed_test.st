
FUNCTION_BLOCK FB
VAR_INPUT
	X : UINT;
END_VAR
VAR_OUTPUT
	Y : UINT;
END_VAR
	Y := X+1;
	Y := X+5;
	Y := X+4;
END_FUNCTION_BLOCK

FUNCTION_BLOCK PGRM
VAR
	INST_FB : FB;
END_VAR
	INST_FB.X := 5;
	INST_FB();
END_FUNCTION_BLOCK