package server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

import com.google.gson.Gson;

import models.Review;
import models.SQLCalls;


@WebServlet("/ReviewServ")
public class ReviewServ extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public ReviewServ() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//Need to map the review to a location and a User
		String locationID = request.getParameter("locationID");
		String userID = request.getParameter("userID");  //maybe getAttribute()?? since user won't be changing often
														 //so maybe we can session.setAttribute("userID", userID) 

		// Connect to MySQL database
		SQLCalls ReviewCalls = new SQLCalls();
		String reviewTitle = "";
		String reviewBody = "";
		
		// Communicate with front end
		PrintWriter pw = response.getWriter();
		
		if(locationID != null && userID != null){
			reviewTitle = ReviewCalls.reviewToTitle(locationID, userID);
			reviewBody = ReviewCalls.reviewToBody(locationID, userID);
			String userName = ReviewCalls.reviewToName(userID);
			String locName = ReviewCalls.reviewToLocation(locationID);
			String reviewImage = ReviewCalls.reviewToImage(locationID, userID);
			int upvote = ReviewCalls.reviewToUpvote(locationID);
			int downvote = ReviewCalls.reviewToDownvote(locationID);
			double elevatorRating = ReviewCalls.reviewToElevatorRating(locationID, userID);
			double rampRating = ReviewCalls.reviewToRampRating(locationID, userID);
			double doorRating = ReviewCalls.reviewToDoorRating(locationID, userID);
			double otherRating = ReviewCalls.reviewToOtherRating(locationID, userID);
			
			Review review = new Review(reviewTitle, reviewBody, elevatorRating, rampRating, doorRating, otherRating,
					userName, upvote, downvote, locName, reviewImage);
		
			Gson gson = new Gson();
			String jsonReview = gson.toJson(review);
			
			// Send JSON to front end
			pw.println(jsonReview);
		}
		
		//NOTE FOR FRONTEND: these are the names of the buttons to submit forms
		//1. name="submit" is for submitting a review; return 1 if success
		//2. name="upvote" is for upvoting; return 2 if success
		//3. name="downvote" for downvoting; return 3 if success
		String requestType = request.getParameter("requestType");
		
		if(requestType.contentEquals("submit")){
			String newtitle = request.getParameter("title");
			String newbody = request.getParameter("body");
			double elevatorRating = Double.parseDouble(request.getParameter("elevatorRating"));
			double rampRating = Double.parseDouble(request.getParameter("rampRating"));
			double doorRating = Double.parseDouble(request.getParameter("doorRating"));
			double otherRating = Double.parseDouble(request.getParameter("otherRating"));
			ReviewCalls.addReview(locationID, userID, newtitle, newbody, elevatorRating, rampRating, doorRating, otherRating);
			pw.println("1");
		}
		if (requestType.contentEquals("upvote")) {
			int currupvote = ReviewCalls.reviewToUpvote(locationID);
			currupvote++;
			ReviewCalls.addUpvote(locationID, currupvote);
			pw.println("2");
		}
		//if downvote, increase the downvote count in database, dependent upon whether or not user has already downvoted before
		if (requestType.contentEquals("downvote")) {
			int currdownvote = ReviewCalls.reviewToUpvote(locationID);
			currdownvote--;
			ReviewCalls.addDownvote(locationID, currdownvote);
			pw.println("3");
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
