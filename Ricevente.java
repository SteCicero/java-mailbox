class Ricevente extends Thread
{
	public static SynchPort destport;
	
	public Ricevente(String id)
	{
		super(id);
		destport=new SynchPort<Integer>(1);
	}
	
	public void run()
	{
		cdata<Integer> sample=new cdata<Integer>();
		
		for(int i=0;i<20;i++)
		{
			sample=destport.receive();
			System.out.print("------>Ricevuto da mittente "+sample.mit+" contenuto: "+sample.msg+"\n");
		}
	}
}
