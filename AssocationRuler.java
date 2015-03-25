/**
 * Andrew Elenbogen and Quang Tran
 * Data Mining CS 324
 * Association Rule Project
 * 02/05/15
 * 
 * AssocationRuler is a class that finds association rules, that is, which sets of movie ids appear across many users.
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class AssocationRuler 
{
	/**
	 * NOTE TO GRADER: 
	 * Change these public static final fields to manipulate where on the system the program looks for the input files.
	 */
	public static final String RATINGS_FILE_NAME="/private/tmp/ml-1m/ratings.dat";
	public static final String MOVIE_FILE_NAME="/private/tmp/ml-1m/movies.dat";

	private HashMap<Integer, ArrayList<Integer>> userIdToMovieId;
	private HashMap<Integer, ArrayList<Integer>> movieIdToUserId;
	public HashMap<Integer, String> movieIdToMovieName;
	
	private ArrayList<ArrayList<ArrayList<Integer>>> prevSets=new ArrayList<ArrayList<ArrayList<Integer>>>();

	/**
	 * In the constructor, we read the input files into all three HashMaps. Note that one is so far unused.
	 */
	public AssocationRuler()
	{
		userIdToMovieId=new HashMap<Integer, ArrayList<Integer>>();
		movieIdToUserId=new HashMap<Integer, ArrayList<Integer>>();
		movieIdToMovieName= new  HashMap<Integer, String>();
		
		try(Scanner scanner=new Scanner(new File(RATINGS_FILE_NAME)))
		{
			while(scanner.hasNextLine())
			{
				String[] split=scanner.nextLine().split("::");
				int userId=Integer.parseInt(split[0]);
				int movieId=Integer.parseInt(split[1]);
				if(userIdToMovieId.get(userId)==null)
					userIdToMovieId.put(userId,  new ArrayList<Integer>());
				userIdToMovieId.get(userId).add(movieId);

				if(movieIdToUserId.get(movieId)==null)
					movieIdToUserId.put(movieId, new ArrayList<Integer>());
				movieIdToUserId.get(movieId).add(userId);
			}
		}
		catch(FileNotFoundException e)
		{
			System.err.println("Invalid ratings file name. Application will terminate");
			System.exit(1);
		}

		try(BufferedReader reader=new BufferedReader(new FileReader(new File(MOVIE_FILE_NAME))))
		{
			String curLine=null;
			while( (curLine=reader.readLine() )!=null)
			{
				String[] split=curLine.split("::");
				movieIdToMovieName.put(Integer.parseInt(split[0]), split[1]);
			}
		}
		catch(IOException e)
		{
			System.err.println("Invalid movie file name. Application will terminate");
			System.exit(2);
		}
	}
	/**
	 * This method finds all item sets of size 1 with a support of the parameter or higher using the 
	 * movieIdToUserId HashMap.
	 */
	public ArrayList<ArrayList<Integer>> frequentItemSetsOfSize1(int support)
	{
		ArrayList<ArrayList<Integer>> sets= new ArrayList<ArrayList<Integer>>();

		for(int movieId: movieIdToUserId.keySet())
		{
			if(movieIdToUserId.get(movieId).size()>support)
			{
				ArrayList<Integer> set= new ArrayList<Integer>();
				set.add(movieId);
				sets.add(set);
			}
		}
			
		System.out.println("Size of candidates after pruning before final check for size 1: "+sets.size());
		return sets;
	}
	
	/**
	 * Runs the recursive function frequentItemSetsRec
	 */
	public ArrayList<ArrayList<Integer>> frequentItemSets(int support, int size)
	{
		ArrayList<ArrayList<Integer>> result=frequentItemSetsRec(support, size);
		for(int i=0; i<size-1; i++)
		{
			result.addAll(prevSets.get(i));
		}
		return result;
	}
	
	/**
	 * 
	 * @param support value inputed by the user
	 * @param size k of sets
	 * @return array list of frequent item sets
	 */
	private ArrayList<ArrayList<Integer>> frequentItemSetsRec(int support, int size)
	{
		if(size==1)
		{
			return frequentItemSetsOfSize1(support);
		}
		else
		{
			ArrayList<ArrayList<Integer>> prevSet=frequentItemSetsRec(support, size-1);
			prevSets.add(size-2, prevSet);
			ArrayList<ArrayList<Integer>> candidates=new ArrayList<ArrayList<Integer>>();
			HashSet<ArrayList<Integer>> toRemove=new HashSet<ArrayList<Integer>>();
			//Combine prevSet Frequent items
			for(int i=0; i< prevSet.size()-1; i++)
			{
				for(int j=i+1; j<prevSet.size(); j++)
				{
					boolean valid=true;
					for(int k=1; k<size-1; k++)
					{
						if(prevSet.get(i).get(k)!=prevSet.get(j).get(k))
						{
							valid=false;
							break;
						}
					}
					if(valid)
					{
						ArrayList<Integer> candidate=new ArrayList<Integer>(prevSet.get(i));
						candidate.add(prevSet.get(j).get(0));
						candidates.add(candidate);
					}
				}
			}
			//Check that each candidate has all subsets in tree
			HashTree pruneTree=new HashTree(prevSet, size-1);
			for(ArrayList<Integer>candidate: candidates)
			{
				for(int i=0; i<size; i++)
				{
					Integer removed=candidate.remove(i);
					boolean result=pruneTree.contains(candidate);
					candidate.add(i, removed);
					if(!result)
					{
						toRemove.add(candidate);
						break;
					}
				}
			}
			candidates.removeAll(toRemove);
			
			System.out.println("Size of candidates after pruning before final check for size "+size+": "+candidates.size());
			
			HashTree candidateTree=new HashTree(candidates, size);
			
			for(ArrayList<Integer> currentList: userIdToMovieId.values())
			{
				candidateTree.supportIncrement(currentList);
			}
			
			ArrayList<ArrayList<Integer>> finalCandidates=new ArrayList<ArrayList<Integer>>();
			for(ArrayList<Integer> candidate : candidates)
			{
				int canSup= candidateTree.support(candidate);
				//System.out.println(canSup);
				if(canSup>support)
				{
					finalCandidates.add(candidate);
				}
			}
			
			return finalCandidates;
		}	
	}
	
	/**
	 * This method converts a list of item sets represented by Ids to a list of items represented by Strings.
	 */
	public ArrayList<ArrayList<String>> convertMovieIdToMovieName(ArrayList<ArrayList<Integer>> movieIdItems)
	{	
		ArrayList<ArrayList<String>> stringSets= new ArrayList<ArrayList<String>>();

		for(ArrayList<Integer> currentMovieIdSet: movieIdItems)
		{
			ArrayList<String> stringSet=new ArrayList<String>();
			for(Integer currentMovieId: currentMovieIdSet)
			{
				if(movieIdToMovieName.get(currentMovieId)!=null )
					stringSet.add(movieIdToMovieName.get(currentMovieId));
				else
					stringSet.add(""+currentMovieId);
			}
			stringSets.add(stringSet);
		}
		return stringSets;
	}
	
	/**
	 * 
	 * @param frequentItemSets, which is the array list of frequent item sets
	 * @param confidence level inputed by the user
	 * @return hashmap with association rules
	 */
	public HashMap<Integer, ArrayList<Integer>> getAssociationRules(ArrayList<ArrayList<Integer>> frequentItemSets, double confidence)
	{
		HashMap<Integer, ArrayList<Integer>> toReturn= new HashMap<Integer, ArrayList<Integer>>();
		
		for (ArrayList<Integer> candidate : frequentItemSets)
		{
			for(int i=0; i<candidate.size(); i++)
			{
				int candidateLeftSide=candidate.get(i);
				//support(XUY)/support(x)
				if(movieIdToUserId.get(candidateLeftSide).size()/getSupport(candidate)>confidence)
				{
					ArrayList<Integer> copyOfCandidate=new ArrayList<Integer>(candidate.size());
					for(int j=0; j< candidate.size(); j++)
					{
						copyOfCandidate.add(candidate.get(j));
					}
					copyOfCandidate.remove(i);
					toReturn.put(candidateLeftSide, copyOfCandidate);
				}
			}
			
		}
		return toReturn;
	}
	
	/**
	 * 
	 * @param array list of items
	 * @return support of the item set
	 */
	
	public int getSupport(ArrayList<Integer> itemSet)
	{
		int support=0;
		for(ArrayList<Integer> transactions: userIdToMovieId.values())
		{
			if(transactions.containsAll(itemSet))
				support++;
		}
		return support;
	}
	


	public static void main(String[] args)
	{
		AssocationRuler ruler=new AssocationRuler();
		try(Scanner scanner=new Scanner(System.in))
		{
			System.out.print("Enter a level of support> ");
			int support=scanner.nextInt();
			System.out.print("Enter the size of sets to find> ");
			int size=scanner.nextInt();
			System.out.print("Enter the confidence threshold for association rules> ");
			double confidence=scanner.nextDouble();

			ArrayList<ArrayList<Integer>> result= ruler.frequentItemSets(support, size);
			System.out.println("Id list of Elements With Size "+size+" is: ");
			System.out.println(result);
			System.out.println("Name list is: ");
			System.out.println(ruler.convertMovieIdToMovieName(result));
			
			System.out.println("Association Rules: ");
			HashMap<Integer, ArrayList<Integer>> rules=ruler.getAssociationRules(result, confidence);
			System.out.println(rules);
			HashMap<String, ArrayList<String>> stringRules= new HashMap<String, ArrayList<String>>();
			
			for(Integer current: rules.keySet())
			{
				ArrayList<String> stringArray= new ArrayList<String>();
				for(Integer currentSubValue: rules.get(current))
				{
					stringArray.add(ruler.movieIdToMovieName.get(currentSubValue));
				}
				stringRules.put(ruler.movieIdToMovieName.get(current), stringArray);
			}
			System.out.println(stringRules);
		}
	}

}
