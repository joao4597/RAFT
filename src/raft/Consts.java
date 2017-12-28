package raft;

public final class Consts  {
	
	//Server States
	public static final int FOLLOWER  = 0;
	public static final int LIDER     = 1;
	public static final int CANDIDATE = 2;

	private Consts(){
		//this prevents even the native class from 
		//calling this ctor as well :
		throw new AssertionError();
	}
}
