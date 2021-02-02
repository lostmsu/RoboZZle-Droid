/**
 * 
 */
package com.team242.robozzle;

import com.team242.robozzle.model.Puzzle;
import com.team242.robozzle.service.OperationNotSupportedByClientException;
import com.team242.util.ISO8601DateTimeParser;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.ksoap2.transport.Transport;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.MGF1ParameterSpec;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * @author lost
 * 
 */
public class RobozzleWebClient {
	public enum  SortKind {
		CAMPAIGN(0),
		EASY_TO_HARD(1),
		DESCENDING_ID(2),
		POPULAR(3),
		;
		SortKind(int value){
			this.value = value;
		}
		public final int value;
	}

	static final String ACTION_PREFIX = "http://tempuri.org/IRobozzleService/";
	static final String GET_TOP_SOLVERS2 = "GetTopSolvers2";
	static final String GET_TOP_SOLVERS = "GetTopSolvers";
	static final String LOG_IN = "LogIn";
	static final String REGISTER_USER = "RegisterUser";
	static final String GET_LEVEL = "GetLevels";
	static final String GET_LEVELS = "GetLevels2";
	static final String GET_LEVELS_PAGED = "GetLevelsPaged";
	static final String SUBMIT_SOLUTION = "SubmitSolution";
	static final String SUBMIT_LEVEL_VOTE = "SubmitLevelVote";

	static String action(String method) {
		return ACTION_PREFIX + method;
	}

	static final String NAMESPACE = "http://tempuri.org/";
	static final String URL = "http://www.robozzle.com/RobozzleService.svc";

	final Transport transport;
	final SoapSerializationEnvelope soapEnvelop;

	public RobozzleWebClient() {
		soapEnvelop = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		soapEnvelop.dotNet = true;

		transport = new HttpTransportSE(URL);
		transport.debug = true;
	}

	public SolversResult[] getTopSolversToday() throws IOException,
			XmlPullParserException {
		SoapObject request = new SoapObject(NAMESPACE, GET_TOP_SOLVERS2);

		soapEnvelop.setOutputSoapObject(request);

		transport.call(action(GET_TOP_SOLVERS2), soapEnvelop);

		SoapObject result = (SoapObject) soapEnvelop.bodyIn;
		SoapObject todayNames = (SoapObject) result.getProperty(2);
		SoapObject todaySolved = (SoapObject) result.getProperty(3);
		SolversResult[] solvers = new SolversResult[todayNames
				.getPropertyCount()];
		for (int i = todayNames.getPropertyCount() - 1; i >= 0; i--) {
			solvers[i] = new SolversResult(
					(SoapPrimitive) todayNames.getProperty(i),
					(SoapPrimitive) todaySolved.getProperty(i));
		}
		return solvers;
	}

	public SolversResult[] getTopSolvers() throws IOException,
			XmlPullParserException {
		SoapObject request = new SoapObject(NAMESPACE, GET_TOP_SOLVERS);

		soapEnvelop.setOutputSoapObject(request);

		transport.call(action(GET_TOP_SOLVERS), soapEnvelop);

		SoapObject result = (SoapObject) soapEnvelop.bodyIn;
		SoapObject todayNames = (SoapObject) result.getProperty(0);
		SoapObject todaySolved = (SoapObject) result.getProperty(1);
		SolversResult[] solvers = new SolversResult[todayNames
				.getPropertyCount()];
		for (int i = todayNames.getPropertyCount() - 1; i >= 0; i--) {
			solvers[i] = new SolversResult(
					(SoapPrimitive) todayNames.getProperty(i),
					(SoapPrimitive) todaySolved.getProperty(i));
		}
		return solvers;
	}

	public boolean LogIn(String userName, String password,
			Collection<Integer> solved, Collection<LevelVoteInfo> votes)
			throws IOException, OperationNotSupportedByClientException {
		SoapObject request = new SoapObject(NAMESPACE, LOG_IN);
		request.addProperty("userName", userName);
		try {
			String hash = computeHash(password);
			request.addProperty("password", hash);

			soapEnvelop.setOutputSoapObject(request);

			transport.call(action(LOG_IN), soapEnvelop);

			soapEnvelop.getResponse();
		} catch (NoSuchAlgorithmException e){
			throw new OperationNotSupportedByClientException(e);
		} catch (XmlPullParserException e){
			throw new OperationNotSupportedByClientException(e);
		}
		SoapObject body = obj(soapEnvelop.bodyIn);
		boolean success = Boolean.parseBoolean(prim(body.getProperty(0)).toString());
		
		if (success){
			SoapObject solvedLevels = obj(body.getProperty("solvedLevels"));
			for(int i = solvedLevels.getPropertyCount() - 1; i>=0; i--){
				solved.add(Integer.parseInt(solvedLevels.getProperty(i).toString()));
			}
			
			SoapObject votesList = obj(body.getProperty("votes"));
			for(int i = votesList.getPropertyCount() - 1; i >= 0; i--){
				SoapObject vote = obj(votesList.getProperty(i));
				LevelVoteInfo voteInfo = new LevelVoteInfo(
					int32(prim(vote.getProperty(0))),
					vote.getProperty(1).toString(),
					int32(prim(vote.getProperty(2))),
					int32(prim(vote.getProperty(3))));
				votes.add(voteInfo);
			}
		}
		
		return success;
	}

	public String Register(String userName, String password, String email)
			throws OperationNotSupportedByClientException, IOException {
		try {
			SoapObject request = new SoapObject(NAMESPACE, REGISTER_USER);
			request.addProperty("userName", userName);
			String hash = computeHash(password);
			request.addProperty("password", hash);
			request.addProperty("email", email);

			soapEnvelop.setOutputSoapObject(request);

			transport.call(action(REGISTER_USER), soapEnvelop);
			return prim(soapEnvelop.getResponse()).toString();
		} catch (NoSuchAlgorithmException e){
			throw new OperationNotSupportedByClientException(e);
		} catch (XmlPullParserException e){
			throw new OperationNotSupportedByClientException(e);
		}
	}
	
	public LevelInfo[] GetLevel(int levelID, Map<Integer, Integer> commentCounts) throws IOException, XmlPullParserException{
		SoapObject request = new SoapObject(NAMESPACE, GET_LEVEL);
		request.addProperty("levelId", levelID + "");
		soapEnvelop.setOutputSoapObject(request);
		transport.call(action(GET_LEVEL), soapEnvelop);
		
		return parseLevels();
	}
	
	public LevelInfo[] GetLevels(Map<Integer, Integer> commentCounts) throws IOException, XmlPullParserException{
		SoapObject request = new SoapObject(NAMESPACE, GET_LEVELS);
		soapEnvelop.setOutputSoapObject(request);
		transport.call(action(GET_LEVELS), soapEnvelop);
		
		return parseLevels();
	}
	
	public int GetLevels(int blockIndex, int blockSize, SortKind sortKind, boolean unsolvedOnly, Collection<Puzzle> levels) throws IOException, XmlPullParserException, ParseException{
		SoapObject request = new SoapObject(NAMESPACE, GET_LEVELS_PAGED);
		request.addProperty("blockIndex", blockIndex + "");
		request.addProperty("blockSize", blockSize + "");
		request.addProperty("sortKind", sortKind.value);
		request.addAttribute("unsolvedByUser", unsolvedOnly);
		
		soapEnvelop.setOutputSoapObject(request);
		transport.call(action(GET_LEVELS_PAGED), soapEnvelop);
		
		if (levels != null){
			SoapObject levelsObject = obj(obj(soapEnvelop.bodyIn).getProperty(0));
			for(int i = levelsObject.getPropertyCount() - 1; i >= 0; i--){
				Puzzle level = parseLevelEx(obj(levelsObject.getProperty(i)));
				levels.add(level);
			}
		}
		
		return int32(obj(soapEnvelop.bodyIn).getProperty(1));
	}
	
	public String SubmitSolution(int levelID, String userName, String password, String solution)
			throws NoSuchAlgorithmException, IOException, XmlPullParserException{
		SoapObject request = LevelRequest(SUBMIT_SOLUTION, levelID, userName, password);
		request.addProperty("solution", solution);
		
		soapEnvelop.setOutputSoapObject(request);
		transport.call(action(SUBMIT_SOLUTION), soapEnvelop);
		
		SoapPrimitive response = prim(soapEnvelop.getResponse());
		if (response == null) return null;
		return response.toString();
	}

	private static SoapObject LevelRequest(String command, int levelID, String userName, String password)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		SoapObject request = new SoapObject(NAMESPACE, command);
		request.addProperty("levelId", levelID + "");
		request.addProperty("userName", userName);
		String hash = computeHash(password);
		request.addProperty("password", hash);
		return request;
	}


	public boolean SubmitLevelVote(int levelID, String userName, String password, int like, int difficulty)
			throws NoSuchAlgorithmException, IOException, XmlPullParserException {
		SoapObject request = LevelRequest(SUBMIT_LEVEL_VOTE, levelID, userName, password);
		request.addProperty("vote0", like + "");
		request.addProperty("vote1", difficulty+"");

		soapEnvelop.setOutputSoapObject(request);
		transport.call(action(SUBMIT_LEVEL_VOTE), soapEnvelop);

		SoapPrimitive response = prim(soapEnvelop.getResponse());
		return response != null;
	}
	
	static String str(Object obj){
		return obj == null
			? null
			: obj.toString();
	}
	
	static Puzzle parseLevelEx(SoapObject input) throws ParseException{
		Puzzle result = new Puzzle();
		result.about = str(input.getProperty(0));
		result.allowedCommands = int32(input.getProperty(1));
		result.setColors(stringArray(input.getProperty(2)));
		result.commentCount = int32(input.getProperty(3));
		int votes = int32(input.getProperty(4));
		result.difficulty = votes > 0
			? int32(input.getProperty(5)) * 20 / votes
			: 0;
		result.disliked = int32(input.getProperty(6));
		result.featured = bool(input.getProperty(7));
		result.id = int32(input.getProperty(8));
		result.setItems(stringArray(input.getProperty(9)));
		result.liked = int32(input.getProperty(10));
		result.robotCol = int32(input.getProperty(11));
		result.robotDir = int32(input.getProperty(12));
		result.robotRow = int32(input.getProperty(13));
		result.solutions = int32(input.getProperty(14));
		result.setFunctionLengths(intArray(input.getProperty(15)));
		result.submittedBy = str(input.getProperty(16));
		result.submittedDate = date(input.getProperty(17));
		result.title = str(input.getProperty(18));
		return result;
	}
	
	static Date date(Object prim) throws ParseException{
		return ISO8601DateTimeParser.parse(prim + "");
	}
	
	static boolean bool(Object prim){
		return Boolean.parseBoolean(prim.toString());
	}
	
	static int int32(Object prim){
		return int32((SoapPrimitive)prim);
	}
	
	static String[] stringArray(Object obj){
		return stringArray((SoapObject)obj);
	}
	
	static int[] intArray(Object obj){
		return int32Array((SoapObject)obj);
	}

	LevelInfo[] parseLevels() {
		SoapObject levels = obj(obj(soapEnvelop.bodyIn).getProperty(0));
		LevelInfo[] result = new LevelInfo[levels.getPropertyCount()];
		for(int i = result.length - 1; i >=0; i++){
			SoapObject level = obj(levels.getProperty(i));
			LevelInfo levelInfo = new LevelInfo(
					int32(prim(level.getProperty(0))),
					level.getProperty(1).toString(),
					level.getProperty(2).toString(),
					
					int32Array(obj(level.getProperty(3))),
					stringArray(obj(level.getProperty(4))),
					stringArray(obj(level.getProperty(5))),
					
					int32(prim(level.getProperty(6))),
					int32(prim(level.getProperty(7))),
					int32(prim(level.getProperty(8))),
					
					int32(prim(level.getProperty(9))),
					int32(prim(level.getProperty(10))),
					int32(prim(level.getProperty(11))),
					
					int32(prim(level.getProperty(12))),
					int32(prim(level.getProperty(13))),
					level.getProperty(14).toString(),
					new Date(Date.parse(level.getProperty(15).toString())),
					
					Boolean.parseBoolean(level.getProperty(16).toString()),
					int32(prim(level.getProperty(17))));
			
			result[i] = levelInfo;
		}
		
		return result;
	}
	
	public static class LevelVoteInfo{
		public final int levelID;
		public final String userName;
		public final int vote;
		public final int voteKind;
		
		public LevelVoteInfo(int levelID, String userName, int vote, int voteKind){
			this.levelID = levelID;
			this.userName = userName;
			this.vote = vote;
			this.voteKind = voteKind;
		}
		
		@Override
		public String toString() {
			return "Level: " + levelID + " Vote: " + vote;
		}
	}
	
	public static class LevelInfo{
		public final int id;
		public final String title;
		public final String about;
		
		public final int[] subLengths;
		public final String[] colors;
		public final String[] items;
		
		public final int robotX;
		public final int robotY;
		public final int robotDir;
		
		public final int solutions;
		public final int difficultySum;
		public final int difficultyVoteCount;
		
		public final int liked;
		public final int disliked;
		public final String author;
		public final Date submitted;
		
		public final boolean featured;
		public final int allowedCommands;
		
		public LevelInfo(int id, String title, String about,
				int[] sublengths, String[] colors, String[] items,
				int robotX, int robotY, int robotDir,
				int solutions, int difficultySum, int difficultyVoteCount,
				int liked, int disliked, String author, Date submitted,
				boolean featured, int allowedCommands){
			this.id = id;
			this.title = title;
			this.about = about;
			
			this.subLengths = sublengths.clone();
			this.colors = colors.clone();
			this.items = items.clone();
			
			this.robotX = robotX;
			this.robotY = robotY;
			this.robotDir = robotDir;
			
			this.solutions = solutions;
			this.difficultySum = difficultySum;
			this.difficultyVoteCount = difficultyVoteCount;
			
			this.liked = liked;
			this.disliked = disliked;
			this.author = author;
			this.submitted = submitted;
			
			this.featured = featured;
			this.allowedCommands = allowedCommands;
		}
	}
	
	static SoapObject obj(Object object){
		return (SoapObject)object;
	}
	
	static SoapPrimitive prim(Object object){
		return (SoapPrimitive)object;
	}
	
	static int int32(SoapPrimitive primitive){
		return Integer.parseInt(primitive.toString());
	}
	
	static int[] int32Array(SoapObject obj){
		int[] result = new int[obj.getPropertyCount()];
		for(int i = result.length - 1; i >= 0; i--)
			result[i] = int32(prim(obj.getProperty(i)));
		return result;
	}
	
	static String[] stringArray(SoapObject obj){
		String[] result = new String[obj.getPropertyCount()];
		for(int i = result.length - 1; i >= 0; i--)
			result[i] = obj.getProperty(i).toString();
		return result;
	}

	static final String salt = "5A6fKpgSnXoMpxbcHcb7"; 
	public static String computeHash(String message)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest sha1 = MessageDigest.getInstance(MGF1ParameterSpec.SHA1
				.getDigestAlgorithm());
		message += salt;
		byte[] input = message.getBytes("UTF-8");
		sha1.update(input);
		byte[] hash = sha1.digest();
		return getHex(hash);
	}

	static final String HEXES = "0123456789ABCDEF";

	static String getHex(byte[] raw) {
		if (raw == null) {
			return null;
		}
		final StringBuilder hex = new StringBuilder(2 * raw.length);
		for (final byte b : raw) {
			hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(
					HEXES.charAt((b & 0x0F)));
		}
		return hex.toString();
	}

	public static class SolversResult implements Comparable<SolversResult> {
		public final String user;
		public final int solved;

		public SolversResult(String user, int solved) {
			this.user = user;
			this.solved = solved;
		}

		public SolversResult(SoapPrimitive user, SoapPrimitive solved) {
			this.user = user.toString();
			this.solved = int32(solved);
		}

		public int compareTo(SolversResult another) {
			if (another == null)
				return 1;
			else if (this.solved > another.solved)
				return 1;
			else if (this.solved < another.solved)
				return -1;
			else
				return this.user.compareTo(another.user);
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof SolversResult))
				return false;
			SolversResult another = (SolversResult) o;
			return compareTo(another) == 0;
		}

		@Override
		public String toString() {
			return this.user + ": " + this.solved;
		}
	}
}
