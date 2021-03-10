import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

import javax.print.attribute.IntegerSyntax;
public class Puzzel_Tile{
	
	//based on Manhattan distance
	//estimates the cost between given state to goal state.
	//sums the distance from each block that not in his place to his right place while taking the block color to the calculation. 
	public static int h(Matrix matrix) {
		int d = 0;
		for (int i = 0; i < matrix.matrix.length; i++) {
			for (int j = 0; j < matrix.matrix[0].length; j++) {
				if(matrix.matrix[i][j] != matrix.ans[i][j] && matrix.matrix[i][j] != -1 ) {
					int sumx = 0;
					int sumy = 0;
					for (int k = 0; k < matrix.matrix.length; k++) {
						for (int k2 = 0; k2 < matrix.matrix[0].length; k2++) {
							if(matrix.matrix[k][k2] == matrix.ans[i][j]) {
								sumx = k+1;
								sumy = k2+1;
							}
						}
					}
					int cost =1;
					if(matrix.colors.get(matrix.matrix[i][j]).equals("Red")) {
						cost=30;
					}
					d += (Math.abs(sumx-(i+1)) + Math.abs(sumy-(j+1))) * cost;
				}
			}
		}
		return d;
	}
	
	//BFS:
	//Passes On an entire floor until reach the solution
	//Time complexity:O(branch factor^Depth)
	//Space complexity:O(branch factor^Depth)
	public static void bfs(Matrix matrix, boolean WithOpen) {
		Hashtable<Integer, Matrix> h = new Hashtable<Integer, Matrix>(); 
		Hashtable<Integer, Matrix> openList = new Hashtable<Integer, Matrix>(); 
		Queue<Matrix> queue = new LinkedList<Matrix>(); 
		queue.add(matrix);
		int i=0,o=0,out=0;
		openList.put(o++, matrix);
		int num=1;
		while (queue.size() != 0){ 
			if(WithOpen) {
			System.out.println(openList.toString());
			}
			Matrix m=queue.poll();
			openList.remove(out++);			
			h.put(i++, m);
			String[] steps= {"L","U","R","D"};
			for (int j = 0; j <steps.length; j++) {
				Matrix mat=step(m,steps[j]);
				if(mat!=null) {
					num++;
				}
				if(mat!=null && !queue.contains(mat) && !h.contains(mat) && !openList.contains(mat)) {
					if(mat.victoryOrNot()) {
						mat.findf(mat).updatef(num, mat.cost, mat.path().substring(1));
						return;
					}else {
						openList.put(o++, mat);
						queue.add(mat);
					}
				}
			}
		}
	}

	//Limited_DFS
	//DFS(depth-first search): generates next a child of the deepest node that has not been completely expanded yet until the solution is found.
	//based on DFS with limit depth.
	//Time complexity:O(branch factor^Limit)
	//Space complexity: O(branch factor*Limit) 
	public static String Limited_DFS(Matrix matrix, int limit, Hashtable<Integer, Matrix> h, boolean WithOpen, Hashtable<Matrix, Matrix> openList) {
		if(matrix.victoryOrNot()) {	
			matrix.findf(matrix).updatef(num2, matrix.cost, matrix.path().substring(1));
			return matrix.path().substring(1);
		}else if(limit == 0) {
			openList.put(matrix, matrix);
			return "cutoff";
		}else {
			h.put(limit, matrix);
			boolean isCutoff = false;
			String[] steps= {"L","U","R","D"};
			for (int j = 0; j <steps.length; j++) {
				Matrix mat=step(matrix,steps[j]);
					if(mat!=null && !h.contains(mat)) {
						num2++;//count nodes
						String result = Limited_DFS(mat, limit - 1, h, WithOpen,openList);
						if(result == "cutoff") {
							isCutoff = true;
						}else if(result != "fail") {
							return result;
						}
					}
			}
			h.remove(limit);
			if(isCutoff == true) {
				return "cutoff";
			}else {
				return "fail";
			}
		}
	}
	
	//DFID
	//Calls Limited_DFS for each depth until find the goal depth.
	public static void DFID(Matrix matrix, boolean WithOpen) {
		Hashtable<Matrix, Matrix> openList = new Hashtable<Matrix, Matrix>();
		openList.put(matrix, matrix);
		if(WithOpen) {
			System.out.println(openList.values().toString());
		}
		int depth = 1;
		while(depth!=Integer.MAX_VALUE) {
			Hashtable<Integer, Matrix> h = new Hashtable<Integer, Matrix>();
			openList = new Hashtable<Matrix, Matrix>();
			String result = Limited_DFS(matrix, depth, h, WithOpen,openList);
			if(WithOpen) {
				System.out.println(openList.values().toString());
			}
			if(result != null && result != "cutoff" && result != "fail") {
				return ;
			}
			else if(result == "fail") {
				return;
			}
			depth++;
		}
		return;
	}
	
	//A*
	//Combines greedy search and uniform cost search approaches (both based on BFS).
	//greedy search (Pure Heuristic Search): "calculate" with the Heuristic Search the cost from Given state to goal.
	//uniform cost search: calculate the cost to given state.
	//the combination is to calculate the cost to given state and with the Heuristic Search for the given state to goal, 
	//Combines the calculations to choose wich way you preferred.
	//Time complexity:O(branch factor*Depth)(best case)
	//Space complexity: exponential 
	public static String AStar(Matrix matrix, boolean WithOpen){
		Hashtable<Integer, Matrix> h = new Hashtable<Integer, Matrix>();
		Hashtable<Matrix, Matrix> openList = new Hashtable<Matrix, Matrix>();
		PriorityQueue<Matrix> queue = new PriorityQueue<Matrix>(new Comparator<Matrix>() {public int compare(Matrix m1, Matrix m2) {return findFutureCost(m1)-findFutureCost(m2);}});
		queue.add(matrix); 
		openList.put(matrix, matrix);
		int i = 0;
		int num = 1;
		while(queue.size() != 0) {
			if(WithOpen) {
				System.out.println(openList.values().toString());
			}
			Matrix m=queue.poll(); 
			openList.remove(m);
			h.put(i++, m);
			if(m.victoryOrNot()) {
				m.findf(m).updatef(num, m.cost, m.path().substring(1));
				return "";
			}
			String[] steps= {"L","U","R","D"};
			for (int j = 0; j <steps.length; j++) {
				Matrix mat=step(m,steps[j]);
				if(mat!=null) {
					num++;
				}
				if(mat!=null && !queue.contains(mat) && !h.contains(mat)) {
					queue.add(mat);
					openList.put(mat, mat);
				}else {
					if(queue.contains(mat)) {
						for (Matrix key : openList.keySet()) {
							if(key.equals(mat) && findFutureCost(mat) > findFutureCost(key)) {								
								openList.remove(key);
								queue.remove(key);
								queue.add(mat);
								openList.put(mat, mat);
							}else {
								continue;
							}
						}
					}
				
				}
			}
		}
		return "no path";
	}
	
	//IDA*
	//based on DFS(depth-first search) algoritem
	//evaluate state like A*.
	//Space complexity: Linear 
	public static void IDA(Matrix matrix, boolean WithOpen) {
		Hashtable<Matrix, String> h = new Hashtable<Matrix, String>();
		Stack<Matrix> stack = new Stack<Matrix>();
		int t = h(matrix);
		while(t != Integer.MAX_VALUE) {
			int num = 1;
			int minFutureCost = Integer.MAX_VALUE;
			stack.add(matrix);
			h.put(matrix, "");
			while(stack.size()!=0) {
				if(WithOpen) {
					System.out.println(stack.toString());
				}
				Matrix m = stack.pop();
				if(h.get(m).equals("out")) {
					h.remove(m);
				}
				else {
					h.remove(m);
					h.put(m, "out");
					stack.add(m);
					String[] steps= {"L","U","R","D"};
					for (int j = 0; j <steps.length; j++) {
						Matrix mat=step(m,steps[j]);
						if(mat!= null) {
							num++;
							int matFutureCost=findFutureCost(mat);
							if(matFutureCost > t) {
								minFutureCost = Math.min(minFutureCost, matFutureCost);
								continue;
							}
							if(h.contains(mat)){
								if(h.get(mat).equals("out")) {
									continue;
								}
								else {

									for (Matrix key : h.keySet()) {
										if(key.equals(mat) && findFutureCost(key) > matFutureCost) {								
											stack.remove(key);
											h.remove(key);
										}else {
											continue;
										}
									}
								}
							}
							if(mat.victoryOrNot()) {
								mat.findf(mat).updatef(num, mat.cost, mat.path().substring(1));
								return;	
							}
							stack.add(mat);
							h.put(mat, "");
						}
						}
					}
				}
			t = minFutureCost;
		}
	}
	
		public static int findFutureCost(Matrix matrix) {//f()
			return matrix.cost + h(matrix);//cost +future cost
		}
	public static Matrix step(Matrix m1,String step){//String[] steps= {"L","U","R","D"};
		 Matrix m2=null;
		 if(step=="D") {//empty block up (ie-1,je)
				if (m1.checkIfBlackOrNullPtr(m1.ie-1,m1.je)) {//check if can move and if he dosen't change to the father						
					m2 = new Matrix(m1);
					m2.move(m1.ie-1,m1.je,step);
				}
			}
			else if(step=="U") {//empty block down (ie+1,je)
				if(m1.checkIfBlackOrNullPtr(m1.ie+1,m1.je)) {						
					m2 = new Matrix(m1);
					m2.move(m1.ie+1,m1.je,step);
				}
			}
			else if(step=="R") {//empty block left (ie,je-1)
				if(m1.checkIfBlackOrNullPtr(m1.ie,m1.je-1)) {					
					m2 = new Matrix(m1);
					m2.move(m1.ie,m1.je-1,step);
				}
			}
			else if(step=="L") {//empty block right (ie,je+1}
				if(m1.checkIfBlackOrNullPtr(m1.ie,m1.je+1)) {					
					m2=new Matrix(m1);
					m2.move(m1.ie,m1.je+1,step);
				}
			}
		 return m2;
	 }
	
public static class Matrix{
	public Hashtable<Integer, String> colors = new Hashtable<Integer, String>(); 
	public int[][] matrix;
	public int[][] ans;
	public String lastmove;//
	String path =null;
	int cost;
	Matrix f;
	int num=0;
	public int ie,je;
	public Matrix(Matrix mat){
		this.num=1;
		lastmove="";
		 f=mat;
		 this.ie=mat.ie;
		 this.je=mat.je;
		 this.matrix=new int[mat.matrix.length][mat.matrix[0].length];
		 this.ans=new int[mat.matrix.length][mat.matrix[0].length];
		 cost=mat.cost;
		 for (int i = 0; i < mat.matrix.length; i++) {
			for (int j = 0; j < mat.matrix[0].length; j++) {
				this.matrix[i][j]=mat.matrix[i][j];
				this.ans[i][j]=mat.ans[i][j];
				if(mat.matrix[i][j]!=-1) {
				colors.put(mat.matrix[i][j], mat.colors.get(mat.matrix[i][j]));
				}
			}
		}
	 }
	 public Matrix(int[][] matrix,Hashtable c){
		 this.num=1;
		 lastmove="";
		 f=null;
		 this.matrix=new int[matrix.length][matrix[0].length];
		 this.ans=new int[matrix.length][matrix[0].length];
		 cost=0;
		 int num=1;
		 for (int i = 0; i < matrix.length; i++) {//build the matrix, tha answer matrix and copy the hash table of the colors
			for (int j = 0; j < matrix[0].length; j++) {
				this.matrix[i][j]=matrix[i][j];
				this.ans[i][j]=num;
				num++;
				if (c.get(matrix[i][j])=="Black" ) {
					colors.put(matrix[i][j],"Black");
				}else if( c.get(matrix[i][j])=="Red"){
					colors.put(matrix[i][j],"Red");
				}
				else if(matrix[i][j]==-1){
					ie=i;
					je=j;
				}
				else {
					colors.put(matrix[i][j],"Green");
				}
			}
		}
		 ans[ans.length-1][ans[0].length-1]=-1;
	 }
	 public static Matrix findf(Matrix m) {//find the root
		 if(m.f == null) {return m;}
		 return findf(m.f);
	 }
	 public void updatef(int num,int cost,String path) {//update this values (use it to update the root for the output)
		 this.num=num;
		 this.cost=cost;
		 this.path =path;
	 }
	 public String path() {//build the path
		 if(this.f == null) {return "";}
			 return this.f.path()+"-" +this.f.matrix[this.ie][this.je] + this.lastmove ;
	 }
	 public boolean checkIfBlackOrNullPtr(int i,int j) {//check if it posible to move the empty block to matrix[i][j] and if its not going back to the father
		 if(i<0 ||i>this.matrix.length-1|| j<0 ||j>this.matrix[0].length-1  || this.colors.get(this.matrix[i][j])==("Black") || (this.f!=null && this.f.ie==i && this.f.je==j)) {
			 return false;
		 }
		 return true;
	 }
	 public void move(int i,int j,String side){//move the empty block to this position, update the empty block position ,the cost and keep this move for the path
		 if(this.colors.get(this.matrix[i][j])==("Red")) {
			 this.cost+=29;
		}
		 this.lastmove=side;
		 this.cost++;
		 int temp =this.matrix[i][j];	
		 this.matrix[i][j]=this.matrix[ie][je];
		 this.matrix[ie][je]=temp;
		 this.ie=i;
		 this.je=j;
	 }
	 public boolean victoryOrNot() {//check if it is the goel 
		 for (int i = 0; i < this.matrix.length; i++) {
			for (int j = 0; j < this.matrix[0].length; j++) {
				if(this.matrix[i][j]!= this.ans[i][j]) {
					return false;
				}
			}
		}
		return true;
	 }
	 public String printMatrix() {//print the matrix 
		 String ans = "";
		 for (int i = 0; i < this.matrix.length; i++) {
			 String temp = "";
			for (int j = 0; j < this.matrix[0].length; j++) {
				if(this.matrix[i][j]==-1) {
					temp+="_"+",";
				}else {
				temp+=this.matrix[i][j]+",";
				}
			}
			temp=temp.substring(0, temp.length()-1);
			ans+=temp+"\n";
		}
		 return ans;
	 }
	 @Override
	 public String toString() {//Override to string for Matrix
			String ans = "\n";
			ans+=this.printMatrix();
			return ans;
		}
	 
	 @Override
	    public boolean equals(Object object) { //Override to equals for Matrix
		 	if (!(object instanceof Matrix)) {return false;}
			else  if (object == this) {return true;} 
			else{
				Matrix temp = (Matrix) object;
				for (int i = 0; i < this.ans.length; i++) {
					for (int j = 0; j < this.ans[0].length; j++) {
						if(temp.matrix[i][j] != this.matrix[i][j] || temp.ans[i][j] != this.ans[i][j]) {
							return false;
						}
					}
				}
			}
			return true;
		} 
}
//get input and organize it on objact
//when it read the file their  is if for every line except the matrix line
public static class tilePuzzel{
	public Matrix matrix;
	String algorithm;
	boolean time;
	boolean WithOpen;
	public Hashtable<Integer, String> Colors = new Hashtable<Integer, String>();
	public tilePuzzel(String path) throws IOException{
		long startTime3 = System.nanoTime();
		int sizei=0, sizej = 0;
		int[] Black = null;
		int[] Red= null;
		String[] m = null;
	File file = new File(path); 
		BufferedReader br = new BufferedReader(new FileReader(file)); 
		 String st; 
		 int counter=1;
		 int countmatrix=0;
		  while ((st = br.readLine()) != null) {
		    if(counter==1) {algorithm=st;}
		    else if(counter==2) {if( st.equalsIgnoreCase("no time")) {time=false;}else {time=true;}}
		    else if(counter==3 ) {if( st.equalsIgnoreCase("no open")) {WithOpen=false;}else {WithOpen=true;}}
		    else if(counter==4) {
		    	int x=st.indexOf('x');
		    	sizei=Integer.parseInt(st.substring(0, x));
		    	sizej=Integer.parseInt(st.substring(x+1));
		    	m=new String[sizej];
		    }
		    else if(counter==5) {
		    	st=st.substring(6);
		    		if(st.length()==0||st.equalsIgnoreCase(" ") || st.equalsIgnoreCase(null)) {
		    			Black =new int[1];
		    			Black[0]=0;
		    		}
		    	else{
		    		st=st.substring(1);	
		    		String[] temp=st.split(",");				    	
		    		Black=new int[temp.length];
		    		for (int i = 0; i < temp.length; i++) {
		    			Black[i]=Integer.parseInt(temp[i]);
		    			Colors.put(Integer.parseInt(temp[i]), "Black");   			
					}
		    	}
		    }
		    else if(counter==6) {
		    	st=st.substring(4);
	    		if(st.length()==0||st.equalsIgnoreCase(" ") || st.equalsIgnoreCase(null)) {
	    			Red =new int[1];
	    			Red[0]=0; 
	    		}
	    	else{
	    		st=st.substring(1);	    	
	    		String[] temp=st.split(",");				    	
	    		Red=new int[temp.length];
	    		for (int i = 0; i < temp.length; i++) {
	    			Red[i]=Integer.parseInt(temp[i]);
	    			Colors.put(Integer.parseInt(temp[i]), "Red");  
				}
	    	}
            }
		    else {//evray line in the matrix is a string in m
		    	m[countmatrix]=st;
		    	countmatrix++;
		    }
		    counter++;
		  }
		  int[][] temp=new int[sizei][sizej];//build matrix for matrix constructor
		  for (int i = 0; i < temp.length; i++) {
			String[] s=m[i].split(",");
			for (int j = 0; j < temp[0].length; j++) {
				if(s[j].equalsIgnoreCase("_")) {temp[i][j]=-1;}
				else{temp[i][j]=Integer.parseInt(s[j]);}
			}
		  }
		  this.matrix = new Matrix(temp, Colors);
		  //call the algorithm
		  if(algorithm.equals("BFS")) {
			  bfs(matrix,WithOpen);
		  }
		  else if(algorithm.equals("A*")) {
			  AStar(matrix,WithOpen);
		  }
		  else if(algorithm.equals("DFID")) {
			  DFID(matrix,WithOpen);
		  }
		  else if(algorithm.equals("IDA*")) {
			  IDA(matrix,WithOpen);
		  }
		 
		  //write to the file
		  PrintWriter writer = new PrintWriter("output.txt");
			if(matrix.path==null) {
				writer.println("no path");
				writer.println("Num: "+matrix.num);
			  }else {
				  writer.println(matrix.path);
				  writer.println("Num: "+matrix.num);
				  writer.println("Cost: "+matrix.cost);
			  }
			  long stopTime3 = System.nanoTime();
			  DecimalFormat df = new DecimalFormat("#.###");
			  if(time) {
				  writer.println(df.format((double)(stopTime3 - startTime3) / 1000000000));
			  }
				writer.close();						
				/*
				if(matrix.path==null) {
				System.out.println("no path");
				System.out.println("Num: "+matrix.num);
			  }else {
				  System.out.println(matrix.path);
					System.out.println("Num: "+matrix.num);
					System.out.println("Cost: "+matrix.cost);
			  }
				  System.out.println(df.format((double)(stopTime3 - startTime3) / 1000000000));
		 	*/
	}
}
	public static void main(String[] args) throws IOException {
		 tilePuzzel g = new tilePuzzel("inputoutput//input3.txt");
		//tilePuzzel g = new tilePuzzel("input.txt");
	}
}

