import java.util.*;

class Coppia
{
	Thread id;
	boolean sveglia;
	Coppia(Thread id,boolean sveglia)
	{
		this.id=id;
		this.sveglia=sveglia;
	}
}

public class FairSemaphore
{
	public int contatore;
	private List listaDiAttesa=new LinkedList();
	
	public FairSemaphore(int contatore) throws InterruptedException
	{
		if(contatore<0) throw new InterruptedException();
		this.contatore=contatore;
	}
	
	public synchronized void P()
	{
		contatore--;
		if(contatore<0)			//se risorsa occupata inserisco thread in coda
		{
			listaDiAttesa.add(new Coppia(Thread.currentThread(),false));
			while(true)
			{
				try
				{
					wait();			//metto in wait il thread
				}
				catch(InterruptedException ignored){}
				Coppia c=(Coppia)listaDiAttesa.get(0);
				if(c.id==Thread.currentThread() && c.sveglia)		//controllo se il thread
				{													//deve essere svegliato
					listaDiAttesa.remove(0);
					if(listaDiAttesa.size()>0 && ((Coppia)listaDiAttesa.get(0)).sveglia)
					{
						notifyAll();
					}
					break;
				}
			}
		}
	}
	
	public synchronized void V()
	{
		if(listaDiAttesa.size()>0)						
		{
			int i;
			for(i=0;i<listaDiAttesa.size();i++)
			{
				if(!((Coppia)listaDiAttesa.get(i)).sveglia)
				{
					break;
				}
			}
			if(i<listaDiAttesa.size())
			{
				((Coppia)listaDiAttesa.get(i)).sveglia=true;
			}
			notifyAll();
		}
		contatore++;
	}
}
