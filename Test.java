public class Test
{
	public static void main(String[] args)
	{	
		int N=5;	
		MailBox mb=new MailBox(N);
		Mittente m0=new Mittente("mit0",0);
		Mittente m1=new Mittente("mit1",1);
		Mittente m2=new Mittente("mit2",2);
		Mittente m3=new Mittente("mit3",3);
		Mittente m4=new Mittente("mit4",4);
		
		Ricevente r1=new Ricevente("r1");

		mb.start();
		m0.start();
		m1.start();
		m2.start();
		m3.start();
		m4.start();
		r1.start();
	}
}