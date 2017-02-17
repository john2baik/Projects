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
    //private static HashMap<Integer, Integer> allSets = new HashMap<Integer, Integer>();

    //private static List<int[]> frequentItems = new ArrayList<int[]>();//list of all frequent sets
    public static void main(String[] args){
        System.out.println("Starting timer: ");
        File dataset = new File(args[0]);
        int min_support_count= Integer.parseInt(args[1]);
        HashMap<String, Integer> allSets = new HashMap<String, Integer>();
        List<int[]> frequentItems = new ArrayList<int[]>();//use Arrays.hashCode() to map to allSets

        //apriori algorithm
        try{
            FileReader fr = new FileReader(dataset);
            BufferedReader br = new BufferedReader(fr);
            //for Lk, we need a type that is traversable, can take more keys when needed, and has a support count
            List<int[]> Lk = new ArrayList<int[]>();
            List<int[]> Ck = new ArrayList<int[]>();

            findL1(br, min_support_count, allSets, Lk);//list of the frequent allSets of L1
            frequentItems.addAll(Lk);//adds all the singleton sets
            System.out.println("Frequent items after pass of L1");
            for(int[] l : frequentItems){
                System.out.println(Arrays.toString(l));
            }
            int k = 2;
            while(!(Lk.isEmpty())){
                //if its the first
                //HashMap<Integer, Integer> currentSet = new HashMap<Integer, Integer>();
                fr = new FileReader(dataset);
                br = new BufferedReader(fr);
                Ck.clear();
                generateCanidates(Lk, Ck, min_support_count, k);//create subsets from Lk of size k as Ck, update Ck
               //  for(int[] x : Ck){
               //     printArray(x);
                   // if(x[0] > 50) break;
               // }
                Lk.clear();//remove all items in Lk for new Lk
                // System.out.print(" reached 1");
                findLk(br, min_support_count, Ck, Lk, k, allSets);//this is the problem right now
                frequentItems.addAll(Lk);
                // System.out.print(" reached 2");
                System.out.println("Added to Frequent Items");
                for(int[] l : frequentItems){
                    System.out.println(Arrays.toString(l) + " : " + allSets.get(Arrays.toString((l))));
                }

                // System.out.println("Frequent items for size k = " + k);
                 br.close();
                // //clear Lk:
                // for(int[] z : frequentItems){
                // 	printArray(z);
                // }
                k++;
            }
        } catch (IOException e){
            e.printStackTrace();
        }

        File output = new File(args[2]);
        System.out.println("New file created: " + output.toString());
        //dont use system.out.print, use writer.out.print
        // try{
        // 	PrintWriter writer = new PrintWriter(output.toString(), "UTF-8");
        // 	for(int[] x: frequentItems){
        // 		for(int i = 0; i < x.length; i++){
        // 			writer.print(x[i] + " ");
        // 		}
        // 		int hash = x.hashCode();
        // 		writer.print(" (" + allSets.get(hash) + ")");
        // 		writer.println();
        // 	}
        // 	writer.close();
        // } catch (IOException e){
        // 	e.printStackTrace();
        // }


        //write to file

    }


    /*
    generate canidates from the given list of all the Lk's of size k
    */
    public static void generateCanidates( List<int[]> Lk, List<int[]> Ck, int min_support_count, int k) {
        //    List<int[]> allCanidates = new ArrayList<int[]>();
        //generate all possible pairs for the next subset-k
        for(int i = 0; i < Lk.size(); i++) {//i is a
            for(int j = i + 1; j < Lk.size(); j++) {
                int[] firstPair = Lk.get(i);
                int[] secondPair = Lk.get(j);
                List<int[]> canidates = new ArrayList<int[]>();//list of canidates to be returned after
                merge(canidates, firstPair, secondPair, k); //combined to make new set from its subsets
                for (int[] x : canidates) {//add all the subsets
                    Ck.add(x); // in the form[1,2,3] for triple
                }
            }
        }
    }

    /*
Given 2 int arrays, merge them into one unique one(without repeats),
then find all the subsets of size k for the two arrays that are merged
*/
    public static void merge(List<int[]> canidates, int[] x, int[] y, int k){//need to return
        Set<Integer> set = new HashSet<>();
        for(int i = 0; i < x.length; i++){
            set.add(x[i]);//1 ,2
            set.add(y[i]);//3, 4
        }
        TreeSet sortedSet = new TreeSet<Integer>(set);
        int[] merged = new int[sortedSet.size()];
        Iterator iterator = sortedSet.iterator();
        int count = 0;
        while(iterator.hasNext()){
            merged[count] = (int) iterator.next();
            count++;
        }
        //convert merged
        List<Integer> mergedList = new ArrayList<Integer>();
       // mergedList.addAll(sortedSet);

        for(int i = 0; i < merged.length; i++){
            mergedList.add(merged[i]);
        }

        List<Set<Integer>> subsets = findAllKSubsets(mergedList, k);
        for(Set<Integer> subset: subsets){//iterate through
            int[] sub = setToArray(subset, k);
            Arrays.sort(sub);
            canidates.add(sub);
           // System.out.println(Arrays.toString(sub));
        }
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

     public static int[] setToArray(Set<Integer> set, int k){
 	 	int[] ans = new int[set.size()];
 	 //	System.out.print(ans);
 	 	Iterator<Integer> it = set.iterator();
 	 	int i = 0;
 	 	while(it.hasNext() && i < k){
 	 		//if(i == k-1) break;
 	 		ans[i] = it.next();
 	 		//System.out.print(ans[i] + " ");
 	 		i++;
 	 	}
 	 	Arrays.sort(ans);
 	 	return ans;

   	}


    private static void printArray(int[] anArray) {
        for (int i = 0; i < anArray.length; i++) {
            System.out.print(anArray[i] + " ");
        }
        System.out.println();
    }
	/*for each transaction t in datatbase, find all canidates in Ck that are subset of t;
	increment their count, and return Lk: the subsets that are over the min_support_count
	currentSet hashMap gets updated w/ the counts of each int array
	*/
     public static void findLk(BufferedReader br, int min_support_count, List<int[]> Ck, List<int[]> Lk, int k, HashMap<String, Integer> allSets){
     	HashMap<String, Integer> currentSet = new HashMap<String, Integer>();//keep the counts during the iteration
     	for(int[] z: Ck){
     //		printArray(z);
     	}
     	//int[] o = {486,758};
     	//if(Ck.contains(o)) System.out.println("contains works");
     	//int[] bucket = new int[7919];//random prime int
     	String cur_basket;
         for(int[] l : Ck){
             System.out.println(Arrays.toString(l) + " This is Ck");
         }
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
     			//System.out.println(subsets);
     			//testing if subsets were found corretly

     			//now we see if the subsets have matching subsets in Ck
     			//if matching, we have to map the key somehow and increment that count by 1
     			//convert to a int array, hash the array, currentSet.get(hash) == the count

     			for(Set<Integer> subset: subsets){//iterate through
     				int[] set = setToArray(subset, k);
                    //System.out.println(Arrays.toString(set) + " this is the set");

                    for(int[] ckSet : Ck){
                        Object[] arr1 = {set};
                        Object[] arr2 = {ckSet};
                        if(Arrays.deepEquals(arr1, arr2)){
                            //System.out.println("Found " + Arrays.toString(set) + " in Ck");
                            String key = Arrays.toString(set);
                           // Ck.remove(set);
                            if(currentSet.get(key) == null){
                                currentSet.put(key,1);
                            }
                            else{
                                currentSet.put(key, currentSet.get(key) + 1);
                                if(currentSet.get(key) > 500){
                                    System.out.println(key + " : " + currentSet.get(key) + " just passed 500!!!");

                                }
                            }
                        }
                    }
     			}
     		}
     	}
     	catch (IOException e){
     		e.printStackTrace();
     	}
     	System.out.println("currentSet now complete.");

         //now add all the currentSet to the allSets and also Lk if they meet the min_support_count
         for(String x : currentSet.keySet()){
             int count = currentSet.get(x);
             if(count >= min_support_count){
                 int[] xToArray = stringToArray(x);
                 Lk.add(xToArray);
                 allSets.put(x, count);
               //  System.out.println(x + " " + allSets.get(x));
             }
         }
     }

     /*
     Covnert a string [1,2,3] into an array
      */
     public static int[] stringToArray(String x){
         String[] items = x.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
         int[] results = new int[items.length];

         for (int i = 0; i < items.length; i++) {
             results[i] = Integer.parseInt(items[i]);
         }
         return results;
     }

    /*
    In the first pass, we find the frequent item tableset of the data that is greater than the threshold.
    We first run through the data string by string and map each item with a count.
    Then in the second hashmap, we only map those who have a count that is greater than or equal to the min_support_cnt
    During the first pass, we also map to a class hashmap, the key and the value of each frequent singleton
    */
    //
    public static void findL1(BufferedReader br, int min_support_count, HashMap<String, Integer> allSets, List<int[]> Lk){//returns two arrays

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
        //all the counts are now hashed in the singletonCount

        //List<int[]> Lk = new ArrayList<int[]>();
        //create list of the frequent singletons sets, make the singleton into an array just to follow the same data structure type
        for(int key : singletonCount.keySet()){
            int val = singletonCount.get(key);
            int[] x = {key};
            if(val >= min_support_count){//if it meets the threshhold
                Lk.add(x);//if value is larger than the minsupportcount, add array to the list
                String hash = Arrays.toString(x);
                allSets.put(hash, val);
                //	System.out.println(hash + " : " + val + " ");
            }
        }
        //return Lk; //return a list of the frequent allSets
    }
}

//i made it faster by
