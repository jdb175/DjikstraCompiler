package dijkstra.fun;

public class funnin {
	public static boolean[] a;
	public static int b;
	
	public static int fun (int a) {
		if(a < 5) {
			return fun(a+1);
		} else {
			return a;
		}
	}
	
	public int fun2 () {
		return fun(3);
	}
}
