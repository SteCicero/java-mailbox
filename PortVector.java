public class PortVector <T>
{
	SynchPort <T> [] vporte;
	public FairSemaphore PVectSem, mutexCounters;	
	int [] counters;				//numero di mittenti attuali per ogni porta
	int mitxport=5;					//numero di mittenti per porta
	int dim;						//dimensione array di porte(portvector)
	
	public PortVector(int dim,int mitxport)
	{
		this.dim=dim;
		counters=new int[dim];
		this.mitxport=mitxport;
		try
		{
			PVectSem=new FairSemaphore(0);			//semaforo di sincronizzazione mittente/ricevente
			mutexCounters=new FairSemaphore(1);		//semaforo di mutua esclusione dei contatori 
		}											//dei mittenti sulle porte
		catch(InterruptedException e){}
		vporte=new SynchPort[dim];
		for(int i=0;i<dim;i++)
		{
			counters[i]=0;
			vporte[i]=new SynchPort <T>(mitxport);
		}
	}
	
	private boolean AllChecked(boolean vett[],int n)
	{
		int count=0;
		for(int i=0;i<n;i++)
		{
			if(vett[i])
			{
				count++;
			}
		}
		if(count==n)
		{
			return true;
		}
		return false;
	}
	
	public void Send(String mit,T msg,int portNumber)
	{
		if(portNumber<0 && portNumber>(dim-1) && counters[portNumber]>(dim-1))
		{
			System.out.print("Troppi mittenti su una stessa porta, o porta non valida termino\n");
			return;	
		}
		mutexCounters.P();
		counters[portNumber]++;
		mutexCounters.V();
		PVectSem.V();
		vporte[portNumber].send(mit,msg);
		return;
	}
	
	public cdata Receive(int vett[],int n)
	{
		int randval;
		boolean [] checked=new boolean[n];
		for(int i=0;i<n;i++)			//segno tutte le porte come non controllate
		{
			checked[i]=false;
		}
		cdata<T> sample=new cdata<T>();
		System.out.print("TDown: Receive di PortVector\n");
		PVectSem.P();
		for(;;)
		{
			if(AllChecked(checked,n))		//tutte le porte sono state controllate?
			{
				for(int i=0;i<n;i++)
				{	
					checked[i]=false;
				}
				System.out.print("TDown Receive: non ci sono messaggi mi blocco\n");
				PVectSem.P();
			}
			randval=(int) (Math.random()*n);
			//System.out.print("Estratto a caso indice di vettore di porte: "+randval+" compreso tra 0 e "+(n-1)+"\n");
			if(!checked[randval])		//porta non controllata
			{		
				mutexCounters.P();
				if(counters[randval]>0)					//è presente un messaggio nella porta
				{
					sample=vporte[vett[randval]].receive();
					sample.numporta=vett[randval];
					counters[randval]--;
					mutexCounters.V();
					System.out.print("TDown Receive: E' presente un nuovo messaggio da parte di "+sample.mit+" sulla porta "+sample.numporta+" contenente: "+sample.msg+"\n");
					return sample;
				}
			}
			mutexCounters.V();
			checked[randval]=true;		//segno la porta come controllata
		}
	}
}