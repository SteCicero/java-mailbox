class Mittente extends Thread
{
	private int ritcoef;
	int randval;
	
	public Mittente(String id, int n)
	{
		super(id);
		ritcoef=n;
	}
	
	public void run()
	{	
		for(int i=0;i<4;i++)
		{
			randval=(int) (Math.random()*MailBox.nporte);
			MailBox.io.Send(Thread.currentThread().getName(),((ritcoef*10)+i),randval);
		}
	}
}