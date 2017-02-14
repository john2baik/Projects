import java.util.*;
import java.io.*;//switch to java.io.File if taking too long

/*
In the first pass, return the singleton frequent item set hashmap after having  done a pass through all the
item sets.
Second pass, use the frequent item set table in the double for loop to check each item is a frequent singleton,
if so, add the pair into a hashmap of key(key1, key2), value(the count)
*/ 

class apriori{
	//all sets containing the sets
	//Basic Idea: each array[] int can have a hashcode based on the contents inside the array
	//we can store each subset as a int array and get a hashcode from that array using Arrays.hashCode(array)
	//then we make that hashcode the key, with the value being the number of instances it has appeared
	private static HashMap<Integer, Integer> allSets = new HashMap<Integer, Integer>();
	// private apriori(HashMap<Integer, Integer> allSets){
	// 	this.allSets = allSets;
	// }
	//private static int[] buckets = new int[9949];//9949 is the closest prime to 10000
	private static List<int[]> frequentItems = new ArrayList<int[]>();//list of all frequent sets


	public static void main(String[] args){
		File dataset = new File(args[0]);
		int min_support_count= Integer.parseInt(args[1]);

		//HashMap<Integer, Integer> allSets = new HashMap<Integer, Integer>();
//		HashMap<Integer, Integer> frequentSingletons = new HashMap<Integer, Integer>();
		//List<int[]> allSets = new ArrayList<int[]>(); //current list of allSets
		//apriori algorithm
		try{
			FileReader fr = new FileReader(dataset);
			BufferedReader br = new BufferedReader(fr);

			//for Lk, we need a type that is traversable, can take more keys when needed, and has a support count
			List<int[]> Lk = new ArrayList<int[]>();
		
			//return the frequent singletons hashmap, this allSets list is all the frequent allSets
			//Lk = 
			Lk = findL1(br, min_support_count);//list of the frequent allSets of L1
			frequentItems.addAll(Lk);//adds all the singleton sets
			
			//test to make sure that Lk is successful and that it has successfully been added to all sets
			// for(int[] x : frequentItems){
			// 	for(int i = 0; i < x.length; i++){
			// 		System.out.print(x[i] + " " );
			// 	}
			// 	System.out.print(" : ");
			// 	int hash = Arrays.hashCode(x);
			// 	int val = allSets.get(hash);

			// 	System.out.print(val);
			// 	System.out.println();
			// }
			

			//will eventually be all the frequent sets found and held
			List<int[]> Ck = new ArrayList<int[]>();
			//apriori algorithm

			for(int k = 2; !Lk.isEmpty(); k++){
				//if its the first 
				//HashMap<Integer, Integer> currentSet = new HashMap<Integer, Integer>();
				fr = new FileReader(dataset);
				br = new BufferedReader(fr);
				Ck = generateCanidates(Lk, min_support_count, k);//create subsets from Lk of size k
				for(int[] x : Ck){
					printArray(x);
				}

				//go through each basket and update the  currentSet hashmap
				Lk.clear();//remove all items in Lk for new Lk
				System.out.print(" reached 1");
				Lk = findLk(br, min_support_count, Ck, k);
				System.out.print(" reached 2");

				for(int[] x : Lk){
					printArray(x);
				}
			
				frequentItems.addAll(Lk);//add to the final
				//returns list of sets as well as currentSet getting updated
				br.close();
				//clear Lk: 
			}


			//br = new BufferedReader(fr);
			//frequentPairs = secondPass(br, frequentSingletons, min_support_count);
			//br.close();

		} catch (IOException e){
			e.printStackTrace();
		}

		File output = new File(args[2]);
		System.out.println("New file created: " + output.toString());

		//write to file

	}



	private static void printArray(int[] anArray) {
      for (int i = 0; i < anArray.length; i++) {
         if (i > 0) {
         	  System.out.print(", ");
         }
         System.out.print(anArray[i]);
      }
      System.out.println();
   }
	/*for each transaction t in datatbase, find all canidates in Ck that are subset of t;
	increment their count, and return Lk: the subsets that are over the min_support_count
	currentSet hashMap gets updated w/ the counts of each int array 
	*/
	public static List<int[]> findLk(BufferedReader br, int min_support_count, List<int[]> Ck, int k){
		HashMap<Integer, Integer> currentSet = new HashMap<Integer, Integer>();//keep the counts during the iteration
		//int[] bucket = new int[7919];//random prime int 
		List<int[]> passedSubsets = new ArrayList<int[]>();//to be returned as the new Ck
		String cur_basket;
		try{
			while((cur_basket = br.readLine()) != null){//translate each item name into integer
				String[] items = cur_basket.split(" ");
				int[] basket = new int[items.length];//change to all integers
				for(int i = 0; i < items.length; i++){//iterating through all the baskets
					int item = Integer.parseInt(items[i]);// one item in the basket
					basket[i] = item;
				}
				//for current basket
				List<Integer> basketList = new ArrayList<Integer>();

				for(int i = 0; i < basket.length; i++){
					basketList.add(basket[i]);
				}
				//find all the subsets of size k from the basket
				List<Set<Integer>> subsets = findAllKSubsets(basketList, k);
				//testing if subsets were found corretly
			
				//now we see if the subsets have matching subsets in Ck
				//if matching, we have to map the key somehow and increment that count by 1
				//convert to a int array, hash the array, currentSet.get(hash) == the count
				for(Set<Integer> subset: subsets){//iterate through 
					int[] set = setToArray(subset);
					if(Ck.contains(set)){//only if it's part of the Ck
						int hash = (Arrays.hashCode(set));
						if(currentSet.get(hash) == null){
							currentSet.put(hash,1);
						}
						else if(currentSet.get(hash) >= min_support_count){
							passedSubsets.add(set);
							currentSet.put(hash, currentSet.get(hash) + 1);
						}
						else{
							currentSet.put(hash, currentSet.get(hash) + 1);
						}
					}
				}

			}
			

		}
		catch (IOException e){
			e.printStackTrace();
		}	


		return passedSubsets;
		//iterate through hashmap to see which ones are frequent
	}

	public static int[] setToArray(Set<Integer> subset){
		int[] arr = new int[subset.size()];
		int index = 0; 
		for(Integer i : subset){
			arr[index++] = i;
		}
		return arr;
	}

	/*
	Method to return all the subsets made from the possible pairs given k-length in an array
	i.e. [1,2,3,4,5], k = 3 ->[1,2,3], [1,2,4], [1,2,5], [2,3,4], [2,3,5], [3,4,5]
	*/
	public static List<Set<Integer>> findAllKSubsets(List<Integer> basket, int k){
		List<Set<Integer>> res = new ArrayList<>();
		getSubsets(basket, k, 0, new HashSet<Integer>(), res);
		return res;
	}

	public static void getSubsets(List<Integer> basket, int k, int idx, Set<Integer> current, List<Set<Integer>> solution){
		if(current.size() == k){
			solution.add(new HashSet<>(current));
			return;
		}
		if(idx == basket.size()) return;
		int x = basket.get(idx);
		current.add(x);
		getSubsets(basket,k,idx+1,current,solution);
		current.remove(x);
		getSubsets(basket, k, idx+1, current, solution);
	}

	/* 
	generate canidates from the given list of all the Lk's of size k
	*/
	public static List<int[]> generateCanidates( List<int[]> Lk, int min_support_count, int k){
		//generate all possible itemset of pairs first
		List<int[]> allCanidates = new ArrayList<int[]>();
		//allSets is the list of frequent sets in the parameter
		//generate all possible pairs for the next subset-k
		for(int i = 0; i < Lk.size(); i++){//i is a 
			for(int j = i+1; j < Lk.size(); j++){
				int[] firstPair = Lk.get(i);
				int[] secondPair = Lk.get(j);
				List<int[]> canidates = new ArrayList<int[]>();
				canidates= merge(firstPair, secondPair, k); //combined to make new set from its subsets
				for(int[] x : canidates){//add all the subsets
					allCanidates.add(x); // in the form[1,2,3] for triple
				}
			}
		}

		// Test to make sure the canidates are generated correctly
		int test = 0;
		// for(int[] z : allCanidates){
		//  	for(int i = 0; i < z.length; i++){
		// 		System.out.print(z[i] + " ");
		//  	}
		//  	System.out.println();
			
		// }
		


		return allCanidates;
		//canidates now contain all the possible canidate sets from the previous frequent k-itemset
		//format: [1,2] or []
	}

	/*
	Given 2 int arrays, merge them into one unique one(without repeats), 
	returns sorted array
	*/
	public static List<int[]> merge(int[] x, int[] y, int k){//need to return 
		List<int[]> ans = new ArrayList<int[]>();
		Set<Integer> set = new HashSet<Integer>();
		int[] merged;
		
			for(int i = 0; i < x.length; i++){
				set.add(x[i]);//1 ,2 
				set.add(y[i]);//3, 4
			}
			TreeSet sortedSet = new TreeSet<Integer>(set);
			merged = new int[sortedSet.size()];
			Iterator iterator = sortedSet.iterator();
			int count = 0;
			while(iterator.hasNext()){
				merged[count] = (int) iterator.next();
				count++;
			}
		
		List<Integer> mergedList = new ArrayList<Integer>();

		for(int i = 0; i < merged.length; i++){
			mergedList.add(merged[i]);
		}
		List<Set<Integer>> subsets = findAllKSubsets(mergedList, k);
		for(Set<Integer> subset: subsets){//iterate through 
			int[] sub = setToArray(subset);
			ans.add(sub);
		}
		return ans;
	}

	
	// public static int hash(int i, int j, int size){
	// 	return (i*j * 109) % size;
	// }



	/* 
	In the first pass, we find the frequent item tableset of the data that is greater than the threshold.
	We first run through the data string by string and map each item with a count.
	Then in the second hashmap, we only map those who have a count that is greater than or equal to the min_support_cnt
	During the first pass, we also create all possible pairs within the transaction, hash it to a bucket and then increment the bucket counter
	*/
	//
	public static List<int[]> findL1(BufferedReader br, int min_support_count){//returns two arrays
		
		String cur_basket;
		HashMap<Integer, Integer> singletonCount = new HashMap<Integer, Integer>();

		//get a count for each item and map it to singletonCount
		try{
			while((cur_basket = br.readLine()) != null){//translate each item name into integer
				String[] items = cur_basket.split(" ");
				for(int i = 0; i < items.length; i++){//iterating through all the baskets
					int item = Integer.parseInt(items[i]);// one item in the basket
					//set the singleton count using hashmap
					if(singletonCount.get(item) == null){
						singletonCount.put(item, 1);
					}
					else{//increment the count of the item 
						singletonCount.put(item, singletonCount.get(item) + 1);
					}
				}

			}
		} catch (IOException e){
			e.printStackTrace();
		}	
		List<int[]> frequentSingletons = new ArrayList<int[]>();
		//create list of the frequent singletons sets, make the singleton into an array just to follow the same data structure type
		for(int key : singletonCount.keySet()){
			int val = singletonCount.get(key);
			int[] x = {key};
			//x[0] = val;
		//	x[1] = key;
			if(val >= min_support_count){//if it meets the threshhold
				frequentSingletons.add(x);//if value is larger than the minsupportcount, add array to the list
				int hash = Arrays.hashCode(x);
				allSets.put(hash, val);//put the count of the array in the allSets hashmap
				//System.out.print("readched here");
			}
		}
		return frequentSingletons; //return a list of the frequent allSets
	}

	//perhaps use a TreeNode
	// public static final class TreeNode{
	// 	private TreeNode[] children;//each node has a list of its children nodes
	// 	private TreeNode parent;//each node has 1 parent
	// 	private int count;//the count of the nodes frequency
	// 	private int[] keys;

	// 	public TreeNode(TreeNode parent, TreeNode[] children, int[] keys; int count){
	// 		this.parent = parent;
	// 		this.children = children;
	// 		this.keys = keys;
	// 		this.count = count;
	// 	}

	// }
}

