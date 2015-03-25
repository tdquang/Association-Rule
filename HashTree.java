import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
/**
 * Andrew Elenbogen and Quang Tran
 * Data Mining CS 324
 * Association Rule Project
 * 02/05/15
 */

public class HashTree 
{
	private HashMap<Integer, HashMap> tree;
	private int size;
	
	/**
	 * Builds the HashTree based on the given list of lists
	 */
	public HashTree(ArrayList<ArrayList<Integer>> input, int size)
	{
		tree=new HashMap<Integer, HashMap>();
		this.size=size;
		
		for(ArrayList<Integer> currentArray: input)
		{
			Collections.sort(currentArray);
			HashMap currentMap=tree;
			
			for(Integer currentValue: currentArray)
			{
				if(currentMap.get(currentValue)==null)
				{
					HashMap nextTree;
					if(currentArray.lastIndexOf(currentValue)==currentArray.size()-2)
					{
						nextTree=new HashMap<Integer, Integer>();
					}
					else if(currentArray.lastIndexOf(currentValue)==currentArray.size()-1)
					{
						currentMap.put(currentValue, 0);
						break;
					}
					else
					{
						nextTree=new HashMap();
					}
					currentMap.put(currentValue, nextTree);
					currentMap=nextTree;
				}
				else
				{
					currentMap=(HashMap) currentMap.get(currentValue);
				}
			}
		}	
	}
	
	/**
	 * Returns whether or not it contains the given input
	 */
	public boolean contains(ArrayList<Integer> input)
	{
		Collections.sort(input);
		HashMap currentMap=tree;
		for(Integer currentValue: input)
		{
			if(currentMap.get(currentValue)==null)
				return false;
			else if(currentMap.get(currentValue) instanceof Integer && input.lastIndexOf(currentValue)==input.size()-1)
				return true;
			else if(currentMap.get(currentValue) instanceof Integer)
				return false;
			else
				currentMap= (HashMap) currentMap.get(currentValue);
		}
		return false;
	}
	
	/**
	 * Goes to the bottom of the HashTree where the integer support is stored and returns it. Returns
	 * -1 if the element is not in the tree.
	 */
	public int support(ArrayList<Integer> input)
	{
		Collections.sort(input);
		HashMap currentMap=tree;
		for(Integer currentValue: input)
		{
			if(currentMap.get(currentValue)==null)
				return -1;
			if(currentMap.get(currentValue) instanceof Integer)
				return (Integer) currentMap.get(currentValue);
			else
				currentMap= (HashMap) currentMap.get(currentValue);
		}
		return -1;
	}
	/**
	 * Increments all chains which are contained within the input. Also, sorts the input.
	 */
	public void supportIncrement(Collection<Integer> input)
	{
		ArrayList<Integer> array=new ArrayList<Integer>(input);
		Collections.sort(array);	
		supportIncrementRec(array, 0, tree);
	}
	
	/**
	 * The workhorse recursive function that actually does what supportIncrement purports to do.
	 */
	private void supportIncrementRec(ArrayList<Integer> input, int toCheck, HashMap tree)
	{
		if(toCheck>=input.size())
			return;
		
		int checkingItem=input.get(toCheck);
		
		if(tree.get(checkingItem)!=null && !(tree.get(checkingItem) instanceof Integer))
		{
			supportIncrementRec(input, toCheck+1, (HashMap) tree.get(input.get(toCheck)));
			supportIncrementRec(input, toCheck+2, tree);
			return;
		}
		else if(tree.get(checkingItem)!=null && tree.get(checkingItem) instanceof Integer)
		{
			tree.put(checkingItem, ((Integer) tree.get(checkingItem))+1 );
		}
		supportIncrementRec(input, toCheck+1, tree);
	}
	
	

}
