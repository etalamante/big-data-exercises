package nearsoft.academy.bigdata.recommendation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

public class MovieRecommender {
	public static void main(String[] args) {
	}
	
	ArrayList<String> users = new ArrayList<String>();
	ArrayList<String> movies = new ArrayList<String>();
	ArrayList<String> scores = new ArrayList<String>();
	Map<String, Integer> userIndexes = new HashMap<String, Integer>();
	Map<String, Integer> movieIndexes = new HashMap<String, Integer>();
	int totalUsers = 0;
	int totalProducts = 0;
	int totalScores = 0;
	int idUser;
	int idMovie;
	String[] user;
	String[] movie;
	String[] score;
	String outputFile;
	
	
	public MovieRecommender(String inputFilePath) throws IOException, TasteException {
		

		InputStream fileStream = new FileInputStream(inputFilePath);
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader decoder = new InputStreamReader(gzipStream);
        BufferedReader buffer = new BufferedReader(decoder);
		PrintWriter stream = new PrintWriter(new File ("movies.txt"));
		
		String lineToAnalize = "";
		lineToAnalize = buffer.readLine();
		for(int i = 0; lineToAnalize != null ; ) {
			
			if(lineToAnalize.contains("review/userId: ")) {
				user = lineToAnalize.split("review/userId: ");
				users.add(user[1]);
				for(int x = 1; x < 4 ; x++) {
					lineToAnalize = buffer.readLine();
					if (x == 3) {
						i = x + i;
						continue;
					}
				}
			} else if(lineToAnalize.contains("product/productId: ")) {
				movie = lineToAnalize.split("product/productId: ");
				movies.add(movie[1]);
				lineToAnalize = buffer.readLine();
				i++;
				continue;
			} else if(lineToAnalize.contains("review/score: ")) {	
				score = lineToAnalize.split("review/score: ");
				scores.add(score[1]);
				for(int x = 1; x < 6 ; x++) {
					lineToAnalize = buffer.readLine();
					if (x == 5) {
						i = x + i;
						continue;
					}
				}
			} else {
				lineToAnalize = buffer.readLine();
				i++;
			}
		}
		
		idUser = 1;
		idMovie = 1;
		for(String stringValue : users) {
			if(!userIndexes.containsKey(stringValue)) {
				userIndexes.put(stringValue, idUser);
				idUser++;
			}
		}
		for(String stringValue : movies) {
			if(!movieIndexes.containsKey(stringValue)) {
				movieIndexes.put(stringValue, idMovie);
				idMovie++;
			}
		}
		for(int i = 0; i < scores.size(); i++) {
			stream.write(userIndexes.get(users.get(i)) + "," + movieIndexes.get(movies.get(i)) + 
					"," + scores.get(i));
			stream.println();
		}
		
		stream.close();
	}
	
	
	
	public List<String> getRecommendationsForUser(String userToAnalize) throws TasteException {
		
		List<RecommendedItem> recommendations = null;
		try {
		DataModel model = new FileDataModel(new File("movies.txt"));
		UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
    	UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
    	UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, 
    			similarity);
    	recommendations = recommender.recommend(userIndexes.get(userToAnalize), 3);
		} catch(IOException e) {
			        System.err.println("Caught IOException: " + e.getMessage());
		} catch(TasteException e){
			        System.err.println("Caught TasteException: " + e.getMessage());
		}
		
    	
    	List<String> listElement = new ArrayList<String>();
    	for (RecommendedItem recommendation : recommendations) {
      	  listElement.add(getMovieKey((int) recommendation.getItemID()));
      	}
    	return listElement;
	}
	
	public String getMovieKey(int movieValue) {
		for(String keyString : movieIndexes.keySet()) {
			if(movieIndexes.get(keyString) == movieValue)
				return keyString;
		}
		return null;
	}
	
	public int getTotalReviews() throws IOException {
		return this.scores.size();
	}	
    public int getTotalProducts() throws TasteException {
        return this.movieIndexes.size();
    }	
    public int getTotalUsers() throws TasteException, IOException {
        return this.userIndexes.size();
    }
	
}
