public class SynchPort<T>
{
	public FairSemaphore mutex,R;
	FairSemaphore [] M;
	public cdata<T> [] bufport;
	int nmitt,primo,ultimo;
	public int counter;
	
	public SynchPort(int nmitt)
	{
		counter=0;
		primo=0;
		ultimo=0;
		bufport=new cdata[nmitt];
		this.nmitt=nmitt;
		M=new FairSemaphore[nmitt];
		for(int i=0;i<nmitt;i++)
		{
			bufport[i]=new cdata<T>();
			try
			{
				M[i]=new FairSemaphore(0);
			}
			catch(InterruptedException e){}
		}
		try
		{
			R=new FairSemaphore(0);
			mutex=new FairSemaphore(1);
		}
		catch(InterruptedException e){}
	}
	
	public void send(String mit,T msg)
	{
		System.out.print(mit+": send\n");
		int i;
		cdata<T> message = new cdata<T>();
		message.mit=mit;
		message.msg=msg;
		mutex.P();
		if(!(counter<nmitt))
		{
			System.out.print(mit+": Troppe connessioni sulla porta, send abortita\n");
			mutex.V();
			return;
		}
		i=ultimo;
		ultimo=(ultimo+1)%nmitt;
		bufport[i]=message;
		counter++;
		mutex.V();
		R.V();
		M[i].P();
	}
	
	public cdata receive()
	{
		System.out.print(Thread.currentThread().getName()+": receive\n");
		int i;
		cdata<T> message = new cdata<T>();
		R.P();
		i=primo;
		primo=(primo+1)%nmitt;
		message=bufport[i];
		M[i].V();
		mutex.P();
		counter--;
		mutex.V();
		return message;
	}
}