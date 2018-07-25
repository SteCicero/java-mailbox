import java.util.*;

class Buffer<T>
{
	int in,out,counter;
	FairSemaphore lockbuf,fullbuf,emptybuf;
	int size;
	cdata<T>[] vectbuf;
	
	public Buffer(int n)
	{
		in=0;
		out=0;
		counter=0;
		size=n;
		vectbuf=new cdata[size];
		for(int i=0;i<size;i++)
		{
			vectbuf[i]=new cdata<T>();
		}
		try
		{
			lockbuf=new FairSemaphore(1);
			fullbuf=new FairSemaphore(0);
			emptybuf=new FairSemaphore(0);
		}
		catch(InterruptedException e){}
	}
	
	public boolean CheckFreeSpace()
	{
		for(;;)
		{
			lockbuf.P();
			if(counter==size)
			{
				System.out.print("Buffer pieno\n");
				lockbuf.V();
				return false;
			}
			else
			{
				System.out.print("Spazio libero pari a "+(size-counter)+" unità\n");
				lockbuf.V();
				return true;
			}
		}
	}
	
	public void waitForElem()
	{
		for(;;)
		{
			lockbuf.P();
			if(counter==0)
			{
				System.out.print("Buffer vuoto, vado in sleep\n");
				lockbuf.V();
				emptybuf.P();
			}
			else
			{
				System.out.print("Ci sono "+counter+" elementi nel buffer, proseguo\n");
				lockbuf.V();
				return;
			}
		}
	}
	
	public void put(cdata<T> value)
	{
		lockbuf.P();
		System.out.print("Inserisco elemento "+value.msg+" nell'indice "+in+" del buffer\n");
		vectbuf[in]=value;
		in=(in+1)%size;
		counter++;
		if(counter==1)
		{
			System.out.print("Signal su emptybuffer\n");
			emptybuf.V();
		}
		lockbuf.V();
	}
	
	public cdata<T> get()
	{	
		lockbuf.P();
		System.out.print("Estraggo elemento dal buffer\n");
		cdata value;
		value=vectbuf[out];
		System.out.print("Estratto valore: "+value.msg+" da indice "+out+"\n");
		out=(out+1)%size;
		counter--;
		lockbuf.V();
		System.out.print("Signal su fullbuffer\n");
		fullbuf.V();
		return value;
	}
}


public class MailBox extends Thread
{
	FairSemaphore mutlist;						//semaforo di mutua esclusione della lista ausiliaria
	LinkedList<cdata> auxlist;					//lista ausiliaria usata in caso di buffer pieno
	public static PortVector <Integer> io;
	int[] rangeport;
	Buffer buffo;								//buffer
	public static int nporte;					//numero di porte
	public static int nmitt;					//numero di mittenti
	
	public MailBox(int nporte)
	{
		try
		{
			mutlist=new FairSemaphore(1);
		}
		catch(InterruptedException e){}
		nmitt=5;
		auxlist=new LinkedList<cdata>();	//inizializzo lista ausiliaria
		buffo=new Buffer(3);				//inizializzo buffer
		io=new PortVector <Integer>(nporte,nmitt);
		this.nporte=nporte;
		rangeport=new int[nporte];
		for(int i=0;i<nporte;i++)			//creo vettore con i numeri di synchport
		{
			rangeport[i]=i;
		}
	}
	
	public void run()
	{
		TDown td1=new TDown("TDown",io,rangeport,nporte,buffo,auxlist,mutlist);
		TUp tu1=new TUp("TUp",buffo,auxlist,nmitt,mutlist);
		td1.start();
		tu1.start();
	}
}

class TDown extends Thread					//thread che si occupa di ricevere i messaggi
{											//e inserirli nel buffer o nella lista
	FairSemaphore mutlist;
	LinkedList auxlist;
	int[] vettore;
	PortVector <Integer> io;
	int dimVet;
	Buffer <Integer>buffo;
	
	public TDown(String id,PortVector <Integer>io,int[] vettore,int dimVet,Buffer buffo,LinkedList auxlist,FairSemaphore mutlist)
	{
		super(id);
		this.io=io;
		this.vettore=vettore;
		this.dimVet=dimVet;
		this.buffo=buffo;
		this.mutlist=mutlist;
		this.auxlist=auxlist;
	}
	
	public void run()
	{
		cdata <Integer> dato; 
		for(;;)
		{
			dato=io.Receive(vettore,dimVet);
			mutlist.P();
			if(buffo.CheckFreeSpace() && auxlist.isEmpty()) //caso buffer non pieno e lista vuota
			{
				System.out.print("TDown: inserisco messaggio nel buffer\n");
				buffo.put(dato);							//inserisco nel buffer
			}
			else											//in tutti gli altri casi
			{
				System.out.print("TDown: inserisco messaggio nella lista ausiliaria\n");
				auxlist.add(dato);							//inserisco in lista
			}
			mutlist.V();
		}
	}
}

class TUp extends Thread							//thread che si occupa di estrarre i messaggi
{													//dal buffer e inviarli al destinatario
	public FairSemaphore mutlist;					//e trasferire i messaggi dalla lista al buffer
	Buffer buffo;
	String [] prmit;
	int nmitt;
	LinkedList auxlist;
	
	public TUp(String id,Buffer buffo,LinkedList auxlist,int nmitt,FairSemaphore mutlist)
	{
		super(id);
		this.buffo=buffo;
		this.prmit=new String[nmitt];
		this.nmitt=nmitt;
		this.auxlist=auxlist;
		this.mutlist=mutlist;
		for(int i=0;i<nmitt;i++)
		{
			prmit[i]="mit"+i;
		}
	}
	
	private boolean fromListToBuf()					//sposta un messaggio dalla lista al buffer
	{												//in base alla priorità
		cdata<Integer> temp=new cdata<Integer>();
		for(int i=0;i<nmitt;i++)
		{
			for (Iterator<cdata> eG = auxlist.iterator(); eG.hasNext();)
			{
				temp=eG.next();
				if(temp.mit.equals(prmit[i]))
				{
					//System.out.print("TUp: spostato messaggio: "+temp.msg+" del mittente: "+temp.mit+"\n");
					buffo.put(temp);
					eG.remove();
					return true;	
				}	
			}	
		}
		System.out.print("TUp: errore nello spostamento da lista a buffer\n");
		return false;
	}
	
	public void run()
	{
		int i=0;
		for(;;)
		{
			System.out.print("Processo TUp: ciclo numero "+i+"\n");
			i++;
			buffo.waitForElem();
			cdata<Integer> temp;
			temp=buffo.get();
			Ricevente.destport.send(temp.mit,temp.msg);
			mutlist.P();
			if(!auxlist.isEmpty() && buffo.CheckFreeSpace())
			{
				fromListToBuf();	
			}
			mutlist.V();
		}
		
	}
}